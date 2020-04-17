package fr.gouv.vitamui.cucumber.back.steps.iam.group;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_UPDATE_GROUPS;
import static fr.gouv.vitamui.utils.TestConstants.ADMIN_GROUP_NAME;
import static fr.gouv.vitamui.utils.TestConstants.CLIENT1_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.model.Filters;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Groups dans IAM admin : opérations de mise à jour partielle.
 *
 *
 */
public class ApiIamExternalGroupPatchSteps extends CommonSteps {

    private Map<String, Object> buildGroupToPatch() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", testContext.groupDto.getId());
        map.put("description", UPDATED + testContext.savedGroupDto.getDescription());
        return map;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_GROUPS_met_à_jour_partiellement_un_groupe_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_GROUPS() {
        final Map<String, Object> dto = buildGroupToPatch();
        try {
            testContext.groupDto = getGroupRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne le groupe partiellement mis à jour$")
    public void le_serveur_retourne_le_groupe_partiellement_mis_à_jour() {
        assertThat(testContext.groupDto.getName()).isEqualTo(testContext.savedGroupDto.getName());
        assertThat(testContext.groupDto.getDescription()).isEqualTo(UPDATED + testContext.savedGroupDto.getDescription());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_GROUPS_met_à_jour_partiellement_un_groupe_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_GROUPS() {
        final Map<String, Object> dto = buildGroupToPatch();
        dto.put("readonly", true);
        try {
            testContext.groupDto = getGroupRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du groupe à cause du readonly$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_groupe_à_cause_du_readonly() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update group "
                + testContext.groupDto.getId() + ": readonly must be set to false");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_GROUPS mais avec un mauvais client met à jour partiellement un groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_GROUPS_mais_avec_un_mauvais_client_met_à_jour_partiellement_un_groupe_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_GROUPS() {
        final Map<String, Object> dto = buildGroupToPatch();
        dto.put("customerId", CLIENT1_CUSTOMER_ID);
        testContext.level = "";
        try {
            getGroupRestClient(true, null, new String[] { ROLE_UPDATE_GROUPS }).patch(getContext(client1TenantIdentifier,
                    tokenUserTest(new String[] { ROLE_UPDATE_GROUPS }, client1TenantIdentifier, CLIENT1_CUSTOMER_ID, testContext.level)), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du groupe à cause du mauvais client$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_groupe_à_cause_du_mauvais_client() {
        assertThat(testContext.exception).isNotNull();
        final String id = testContext.groupDto.getId();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update group " + id
                + ": no group found for id " + id + " - customerId " + CLIENT1_CUSTOMER_ID);
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_GROUPS_met_à_jour_partiellement_un_groupe_avec_un_nom_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_GROUPS() {
        final Map<String, Object> dto = buildGroupToPatch();
        dto.put("name", ADMIN_GROUP_NAME);
        try {
            testContext.groupDto = getGroupRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du groupe à cause du nom existant$")
    public void le_serveur_refuse_la_mise_à_jour_du_groupe_partielle_à_cause_du_nom_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update group "
                + testContext.groupDto.getId() + ": group already exists");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_GROUPS sans le bon niveau met à jour partiellement un groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_GROUPS_sans_le_bon_niveau_met_à_jour_partiellement_un_groupe_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_GROUPS() {
        final Map<String, Object> dto = buildGroupToPatch();
        try {
            getGroupRestClient(true, null, new String[] { ROLE_UPDATE_GROUPS }).patch(getContext(proofTenantIdentifier,
                    tokenUserTest(new String[] { ROLE_UPDATE_GROUPS }, proofTenantIdentifier, SYSTEM_CUSTOMER_ID, "WRONGLEVEL")), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du groupe à cause du mauvais niveau$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_groupe_à_cause_du_mauvais_niveau() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update group "
                + testContext.groupDto.getId() + ": level  is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe avec un profil inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_GROUPS_met_à_jour_partiellement_un_groupe_avec_un_profil_inexistant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_GROUPS() {
        final Map<String, Object> dto = buildGroupToPatch();
        dto.put("profileIds", Arrays.asList("fakeId"));
        try {
            getGroupRestClient(true, null, new String[] { ROLE_UPDATE_GROUPS }).patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du groupe à cause du profil inexistant$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_groupe_à_cause_du_profil_inexistant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update group " + testContext.groupDto.getId() + ": no profiles");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe avec un profil d'un autre client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_GROUPS_met_à_jour_partiellement_un_groupe_avec_un_profil_d_un_autre_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_GROUPS() {
        final Map<String, Object> dto = buildGroupToPatch();

        final Bson filter = Filters.ne("customerId", TestConstants.SYSTEM_CUSTOMER_ID);
        final Document profileFromAnotherClient = getProfilesCollection().find(filter).first();
        dto.put("profileIds", Arrays.asList(profileFromAnotherClient.get("_id").toString()));
        try {
            getGroupRestClient(true, null, new String[] { ROLE_UPDATE_GROUPS }).patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour partielle du groupe à cause du profil d'un autre client$")
    public void le_serveur_refuse_la_mise_à_jour_partielle_du_groupe_à_cause_du_profil_d_un_autre_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update group "
                + testContext.groupDto.getId() + ": profile and group customerId must be equals");
    }

    @Given("^deux tenants et un rôle par défaut pour la mise à jour partielle d'un groupe$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_mise_à_jour_partielle_d_un_groupe() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur met à jour partiellement un groupe$")
    public void cet_utilisateur_met_à_jour_partiellement_un_groupe() {
        final Map<String, Object> dto = buildGroupToPatch();
        try {
            getGroupRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .patch(getContext(testContext.tenantIHMContext, testContext.tokenUser), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^une trace de mise à jour du groupe est présente dans vitam$")
    public void une_trace_de_mise_à_jour_du_groupe_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(testContext.groupDto.getCustomerId(), testContext.groupDto.getIdentifier(), "groups", "EXT_VITAMUI_UPDATE_GROUP");
    }
}
