package fr.gouv.vitamui.iam.server.common.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.security.WebSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(value = { WebSecurityConfig.class })
public abstract class ApiIamControllerTest<T extends IdDto> extends ApiCrudControllerTest<T> {

    @MockitoBean
    private ApiAuthenticationProvider apiAuthenticationProvider;

    @MockitoBean
    private RestExceptionHandler restExceptionHandler;

    @MockitoBean
    private CustomerRepository repository;

    @MockitoBean
    private SecurityService securityService;
}
