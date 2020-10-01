package fr.gouv.vitamui.cucumber.back.steps.referential.rule;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Rules dans Referentiel admin : opérations de récupération.
 *
 *
 */
public class ApiReferentialExternalRuleDeleteSteps extends CommonSteps {

    private RuleDto ruleDto;

    @When("^un utilisateur avec le rôle ROLE_GET_RULES supprime une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_supprime_une_règle_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
        getRuleRestClient().delete(getSystemTenantUserAdminContext(), "RuleTest");
    }

    @Then("^la règle n'existe pas$")
    public void le_type_de_la_règle_est_à_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNull();
    }
}
