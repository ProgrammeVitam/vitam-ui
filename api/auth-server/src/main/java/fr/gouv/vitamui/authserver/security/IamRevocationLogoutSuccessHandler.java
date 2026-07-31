/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcLogoutAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Wraps the default {@link OidcLogoutAuthenticationSuccessHandler} to propagate the end-of-session
 * across the vitam-ui apps: after SAS accepted the OIDC logout, we ask IAM to delete every
 * opaque {@code TOK-<UUID>} pointing to the user (identified by the {@code sub} of the id_token).
 * Resource servers on the other tabs will then get 401 at the next call and force a fresh SSO.
 *
 * <p>The IAM call is best-effort — failing to reach IAM must not block the browser redirect back to
 * the client's {@code post_logout_redirect_uri} (the user has already, from their perspective, logged
 * out). We log and continue.
 */
public class IamRevocationLogoutSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IamRevocationLogoutSuccessHandler.class);

    private final IamClient iamClient;
    private final AuthenticationSuccessHandler delegate;

    public IamRevocationLogoutSuccessHandler(IamClient iamClient) {
        this.iamClient = iamClient;
        this.delegate = new OidcLogoutAuthenticationSuccessHandler();
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        if (authentication instanceof OidcLogoutAuthenticationToken logout) {
            String userId = logout.getIdToken() != null ? logout.getIdToken().getSubject() : null;
            if (userId != null && !userId.isBlank()) {
                try {
                    iamClient.invalidateTokensOfUser(userId);
                    LOGGER.info("Invalidated IAM tokens for userId={} on OIDC logout", userId);
                } catch (Exception e) {
                    LOGGER.warn("Failed to invalidate IAM tokens for userId={} — logout continues: {}", userId, e.toString());
                }
            } else {
                LOGGER.warn("OIDC logout without a resolvable subject — skipping IAM token invalidation");
            }
        }
        delegate.onAuthenticationSuccess(request, response, authentication);
    }
}
