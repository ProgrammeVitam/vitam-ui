package fr.gouv.vitamui.cucumber.back.steps.referential.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Teste l'API Contextes dans Referentiel admin : opérations de récupération.
 *
 *
 */
public class ApiReferentialExternalContextGetSteps extends CommonSteps {
    private List<ContextDto> contextDtos;

    private ContextDto contextDto;
    
    private JsonNode contextHistory;

    @When("^un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère tous les contextes applicatifs en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CONTEXTS_récupère_tous_les_contextes_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CONTEXTS() {
    	contextDtos = getContextRestClient().getAll(getSystemTenantUserAdminContext());
    }
    
    @Then("^le serveur retourne tous les contextes$")
    public void le_serveur_retourne_tous_les_contextes() {
        assertThat(contextDtos).isNotNull();

        final int size = contextDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(1);
    }
    
    @When("^un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère un contexte par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CONTEXTS_récupère_un_contexte_par_son_identifiant_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CONTEXTS() {
    	contextDto = getContextRestClient().getOne(getSystemTenantUserAdminContext(), TestConstants.CONTEXT_IDENTIFIER, Optional.empty());
    }

    @Then("^le serveur retourne le contexte avec cet identifiant$")
    public void le_serveur_retourne_le_client_avec_cet_identifiant() {
        assertThat(contextDto).isNotNull();
        assertThat(contextDto.getIdentifier()).isEqualTo(TestConstants.CONTEXT_IDENTIFIER);
        assertThat(contextDto.getName()).isEqualTo(TestConstants.CONTEXT_NAME);
    }
    
    @When("^un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère tous les contextes par code et nom en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CUSTOMERS_récupère_tous_les_contextes_par_code_et_nom_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CONTEXTS() {
        QueryDto criteria = QueryDto.criteria(
        	"id", TestConstants.CONTEXT_IDENTIFIER, CriterionOperator.EQUALS).addCriterion(
        	"name",TestConstants.CONTEXT_NAME, CriterionOperator.EQUALS);
        contextDtos = getContextRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }
    
    @Then("^le serveur retourne les contextes par code et nom$")
    public void le_serveur_retourne_les_contextes_par_code_et_nom() {
        assertThat(contextDtos).isNotNull().isNotEmpty();
        assertThat(contextDtos.stream().anyMatch(c -> 
        	(c.getIdentifier().equals(TestConstants.CONTEXT_IDENTIFIER) && c.getName().equals(TestConstants.CONTEXT_NAME) 
        ))).isTrue();
    }
    
    @When("^un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère tous les contextes par code ou nom en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CUSTOMERS_récupère_tous_les_contextes_par_code_ou_nom_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CONTEXTS() {
        QueryDto criteria = new QueryDto(QueryOperator.OR)
        	.addCriterion("id", TestConstants.CONTEXT_IDENTIFIER, CriterionOperator.EQUALS)
        	.addCriterion("name",TestConstants.CONTEXT_NAME, CriterionOperator.EQUALS);
        contextDtos = getContextRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }
    
    @Then("^le serveur retourne les contextes par code ou nom$")
    public void le_serveur_retourne_les_contextes_par_code_ou_nom() {
        assertThat(contextDtos).isNotNull().isNotEmpty();
        assertThat(contextDtos.stream().anyMatch(c -> 
        	(c.getIdentifier().equals(TestConstants.CONTEXT_IDENTIFIER) || c.getName().equals(TestConstants.CONTEXT_NAME) 
        ))).isTrue();
    }
    
    @When("^un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère tous les contextes avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CONTEXTS_récupère_tous_les_contextes_avec_pagination_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CONTEXTS() {
    	final PaginatedValuesDto<ContextDto> paginatedContexts = getContextRestClient().getAllPaginated(
    		getSystemTenantUserAdminContext(), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        contextDtos = new ArrayList<>(paginatedContexts.getValues());
    }

    @Then("^le serveur retourne les contexts paginés$")
    public void le_serveur_retourne_les_clients_paginés() {
    	le_serveur_retourne_les_contextes_par_code_ou_nom();
    }
    
    @When("^un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère l'historique d'un contexte à partir de son son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CONTEXTS_récupère_l_historique_d_un_contexte_à_partir_de_son_identifiant_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CONTEXTS() {
    	contextHistory = getContextRestClient().findHistoryById(getSystemTenantUserAdminContext(), TestConstants.CONTEXT_IDENTIFIER);
    }
    
    @Then("^le serveur retourne l'historique du contexte$")
    public void le_serveur_retourne_l_historique_du_contexte() {
        assertThat(contextHistory).isNotNull();
    }
    
}
