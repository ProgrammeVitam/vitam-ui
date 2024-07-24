package fr.gouv.vitamui.iam.external.server.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.external.server.security.WebSecurityConfig;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import(value = { WebSecurityConfig.class, RestExceptionHandler.class })
public abstract class ApiIamControllerTest<T extends IdDto> extends ApiControllerTest<T> {

    @MockBean
    private ExternalApiAuthenticationProvider apiAuthenticationProvider;
}
