package fr.gouv.vitamui.cucumber.back.steps.iam.subrogation;

import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;

/**
 * Teste l'API subrogations dans IAM admin : opérations de vérification.
 *
 *
 */
public class ApiIamExternalSubrogationCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'une subrogation par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_une_subrogation_par_son_identifiant() {
        try {
            final QueryDto criteria = QueryDto.criteria("id", getOrInitializeDefaultSubrogationId(), CriterionOperator.EQUALS);

            getSubrogationRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
