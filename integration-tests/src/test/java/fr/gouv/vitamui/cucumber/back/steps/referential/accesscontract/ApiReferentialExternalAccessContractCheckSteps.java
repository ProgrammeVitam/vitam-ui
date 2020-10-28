package fr.gouv.vitamui.cucumber.back.steps.referential.accesscontract;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.AccessContractDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.When;

/**
 * Teste l'API Access Contracts dans Referential admin : opérations de vérification.
 *
 *
 */
public class ApiReferentialExternalAccessContractCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un contrat d'accès par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_contrat_d_accès_par_son_identifiant() {
        try {
            final AccessContractDto accessContractDto = new AccessContractDto();
            accessContractDto.setIdentifier(TestConstants.ACCESS_CONTRACT_NAME);
            testContext.bResponse = getAccessContractRestClient().check(getSystemTenantUserAdminContext(), accessContractDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
