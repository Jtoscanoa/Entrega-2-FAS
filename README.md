# Entrega 2 FAS — Apuntado a 101

Repositorio del **Entregable 2** de *Fundamentos de Arquitectura de Software* (UNAL Medellín, 2026-2): diseño detallado, arquitectura de información, ADR y gobernanza del sistema digital de juego de cartas (variante Apuntado a 101 con soporte a la modalidad Clásica 4-3-3).

**Equipo 4 menos 1:** Juan Pablo Mazo Berrío · Jose Mauricio Toscano Aguas · Duvan Steven Calderón Taborda

## Contenido

| Ruta | Qué hay |
|---|---|
| `docs/modelos/` | Las 11 figuras del documento en draw.io (`F01_…` a `F11_…`), con exportación PNG y SVG, y `E2_diagramas_todos.drawio` con todas como páginas |
| `docs/adrs/` | ADR-01 a ADR-09 en Markdown (formato Nygard extendido) |
| `docs/datos/schema.sql` | Esquema relacional en 3FN (13 tablas, PostgreSQL 16) |
| `docs/datos/verificar_integridad.sql` | Script que comprueba las restricciones citadas en el §2.2 |
| `src/main/java/` | Esqueleto ejecutable del servidor: 11 componentes, 17 dependencias por interfaces y motor de reglas completo |
| `src/main/resources/db/migration/` | Migración inicial (`V1__esquema_inicial.sql`, igual a `schema.sql`) |
| `tests/architecture/` | Fitness functions estructurales: `ReglasArquitecturaTest` (ArchUnit) y `MetricasAcoplamientoTest` |
| `tests/fitness/` | Fitness functions de comportamiento: casos borde de la Tabla 4, cartas sueltas, configuración, extensibilidad y concurrencia |
| `config/pmd/complejidad.xml` | Límite de complejidad ciclomática (CC ≤ 10) |
| `.github/workflows/fitness-functions.yml` | Pipeline de la Figura 6 |

## Cómo ejecutar

Requisitos: **JDK 21** y **Maven 3.9**.

```bash
mvn verify
```

`mvn verify` compila con Java 21 y luego ejecuta:
- las reglas ArchUnit;
- las pruebas JUnit de `tests/architecture` y `tests/fitness` (el `pom.xml` declara ambos directorios como fuentes de prueba);
- JaCoCo, que exige al menos el 95 % de ramas en cada paquete de `domain.rules`;
- PMD, que exige CC ≤ 10 por método.

Si algo falla, el build queda en rojo. Las métricas de acoplamiento quedan en `target/metricas-acoplamiento.csv`.

Para comprobar el esquema en una base PostgreSQL 16 vacía:

```bash
psql -d apuntado -f docs/datos/schema.sql
psql -d apuntado -f docs/datos/verificar_integridad.sql   # imprime cinco NOTICE "OK: ..."
```

## Fitness functions y dónde están

| ID | Qué protege | Implementación |
|---|---|---|
| FF-01, FF-01b | `domain.rules` y su núcleo `modelo` son Java puro | `ReglasArquitecturaTest` |
| FF-01c | TurnOrchestrator es mediador estricto (no calcula cartas ni puntos) | `ReglasArquitecturaTest` |
| FF-02 | Sin ciclos entre dominios ni entre paquetes | `ReglasArquitecturaTest` |
| FF-03 | Las variantes de reglas no dependen entre sí | `ReglasArquitecturaTest` |
| FF-04 | Entre componentes solo se usan interfaces | `ReglasArquitecturaTest` |
| FF-05 | Solo `gameplay.mazo` y `gameplay.auditoria` conocen `OrdenMazo` | `ReglasArquitecturaTest` |
| FF-06, FF-07 | El gateway no contiene reglas ni toca la persistencia ni entidades | `ReglasArquitecturaTest` |
| FF-08, FF-08b | Ca, Ce e I coinciden con el E1; la persistencia no depende del dominio | `MetricasAcoplamientoTest`, `ReglasArquitecturaTest` |
| FF-09 | CC ≤ 10 por método | PMD (`config/pmd/complejidad.xml`) |
| FF-10 | Cobertura de ramas ≥ 95 % en `domain.rules`; suite < 60 s | JaCoCo + `CasosBordeReglasTest`, `CartasSueltasTest`, `ModeloYBuscadorTest` |
| FF-12 | Configuración sin redespliegue (umbral 150 y comodines) | `ConfiguracionSinRedespliegueTest` |
| FF-13 | Variante nueva sin tocar el orquestador | `ExtensibilidadVarianteTest` + paso `git diff` del pipeline |
| FF-17 | Doble cierre y temporizador obsoleto | `ConcurrenciaCierreTest` |

Pendientes por fase (Tabla 10b del documento): FF-05 y FF-11 (commit-reveal) se cierran en la Fase 2. FF-14 y FF-15 (k6), FF-16 (Testcontainers) y FF-18 (Spring Security) llegan con el MVP del E3.

## Alcance del esqueleto

- Es Java 21 sin framework; la raíz de composición es `Aplicacion.ensamblar()`. En el E3, Spring Boot asume ese papel y se agregan la API STOMP y JPA.
- `MatchStateRepository` guarda en memoria, con la misma semántica atómica que tendrá la versión JDBC: una ronda se liquida una sola vez.
- BotEngine, DealAudit y WagerAndEconomy son stubs mínimos de la Fase 1.
- Para agregar una variante de juego basta con crear una clase que implemente `ReglaJuego` y declararla en `META-INF/services` (ver `tests/fitness/.../ReglaDePrueba.java`).
