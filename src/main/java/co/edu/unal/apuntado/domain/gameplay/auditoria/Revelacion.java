package co.edu.unal.apuntado.domain.gameplay.auditoria;

import java.util.List;

/** Orden y sal revelados al liquidar la ronda, junto con la huella publicada. */
public record Revelacion(List<String> orden, String sal, String huella) {
    public Revelacion {
        orden = List.copyOf(orden);
    }
}
