package fr.gouv.vitamui.iam.external.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.client.ProfileInternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;

public class ProfileExternalServiceTest {

    private ProfileExternalService profileExternalService;

    private final ProfileInternalRestClient profileInternalRestClient = Mockito.mock(ProfileInternalRestClient.class);

    private final ExternalSecurityService externalSecurityService = Mockito.mock(ExternalSecurityService.class);

    @Before
    public void before() {
        profileExternalService = new ProfileExternalService(profileInternalRestClient, externalSecurityService);
        Mockito.reset(profileInternalRestClient, externalSecurityService);
    }

    @Test
    public void testGetRestrictedKeys() {
        Mockito.when(externalSecurityService.hasRole(ServicesData.ROLE_GET_PROFILES_ALL_TENANTS)).thenReturn(false);
        Collection<String> restrictedKeys = profileExternalService.getRestrictedKeys();
        assertThat(restrictedKeys).containsExactly("customerId", "level", "tenantIdentifier");

        Mockito.when(externalSecurityService.hasRole(ServicesData.ROLE_GET_PROFILES_ALL_TENANTS)).thenReturn(true);
        restrictedKeys = profileExternalService.getRestrictedKeys();
        assertThat(restrictedKeys).containsExactly("customerId", "level");
    }

    @Test
    public void testGetAllAllowed() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(userCustomerId, 10);

        final QueryDto criteria = QueryDto.criteria("customerId", userCustomerId, CriterionOperator.EQUALS);
        profileExternalService.getAll(criteria.toOptionalJson(), Optional.empty());
    }

    @Test
    public void testGetAllAllowedCustomerIdForbidden() {
        final String userCustomerId = "customerIdAllowed";

        mockSecurityContext(userCustomerId, 10, ServicesData.ROLE_GET_PROFILES_ALL_TENANTS);
        final QueryDto criteria = QueryDto.criteria("customerId", "customerIdForbidden", CriterionOperator.EQUALS);
        profileExternalService.getAll(criteria.toOptionalJson(), Optional.empty());
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
        Mockito.when(externalSecurityService.userIsRootLevel()).thenReturn(true);
        Mockito.when(externalSecurityService.getCustomerId()).thenReturn(userCustomerId);
        Mockito.when(externalSecurityService.getTenantIdentifier()).thenReturn(tenantIdentifier);
        Mockito.when(externalSecurityService.getUser()).thenReturn(user);
        Mockito.when(externalSecurityService.getHttpContext())
        .thenReturn(new ExternalHttpContext(10, "userToken", "applicationid", "id"));
    }
}
