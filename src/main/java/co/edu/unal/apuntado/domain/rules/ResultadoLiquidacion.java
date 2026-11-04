package co.edu.unal.apuntado.domain.rules;

import java.util.Map;
import java.util.Set;

/** Resultado inmutable de una liquidación; el orquestador lo adopta sin recalcular. */
public record ResultadoLiquidacion(Map<String, Integer> puntosRonda, Map<String, Integer> marcador,
                                   Set<String> eliminados, String ganador) {
    public ResultadoLiquidacion {
        puntosRonda = Map.copyOf(puntosRonda);
        marcador = Map.copyOf(marcador);
        eliminados = Set.copyOf(eliminados);
    }
}
