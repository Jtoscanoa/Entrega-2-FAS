package co.edu.unal.apuntado.domain.rules;

import java.util.Map;

/** Interfaz provista por RoundScoringComponent. */
public interface ILiquidacion {
    ResultadoLiquidacion liquidar(SolicitudLiquidacion solicitud);

    int puntajeReingreso(Map<String, Integer> marcadorActivos);
}
