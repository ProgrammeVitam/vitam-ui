package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.val;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link CustomDelegatedClientAuthenticationAction}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class CustomDelegatedClientAuthenticationActionTest extends BaseWebflowActionTest {

    private static final String EMAIL1 = "julien@vitamui.com";

    private static final String EMAIL2 = "pierre@vitamui.com";

    private CustomDelegatedClientAuthenticationAction action;

    @Override
    @Before
    public void setUp() throws FileNotFoundException, InvalidParseOperationException {
        super.setUp();

        val configContext = mock(DelegatedClientAuthenticationConfigurationContext.class);
        when(configContext.getDelegatedClientIdentityProvidersProducer()).thenReturn(mock(DelegatedClientIdentityProviderConfigurationProducer.class));
        when(configContext.getDelegatedClientNameExtractor()).thenReturn(mock(DelegatedClientNameExtractor.class));
        action = new CustomDelegatedClientAuthenticationAction(configContext, mock(DelegatedClientAuthenticationWebflowManager.class),
            mock(DelegatedClientAuthenticationFailureEvaluator.class), mock(IdentityProviderHelper.class),
            mock(ProvidersService.class), mock(Utils.class), mock(TicketRegistry.class), "");
    }

    @Test
    public void testUsernameNoSubrogation() {
        requestParameters.put("username", EMAIL1);

        action.doExecute(context);

        assertEquals(EMAIL1, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertNull(flowParameters.get("surrogate"));
        assertNull(flowParameters.get("superUser"));
    }

    @Test
    public void testUsernameRequestedSubrogation() {
        requestParameters.put("username", "," + EMAIL2);

        action.doExecute(context);

        assertEquals(EMAIL2, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertNull(flowParameters.get("surrogate"));
        assertNull(flowParameters.get("superUser"));
    }

    @Test
    public void testUsernameWithSubrogation() {
        requestParameters.put("username", EMAIL1 + "," + EMAIL2);

        action.doExecute(context);

        assertEquals(EMAIL1 + "," + EMAIL2,
                ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals(EMAIL1, flowParameters.get("surrogate"));
        assertEquals(EMAIL2, flowParameters.get("superUser"));
    }

    @Test
    public void testNoUsername() {
        action.doExecute(context);

        assertNull(flowParameters.get("credential"));
        assertNull(flowParameters.get("surrogate"));
        assertNull(flowParameters.get("superUser"));
    }
}
