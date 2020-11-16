/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
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
 * Teste l'API Rules dans Referentiel admin : operations de recuperation.
 *
 *
 */
public class ApiReferentialExternalRulePatchSteps extends CommonSteps {

    private RuleDto ruleDto;

    @Given("la regle RuleTest a ses valeurs par defaut")
    public void la_regle_RuleTest_a_ses_valeurs_par_defaut() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleType", "StorageRule");
        partialDto.put("ruleDuration", "1");
        partialDto.put("ruleMeasurement", "DAY");
        partialDto.put("ruleDescription", "Test rule Description 1");

        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @When("^un utilisateur avec le role ROLE_GET_RULES modifie le type d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_modifie_le_type_d_une_regle_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleType", "AppraisalRule");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^le type de la regle est a jour$")
    public void le_type_de_la_regle_est_a_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleType()).isEqualTo("AppraisalRule");
    }

    @When("^un utilisateur avec le role ROLE_GET_RULES modifie la duree d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_modifie_la_duree_d_une_regle_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleDuration", "150");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^la duree de la regle est a jour$")
    public void la_duree_de_la_regle_est_a_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleDuration()).isEqualTo("150");
    }

    @When("^un utilisateur avec le role ROLE_GET_RULES modifie la mesure de la duree d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_modifie_la_mesure_de_la_duree_d_une_regle_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleMeasurement", "YEAR");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^la mesure de la duree de la regle est a jour$")
    public void la_mesure_de_la_duree_de_la_regle_est_a_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleMeasurement()).isEqualTo("YEAR");
    }

    @When("^un utilisateur avec le role ROLE_GET_RULES modifie la description d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_modifie_la_description_d_une_regle_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleDescription", "Nouvelle description");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^la description de la regle est a jour$")
    public void la_description_de_la_regle_est_a_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleDescription()).isEqualTo("Nouvelle description");
    }

    @When("^un utilisateur avec le role ROLE_GET_RULES modifie les champs d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_modifie_les_champs_d_une_regle_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "RuleTest");
        partialDto.put("ruleType", "AppraisalRule");
        partialDto.put("ruleDuration", "150");
        partialDto.put("ruleMeasurement", "YEAR");
        partialDto.put("ruleDescription", "Nouvelle description");
        getRuleRestClient().patch(getSystemTenantUserAdminContext(), partialDto);
    }

    @Then("^les champs de la regle sont a jour$")
    public void les_champs_de_la_regle_sont_a_jour() {
        ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest");
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleType()).isEqualTo("AppraisalRule");
        assertThat(ruleDto.getRuleDuration()).isEqualTo("150");
        assertThat(ruleDto.getRuleMeasurement()).isEqualTo("YEAR");
        assertThat(ruleDto.getRuleDescription()).isEqualTo("Nouvelle description");
    }
}
