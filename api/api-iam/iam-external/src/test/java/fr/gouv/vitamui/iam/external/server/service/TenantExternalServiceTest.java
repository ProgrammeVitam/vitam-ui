package fr.gouv.vitamui.iam.external.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.client.TenantInternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;

public class TenantExternalServiceTest {

    private TenantExternalService tenantExternalService;

    private final TenantInternalRestClient tenantInternalRestClient = Mockito.mock(TenantInternalRestClient.class);

    private final ExternalSecurityService externalSecurityService = Mockito.mock(ExternalSecurityService.class);

    @Before
    public void before() {
        tenantExternalService = new TenantExternalService(externalSecurityService, tenantInternalRestClient);
        Mockito.reset(tenantInternalRestClient, externalSecurityService);
    }

    @Test
    public void testGetRestrictedKeys() {
        Mockito.when(externalSecurityService.hasRole(ServicesData.ROLE_GET_TENANTS)).thenReturn(false);
        Collection<String> restrictedKeys = tenantExternalService.getRestrictedKeys();
        assertThat(restrictedKeys).containsExactly("customerId", "identifier");

        Mockito.when(externalSecurityService.hasRole(ServicesData.ROLE_GET_ALL_TENANTS)).thenReturn(true);
        restrictedKeys = tenantExternalService.getRestrictedKeys();
        assertThat(restrictedKeys).isEmpty();
    }

    @Test
    public void testGetAllAllowed() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(userCustomerId, 10);

        final QueryDto criteria = QueryDto.criteria("customerId", userCustomerId, CriterionOperator.EQUALS);
        tenantExternalService.getAll(criteria.toOptionalJson());
    }

    @Test
    public void testGetAllAllowedCustomerIdError() {
        final String userCustomerId = "customerIdAllowed";

        mockSecurityContext(userCustomerId, 10, ServicesData.ROLE_GET_TENANTS);
        final QueryDto criteria = QueryDto.criteria("customerId", "customerIdForbidden", CriterionOperator.EQUALS);
        tenantExternalService.getAll(criteria.toOptionalJson());
    }

    private void mockSecurityContext(final String userCustomerId, final Integer tenantIdentifier,
            final String... userRoles) {
        final AuthUserDto user = new AuthUserDto();
        user.setLevel("");
        user.setCustomerId(userCustomerId);
        final List<String> roles = Arrays.asList(userRoles);
        if (roles != null && roles.size() > 0) {
            roles.forEach(r -> Mockito.when(externalSecurityService.hasRole(r)).thenReturn(true));
        }
        Mockito.when(externalSecurityService.getCustomerId()).thenReturn(userCustomerId);
        Mockito.when(externalSecurityService.getTenantIdentifier()).thenReturn(tenantIdentifier);
        Mockito.when(externalSecurityService.getUser()).thenReturn(user);
        Mockito.when(externalSecurityService.getHttpContext())
                .thenReturn(new ExternalHttpContext(10, "userToken", "applicationid", "id"));
    }
}
