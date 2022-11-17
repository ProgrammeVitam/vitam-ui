package fr.gouv.vitamui.archives.search.rest;

import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.archives.search.service.SearchCriteriaHistoryService;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(classes = { UICommonsAutoSpringMockConfiguration.class, UICommonsAutoConfiguration.class })
@WebMvcTest(controllers = { SearchCriteriaHistoryController.class })
public class SearchCriteriaHistoryControllerTest extends UiArchiveSearchControllerTest<SearchCriteriaHistoryDto> {

    @Value("${ui-archive-search.prefix}")
    protected String apiUrl;

    private final String PREFIX = "/archive-search/searchcriteriahistory";

    @MockBean
    private SearchCriteriaHistoryService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SearchCriteriaHistoryControllerTest.class);

    @MockBean
    private BuildProperties buildProperties;


    @Test
    public void testCreateSearchCriteriaHistory() {
        super.testCreateEntity();
    }

    @Test
    public void testUpdateSearchCriteriaHistory() {
        super.testUpdateEntity();
    }

    @Test
    public void testDeleteSearchCriteriaHistory() {
        super.performDelete("/1");
    }

    @Test
    public void testGetAllSearchCriteriaHistory() {
        super.testGetAllEntity();
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected Class<SearchCriteriaHistoryDto> getDtoClass() {
        return SearchCriteriaHistoryDto.class;
    }

    @Override
    protected SearchCriteriaHistoryDto buildDto() {
        return new SearchCriteriaHistoryDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.create(any(), any(SearchCriteriaHistoryDto.class))).thenReturn(new SearchCriteriaHistoryDto());
        Mockito.when(service.update(any(), any(SearchCriteriaHistoryDto.class))).thenReturn(new SearchCriteriaHistoryDto());
        Mockito.when(service.getOne(any(), any(), any())).thenReturn(new SearchCriteriaHistoryDto());
    }
}
