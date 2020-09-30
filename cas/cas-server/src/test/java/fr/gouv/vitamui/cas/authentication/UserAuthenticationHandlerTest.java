package fr.gouv.vitamui.cas.authentication;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.val;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Before;
import org.junit.Test;

import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests {@link UserAuthenticationHandler}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class UserAuthenticationHandlerTest {

    private static final String USERNAME = "jleleu@test.com";

    private static final String PASSWORD = "password";

    private UserAuthenticationHandler handler;

    private CasExternalRestClient casExternalRestClient;

    private Credential credential;

    @Before
    public void setUp() {
        casExternalRestClient = mock(CasExternalRestClient.class);
        val utils = new Utils(null, 0, null, null);
        handler = new UserAuthenticationHandler(null, new DefaultPrincipalFactory(), casExternalRestClient, utils, null);
        credential = new UsernamePasswordCredential(USERNAME, PASSWORD);
    }

    @Test
    public void testSuccessfulAuthentication() throws GeneralSecurityException, PreventedException {
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null)))
                .thenReturn(basicUser(UserStatusEnum.ENABLED));

        val result = handler.authenticate(credential);
        assertEquals(USERNAME, result.getPrincipal().getId());
    }

    @Test
    public void testSuccessfulAuthentication_when_spaceBeforeAndAfterUsername() throws GeneralSecurityException, PreventedException {
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null)))
                .thenReturn(basicUser(UserStatusEnum.ENABLED));

        credential = new UsernamePasswordCredential(" " +USERNAME + "  ", PASSWORD);
        val result = handler.authenticate(credential);
        assertEquals(USERNAME, result.getPrincipal().getId());
    }

    @Test(expected = AccountNotFoundException.class)
    public void testNoUser() throws GeneralSecurityException, PreventedException {
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null))).thenReturn(null);

        handler.authenticate(credential);
    }

    @Test(expected = AccountException.class)
    public void testUserDisabled() throws GeneralSecurityException, PreventedException {
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null)))
                .thenReturn(basicUser(UserStatusEnum.DISABLED));

        handler.authenticate(credential);
    }

    @Test(expected = AccountException.class)
    public void testUserCannotLogin() throws GeneralSecurityException, PreventedException {
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null)))
                .thenReturn(basicUser(UserStatusEnum.BLOCKED));

        handler.authenticate(credential);
    }

    @Test(expected = AccountPasswordMustChangeException.class)
    public void testExpiredPassword() throws GeneralSecurityException, PreventedException {
        val user = basicUser(UserStatusEnum.ENABLED);
        user.setPasswordExpirationDate(OffsetDateTime.now().minusDays(1));
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null)))
            .thenReturn(user);

        handler.authenticate(credential);
    }

    @Test(expected = CredentialException.class)
    public void testUserBadCredentials() throws GeneralSecurityException, PreventedException {
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null)))
                .thenThrow(new InvalidAuthenticationException(""));

        handler.authenticate(credential);
    }

    @Test(expected = AccountLockedException.class)
    public void testUserLockedAccount() throws GeneralSecurityException, PreventedException {
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null)))
                .thenThrow(new TooManyRequestsException(""));

        handler.authenticate(credential);
    }

    @Test(expected = PreventedException.class)
    public void testTechnicalError() throws GeneralSecurityException, PreventedException {
        when(casExternalRestClient.login(any(ExternalHttpContext.class), eq(USERNAME), eq(PASSWORD), eq(null), eq(null)))
                .thenThrow(new BadRequestException(""));

        handler.authenticate(credential);
    }

    private UserDto basicUser(final UserStatusEnum status) {
        val user = new UserDto();
        user.setStatus(status);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setPasswordExpirationDate(OffsetDateTime.now().plusDays(1));
        return user;
    }
}
