package fr.gouv.vitamui.ui.commons.rest;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.config.UIPropertiesImpl;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(classes = { UICommonsAutoConfiguration.class, UICommonsAutoSpringMockConfiguration.class })
@WebMvcTest(controllers = { SecurityController.class })
@Import(value = { SecurityConfig.class, ServerIdentityConfiguration.class })
public class SecurityControllerTest extends UIControllerTest<UserDto> {

    @TestConfiguration
    static class UserConfiguration {

        @Bean
        public UIProperties uiProperties() {
            final UIPropertiesImpl properties = new UIPropertiesImpl();
            properties.setIamExternalClient(new RestClientConfiguration());
            return properties;
        }
    }

    @Value("${ui-prefix}")
    protected String apiUrl;

    private final String PREFIX = "/security";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SecurityControllerTest.class);

    @MockBean
    private BuildProperties buildProperties;

    @Test
    public void testGetUserConnected() throws Exception {
        preparedServices();
        super.performGet(StringUtils.EMPTY);
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected Class<UserDto> getDtoClass() {
        return UserDto.class;
    }

    @Override
    protected UserDto buildDto() {
        return new UserDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
    }
}
