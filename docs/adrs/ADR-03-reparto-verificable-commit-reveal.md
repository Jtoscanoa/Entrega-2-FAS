# ADR-03: Sellar cada mazo con una huella SHA-256 antes de repartir y revelar su orden al cerrar la ronda

## Estado

Accepted (Aceptado).

## Contexto

En la mesa física, quien baraja y reparte controla el orden del mazo y nadie puede comprobarlo; al pasar a la web, esa desconfianza se traslada al Anfitrión y al servidor. Se evaluaron: (a) barajar en el servidor sin evidencia; (b) barajar en el cliente del Anfitrión; (c) barajar entre todos los jugadores (barajado distribuido); (d) barajar en el servidor y comprometerse con una huella antes de repartir. (a) y (b) reproducen el problema; (c) es desproporcionada para el MVP.

## Decisión

Opción (d). DeckManagementComponent baraja con `SecureRandom` (Fisher–Yates); DealAuditComponent calcula SHA-256(orden + sal) con una sal aleatoria, publica la huella antes de repartir y guarda orden y sal. Al liquidar la ronda se revelan ambos y cada jugador puede recalcular la huella desde la vista "Verificar reparto". El Anfitrión nunca recibe el orden del mazo.

## Consecuencias

**Positivas:** el reparto es verificable por cualquier jugador, la organización deja de ser un punto de confianza y el costo es mínimo.

**Negativas:** la huella prueba que el orden no cambió, no que fuera aleatorio; el servidor sigue siendo una parte confiable, riesgo que se acepta en el MVP porque solo se usan fichas virtuales (RD3).

## Cumplimiento (Compliance)

Prueba automatizada que compara SHA-256(orden + sal) con la huella publicada; regla ArchUnit según la cual solo DeckManagementComponent y DealAuditComponent acceden al orden del mazo; prueba de contrato: ningún mensaje al cliente incluye el orden antes de la revelación.

## Notas

- Autor: Duvan Steven Calderón Taborda | Fecha de aprobación: 29/9/2026 | Revisores: Jose Mauricio Toscano Aguas y Juan Pablo Mazo Berrío
- Fuente: Entregable 1, §5.3.
- ADR relacionados: [ADR-01](./ADR-01-particionamiento-por-dominio.md) (DeckManagement y DealAudit en `domain.gameplay`), [ADR-05](./ADR-05-persistencia-postgresql.md) (el orden del mazo no se persiste antes de revelarse).
