package co.edu.unal.apuntado.domain.gameplay;

import java.util.List;

/** Interfaz provista por TurnOrchestratorComponent. Cada comando lleva el número de turno. */
public interface ITurno {
    void iniciarPartida(String partida, String sala, List<String> jugadores);

    ResultadoJugada robar(String partida, String jugador, int numeroTurno);

    ResultadoJugada descartar(String partida, String jugador, String carta, int numeroTurno);

    ResultadoJugada cerrar(String partida, String jugador, String descarte, int numeroTurno);

    /** Vence el turno con su número; un vencimiento obsoleto no tiene efecto (ADR-08). */
    boolean vencerTurno(String partida, int numeroTurno);

    VistaPartida vistaDe(String partida, String jugador);
}
