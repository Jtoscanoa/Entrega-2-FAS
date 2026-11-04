package co.edu.unal.apuntado.domain.gameplay;

import co.edu.unal.apuntado.domain.gameplay.bot.DecisionBot;
import co.edu.unal.apuntado.domain.gameplay.bot.IBot;
import co.edu.unal.apuntado.domain.gameplay.mazo.IMazo;
import co.edu.unal.apuntado.domain.gameplay.mazo.RevelacionReparto;
import co.edu.unal.apuntado.domain.rules.IConfiguracion;
import co.edu.unal.apuntado.domain.rules.IEvaluadorMano;
import co.edu.unal.apuntado.domain.rules.ILiquidacion;
import co.edu.unal.apuntado.domain.rules.ResultadoLiquidacion;
import co.edu.unal.apuntado.domain.rules.SolicitudLiquidacion;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;
import co.edu.unal.apuntado.domain.wager.IEconomia;
import co.edu.unal.apuntado.infrastructure.persistence.IMatchState;
import co.edu.unal.apuntado.infrastructure.persistence.RegistroPartida;
import co.edu.unal.apuntado.infrastructure.persistence.RegistroRonda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Mediador estricto (Tabla 3b): controla la secuencia del turno y la invocación
 * de interfaces. No evalúa manos ni calcula puntos (FF-01c). Ce = 7.
 */
public final class TurnOrchestratorComponent implements ITurno, IEventosPartida {

    private final IMazo mazo;
    private final IBot bot;
    private final IEvaluadorMano evaluador;
    private final ILiquidacion liquidacion;
    private final IConfiguracion configuracion;
    private final IEconomia economia;
    private final IMatchState estado;
    private final Map<String, Mesa> mesas = new ConcurrentHashMap<>();
    private final List<Consumer<EventoPartida>> oyentes = new CopyOnWriteArrayList<>();

    public TurnOrchestratorComponent(IMazo mazo, IBot bot, IEvaluadorMano evaluador, ILiquidacion liquidacion,
                                     IConfiguracion configuracion, IEconomia economia, IMatchState estado) {
        this.mazo = mazo;
        this.bot = bot;
        this.evaluador = evaluador;
        this.liquidacion = liquidacion;
        this.configuracion = configuracion;
        this.economia = economia;
        this.estado = estado;
    }

    @Override
    public void suscribir(Consumer<EventoPartida> oyente) {
        oyentes.add(oyente);
    }

    // ------------------------------------------------------------------ inicio

    @Override
    public void iniciarPartida(String partida, String sala, List<String> jugadores) {
        ConfiguracionReglas cfg = configuracion.obtener(sala);
        configuracion.validarMesa(cfg, jugadores.size());
        Mesa mesa = new Mesa(partida, cfg, jugadores);
        mesas.put(partida, mesa);
        conCandado(mesa, () -> {
            jugadores.forEach(j -> economia.debitarApuesta(j, partida, cfg.apuesta()));
            estado.guardarPartida(new RegistroPartida(partida, sala, cfg.modalidad(), jugadores));
            nuevaRonda(mesa);
            return null;
        });
    }

    private void nuevaRonda(Mesa mesa) {
        mesa.numeroRonda++;
        mesa.huella = mazo.nuevaRonda(mesa.ronda(), mesa.cfg);
        estado.registrarInicioRonda(mesa.id, mesa.numeroRonda, mesa.huella);
        publicar(mesa, "RondaIniciada", null, Map.of("huella", mesa.huella));
        List<List<Carta>> manos = mazo.repartir(mesa.ronda(), mesa.jugadores.size());
        for (int i = 0; i < manos.size(); i++) {
            String j = mesa.jugadores.get((mesa.indiceTurno + i) % mesa.jugadores.size());
            mesa.manos.put(j, new ArrayList<>(manos.get(i)));
            publicar(mesa, "ManoEntregada", j, Map.of("cartas", codigos(manos.get(i))));
        }
        mesa.robo = true; // quien abre recibe 11 cartas e inicia descartando (RF03)
        publicar(mesa, "TurnoAsignado", null, Map.of("jugador", mesa.turnoDe(), "numeroTurno", mesa.numeroTurno));
    }

    // ---------------------------------------------------------------- comandos

    @Override
    public ResultadoJugada robar(String partida, String jugador, int numeroTurno) {
        Mesa mesa = mesa(partida);
        return conCandado(mesa, () -> {
            String motivo = validarTurno(mesa, jugador, numeroTurno);
            if (motivo != null || mesa.robo) {
                return ResultadoJugada.rechazo(motivo != null ? motivo : "YA_ROBO");
            }
            Carta carta = mazo.robar(mesa.ronda());
            mesa.manoDe(jugador).add(carta);
            mesa.robo = true;
            publicar(mesa, "CartaRecibida", jugador, Map.of("carta", carta.codigo()));
            publicar(mesa, "CartaRobada", null, Map.of("jugador", jugador));
            return ResultadoJugada.OK;
        });
    }

    @Override
    public ResultadoJugada descartar(String partida, String jugador, String carta, int numeroTurno) {
        Mesa mesa = mesa(partida);
        return conCandado(mesa, () -> {
            String motivo = validarJugada(mesa, jugador, carta, numeroTurno);
            if (motivo != null) {
                return ResultadoJugada.rechazo(motivo);
            }
            aplicarDescarte(mesa, jugador, Carta.of(carta));
            return ResultadoJugada.OK;
        });
    }

    private void aplicarDescarte(Mesa mesa, String jugador, Carta carta) {
        mesa.manoDe(jugador).remove(carta);
        mazo.descartar(mesa.ronda(), carta);
        mesa.siguienteTurno();
        publicar(mesa, "CartaDescartada", null, Map.of("jugador", jugador, "carta", carta.codigo()));
        publicar(mesa, "TurnoAsignado", null, Map.of("jugador", mesa.turnoDe(), "numeroTurno", mesa.numeroTurno));
    }

    @Override
    public ResultadoJugada cerrar(String partida, String jugador, String descarte, int numeroTurno) {
        Mesa mesa = mesa(partida);
        return conCandado(mesa, () -> {
            String motivo = validarJugada(mesa, jugador, descarte, numeroTurno);
            if (motivo != null) {
                return ResultadoJugada.rechazo(motivo);
            }
            List<Carta> resto = new ArrayList<>(mesa.manoDe(jugador));
            resto.remove(Carta.of(descarte));
            ResultadoValidacion r = evaluador.validarCierre(resto, mesa.cfg);
            if (!r.aceptado()) {
                publicar(mesa, "CierreRechazado", jugador, Map.of("motivo", r.motivo()));
                return ResultadoJugada.rechazo(r.motivo());
            }
            return liquidarRonda(mesa, jugador, r);
        });
    }

    /** Liquida, revela, persiste en una transacción y solo después actualiza la memoria y difunde. */
    private ResultadoJugada liquidarRonda(Mesa mesa, String cerrador, ResultadoValidacion cierre) {
        Map<String, Integer> perdedores = new HashMap<>();
        mesa.jugadores.stream().filter(j -> !j.equals(cerrador)).forEach(j ->
                perdedores.put(j, evaluador.calcularCartasSueltas(mesa.manoDe(j), mesa.cfg).puntos()));
        ResultadoLiquidacion liq = liquidacion.liquidar(new SolicitudLiquidacion(mesa.marcador, cerrador,
                cierre.puntos(), perdedores, mesa.cfg.umbralEliminacion()));
        RevelacionReparto rev = mazo.revelar(mesa.ronda());
        boolean escrito = estado.guardarRonda(new RegistroRonda(mesa.id, mesa.numeroRonda, cerrador,
                cierre.tipo().name(), liq.puntosRonda(), liq.marcador(), String.join(",", rev.orden()), rev.sal()));
        if (!escrito) {
            return ResultadoJugada.rechazo("RONDA_YA_LIQUIDADA");
        }
        mesa.marcador.putAll(liq.marcador());
        mesa.numeroTurno++;
        publicar(mesa, "RondaLiquidada", null, Map.of("puntos", liq.puntosRonda(), "marcador", liq.marcador(),
                "orden", rev.orden(), "sal", rev.sal()));
        return ResultadoJugada.OK;
    }

    @Override
    public boolean vencerTurno(String partida, int numeroTurno) {
        Mesa mesa = mesa(partida);
        return conCandado(mesa, () -> {
            if (numeroTurno != mesa.numeroTurno) {
                return false; // vencimiento obsoleto: sin efecto (ADR-08, FF-17)
            }
            String jugador = mesa.turnoDe();
            if (!mesa.robo) {
                mesa.manoDe(jugador).add(mazo.robar(mesa.ronda()));
            }
            DecisionBot d = bot.decidir(mesa.manoDe(jugador), mesa.cfg, false);
            aplicarDescarte(mesa, jugador, d.descarte());
            return true;
        });
    }

    @Override
    public VistaPartida vistaDe(String partida, String jugador) {
        Mesa mesa = mesa(partida);
        return conCandado(mesa, () -> {
            Carta cima = mazo.cimaDescarte(mesa.ronda());
            return new VistaPartida(codigos(mesa.manoDe(jugador)), cima == null ? null : cima.codigo(),
                    mesa.marcador, mesa.turnoDe(), mesa.numeroTurno, mesa.huella);
        });
    }

    // --------------------------------------------------------------- utilidades

    private static String validarTurno(Mesa mesa, String jugador, int numeroTurno) {
        if (numeroTurno != mesa.numeroTurno) {
            return "NUMERO_DE_TURNO_OBSOLETO";
        }
        return jugador.equals(mesa.turnoDe()) ? null : "NO_ES_SU_TURNO";
    }

    private static String validarJugada(Mesa mesa, String jugador, String carta, int numeroTurno) {
        String motivo = validarTurno(mesa, jugador, numeroTurno);
        if (motivo != null) {
            return motivo;
        }
        if (!mesa.robo) {
            return "DEBE_ROBAR";
        }
        return mesa.manoDe(jugador).contains(Carta.of(carta)) ? null : "CARTA_NO_ESTA_EN_LA_MANO";
    }

    private <T> T conCandado(Mesa mesa, Supplier<T> accion) {
        mesa.candado.lock();
        try {
            return accion.get();
        } finally {
            mesa.candado.unlock();
        }
    }

    private void publicar(Mesa mesa, String tipo, String destinatario, Map<String, Object> datos) {
        EventoPartida e = new EventoPartida(mesa.id, tipo, destinatario, datos);
        oyentes.forEach(o -> o.accept(e));
    }

    private static List<String> codigos(List<Carta> cartas) {
        return cartas.stream().map(Carta::codigo).toList();
    }

    private Mesa mesa(String partida) {
        Mesa m = mesas.get(partida);
        if (m == null) {
            throw new IllegalArgumentException("Partida desconocida: " + partida);
        }
        return m;
    }
}
