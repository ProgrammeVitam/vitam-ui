package fr.gouv.vitamui.cucumber.back.steps.referential.accesscontract;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.AccessContractDto;
import fr.gouv.vitamui.utils.FactoryDto;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Access Contract dans Referential admin : opérations de création.
 *
 *
 */

public class ApiReferentialExternalAccessContractCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_ACCESS_CONTRACT ajoute un nouveau contrat d'accès en utilisant un certificat full access avec le rôle ROLE_CREATE_ACCESS_CONTRACT$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_ACCESS_CONTRACT_ajoute_un_nouveau_contrat_d_accès_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_ACCESS_CONTRACT() {
        final AccessContractDto dto = FactoryDto.buildDto(AccessContractDto.class);
        testContext.savedAccessContractDto = getAccessContractRestClient().create(getSystemTenantUserAdminContext(), dto);
    }

    @Then("^le serveur retourne le contrat d'accès créé$")
    public void le_status_de_la_réponse_doit_etre() {
        assertThat(testContext.savedAccessContractDto).overridingErrorMessage("la réponse retournée est null").isNotNull();
    }
}
