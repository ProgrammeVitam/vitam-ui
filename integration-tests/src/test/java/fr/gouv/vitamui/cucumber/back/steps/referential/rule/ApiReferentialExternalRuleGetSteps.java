package fr.gouv.vitamui.cucumber.back.steps.referential.rule;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Rules dans Referentiel admin : opérations de récupération.
 *
 *
 */
public class ApiReferentialExternalRuleGetSteps extends CommonSteps {
    private List<RuleDto> ruleDtos;

    private RuleDto ruleDto;

    @When("^un utilisateur avec le rôle ROLE_GET_RULES récupère toutes les règles en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_récupère_toutes_les_règles_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
    	ruleDtos = getRuleRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne toutes les règles")
    public void le_serveur_retourne_tous_les_règles() {
        assertThat(ruleDtos).isNotNull();

        final int size = ruleDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(1);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_RULES récupère une règle par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_récupère_une_règle_par_son_identifiant_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
    	ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest", Optional.empty());
    }

    @Then("^le serveur retourne la règle avec cet identifiant$")
    public void le_serveur_retourne_la_règle_avec_cet_identifiant() {
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleId()).isEqualTo("RuleTest");
    }

    @When("^un utilisateur avec le rôle ROLE_GET_RULES récupère toutes les règles par intitulé en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_récupère_toutes_les_règles_par_intitulé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
        QueryDto criteria = QueryDto.criteria(
        	"ruleValue", "Test rule value 1", CriterionOperator.EQUALS);
        ruleDtos = getRuleRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne toutes les règle avec cet intitulé$")
    public void le_serveur_retourne_toutes_les_règles_avec_cet_intitulé() {
        assertThat(ruleDtos).isNotNull().isNotEmpty();
        assertThat(ruleDtos.stream().anyMatch(c ->
        	(c.getRuleId().equals("RuleTest") && c.getRuleValue().equals("Test rule value 1")
        ))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_RULES récupère toutes les règles par type en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_récupère_toutes_les_règles_par_type_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
        QueryDto criteria = QueryDto.criteria(
            "ruleType", "StorageRule", CriterionOperator.EQUALS);
        ruleDtos = getRuleRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne toutes les règle avec ce type")
    public void le_serveur_retourne_toutes_les_règles_avec_ce_type() {
        assertThat(ruleDtos).isNotNull().isNotEmpty();
        assertThat(ruleDtos.stream().anyMatch(c ->
            (c.getRuleId().equals("RuleTest") && c.getRuleType().equals("StorageRule")
            ))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère toutes les règles avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CONTEXTS_récupère_toutes_les_règles_avec_pagination_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CONTEXTS() {
    	final PaginatedValuesDto<RuleDto> paginatedRules = getRuleRestClient().getAllPaginated(
    		getSystemTenantUserAdminContext(), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        ruleDtos = new ArrayList<>(paginatedRules.getValues());
    }

    @Then("^le serveur retourne les règles paginées$")
    public void le_serveur_retourne_les_règles_paginées() {
    	assertThat(ruleDtos).isNotNull().isNotEmpty();
        assertThat(ruleDtos).size().isLessThanOrEqualTo(10);
    }
}
