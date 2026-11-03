package fr.gouv.vitamui.cas.webflow.login.actions;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.ContextConfiguration;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link I18NSendPasswordResetInstructionsAction}.
 */
@ContextConfiguration(classes = I18NSendPasswordResetInstructionsActionTest.class)
public class I18NSendPasswordResetInstructionsActionTest extends BaseWebflowActionTest {

    private static final String EMAIL = "user@test.com";
    private static final String CUSTOMER_ID = "customerId";
    private static final String RESET_TICKET = "TST-1-secretPartOfTheResetTicket";
    private static final String RESET_URL = "https://dev.vitamui.com/cas/login?pswdrst=" + RESET_TICKET;

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();

    private Logger actionLogger;

    private Level previousLevel;

    private I18NSendPasswordResetInstructionsAction action;

    @Before
    @Override
    public void setUp() throws FileNotFoundException {
        super.setUp();
        try {
            givenAnInternalAccount();
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private void givenAnInternalAccount() throws Throwable {
        final var communicationsManager = mock(CommunicationsManager.class);
        when(communicationsManager.isMailSenderDefined()).thenReturn(true);
        final var passwordManagementService = mock(PasswordManagementService.class);
        when(passwordManagementService.findEmail(any())).thenReturn(EMAIL);
        final var identityProviderHelper = mock(IdentityProviderHelper.class);
        when(identityProviderHelper.identifierMatchProviderPattern(any(), anyString(), anyString())).thenReturn(true);
        final var providersService = mock(ProvidersService.class);
        when(providersService.getProviders()).thenReturn(List.of());
        final var messageSource = new StaticMessageSource();
        messageSource.setUseCodeAsDefaultMessage(true);
        final var utils = mock(Utils.class);
        when(utils.sanitizePasswordResetUrl(anyString())).thenCallRealMethod();
        when(utils.htmlEmail(any(), any(), any(), any(), any(), any())).thenReturn(true);

        action = new I18NSendPasswordResetInstructionsAction(
            new CasConfigurationProperties(),
            communicationsManager,
            passwordManagementService,
            mock(TicketRegistry.class),
            mock(TicketFactory.class),
            mock(PrincipalResolver.class),
            mock(PasswordResetUrlBuilder.class),
            mock(MultifactorAuthenticationProviderSelector.class),
            mock(AuthenticationSystemSupport.class),
            mock(ServicesManager.class),
            messageSource,
            providersService,
            identityProviderHelper,
            utils,
            "platform"
        ) {
            @Override
            protected URL buildPasswordResetUrl(final String username, final WebApplicationService service)
                throws Throwable {
                return URI.create(RESET_URL).toURL();
            }
        };

        when(request.getParameter("username")).thenReturn(EMAIL);
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID);

        actionLogger = (Logger) LoggerFactory.getLogger(I18NSendPasswordResetInstructionsAction.class);
        previousLevel = actionLogger.getLevel();
        actionLogger.setLevel(Level.DEBUG);
        logs.start();
        actionLogger.addAppender(logs);
    }

    @After
    public void tearDown() {
        actionLogger.detachAppender(logs);
        actionLogger.setLevel(previousLevel);
    }

    @Test
    public void theResetTicketIsNotWrittenToTheLogs() throws Throwable {
        final var event = action.doExecuteInternal(context);

        assertThat(event.getId()).isEqualTo("success");
        assertThat(logs.list).extracting(ILoggingEvent::getFormattedMessage).noneMatch(m -> m.contains(RESET_TICKET));
        assertThat(logs.list)
            .extracting(ILoggingEvent::getFormattedMessage)
            .anyMatch(m -> m.startsWith("Generated password reset URL"));
    }
}
