package fr.gouv.vitamui.iam.internal.server.common.rest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.security.IamApiAuthenticationProvider;
import fr.gouv.vitamui.iam.internal.server.security.WebSecurityConfig;

@Import(value = { WebSecurityConfig.class, ServerIdentityConfiguration.class })
@TestPropertySource(properties = { "spring.config.name=iam-internal-application" })
public abstract class ApiIamControllerTest<T extends IdDto> extends ApiCrudControllerTest<T> {

    @MockBean
    private IamApiAuthenticationProvider iamApiAuthenticationProvider;

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private CustomerRepository repository;
}
