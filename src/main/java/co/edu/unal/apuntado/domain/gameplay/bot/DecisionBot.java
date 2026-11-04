package co.edu.unal.apuntado.domain.gameplay.bot;

import co.edu.unal.apuntado.domain.rules.modelo.Carta;

/** Jugada propuesta por un bot: cerrar o no, y qué carta descartar. */
public record DecisionBot(boolean cerrar, Carta descarte) {
}
