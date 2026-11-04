package co.edu.unal.apuntado.fitness;

import co.edu.unal.apuntado.domain.rules.evaluacion.BuscadorCombinaciones;
import co.edu.unal.apuntado.domain.rules.evaluacion.RegistroReglas;
import co.edu.unal.apuntado.domain.rules.evaluacion.ReglaApuntado101;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;
import co.edu.unal.apuntado.domain.rules.modelo.TipoCierre;
import org.junit.jupiter.api.Test;

import java.util.List;

import static co.edu.unal.apuntado.fitness.Manos.de;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Objetos de valor del núcleo compartido y bordes del buscador (FF-10). */
class ModeloYBuscadorTest {

    @Test
    void codigosDeCartaIdaYVuelta() {
        for (String c : List.of("AC", "2D", "10T", "JP", "QC", "KD", "*")) {
            assertEquals(c, Carta.of(c).codigo());
            assertEquals(c, Carta.of(c).toString());
        }
        assertThrows(IllegalArgumentException.class, () -> new Carta(14, 'C', false));
        assertThrows(IllegalArgumentException.class, () -> new Carta(0, 'C', false));
        assertThrows(IllegalArgumentException.class, () -> new Carta(5, 'X', false));
        assertThrows(NullPointerException.class, () -> Carta.of(null));
    }

    @Test
    void configuracionRechazaParametrosFueraDeRango() {
        ConfiguracionReglas a = ConfiguracionReglas.apuntado();
        assertThrows(IllegalArgumentException.class, () -> a.conModalidad(" "));
        assertThrows(IllegalArgumentException.class, () -> a.conModalidad(null));
        assertThrows(IllegalArgumentException.class, () -> a.conUmbral(0));
        assertThrows(IllegalArgumentException.class, () -> a.conResiduoMaximo(-1));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracionReglas("X", 101, 4, false, false, false,
                0, 0, -1, -51, 1, false, false, 30));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracionReglas("X", 101, 4, false, false, false,
                -1, 0, 0, -51, 1, false, false, 30));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracionReglas("X", 101, 4, false, false, false,
                0, -1, 0, -51, 1, false, false, 30));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracionReglas("X", 101, 4, false, false, false,
                0, 0, 0, -51, 0, false, false, 30));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracionReglas("X", 101, 4, false, false, false,
                0, 0, 0, -51, 1, false, false, 4));
        assertEquals(1, ConfiguracionReglas.clasica().maxRondas());
    }

    @Test
    void resultadoSinCombinacionesYMotivo() {
        ResultadoValidacion r = new ResultadoValidacion(true, TipoCierre.COMPLETO, -10, null, null);
        assertTrue(r.combinaciones().isEmpty());
        assertNull(ResultadoValidacion.rechazar("X").tipo());
    }

    @Test
    void bordesDelBuscador() {
        assertThrows(IllegalArgumentException.class, () -> new BuscadorCombinaciones(
                de("AC 2C 3C 4C 5C 6C 7C 8C 9C 10C JC QC"), false));
        assertFalse(new BuscadorCombinaciones(de("* *"), true).aPintas());
        assertNull(new BuscadorCombinaciones(de("2C 5D 9P KT"), false).particionExacta(4));
        assertNull(new BuscadorCombinaciones(de("2C 2D 2P 9T"), false).particionLibre());
        assertNull(new BuscadorCombinaciones(de("2C 5D 9P KT AC 4D 6P 8T JC QD"), false).mejorParticionConSobrantes(3));
        assertEquals(0, new BuscadorCombinaciones(de("2C 2D 2P 9T 9C 9D"), false).minimizarSueltas().puntos());
    }

    @Test
    void registroResuelveLasDosModalidadesYApuntadoSinTernas() {
        RegistroReglas registro = new RegistroReglas();
        assertTrue(registro.soporta(ConfiguracionReglas.APUNTADO));
        assertTrue(registro.soporta(ConfiguracionReglas.CLASICA));
        assertFalse(registro.soporta("OTRA"));
        assertEquals(ResultadoValidacion.ESTRUCTURA_INVALIDA, new ReglaApuntado101()
                .evaluarCierre(de("2C 5D 9P KT AC 4D 6P 8T JC QD"), ConfiguracionReglas.apuntado()).motivo());
    }
}
