package co.edu.unal.apuntado.domain.rules;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Único responsable de aplicar los puntos de la ronda, determinar eliminaciones
 * y calcular el puntaje de reenganche (RF07, RF08); no mueve fichas.
 */
public final class RoundScoringComponent implements ILiquidacion {

    @Override
    public ResultadoLiquidacion liquidar(SolicitudLiquidacion s) {
        Map<String, Integer> ronda = new HashMap<>(s.puntosPerdedores());
        ronda.put(s.cerrador(), s.puntosCerrador());
        Map<String, Integer> marcador = new HashMap<>();
        Set<String> eliminados = new HashSet<>();
        ronda.forEach((jugador, puntos) -> {
            int total = s.marcadorPrevio().getOrDefault(jugador, 0) + puntos;
            marcador.put(jugador, total);
            if (total >= s.umbral()) {
                eliminados.add(jugador);
            }
        });
        return new ResultadoLiquidacion(ronda, marcador, eliminados, ganador(ronda, eliminados));
    }

    /**
     * Gana el único activo que queda; si todos superan el umbral a la vez, gana el
     * menor marcador de la ronda (Tabla 4).
     */
    private static String ganador(Map<String, Integer> ronda, Set<String> eliminados) {
        Set<String> activos = new HashSet<>(ronda.keySet());
        activos.removeAll(eliminados);
        if (activos.size() == 1) {
            return activos.iterator().next();
        }
        if (activos.isEmpty()) {
            return ronda.entrySet().stream().min(Map.Entry.comparingByValue()).orElseThrow().getKey();
        }
        return null;
    }

    @Override
    public int puntajeReingreso(Map<String, Integer> marcadorActivos) {
        return marcadorActivos.values().stream().max(Comparator.naturalOrder()).orElse(0);
    }
}
