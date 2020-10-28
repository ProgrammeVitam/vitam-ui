package fr.gouv.vitamui.cucumber.back.steps.referential.ingestcontract;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.utils.FactoryDto;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Ingest Contract dans Referential admin : opérations de création.
 *
 *
 */

public class ApiReferentialExternalIngestContractCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_INGEST_CONTRACT ajoute un nouveau contrat d'entrée en utilisant un certificat full access avec le rôle ROLE_CREATE_INGEST_CONTRACT$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_INGEST_CONTRACT_ajoute_un_nouveau_contrat_d_entrée_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_INGEST_CONTRACT() {
        final IngestContractDto dto = FactoryDto.buildDto(IngestContractDto.class);
        testContext.savedIngestContractDto = getIngestContractRestClient().create(getSystemTenantUserAdminContext(), dto);
    }

    @Then("^le serveur retourne le contrat d'entrée créé$")
    public void le_status_de_la_réponse_doit_etre() {
        assertThat(testContext.savedIngestContractDto).overridingErrorMessage("la réponse retournée est null").isNotNull();
    }

}
