package fr.gouv.vitamui.cas.webflow.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;

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
    public void setUp() {
        super.setUp();

        action = new CustomDelegatedClientAuthenticationAction(mock(CasDelegatingWebflowEventResolver.class),
                            mock(CasWebflowEventResolver.class), mock(AdaptiveAuthenticationPolicy.class),
                            mock(Clients.class), mock(ServicesManager.class), mock(AuditableExecution.class),
                            mock(DelegatedClientWebflowManager.class), mock(AuthenticationSystemSupport.class),
                            mock(CasConfigurationProperties.class), mock(AuthenticationServiceSelectionPlan.class),
                            mock(CentralAuthenticationService.class), mock(SingleSignOnParticipationStrategy.class),
                            mock(SessionStore.class), new ArrayList<>(), mock(IdentityProviderHelper.class),
                            mock(ProvidersService.class), mock(Utils.class), mock(TicketRegistry.class), "", ",");
    }

    @Test
    public void testUsernameNoSubrogation() {
        requestParameters.put("username", EMAIL1);

        action.doExecute(context);

        assertEquals(EMAIL1, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals(null, flowParameters.get("surrogate"));
        assertEquals(null, flowParameters.get("superUser"));
    }

    @Test
    public void testUsernameRequestedSubrogation() {
        requestParameters.put("username", "," + EMAIL2);

        action.doExecute(context);

        assertEquals(EMAIL2, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals(null, flowParameters.get("surrogate"));
        assertEquals(null, flowParameters.get("superUser"));
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

        assertEquals(null, flowParameters.get("credential"));
        assertEquals(null, flowParameters.get("surrogate"));
        assertEquals(null, flowParameters.get("superUser"));
    }
}
