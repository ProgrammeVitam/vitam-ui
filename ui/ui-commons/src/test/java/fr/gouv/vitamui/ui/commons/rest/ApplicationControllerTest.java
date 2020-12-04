package fr.gouv.vitamui.ui.commons.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.config.UIPropertiesImpl;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(classes = { UICommonsAutoConfiguration.class, UICommonsAutoSpringMockConfiguration.class })
@WebMvcTest(controllers = { ApplicationController.class })
@Import(value = { SecurityConfig.class, ServerIdentityConfiguration.class })
public class ApplicationControllerTest extends UIControllerTest<ApplicationDto> {

    @TestConfiguration
    static class UserConfiguration {

        @Bean
        public UIProperties uiProperties() {
            final UIPropertiesImpl properties = new UIPropertiesImpl();
            properties.setIamExternalClient(new RestClientConfiguration());
            return properties;
        }
    }

    protected String apiUrl = "/ui-api/ui";

    @MockBean
    private ApplicationService applicationService;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationControllerTest.class);

    private static final String PREFIX = "/applications";

    @Test
    public void testGetApplications() throws Exception {
        super.performGet("", ImmutableMap.of("filterApp", true));
    }

    @Test
    public void testGetConfiguration() throws Exception {
        super.performGet("/conf");
    }

    @Override
    protected String getRessourcePrefix() {
        return apiUrl + ApplicationControllerTest.PREFIX;
    }

    @Override
    protected Class<ApplicationDto> getDtoClass() {
        return ApplicationDto.class;
    }

    @Override
    protected ApplicationDto buildDto() {
        final ApplicationDto application = new ApplicationDto();
        return application;
    }

    @Override
    protected VitamUILogger getLog() {
        return ApplicationControllerTest.LOGGER;
    }

    @Override
    protected void preparedServices() {
    }

}
