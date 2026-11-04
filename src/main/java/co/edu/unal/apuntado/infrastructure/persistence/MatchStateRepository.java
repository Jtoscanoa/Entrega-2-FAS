package co.edu.unal.apuntado.infrastructure.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Esqueleto del E2: guarda en memoria con la misma semántica atómica que tendrá
 * la versión JDBC sobre PostgreSQL del E3 (schema en docs/datos/schema.sql).
 */
public final class MatchStateRepository implements IMatchState {

    private static final int SALDO_INICIAL = 1000;
    private final Map<String, RegistroPartida> partidas = new ConcurrentHashMap<>();
    private final Map<String, String> rondas = new ConcurrentHashMap<>();
    private final Map<String, RegistroRonda> liquidadas = new ConcurrentHashMap<>();
    private final Map<String, Integer> saldos = new ConcurrentHashMap<>();

    @Override
    public void guardarPartida(RegistroPartida partida) {
        partidas.put(partida.id(), partida);
    }

    @Override
    public void registrarInicioRonda(String partida, int numero, String huella) {
        rondas.put(clave(partida, numero), huella);
    }

    @Override
    public boolean guardarRonda(RegistroRonda ronda) {
        return liquidadas.putIfAbsent(clave(ronda.partida(), ronda.numero()), ronda) == null;
    }

    @Override
    public int saldoDe(String jugador) {
        return saldos.getOrDefault(jugador, SALDO_INICIAL);
    }

    @Override
    public void registrarMovimiento(String jugador, String partida, String tipo, int monto) {
        saldos.merge(jugador, SALDO_INICIAL + monto, (actual, nuevo) -> actual + monto);
    }

    public int rondasLiquidadas() {
        return liquidadas.size();
    }

    private static String clave(String partida, int numero) {
        return partida + "#" + numero;
    }
}
