package co.edu.unal.apuntado.domain.rules.modelo;

import java.util.List;

/** Combinación válida de 3 a 5 cartas (grupo o escalera). */
public record Combinacion(List<Carta> cartas, TipoCombinacion tipo) {
    public Combinacion {
        cartas = List.copyOf(cartas);
    }

    public int tamano() {
        return cartas.size();
    }
}
