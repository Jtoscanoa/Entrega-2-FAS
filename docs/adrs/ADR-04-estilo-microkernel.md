# ADR-04: Monolito modular con un motor de reglas organizado como microkernel de compilación

## Estado

Accepted (Aceptado). Refina el [ADR-02](./ADR-02-strategy-monolito-modular.md): el registro de estrategias se ubica en HandEvaluatorComponent.

## Contexto

El Top 3 exige agregar variantes sin tocar el orquestador (E2) y probar las reglas sin red ni base de datos (E3); RD2 limita la complejidad a lo que tres estudiantes construyen para el E3. Se evaluaron (Tabla 2): (a) microkernel con plug-ins cargados en ejecución; (b) monolito modular con Strategy y registro por `ServiceLoader`; (c) Service-Based con servicios de juego, reglas y economía. Capas y microservicios quedaron como referencia.

## Decisión

Opción (b), con un solo cuanto. TurnOrchestratorComponent depende solo de `IEvaluadorMano`; HandEvaluatorComponent contiene el contrato `ReglaJuego`, el registro `RegistroReglas` y las estrategias `ReglaApuntado101` y `ReglaClasicaCuartaTerna`. GameConfigurationComponent entrega la clave de modalidad en `ConfiguracionReglas` y el registro resuelve la estrategia. Una variante nueva (p. ej., Continental) es una clase que implementa `ReglaJuego`, declarada en `META-INF/services` y con su tabla de casos: el núcleo no se modifica ni se recompila; solo se redespliega el servidor.

## Consecuencias

**Positivas:** reglas en Java puro, probadas en cerca de 1 s; E2 medible en cada pull request; llamadas en memoria (E4).

**Negativas:** una variante exige redespliegue (se acepta: es infrecuente y los parámetros sí cambian en caliente); un error en una estrategia afecta al proceso, porque no hay aislamiento de plug-ins (lo mitigan la suite y CC ≤ 10); el escalado horizontal queda fuera hasta cumplir un criterio de migración (§1.1).

## Cumplimiento (Compliance)

- FF-01 a FF-04 (ArchUnit).
- FF-01b (ArchUnit): núcleo `domain.rules.modelo` puro.
- FF-08 (métricas de acoplamiento).
- FF-13 (`ExtensibilidadVarianteTest` y `git diff` en los pull requests con etiqueta "variante").

## Notas

- Autor: Juan Pablo Mazo Berrío. Fecha de aprobación: octubre de 2026. Revisores: Juan Pablo Mazo Berrío, Jose Mauricio Toscano Aguas, Duvan Steven Calderón Taborda (consenso unánime, 3 de 3).
- Fuente: Entregable 2, §3.1. Corresponde al ADR-03 (estilo) de la guía del Entregable 2.
- ADR relacionados: [ADR-01](./ADR-01-particionamiento-por-dominio.md) (módulos por dominio), [ADR-02](./ADR-02-strategy-monolito-modular.md) (refinado por este ADR), [ADR-09](./ADR-09-despliegue.md) (materializa el cuanto único).
