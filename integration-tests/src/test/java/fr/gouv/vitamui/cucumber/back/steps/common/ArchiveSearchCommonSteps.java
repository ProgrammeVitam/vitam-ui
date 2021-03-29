package fr.gouv.vitamui.cucumber.back.steps.common;

import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.cucumber.common.parametertypes.LevelParameterType;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.utils.TestConstants.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common steps for the ARCHIVE SEARCH API.
 */
public class ArchiveSearchCommonSteps extends CommonSteps {

    @Given("^un critere de recherche a été créé$")
    public void un_critere_de_recherche_a_été_créé() {
        testContext.savedSearchCriteriaHistoryDto = FactoryDto.buildDto(SearchCriteriaHistoryDto.class);
        testContext.searchCriteriaHistoryDto = getSearchCriteriaHistoryExternalRestClient().create(getSystemTenantUserAdminContext(), testContext.savedSearchCriteriaHistoryDto);
    }
}
