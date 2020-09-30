package fr.gouv.vitamui.cucumber.back.steps.referential.context;

import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Contextes dans Referential admin : opérations de vérification.
 *
 *
 */
public class ApiReferentialExternalContextCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un contexte par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_contexte_par_son_identifiant() {
        try {
            final QueryDto criteria = QueryDto.criteria("id", TestConstants.CONTEXT_NAME, CriterionOperator.EQUALS);
            testContext.bResponse = getContextRestClient().checkExist(getSystemTenantUserAdminContext(), TestConstants.CONTEXT_IDENTIFIER);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur vérifie l'existence d'un contexte par son code et son nom$")
    public void un_utilisateur_vérifie_l_existence_d_un_contexte_par_son_code_et_son_nom() {
        try {
            final QueryDto criteria = QueryDto.criteria(
                	"id", TestConstants.CONTEXT_IDENTIFIER, CriterionOperator.EQUALS).addCriterion(
                	"name",TestConstants.CONTEXT_NAME, CriterionOperator.EQUALS);
            testContext.bResponse = getContextRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
