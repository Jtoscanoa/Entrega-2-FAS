package co.edu.unal.apuntado.domain.gameplay.mazo;

import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;

import java.util.List;

/** Interfaz provista por DeckManagementComponent. Nunca expone el orden del mazo. */
public interface IMazo {
    /** Baraja, sella y devuelve la huella que se publica antes de repartir. */
    String nuevaRonda(String ronda, ConfiguracionReglas cfg);

    /** Reparte 11 cartas al primero (quien abre) y 10 a cada uno de los demás. */
    List<List<Carta>> repartir(String ronda, int jugadores);

    Carta robar(String ronda);

    void descartar(String ronda, Carta carta);

    Carta cimaDescarte(String ronda);

    RevelacionReparto revelar(String ronda);
}
