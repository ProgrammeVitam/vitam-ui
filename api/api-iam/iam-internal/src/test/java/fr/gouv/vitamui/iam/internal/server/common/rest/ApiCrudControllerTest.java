package fr.gouv.vitamui.iam.internal.server.common.rest;

import java.util.List;

import org.springframework.security.core.Authentication;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.rest.AbstractMockMvcCrudControllerTest;
import fr.gouv.vitamui.iam.security.authentication.ExternalAuthentication;

public abstract class ApiCrudControllerTest<T extends IdDto> extends AbstractMockMvcCrudControllerTest<T> {

    @Override
    protected Authentication buildUserAuthenticated() {
        final Authentication authentication = new ExternalAuthentication(buildPrincipal(), buildCredentials(), null, buildUserRoles());
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
