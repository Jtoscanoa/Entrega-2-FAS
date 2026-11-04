package co.edu.unal.apuntado.domain.gameplay.mazo;

import co.edu.unal.apuntado.domain.gameplay.auditoria.IAuditoriaReparto;
import co.edu.unal.apuntado.domain.gameplay.auditoria.OrdenMazo;
import co.edu.unal.apuntado.domain.gameplay.auditoria.Revelacion;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;

import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Baraja con azar criptográfico (Fisher–Yates), reparte, repone y suministra
 * cartas del mazo y de la pila (RF10, RF14, RF18).
 */
public final class DeckManagementComponent implements IMazo {

    private static final String PINTAS = "CDTP";
    private final SecureRandom azar = new SecureRandom();
    private final IAuditoriaReparto auditoria;
    private final Map<String, Mesa> mesas = new ConcurrentHashMap<>();

    public DeckManagementComponent(IAuditoriaReparto auditoria) {
        this.auditoria = auditoria;
    }

    /** Mazo y pila de una ronda. */
    private static final class Mesa {
        private final Deque<Carta> mazo = new ArrayDeque<>();
        private final Deque<Carta> descarte = new ArrayDeque<>();
    }

    @Override
    public String nuevaRonda(String ronda, ConfiguracionReglas cfg) {
        List<Carta> cartas = barajar(baraja(cfg));
        Mesa mesa = new Mesa();
        mesa.mazo.addAll(cartas);
        mesas.put(ronda, mesa);
        return auditoria.sellar(ronda, new OrdenMazo(cartas.stream().map(Carta::codigo).toList()));
    }

    private static List<Carta> baraja(ConfiguracionReglas cfg) {
        List<Carta> cartas = new ArrayList<>();
        int mazos = cfg.segundoMazo() ? 2 : 1;
        for (int m = 0; m < mazos; m++) {
            for (char p : PINTAS.toCharArray()) {
                for (int r = 1; r <= 13; r++) {
                    cartas.add(new Carta(r, p, false));
                }
            }
            if (cfg.comodinesHabilitados()) {
                cartas.add(Carta.COMODIN);
                cartas.add(Carta.COMODIN);
            }
        }
        return cartas;
    }

    private List<Carta> barajar(List<Carta> cartas) {
        for (int i = cartas.size() - 1; i > 0; i--) {
            int j = azar.nextInt(i + 1);
            Carta t = cartas.get(i);
            cartas.set(i, cartas.get(j));
            cartas.set(j, t);
        }
        return cartas;
    }

    @Override
    public List<List<Carta>> repartir(String ronda, int jugadores) {
        Mesa mesa = mesa(ronda);
        List<List<Carta>> manos = new ArrayList<>();
        for (int j = 0; j < jugadores; j++) {
            int n = j == 0 ? 11 : 10;
            List<Carta> mano = new ArrayList<>();
            for (int k = 0; k < n; k++) {
                mano.add(mesa.mazo.pop());
            }
            manos.add(mano);
        }
        return manos;
    }

    @Override
    public Carta robar(String ronda) {
        Mesa mesa = mesa(ronda);
        if (mesa.mazo.isEmpty()) {
            reponer(ronda, mesa);
        }
        return mesa.mazo.pop();
    }

    /** RF14: rebaraja la pila de descarte excepto su carta superior. */
    private void reponer(String ronda, Mesa mesa) {
        Carta cima = mesa.descarte.pop();
        List<Carta> resto = barajar(new ArrayList<>(mesa.descarte));
        mesa.descarte.clear();
        mesa.descarte.push(cima);
        mesa.mazo.addAll(resto);
        auditoria.sellar(ronda + "#reposicion", new OrdenMazo(resto.stream().map(Carta::codigo).toList()));
    }

    @Override
    public void descartar(String ronda, Carta carta) {
        mesa(ronda).descarte.push(carta);
    }

    @Override
    public Carta cimaDescarte(String ronda) {
        return mesa(ronda).descarte.peek();
    }

    @Override
    public RevelacionReparto revelar(String ronda) {
        Revelacion r = auditoria.revelar(ronda);
        return new RevelacionReparto(r.orden(), r.sal(), r.huella());
    }

    private Mesa mesa(String ronda) {
        Mesa m = mesas.get(ronda);
        if (m == null) {
            throw new IllegalStateException("Ronda desconocida: " + ronda);
        }
        return m;
    }
}
