# ADR-01: Adopción de Particionamiento por Dominio para el Sistema de Juego de Cartas

## Estado

Accepted (Aceptado).

## Contexto

Según Richards & Ford (2025, Cap. 9), el particionamiento de primer nivel estructura el sistema por capas técnicas o por áreas de dominio. Con variantes de reglas, apuestas, reparto verificable y multijugador a distancia, las capas técnicas dispersan la lógica del juego y encarecen el mantenimiento.

## Decisión

Se adopta un Particionamiento por Dominio: `domain.lobby` (LobbyParty), `domain.gameplay` (TurnOrchestrator, DeckManagement, DealAudit, BotEngine), `domain.rules` (HandEvaluator, RoundScoring, GameConfiguration) y `domain.wager` (WagerAndEconomy); `presentation`, `infrastructure.realtime` (SessionGateway) e `infrastructure.persistence` (MatchStateRepository) son paquetes de soporte sin reglas de negocio.

## Consecuencias

**Positivas:** alta cohesión; los cambios de reglas se confinan a `domain.rules`.

**Negativas:** exige contratos claros entre dominios para evitar dependencias circulares.

## Cumplimiento (Compliance)

Fitness functions en ArchUnit (servidor en Java, RD1): `domain.rules` no importa infraestructura ni presentación y no hay ciclos entre los paquetes `domain.*`.

## Notas

- Autor: Jose Mauricio Toscano Aguas | Fecha de aprobación: 29/9/2026 | Revisores: Juan Pablo Mazo Berrío y Duvan Steven Calderón Taborda
- Fuente: Entregable 1, §5.1.
- ADR relacionados: [ADR-02](./ADR-02-strategy-monolito-modular.md), [ADR-04](./ADR-04-estilo-microkernel.md) (organiza el servidor en módulos por dominio según este ADR).
