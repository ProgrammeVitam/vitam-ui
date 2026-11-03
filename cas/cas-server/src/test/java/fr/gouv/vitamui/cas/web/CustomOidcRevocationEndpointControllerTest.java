package fr.gouv.vitamui.cas.web;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitamui.commons.api.CommonConstants.AUTHTOKEN_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link CustomOidcRevocationEndpointController}.
 */
public class CustomOidcRevocationEndpointControllerTest {

    private static final String AUTH_TOKEN = "TOK-1-authToken";

    private TicketRegistry ticketRegistry;

    private CasApi casApi;

    private CustomOidcRevocationEndpointController controller;

    @Before
    public void setUp() {
        ticketRegistry = mock(TicketRegistry.class);
        final var configurationContext = mock(OidcConfigurationContext.class);
        when(configurationContext.getTicketRegistry()).thenReturn(ticketRegistry);
        casApi = mock(CasApi.class);
        controller = new CustomOidcRevocationEndpointController(
            configurationContext,
            casApi,
            new Utils(null, null, null, null, null)
        );
    }

    @Test
    public void revokingAnAccessTokenRevokesTheIamToken() throws Exception {
        final Map<String, List<Object>> attributes = new HashMap<>();
        attributes.put(AUTHTOKEN_ATTRIBUTE, List.of(AUTH_TOKEN));
        attributes.put(SUPER_USER_ATTRIBUTE, List.of("admin@test.com"));
        attributes.put(SUPER_USER_CUSTOMER_ID_ATTRIBUTE, List.of("adminCustomer"));
        givenAccessToken(attributes);

        final var mv = controller.generateRevocationResponse(AUTH_TOKEN, "client", new MockHttpServletResponse());

        assertEquals(200, mv.getStatus().value());
        verify(casApi).logout(AUTH_TOKEN, "admin@test.com", "adminCustomer");
        verify(ticketRegistry).deleteTicket(AUTH_TOKEN);
    }

    @Test
    public void anAccessTokenWithoutIamTokenIsOnlyRevokedInTheRegistry() throws Exception {
        givenAccessToken(new HashMap<>());

        controller.generateRevocationResponse(AUTH_TOKEN, "client", new MockHttpServletResponse());

        verify(casApi, never()).logout(anyString(), any(), any());
        verify(ticketRegistry).deleteTicket(AUTH_TOKEN);
    }

    private void givenAccessToken(final Map<String, List<Object>> attributes) {
        final var principal = mock(Principal.class);
        when(principal.getAttributes()).thenReturn(attributes);
        final var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        final var accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getId()).thenReturn(AUTH_TOKEN);
        when(accessToken.getAuthentication()).thenReturn(authentication);
        when(ticketRegistry.getTicket(AUTH_TOKEN, OAuth20Token.class)).thenReturn(accessToken);
    }
}
