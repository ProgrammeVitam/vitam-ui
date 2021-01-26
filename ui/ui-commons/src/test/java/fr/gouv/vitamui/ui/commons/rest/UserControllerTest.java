package fr.gouv.vitamui.ui.commons.rest;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.config.UIPropertiesImpl;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import fr.gouv.vitamui.ui.commons.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(classes = {UICommonsAutoConfiguration.class, UICommonsAutoSpringMockConfiguration.class})
@WebMvcTest(controllers = {UserController.class})
@Import(value = {SecurityConfig.class, ServerIdentityConfiguration.class})
public class UserControllerTest extends UIControllerTest<UserDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserControllerTest.class);

    @MockBean
    private UserService userService;

    @Value("${ui-prefix}")
    protected String apiUrl;

    private final String PREFIX = "/users";

    @TestConfiguration
    static class UserConfiguration {

        @Bean
        public UIProperties uiProperties() {
            final UIPropertiesImpl properties = new UIPropertiesImpl();
            properties.setIamExternalClient(new RestClientConfiguration());
            return properties;
        }


        @MockBean
        private BuildProperties buildProperties;

    }

    @Test
    public void patchAnalyticsOk() throws Exception {
        LOGGER.debug("testPatchAnalytics");
        Map<String, Object> analytics = ImmutableMap.of(APPLICATION_ID, "API_SUPERVISION_APP");
        ResultActions result = this.performPost(getUriBuilder(CommonConstants.PATH_ANALYTICS), asJsonString(analytics), status().isCreated());
        Mockito.verify(userService).patchAnalytics(any(ExternalHttpContext.class), eq(analytics));
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
