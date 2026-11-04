package co.edu.unal.apuntado.domain.gameplay;

import java.util.Map;

/**
 * Evento de dominio publicado después del commit (ADR-06). Si destinatario es
 * null el evento es público (/topic); si no, es privado (/user/queue).
 */
public record EventoPartida(String partida, String tipo, String destinatario, Map<String, Object> datos) {
    public EventoPartida {
        datos = Map.copyOf(datos);
    }
}
