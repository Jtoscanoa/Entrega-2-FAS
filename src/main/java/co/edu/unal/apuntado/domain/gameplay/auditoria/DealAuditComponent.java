package co.edu.unal.apuntado.domain.gameplay.auditoria;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sella cada mazo con SHA-256(orden | sal) y lo revela al liquidar (ADR-03).
 * Fase 1: funcional pero sin la verificación pública ni FF-11, que van en Fase 2
 * (Tabla 10b).
 */
public final class DealAuditComponent implements IAuditoriaReparto {

    private final SecureRandom azar = new SecureRandom();
    private final Map<String, Revelacion> sellos = new ConcurrentHashMap<>();

    @Override
    public String sellar(String ronda, OrdenMazo orden) {
        byte[] bytes = new byte[16];
        azar.nextBytes(bytes);
        String sal = HexFormat.of().formatHex(bytes);
        String huella = huella(orden.serializado(), sal);
        sellos.put(ronda, new Revelacion(orden.codigos(), sal, huella));
        return huella;
    }

    @Override
    public Revelacion revelar(String ronda) {
        Revelacion r = sellos.get(ronda);
        if (r == null) {
            throw new IllegalStateException("Ronda sin sello: " + ronda);
        }
        return r;
    }

    /** SHA-256 de "orden|sal" en hexadecimal; cualquier jugador puede recalcularla. */
    public static String huella(String ordenSerializado, String sal) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] d = sha.digest((ordenSerializado + "|" + sal).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
