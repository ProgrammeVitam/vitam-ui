package fr.gouv.vitamui.cucumber.back.steps.iam.profile;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_UPDATE_PROFILES;
import static fr.gouv.vitamui.utils.TestConstants.CLIENT1_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_USER_PROFILE_NAME;
import static fr.gouv.vitamui.utils.TestConstants.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Profiles dans IAM admin : opérations de mise à jour partielle.
 *
 *
 */
public class ApiIamExternalProfilePatchSteps extends CommonSteps {

    private Map<String, Object> buildProfileToPatch() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", testContext.profileDto.getId());
        map.put("description", UPDATED + testContext.savedProfileDto.getDescription());
        map.put("customerId", testContext.savedProfileDto.getCustomerId());
        map.put("tenantIdentifier", testContext.savedProfileDto.getTenantIdentifier());
        return map;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROFILES_met_à_jour_partiellement_un_profil_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        final Map<String, Object> dto = buildProfileToPatch();
        testContext.profileDto = getProfileRestClient().patch(getSystemTenantUserAdminContext(), dto);
    }

    @Then("^le serveur retourne le profil partiellement mis à jour$")
    public void le_serveur_retourne_le_profil_partiellement_mis_à_jour() {
        assertThat(testContext.profileDto.getName()).isEqualTo(testContext.savedProfileDto.getName());
        assertThat(testContext.profileDto.getDescription()).isEqualTo(UPDATED + testContext.savedProfileDto.getDescription());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROFILES_met_à_jour_partiellement_un_profil_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        final Map<String, Object> dto = buildProfileToPatch();
        dto.put("readonly", true);
        try {
            testContext.profileDto = getProfileRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du profil à cause du readonly$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_profil_à_cause_du_readonly() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch profile "
                + testContext.profileDto.getId() + ": readonly must be set to false");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROFILES_met_à_jour_partiellement_un_profil_mais_avec_un_mauvais_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        final Map<String, Object> dto = buildProfileToPatch();
        dto.put("customerId", CLIENT1_CUSTOMER_ID);
        testContext.level = "";
        try {
            getProfileRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du profil à cause du mauvais client$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_profil_à_cause_du_mauvais_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch profile: customerId " + CLIENT1_CUSTOMER_ID + " is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil mais avec un mauvais tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROFILES_met_à_jour_partiellement_un_profil_mais_avec_un_mauvais_tenant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        final Map<String, Object> dto = buildProfileToPatch();
        dto.put("tenantIdentifiant", client1TenantIdentifier);
        testContext.level = "";
        try {
            getProfileRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du profil à cause du mauvais tenant$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_profil_à_cause_du_mauvais_tenant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch profile "
                + testContext.profileDto.getId() + ": key tenantIdentifiant is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROFILES_met_à_jour_partiellement_un_profil_avec_un_nom_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        final Map<String, Object> dto = buildProfileToPatch();
        dto.put("name", SYSTEM_USER_PROFILE_NAME);
        try {
            testContext.profileDto = getProfileRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du profil à cause du nom existant$")
    public void le_serveur_refuse_la_mise_à_jour_du_profil_partielle_à_cause_du_nom_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch profile "
                + testContext.profileDto.getId() + ": profile already exists");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROFILES sans le bon niveau met à jour partiellement un profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROFILES_sans_le_bon_niveau_met_à_jour_partiellement_un_profil_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        final Map<String, Object> dto = buildProfileToPatch();
        try {
            getProfileRestClient(true, null, new String[] { ROLE_UPDATE_PROFILES }).patch(getContext(proofTenantIdentifier,
                    tokenUserTest(new String[] { ROLE_UPDATE_PROFILES }, proofTenantIdentifier, SYSTEM_CUSTOMER_ID, "WRONGLEVEL")), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du profil à cause du mauvais niveau$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_profil_à_cause_du_mauvais_niveau() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch profile "
                + testContext.profileDto.getId() + ": level  is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROFILES désactive un profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROFILES_désactive_un_profil_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        final Map<String, Object> dto = buildProfileToPatch();
        dto.put("enabled", false);
        try {
            testContext.profileDto = getProfileRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du profil à cause de la désactivation$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_profil_à_cause_de_la_désactivation() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString().startsWith("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch profile "
                + testContext.profileDto.getId() + ": the profile is referenced by")).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil avec un rôle qu'il ne possède pas dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROFILES_met_à_jour_partiellement_un_profil_avec_un_rôle_qu_il_ne_possède_pas_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        final Map<String, Object> dto = buildProfileToPatch();
        final Map<String, Object> role = new HashMap<>();
        role.put("name", TestConstants.NEW_ROLE);
        dto.put("roles", Arrays.asList(role));
        try {
            testContext.profileDto = getProfileRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du profil de l'utilisateur qui ne possède pas le bon rôle$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_profil_de_l_utilisateur_qui_ne_possède_pas_le_bon_rôle() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to patch profile "
                + testContext.profileDto.getId() + ": role " + TestConstants.NEW_ROLE + " does not exist");
    }

    @Given("^deux tenants et un rôle par défaut pour la mise à jour partielle d'un profil$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_mise_à_jour_partielle_d_un_profil() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur met à jour partiellement un profil$")
    public void cet_utilisateur_met_à_jour_partiellement_un_profil() {
        final Map<String, Object> dto = buildProfileToPatch();
        try {
            getProfileRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .patch(getContext(testContext.tenantIHMContext, testContext.tokenUser), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^une trace de mise à jour du profil est présente dans vitam$")
    public void une_trace_de_mise_à_jour_du_profil_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(testContext.profileDto.getCustomerId(), testContext.profileDto.getIdentifier(), "profiles", "EXT_VITAMUI_UPDATE_PROFILE");
    }
}
