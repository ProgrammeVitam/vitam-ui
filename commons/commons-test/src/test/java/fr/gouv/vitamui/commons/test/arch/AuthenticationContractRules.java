package fr.gouv.vitamui.commons.test.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Garde-fous du contrat d'authentification.
 *
 * Le contrat que le module IAM expose à un serveur d'authentification vit dans son propre module,
 * {@code iam-auth-contract}. Son intérêt tient entièrement à ce qu'il ne contient rien d'autre : dès
 * qu'un DTO d'administration ou un helper de protocole s'y invite, consommer le contrat redevient
 * consommer IAM en entier, et le découplage est perdu sans que rien ne le signale.
 *
 * Le garde-fou qui mord aujourd'hui est ailleurs : le pom du module bannit ces dépendances, et le build
 * échoue avant la compilation. Les règles qui suivent en sont le doublon lisible — elles nomment
 * l'intention là où le pom ne dit qu'une liste d'artefacts, et elles couvrent le jour où le module
 * gagnerait une dépendance transitive que la liste n'anticipait pas.
 *
 * Deux règles manquent encore, et ce sont les plus utiles. Un serveur d'authentification ne devrait
 * dépendre d'IAM que par ce contrat, et le contrat REST d'authentification ne devrait exposer que des
 * types de ce module — {@code CasController} renvoie aujourd'hui des DTO d'administration. Ni l'une ni
 * l'autre ne passe en l'état ; elles s'activeront au raccordement de cas-server et d'auth-server, et
 * leur place sera ici.
 */
public class AuthenticationContractRules {

    private static final String CONTRACT = "fr.gouv.vitamui.iam.auth.contract..";

    @ArchTest
    public static final ArchRule contract_does_not_depend_on_the_administration_model = noClasses()
        .that()
        .resideInAPackage(CONTRACT)
        .should()
        .dependOnClassesThat()
        .resideInAPackage("fr.gouv.vitamui.iam.common..")
        .because(
            "le contrat d'authentification doit rester consommable sans embarquer le modèle d'administration d'IAM"
        );

    @ArchTest
    public static final ArchRule contract_does_not_depend_on_the_server_layer = noClasses()
        .that()
        .resideInAPackage(CONTRACT)
        .should()
        .dependOnClassesThat()
        .resideInAPackage("fr.gouv.vitamui.iam.server..")
        .because("le contrat est publié vers l'extérieur : il ne peut pas dépendre de l'implémentation du serveur");

    @ArchTest
    public static final ArchRule contract_does_not_depend_on_an_authentication_protocol = noClasses()
        .that()
        .resideInAPackage(CONTRACT)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.pac4j..", "org.apereo..")
        .because(
            "la logique de protocole appartient au serveur d'authentification ; l'y laisser entrer recrée le cycle " +
            "qui a imposé de figer la version de pac4j lors de la montée en CAS 7.3.8"
        );
}
