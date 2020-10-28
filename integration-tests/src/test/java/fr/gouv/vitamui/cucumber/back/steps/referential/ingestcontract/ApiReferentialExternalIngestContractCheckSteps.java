package fr.gouv.vitamui.cucumber.back.steps.referential.ingestcontract;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.When;

/**
 * Teste l'API Ingest Contracts dans Referential admin : opérations de vérification.
 *
 *
 */
public class ApiReferentialExternalIngestContractCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un contrat d'entrée par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_contrat_d_entrée_par_son_identifiant() {
        try {
            final IngestContractDto ingestContractDto = new IngestContractDto();
            ingestContractDto.setIdentifier(TestConstants.INGEST_CONTRACT_NAME);
            testContext.bResponse = getIngestContractRestClient().check(getSystemTenantUserAdminContext(), ingestContractDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
