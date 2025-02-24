package fr.gouv.vitamui.pastis.server.rest;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.rest.AbstractRestControllerMockMvcTest;
import fr.gouv.vitamui.iam.security.authentication.ExternalAuthentication;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;

import java.util.List;

public abstract class ControllerTest extends AbstractRestControllerMockMvcTest {

    @MockBean
    private ExternalApiAuthenticationProvider apiAuthenticationProvider;

    @Override
    protected Authentication buildUserAuthenticated() {
        final Authentication authentication = new ExternalAuthentication(
            buildPrincipal(),
            buildCredentials(),
            null,
            buildUserRoles()
        );
        return authentication;
    }

    protected AuthUserDto buildPrincipal() {
        final AuthUserDto user = new AuthUserDto();
        user.setFirstname("test");
        return user;
    }

    protected ExternalHttpContext buildCredentials() {
        return null;
    }

    protected List<String> buildUserRoles() {
        return ServicesData.getServicesByName(getServices());
    }

    protected abstract String[] getServices();
}
