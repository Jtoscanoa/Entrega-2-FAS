package co.edu.unal.apuntado.infrastructure.realtime;

import co.edu.unal.apuntado.domain.gameplay.EventoPartida;
import co.edu.unal.apuntado.domain.gameplay.IEventosPartida;
import co.edu.unal.apuntado.domain.gameplay.ITurno;
import co.edu.unal.apuntado.domain.gameplay.ResultadoJugada;
import co.edu.unal.apuntado.domain.gameplay.VistaPartida;
import co.edu.unal.apuntado.domain.lobby.ILobby;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Adaptador de tiempo real sin reglas (RF17). Solo usa ILobby, ITurno e
 * IEventosPartida (FF-06) y nunca toca la persistencia (FF-07). Entrega a cada
 * cliente solo su vista: los eventos privados van a su dueño.
 */
public final class SessionGatewayComponent implements ISesion {

    private final ILobby lobby;
    private final ITurno turno;
    private final Map<String, Conexion> conexiones = new ConcurrentHashMap<>();

    /** Canal abierto de un jugador en una partida. */
    private record Conexion(String partida, Consumer<String> canal) {
    }

    public SessionGatewayComponent(ILobby lobby, ITurno turno, IEventosPartida eventos) {
        this.lobby = lobby;
        this.turno = turno;
        eventos.suscribir(this::entregar);
    }

    private void entregar(EventoPartida e) {
        conexiones.forEach((jugador, c) -> {
            boolean mismaPartida = c.partida().equals(e.partida());
            boolean visible = e.destinatario() == null || e.destinatario().equals(jugador);
            if (mismaPartida && visible) {
                c.canal().accept(e.tipo() + " " + e.datos());
            }
        });
    }

    @Override
    public void conectar(String jugador, String partida, Consumer<String> canal) {
        conexiones.put(jugador, new Conexion(partida, canal));
    }

    @Override
    public void desconectar(String jugador) {
        conexiones.remove(jugador); // la mesa sigue en memoria (E7)
    }

    @Override
    public int unirse(String codigo, String jugador) {
        return lobby.unirse(codigo, jugador);
    }

    @Override
    public ResultadoJugada robar(String partida, String jugador, int numeroTurno) {
        return turno.robar(partida, jugador, numeroTurno);
    }

    @Override
    public ResultadoJugada descartar(String partida, String jugador, String carta, int numeroTurno) {
        return turno.descartar(partida, jugador, carta, numeroTurno);
    }

    @Override
    public ResultadoJugada cerrar(String partida, String jugador, String descarte, int numeroTurno) {
        return turno.cerrar(partida, jugador, descarte, numeroTurno);
    }

    @Override
    public VistaPartida reanudar(String partida, String jugador) {
        return turno.vistaDe(partida, jugador);
    }
}
