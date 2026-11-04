package co.edu.unal.apuntado.domain.rules;

import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Valida y conserva los parámetros de cada sala y entrega la clave de modalidad
 * (RF09). Los parámetros cambian sin recompilar ni redesplegar (E1, FF-12).
 */
public final class GameConfigurationComponent implements IConfiguracion {

    private static final int CARTAS_POR_MAZO = 52;
    private static final int MIN_JUGADORES = 2;
    private static final int MAX_JUGADORES = 6;
    /** Con 5 jugadores quedaría 1 sola carta de reserva: se exige segundo mazo (Tabla 4). */
    private static final int JUGADORES_SEGUNDO_MAZO = 5;

    private final Map<String, ConfiguracionReglas> porSala = new ConcurrentHashMap<>();

    @Override
    public void registrar(String sala, ConfiguracionReglas cfg) {
        porSala.put(sala, cfg);
    }

    @Override
    public ConfiguracionReglas obtener(String sala) {
        ConfiguracionReglas cfg = porSala.get(sala);
        if (cfg == null) {
            throw new IllegalArgumentException("Sala sin configuración: " + sala);
        }
        return cfg;
    }

    @Override
    public void validarMesa(ConfiguracionReglas cfg, int jugadores) {
        if (jugadores < MIN_JUGADORES || jugadores > MAX_JUGADORES) {
            throw new IllegalArgumentException("La mesa admite de 2 a 6 jugadores");
        }
        if (jugadores >= JUGADORES_SEGUNDO_MAZO && !cfg.segundoMazo()) {
            throw new IllegalArgumentException("Con 5 o 6 jugadores se exige segundo mazo: "
                    + (jugadores * 10 + 1) + " cartas repartidas de " + CARTAS_POR_MAZO);
        }
    }
}
