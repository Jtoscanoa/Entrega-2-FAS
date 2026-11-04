package co.edu.unal.apuntado.domain.wager;

/** Interfaz provista por WagerAndEconomyComponent. */
public interface IEconomia {
    boolean puedeApostar(String jugador, int apuesta);

    void debitarApuesta(String jugador, String partida, int apuesta);

    void acreditarPozo(String ganador, String partida, int pozo);
}
