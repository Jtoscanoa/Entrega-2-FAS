package co.edu.unal.apuntado.domain.rules.modelo;

import java.util.List;

/** Agrupación de una mano que minimiza los puntos de las cartas sueltas (RF07). */
public record Agrupacion(int puntos, List<Combinacion> combinaciones, List<Carta> sueltas) {
    public Agrupacion {
        combinaciones = List.copyOf(combinaciones);
        sueltas = List.copyOf(sueltas);
    }
}
