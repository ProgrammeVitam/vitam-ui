package fr.gouv.vitamui.portal.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.portal.config.PortalApplicationProperties;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.rest.ApplicationController;
import fr.gouv.vitamui.ui.commons.rest.UIControllerTest;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(
    classes = {
        UICommonsAutoSpringMockConfiguration.class,
        UICommonsAutoConfiguration.class,
        ServerIdentityAutoConfiguration.class,
    }
)
@Import(value = { PortalApplicationProperties.class })
@WebMvcTest(controllers = { ApplicationController.class })
public class ApplicationControllerTest extends UIControllerTest<ApplicationDto> {

    @Value("${ui-prefix}")
    protected String apiUrl;

    @MockBean
    private ApplicationService applicationService;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationControllerTest.class);

    private static final String PREFIX = "/ui/applications";

    @Test
    public void testGetApplications() {
        final ResultActions result = performGet("/filtered");
    }

    @Test
    public void testfindApplicationsByNames() {
        final ResultActions result = performGet("/filtered", ImmutableMap.of("term", "ab", "lang", "ENGLISH"));
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
    protected void preparedServices() {}
}
