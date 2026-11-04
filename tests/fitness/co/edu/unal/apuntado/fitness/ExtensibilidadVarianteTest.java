package co.edu.unal.apuntado.fitness;

import co.edu.unal.apuntado.Aplicacion;
import co.edu.unal.apuntado.domain.rules.HandEvaluatorComponent;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.infrastructure.realtime.SessionGatewayComponent;
import org.junit.jupiter.api.Test;

import static co.edu.unal.apuntado.fitness.Manos.de;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FF-13 (E2): una variante nueva se resuelve por su clave sin modificar el
 * orquestador ni el registro. El chequeo de "0 archivos fuera de domain.rules"
 * lo hace el pipeline con git diff en los pull requests con etiqueta variante.
 */
class ExtensibilidadVarianteTest {

    @Test
    void varianteDelClasspathDePruebasSeResuelveSinTocarElNucleo() {
        ConfiguracionReglas cfg = ConfiguracionReglas.apuntado().conModalidad(ReglaDePrueba.MODALIDAD);
        HandEvaluatorComponent evaluador = new HandEvaluatorComponent();
        assertTrue(evaluador.validarCierre(de("2C 3C 4C 5C 6D 7D 8D 9D 10C JC"), cfg).aceptado());
        assertFalse(evaluador.validarCierre(de("2C 3C 4C 5C 6D 7D 8D 9D 10C JT"), cfg).aceptado());
    }

    @Test
    void elEnsambladoDelNucleoNoCambiaConLaVariante() {
        SessionGatewayComponent gateway = Aplicacion.ensamblar();
        assertNotNull(gateway);
    }
}
