package co.edu.unal.apuntado.domain.rules.evaluacion;

import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.Combinacion;
import co.edu.unal.apuntado.domain.rules.modelo.ConfiguracionReglas;
import co.edu.unal.apuntado.domain.rules.modelo.ResultadoValidacion;
import co.edu.unal.apuntado.domain.rules.modelo.TipoCierre;

import java.util.List;

/** Plug-in de la Clásica (RF06): exactamente 1 cuarta y 2 ternas, sin sobrantes. */
public final class ReglaClasicaCuartaTerna implements ReglaJuego {

    @Override
    public String modalidad() {
        return ConfiguracionReglas.CLASICA;
    }

    @Override
    public ResultadoValidacion evaluarCierre(List<Carta> mano10, ConfiguracionReglas cfg) {
        BuscadorCombinaciones b = new BuscadorCombinaciones(mano10, cfg.comodinesHabilitados());
        List<Combinacion> particion = b.particionExacta(4, 3, 3);
        return particion != null
                ? ResultadoValidacion.aceptar(TipoCierre.CLASICO_433, -10, particion)
                : ResultadoValidacion.rechazar(ResultadoValidacion.ESTRUCTURA_INVALIDA);
    }
}
