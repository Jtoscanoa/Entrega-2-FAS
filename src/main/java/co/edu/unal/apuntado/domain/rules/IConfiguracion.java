package co.edu.unal.apuntado.domain.rules;

import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;

/** Interfaz provista por GameConfigurationComponent. */
public interface IConfiguracion {
    void registrar(String sala, ConfiguracionReglas cfg);

    ConfiguracionReglas obtener(String sala);

    void validarMesa(ConfiguracionReglas cfg, int jugadores);
}
