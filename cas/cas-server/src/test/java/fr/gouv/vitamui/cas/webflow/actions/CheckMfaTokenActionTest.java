package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import lombok.val;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link CheckMfaTokenAction}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class CheckMfaTokenActionTest extends BaseWebflowActionTest {

    private static final String TOKEN = "000";

    private TicketRegistry ticketRegistry;

    private CheckMfaTokenAction action;

    private CasSimpleMultifactorAuthenticationTicket ticket;

    @Override
    @Before
    public void setUp() throws FileNotFoundException, InvalidParseOperationException {
        super.setUp();

        ticketRegistry = mock(TicketRegistry.class);
        action = new CheckMfaTokenAction(ticketRegistry);

        val credential = mock(CasSimpleMultifactorTokenCredential.class);
        when(credential.getToken()).thenReturn(TOKEN);
        when(credential.getId()).thenReturn(TOKEN);
        flowParameters.put("credential", credential);

        ticket = mock(CasSimpleMultifactorAuthenticationTicket.class);
        when(ticketRegistry.getTicket("CASMFA-" + TOKEN, CasSimpleMultifactorAuthenticationTicket.class)).thenReturn(
            ticket
        );
    }

    @Test
    public void tokenNotExpired() {
        val creationDate = ZonedDateTime.now().minus(30, ChronoUnit.SECONDS);
        when(ticket.getCreationTime()).thenReturn(creationDate);

        val event = action.doExecute(context);

        assertEquals("success", event.getId());
    }

    @Test
    public void tokenExpired() {
        val creationDate = ZonedDateTime.now().minus(70, ChronoUnit.SECONDS);
        when(ticket.getCreationTime()).thenReturn(creationDate);

        val event = action.doExecute(context);

        assertEquals("error", event.getId());
    }
}
