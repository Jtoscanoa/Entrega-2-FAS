# ADR-07: Autenticación OIDC

## Estado

Accepted (Aceptado).

## Contexto

- Número de cuantos: 1: un único artefacto desplegable con su base de datos y comunicación síncrona interna. El proveedor OIDC queda fuera del cuanto porque el servidor valida los tokens con las llaves públicas en caché y no lo invoca en cada jugada. (E2, §1.1)
- El navegador descarga la SPA y abre un WebSocket seguro (STOMP 1.2 sobre WSS 443, con latido cada 10 s) contra una sola instancia del contenedor. (E2, §6.1)

## Decisión

OIDC con código de autorización + PKCE (Sakimura et al., 2015) desde la SPA; el servidor valida el JWT con las llaves públicas (JWKS) en caché, también en el CONNECT de STOMP; los bots no tienen identidad.

### Alternativas descartadas

- Usuarios y contraseñas propios (gestionar credenciales).
- Invitados anónimos (sin saldo ni reconexión).

## Consecuencias

- El servidor habla con el proveedor OIDC solo para descargar las llaves públicas, que guarda en caché. (E2, §6.1)
- El proveedor OIDC queda fuera del cuanto: el servidor no lo invoca en cada jugada. (E2, §1.1)

## Cumplimiento (Compliance)

FF-18: un CONNECT sin token o con token vencido se rechaza (E3).

## Notas

- Autor: Juan Pablo Mazo Berrío. Fecha de aprobación: octubre de 2026. Revisores: Juan Pablo Mazo Berrío, Jose Mauricio Toscano Aguas, Duvan Steven Calderón Taborda (consenso unánime, 3 de 3).
- Fuente: Entregable 2, Tabla 8 (resumen); contexto y consecuencias tomados de §1.1 y §6.1.
- ADR relacionados: [ADR-04](./ADR-04-estilo-microkernel.md) (cuanto único), [ADR-06](./ADR-06-sincronizacion-stomp.md) (CONNECT de STOMP), [ADR-09](./ADR-09-despliegue.md) (topología de despliegue).
