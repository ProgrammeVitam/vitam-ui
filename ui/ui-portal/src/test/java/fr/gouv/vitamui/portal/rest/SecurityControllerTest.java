package fr.gouv.vitamui.portal.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.portal.config.PortalApplicationProperties;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.rest.SecurityController;
import fr.gouv.vitamui.ui.commons.rest.UIControllerTest;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(classes = { UICommonsAutoSpringMockConfiguration.class, UICommonsAutoConfiguration.class })
@WebMvcTest(controllers = { SecurityController.class })
@Import(value = { PortalApplicationProperties.class, SecurityConfig.class, ServerIdentityConfiguration.class })
@TestPropertySource(properties = { "spring.config.name=ui-portal-application" })
public class SecurityControllerTest extends UIControllerTest<UserDto> {

    @Value("${ui-portal.prefix}")
    protected String apiUrl;

    private final String PREFIX = "/security";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SecurityControllerTest.class);

    @MockBean
    private BuildProperties buildProperties;

    @Test
    public void testGetUserConnected() throws Exception {
        super.testGetAllEntity();
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
