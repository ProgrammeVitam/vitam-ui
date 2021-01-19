package fr.gouv.vitamui.iam.external.server.service;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.test.utils.UserBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;


/**
 * Unit test for {@link UserExternalService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserExternalServiceTest {

    private UserExternalService userExternalService;

    private AuthUserDto authUserDto;

    @Mock
    private ExternalSecurityService externalSecurityService;

    @Mock
    private UserInternalRestClient userInternalRestClient;

    @Before
    public void init() {
        userExternalService = new UserExternalService(userInternalRestClient, externalSecurityService);
        authUserDto = new AuthUserDto();
        authUserDto.setId("2626");
    }

    /**
     * Test method for {@link UserExternalService#checkLogbookRight}.
     */
    @Test(expected = ForbiddenException.class)
    public void checkLogbookRight_when_accesOther_withoutRoleGetUsers() {
        when(externalSecurityService.hasRole(ServicesData.ROLE_GET_USERS)).thenReturn(false);
        when(externalSecurityService.getUser()).thenReturn(authUserDto);
        userExternalService.checkLogbookRight("2666");
    }

    @Test
    public void patchAnalyticsShouldReturnUserWithNewData() {
        UserDto user = UserBuilder.buildWithAnalytics();
        when(userInternalRestClient.patchAnalytics(any(), any())).thenReturn(user);
        mockSecurityContext();
        Map<String, Object> analytics = Map.of(APPLICATION_ID, "RECORD_MANAGEMENT_APP");

        UserDto result = userExternalService.patchAnalytics(analytics);

        assertThat(result).isEqualTo(user);
        ArgumentCaptor<Map<String, Object>> arg = ArgumentCaptor.forClass(Map.class);
        verify(userInternalRestClient).patchAnalytics(any(), arg.capture());
        assertThat(arg.getValue()).isEqualTo(analytics);
    }

    private void mockSecurityContext(final String... userRoles) {
        final AuthUserDto user = new AuthUserDto();
        user.setId("79");
        user.setLevel("");
        user.setCustomerId("customerIdAllowed");
        final List<String> roles = Arrays.asList(userRoles);

        roles.forEach(r -> Mockito.when(externalSecurityService.hasRole(r)).thenReturn(true));

        when(externalSecurityService.getUser()).thenReturn(user);
        when(externalSecurityService.getHttpContext()).thenReturn(new ExternalHttpContext(10, "userToken", APPLICATION_ID, "identifier"));
    }
}
