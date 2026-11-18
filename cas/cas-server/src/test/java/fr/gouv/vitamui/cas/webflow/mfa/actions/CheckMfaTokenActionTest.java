package fr.gouv.vitamui.cas.webflow.mfa.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.webflow.mfa.MfaAttempts;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link CheckMfaTokenAction} and {@link ResetMfaAttemptsAction}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CheckMfaTokenActionTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class CheckMfaTokenActionTest extends BaseWebflowActionTest {

    private static final String TOKEN = "000";

    private static final String WRONG_TOKEN = "999";

    private static final int MAX_ATTEMPTS = 3;

    private TicketRegistry ticketRegistry;

    private CheckMfaTokenAction action;

    private ResetMfaAttemptsAction resetAction;

    private CasSimpleMultifactorAuthenticationTicket ticket;

    @Override
    @Before
    public void setUp() throws FileNotFoundException {
        super.setUp();

        ticketRegistry = mock(TicketRegistry.class);
        final var mfaAttempts = new MfaAttempts(MAX_ATTEMPTS, Duration.ofMinutes(15));
        action = new CheckMfaTokenAction(ticketRegistry, mfaAttempts);
        resetAction = new ResetMfaAttemptsAction(mfaAttempts);
        final var authentication = mock(Authentication.class);
        final var principal = principal("user");
        when(authentication.getPrincipal()).thenReturn(principal);
        WebUtils.putAuthentication(authentication, context);
        submitToken(TOKEN);

        ticket = mock(CasSimpleMultifactorAuthenticationTicket.class);
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now().minus(30, ChronoUnit.SECONDS));
        issueTicketTo(principal);
        when(ticketRegistry.getTicket("CASMFA-" + TOKEN, CasSimpleMultifactorAuthenticationTicket.class)).thenReturn(
            ticket
        );
        when(
            ticketRegistry.getTicket("CASMFA-" + WRONG_TOKEN, CasSimpleMultifactorAuthenticationTicket.class)
        ).thenThrow(new InvalidTicketException("CASMFA-" + WRONG_TOKEN));
    }

    @Test
    public void shouldAcceptAValidToken() {
        assertEquals("success", action.doExecute(context).getId());
    }

    @Test
    public void shouldRefuseAnExpiredToken() {
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now().minus(70, ChronoUnit.SECONDS));

        assertEquals("error", action.doExecute(context).getId());
    }

    @Test
    public void shouldRefuseATokenIssuedToAnotherPrincipal() {
        issueTicketTo(principal("attacker"));

        assertEquals("error", action.doExecute(context).getId());
    }

    @Test
    public void shouldRefuseEveryTokenOnceTooManyTokensWereSubmitted() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            submitToken(WRONG_TOKEN);
            assertEquals("error", action.doExecute(context).getId());
        }

        submitToken(TOKEN);
        assertEquals(CheckMfaTokenAction.TRANSITION_ID_TOO_MANY_ATTEMPTS, action.doExecute(context).getId());
    }

    @Test
    public void shouldCountTheTokensThatPassTheCheckUntilTheAuthenticationCompletes() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            submitToken(TOKEN);
            assertEquals("success", action.doExecute(context).getId());
        }

        submitToken(TOKEN);
        assertEquals(CheckMfaTokenAction.TRANSITION_ID_TOO_MANY_ATTEMPTS, action.doExecute(context).getId());
    }

    @Test
    public void shouldAcceptTokensAgainOnceTheAuthenticationCompleted() {
        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
                submitToken(WRONG_TOKEN);
                assertEquals("error", action.doExecute(context).getId());
            }
            submitToken(TOKEN);
            assertEquals("success", action.doExecute(context).getId());
            assertEquals("success", resetAction.doExecute(context).getId());
        }
    }

    private static Principal principal(final String id) {
        final var principal = mock(Principal.class);
        when(principal.getId()).thenReturn(id);
        return principal;
    }

    private void issueTicketTo(final Principal principal) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal);
        when(ticket.getProperties()).thenReturn(properties);
    }

    private void submitToken(final String token) {
        final var credential = mock(CasSimpleMultifactorTokenCredential.class);
        when(credential.getToken()).thenReturn(token);
        when(credential.getId()).thenReturn(token);
        WebUtils.putCredential(context, credential);
    }
}
