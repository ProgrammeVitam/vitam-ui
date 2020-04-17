package fr.gouv.vitamui.cucumber.back.steps.iam.tenant;

import static fr.gouv.vitamui.utils.TestConstants.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Tenants dans IAM admin : opérations de mise à jour partielle.
 *
 */
public class ApiIamExternalTenantPatchSteps extends CommonSteps {

    private Map<String, Object> buildTenantToPatch() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", testContext.savedTenantDto.getId());
        map.put("name", UPDATED + testContext.savedTenantDto.getName());
        return map;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_TENANTS met à jour partiellement un tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_TENANTS_met_à_jour_partiellement_un_tenant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_TENANTS() {
        final Map<String, Object> dto = buildTenantToPatch();
        try {
            testContext.tenantDto = getTenantRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne le tenant partiellement mis à jour$")
    public void le_serveur_retourne_le_tenant_partiellement_mis_à_jour() {
        assertThat(testContext.tenantDto.getId()).isEqualTo(testContext.savedTenantDto.getId());
        assertThat(testContext.tenantDto.getName()).isEqualTo(UPDATED + testContext.savedTenantDto.getName());
    }

    @Then("^une trace de mise à jour du tenant est présente dans vitam$")
    public void une_trace_de_mise_à_jour_du_tenant_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(TestConstants.SYSTEM_CUSTOMER_ID, testContext.tenantDto.getIdentifier().toString(), "tenants",
                "EXT_VITAMUI_UPDATE_TENANT");
    }

}
