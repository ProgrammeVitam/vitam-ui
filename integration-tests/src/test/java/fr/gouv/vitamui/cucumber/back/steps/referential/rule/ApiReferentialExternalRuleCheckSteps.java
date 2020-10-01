package fr.gouv.vitamui.cucumber.back.steps.referential.rule;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.common.utils.ReferentialDtoBuilder;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

/**
 * Teste l'API Rules dans Referential admin : opérations de vérification.
 *
 *
 */
public class ApiReferentialExternalRuleCheckSteps extends CommonSteps {

    @Given("la règle RuleTest existe")
    public void la_règle_RuleTest_existe() {
        final RuleDto ruleDto = ReferentialDtoBuilder.buildRuleDto(null, "RuleTest", "StorageRule", "Test rule value 1", "Test rule Description 1", "1", "DAY");
        if(!getRuleRestClient().check(getSystemTenantUserAdminContext(), ruleDto)) {
            getRuleRestClient().create(getSystemTenantUserAdminContext(), ruleDto);
        }
    }

    @Given("la règle RuleTest n'existe pas")
    public void la_règle_RuleTest_n_existe_pas() {
        final RuleDto ruleDto = ReferentialDtoBuilder.buildRuleDto(null, "RuleTest", "StorageRule", "Test rule value 1", "Test rule Description 1", "1", "DAY");
        if(getRuleRestClient().check(getSystemTenantUserAdminContext(), ruleDto)) {
            getRuleRestClient().delete(getSystemTenantUserAdminContext(), "RuleTest");
        }
    }

    @When("^un utilisateur vérifie l'existence de la règle RuleTest par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_de_la_règle_RuleTest_par_son_identifiant() {
        try {
            final RuleDto ruleDto = new RuleDto();
            ruleDto.setRuleId("RuleTest");
            testContext.bResponse = getRuleRestClient().check(getSystemTenantUserAdminContext(), ruleDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
