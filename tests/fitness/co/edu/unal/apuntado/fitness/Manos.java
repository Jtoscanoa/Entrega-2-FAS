package co.edu.unal.apuntado.fitness;

import co.edu.unal.apuntado.domain.rules.modelo.Carta;

import java.util.Arrays;
import java.util.List;

/** Utilidad de pruebas: "7C 8C 9C" → lista de cartas (C ♥, D ♦, T ♣, P ♠). */
final class Manos {
    private Manos() {
    }

    static List<Carta> de(String codigos) {
        return Arrays.stream(codigos.trim().split("\\s+")).map(Carta::of).toList();
    }
}
