package co.edu.unal.apuntado.infrastructure.persistence;

import java.util.Map;

/** Todo lo que escribe la liquidación de una ronda (§2.4): marcador, puntos y revelación. */
public record RegistroRonda(String partida, int numero, String cerrador, String tipoCierre,
                            Map<String, Integer> puntosRonda, Map<String, Integer> marcador,
                            String ordenRevelado, String sal) {
    public RegistroRonda {
        puntosRonda = Map.copyOf(puntosRonda);
        marcador = Map.copyOf(marcador);
    }
}
