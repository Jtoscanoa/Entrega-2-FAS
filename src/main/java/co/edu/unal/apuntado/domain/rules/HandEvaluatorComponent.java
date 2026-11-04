package co.edu.unal.apuntado.domain.rules;

import co.edu.unal.apuntado.domain.rules.evaluacion.RegistroReglas;
import co.edu.unal.apuntado.domain.rules.evaluacion.ReglaJuego;
import co.edu.unal.apuntado.domain.rules.modelo.Agrupacion;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;

import java.util.List;
import java.util.Optional;

/**
 * Único responsable de validar combinaciones y cierres con la regla activa y de
 * calcular las cartas sueltas; no asigna puntos ni conoce la partida (Ce = 0).
 */
public final class HandEvaluatorComponent implements IEvaluadorMano {

    private static final int CARTAS_CIERRE = 10;
    private final RegistroReglas registro;

    public HandEvaluatorComponent() {
        this(new RegistroReglas());
    }

    HandEvaluatorComponent(RegistroReglas registro) {
        this.registro = registro;
    }

    @Override
    public ResultadoValidacion validarCierre(List<Carta> mano10, ConfiguracionReglas cfg) {
        Optional<ReglaJuego> regla = registro.obtener(cfg.modalidad());
        if (regla.isEmpty()) {
            return ResultadoValidacion.rechazar(ResultadoValidacion.MODALIDAD_DESCONOCIDA);
        }
        if (mano10.size() != CARTAS_CIERRE) {
            return ResultadoValidacion.rechazar(ResultadoValidacion.NUMERO_DE_CARTAS);
        }
        return regla.get().evaluarCierre(mano10, cfg);
    }

    @Override
    public Agrupacion calcularCartasSueltas(List<Carta> mano, ConfiguracionReglas cfg) {
        return registro.obtener(cfg.modalidad())
                .orElseThrow(() -> new IllegalArgumentException(ResultadoValidacion.MODALIDAD_DESCONOCIDA))
                .cartasSueltas(mano, cfg);
    }
}
