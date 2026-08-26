package fr.gouv.vitamui.cas.webflow.login.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.delegation.Pac4jClientIdentityProviderDto;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.cas.ResolvedIdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link DispatcherAction}.
 */
@ContextConfiguration(classes = DispatcherActionTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class DispatcherActionTest extends BaseWebflowActionTest {

    private static final String USER_1 = "user1@vitamui.com";
    private static final String CUSTOMER_ID_1 = "customer1";
    private static final String USER_2 = "user2@vitamui.fr";
    private static final String CUSTOMER_ID_2 = "customer2";

    private static final String PROVIDER_ID = "providerId";

    private IdentityProviderHelper identityProviderHelper;

    private CasApi casApi;

    private DispatcherAction action;

    private Pac4jClientIdentityProviderDto provider;

    @Override
    @Before
    public void setUp() throws FileNotFoundException {
        super.setUp();

        ProvidersService providersService = mock(ProvidersService.class);
        identityProviderHelper = mock(IdentityProviderHelper.class);
        casApi = mock(CasApi.class);

        final Utils utils = new Utils(null, 0, null, null, "");
        action = new DispatcherAction(
            providersService,
            identityProviderHelper,
            casApi,
            utils,
            mock(SessionStore.class)
        );

        final SAML2Client client = new SAML2Client();
        provider = new Pac4jClientIdentityProviderDto(new IdentityProviderDto(), client);
        provider.setId(PROVIDER_ID);
        provider.setInternal(true);
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(anyList(), eq(USER_1), eq(CUSTOMER_ID_1))
        ).thenReturn(Optional.of(provider));
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(anyList(), eq(USER_2), eq(CUSTOMER_ID_2))
        ).thenReturn(Optional.of(provider));
        when(identityProviderHelper.findById(anyList(), eq(PROVIDER_ID))).thenReturn(Optional.of(provider));
        when(casApi.resolveIdentityProvider(anyString(), anyString())).thenAnswer(
            invocation -> new ResolvedIdentityProviderDto(PROVIDER_ID, provider.getInternal())
        );
    }

    @Test
    public void testNoIdentityProvider() throws IOException {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);

        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(anyList(), eq(USER_1), eq(CUSTOMER_ID_1))
        ).thenReturn(Optional.empty());
        when(casApi.resolveIdentityProvider(eq(USER_1), eq(CUSTOMER_ID_1))).thenReturn(
            new ResolvedIdentityProviderDto(null, false)
        );

        final Event event = action.doExecute(context);

        assertEquals("badConfiguration", event.getId());
    }

    @Test
    public void testInternalAuthnOK() throws IOException {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);

        final Event event = action.doExecute(context);

        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalAuthnDisabled() throws IOException {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);

        UserDto userDto = new UserDto();
        userDto.setCustomerId(CUSTOMER_ID_1);
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casApi.getUser(eq(USER_1), eq(CUSTOMER_ID_1), eq(PROVIDER_ID), eq(null))).thenReturn(
            new fr.gouv.vitamui.commons.security.client.dto.AuthUserDto(userDto)
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testUnknownUserIsNotBlockedSoThatAccountExistenceIsNotDisclosed() throws IOException {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);

        when(casApi.getUser(eq(USER_1), eq(CUSTOMER_ID_1), eq(PROVIDER_ID), eq(null))).thenReturn(null);

        final Event event = action.doExecute(context);

        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalSubrogation() throws IOException {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USER_2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);

        final Event event = action.doExecute(context);

        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalSubrogationSurrogateDisabled() throws IOException {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USER_2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);

        UserDto userDto = new UserDto();
        userDto.setCustomerId(CUSTOMER_ID_2);
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casApi.getUser(eq(USER_2), eq(CUSTOMER_ID_2), eq(PROVIDER_ID), eq(null))).thenReturn(
            new fr.gouv.vitamui.commons.security.client.dto.AuthUserDto(userDto)
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testInternalSubrogationSuperUserDisabled() throws IOException {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USER_2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);

        UserDto userDto = new UserDto();
        userDto.setCustomerId(CUSTOMER_ID_1);
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casApi.getUser(eq(USER_1), eq(CUSTOMER_ID_1), eq(PROVIDER_ID), eq(null))).thenReturn(
            new fr.gouv.vitamui.commons.security.client.dto.AuthUserDto(userDto)
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternal() throws IOException {
        provider.setInternal(false);

        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);

        final Event event = action.doExecute(context);

        assertEquals("stop", event.getId());
    }

    @Test
    public void testExternalDisabled() throws IOException {
        provider.setInternal(false);

        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);

        UserDto userDto = new UserDto();
        userDto.setCustomerId(CUSTOMER_ID_1);
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casApi.getUser(eq(USER_1), eq(CUSTOMER_ID_1), eq(PROVIDER_ID), eq(null))).thenReturn(
            new fr.gouv.vitamui.commons.security.client.dto.AuthUserDto(userDto)
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternalSubrogation() throws IOException {
        provider.setInternal(false);

        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USER_2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);

        final Event event = action.doExecute(context);

        assertEquals("stop", event.getId());
    }

    @Test
    public void testExternalSubrogationSurrogateDisabled() throws IOException {
        provider.setInternal(false);

        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USER_2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);

        UserDto userDto = new UserDto();
        userDto.setCustomerId(CUSTOMER_ID_2);
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casApi.getUser(eq(USER_2), eq(CUSTOMER_ID_2), eq(PROVIDER_ID), eq(null))).thenReturn(
            new fr.gouv.vitamui.commons.security.client.dto.AuthUserDto(userDto)
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternalSubrogationSuperUserDisabled() throws IOException {
        provider.setInternal(false);

        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USER_2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);

        UserDto userDto = new UserDto();
        userDto.setCustomerId(CUSTOMER_ID_1);
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casApi.getUser(eq(USER_1), eq(CUSTOMER_ID_1), eq(PROVIDER_ID), eq(null))).thenReturn(
            new fr.gouv.vitamui.commons.security.client.dto.AuthUserDto(userDto)
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }
}
