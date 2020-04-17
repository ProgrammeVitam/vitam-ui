package fr.gouv.vitamui.cucumber.back.steps.iam.provider;

import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Identity providers dans IAM admin : opérations de vérification.
 *
 *
 */
public class ApiIamExternalIdentityPoviderCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un provider par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_provider_par_son_identifiant() {
        try {
            final QueryDto criteria = QueryDto.criteria("id", TestConstants.SYSTEM_IDP_ID, CriterionOperator.EQUALS);

            getIdentityProviderRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
