package co.edu.unal.apuntado.fitness;

import co.edu.unal.apuntado.domain.rules.GameConfigurationComponent;
import co.edu.unal.apuntado.domain.rules.HandEvaluatorComponent;
import co.edu.unal.apuntado.domain.rules.IEvaluadorMano;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;
import co.edu.unal.apuntado.domain.rules.modelo.TipoCierre;
import org.junit.jupiter.api.Test;

import static co.edu.unal.apuntado.fitness.Manos.de;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tabla 4 del E2 como pruebas (FF-10, E3): casos borde de combinaciones y
 * cierres. Java puro: sin red ni base de datos.
 */
class CasosBordeReglasTest {

    private final IEvaluadorMano evaluador = new HandEvaluatorComponent();
    private final ConfiguracionReglas apuntado = ConfiguracionReglas.apuntado();
    private final ConfiguracionReglas clasica = ConfiguracionReglas.clasica();
    private final ConfiguracionReglas conComodines = apuntado.conComodines(true);

    private ResultadoValidacion cierre(String mano, ConfiguracionReglas cfg) {
        return evaluador.validarCierre(de(mano), cfg);
    }

    @Test
    void escaleraConAsBajoYAlto() {
        assertEquals(TipoCierre.COMPLETO, cierre("AC 2C 3C QP KP AP 5T 5D 5P 5C", apuntado).tipo());
    }

    @Test
    void escaleraQueDaLaVueltaSeRechaza() {
        assertFalse(cierre("KT AT 2T 4C 4D 4P 9C 9D 9P 9T", apuntado).aceptado());
    }

    @Test
    void comodinEnUnExtremo() {
        assertEquals(TipoCierre.COMPLETO, cierre("* 5D 6D QC KC * 3T 3D 3P 3C", conComodines).tipo());
    }

    @Test
    void dosComodinesEnUnaTernaSeRechazan() {
        // "* * 7♠" no es terna (mínimo 2 cartas naturales): el 7♠ queda suelto; "* 7♠ 8♠" sí lo es.
        assertEquals(7, evaluador.calcularCartasSueltas(de("* * 7P"), conComodines).puntos());
        assertEquals(0, evaluador.calcularCartasSueltas(de("* 7P 8P"), conComodines).puntos());
    }

    @Test
    void comodinDeshabilitadoSeRechaza() {
        assertFalse(cierre("* 5D 6D 2C 2D 2P 9C 9D 9P 9T", apuntado).aceptado());
    }

    @Test
    void segundoMazoGrupoValidoYEscaleraConRangoRepetidoNo() {
        assertEquals(TipoCierre.COMPLETO, cierre("7C 7C 7T 2C 2D 2P 9C 9D 9P 9T", apuntado).tipo());
        assertFalse(cierre("5C 6C 6C 2C 2D 2P 9C 9D 9P 9T", apuntado).aceptado());
    }

    @Test
    void residuoIgualAlMaximoSeAcepta() {
        ResultadoValidacion r = cierre("2C 2D 2P 6C 6D 6P 9C 9D 9P 4T", apuntado);
        assertEquals(TipoCierre.APUNTADO, r.tipo());
        assertEquals(4, r.puntos());
    }

    @Test
    void residuoExcedidoSeRechazaConMotivo() {
        assertEquals(ResultadoValidacion.RESIDUO_EXCEDIDO, cierre("2C 2D 2P 6C 6D 6P 9C 9D 9P 5T", apuntado).motivo());
        assertEquals(ResultadoValidacion.RESIDUO_EXCEDIDO,
                cierre("2C 2D 2P 6C 6D 6P 9C 9D 9P 4T", apuntado.conResiduoMaximo(2)).motivo());
    }

    @Test
    void comodinSobranteDaCierreCompleto() {
        ResultadoValidacion r = cierre("2C 2D 2P 6C 6D 6P 9C 9D 9P *", conComodines);
        assertEquals(TipoCierre.COMPLETO, r.tipo());
        assertEquals(-10, r.puntos());
    }

    @Test
    void cierreAPintasSoloSiSeHabilita() {
        String mano = "AC 3C 5C 7C 9C JC KC 2C 6C QC";
        assertEquals(TipoCierre.A_PINTAS, cierre(mano, apuntado.conCierreAPintas(true)).tipo());
        assertFalse(cierre(mano, apuntado).aceptado());
    }

    @Test
    void aPintasYCompletoAplicaElMasFavorable() {
        ResultadoValidacion r = cierre("AC 2C 3C 4C 5C 6C 7C 8C 9C 10C", apuntado.conCierreAPintas(true));
        assertEquals(TipoCierre.A_PINTAS, r.tipo());
        assertEquals(-25, r.puntos());
    }

    @Test
    void dosEscalerasDeCincoCompletoEnApuntadoRechazoEnClasica() {
        String mano = "5P 6P 7P 8P 9P 2C 3C 4C 5C 6C";
        assertEquals(TipoCierre.COMPLETO, cierre(mano, apuntado).tipo());
        assertEquals(ResultadoValidacion.ESTRUCTURA_INVALIDA, cierre(mano, clasica).motivo());
    }

    @Test
    void clasicaCuartaYDosTernas() {
        ResultadoValidacion r = cierre("4C 5C 6C 7C 8T 8D 8P QD QT QP", clasica);
        assertEquals(TipoCierre.CLASICO_433, r.tipo());
        assertEquals(-10, r.puntos());
    }

    @Test
    void manoConNumeroDeCartasIncorrecto() {
        assertEquals(ResultadoValidacion.NUMERO_DE_CARTAS, cierre("2C 2D 2P", apuntado).motivo());
    }

    @Test
    void modalidadDesconocida() {
        assertEquals(ResultadoValidacion.MODALIDAD_DESCONOCIDA,
                cierre("2C 2D 2P 6C 6D 6P 9C 9D 9P 4T", apuntado.conModalidad("NO_EXISTE")).motivo());
    }

    @Test
    void mesaDeCincoOSeisConUnMazoSeRechaza() {
        GameConfigurationComponent cfg = new GameConfigurationComponent();
        assertThrows(IllegalArgumentException.class, () -> cfg.validarMesa(apuntado, 5));
        assertThrows(IllegalArgumentException.class, () -> cfg.validarMesa(apuntado, 6));
        cfg.validarMesa(apuntado.conSegundoMazo(true), 6);
        cfg.validarMesa(apuntado, 4);
        assertThrows(IllegalArgumentException.class, () -> cfg.validarMesa(apuntado, 1));
        assertThrows(IllegalArgumentException.class, () -> cfg.validarMesa(apuntado.conSegundoMazo(true), 7));
        assertTrue(true);
    }
}
