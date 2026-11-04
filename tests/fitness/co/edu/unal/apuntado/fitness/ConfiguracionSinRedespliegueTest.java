package co.edu.unal.apuntado.fitness;

import co.edu.unal.apuntado.domain.rules.GameConfigurationComponent;
import co.edu.unal.apuntado.domain.rules.HandEvaluatorComponent;
import co.edu.unal.apuntado.domain.rules.RoundScoringComponent;
import co.edu.unal.apuntado.domain.rules.ResultadoLiquidacion;
import co.edu.unal.apuntado.domain.rules.SolicitudLiquidacion;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static co.edu.unal.apuntado.fitness.Manos.de;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FF-12 (E1): umbral 150 y comodines se aplican con el mismo binario, sin redesplegar. */
class ConfiguracionSinRedespliegueTest {

    @Test
    void umbral150YComodinesConElMismoBinario() {
        GameConfigurationComponent configuracion = new GameConfigurationComponent();
        configuracion.registrar("K7Q2ZP", ConfiguracionReglas.apuntado().conUmbral(150).conComodines(true));
        ConfiguracionReglas cfg = configuracion.obtener("K7Q2ZP");

        assertTrue(new HandEvaluatorComponent().validarCierre(de("* 5D 6D 2C 2D 2P 9C 9D 9P 9T"), cfg).aceptado());
        ResultadoLiquidacion r = new RoundScoringComponent().liquidar(new SolicitudLiquidacion(
                Map.of("ana", 90, "beto", 120), "ana", -10, Map.of("beto", 25), cfg.umbralEliminacion()));
        assertEquals(Integer.valueOf(145), r.marcador().get("beto"));
        assertFalse(r.eliminados().contains("beto"));
    }

    @Test
    void liquidacionEliminaYGanaElUnicoActivo() {
        ResultadoLiquidacion r = new RoundScoringComponent().liquidar(new SolicitudLiquidacion(
                Map.of("ana", 50, "beto", 95), "ana", 3, Map.of("beto", 20), 101));
        assertEquals(Map.of("ana", 53, "beto", 115), r.marcador());
        assertEquals("ana", r.ganador());
    }

    @Test
    void siTodosSuperanElUmbralGanaElMenorDeLaRonda() {
        ResultadoLiquidacion r = new RoundScoringComponent().liquidar(new SolicitudLiquidacion(
                Map.of("ana", 98, "beto", 95), "ana", 4, Map.of("beto", 20), 101));
        assertEquals("ana", r.ganador());
    }

    @Test
    void sinGanadorMientrasHayaVariosActivos() {
        ResultadoLiquidacion r = new RoundScoringComponent().liquidar(new SolicitudLiquidacion(
                Map.of(), "ana", -10, Map.of("beto", 20, "caro", 5), 101));
        assertNull(r.ganador());
        assertEquals(20, new RoundScoringComponent().puntajeReingreso(r.marcador()));
        assertEquals(0, new RoundScoringComponent().puntajeReingreso(Map.of()));
    }

    @Test
    void configuracionInvalidaOAusenteSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionReglas.apuntado().conResiduoMaximo(5));
        assertThrows(IllegalArgumentException.class, () -> new GameConfigurationComponent().obtener("NADA"));
        assertThrows(IllegalArgumentException.class,
                () -> new HandEvaluatorComponent().calcularCartasSueltas(de("2C"),
                        ConfiguracionReglas.apuntado().conModalidad("NO_EXISTE")));
    }
}
