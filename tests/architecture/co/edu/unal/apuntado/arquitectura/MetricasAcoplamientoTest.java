package co.edu.unal.apuntado.arquitectura;

import co.edu.unal.apuntado.domain.gameplay.TurnOrchestratorComponent;
import co.edu.unal.apuntado.domain.gameplay.auditoria.DealAuditComponent;
import co.edu.unal.apuntado.domain.gameplay.bot.BotEngineComponent;
import co.edu.unal.apuntado.domain.gameplay.mazo.DeckManagementComponent;
import co.edu.unal.apuntado.domain.lobby.LobbyPartyComponent;
import co.edu.unal.apuntado.domain.rules.GameConfigurationComponent;
import co.edu.unal.apuntado.domain.rules.HandEvaluatorComponent;
import co.edu.unal.apuntado.domain.rules.RoundScoringComponent;
import co.edu.unal.apuntado.domain.wager.WagerAndEconomyComponent;
import co.edu.unal.apuntado.infrastructure.persistence.MatchStateRepository;
import co.edu.unal.apuntado.infrastructure.realtime.SessionGatewayComponent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FF-08 (holística): construye el grafo de componentes desde el bytecode (las
 * interfaces que cada componente recibe en su constructor), suma la dependencia
 * aferente de la UI (TypeScript, fuera del bytecode) y compara Ca, Ce e
 * I = Ce / (Ca + Ce) con el E1 §4.5 (Martin, 2003).
 */
class MetricasAcoplamientoTest {

    private static final List<Class<?>> COMPONENTES = List.of(SessionGatewayComponent.class,
            LobbyPartyComponent.class, TurnOrchestratorComponent.class, DeckManagementComponent.class,
            DealAuditComponent.class, BotEngineComponent.class, HandEvaluatorComponent.class,
            RoundScoringComponent.class, GameConfigurationComponent.class, WagerAndEconomyComponent.class,
            MatchStateRepository.class);
    private static final String UI = "UI Web (SPA)";
    private static final Map<String, Set<String>> GRAFO = construirGrafo();

    private static String nombre(Class<?> c) {
        return c.getSimpleName().replace("Component", "");
    }

    private static Map<String, Set<String>> construirGrafo() {
        Map<Class<?>, String> proveedor = new HashMap<>();
        COMPONENTES.forEach(c -> Arrays.stream(c.getInterfaces()).forEach(i -> proveedor.put(i, nombre(c))));
        Map<String, Set<String>> grafo = new TreeMap<>();
        grafo.put(UI, new TreeSet<>(Set.of(nombre(SessionGatewayComponent.class))));
        for (Class<?> c : COMPONENTES) {
            Set<String> destinos = new TreeSet<>();
            for (Constructor<?> k : c.getDeclaredConstructors()) {
                Arrays.stream(k.getParameterTypes()).map(proveedor::get).filter(p -> p != null).forEach(destinos::add);
            }
            grafo.put(nombre(c), destinos);
        }
        return grafo;
    }

    private static int ce(String c) {
        return GRAFO.get(c).size();
    }

    private static int ca(String c) {
        return (int) GRAFO.values().stream().filter(d -> d.contains(c)).count();
    }

    private static double inestabilidad(String c) {
        int total = ca(c) + ce(c);
        return total == 0 ? 0 : (double) ce(c) / total;
    }

    @Test
    void lasMetricasCoincidenConElE1() throws IOException {
        assertEquals(17, GRAFO.values().stream().mapToInt(Set::size).sum(), "dependencias");
        assertEquals(7, ce("TurnOrchestrator"), "Ce del orquestador");
        for (String estable : List.of("HandEvaluator", "RoundScoring", "GameConfiguration", "DealAudit",
                "MatchStateRepository")) {
            assertEquals(0.0, inestabilidad(estable), "I de " + estable);
        }
        List<String> filas = new ArrayList<>(List.of("componente,Ca,Ce,I"));
        GRAFO.keySet().forEach(c -> filas.add(String.format(java.util.Locale.ROOT, "%s,%d,%d,%.2f", c, ca(c),
                ce(c), inestabilidad(c))));
        Path csv = Path.of("target", "metricas-acoplamiento.csv");
        Files.createDirectories(csv.getParent());
        Files.write(csv, filas);
    }

    @Test
    void soloSeAceptanLasViolacionesDelSdpDocumentadas() {
        List<String> violaciones = new ArrayList<>();
        GRAFO.forEach((origen, destinos) -> destinos.forEach(destino -> {
            if (inestabilidad(origen) < inestabilidad(destino) - 1e-9) {
                violaciones.add(origen + "->" + destino);
            }
        }));
        assertTrue(Set.of("SessionGateway->LobbyParty", "SessionGateway->TurnOrchestrator")
                .containsAll(violaciones), "Nuevas violaciones del SDP: " + violaciones);
    }
}
