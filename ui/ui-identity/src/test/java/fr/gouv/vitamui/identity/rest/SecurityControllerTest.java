package fr.gouv.vitamui.identity.rest;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.ui.commons.rest.SecurityController;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { SecurityController.class })
public class SecurityControllerTest extends UiIdentityRestControllerTest<UserDto> {

    @Value("${ui-identity.prefix}")
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
