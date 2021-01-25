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
 * Teste l'API Rules dans Referentiel admin : operations de recuperation.
 *
 *
 */
public class ApiReferentialExternalRuleGetSteps extends CommonSteps {
    private List<RuleDto> ruleDtos;

    private RuleDto ruleDto;

    @When("^un utilisateur avec le role ROLE_GET_RULES recupere toutes les regles en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_recupere_toutes_les_regles_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
    	ruleDtos = getRuleRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne toutes les regles")
    public void le_serveur_retourne_tous_les_regles() {
        assertThat(ruleDtos).isNotNull();

        final int size = ruleDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(1);
    }

    @When("^un utilisateur avec le role ROLE_GET_RULES recupere une regle par son identifiant en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_recupere_une_regle_par_son_identifiant_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
    	ruleDto = getRuleRestClient().getOne(getSystemTenantUserAdminContext(), "RuleTest", Optional.empty());
    }

    @Then("^le serveur retourne la regle avec cet identifiant$")
    public void le_serveur_retourne_la_regle_avec_cet_identifiant() {
        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleId()).isEqualTo("RuleTest");
    }

    @When("^un utilisateur avec le role ROLE_GET_RULES recupere toutes les regles par intitule en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_recupere_toutes_les_regles_par_intitule_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
        QueryDto criteria = QueryDto.criteria(
        	"ruleValue", "Test rule value 1", CriterionOperator.EQUALS);
        ruleDtos = getRuleRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne toutes les regle avec cet intitule$")
    public void le_serveur_retourne_toutes_les_regles_avec_cet_intitule() {
        assertThat(ruleDtos).isNotNull().isNotEmpty();
        assertThat(ruleDtos.stream().anyMatch(c ->
        	(c.getRuleId().equals("RuleTest") && c.getRuleValue().equals("Test rule value 1")
        ))).isTrue();
    }

    @When("^un utilisateur avec le role ROLE_GET_RULES recupere toutes les regles par type en utilisant un certificat full access avec le role ROLE_GET_RULES$")
    public void un_utilisateur_avec_le_role_ROLE_GET_RULES_recupere_toutes_les_regles_par_type_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_RULES() {
        QueryDto criteria = QueryDto.criteria(
            "ruleType", "StorageRule", CriterionOperator.EQUALS);
        ruleDtos = getRuleRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne toutes les regle avec ce type")
    public void le_serveur_retourne_toutes_les_regles_avec_ce_type() {
        assertThat(ruleDtos).isNotNull().isNotEmpty();
        assertThat(ruleDtos.stream().anyMatch(c ->
            (c.getRuleId().equals("RuleTest") && c.getRuleType().equals("StorageRule")
            ))).isTrue();
    }

    @When("^un utilisateur avec le role ROLE_GET_CONTEXTS recupere toutes les regles avec pagination en utilisant un certificat full access avec le role ROLE_GET_CONTEXTS$")
    public void un_utilisateur_avec_le_role_ROLE_GET_CONTEXTS_recupere_toutes_les_regles_avec_pagination_en_utilisant_un_certificat_full_access_avec_le_role_ROLE_GET_CONTEXTS() {
    	final PaginatedValuesDto<RuleDto> paginatedRules = getRuleRestClient().getAllPaginated(
    		getSystemTenantUserAdminContext(), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        ruleDtos = new ArrayList<>(paginatedRules.getValues());
    }

    @Then("^le serveur retourne les regles paginees$")
    public void le_serveur_retourne_les_regles_paginees() {
    	assertThat(ruleDtos).isNotNull().isNotEmpty();
        assertThat(ruleDtos).size().isLessThanOrEqualTo(10);
    }
}
