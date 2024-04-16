package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.provider.Pac4jClientIdentityProviderDto;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.execution.Event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link DispatcherAction}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class DispatcherActionTest extends BaseWebflowActionTest {

    private static final String USER_1 = "user1@vitamui.com";
    private static final String CUSTOMER_ID_1 = "customer1";
    private static final String USER_2 = "user2@vitamui.fr";
    private static final String CUSTOMER_ID_2 = "customer2";

    private static final String PASSWORD = "password";

    private IdentityProviderHelper identityProviderHelper;

    private CasExternalRestClient casExternalRestClient;

    private DispatcherAction action;

    private Pac4jClientIdentityProviderDto provider;

    @Override
    @Before
    public void setUp() throws FileNotFoundException, InvalidParseOperationException {
        super.setUp();

        ProvidersService providersService = mock(ProvidersService.class);
        identityProviderHelper = mock(IdentityProviderHelper.class);
        casExternalRestClient = mock(CasExternalRestClient.class);

        final Utils utils = new Utils(null, 0, null, null, "");
        action = new DispatcherAction(providersService, identityProviderHelper, casExternalRestClient, utils,
            mock(SessionStore.class));

        final SAML2Client client = new SAML2Client();
        provider = new Pac4jClientIdentityProviderDto(new IdentityProviderDto(), client);
        provider.setInternal(true);
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(anyList(), eq(USER_1), eq(CUSTOMER_ID_1)))
            .thenReturn(Optional.of(provider));
    }

    @Test
    public void testNoIdentityProvider() throws IOException {

        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);

        when(identityProviderHelper.findByUserIdentifierAndCustomerId(anyList(), eq(USER_1), eq(CUSTOMER_ID_1)))
            .thenReturn(Optional.empty());

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
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casExternalRestClient.getUserByEmailAndCustomerId(any(ExternalHttpContext.class), eq(USER_1), eq(CUSTOMER_ID_1),
            eq(Optional.empty())))
            .thenReturn(userDto);

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
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
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casExternalRestClient.getUserByEmailAndCustomerId(any(ExternalHttpContext.class), eq(USER_2), eq(CUSTOMER_ID_2),
            eq(Optional.empty()))).thenReturn(userDto);

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
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casExternalRestClient.getUserByEmailAndCustomerId(any(ExternalHttpContext.class), eq(USER_1), eq(CUSTOMER_ID_1),
            eq(Optional.empty()))).thenReturn(userDto);

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
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casExternalRestClient.getUserByEmailAndCustomerId(any(ExternalHttpContext.class), eq(USER_1), eq(CUSTOMER_ID_1),
            eq(Optional.empty()))).thenReturn(userDto);

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
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casExternalRestClient.getUserByEmailAndCustomerId(any(ExternalHttpContext.class), eq(USER_2), eq(CUSTOMER_ID_2),
            eq(Optional.empty()))).thenReturn(userDto);

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
        userDto.setStatus(UserStatusEnum.BLOCKED);
        when(casExternalRestClient.getUserByEmailAndCustomerId(any(ExternalHttpContext.class), eq(USER_1), eq(CUSTOMER_ID_1),
            eq(Optional.empty()))).thenReturn(userDto);

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }
}
