package co.edu.unal.apuntado.infrastructure.realtime;

import co.edu.unal.apuntado.domain.gameplay.ResultadoJugada;
import co.edu.unal.apuntado.domain.gameplay.VistaPartida;

import java.util.function.Consumer;

/** Interfaz provista por SessionGatewayComponent a la UI (REST + STOMP en el E3). */
public interface ISesion {
    void conectar(String jugador, String partida, Consumer<String> canal);

    void desconectar(String jugador);

    int unirse(String codigo, String jugador);

    ResultadoJugada robar(String partida, String jugador, int numeroTurno);

    ResultadoJugada descartar(String partida, String jugador, String carta, int numeroTurno);

    ResultadoJugada cerrar(String partida, String jugador, String descarte, int numeroTurno);

    VistaPartida reanudar(String partida, String jugador);
}
