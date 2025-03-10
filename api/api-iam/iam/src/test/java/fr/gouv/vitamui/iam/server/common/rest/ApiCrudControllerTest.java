package fr.gouv.vitamui.iam.server.common.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.rest.AbstractMockMvcCrudControllerTest;
import fr.gouv.vitamui.iam.security.authentication.AuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

public abstract class ApiCrudControllerTest<T extends IdDto> extends AbstractMockMvcCrudControllerTest<T> {

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
