package co.edu.unal.apuntado.domain.wager;

import co.edu.unal.apuntado.infrastructure.persistence.IMatchState;

/**
 * Stub de Fase 1 (Tabla 10b): apuesta 0 por defecto; debita apuestas y acredita
 * el pozo con fichas virtuales (RD3).
 */
public final class WagerAndEconomyComponent implements IEconomia {

    private final IMatchState estado;

    public WagerAndEconomyComponent(IMatchState estado) {
        this.estado = estado;
    }

    @Override
    public boolean puedeApostar(String jugador, int apuesta) {
        return estado.saldoDe(jugador) >= apuesta;
    }

    @Override
    public void debitarApuesta(String jugador, String partida, int apuesta) {
        if (apuesta > 0) {
            estado.registrarMovimiento(jugador, partida, "APUESTA", -apuesta);
        }
    }

    @Override
    public void acreditarPozo(String ganador, String partida, int pozo) {
        if (pozo > 0) {
            estado.registrarMovimiento(ganador, partida, "POZO", pozo);
        }
    }
}
