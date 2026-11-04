package co.edu.unal.apuntado.fitness;

import co.edu.unal.apuntado.domain.gameplay.ResultadoJugada;
import co.edu.unal.apuntado.domain.gameplay.TurnOrchestratorComponent;
import co.edu.unal.apuntado.domain.gameplay.VistaPartida;
import co.edu.unal.apuntado.domain.gameplay.bot.BotEngineComponent;
import co.edu.unal.apuntado.domain.gameplay.mazo.IMazo;
import co.edu.unal.apuntado.domain.gameplay.mazo.RevelacionReparto;
import co.edu.unal.apuntado.domain.rules.GameConfigurationComponent;
import co.edu.unal.apuntado.domain.rules.HandEvaluatorComponent;
import co.edu.unal.apuntado.domain.rules.RoundScoringComponent;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.wager.WagerAndEconomyComponent;
import co.edu.unal.apuntado.infrastructure.persistence.MatchStateRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static co.edu.unal.apuntado.fitness.Manos.de;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FF-17 (E5, R10, ADR-06, ADR-08): dos cierres simultáneos producen una sola
 * liquidación y un vencimiento de turno obsoleto no tiene efecto.
 */
class ConcurrenciaCierreTest {

    private final MatchStateRepository repositorio = new MatchStateRepository();
    private final TurnOrchestratorComponent orquestador = orquestador();

    private TurnOrchestratorComponent orquestador() {
        HandEvaluatorComponent evaluador = new HandEvaluatorComponent();
        GameConfigurationComponent configuracion = new GameConfigurationComponent();
        configuracion.registrar("SALA", ConfiguracionReglas.apuntado());
        TurnOrchestratorComponent to = new TurnOrchestratorComponent(new MazoFijo(), new BotEngineComponent(evaluador),
                evaluador, new RoundScoringComponent(), configuracion, new WagerAndEconomyComponent(repositorio),
                repositorio);
        to.iniciarPartida("P1", "SALA", List.of("ana", "beto"));
        return to;
    }

    @Test
    void dosCierresSimultaneosLiquidanUnaSolaVez() throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<ResultadoJugada>> r = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            r.add(hilos.submit(() -> {
                salida.await();
                return orquestador.cerrar("P1", "ana", "2D", 0);
            }));
        }
        salida.countDown();
        int aceptados = 0;
        for (Future<ResultadoJugada> f : r) {
            aceptados += f.get().aceptado() ? 1 : 0;
        }
        hilos.shutdown();
        assertEquals(1, aceptados);
        assertEquals(1, repositorio.rondasLiquidadas());
    }

    @Test
    void comandoConNumeroDeTurnoViejoSeRechazaSinEfectos() {
        assertTrue(orquestador.descartar("P1", "ana", "2D", 0).aceptado());
        ResultadoJugada viejo = orquestador.descartar("P1", "ana", "KD", 0);
        assertFalse(viejo.aceptado());
        assertEquals("NUMERO_DE_TURNO_OBSOLETO", viejo.motivo());
        assertFalse(orquestador.cerrar("P1", "beto", "2T", 1).aceptado()); // beto no ha robado
    }

    @Test
    void elTemporizadorObsoletoNoTieneEfecto() {
        assertTrue(orquestador.descartar("P1", "ana", "2D", 0).aceptado());
        assertFalse(orquestador.vencerTurno("P1", 0));
        assertTrue(orquestador.vencerTurno("P1", 1)); // turno de beto: roba y descarta por defecto
        VistaPartida vista = orquestador.vistaDe("P1", "beto");
        assertEquals(10, vista.mano().size());
        assertEquals(2, vista.numeroTurno());
    }

    @Test
    void robarDosVecesOFueraDeTurnoSeRechaza() {
        assertTrue(orquestador.descartar("P1", "ana", "2D", 0).aceptado());
        assertTrue(orquestador.robar("P1", "beto", 1).aceptado());
        assertFalse(orquestador.robar("P1", "beto", 1).aceptado());
        assertFalse(orquestador.robar("P1", "ana", 1).aceptado());
    }

    /** Mazo determinista: ana (abre) recibe un cierre completo más el 2♦; beto, 10 cartas. */
    private static final class MazoFijo implements IMazo {
        private final List<Carta> descarte = new ArrayList<>();

        @Override
        public String nuevaRonda(String ronda, ConfiguracionReglas cfg) {
            return "huella-de-prueba";
        }

        @Override
        public List<List<Carta>> repartir(String ronda, int jugadores) {
            return List.of(new ArrayList<>(de("7C 8C 9C 5T 5D 5P KD KT KP KC 2D")),
                    new ArrayList<>(de("2T 3D 4P 6C 8D 10T JP QC AD 3P")));
        }

        @Override
        public Carta robar(String ronda) {
            return Carta.of("JD");
        }

        @Override
        public void descartar(String ronda, Carta carta) {
            descarte.add(carta);
        }

        @Override
        public Carta cimaDescarte(String ronda) {
            return descarte.isEmpty() ? null : descarte.get(descarte.size() - 1);
        }

        @Override
        public RevelacionReparto revelar(String ronda) {
            return new RevelacionReparto(List.of("7C"), "sal", "huella-de-prueba");
        }
    }
}
