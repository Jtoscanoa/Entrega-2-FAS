package co.edu.unal.apuntado.domain.rules.modelo;

/**
 * Parámetros de la sala (RF09). Son datos: cambian sin recompilar (E1). La
 * modalidad es una clave abierta que el registro de reglas resuelve (ADR-04).
 */
public record ConfiguracionReglas(
        String modalidad,
        int umbralEliminacion,
        int residuoMaximo,
        boolean comodinesHabilitados,
        boolean robarDelDescarte,
        boolean reingresoPermitido,
        int cuotaReingreso,
        int maxReingresos,
        int apuesta,
        int metaVictoria,
        int maxRondas,
        boolean cierreAPintas,
        boolean segundoMazo,
        int segundosPorTurno) {

    public static final String APUNTADO = "APUNTADO_101";
    public static final String CLASICA = "CLASICA_433";

    public ConfiguracionReglas {
        if (modalidad == null || modalidad.isBlank()) {
            throw new IllegalArgumentException("modalidad requerida");
        }
        if (umbralEliminacion < 1 || residuoMaximo < 0 || residuoMaximo > 4) {
            throw new IllegalArgumentException("umbral o residuo máximo fuera de rango");
        }
        if (apuesta < 0 || cuotaReingreso < 0 || maxReingresos < 0 || maxRondas < 1 || segundosPorTurno < 5) {
            throw new IllegalArgumentException("parámetro negativo o fuera de rango");
        }
    }

    /** Valores por defecto del Apuntado a 101 (E1, tabla de reglas consolidadas). */
    public static ConfiguracionReglas apuntado() {
        return new ConfiguracionReglas(APUNTADO, 101, 4, false, false, false, 0, 0, 0, -51, 99, false, false, 30);
    }

    /** La Clásica 4-3-3 es una partida atómica: máximo de rondas = 1 (Tabla 1). */
    public static ConfiguracionReglas clasica() {
        return new ConfiguracionReglas(CLASICA, 101, 4, false, false, false, 0, 0, 0, -51, 1, false, false, 30);
    }

    public ConfiguracionReglas conModalidad(String nueva) {
        return new ConfiguracionReglas(nueva, umbralEliminacion, residuoMaximo, comodinesHabilitados,
                robarDelDescarte, reingresoPermitido, cuotaReingreso, maxReingresos, apuesta, metaVictoria,
                maxRondas, cierreAPintas, segundoMazo, segundosPorTurno);
    }

    public ConfiguracionReglas conUmbral(int umbral) {
        return new ConfiguracionReglas(modalidad, umbral, residuoMaximo, comodinesHabilitados, robarDelDescarte,
                reingresoPermitido, cuotaReingreso, maxReingresos, apuesta, metaVictoria, maxRondas,
                cierreAPintas, segundoMazo, segundosPorTurno);
    }

    public ConfiguracionReglas conComodines(boolean habilitados) {
        return new ConfiguracionReglas(modalidad, umbralEliminacion, residuoMaximo, habilitados, robarDelDescarte,
                reingresoPermitido, cuotaReingreso, maxReingresos, apuesta, metaVictoria, maxRondas,
                cierreAPintas, segundoMazo, segundosPorTurno);
    }

    public ConfiguracionReglas conResiduoMaximo(int residuo) {
        return new ConfiguracionReglas(modalidad, umbralEliminacion, residuo, comodinesHabilitados, robarDelDescarte,
                reingresoPermitido, cuotaReingreso, maxReingresos, apuesta, metaVictoria, maxRondas,
                cierreAPintas, segundoMazo, segundosPorTurno);
    }

    public ConfiguracionReglas conCierreAPintas(boolean habilitado) {
        return new ConfiguracionReglas(modalidad, umbralEliminacion, residuoMaximo, comodinesHabilitados,
                robarDelDescarte, reingresoPermitido, cuotaReingreso, maxReingresos, apuesta, metaVictoria,
                maxRondas, habilitado, segundoMazo, segundosPorTurno);
    }

    public ConfiguracionReglas conSegundoMazo(boolean habilitado) {
        return new ConfiguracionReglas(modalidad, umbralEliminacion, residuoMaximo, comodinesHabilitados,
                robarDelDescarte, reingresoPermitido, cuotaReingreso, maxReingresos, apuesta, metaVictoria,
                maxRondas, cierreAPintas, habilitado, segundosPorTurno);
    }
}
