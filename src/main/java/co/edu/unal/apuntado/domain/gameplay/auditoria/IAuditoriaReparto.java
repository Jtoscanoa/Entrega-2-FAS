package co.edu.unal.apuntado.domain.gameplay.auditoria;

/** Interfaz provista por DealAuditComponent (commit-reveal, ADR-03). */
public interface IAuditoriaReparto {
    /** Sella el orden y devuelve la huella SHA-256 que se publica antes de repartir. */
    String sellar(String ronda, OrdenMazo orden);

    Revelacion revelar(String ronda);
}
