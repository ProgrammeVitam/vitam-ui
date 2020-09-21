package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import org.apache.commons.collections4.CollectionUtils;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

public class ExternalServiceTest {

    protected void mockSecurityContext(ExternalSecurityService externalSecurityService, final String userCustomerId, final Integer tenantIdentifier,
                                       final String... userRoles) {
        final AuthUserDto user = new AuthUserDto();
        user.setLevel("");
        user.setCustomerId(userCustomerId);
        final List<String> roles = Arrays.asList(userRoles);
        if (CollectionUtils.isNotEmpty(roles)) {
            roles.forEach(r -> Mockito.when(externalSecurityService.hasRole(r)).thenReturn(true));
        }

        Mockito.when(externalSecurityService.getUser()).thenReturn(user);
        Mockito.when(externalSecurityService.getHttpContext())
            .thenReturn(new ExternalHttpContext(10, "userToken", "applicationid", "id"));
    }
}
