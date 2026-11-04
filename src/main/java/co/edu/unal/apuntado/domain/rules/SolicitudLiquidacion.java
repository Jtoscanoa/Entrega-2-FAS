package co.edu.unal.apuntado.domain.rules;

import java.util.Map;

/**
 * Datos de una liquidación. El orquestador entrega puntos ya calculados por el
 * evaluador: RoundScoring no consulta la partida (Ley de Deméter, E1 §4.5).
 */
public record SolicitudLiquidacion(Map<String, Integer> marcadorPrevio, String cerrador, int puntosCerrador,
                                   Map<String, Integer> puntosPerdedores, int umbral) {
    public SolicitudLiquidacion {
        marcadorPrevio = Map.copyOf(marcadorPrevio);
        puntosPerdedores = Map.copyOf(puntosPerdedores);
    }
}
