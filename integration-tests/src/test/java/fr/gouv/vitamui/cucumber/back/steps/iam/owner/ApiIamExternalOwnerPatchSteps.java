package fr.gouv.vitamui.cucumber.back.steps.iam.owner;

import static fr.gouv.vitamui.utils.TestConstants.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Owner dans IAM admin : opérations de mise à jour partielle.
 *
 */
public class ApiIamExternalOwnerPatchSteps extends CommonSteps {

    private Map<String, Object> buildOwnerToPatch() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", testContext.savedOwnerDto.getId());
        map.put("companyName", UPDATED + testContext.savedOwnerDto.getCompanyName());
        return map;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_OWNERS met à jour partiellement un propriétaire dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_OWNERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_OWNERS_met_à_jour_partiellement_un_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_OWNERS() {
        final Map<String, Object> dto = buildOwnerToPatch();
        try {
            testContext.ownerDto = getOwnerRestClient().patch(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne le propriétaire partiellement mis à jour$")
    public void le_serveur_retourne_le_propriétaire_partiellement_mis_à_jour() {
        assertThat(testContext.ownerDto.getName()).isEqualTo(testContext.savedOwnerDto.getName());
        assertThat(testContext.ownerDto.getCompanyName())
                .isEqualTo(UPDATED + testContext.savedOwnerDto.getCompanyName());
    }

    @Then("^une trace de mise à jour du propriétaire est présente dans vitam$")
    public void une_trace_de_mise_à_jour_du_propriétaire_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(TestConstants.SYSTEM_CUSTOMER_ID, testContext.ownerDto.getIdentifier(), "owners",
                "EXT_VITAMUI_UPDATE_OWNER");
    }

}
