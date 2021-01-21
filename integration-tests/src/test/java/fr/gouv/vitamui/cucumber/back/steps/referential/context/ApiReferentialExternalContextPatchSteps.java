/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.cucumber.back.steps.referential.context;

import static fr.gouv.vitamui.utils.TestConstants.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Context dans Referential admin : opérations de mise à jour partielle.
 *
 */
public class ApiReferentialExternalContextPatchSteps extends CommonSteps {

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
