package fr.gouv.vitamui.iam.server.common.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.security.WebSecurityConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import(value = { WebSecurityConfig.class })
public abstract class ApiIamControllerTest<T extends IdDto> extends ApiCrudControllerTest<T> {

    @MockBean
    private ApiAuthenticationProvider apiAuthenticationProvider;

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private CustomerRepository repository;

    @MockBean
    private SecurityService securityService;
}
