package fr.gouv.vitamui.cas.webflow.mfa.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link CheckMfaTokenAction}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CheckMfaTokenActionTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class CheckMfaTokenActionTest extends BaseWebflowActionTest {

    private static final String TOKEN = "000";

    private static final int MAX_FAILED_ATTEMPTS = 3;

    private TicketRegistry ticketRegistry;

    private CheckMfaTokenAction action;

    private CasSimpleMultifactorAuthenticationTicket ticket;

    @Override
    @Before
    public void setUp() throws FileNotFoundException {
        super.setUp();

        ticketRegistry = mock(TicketRegistry.class);
        action = new CheckMfaTokenAction(ticketRegistry, MAX_FAILED_ATTEMPTS, Duration.ofMinutes(15));
        final var principal = mock(Principal.class);
        when(principal.getId()).thenReturn("user");
        final var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        WebUtils.putAuthentication(authentication, context);
        submitToken(TOKEN);

        ticket = mock(CasSimpleMultifactorAuthenticationTicket.class);
        when(ticketRegistry.getTicket("CASMFA-" + TOKEN, CasSimpleMultifactorAuthenticationTicket.class)).thenReturn(
            ticket
        );
    }

    @Test
    public void tokenNotExpired() {
        final var creationDate = ZonedDateTime.now().minus(30, ChronoUnit.SECONDS);
        when(ticket.getCreationTime()).thenReturn(creationDate);

        final var event = action.doExecute(context);

        assertEquals("success", event.getId());
    }

    @Test
    public void tokenExpired() {
        final var creationDate = ZonedDateTime.now().minus(70, ChronoUnit.SECONDS);
        when(ticket.getCreationTime()).thenReturn(creationDate);

        final var event = action.doExecute(context);

        assertEquals("error", event.getId());
    }

    @Test
    public void validTokenIsRefusedOnceTooManyWrongTokensWereSubmitted() {
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now().minus(30, ChronoUnit.SECONDS));
        when(ticketRegistry.getTicket("CASMFA-999", CasSimpleMultifactorAuthenticationTicket.class)).thenThrow(
            new InvalidTicketException("CASMFA-999")
        );

        for (int i = 0; i < MAX_FAILED_ATTEMPTS; i++) {
            submitToken("999");
            assertEquals("error", action.doExecute(context).getId());
        }

        submitToken(TOKEN);
        assertEquals("error", action.doExecute(context).getId());
    }

    @Test
    public void validTokenResetsTheWrongTokenCount() {
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now().minus(30, ChronoUnit.SECONDS));

        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < MAX_FAILED_ATTEMPTS - 1; i++) {
                submitToken("999");
                assertEquals("error", action.doExecute(context).getId());
            }
            submitToken(TOKEN);
            assertEquals("success", action.doExecute(context).getId());
        }
    }

    private void submitToken(final String token) {
        final var credential = mock(CasSimpleMultifactorTokenCredential.class);
        when(credential.getToken()).thenReturn(token);
        when(credential.getId()).thenReturn(token);
        WebUtils.putCredential(context, credential);
    }
}
