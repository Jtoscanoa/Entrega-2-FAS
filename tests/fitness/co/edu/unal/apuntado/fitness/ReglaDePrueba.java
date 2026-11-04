package co.edu.unal.apuntado.fitness;

import co.edu.unal.apuntado.domain.rules.evaluacion.ReglaJuego;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;
import co.edu.unal.apuntado.domain.rules.modelo.TipoCierre;

import java.util.List;

/**
 * Variante que solo existe en el classpath de pruebas (FF-13): se declara en
 * tests/resources/META-INF/services sin tocar el código de producción.
 */
public final class ReglaDePrueba implements ReglaJuego {

    public static final String MODALIDAD = "VARIANTE_DE_PRUEBA";

    @Override
    public String modalidad() {
        return MODALIDAD;
    }

    @Override
    public ResultadoValidacion evaluarCierre(List<Carta> mano10, ConfiguracionReglas cfg) {
        boolean todasRojas = mano10.stream().allMatch(c -> c.pinta() == 'C' || c.pinta() == 'D');
        return todasRojas ? ResultadoValidacion.aceptar(TipoCierre.COMPLETO, -10, List.of())
                : ResultadoValidacion.rechazar(ResultadoValidacion.ESTRUCTURA_INVALIDA);
    }
}
