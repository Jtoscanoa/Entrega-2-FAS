package co.edu.unal.apuntado.domain.lobby;

import co.edu.unal.apuntado.domain.gameplay.ITurno;
import co.edu.unal.apuntado.domain.rules.IConfiguracion;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.wager.IEconomia;
import co.edu.unal.apuntado.infrastructure.persistence.IMatchState;
import co.edu.unal.apuntado.infrastructure.persistence.RegistroPartida;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Crea salas, gestiona invitaciones, incorpora jugadores e inicia la partida (RF01). */
public final class LobbyPartyComponent implements ILobby {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CUPO = 6;
    private final SecureRandom azar = new SecureRandom();
    private final ITurno turno;
    private final IConfiguracion configuracion;
    private final IEconomia economia;
    private final IMatchState estado;
    private final Map<String, Sala> salas = new ConcurrentHashMap<>();

    public LobbyPartyComponent(ITurno turno, IConfiguracion configuracion, IEconomia economia, IMatchState estado) {
        this.turno = turno;
        this.configuracion = configuracion;
        this.economia = economia;
        this.estado = estado;
    }

    /** Sala privada con su anfitrión y sus asientos. */
    private record Sala(String codigo, String anfitrion, List<String> asientos) {
    }

    @Override
    public String crearSala(String anfitrion, ConfiguracionReglas cfg) {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            codigo.append(ALFABETO.charAt(azar.nextInt(ALFABETO.length())));
        }
        String c = codigo.toString();
        configuracion.registrar(c, cfg);
        salas.put(c, new Sala(c, anfitrion, new ArrayList<>(List.of(anfitrion))));
        return c;
    }

    @Override
    public synchronized int unirse(String codigo, String jugador) {
        Sala sala = sala(codigo);
        int apuesta = configuracion.obtener(codigo).apuesta();
        if (sala.asientos().size() >= CUPO || !economia.puedeApostar(jugador, apuesta)) {
            throw new IllegalStateException("Sin cupo o saldo insuficiente");
        }
        sala.asientos().add(jugador);
        return sala.asientos().size();
    }

    @Override
    public String iniciar(String codigo, String anfitrion) {
        Sala sala = sala(codigo);
        if (!sala.anfitrion().equals(anfitrion)) {
            throw new IllegalStateException("Solo el anfitrión inicia la partida");
        }
        String partida = codigo + "-P1";
        estado.guardarPartida(new RegistroPartida(partida, codigo,
                configuracion.obtener(codigo).modalidad(), sala.asientos()));
        turno.iniciarPartida(partida, codigo, sala.asientos());
        return partida;
    }

    private Sala sala(String codigo) {
        Sala s = salas.get(codigo);
        if (s == null) {
            throw new IllegalArgumentException("Sala inexistente: " + codigo);
        }
        return s;
    }
}
