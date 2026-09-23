package fr.gouv.vitamui.cas.webflow.login.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.delegation.Pac4jClientIdentityProviderDto;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.auth.contract.HrdEntryDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
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
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
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
    private static final String PROVIDER_ID_1 = "provider1";
    private static final String USER_2 = "user2@vitamui.fr";
    private static final String CUSTOMER_ID_2 = "customer2";
    private static final String PROVIDER_ID_2 = "provider2";

    private CasApi casApi;

    private DispatcherAction action;

    @Override
    @Before
    public void setUp() throws FileNotFoundException {
        super.setUp();

        ProvidersService providersService = mock(ProvidersService.class);
        casApi = mock(CasApi.class);

        final SAML2Client client = new SAML2Client();
        IdentityProviderDto providerDto = new IdentityProviderDto();
        providerDto.setId(PROVIDER_ID_1);
        Pac4jClientIdentityProviderDto provider = new Pac4jClientIdentityProviderDto(providerDto, client);
        when(providersService.getProviders()).thenReturn(List.of(provider));

        final Utils utils = new Utils(null, 0, null, null, "");
        action = new DispatcherAction(providersService, casApi, utils, mock(SessionStore.class));
    }

    @Test
    public void testNoIdentityProvider() throws IOException {
        givenLogin(USER_1, CUSTOMER_ID_1);

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(emptyList());

        final Event event = action.doExecute(context);

        assertEquals("badConfiguration", event.getId());
    }

    @Test
    public void testInternalAuthnOK() throws IOException {
        givenLogin(USER_1, CUSTOMER_ID_1);

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, true, UserStatusEnum.ENABLED))
        );

        final Event event = action.doExecute(context);

        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalAuthnDisabled() throws IOException {
        givenLogin(USER_1, CUSTOMER_ID_1);

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, true, UserStatusEnum.BLOCKED))
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testInternalSubrogation() throws IOException {
        givenSubrogation();

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, true, UserStatusEnum.ENABLED))
        );
        when(casApi.resolveHrd(eq(USER_2))).thenReturn(
            List.of(entry(CUSTOMER_ID_2, PROVIDER_ID_2, true, UserStatusEnum.ENABLED))
        );

        final Event event = action.doExecute(context);

        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalSubrogationSurrogateDisabled() throws IOException {
        givenSubrogation();

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, true, UserStatusEnum.ENABLED))
        );
        when(casApi.resolveHrd(eq(USER_2))).thenReturn(
            List.of(entry(CUSTOMER_ID_2, PROVIDER_ID_2, true, UserStatusEnum.BLOCKED))
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testInternalSubrogationSuperUserDisabled() throws IOException {
        givenSubrogation();

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, true, UserStatusEnum.BLOCKED))
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternal() throws IOException {
        givenLogin(USER_1, CUSTOMER_ID_1);

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, false, UserStatusEnum.ENABLED))
        );

        final Event event = action.doExecute(context);

        assertEquals("stop", event.getId());
    }

    @Test
    public void testExternalDisabled() throws IOException {
        givenLogin(USER_1, CUSTOMER_ID_1);

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, false, UserStatusEnum.BLOCKED))
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternalSubrogation() throws IOException {
        givenSubrogation();

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, false, UserStatusEnum.ENABLED))
        );
        when(casApi.resolveHrd(eq(USER_2))).thenReturn(
            List.of(entry(CUSTOMER_ID_2, PROVIDER_ID_2, false, UserStatusEnum.ENABLED))
        );

        final Event event = action.doExecute(context);

        assertEquals("stop", event.getId());
    }

    @Test
    public void testExternalSubrogationSurrogateDisabled() throws IOException {
        givenSubrogation();

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, false, UserStatusEnum.ENABLED))
        );
        when(casApi.resolveHrd(eq(USER_2))).thenReturn(
            List.of(entry(CUSTOMER_ID_2, PROVIDER_ID_2, false, UserStatusEnum.BLOCKED))
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternalSubrogationSuperUserDisabled() throws IOException {
        givenSubrogation();

        when(casApi.resolveHrd(eq(USER_1))).thenReturn(
            List.of(entry(CUSTOMER_ID_1, PROVIDER_ID_1, false, UserStatusEnum.BLOCKED))
        );

        final Event event = action.doExecute(context);

        assertEquals("disabled", event.getId());
    }

    private void givenLogin(String email, String customerId) {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, email);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, customerId);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);
    }

    private void givenSubrogation() {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER_1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USER_2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);
    }

    private static HrdEntryDto entry(String customerId, String providerId, boolean internal, UserStatusEnum status) {
        HrdEntryDto entry = new HrdEntryDto();
        entry.setCustomerId(customerId);
        entry.setIdentityProviderId(providerId);
        entry.setInternal(internal);
        entry.setUserStatus(status == null ? null : status.name());
        return entry;
    }
}
