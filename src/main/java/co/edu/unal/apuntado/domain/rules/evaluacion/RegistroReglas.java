package co.edu.unal.apuntado.domain.rules.evaluacion;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro de plug-ins (ADR-04): descubre las estrategias declaradas en
 * META-INF/services. La modalidad es una clave abierta: agregar una variante no
 * modifica esta clase ni el orquestador.
 */
public final class RegistroReglas {

    private final Map<String, ReglaJuego> reglas = new ConcurrentHashMap<>();

    public RegistroReglas() {
        ServiceLoader.load(ReglaJuego.class, RegistroReglas.class.getClassLoader())
                .forEach(r -> reglas.put(r.modalidad(), r));
    }

    public Optional<ReglaJuego> obtener(String modalidad) {
        return Optional.ofNullable(reglas.get(modalidad));
    }

    public boolean soporta(String modalidad) {
        return reglas.containsKey(modalidad);
    }
}
