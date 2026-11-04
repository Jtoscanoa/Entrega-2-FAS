package co.edu.unal.apuntado;

import co.edu.unal.apuntado.domain.gameplay.TurnOrchestratorComponent;
import co.edu.unal.apuntado.domain.gameplay.auditoria.DealAuditComponent;
import co.edu.unal.apuntado.domain.gameplay.bot.BotEngineComponent;
import co.edu.unal.apuntado.domain.gameplay.mazo.DeckManagementComponent;
import co.edu.unal.apuntado.domain.lobby.LobbyPartyComponent;
import co.edu.unal.apuntado.domain.rules.GameConfigurationComponent;
import co.edu.unal.apuntado.domain.rules.HandEvaluatorComponent;
import co.edu.unal.apuntado.domain.rules.RoundScoringComponent;
import co.edu.unal.apuntado.domain.wager.WagerAndEconomyComponent;
import co.edu.unal.apuntado.infrastructure.persistence.MatchStateRepository;
import co.edu.unal.apuntado.infrastructure.realtime.SessionGatewayComponent;

/**
 * Raíz de composición del esqueleto: conecta los 11 componentes por sus
 * interfaces (17 dependencias, Figura 1). En el E3 este papel lo asume Spring.
 */
public final class Aplicacion {

    private Aplicacion() {
    }

    public static SessionGatewayComponent ensamblar() {
        MatchStateRepository repositorio = new MatchStateRepository();
        HandEvaluatorComponent evaluador = new HandEvaluatorComponent();
        GameConfigurationComponent configuracion = new GameConfigurationComponent();
        WagerAndEconomyComponent economia = new WagerAndEconomyComponent(repositorio);
        DeckManagementComponent mazo = new DeckManagementComponent(new DealAuditComponent());
        TurnOrchestratorComponent orquestador = new TurnOrchestratorComponent(mazo,
                new BotEngineComponent(evaluador), evaluador, new RoundScoringComponent(), configuracion,
                economia, repositorio);
        LobbyPartyComponent lobby = new LobbyPartyComponent(orquestador, configuracion, economia, repositorio);
        return new SessionGatewayComponent(lobby, orquestador, orquestador);
    }
}
