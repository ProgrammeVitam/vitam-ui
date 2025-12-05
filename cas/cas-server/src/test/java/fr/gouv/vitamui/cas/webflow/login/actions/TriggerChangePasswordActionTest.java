package fr.gouv.vitamui.cas.webflow.login.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.util.Utils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link TriggerChangePasswordAction}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TriggerChangePasswordActionTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class TriggerChangePasswordActionTest extends BaseWebflowActionTest {

    private TriggerChangePasswordAction action;

    @Override
    @Before
    public void setUp() throws FileNotFoundException {
        super.setUp();

        final var tgtId = "TGT-1";

        flowParameters.put("ticketGrantingTicketId", tgtId);

        final var ticketRegistrySupport = mock(TicketRegistrySupport.class);
        action = new TriggerChangePasswordAction(ticketRegistrySupport, mock(Utils.class));

        when(ticketRegistrySupport.getAuthenticatedPrincipalFrom(tgtId)).thenReturn(mock(Principal.class));
    }

    @Test
    public void changePassword() {
        requestParameters.put("doChangePassword", "yes");

        final var event = action.doExecute(context);

        assertEquals("changePassword", event.getId());
    }

    @Test
    public void dontChangePassword() {
        final var event = action.doExecute(context);

        assertEquals("continue", event.getId());
    }
}
