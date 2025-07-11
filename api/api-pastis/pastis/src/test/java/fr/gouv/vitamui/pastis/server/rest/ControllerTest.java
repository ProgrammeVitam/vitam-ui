package fr.gouv.vitamui.pastis.server.rest;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.rest.AbstractRestControllerMockMvcTest;
import fr.gouv.vitamui.iam.security.authentication.AuthenticationToken;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

public abstract class ControllerTest extends AbstractRestControllerMockMvcTest {

    @MockitoBean
    private ApiAuthenticationProvider apiAuthenticationProvider;

    @MockitoBean
    protected SecurityService securityService;

    @Override
    protected Authentication buildUserAuthenticated() {
        final Authentication authentication = new AuthenticationToken(
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

    protected HttpContext buildCredentials() {
        return null;
    }

    protected List<String> buildUserRoles() {
        return ServicesData.getServicesByName(getServices());
    }

    protected abstract String[] getServices();
}
