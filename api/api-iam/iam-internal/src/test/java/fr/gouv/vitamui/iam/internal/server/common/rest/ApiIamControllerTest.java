package fr.gouv.vitamui.iam.internal.server.common.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.security.IamApiAuthenticationProvider;
import fr.gouv.vitamui.iam.internal.server.security.WebSecurityConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import(value = { WebSecurityConfig.class })
public abstract class ApiIamControllerTest<T extends IdDto> extends ApiCrudControllerTest<T> {

    @MockBean
    private IamApiAuthenticationProvider iamApiAuthenticationProvider;

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private CustomerRepository repository;
}
