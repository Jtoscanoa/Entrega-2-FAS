# ADR-09: Despliegue

## Estado

Accepted (Aceptado).

## Contexto

- La Figura 7 materializa el cuanto único. El navegador descarga la SPA y abre un WebSocket seguro (STOMP 1.2 sobre WSS 443, con latido cada 10 s) contra una sola instancia del contenedor, donde corre `apuntado-server.jar` (Java 21, Spring Boot 3), que también sirve la SPA (E4, E7). (E2, §6.1)
- El servidor habla con PostgreSQL 16 gestionado en la misma región por JDBC con TLS y respaldo diario (E5, RF13), y con el proveedor OIDC solo para descargar las llaves públicas, que guarda en caché. (E2, §6.1)

## Decisión

Una imagen Docker (Spring Boot también sirve la SPA) en un PaaS con WebSocket y PostgreSQL gestionado; se despliega solo con el pipeline en verde y entre partidas.

### Alternativas descartadas

- Máquina virtual propia (más operación).
- Kubernetes (desproporcionado para RD2).
- Funciones serverless (sin WebSocket ni estado).

## Consecuencias

- GitHub Actions publica la imagen en ghcr.io únicamente si las fitness functions están en verde. (E2, §6.1)
- Compensaciones registradas en la Tabla 12 del E2:

| Decisión | Se gana | Se cede | Se revisa si… |
|---|---|---|---|
| Monolito modular (ADR-04) | Testability, Reliability y simplicidad (RD2) | Despliegue independiente y escalado horizontal | Se cumple un criterio de migración (§1.1) |
| Variantes compiladas (ADR-04) | Contrato simple, sin cargador dinámico | Cada variante exige redespliegue | Se piden variantes sin desplegar |
| Estado en memoria y persistencia por ronda (ADR-05) | Latencia (E4) y una sola transacción (E5) | La ronda en curso se pierde si el servidor cae (R3) | Las fichas adquieren valor real |
| Un candado por partida (ADR-06) | Consistencia sin carreras (R10) | Una partida no se reparte entre instancias | Scalability pasa a ser driver |


## Cumplimiento (Compliance)

Etapa de despliegue condicionada (Figura 6 del E2) y health check.

## Notas

- Autor: Duvan Steven Calderón Taborda. Fecha de aprobación: octubre de 2026. Revisores: Juan Pablo Mazo Berrío, Jose Mauricio Toscano Aguas, Duvan Steven Calderón Taborda (consenso unánime, 3 de 3).
- Fuente: Entregable 2, Tabla 8 (resumen); contexto y consecuencias tomados de §6.1 y la Tabla 12.
- ADR relacionados: [ADR-04](./ADR-04-estilo-microkernel.md) (cuanto único), [ADR-05](./ADR-05-persistencia-postgresql.md) (PostgreSQL gestionado), [ADR-06](./ADR-06-sincronizacion-stomp.md) (WebSocket con STOMP), [ADR-07](./ADR-07-autenticacion-oidc.md) (proveedor OIDC).
