package co.edu.unal.apuntado.domain.rules.evaluacion;

import co.edu.unal.apuntado.domain.rules.modelo.Agrupacion;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;

import java.util.List;

/**
 * Contrato del microkernel de compilación (ADR-04). Una variante nueva es una
 * clase que implementa este contrato y se declara en META-INF/services.
 */
public interface ReglaJuego {

    /** Clave de la modalidad, p. ej. "APUNTADO_101". */
    String modalidad();

    /** Evalúa un cierre sobre las 10 cartas que quedan tras el descarte. */
    ResultadoValidacion evaluarCierre(List<Carta> mano10, ConfiguracionReglas cfg);

    /** Cartas sueltas con la agrupación que minimiza el puntaje del perdedor (RF07). */
    default Agrupacion cartasSueltas(List<Carta> mano, ConfiguracionReglas cfg) {
        return new BuscadorCombinaciones(mano, cfg.comodinesHabilitados()).minimizarSueltas();
    }
}
