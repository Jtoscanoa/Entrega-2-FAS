package co.edu.unal.apuntado.domain.lobby;

import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;

/** Interfaz provista por LobbyPartyComponent. */
public interface ILobby {
    String crearSala(String anfitrion, ConfiguracionReglas cfg);

    int unirse(String codigo, String jugador);

    String iniciar(String codigo, String anfitrion);
}
