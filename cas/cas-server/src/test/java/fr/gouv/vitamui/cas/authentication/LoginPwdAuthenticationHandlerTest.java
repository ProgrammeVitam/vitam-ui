package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import jakarta.servlet.http.HttpServletRequest;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link LoginPwdAuthenticationHandler}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LoginPwdAuthenticationHandlerTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class LoginPwdAuthenticationHandlerTest {

    private static final String USERNAME = "user@test.com";
    private static final String CUSTOMER_ID = "customerId";
    private static final String SUPER_USER_EMAIL = "superuser@system.com";
    private static final String SUPER_USER_CUSTOMER_ID = "superuser_customerId";

    private static final String PASSWORD = "password";
    private static final String IP_ADDRESS = "1.2.3.4";
    private static final String IP_HEADER_NAME = "X-Real-IP";

    private LoginPwdAuthenticationHandler handler;

    private CasApi casApi;

    private Credential credential;
    private LocalAttributeMap<Object> flowParameters;

    @Before
    public void setUp() {
        casApi = mock(CasApi.class);
        handler = new LoginPwdAuthenticationHandler(new DefaultPrincipalFactory(), casApi, IP_HEADER_NAME);
        credential = new UsernamePasswordCredential("ignored", PASSWORD);

        RequestContext requestContext = mock(RequestContext.class);

        flowParameters = new LocalAttributeMap<>();
        doReturn(flowParameters).when(requestContext).getFlowScope();
        RequestContextHolder.setRequestContext(requestContext);

        ExternalContext externalContext = mock(ExternalContext.class);
        HttpServletRequest nativeRequest = mock(HttpServletRequest.class);
        doReturn(externalContext).when(requestContext).getExternalContext();
        doReturn(nativeRequest).when(externalContext).getNativeRequest();
        doReturn(IP_ADDRESS).when(nativeRequest).getHeader(IP_HEADER_NAME);
    }

    @After
    public void reset() {
        RequestContextHolder.setRequestContext(null);
    }

    @Test
    public void testSuccessfulAuthentication() throws Throwable {
        // Given
        givenLoginRequestInRequestContext();

        when(casApi.login(eq(userCredentials()))).thenReturn(basicUser(UserStatusEnum.ENABLED));

        // When
        final var result = handler.authenticate(credential, null);

        // Then
        assertEquals(USERNAME, result.getPrincipal().getId());
        assertEquals(USERNAME, result.getPrincipal().getAttributes().get(Constants.FLOW_LOGIN_EMAIL).getFirst());
        assertEquals(
            CUSTOMER_ID,
            result.getPrincipal().getAttributes().get(Constants.FLOW_LOGIN_CUSTOMER_ID).getFirst()
        );
        assertNull(result.getPrincipal().getAttributes().get(Constants.FLOW_SURROGATE_EMAIL));
        assertNull(result.getPrincipal().getAttributes().get(Constants.FLOW_SURROGATE_CUSTOMER_ID));
    }

    @Test
    public void testSuccessfulSubrogationAuthentication() throws Throwable {
        // Given
        givenSubrogationRequestInRequestContext();

        when(casApi.login(eq(surrogateCredentials()))).thenReturn(basicUser(UserStatusEnum.ENABLED));

        // When
        final var result = handler.authenticate(credential, null);

        // Then
        assertEquals(SUPER_USER_EMAIL, result.getPrincipal().getId());
        assertEquals(
            SUPER_USER_EMAIL,
            result.getPrincipal().getAttributes().get(Constants.FLOW_LOGIN_EMAIL).getFirst()
        );
        assertEquals(
            SUPER_USER_CUSTOMER_ID,
            result.getPrincipal().getAttributes().get(Constants.FLOW_LOGIN_CUSTOMER_ID).getFirst()
        );
        assertEquals(USERNAME, result.getPrincipal().getAttributes().get(Constants.FLOW_SURROGATE_EMAIL).getFirst());
        assertEquals(
            CUSTOMER_ID,
            result.getPrincipal().getAttributes().get(Constants.FLOW_SURROGATE_CUSTOMER_ID).getFirst()
        );
    }

    @Test
    public void testNoUser() {
        // Given
        givenLoginRequestInRequestContext();

        when(casApi.login(eq(userCredentials()))).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> handler.authenticate(credential, null)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    public void testUserDisabled() {
        // Given
        givenLoginRequestInRequestContext();

        when(casApi.login(eq(userCredentials()))).thenReturn(basicUser(UserStatusEnum.DISABLED));

        // When / Then
        assertThatThrownBy(() -> handler.authenticate(credential, null)).isInstanceOf(AccountException.class);
    }

    @Test
    public void testUserCannotLogin() {
        // Given
        givenLoginRequestInRequestContext();

        when(casApi.login(eq(userCredentials()))).thenReturn(basicUser(UserStatusEnum.BLOCKED));

        // When / Then
        assertThatThrownBy(() -> handler.authenticate(credential, null)).isInstanceOf(AccountException.class);
    }

    @Test
    public void testExpiredPassword() {
        // Given
        givenLoginRequestInRequestContext();

        final var user = basicUser(UserStatusEnum.ENABLED);
        user.setPasswordExpirationDate(OffsetDateTime.now().minusDays(1));
        when(casApi.login(eq(userCredentials()))).thenReturn(user);

        // When / Then
        assertThatThrownBy(() -> handler.authenticate(credential, null)).isInstanceOf(
            AccountPasswordMustChangeException.class
        );
    }

    @Test
    public void testUserBadCredentials() {
        // Given
        givenLoginRequestInRequestContext();

        when(casApi.login(eq(userCredentials()))).thenThrow(new InvalidAuthenticationException(""));

        // When / Then
        assertThatThrownBy(() -> handler.authenticate(credential, null)).isInstanceOf(CredentialException.class);
    }

    @Test
    public void testUserLockedAccount() {
        // Given
        givenLoginRequestInRequestContext();

        when(casApi.login(eq(userCredentials()))).thenThrow(new TooManyRequestsException(""));

        // When / Then
        assertThatThrownBy(() -> handler.authenticate(credential, null)).isInstanceOf(AccountLockedException.class);
    }

    @Test
    public void testTechnicalError() {
        // Given
        givenLoginRequestInRequestContext();

        when(casApi.login(eq(userCredentials()))).thenThrow(new BadRequestException(""));

        // When / Then
        assertThatThrownBy(() -> handler.authenticate(credential, null)).isInstanceOf(PreventedException.class);
    }

    private UserDto basicUser(final UserStatusEnum status) {
        final var user = new UserDto();
        user.setStatus(status);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setPasswordExpirationDate(OffsetDateTime.now().plusDays(1));
        return user;
    }

    private void givenLoginRequestInRequestContext() {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USERNAME);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, null);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, null);
    }

    private void givenSubrogationRequestInRequestContext() {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, SUPER_USER_EMAIL);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, SUPER_USER_CUSTOMER_ID);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USERNAME);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID);
    }

    private LoginRequestDto userCredentials() {
        final LoginRequestDto credentials = new LoginRequestDto();

        credentials.setLoginEmail(USERNAME);
        credentials.setPassword(PASSWORD);
        credentials.setLoginCustomerId(CUSTOMER_ID);
        credentials.setSurrogateEmail(null);
        credentials.setSurrogateCustomerId(null);
        credentials.setIp(IP_ADDRESS);

        return credentials;
    }

    private LoginRequestDto surrogateCredentials() {
        final LoginRequestDto credentials = userCredentials();

        credentials.setSurrogateEmail(credentials.getLoginEmail());
        credentials.setSurrogateCustomerId(credentials.getLoginCustomerId());

        credentials.setLoginEmail(SUPER_USER_EMAIL);
        credentials.setLoginCustomerId(SUPER_USER_CUSTOMER_ID);

        return credentials;
    }
}
