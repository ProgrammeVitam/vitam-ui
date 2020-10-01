package fr.gouv.vitamui.cucumber.back.steps.referential.rule;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.common.utils.ReferentialDtoBuilder;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Rules dans Referentiel admin : opérations de récupération.
 *
 *
 */
public class ApiReferentialExternalRulePatchSteps extends CommonSteps {

    private RuleDto ruleDto;

    @Given("la règle RuleTest a ses valeurs par défaut")
    public void la_règle_RuleTest_a_ses_valeurs_par_défaut() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleType", "StorageRule");
        partialDto.put("ruleDuration", "1");
        partialDto.put("ruleMeasurement", "DAY");
        partialDto.put("ruleDescription", "Test rule Description 1");

        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_RULES modifie le type d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_modifie_le_type_d_une_règle_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleType", "AppraisalRule");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^le type de la règle est à jour$")
    public void le_type_de_la_règle_est_à_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleType()).isEqualTo("AppraisalRule");
    }

    @When("^un utilisateur avec le rôle ROLE_GET_RULES modifie la durée d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_modifie_la_durée_d_une_règle_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleDuration", "150");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^la durée de la règle est à jour$")
    public void la_durée_de_la_règle_est_à_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleDuration()).isEqualTo("150");
    }

    @When("^un utilisateur avec le rôle ROLE_GET_RULES modifie la mesure de la durée d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_modifie_la_mesure_de_la_durée_d_une_règle_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleMeasurement", "YEAR");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^la mesure de la durée de la règle est à jour$")
    public void la_mesure_de_la_durée_de_la_règle_est_à_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleMeasurement()).isEqualTo("YEAR");
    }

    @When("^un utilisateur avec le rôle ROLE_GET_RULES modifie la description d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_modifie_la_description_d_une_règle_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleDescription", "Nouvelle description");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^la description de la règle est à jour$")
    public void la_description_de_la_règle_est_à_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleDescription()).isEqualTo("Nouvelle description");
    }

    @When("^un utilisateur avec le rôle ROLE_GET_RULES modifie les champs d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_RULES_modifie_les_champs_d_une_règle_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleType", "AppraisalRule");
        partialDto.put("ruleDuration", "150");
        partialDto.put("ruleMeasurement", "YEAR");
        partialDto.put("ruleDescription", "Nouvelle description");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^les champs de la règle sont à jour$")
    public void les_champs_de_la_règle_sont_à_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleType()).isEqualTo("AppraisalRule");
        assertThat(ruleDto.getRuleDuration()).isEqualTo("150");
        assertThat(ruleDto.getRuleMeasurement()).isEqualTo("YEAR");
        assertThat(ruleDto.getRuleDescription()).isEqualTo("Nouvelle description");
    }
}
