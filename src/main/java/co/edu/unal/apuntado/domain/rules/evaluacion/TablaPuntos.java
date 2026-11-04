package co.edu.unal.apuntado.domain.rules.evaluacion;

import co.edu.unal.apuntado.domain.rules.modelo.Carta;

/** Puntos de una carta (RF07): A, J, Q y K = 10; 2 a 10 = valor visible; comodín = 0. */
public final class TablaPuntos {

    private TablaPuntos() {
    }

    public static int puntos(Carta carta) {
        if (carta.comodin()) {
            return 0;
        }
        int r = carta.rango();
        return r == 1 || r > 10 ? 10 : r;
    }
}
