package co.edu.unal.apuntado.infrastructure.persistence;

/**
 * Interfaz provista por MatchStateRepository. Sus tipos son propios del paquete:
 * la persistencia no depende de ningún dominio (FF-08b).
 */
public interface IMatchState {
    void guardarPartida(RegistroPartida partida);

    void registrarInicioRonda(String partida, int numero, String huella);

    /**
     * Persiste la liquidación completa en una sola operación atómica. Devuelve
     * false, sin escribir nada, si la ronda ya estaba liquidada (cierre
     * concurrente o reintento): equivale al UPDATE condicional sobre EN_CURSO.
     */
    boolean guardarRonda(RegistroRonda ronda);

    int saldoDe(String jugador);

    void registrarMovimiento(String jugador, String partida, String tipo, int monto);
}
