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
package fr.gouv.vitamui.cucumber.back.steps.archivesearch;

import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static fr.gouv.vitamui.utils.TestConstants.SEARCH_CRITERIA_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiArchiveSearchSearchCriteriaHistoryCoupleOfCheckSteps extends CommonSteps {

    private List<SearchCriteriaHistoryDto> searchCriteriaHistoryDtos;
    private SearchCriteriaHistoryDto searchCriteriaHistoryDto;

    @When("^l'utilisateur avec le rôle ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH récupère tous les critères enregistrés$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_ACCESS_CONTRACT_récupère_tous_les_criteres_enregistres_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH() {
        searchCriteriaHistoryDtos = getSearchCriteriaHistoryExternalRestClient()
            .getSearchCriteriaHistory(getSystemTenantUserAdminContext());
    }

    @Then("le nom de la recherche est {string}")
    public void le_serveur_retourne_la_liste_des_criteres_enregistres(String name) {
        searchCriteriaHistoryDto = searchCriteriaHistoryDtos
            .stream()
            .filter(c -> c.getName().equals(SEARCH_CRITERIA_NAME))
            .findFirst()
            .get();
        assertThat(searchCriteriaHistoryDtos.get(0).getName()).isEqualTo(SEARCH_CRITERIA_NAME);
    }

    @Then("la liste des critères est de taille {int}")
    public void nombre_de_criteres(Integer size) {
        assertThat(searchCriteriaHistoryDto.getSearchCriteriaList().size()).isEqualTo(2);
    }

    @Then("l'utilisateur associé a l'identifiant {string}")
    public void utilisateur_associe(String identifiant) {
        assertThat(searchCriteriaHistoryDto.getUserId()).isEqualTo("1");
    }
}
