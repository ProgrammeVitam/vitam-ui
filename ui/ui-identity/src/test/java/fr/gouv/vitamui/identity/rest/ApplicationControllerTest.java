package fr.gouv.vitamui.identity.rest;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.ui.commons.rest.ApplicationController;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { ApplicationController.class })
public class ApplicationControllerTest extends UiIdentityRestControllerTest<ApplicationDto> {

    @Value("${ui-identity.prefix}")
    protected String apiUrl;

    @MockBean
    private ApplicationService applicationService;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationControllerTest.class);

    private static final String PREFIX = "/ui/applications";

    @Test
    public void testGetApplications() throws Exception {
        super.testGetAllEntity();
    }

    @Test
    public void testfindApplicationsByNames() throws Exception {
        LOGGER.debug("testfindApplicationsByName");
        performGet(StringUtils.EMPTY, ImmutableMap.of("term", "ab", "lang", "ENGLISH"));
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + ApplicationControllerTest.PREFIX;
    }

    @Override
    protected Class<ApplicationDto> getDtoClass() {
        return ApplicationDto.class;
    }

    @Override
    protected ApplicationDto buildDto() {
        return new ApplicationDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return ApplicationControllerTest.LOGGER;
    }

    @Override
    protected void preparedServices() {
        // do nothing
    }

}
