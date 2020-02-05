package fr.gouv.vitamui.iam.external.server.service;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;


/**
 * Unit test for {@link UserExternalService}.
 *
 *
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

}
