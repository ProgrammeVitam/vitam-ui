package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.cas.logout.TerminateApiSessionAction;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the customized {@link TerminateApiSessionAction}.
 */
@ContextConfiguration(classes = TerminateApiSessionActionTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class TerminateApiSessionActionTest extends BaseWebflowActionTest {

    private static final String LOGOUT_URL = "http://dev.vitamui.com:8080/cas/app1/callback";

    @Test
    public void testPerformGeneralLogout() throws Throwable {
        // Mocker ServicesManager
        final ServicesManager servicesManager = mock(ServicesManager.class);
        RegexRegisteredService registeredService = new RegexRegisteredService();
        registeredService.setLogoutUrl(LOGOUT_URL);
        when(servicesManager.getAllServices()).thenReturn(List.of(registeredService));

        // Mocker LogoutManager
        final LogoutManager logoutManager = mock(LogoutManager.class);
        when(logoutManager.performLogout(any())).thenReturn(Collections.emptyList());

        // Mocker les autres dépendances
        final CentralAuthenticationService cas = mock(CentralAuthenticationService.class);
        final CasCookieBuilder ticketCookie = mock(CasCookieBuilder.class);
        final CasCookieBuilder warnCookie = mock(CasCookieBuilder.class);
        final CasConfigurationProperties casProperties = mock(CasConfigurationProperties.class);
        when(casProperties.getLogout()).thenReturn(new LogoutProperties());
        final Action frontChannelLogoutAction = mock(Action.class);
        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        final Utils utils = mock(Utils.class);
        final CasApi casApi = mock(CasApi.class);

        // Créer l'action avec les mocks
        final TestableTerminateApiSessionAction action = new TestableTerminateApiSessionAction(
            cas,
            ticketCookie,
            warnCookie,
            casProperties.getLogout(),
            logoutManager,
            mock(ConfigurableApplicationContext.class),
            utils,
            casApi,
            servicesManager,
            casProperties,
            frontChannelLogoutAction,
            ticketRegistry
        );

        // Mocker le TGT factice et le ST pour CAS 7
        final TicketGrantingTicket fakeTgt = mock(TicketGrantingTicket.class);
        final Authentication fakeAuth = mock(Authentication.class);
        when(fakeTgt.getAuthentication()).thenReturn(fakeAuth);
        when(fakeAuth.getPrincipal()).thenReturn(() -> "tgtId");
        when(cas.createTicketGrantingTicket(any())).thenReturn(fakeTgt);
        when(cas.grantServiceTicket(anyString(), any(WebApplicationService.class), any())).thenReturn(
            mock(ServiceTicket.class)
        );

        List<SingleLogoutRequestContext> result = action.performGeneralLogoutPublic("tgtId");
        Assert.assertNotNull(result);
        verify(logoutManager).performLogout(any());

        // Vérification simple
        Assert.assertNotNull(result);
        verify(logoutManager, times(1)).performLogout(any());
    }

    @Test
    public void testTheIamTokenIsRevokedWithTheSessionTgt() {
        final Fixture f = new Fixture();
        final TicketGrantingTicket tgt = tgtCarrying("TOK-123", "super@user.fr", "customer1", f);

        f.action.revokeIamSessionPublic("TGT-1", tgt);

        verify(f.casApi).logout("TOK-123", "super@user.fr", "customer1");
    }

    @Test
    public void testNothingIsRevokedWhenTheTgtIsGone() {
        final Fixture f = new Fixture();

        f.action.revokeIamSessionPublic("TGT-1", null);

        verify(f.casApi, times(0)).logout(any(), any(), any());
    }

    @Test
    public void testASessionWithoutIamTokenDoesNotBreakTheLogout() {
        final Fixture f = new Fixture();
        final TicketGrantingTicket tgt = tgtCarrying(null, null, null, f);

        f.action.revokeIamSessionPublic("TGT-1", tgt);

        verify(f.casApi, times(0)).logout(any(), any(), any());
    }

    private TicketGrantingTicket tgtCarrying(
        final String authToken,
        final String superUser,
        final String superUserCustomerId,
        final Fixture f
    ) {
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        final Authentication authentication = mock(Authentication.class);
        when(tgt.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(() -> "userId");
        when(f.utils.getAttributeValue(any(), org.mockito.ArgumentMatchers.eq(CommonConstants.AUTHTOKEN_ATTRIBUTE))).thenReturn(authToken);
        when(f.utils.getAttributeValue(any(), org.mockito.ArgumentMatchers.eq(CommonConstants.SUPER_USER_ATTRIBUTE))).thenReturn(superUser);
        when(
            f.utils.getAttributeValue(any(), org.mockito.ArgumentMatchers.eq(CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE))
        ).thenReturn(superUserCustomerId);
        return tgt;
    }

    private static class Fixture {

        final Utils utils = mock(Utils.class);
        final CasApi casApi = mock(CasApi.class);
        final TestableTerminateApiSessionAction action;

        Fixture() {
            final CasConfigurationProperties casProperties = mock(CasConfigurationProperties.class);
            when(casProperties.getLogout()).thenReturn(new LogoutProperties());
            action = new TestableTerminateApiSessionAction(
                mock(CentralAuthenticationService.class),
                mock(CasCookieBuilder.class),
                mock(CasCookieBuilder.class),
                casProperties.getLogout(),
                mock(LogoutManager.class),
                mock(ConfigurableApplicationContext.class),
                utils,
                casApi,
                mock(ServicesManager.class),
                casProperties,
                mock(Action.class),
                mock(TicketRegistry.class)
            );
        }
    }

    private static class TestableTerminateApiSessionAction extends TerminateApiSessionAction {

        public void revokeIamSessionPublic(final String tgtId, final TicketGrantingTicket ticket) {
            revokeIamSession(tgtId, ticket);
        }


        public TestableTerminateApiSessionAction(
            CentralAuthenticationService cas,
            CasCookieBuilder ticketCookie,
            CasCookieBuilder warnCookie,
            LogoutProperties logoutProps,
            LogoutManager logoutManager,
            ConfigurableApplicationContext ctx,
            Utils utils,
            CasApi casApi,
            ServicesManager servicesManager,
            CasConfigurationProperties casProperties,
            Action frontChannelLogoutAction,
            TicketRegistry ticketRegistry
        ) {
            super(
                cas,
                ticketCookie,
                warnCookie,
                logoutProps,
                logoutManager,
                ctx,
                utils,
                casApi,
                servicesManager,
                casProperties,
                frontChannelLogoutAction,
                ticketRegistry,
                null
            );
        }

        // Expose protected method as public for testing
        public List<SingleLogoutRequestContext> performGeneralLogoutPublic(String tgtId) {
            return performGeneralLogout(tgtId);
        }
    }
}
