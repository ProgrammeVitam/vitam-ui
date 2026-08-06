package fr.gouv.vitamui.commons.test.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Guardrails of the authentication contract.
 *
 * The contract IAM exposes to an authentication server lives in its own module,
 * {@code iam-auth-contract}. Its whole value lies in holding nothing else: the moment an administration
 * DTO or a protocol helper slips in, consuming the contract becomes consuming the whole of IAM again,
 * and the decoupling is lost without anything saying so.
 *
 * The guardrail that actually bites today is elsewhere: the module's pom bans these dependencies and the
 * build fails before compilation. The rules below are its readable counterpart — they name the intent
 * where the pom only lists artifacts, and they cover the day the module gains a transitive dependency
 * the list did not anticipate.
 *
 * Two rules are still missing, and they are the most useful ones. An authentication server should only
 * depend on IAM through this contract, and the authentication REST contract should only expose types
 * from this module — {@code CasController} still returns administration DTOs. Neither passes as things
 * stand; they will be switched on when cas-server and auth-server are wired to the contract, and this is
 * where they belong.
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
        .because("the authentication contract must stay consumable without dragging in IAM's administration model");

    @ArchTest
    public static final ArchRule contract_does_not_depend_on_the_server_layer = noClasses()
        .that()
        .resideInAPackage(CONTRACT)
        .should()
        .dependOnClassesThat()
        .resideInAPackage("fr.gouv.vitamui.iam.server..")
        .because("the contract is published outwards: it cannot depend on the server implementation");

    @ArchTest
    public static final ArchRule contract_does_not_depend_on_an_authentication_protocol = noClasses()
        .that()
        .resideInAPackage(CONTRACT)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.pac4j..", "org.apereo..")
        .because(
            "protocol logic belongs to the authentication server; letting it in recreates the cycle that forced " +
            "the pac4j version to be pinned during the upgrade to CAS 7.3.8"
        );
}
