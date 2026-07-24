/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import java.net.URI;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.util.StringUtils;

/**
 * Validator for {@code post_logout_redirect_uri} that tolerates extra query parameters appended
 * by clients (angular-oauth2-oidc pattern: {@code https://portal/user?isSubrogation=true&…}).
 *
 * <p>Matching rule: an incoming URI matches a registered URI if {@code scheme + host + port + path}
 * are equal, regardless of query string / fragment. The client is expected to send query params
 * that carry application state (e.g. the subrogation triggers), which the default
 * {@code OidcLogoutAuthenticationValidator} rejects because of its strict {@code Set.contains}.
 *
 * <p>Logs both the incoming value and the registered candidates on mismatch to make the diagnostic
 * obvious in TRACE.
 */
public final class TolerantPostLogoutRedirectUriValidator implements Consumer<OidcLogoutAuthenticationContext> {

    public static final TolerantPostLogoutRedirectUriValidator INSTANCE = new TolerantPostLogoutRedirectUriValidator();

    private static final Logger LOGGER = LoggerFactory.getLogger(TolerantPostLogoutRedirectUriValidator.class);

    @Override
    public void accept(OidcLogoutAuthenticationContext context) {
        OidcLogoutAuthenticationToken auth = context.getAuthentication();
        RegisteredClient client = context.getRegisteredClient();
        String incoming = auth.getPostLogoutRedirectUri();
        if (!StringUtils.hasText(incoming)) {
            return;
        }
        for (String registered : client.getPostLogoutRedirectUris()) {
            if (uriBaseEquals(incoming, registered)) {
                LOGGER.debug(
                    "Post-logout redirect matched (tolerant): incoming={} registered={}",
                    incoming,
                    registered
                );
                return;
            }
        }
        LOGGER.warn(
            "Post-logout redirect refused: incoming={} registered={}",
            incoming,
            client.getPostLogoutRedirectUris()
        );
        OAuth2Error error = new OAuth2Error(
            OAuth2ErrorCodes.INVALID_REQUEST,
            "OpenID Connect 1.0 Logout Request Parameter: post_logout_redirect_uri",
            "https://openid.net/specs/openid-connect-rpinitiated-1_0.html#ValidationAndErrorHandling"
        );
        throw new OAuth2AuthenticationException(error);
    }

    private static boolean uriBaseEquals(String left, String right) {
        try {
            URI l = URI.create(left);
            URI r = URI.create(right);
            return (
                nullSafeEquals(l.getScheme(), r.getScheme()) &&
                nullSafeEquals(l.getHost(), r.getHost()) &&
                l.getPort() == r.getPort() &&
                nullSafeEquals(defaulted(l.getPath()), defaulted(r.getPath()))
            );
        } catch (IllegalArgumentException e) {
            return left.equals(right);
        }
    }

    private static String defaulted(String path) {
        return path == null || path.isBlank() ? "/" : path;
    }

    private static boolean nullSafeEquals(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private TolerantPostLogoutRedirectUriValidator() {}
}
