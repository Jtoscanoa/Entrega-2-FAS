package co.edu.unal.apuntado.domain.gameplay;

import java.util.function.Consumer;

/** Interfaz provista por TurnOrchestratorComponent para suscribirse a eventos. */
public interface IEventosPartida {
    void suscribir(Consumer<EventoPartida> oyente);
}
