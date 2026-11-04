package co.edu.unal.apuntado.domain.rules.evaluacion;

import co.edu.unal.apuntado.domain.rules.modelo.Agrupacion;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.Combinacion;
import co.edu.unal.apuntado.domain.rules.modelo.TipoCombinacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Representa cada subconjunto de la mano (hasta 11 cartas) como una máscara de
 * bits y enumera las combinaciones válidas de 3 a 5 cartas. En cada paso decide
 * el destino de la carta de menor índice aún sin ubicar, así cada agrupación se
 * explora una sola vez (E2 §1.4).
 */
public final class BuscadorCombinaciones {

    private static final int MIN = 3;
    private static final int MAX = 5;
    private static final int SIN_SOLUCION = Integer.MAX_VALUE;

    private final List<Carta> mano;
    private final boolean comodines;
    private final List<Integer> validas = new ArrayList<>();

    public BuscadorCombinaciones(List<Carta> mano, boolean comodinesHabilitados) {
        if (mano.size() > 11) {
            throw new IllegalArgumentException("La mano admite hasta 11 cartas");
        }
        this.mano = List.copyOf(mano);
        this.comodines = comodinesHabilitados;
        int todas = 1 << this.mano.size();
        for (int m = 1; m < todas; m++) {
            int k = Integer.bitCount(m);
            if (k >= MIN && k <= MAX && tipo(m) != null) {
                validas.add(m);
            }
        }
    }

    // ------------------------------------------------------------------ validez

    /** Devuelve el tipo de la combinación o null si no es válida. */
    TipoCombinacion tipo(int mascara) {
        List<Carta> cartas = cartas(mascara);
        List<Carta> naturales = cartas.stream().filter(c -> !c.comodin()).toList();
        if (!admisible(cartas.size(), naturales.size())) {
            return null;
        }
        if (esGrupo(naturales)) {
            return TipoCombinacion.GRUPO;
        }
        return esEscalera(naturales, cartas.size()) ? TipoCombinacion.ESCALERA : null;
    }

    private boolean admisible(int total, int naturales) {
        int comodinesUsados = total - naturales;
        boolean comodinPermitido = comodinesUsados == 0 || comodines;
        return comodinPermitido && naturales >= 2;
    }

    private static boolean esGrupo(List<Carta> naturales) {
        int rango = naturales.get(0).rango();
        return naturales.stream().allMatch(c -> c.rango() == rango);
    }

    private static boolean esEscalera(List<Carta> naturales, int tamano) {
        char pinta = naturales.get(0).pinta();
        if (!naturales.stream().allMatch(c -> c.pinta() == pinta)) {
            return false;
        }
        int[] bajos = naturales.stream().mapToInt(Carta::rango).sorted().toArray();
        int[] altos = naturales.stream().mapToInt(c -> c.rango() == 1 ? 14 : c.rango()).sorted().toArray();
        return cabeEnVentana(bajos, tamano) || cabeEnVentana(altos, tamano);
    }

    /** Rangos distintos que caben en una ventana de 'tamano' posiciones (sin vuelta). */
    private static boolean cabeEnVentana(int[] rangos, int tamano) {
        boolean distintos = Arrays.stream(rangos).distinct().count() == rangos.length;
        return distintos && rangos[rangos.length - 1] - rangos[0] < tamano;
    }

    // --------------------------------------------------------------- particiones

    /** Partición exacta de toda la mano con los tamaños indicados (p. ej. 4, 3, 3). */
    public List<Combinacion> particionExacta(int... tamanos) {
        int[] faltan = new int[MAX + 1];
        for (int t : tamanos) {
            faltan[t]++;
        }
        List<Integer> elegidas = new ArrayList<>();
        return cubrir(todas(), faltan, elegidas) ? aCombinaciones(elegidas) : null;
    }

    /** Partición de toda la mano en combinaciones de 3 a 5 cartas (cierre completo). */
    public List<Combinacion> particionLibre() {
        List<Integer> elegidas = new ArrayList<>();
        return cubrir(todas(), null, elegidas) ? aCombinaciones(elegidas) : null;
    }

    private boolean cubrir(int restante, int[] faltan, List<Integer> elegidas) {
        if (restante == 0) {
            return faltan == null || Arrays.stream(faltan).allMatch(f -> f == 0);
        }
        int menor = Integer.lowestOneBit(restante);
        for (int m : validas) {
            if (aplicable(m, menor, restante, faltan)) {
                usar(m, faltan, -1);
                elegidas.add(m);
                if (cubrir(restante & ~m, faltan, elegidas)) {
                    return true;
                }
                elegidas.remove(elegidas.size() - 1);
                usar(m, faltan, +1);
            }
        }
        return false;
    }

    private static boolean aplicable(int m, int menor, int restante, int[] faltan) {
        boolean contiene = (m & menor) != 0 && (m & restante) == m;
        return contiene && (faltan == null || faltan[Integer.bitCount(m)] > 0);
    }

    private static void usar(int m, int[] faltan, int delta) {
        if (faltan != null) {
            faltan[Integer.bitCount(m)] += delta;
        }
    }

    /**
     * Elige 'cantidad' ternas disjuntas que minimizan el valor del sobrante
     * (cierre por apuntado). Devuelve null si no existen.
     */
    public Agrupacion mejorParticionConSobrantes(int cantidad) {
        List<Integer> ternas = validas.stream().filter(m -> Integer.bitCount(m) == MIN).toList();
        Mejor mejor = new Mejor();
        buscarTernas(ternas, 0, cantidad, 0, new ArrayList<>(), mejor);
        return mejor.puntos == SIN_SOLUCION ? null : agrupacionDe(mejor.elegidas);
    }

    private void buscarTernas(List<Integer> ternas, int desde, int faltan, int usadas, List<Integer> actual,
                              Mejor mejor) {
        if (faltan == 0) {
            mejor.considerar(puntos(todas() & ~usadas), actual);
            return;
        }
        for (int i = desde; i < ternas.size(); i++) {
            int m = ternas.get(i);
            if ((m & usadas) == 0) {
                actual.add(m);
                buscarTernas(ternas, i + 1, faltan - 1, usadas | m, actual, mejor);
                actual.remove(actual.size() - 1);
            }
        }
    }

    /** Mejor selección de ternas encontrada hasta el momento. */
    private static final class Mejor {
        private int puntos = SIN_SOLUCION;
        private List<Integer> elegidas = List.of();

        void considerar(int candidato, List<Integer> actual) {
            if (candidato < puntos) {
                puntos = candidato;
                elegidas = List.copyOf(actual);
            }
        }
    }

    // --------------------------------------------------- cartas sueltas (RF07)

    /**
     * Programación dinámica sobre subconjuntos (como máximo 2.048 estados):
     * f(S) = mín(puntos(c) + f(S − {c}), mín f(S − m) con c ∈ m ⊆ S), donde c es
     * la carta de menor índice de S.
     */
    public Agrupacion minimizarSueltas() {
        int estados = 1 << mano.size();
        int[] f = new int[estados];
        int[] eleccion = new int[estados];
        for (int s = 1; s < estados; s++) {
            resolverEstado(s, f, eleccion);
        }
        return reconstruir(f, eleccion);
    }

    private void resolverEstado(int s, int[] f, int[] eleccion) {
        int menor = Integer.lowestOneBit(s);
        f[s] = TablaPuntos.puntos(mano.get(Integer.numberOfTrailingZeros(menor))) + f[s & ~menor];
        eleccion[s] = menor;
        for (int m : validas) {
            if ((m & menor) != 0 && (m & s) == m && f[s & ~m] < f[s]) {
                f[s] = f[s & ~m];
                eleccion[s] = m;
            }
        }
    }

    private Agrupacion reconstruir(int[] f, int[] eleccion) {
        List<Combinacion> combinaciones = new ArrayList<>();
        List<Carta> sueltas = new ArrayList<>();
        int s = todas();
        while (s != 0) {
            int m = eleccion[s];
            if (Integer.bitCount(m) == 1) {
                sueltas.add(mano.get(Integer.numberOfTrailingZeros(m)));
            } else {
                combinaciones.add(new Combinacion(cartas(m), tipo(m)));
            }
            s &= ~m;
        }
        return new Agrupacion(f[todas()], combinaciones, sueltas);
    }

    // ---------------------------------------------------------------- utilidades

    /** Cierre a pintas: todas las cartas naturales de la misma pinta (RF12). */
    public boolean aPintas() {
        List<Carta> naturales = mano.stream().filter(c -> !c.comodin()).toList();
        return !naturales.isEmpty() && naturales.stream().allMatch(c -> c.pinta() == naturales.get(0).pinta());
    }

    private Agrupacion agrupacionDe(List<Integer> elegidas) {
        int usadas = elegidas.stream().reduce(0, (a, b) -> a | b);
        List<Combinacion> combinaciones = aCombinaciones(elegidas);
        return new Agrupacion(puntos(todas() & ~usadas), combinaciones, cartas(todas() & ~usadas));
    }

    private int puntos(int mascara) {
        return cartas(mascara).stream().mapToInt(TablaPuntos::puntos).sum();
    }

    private List<Carta> cartas(int mascara) {
        List<Carta> r = new ArrayList<>();
        for (int i = 0; i < mano.size(); i++) {
            if ((mascara & (1 << i)) != 0) {
                r.add(mano.get(i));
            }
        }
        return r;
    }

    private int todas() {
        return (1 << mano.size()) - 1;
    }

    private List<Combinacion> aCombinaciones(List<Integer> mascaras) {
        return mascaras.stream().map(m -> new Combinacion(cartas(m), tipo(m))).toList();
    }
}
