package fr.gouv.vitamui.cucumber.back.steps.referential.rule;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.common.utils.ReferentialDtoBuilder;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Rules dans Referential admin : opérations de création.
 *
 *
 */

public class ApiReferentialExternalRuleCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_RULES ajoute une nouvelle règle en utilisant un certificat full access avec le rôle ROLE_CREATE_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_RULES_ajoute_une_nouvelle_règle_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_RULES() {
        final RuleDto ruleDto = ReferentialDtoBuilder.buildRuleDto(null, "RuleTest", "StorageRule", "Test rule value 1", "Test rule Description 1", "1", "DAY");
        testContext.savedRuleDto = getRuleRestClient().create(getSystemTenantUserAdminContext(), ruleDto);
    }

    @Then("^le serveur retourne la règle créée$")
    public void le_serveur_retourne_la_règle_créée() {
        assertThat(testContext.savedRuleDto).overridingErrorMessage("la réponse retournée est null").isNotNull();
    }

}
