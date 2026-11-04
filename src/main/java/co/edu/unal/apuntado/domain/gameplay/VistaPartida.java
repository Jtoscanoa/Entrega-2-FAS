package co.edu.unal.apuntado.domain.gameplay;

import java.util.List;
import java.util.Map;

/** Vista personalizada de la mesa: nunca incluye manos ajenas ni el orden del mazo. */
public record VistaPartida(List<String> mano, String cima, Map<String, Integer> marcador, String turnoDe,
                           int numeroTurno, String huella) {
    public VistaPartida {
        mano = List.copyOf(mano);
        marcador = Map.copyOf(marcador);
    }
}
