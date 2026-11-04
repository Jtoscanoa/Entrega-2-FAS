package co.edu.unal.apuntado.arquitectura;

import co.edu.unal.apuntado.domain.gameplay.auditoria.OrdenMazo;
import co.edu.unal.apuntado.domain.rules.evaluacion.ReglaJuego;
import co.edu.unal.apuntado.domain.rules.modelo.Carta;
import co.edu.unal.apuntado.domain.rules.modelo.Combinacion;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameMatching;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Fitness functions estáticas y atómicas (Tabla 11). Se ejecutan en cada push
 * con mvn verify; un build rojo bloquea la fusión (Figura 6).
 */
@AnalyzeClasses(packages = "co.edu.unal.apuntado", importOptions = ImportOption.DoNotIncludeTests.class)
class ReglasArquitecturaTest {

    @ArchTest // FF-01 · ADR-01, E3: el motor de reglas es Java puro
    static final ArchRule ff01_reglas_aisladas = noClasses()
            .that().resideInAPackage("..domain.rules..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..",
                    "..domain.gameplay..", "..domain.lobby..", "..domain.wager..",
                    "org.springframework..", "jakarta..", "java.sql..");

    @ArchTest // FF-01b · ADR-04: el núcleo compartido no depende de nada fuera de java.*
    static final ArchRule ff01b_nucleo_modelo_puro = classes()
            .that().resideInAPackage("..domain.rules.modelo..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("..domain.rules.modelo..", "java..");

    @ArchTest // FF-01c · R2: TurnOrchestrator como mediador estricto (sin cálculos)
    static final ArchRule ff01c_orquestador_sin_calculo_de_cartas = noClasses()
            .that().haveSimpleName("TurnOrchestratorComponent")
            .should().callMethodWhere(target(owner(assignableTo(Carta.class).or(assignableTo(Combinacion.class))))
                    .and(target(nameMatching("puntos|valor|rango|compareTo|sumar.*|calcular.*"))))
            .because("La evaluación corresponde a IEvaluadorMano y la puntuación a ILiquidacion");

    @ArchTest // FF-02 · ADR-01: sin ciclos entre dominios
    static final ArchRule ff02a_sin_ciclos_entre_dominios = slices()
            .matching("co.edu.unal.apuntado.domain.(*)..").should().beFreeOfCycles();

    @ArchTest // FF-02 · sin ciclos entre ningún par de paquetes
    static final ArchRule ff02b_sin_ciclos_entre_paquetes = slices()
            .matching("co.edu.unal.apuntado.(**)").should().beFreeOfCycles();

    @ArchTest // FF-03 · E2: ReglaClasica y ReglaApuntado no dependen entre sí
    static final ArchRule ff03_variantes_independientes = classes()
            .that().implement(ReglaJuego.class)
            .should(noDependerDeOtraVariante());

    @ArchTest // FF-04 · ADR-04: entre componentes solo se usan interfaces
    static final ArchRule ff04_solo_interfaces_entre_componentes = noClasses()
            .that().haveSimpleNameEndingWith("Component").or().haveSimpleName("MatchStateRepository")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Component")
            .orShould().dependOnClassesThat().haveSimpleName("MatchStateRepository");

    @ArchTest // FF-05 · ADR-03, E6: el orden secreto del mazo no sale de mazo y auditoría
    static final ArchRule ff05_orden_del_mazo = noClasses()
            .that().resideOutsideOfPackages("..gameplay.mazo..", "..gameplay.auditoria..")
            .should().dependOnClassesThat().areAssignableTo(OrdenMazo.class);

    @ArchTest // FF-06 · ADR-06: el gateway no contiene reglas; solo usa ILobby, ITurno e IEventosPartida
    static final ArchRule ff06_gateway_solo_por_interfaces = classes()
            .that().resideInAPackage("..infrastructure.realtime..")
            .should().onlyDependOnClassesThat(resideInAnyPackage("..infrastructure.realtime..", "java..")
                    .or(simpleName("ILobby")).or(simpleName("ITurno")).or(simpleName("IEventosPartida"))
                    .or(simpleName("EventoPartida")).or(simpleName("ResultadoJugada"))
                    .or(simpleName("VistaPartida")));

    @ArchTest // FF-07 · guía §5.1: la entrada no accede a la persistencia ni a entidades
    static final ArchRule ff07_entrada_sin_persistencia = noClasses()
            .that().resideInAPackage("..infrastructure.realtime..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("Entity");

    @ArchTest // FF-08b · ADR-05: la persistencia no depende de ningún dominio
    static final ArchRule ff08b_persistencia_sin_dominio = noClasses()
            .that().resideInAPackage("..infrastructure.persistence..")
            .should().dependOnClassesThat().resideInAPackage("..domain..");

    /** Condición propia de FF-03: una variante no puede usar otra implementación de ReglaJuego. */
    private static ArchCondition<JavaClass> noDependerDeOtraVariante() {
        return new ArchCondition<>("no depender de otra variante de ReglaJuego") {
            @Override
            public void check(JavaClass variante, ConditionEvents events) {
                variante.getDirectDependenciesFromSelf().stream()
                        .map(d -> d.getTargetClass())
                        .filter(t -> !t.equals(variante) && t.isAssignableTo(ReglaJuego.class) && !t.isInterface())
                        .forEach(t -> events.add(SimpleConditionEvent.violated(variante,
                                variante.getSimpleName() + " depende de la variante " + t.getSimpleName())));
            }
        };
    }
}
