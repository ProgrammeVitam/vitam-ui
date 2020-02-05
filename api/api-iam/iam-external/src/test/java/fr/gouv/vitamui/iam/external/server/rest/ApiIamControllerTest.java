package fr.gouv.vitamui.iam.external.server.rest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.external.server.security.WebSecurityConfig;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;

@Import(value = { WebSecurityConfig.class, ServerIdentityConfiguration.class, RestExceptionHandler.class })
@TestPropertySource(properties = { "spring.config.name=iam-external-application" })
public abstract class ApiIamControllerTest<T extends IdDto> extends ApiControllerTest<T> {

    @MockBean
    private ExternalApiAuthenticationProvider apiAuthenticationProvider;
}
