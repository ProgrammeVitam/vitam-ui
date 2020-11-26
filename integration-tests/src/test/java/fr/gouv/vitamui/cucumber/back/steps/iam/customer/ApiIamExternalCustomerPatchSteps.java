package fr.gouv.vitamui.cucumber.back.steps.iam.customer;

import static fr.gouv.vitamui.utils.TestConstants.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Customer dans IAM admin : opérations de mise à jour partielle.
 *
 */
public class ApiIamExternalCustomerPatchSteps extends CommonSteps {

    private Map<String, Object> buildCustomerToPatch() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", testContext.basicCustomerDto.getId());
        map.put("companyName", UPDATED + testContext.savedBasicCustomerDto.getCompanyName());
        return map;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS met à jour partiellement un client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_CUSTOMERS_met_à_jour_partiellement_un_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_CUSTOMERS() {
        final Map<String, Object> dto = buildCustomerToPatch();
        final CustomerPatchFormData patchFormData = new CustomerPatchFormData();

        patchFormData.setHeader(Optional.empty());
        patchFormData.setFooter(Optional.empty());
        patchFormData.setPortal(Optional.empty());

        patchFormData.setPartialCustomerDto(dto);
        try {
            testContext.basicCustomerDto = getCustomerWebClient().patch(getSystemTenantUserAdminContext(), dto.get("id").toString(), patchFormData);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne le client partiellement mis à jour$")
    public void le_serveur_retourne_le_client_partiellement_mis_à_jour() {
        assertThat(testContext.basicCustomerDto.getName()).isEqualTo(testContext.savedBasicCustomerDto.getName());
        assertThat(testContext.basicCustomerDto.getCompanyName()).isEqualTo(UPDATED + testContext.savedBasicCustomerDto.getCompanyName());
    }

    @Then("^une trace de mise à jour du client est présente dans vitam$")
    public void une_trace_de_mise_à_jour_du_client_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(TestConstants.SYSTEM_CUSTOMER_ID, testContext.basicCustomerDto.getIdentifier(), "customers", "EXT_VITAMUI_UPDATE_CUSTOMER");
    }

}
