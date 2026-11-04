package co.edu.unal.apuntado.fitness;

import co.edu.unal.apuntado.domain.rules.HandEvaluatorComponent;
import co.edu.unal.apuntado.domain.rules.evaluacion.TablaPuntos;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static co.edu.unal.apuntado.fitness.Manos.de;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RF07: los perdedores suman sus cartas sueltas con la agrupación que minimiza
 * su puntaje. La programación dinámica se compara con una fuerza bruta
 * independiente (FF-10, riesgo R7).
 */
class CartasSueltasTest {

    private final HandEvaluatorComponent evaluador = new HandEvaluatorComponent();
    private final ConfiguracionReglas cfg = ConfiguracionReglas.apuntado();

    @Test
    void cartaEnDisputaUsaLaAgrupacionOptima() {
        // 7♥ 8♥ 9♥ 9♣ 9♠ K♦ Q♣ 5♠ 3♦ 2♣: la escalera deja 48; el grupo de nueves, 45.
        assertEquals(45, evaluador.calcularCartasSueltas(de("7C 8C 9C 9T 9P KD QT 5P 3D 2T"), cfg).puntos());
    }

    @Test
    void programacionDinamicaIgualAFuerzaBrutaEn300Manos() {
        Random azar = new Random(20261103L);
        List<Carta> baraja = new ArrayList<>();
        for (char p : "CDTP".toCharArray()) {
            for (int r = 1; r <= 13; r++) {
                baraja.add(new Carta(r, p, false));
            }
        }
        for (int i = 0; i < 300; i++) {
            Collections.shuffle(baraja, azar);
            List<Carta> mano = List.copyOf(baraja.subList(0, 10));
            assertEquals(fuerzaBruta(mano), evaluador.calcularCartasSueltas(mano, cfg).puntos(), "mano " + mano);
        }
    }

    /** Recorre todos los conjuntos de combinaciones disjuntas (incluir o excluir cada una). */
    private static int fuerzaBruta(List<Carta> mano) {
        List<Integer> combos = new ArrayList<>();
        for (int m = 1; m < (1 << mano.size()); m++) {
            int k = Integer.bitCount(m);
            if (k >= 3 && k <= 5 && valida(mano, m)) {
                combos.add(m);
            }
        }
        return probar(mano, combos, 0, 0);
    }

    private static int probar(List<Carta> mano, List<Integer> combos, int i, int usadas) {
        if (i == combos.size()) {
            int s = 0;
            for (int j = 0; j < mano.size(); j++) {
                if ((usadas & (1 << j)) == 0) {
                    s += TablaPuntos.puntos(mano.get(j));
                }
            }
            return s;
        }
        int sin = probar(mano, combos, i + 1, usadas);
        int m = combos.get(i);
        return (m & usadas) == 0 ? Math.min(sin, probar(mano, combos, i + 1, usadas | m)) : sin;
    }

    /** Validez escrita de forma independiente: grupo o escalera sin vuelta, As bajo o alto. */
    private static boolean valida(List<Carta> mano, int m) {
        List<Carta> c = new ArrayList<>();
        for (int j = 0; j < mano.size(); j++) {
            if ((m & (1 << j)) != 0) {
                c.add(mano.get(j));
            }
        }
        boolean grupo = c.stream().allMatch(x -> x.rango() == c.get(0).rango());
        if (grupo) {
            return true;
        }
        if (!c.stream().allMatch(x -> x.pinta() == c.get(0).pinta())) {
            return false;
        }
        return consecutivas(c.stream().map(Carta::rango).sorted().toList())
                || consecutivas(c.stream().map(x -> x.rango() == 1 ? 14 : x.rango()).sorted().toList());
    }

    private static boolean consecutivas(List<Integer> r) {
        for (int j = 1; j < r.size(); j++) {
            if (r.get(j) != r.get(j - 1) + 1) {
                return false;
            }
        }
        return true;
    }
}
