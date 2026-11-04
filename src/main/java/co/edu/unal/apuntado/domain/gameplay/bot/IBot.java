package co.edu.unal.apuntado.domain.gameplay.bot;

import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;

import java.util.List;

/** Interfaz provista por BotEngineComponent. */
public interface IBot {
    DecisionBot decidir(List<Carta> mano, ConfiguracionReglas cfg, boolean puedeCerrar);
}
