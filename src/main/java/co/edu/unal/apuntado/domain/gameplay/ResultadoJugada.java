package co.edu.unal.apuntado.domain.gameplay;

/** Respuesta a un comando: aceptado o rechazado con su motivo (RF16). */
public record ResultadoJugada(boolean aceptado, String motivo) {
    public static final ResultadoJugada OK = new ResultadoJugada(true, null);

    public static ResultadoJugada rechazo(String motivo) {
        return new ResultadoJugada(false, motivo);
    }
}
