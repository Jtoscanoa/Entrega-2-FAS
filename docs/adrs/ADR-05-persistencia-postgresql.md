# ADR-05: PostgreSQL relacional en 3FN, con el estado vivo en memoria y persistencia transaccional por ronda

## Estado

Accepted (Aceptado).

## Contexto

El Apuntado acumula penalizaciones durante N rondas hasta el umbral y la Clásica es atómica; hay que guardar salas, partidas, jugadores, manos, robos, descartes y marcadores, liquidar sin diferencias (E5) y consultar el historial (RF13). Alternativas (Tabla 5): (a) PostgreSQL en 3FN; (b) MongoDB con la partida como documento; (c) PostgreSQL con JSONB para el historial; (d) SQLite embebida. Momento de escritura: (e) en cada jugada; (f) al iniciar y al liquidar cada ronda. También hay que evitar que una caída del servidor durante la transición «Liquidando» deje una ronda calculada a medias y corrompa el marcador.

## Decisión

(a) y (f). Una base PostgreSQL 16 gestionada con las 13 tablas de la Figura 3, migraciones con Flyway y acceso exclusivo de MatchStateRepository. Al iniciar la ronda se escribe su huella; al liquidarla, todo lo demás en una sola transacción. El orden del mazo no se persiste antes de revelarse. «Liquidando» reside únicamente en memoria como estado de la FSM y no en la BD, donde la ronda transita directamente de `EN_CURSO` a `LIQUIDADA`; el `UPDATE` condicional sobre `EN_CURSO` que afecta 0 filas (cierre concurrente o reintento) hace rollback inmediato.

## Consecuencias

**Positivas:** la base impone la integridad (FK, CHECK, UNIQUE) además del código; una transacción cubre toda la liquidación; el historial se consulta con SQL.

**Negativas:** 13 entidades que mapear; una caída a mitad de ronda la anula (R3, aceptado porque las fichas son virtuales, RD3).

## Cumplimiento (Compliance)

- FF-08b (el repositorio no depende de ningún dominio).
- FF-16 (prueba de integración con Testcontainers que inyecta un fallo a mitad de la liquidación y exige 0 filas escritas).
- FF-17 (cierres concurrentes).
- Revisión de cada migración Flyway en el pull request.

## Notas

- Autor: Jose Mauricio Toscano Aguas. Fecha de aprobación: octubre de 2026. Revisores: Juan Pablo Mazo Berrío, Jose Mauricio Toscano Aguas, Duvan Steven Calderón Taborda (consenso unánime, 3 de 3).
- Fuente: Entregable 2, §3.2. Corresponde al ADR-04 (persistencia) de la guía del Entregable 2.
- ADR relacionados: [ADR-03](./ADR-03-reparto-verificable-commit-reveal.md) (huella y orden del mazo), [ADR-06](./ADR-06-sincronizacion-stomp.md) (eventos publicados tras el commit), [ADR-09](./ADR-09-despliegue.md) (PostgreSQL gestionado).
