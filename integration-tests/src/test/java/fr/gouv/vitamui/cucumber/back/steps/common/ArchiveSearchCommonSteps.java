package fr.gouv.vitamui.cucumber.back.steps.common;

import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import io.cucumber.java.en.Given;

/**
 * Common steps for the ARCHIVE SEARCH API.
 */
public class ArchiveSearchCommonSteps extends CommonSteps {

    @Given("^un critere de recherche a été créé$")
    public void un_critere_de_recherche_a_été_créé() {
        testContext.savedSearchCriteriaHistoryDto = FactoryDto.buildDto(SearchCriteriaHistoryDto.class);
        testContext.searchCriteriaHistoryDto = getSearchCriteriaHistoryExternalRestClient()
            .create(getSystemTenantUserAdminContext(), testContext.savedSearchCriteriaHistoryDto);
    }
}
