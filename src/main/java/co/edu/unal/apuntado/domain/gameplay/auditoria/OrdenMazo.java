package co.edu.unal.apuntado.domain.gameplay.auditoria;

import java.util.List;

/**
 * Orden secreto del mazo. Solo DeckManagement y DealAudit lo conocen (ADR-03,
 * E6, FF-05): nunca sale del servidor antes de la revelación.
 */
public record OrdenMazo(List<String> codigos) {
    public OrdenMazo {
        codigos = List.copyOf(codigos);
    }

    String serializado() {
        return String.join(",", codigos);
    }
}
