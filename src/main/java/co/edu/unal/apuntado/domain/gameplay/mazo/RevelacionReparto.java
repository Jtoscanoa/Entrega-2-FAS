package co.edu.unal.apuntado.domain.gameplay.mazo;

import java.util.List;

/** Vista pública de la revelación: orden, sal y huella (E6). */
public record RevelacionReparto(List<String> orden, String sal, String huella) {
    public RevelacionReparto {
        orden = List.copyOf(orden);
    }
}
