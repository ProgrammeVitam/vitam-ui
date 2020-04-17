package fr.gouv.vitamui.cucumber.back.steps.iam.user;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_UPDATE_ME_USERS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_UPDATE_USERS;
import static fr.gouv.vitamui.utils.TestConstants.CLIENT1_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Users dans IAM admin : opérations de mise à jour partielle.
 *
 *
 */
public class ApiIamUserPatchSteps extends CommonSteps {

    UserDto patchedUser;

    private Map<String, Object> buildUserToPatch() {
        patchedUser = FactoryDto.buildDto(UserDto.class);
        patchedUser.setId(testContext.savedUserDto.getId());
        final Map<String, Object> dto = new HashMap<>();
        dto.put("id", testContext.savedUserDto.getId());
        dto.put("firstname", testContext.savedUserDto.getFirstname());
        dto.put("lastname", TestConstants.UPDATED + testContext.savedUserDto.getLastname());
        return dto;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_partiellement_un_utilisateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        patchedUser = getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).patch(getSystemTenantUserAdminContext(), buildUserToPatch());
    }

    @Then("^le serveur retourne l'utilisateur mis à jour partiellement$")
    public void le_serveur_retourne_l_utilisateur_mis_à_jour_partiellement() {
        assertThat(patchedUser).isNotNull();
        assertThat(patchedUser.getId()).isEqualTo(testContext.savedUserDto.getId());
        assertThat(patchedUser.getEmail()).isEqualTo(testContext.savedUserDto.getEmail());
        assertThat(patchedUser.getLastname()).isEqualTo(TestConstants.UPDATED + testContext.savedUserDto.getLastname());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_partiellement_un_utilisateur_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        final Map<String, Object> patch = buildUserToPatch();
        patch.put("readonly", true);
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).patch(getSystemTenantUserAdminContext(), patch);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle de l'utilisateur à cause du readonly$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_de_l_utilisateur_à_cause_du_readonly() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch user "
                + patchedUser.getId() + patchedUser.getId() + " cannot patch readonly");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_partiellement_un_utilisateur_mais_avec_un_mauvais_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        final Map<String, Object> patch = buildUserToPatch();
        patch.put("customerId", CLIENT1_CUSTOMER_ID);
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).patch(getSystemTenantUserAdminContext(), patch);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle de l'utilisateur à cause du mauvais client$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_de_l_utilisateur_à_cause_du_mauvais_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch user: customerId " + CLIENT1_CUSTOMER_ID
                        + " is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur mais avec un email existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_partiellement_un_utilisateur_mais_avec_un_email_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        final Map<String, Object> patch = buildUserToPatch();
        patch.put("email", TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain);
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).patch(getSystemTenantUserAdminContext(), patch);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle de l'utilisateur à cause de l'email existant$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_de_l_utilisateur_à_cause_de_l_email_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch user " + patchedUser.getId() + ": mail already exists");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur mais avec un groupe inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_partiellement_un_utilisateur_mais_avec_un_groupe_inexistant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        final Map<String, Object> patch = buildUserToPatch();
        patch.put("groupId", "fakeGroup");
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).patch(getSystemTenantUserAdminContext(), patch);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle de l'utilisateur à cause du groupe inexistant$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_de_l_utilisateur_à_cause_du_groupe_inexistant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch user " + patchedUser.getId() + ": group does not exist");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS sans le bon niveau met à jour partiellement un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_sans_le_bon_niveau_met_à_jour_partiellement_un_utilisateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).patch(getContext(proofTenantIdentifier,
                    tokenUserTest(new String[] { ROLE_UPDATE_USERS }, proofTenantIdentifier, SYSTEM_CUSTOMER_ID, "WRONGLEVEL")), buildUserToPatch());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle de l'utilisateur à cause du mauvais niveau$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_de_l_utilisateur_à_cause_du_mauvais_niveau() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch user "
                + patchedUser.getId() + ": level  is not allowed");
    }

    @Given("^deux tenants et un rôle par défaut pour la mise à jour partielle d'un utilisateur$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_mise_à_jour_partielle_d_un_utilisateur() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur met à jour partiellement un utilisateur$")
    public void cet_utilisateur_met_à_jour_partiellement_un_utilisateur() {
        try {
            getUserRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .patch(getContext(testContext.tenantIHMContext, testContext.tokenUser), buildUserToPatch());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_ME_USERS se met à jour partiellement dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_ME_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_ME_USERS_se_met_à_jour_partiellement_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_ME_USERS() {
        writeToken(TestConstants.TESTS_TOKEN_ID, testContext.savedUserDto.getId());
        patchedUser = getUserRestClient(true, null, new String[] { ROLE_UPDATE_ME_USERS })
                .patchMe(getContext(proofTenantIdentifier, TestConstants.TESTS_TOKEN_ID), buildUserToPatch());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_ME_USERS se met à jour partiellement avec un autre identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_ME_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_ME_USERS_se_met_à_jour_partiellement_avec_un_autre_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_ME_USERS() {
        final Map<String, Object> patch = buildUserToPatch();
        patch.put("id", SYSTEM_USER_ID);
        writeToken(TestConstants.TESTS_TOKEN_ID, testContext.savedUserDto.getId());
        patchedUser = getUserRestClient(true, null, new String[] { ROLE_UPDATE_ME_USERS })
                .patchMe(getContext(proofTenantIdentifier, TestConstants.TESTS_TOKEN_ID), patch);
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_ME_USERS se met à jour partiellement dans un tenant auquel il est autorisé en utilisant un certificat full access sans le rôle ROLE_UPDATE_ME_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_ME_USERS_se_met_à_jour_partiellement_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_sans_le_rôle_ROLE_UPDATE_ME_USERS() {
        writeToken(TestConstants.TESTS_TOKEN_ID, testContext.savedUserDto.getId());
        try {
            getUserRestClient(true, null, new String[] { ROLE_LOGBOOKS }).patchMe(getContext(proofTenantIdentifier, TestConstants.TESTS_TOKEN_ID),
                    buildUserToPatch());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour de lui-même$")
    public void le_serveur_refuse_la_mise_à_jour_de_lui_même() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception instanceof ForbiddenException).isTrue();
    }

    @Then("^une trace de mise à jour de l'utilisateur est présente dans vitam$")
    public void une_trace_de_mise_à_jour_utilisateur_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(patchedUser.getCustomerId(), patchedUser.getIdentifier(), "users", "EXT_VITAMUI_UPDATE_USER");
    }

}
