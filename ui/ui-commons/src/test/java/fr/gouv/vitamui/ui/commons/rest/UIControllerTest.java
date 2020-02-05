package fr.gouv.vitamui.ui.commons.rest;

import java.util.Collection;
import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.rest.AbstractMockMvcCrudControllerTest;

public abstract class UIControllerTest<T extends IdDto> extends AbstractMockMvcCrudControllerTest<T> {

    @Override
    protected Authentication buildUserAuthenticated() {
        final Authentication authentication = new UsernamePasswordAuthenticationToken(buildPrincipal(),
                buildCredentials(), buildGrantedAuthorities());
        return authentication;
    }

    protected Object buildPrincipal() {
        final AuthUserDto principal = new AuthUserDto("test", new HashMap<>());
        return principal;
    }

    protected Object buildCredentials() {
        return null;
    }

    protected Collection<? extends GrantedAuthority> buildGrantedAuthorities() {
        return null;
    }

    @Override
    protected HttpHeaders getHeaders() {
        final HttpHeaders httpHeaders = super.getHeaders();
        httpHeaders.add(CommonConstants.X_TENANT_ID_HEADER, getDefaultTenantIdHeader());
        return httpHeaders;
    }

    protected String getDefaultTenantIdHeader() {
        return "1";
    }

}
