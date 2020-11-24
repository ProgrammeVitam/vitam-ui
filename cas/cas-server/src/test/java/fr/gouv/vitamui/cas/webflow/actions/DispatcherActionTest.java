package fr.gouv.vitamui.cas.webflow.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.execution.Event;

import fr.gouv.vitamui.cas.provider.SamlIdentityProviderDto;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;

/**
 * Tests {@link DispatcherAction}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class DispatcherActionTest extends BaseWebflowActionTest {

    private static final String USERNAME = "julien@vitamui.com";

    private static final String SURROGATE = "pierre@vitamui.com";

    private static final String PASSWORD = "password";

    private ProvidersService providersService;

    private IdentityProviderHelper identityProviderHelper;

    private CasExternalRestClient casExternalRestClient;

    private DispatcherAction action;

    private SamlIdentityProviderDto provider;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        providersService = mock(ProvidersService.class);
        identityProviderHelper = mock(IdentityProviderHelper.class);
        casExternalRestClient = mock(CasExternalRestClient.class);

        final Utils utils = new Utils(null, 0, null, null);
        action = new DispatcherAction(providersService, identityProviderHelper, casExternalRestClient, ",", utils, mock(SessionStore.class));

        final SAML2Client client = new SAML2Client();
        provider = new SamlIdentityProviderDto(new IdentityProviderDto(), client);
        provider.setInternal(true);
        when(identityProviderHelper.findByUserIdentifier(any(LinkedList.class), eq(USERNAME)))
                .thenReturn(Optional.of(provider));
    }

    @Test
    public void testNoIdP() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(USERNAME, PASSWORD));
        when(identityProviderHelper.findByUserIdentifier(any(LinkedList.class), eq(USERNAME)))
            .thenReturn(Optional.empty());

        final Event event = action.doExecute(context);

        assertEquals(USERNAME, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("badConfiguration", event.getId());
    }

    @Test
    public void testInternal() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(USERNAME, PASSWORD));

        final Event event = action.doExecute(context);

        assertEquals(USERNAME, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalSuccess_when_spaceBeforeAndAfterUsername() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(" " + USERNAME + "  ", PASSWORD));

        final Event event = action.doExecute(context);
        verify((casExternalRestClient), times(1))
                .getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME), any(Optional.class));

        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalDisabled() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(USERNAME, PASSWORD));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME), eq(Optional.empty()))).thenThrow(InvalidFormatException.class);

        final Event event = action.doExecute(context);

        assertEquals(USERNAME, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("disabled", event.getId());
    }

    @Test
    public void testInternalSubrogation() throws IOException {
        flowParameters.put("credential",
                new UsernamePasswordCredential("," + USERNAME, PASSWORD));

        final Event event = action.doExecute(context);

        assertEquals(USERNAME, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalPrefilledSubrogation() throws IOException {
        flowParameters.put("credential",
                new UsernamePasswordCredential(SURROGATE + "," + USERNAME, PASSWORD));

        final Event event = action.doExecute(context);

        assertEquals(SURROGATE + "," + USERNAME,
                ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("success", event.getId());
    }

    @Test
    public void testInternalPrefilledSubrogationSurrogateDisabled() throws IOException {
        flowParameters.put("credential",
            new UsernamePasswordCredential(SURROGATE + "," + USERNAME, PASSWORD));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(SURROGATE), eq(Optional.empty()))).thenThrow(InvalidFormatException.class);

        final Event event = action.doExecute(context);

        assertEquals(SURROGATE + "," + USERNAME,
            ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("disabled", event.getId());
    }

    @Test
    public void testInternalPrefilledSubrogationSuperUserDisabled() throws IOException {
        flowParameters.put("credential",
            new UsernamePasswordCredential(SURROGATE + "," + USERNAME, PASSWORD));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME), eq(Optional.empty()))).thenThrow(InvalidFormatException.class);

        final Event event = action.doExecute(context);

        assertEquals(SURROGATE + "," + USERNAME,
            ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternal() throws IOException {
        provider.setInternal(false);

        flowParameters.put("credential", new UsernamePasswordCredential(USERNAME, PASSWORD));

        final Event event = action.doExecute(context);

        assertEquals(USERNAME, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("stop", event.getId());
    }

    @Test
    public void testExternalDisabled() throws IOException {
        provider.setInternal(false);

        flowParameters.put("credential", new UsernamePasswordCredential(USERNAME, PASSWORD));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME), eq(Optional.empty()))).thenThrow(InvalidFormatException.class);

        final Event event = action.doExecute(context);

        assertEquals(USERNAME, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternalSubrogation() throws IOException {
        provider.setInternal(false);

        flowParameters.put("credential",
                new UsernamePasswordCredential("," + USERNAME, PASSWORD));

        final Event event = action.doExecute(context);

        assertEquals(USERNAME, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("stop", event.getId());
    }

    @Test
    public void testExternalPrefilledSubrogation() throws IOException {
        provider.setInternal(false);

        flowParameters.put("credential",
                new UsernamePasswordCredential(SURROGATE + "," + USERNAME, PASSWORD));

        final Event event = action.doExecute(context);

        assertEquals(SURROGATE + "," + USERNAME,
                ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("stop", event.getId());
    }

    @Test
    public void testExternalPrefilledSubrogationSurrogateDisabled() throws IOException {
        provider.setInternal(false);

        flowParameters.put("credential", new UsernamePasswordCredential(SURROGATE + "," + USERNAME, PASSWORD));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(SURROGATE), eq(Optional.empty()))).thenThrow(InvalidFormatException.class);

        final Event event = action.doExecute(context);

        assertEquals(SURROGATE + "," + USERNAME,
            ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("disabled", event.getId());
    }

    @Test
    public void testExternalPrefilledSubrogationSuperUserDisabled() throws IOException {
        provider.setInternal(false);

        flowParameters.put("credential", new UsernamePasswordCredential(SURROGATE + "," + USERNAME, PASSWORD));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME), eq(Optional.empty()))).thenThrow(InvalidFormatException.class);

        final Event event = action.doExecute(context);

        assertEquals(SURROGATE + "," + USERNAME,
            ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals("disabled", event.getId());
    }
}
