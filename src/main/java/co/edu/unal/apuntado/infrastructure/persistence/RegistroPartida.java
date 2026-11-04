package co.edu.unal.apuntado.infrastructure.persistence;

import java.util.List;

/** Fila de partida con su configuración congelada (tablas partida y configuracion_reglas). */
public record RegistroPartida(String id, String sala, String modalidad, List<String> participantes) {
    public RegistroPartida {
        participantes = List.copyOf(participantes);
    }
}
