/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitamui.cas.web;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fr.gouv.vitamui.commons.api.CommonConstants.AUTHTOKEN_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link CustomOidcRevocationEndpointController}.
 */
public class CustomOidcRevocationEndpointControllerTest {

    private static final String AUTH_TOKEN = "TOK-1-authToken";

    private static final String REFRESH_TOKEN = "RT-1-refreshToken";

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
    public void shouldRevokeTheIamTokenAfterTheAccessToken() throws Exception {
        givenToken(AUTH_TOKEN, mock(OAuth20AccessToken.class), iamAttributes());

        final var mv = controller.generateRevocationResponse(AUTH_TOKEN, "client", new MockHttpServletResponse());

        assertEquals(200, mv.getStatus().value());
        final var order = inOrder(ticketRegistry, casApi);
        order.verify(ticketRegistry).deleteTicket(AUTH_TOKEN);
        order.verify(casApi).logout(AUTH_TOKEN, "admin@test.com", "adminCustomer");
    }

    @Test
    public void shouldRevokeTheRefreshTokenItsAccessTokensAndTheIamToken() throws Exception {
        final var refreshToken = givenToken(REFRESH_TOKEN, mock(OAuth20RefreshToken.class), iamAttributes());
        when(refreshToken.getAccessTokens()).thenReturn(new HashSet<>(Set.of(AUTH_TOKEN)));

        final var mv = controller.generateRevocationResponse(REFRESH_TOKEN, "client", new MockHttpServletResponse());

        assertEquals(200, mv.getStatus().value());
        verify(ticketRegistry).deleteTicket(AUTH_TOKEN);
        verify(ticketRegistry).deleteTicket(REFRESH_TOKEN);
        verify(casApi).logout(AUTH_TOKEN, "admin@test.com", "adminCustomer");
    }

    @Test
    public void shouldRevokeTheAccessTokenEvenWhenIamFails() throws Exception {
        givenToken(AUTH_TOKEN, mock(OAuth20AccessToken.class), iamAttributes());
        doThrow(HttpServerErrorException.create(HttpStatus.BAD_GATEWAY, "", null, null, null))
            .when(casApi)
            .logout(anyString(), any(), any());

        final var mv = controller.generateRevocationResponse(AUTH_TOKEN, "client", new MockHttpServletResponse());

        assertEquals(200, mv.getStatus().value());
        verify(ticketRegistry).deleteTicket(AUTH_TOKEN);
    }

    @Test
    public void shouldOnlyRevokeInTheRegistryAnAccessTokenWithoutIamToken() throws Exception {
        givenToken(AUTH_TOKEN, mock(OAuth20AccessToken.class), new HashMap<>());

        controller.generateRevocationResponse(AUTH_TOKEN, "client", new MockHttpServletResponse());

        verify(casApi, never()).logout(anyString(), any(), any());
        verify(ticketRegistry).deleteTicket(AUTH_TOKEN);
    }

    private static Map<String, List<Object>> iamAttributes() {
        final Map<String, List<Object>> attributes = new HashMap<>();
        attributes.put(AUTHTOKEN_ATTRIBUTE, List.of(AUTH_TOKEN));
        attributes.put(SUPER_USER_ATTRIBUTE, List.of("admin@test.com"));
        attributes.put(SUPER_USER_CUSTOMER_ID_ATTRIBUTE, List.of("adminCustomer"));
        return attributes;
    }

    private <T extends OAuth20Token> T givenToken(
        final String id,
        final T token,
        final Map<String, List<Object>> attributes
    ) {
        final var principal = mock(Principal.class);
        when(principal.getAttributes()).thenReturn(attributes);
        final var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(token.getId()).thenReturn(id);
        when(token.getAuthentication()).thenReturn(authentication);
        when(ticketRegistry.getTicket(id, OAuth20Token.class)).thenReturn(token);
        return token;
    }
}
