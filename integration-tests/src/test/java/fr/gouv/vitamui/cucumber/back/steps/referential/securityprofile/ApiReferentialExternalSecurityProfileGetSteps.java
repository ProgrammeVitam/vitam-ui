package fr.gouv.vitamui.cucumber.back.steps.referential.securityprofile;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Security Profiles dans Referentiel admin : opérations de récupération.
 *
 *
 */
public class ApiReferentialExternalSecurityProfileGetSteps extends CommonSteps {
    private List<SecurityProfileDto> securityProfileDtos;

    private SecurityProfileDto securityProfileDto;

    private JsonNode contextHistory;

    @When("^un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère tous les profile de sécurité applicatifs en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_SECURITY_PROFILE_récupère_tous_les_profile_de_sécurité_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_SECURITY_PROFILE() {
    	securityProfileDtos = getSecurityProfileRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne tous les profile de sécurité$")
    public void le_serveur_retourne_tous_les_profile_de_sécurité() {
        assertThat(securityProfileDtos).isNotNull();

        final int size = securityProfileDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(1);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère un profile de sécurité par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_SECURITY_PROFILE_récupère_un_profile_de_sécurité_par_son_identifiant_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_SECURITY_PROFILE() {
    	securityProfileDto = getSecurityProfileRestClient().getOne(getSystemTenantUserAdminContext(), TestConstants.SECURITY_PROFILE_IDENTIFIER, Optional.empty());
    }

    @Then("^le serveur retourne le profile de sécurité avec cet identifiant$")
    public void le_serveur_retourne_le_client_avec_cet_identifiant() {
        assertThat(securityProfileDto).isNotNull();
        assertThat(securityProfileDto.getIdentifier()).isEqualTo(TestConstants.SECURITY_PROFILE_IDENTIFIER);
        assertThat(securityProfileDto.getName()).isEqualTo(TestConstants.SECURITY_PROFILE_NAME);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère tous les profile de sécurité par code et nom en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_SECURITY_PROFILE_récupère_tous_les_profile_de_sécurité_par_code_et_nom_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_SECURITY_PROFILE() {
        QueryDto criteria = QueryDto.criteria(
        	"id", TestConstants.SECURITY_PROFILE_IDENTIFIER, CriterionOperator.EQUALS).addCriterion(
        	"name",TestConstants.SECURITY_PROFILE_NAME, CriterionOperator.EQUALS);
        securityProfileDtos = getSecurityProfileRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne les profile de sécurité par code et nom$")
    public void le_serveur_retourne_les_profile_de_sécurité_par_code_et_nom() {
        assertThat(securityProfileDtos).isNotNull().isNotEmpty();
        assertThat(securityProfileDtos.stream().anyMatch(c ->
        	(c.getIdentifier().equals(TestConstants.SECURITY_PROFILE_IDENTIFIER) && c.getName().equals(TestConstants.SECURITY_PROFILE_NAME)
        ))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère tous les profile de sécurité par code ou nom en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_SECURITY_PROFILE_récupère_tous_les_profile_de_sécurité_par_code_ou_nom_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_SECURITY_PROFILE() {
        QueryDto criteria = new QueryDto(QueryOperator.OR)
        	.addCriterion("id", TestConstants.SECURITY_PROFILE_IDENTIFIER, CriterionOperator.EQUALS)
        	.addCriterion("name",TestConstants.SECURITY_PROFILE_NAME, CriterionOperator.EQUALS);
        securityProfileDtos = getSecurityProfileRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne les profile de sécurité par code ou nom$")
    public void le_serveur_retourne_les_profile_de_sécurité_par_code_ou_nom() {
        assertThat(securityProfileDtos).isNotNull().isNotEmpty();
        assertThat(securityProfileDtos.stream().anyMatch(c ->
        	(c.getIdentifier().equals(TestConstants.SECURITY_PROFILE_IDENTIFIER) || c.getName().equals(TestConstants.SECURITY_PROFILE_NAME)
        ))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère tous les profile de sécurité avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_SECURITY_PROFILE_récupère_tous_les_profile_de_sécurité_avec_pagination_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_SECURITY_PROFILE() {
    	final PaginatedValuesDto<SecurityProfileDto> paginatedContexts = getSecurityProfileRestClient().getAllPaginated(
    		getSystemTenantUserAdminContext(), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        securityProfileDtos = new ArrayList<>(paginatedContexts.getValues());
    }

    @Then("^le serveur retourne les profile de sécurité paginés$")
    public void le_serveur_retourne_les_clients_paginés() {
    	le_serveur_retourne_les_profile_de_sécurité_par_code_ou_nom();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère l'historique d'un profile de sécurité à partir de son son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_SECURITY_PROFILE_récupère_l_historique_d_un_profile_de_sécurité_à_partir_de_son_identifiant_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_SECURITY_PROFILE() {
    	contextHistory = getSecurityProfileRestClient().findHistoryById(getSystemTenantUserAdminContext(), TestConstants.SECURITY_PROFILE_IDENTIFIER);
    }

    @Then("^le serveur retourne l'historique du profile de sécurité$")
    public void le_serveur_retourne_l_historique_du_profile_de_sécurité() {
        assertThat(contextHistory).isNotNull();
    }

}
