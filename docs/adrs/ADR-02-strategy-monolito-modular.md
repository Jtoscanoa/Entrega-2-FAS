# ADR-02: Implementación del Patrón Strategy dentro de un Monolito Modular para Desacoplar Reglas de Victoria

## Estado

Accepted; refinado por [ADR-04](./ADR-04-estilo-microkernel.md).

## Contexto

Se deben soportar Apuntado a 101 y Clásica 4-3-3 con parámetros por sala. El Microkernel es un estilo monolítico con plug-ins de compilación o de ejecución (Richards & Ford, 2025, Cap. 13). Se evaluaron: (a) condicionales; (b) plug-ins cargados en ejecución; (c) estrategias integradas en compilación, equivalentes a un microkernel con plug-ins de compilación. La opción (b) exige registro, contratos versionados y empaquetado que el alcance no justifica.

## Decisión

Strategy (opción c): interfaz `ReglaJuego` dentro de HandEvaluatorComponent; GameConfigurationComponent entrega `ReglaApuntado101` o `ReglaClasicaCuartaTerna` al iniciar la partida. Los parámetros se leen en ejecución de `ConfiguracionReglas`; solo los algoritmos residen en código.

## Consecuencias

**Positivas:** pruebas unitarias puras, parámetros sin recompilar y nuevas variantes sin modificar el orquestador.

**Negativas:** una modalidad nueva requiere recompilación o empaquetado.

## Cumplimiento (Compliance)

Cobertura superior al 95 % en `validarCierre` y cartas sueltas, y prueba de arquitectura de que TurnOrchestratorComponent solo depende de la interfaz `ReglaJuego`.

## Notas

- Autor: Juan Pablo Mazo Berrío | Fecha de aprobación: 29/9/2026 | Revisores: Jose Mauricio Toscano Aguas y Duvan Steven Calderón Taborda
- Fuente: Entregable 1, §5.2. Estado actualizado según la Tabla 7 del Entregable 2.
- ADR relacionados: [ADR-01](./ADR-01-particionamiento-por-dominio.md), [ADR-04](./ADR-04-estilo-microkernel.md) (refina este ADR: el registro de estrategias se ubica en HandEvaluatorComponent).
