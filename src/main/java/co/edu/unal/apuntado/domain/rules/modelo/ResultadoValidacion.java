package co.edu.unal.apuntado.domain.rules.modelo;

import java.util.List;

/**
 * Resultado inmutable de validar un cierre. Si se rechaza, el motivo se muestra
 * al jugador (RF16).
 */
public record ResultadoValidacion(boolean aceptado, TipoCierre tipo, int puntos, String motivo,
                                  List<Combinacion> combinaciones) {

    public static final String MODALIDAD_DESCONOCIDA = "MODALIDAD_DESCONOCIDA";
    public static final String NUMERO_DE_CARTAS = "NUMERO_DE_CARTAS";
    public static final String ESTRUCTURA_INVALIDA = "ESTRUCTURA_INVALIDA";
    public static final String RESIDUO_EXCEDIDO = "RESIDUO_EXCEDIDO";

    public ResultadoValidacion {
        combinaciones = combinaciones == null ? List.of() : List.copyOf(combinaciones);
    }

    public static ResultadoValidacion aceptar(TipoCierre tipo, int puntos, List<Combinacion> combinaciones) {
        return new ResultadoValidacion(true, tipo, puntos, null, combinaciones);
    }

    public static ResultadoValidacion rechazar(String motivo) {
        return new ResultadoValidacion(false, null, 0, motivo, List.of());
    }
}
