package fr.gouv.vitamui.cas.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.*;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.junit.Before;
import org.junit.Test;

import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Tests {@link IamRestPasswordManagementService}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class IamRestPasswordManagementServiceTest {

    private static final String EMAIL = "jerome@test.com";

    private IamRestPasswordManagementService service;

    private CasExternalRestClient casExternalRestClient;

    private ProvidersService providersService;

    private Map<String, List<Object>> authAttributes;

    private IdentityProviderDto identityProviderDto;

    private IdentityProviderHelper identityProviderHelper;

    @Before
    public void setUp() {
        casExternalRestClient = mock(CasExternalRestClient.class);
        providersService = mock(ProvidersService.class);
        identityProviderHelper = mock(IdentityProviderHelper.class);
        identityProviderDto = new IdentityProviderDto();
        identityProviderDto.setInternal(true);
        when(identityProviderHelper.findByUserIdentifier(any(List.class), eq(EMAIL))).thenReturn(Optional.of(identityProviderDto));
        service = new IamRestPasswordManagementService(casExternalRestClient, null, providersService, identityProviderHelper);
        final Utils utils = new Utils(casExternalRestClient, null);
        service.setUtils(utils);
        final RequestContext context = mock(RequestContext.class);
        RequestContextHolder.setRequestContext(context);
        final MutableAttributeMap<Object> flowParameters = new LocalAttributeMap<>();
        when(context.getConversationScope()).thenReturn(flowParameters);
        when(context.getFlowScope()).thenReturn(flowParameters);
        final Map<String, AuthenticationHandlerExecutionResult> successes = new HashMap<>();
        successes.put("fake", null);
        authAttributes = new HashMap<>();
        flowParameters.put("authentication", new DefaultAuthentication(
            ZonedDateTime.now(),
            mock(Principal.class),
            authAttributes,
            successes,
            new ArrayList<>()
        ));
    }

    @Test
    public void testChangePasswordSuccessfully() {
        assertTrue(service.change(new UsernamePasswordCredential(EMAIL, "password"), new PasswordChangeRequest()));
    }

    @Test
    public void testChangePasswordFailsBecauseOfASuperUser() {
        authAttributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL, Collections.singletonList("fakeSuperUser"));

        try {
            service.change(new UsernamePasswordCredential(EMAIL, "password"), new PasswordChangeRequest());
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("cannot use password management with subrogation", e.getMessage());
        }
    }

    @Test
    public void testChangePasswordFailsBecauseUserIsExternal() {
        identityProviderDto.setInternal(null);

        try {
            service.change(new UsernamePasswordCredential(EMAIL, null), new PasswordChangeRequest());
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("only an internal user [" + EMAIL + "] can change his password", e.getMessage());
        }
    }

    @Test
    public void testChangePasswordFailsBecauseUserIsNotLinkedToAnIdentityProvider() {
        when(identityProviderHelper.findByUserIdentifier(any(List.class), eq(EMAIL))).thenReturn(Optional.empty());

        try {
            service.change(new UsernamePasswordCredential(EMAIL, null), new PasswordChangeRequest());
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("only a user [" + EMAIL + "] linked to an identity provider can change his password", e.getMessage());
        }
    }

    @Test
    public void testChangePasswordFailsAtServer() {
        doThrow(new InvalidAuthenticationException("")).when(casExternalRestClient)
                .changePassword(any(ExternalHttpContext.class), any(String.class), any(String.class));

        assertFalse(service.change(new UsernamePasswordCredential(EMAIL, "password"), new PasswordChangeRequest()));
    }

    @Test
    public void testFindEmailOk() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(EMAIL), eq(Optional.empty())))
                .thenReturn(user(UserStatusEnum.ENABLED));

        assertEquals(EMAIL, service.findEmail(EMAIL));
    }

    @Test
    public void testFindEmailErrorThrown() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(EMAIL), eq(Optional.empty())))
                .thenThrow(new BadRequestException("error"));

        assertNull(service.findEmail(EMAIL));
    }

    @Test
    public void testFindEmailUserNull() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(EMAIL), eq(Optional.empty())))
                .thenReturn(null);

        assertNull(service.findEmail(EMAIL));
    }

    @Test
    public void testFindEmailUserDisabled() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(EMAIL), eq(Optional.empty())))
                .thenReturn(user(UserStatusEnum.DISABLED));

        assertNull(service.findEmail(EMAIL));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetSecurityQuestionsOk() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(EMAIL), eq(Optional.empty())))
                .thenReturn(user(UserStatusEnum.ENABLED));

        service.getSecurityQuestions(EMAIL);
    }

    private UserDto user(final UserStatusEnum status) {
        final UserDto user = new UserDto();
        user.setStatus(status);
        user.setEmail(EMAIL);
        return user;
    }
}
