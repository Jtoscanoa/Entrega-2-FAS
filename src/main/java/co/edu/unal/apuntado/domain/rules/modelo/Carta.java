package co.edu.unal.apuntado.domain.rules.modelo;

import java.util.Objects;

/**
 * Carta de la baraja. La identidad es rango + pinta (E1, Fig. 9); el puntaje
 * pertenece a las reglas (TablaPuntos), no a la carta.
 * Rango: 1 = As, 11 = J, 12 = Q, 13 = K. Pintas: C (corazones), D (diamantes),
 * T (tréboles), P (picas). El comodín no tiene rango ni pinta.
 */
public record Carta(int rango, char pinta, boolean comodin) {

    public static final Carta COMODIN = new Carta(0, '*', true);

    public Carta {
        if (!comodin && (rango < 1 || rango > 13 || "CDTP".indexOf(pinta) < 0)) {
            throw new IllegalArgumentException("Carta inválida: " + rango + pinta);
        }
    }

    /** Crea una carta desde su código: "7C", "10T", "AP", "QD" o "*". */
    public static Carta of(String codigo) {
        Objects.requireNonNull(codigo, "codigo");
        if ("*".equals(codigo)) {
            return COMODIN;
        }
        String r = codigo.substring(0, codigo.length() - 1);
        char p = codigo.charAt(codigo.length() - 1);
        int rango = switch (r) {
            case "A" -> 1;
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            default -> Integer.parseInt(r);
        };
        return new Carta(rango, p, false);
    }

    public String codigo() {
        if (comodin) {
            return "*";
        }
        String r = switch (rango) {
            case 1 -> "A";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> Integer.toString(rango);
        };
        return r + pinta;
    }

    @Override
    public String toString() {
        return codigo();
    }
}
