# ADR-06: Comandos síncronos en memoria, eventos de dominio tras el commit y WebSocket con STOMP

## Estado

Accepted (Aceptado).

## Contexto

Hay dos niveles. Entre componentes del mismo proceso: (a) llamadas síncronas por interfaces; (b) eventos locales para todo; (c) un híbrido. Entre servidor y navegador, para difundir descartes y turnos (E4) y reconectar (E7, RF17): (d) WebSocket (Fette & Melnikov, 2011) con STOMP; (e) Server-Sent Events con REST; (f) consultas periódicas (polling). Para sincronizar turnos: (g) un candado por partida; (h) bloqueo optimista en la base en cada comando.

## Decisión

(c), (d) y (g). TurnOrchestratorComponent invoca de forma síncrona `IEvaluadorMano`, `ILiquidacion`, `IMazo` y las demás interfaces dentro del candado de la partida; tras confirmar, publica eventos de dominio por `IEventosPartida`, que SessionGatewayComponent traduce a STOMP: `/user/queue/partida` para lo privado (mano, carta robada) y `/topic/partidas/{id}` para lo público. Cada comando lleva el `numeroTurno` y el evento `TurnoAsignado` lleva la hora límite absoluta ([ADR-08](./ADR-08-temporizador.md)).

## Consecuencias

**Positivas:** orden total de los eventos de cada partida; sin condiciones de carrera al cerrar; un solo canal bidireccional con latidos que detectan cortes. (b) se descartó porque un comando que espera respuesta (validar un cierre) se vuelve asíncrono sin necesidad; (e) y (f) exigen un segundo canal o añaden latencia.

**Negativas:** una partida vive en una sola instancia (coherente con Scalability descartada); el hosting debe soportar WebSocket; los eventos en memoria se pierden si el proceso cae (se recupera desde la última ronda liquidada).

## Cumplimiento (Compliance)

- FF-06 y FF-07 (el gateway no contiene reglas ni toca la persistencia).
- FF-14 (k6, p95 < 300 ms).
- FF-15 (reconexión ≤ 5 s).
- FF-17 (doble cierre y temporizador obsoleto).

## Notas

- Autor: Duvan Steven Calderón Taborda. Fecha de aprobación: octubre de 2026. Revisores: Juan Pablo Mazo Berrío, Jose Mauricio Toscano Aguas, Duvan Steven Calderón Taborda (consenso unánime, 3 de 3).
- Fuente: Entregable 2, §3.3. Corresponde al ADR-05 (sincronización) de la guía del Entregable 2.
- ADR relacionados: [ADR-05](./ADR-05-persistencia-postgresql.md) (persistencia antes de publicar), [ADR-07](./ADR-07-autenticacion-oidc.md) (validación del token en el CONNECT de STOMP), [ADR-08](./ADR-08-temporizador.md) (hora límite del turno), [ADR-09](./ADR-09-despliegue.md) (hosting con WebSocket).
