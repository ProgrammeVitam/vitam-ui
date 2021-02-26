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
package fr.gouv.vitamui.cucumber.back.steps.referential.ingestcontract;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Ingest Contracts dans Referentiel admin : opérations de récupération.
 *
 *
 */
public class ApiReferentialExternalIngestContractGetSteps extends CommonSteps {
    private List<IngestContractDto> ingestContractDtos;

    private IngestContractDto ingestContractDto;

    private JsonNode contextHistory;

    @When("^un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère tous les contrat d'entrée applicatifs en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_INGEST_CONTRACT_récupère_tous_les_contrat_d_entrée_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_INGEST_CONTRACT() {
    	ingestContractDtos = getIngestContractRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne tous les contrat d'entrée$")
    public void le_serveur_retourne_tous_les_contrat_d_entrée() {
        assertThat(ingestContractDtos).isNotNull();

        final int size = ingestContractDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(1);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère un contrat d'entrée par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_INGEST_CONTRACT_récupère_un_contrat_d_entrée_par_son_identifiant_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_INGEST_CONTRACT() {
    	ingestContractDto = getIngestContractRestClient().getOne(getSystemTenantUserAdminContext(), TestConstants.INGEST_CONTRACT_IDENTIFIER, Optional.empty());
    }

    @Then("^le serveur retourne le contrat d'entrée avec cet identifiant$")
    public void le_serveur_retourne_le_client_avec_cet_identifiant() {
        assertThat(ingestContractDto).isNotNull();
        assertThat(ingestContractDto.getIdentifier()).isEqualTo(TestConstants.INGEST_CONTRACT_IDENTIFIER);
        assertThat(ingestContractDto.getName()).isEqualTo(TestConstants.INGEST_CONTRACT_NAME);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère tous les contrat d'entrée par code et nom en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_INGEST_CONTRACT_récupère_tous_les_contrat_d_entrée_par_code_et_nom_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_INGEST_CONTRACT() {
        QueryDto criteria = QueryDto.criteria(
        	"id", TestConstants.INGEST_CONTRACT_IDENTIFIER, CriterionOperator.EQUALS).addCriterion(
        	"name",TestConstants.INGEST_CONTRACT_NAME, CriterionOperator.EQUALS);
        ingestContractDtos = getIngestContractRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne les contrat d'entrée par code et nom$")
    public void le_serveur_retourne_les_contrat_d_entrée_par_code_et_nom() {
        assertThat(ingestContractDtos).isNotNull().isNotEmpty();
        assertThat(ingestContractDtos.stream().anyMatch(c ->
        	(c.getIdentifier().equals(TestConstants.INGEST_CONTRACT_IDENTIFIER) && c.getName().equals(TestConstants.INGEST_CONTRACT_NAME)
        ))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère tous les contrat d'entrée par code ou nom en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_INGEST_CONTRACT_récupère_tous_les_contrat_d_entrée_par_code_ou_nom_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_INGEST_CONTRACT() {
        QueryDto criteria = new QueryDto(QueryOperator.OR)
        	.addCriterion("id", TestConstants.INGEST_CONTRACT_IDENTIFIER, CriterionOperator.EQUALS)
        	.addCriterion("name",TestConstants.INGEST_CONTRACT_NAME, CriterionOperator.EQUALS);
        ingestContractDtos = getIngestContractRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne les contrat d'entrée par code ou nom$")
    public void le_serveur_retourne_les_contrat_d_entrée_par_code_ou_nom() {
        assertThat(ingestContractDtos).isNotNull().isNotEmpty();
        assertThat(ingestContractDtos.stream().anyMatch(c ->
        	(c.getIdentifier().equals(TestConstants.INGEST_CONTRACT_IDENTIFIER) || c.getName().equals(TestConstants.INGEST_CONTRACT_NAME)
        ))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère tous les contrat d'entrée avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_INGEST_CONTRACT_récupère_tous_les_contrat_d_entrée_avec_pagination_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_INGEST_CONTRACT() {
    	final PaginatedValuesDto<IngestContractDto> paginatedContexts = getIngestContractRestClient().getAllPaginated(
    		getSystemTenantUserAdminContext(), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        ingestContractDtos = new ArrayList<>(paginatedContexts.getValues());
    }

    @Then("^le serveur retourne les contrat d'entrée paginés$")
    public void le_serveur_retourne_les_clients_paginés() {
    	le_serveur_retourne_les_contrat_d_entrée_par_code_ou_nom();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère l'historique d'un contrat d'entrée à partir de son son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_INGEST_CONTRACT_récupère_l_historique_d_un_contrat_d_entrée_à_partir_de_son_identifiant_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_INGEST_CONTRACT() {
    	contextHistory = getIngestContractRestClient().findHistoryById(getSystemTenantUserAdminContext(), TestConstants.INGEST_CONTRACT_IDENTIFIER);
    }

    @Then("^le serveur retourne l'historique du contrat d'entrée$")
    public void le_serveur_retourne_l_historique_du_contrat_d_entrée() {
        assertThat(contextHistory).isNotNull();
    }

}
