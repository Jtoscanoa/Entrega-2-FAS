package co.edu.unal.apuntado.domain.gameplay;

import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/** Estado en memoria de una partida; lo protege su candado (un escritor por partida). */
final class Mesa {
    final ReentrantLock candado = new ReentrantLock();
    final String id;
    final ConfiguracionReglas cfg;
    final List<String> jugadores;
    final Map<String, List<Carta>> manos = new HashMap<>();
    final Map<String, Integer> marcador = new HashMap<>();
    int numeroRonda;
    int numeroTurno;
    int indiceTurno;
    boolean robo;
    String huella;

    Mesa(String id, ConfiguracionReglas cfg, List<String> jugadores) {
        this.id = id;
        this.cfg = cfg;
        this.jugadores = List.copyOf(jugadores);
        jugadores.forEach(j -> marcador.put(j, 0));
    }

    String ronda() {
        return id + "#" + numeroRonda;
    }

    String turnoDe() {
        return jugadores.get(indiceTurno);
    }

    List<Carta> manoDe(String jugador) {
        return manos.computeIfAbsent(jugador, j -> new ArrayList<>());
    }

    void siguienteTurno() {
        numeroTurno++;
        indiceTurno = (indiceTurno + 1) % jugadores.size();
        robo = false;
    }
}
