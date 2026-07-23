/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * Recognises a {@code POST /oauth2/revoke} carrying only {@code client_id} (no client_secret, no code_verifier),
 * as expected from a public OAuth2 client (PKCE / {@link ClientAuthenticationMethod#NONE}), and returns an
 * {@link OAuth2ClientAuthenticationToken} that {@link PublicClientRevocationAuthenticationProvider} completes.
 *
 * <p>Rationale: RFC 7009 does not mandate client authentication for public clients, but SAS enforces it out of
 * the box. This converter opens the door specifically for the {@code /oauth2/revoke} endpoint of a public client.
 * Phase 2 hardening: reconcile with the logout flow so revocation happens through a signed inter-service call
 * from the portal rather than the browser.
 */
public class PublicClientRevocationAuthenticationConverter implements AuthenticationConverter {

    private static final String REVOKE_URI = "/oauth2/revoke";

    private final RegisteredClientRepository registeredClientRepository;

    public PublicClientRevocationAuthenticationConverter(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public @Nullable Authentication convert(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }
        if (!REVOKE_URI.equals(request.getRequestURI())) {
            return null;
        }
        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if (!StringUtils.hasText(clientId)) {
            return null;
        }
        if (StringUtils.hasText(request.getParameter(OAuth2ParameterNames.CLIENT_SECRET))) {
            return null;
        }

        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            return null;
        }
        if (!registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)) {
            return null;
        }

        return new OAuth2ClientAuthenticationToken(
            clientId,
            ClientAuthenticationMethod.NONE,
            null,
            Map.of()
        );
    }
}
