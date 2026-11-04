package co.edu.unal.apuntado.domain.gameplay.bot;

import co.edu.unal.apuntado.domain.rules.IEvaluadorMano;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub de Fase 1 (Tabla 10b): solo propone jugadas válidas usando el evaluador.
 * Cierra si algún descarte deja un cierre aceptado; si no, descarta la carta que
 * deja la mano con menos puntos sueltos. Nunca modifica el estado.
 */
public final class BotEngineComponent implements IBot {

    private final IEvaluadorMano evaluador;

    public BotEngineComponent(IEvaluadorMano evaluador) {
        this.evaluador = evaluador;
    }

    @Override
    public DecisionBot decidir(List<Carta> mano, ConfiguracionReglas cfg, boolean puedeCerrar) {
        DecisionBot mejor = null;
        int menor = Integer.MAX_VALUE;
        for (int i = 0; i < mano.size(); i++) {
            List<Carta> resto = sin(mano, i);
            if (puedeCerrar && evaluador.validarCierre(resto, cfg).aceptado()) {
                return new DecisionBot(true, mano.get(i));
            }
            int puntos = evaluador.calcularCartasSueltas(resto, cfg).puntos();
            if (puntos < menor) {
                menor = puntos;
                mejor = new DecisionBot(false, mano.get(i));
            }
        }
        return mejor;
    }

    private static List<Carta> sin(List<Carta> mano, int indice) {
        List<Carta> resto = new ArrayList<>(mano);
        resto.remove(indice);
        return resto;
    }
}
