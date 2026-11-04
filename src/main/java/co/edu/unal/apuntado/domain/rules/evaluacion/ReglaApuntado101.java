package co.edu.unal.apuntado.domain.rules.evaluacion;

import co.edu.unal.apuntado.domain.rules.modelo.Agrupacion;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.Combinacion;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;
import co.edu.unal.apuntado.domain.rules.modelo.TipoCierre;

import java.util.List;

/**
 * Plug-in del Apuntado (RF05, RF12). Prueba del cierre más favorable al menos
 * favorable: a pintas (−25), completo (−10) y 3 ternas con residuo ≤ máximo.
 */
public final class ReglaApuntado101 implements ReglaJuego {

    private static final int TERNAS = 3;

    @Override
    public String modalidad() {
        return ConfiguracionReglas.APUNTADO;
    }

    @Override
    public ResultadoValidacion evaluarCierre(List<Carta> mano10, ConfiguracionReglas cfg) {
        BuscadorCombinaciones b = new BuscadorCombinaciones(mano10, cfg.comodinesHabilitados());
        if (cfg.cierreAPintas() && b.aPintas()) {
            return ResultadoValidacion.aceptar(TipoCierre.A_PINTAS, -25, List.of());
        }
        List<Combinacion> completo = b.particionLibre();
        if (completo != null) {
            return ResultadoValidacion.aceptar(TipoCierre.COMPLETO, -10, completo);
        }
        return conResiduo(b.mejorParticionConSobrantes(TERNAS), cfg);
    }

    private static ResultadoValidacion conResiduo(Agrupacion ternas, ConfiguracionReglas cfg) {
        if (ternas == null) {
            return ResultadoValidacion.rechazar(ResultadoValidacion.ESTRUCTURA_INVALIDA);
        }
        return ternas.puntos() <= cfg.residuoMaximo()
                ? ResultadoValidacion.aceptar(TipoCierre.APUNTADO, ternas.puntos(), ternas.combinaciones())
                : ResultadoValidacion.rechazar(ResultadoValidacion.RESIDUO_EXCEDIDO);
    }
}
