package co.edu.unal.apuntado.domain.rules;

import co.edu.unal.apuntado.domain.rules.modelo.Agrupacion;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;

import java.util.List;

/** Interfaz provista por HandEvaluatorComponent. */
public interface IEvaluadorMano {
    ResultadoValidacion validarCierre(List<Carta> mano10, ConfiguracionReglas cfg);

    Agrupacion calcularCartasSueltas(List<Carta> mano, ConfiguracionReglas cfg);
}
