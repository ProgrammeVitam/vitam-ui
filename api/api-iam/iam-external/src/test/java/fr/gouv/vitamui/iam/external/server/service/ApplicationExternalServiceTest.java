package fr.gouv.vitamui.iam.external.server.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;

public class ApplicationExternalServiceTest {

    private ApplicationExternalService applicationExternalService;

    private final ApplicationInternalRestClient applicationInternalRestClient = Mockito.mock(ApplicationInternalRestClient.class);

    private final ExternalSecurityService externalSecurityService = Mockito.mock(ExternalSecurityService.class);

    @Before
    public void before() {
        applicationExternalService = new ApplicationExternalService(applicationInternalRestClient, externalSecurityService);
        Mockito.reset(applicationInternalRestClient, externalSecurityService);
    }

    @Test
    public void testGetAllAllowed() {
        final String userIdentifier = "identifier";
        mockSecurityContext("customerIdAllowed", 10);

        final QueryDto criteria = QueryDto.criteria("identifier", userIdentifier, CriterionOperator.EQUALS);
        applicationExternalService.getAll(criteria.toOptionalJson(), Optional.empty());
    }

    private void mockSecurityContext(final String userCustomerId, final Integer tenantIdentifier, final String... userRoles) {
        final AuthUserDto user = new AuthUserDto();
        user.setLevel("");
        user.setCustomerId(userCustomerId);
        final List<String> roles = Arrays.asList(userRoles);
        if (roles != null && roles.size() > 0) {
            roles.forEach(r -> Mockito.when(externalSecurityService.hasRole(r)).thenReturn(true));
        }
        Mockito.when(externalSecurityService.userIsRootLevel()).thenReturn(true);
        Mockito.when(externalSecurityService.getCustomerId()).thenReturn(userCustomerId);
        Mockito.when(externalSecurityService.getTenantIdentifier()).thenReturn(tenantIdentifier);
        Mockito.when(externalSecurityService.getUser()).thenReturn(user);
        Mockito.when(externalSecurityService.getHttpContext()).thenReturn(new ExternalHttpContext(10, "userToken", "applicationid", "identifier"));
    }
}
