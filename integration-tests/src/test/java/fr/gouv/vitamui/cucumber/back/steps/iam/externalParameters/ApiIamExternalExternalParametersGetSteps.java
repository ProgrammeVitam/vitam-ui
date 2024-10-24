package fr.gouv.vitamui.cucumber.back.steps.iam.externalParameters;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API External Parameters dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamExternalExternalParametersGetSteps extends CommonSteps {

    private Map<String, String> externalParameterMap;

    @When(
        "^un utilisateur avec le rôle ROLE_GET_EXTERNAL_PARAMS récupère le paramétrage associé à son profl en utilisant un certificat full access avec le rôle ROLE_GET_EXTERNAL_PARAMS$"
    )
    public void un_utilisateur_avec_le_rôle_ROLE_GET_EXTERNAL_PARAMS_récupère_le_paramétrage_associé_à_son_profil_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_EXTERNAL_PARAMS() {
        externalParameterMap = getExternalParametersExternalRestClient()
            .getMyExternalParameters(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne le paramétrage associé au profil de l'utilisateur$")
    public void le_serveur_retourne_le_paramétrage_associé_au_profil_de_l_utilisateur() {
        assertThat(externalParameterMap).isNotNull();
        assertThat(externalParameterMap.size() > 0).isTrue();
    }
}
