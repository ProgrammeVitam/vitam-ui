/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Custom {@code AuthenticationProvider} for {@code /connect/logout}. Reproduces the essential
 * validations of Spring's {@code OidcLogoutAuthenticationProvider} (audience, client identity,
 * post_logout_redirect_uri via the registered client) but relaxes two things:
 *
 * <ol>
 *   <li><b>{@code sub} strict check skipped</b>: after a subrogation the current server-side
 *       {@code Authentication} carries the surrogate id whereas the {@code id_token_hint} still
 *       comes from a super-user token cached in another tab. Spring's default provider would reject
 *       this as {@code invalid_token / sub}; we accept it as a legitimate subrogation transition.</li>
 *   <li><b>{@code post_logout_redirect_uri} tolerant match</b>: {@code angular-oauth2-oidc} sometimes
 *       appends extra query params (e.g. {@code ?isSubrogation=true}) to the URI, so we compare on
 *       scheme + host + port + path and ignore query/fragment.</li>
 * </ol>
 *
 * <p>Registered ahead of the default provider through {@code OidcLogoutEndpointConfigurer.authenticationProvider(...)}.
 *
 * <p>Since chantier #5 (Mongo-backed {@code OAuth2AuthorizationService}), authorizations survive SAS
 * restarts — the previous JWT-decode fallback for missing in-memory rows is no longer needed and has
 * been removed. If the authorization cannot be resolved by the id_token_hint, we treat it as an
 * invalid token (which is the correct semantics).
 */
public class SubrogationTolerantOidcLogoutAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        SubrogationTolerantOidcLogoutAuthenticationProvider.class
    );
    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType(OidcIdToken.class.getSimpleName());

    private final RegisteredClientRepository registeredClientRepository;
    // Lazy supplier — SAS wires the shared OAuth2AuthorizationService AFTER our configurer lambda runs,
    // so grabbing it eagerly at construction time yields null. Resolved on the first authenticate().
    private final Supplier<OAuth2AuthorizationService> authorizationServiceSupplier;
    private volatile OAuth2AuthorizationService authorizationService;

    public SubrogationTolerantOidcLogoutAuthenticationProvider(
        RegisteredClientRepository registeredClientRepository,
        Supplier<OAuth2AuthorizationService> authorizationServiceSupplier
    ) {
        this.registeredClientRepository = registeredClientRepository;
        this.authorizationServiceSupplier = authorizationServiceSupplier;
    }

    private OAuth2AuthorizationService resolveAuthorizationService() {
        OAuth2AuthorizationService cached = this.authorizationService;
        if (cached == null) {
            cached = this.authorizationServiceSupplier.get();
            if (cached == null) {
                throw new IllegalStateException(
                    "OAuth2AuthorizationService is not yet available; SAS configurer did not initialise it."
                );
            }
            this.authorizationService = cached;
        }
        return cached;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OidcLogoutAuthenticationToken logout = (OidcLogoutAuthenticationToken) authentication;

        OAuth2Authorization authorization = resolveAuthorizationService().findByToken(
            logout.getIdTokenHint(),
            ID_TOKEN_TOKEN_TYPE
        );
        if (authorization == null) {
            LOGGER.warn("Logout requested with an id_token_hint that has no matching authorization");
            throwError(OAuth2ErrorCodes.INVALID_TOKEN, "id_token_hint");
        }

        OAuth2Authorization.Token<OidcIdToken> authorizedIdToken = authorization.getToken(OidcIdToken.class);
        if (authorizedIdToken.isInvalidated() || authorizedIdToken.isBeforeUse()) {
            throwError(OAuth2ErrorCodes.INVALID_TOKEN, "id_token_hint");
        }
        RegisteredClient registeredClient = this.registeredClientRepository.findById(authorization.getRegisteredClientId());
        OidcIdToken idToken = authorizedIdToken.getToken();

        // Client identity: the audience must include the client that emitted the token.
        List<String> aud = idToken.getAudience();
        if (CollectionUtils.isEmpty(aud) || !aud.contains(registeredClient.getClientId())) {
            throwError(OAuth2ErrorCodes.INVALID_TOKEN, IdTokenClaimNames.AUD);
        }
        if (
            StringUtils.hasText(logout.getClientId()) &&
            !logout.getClientId().equals(registeredClient.getClientId())
        ) {
            throwError(OAuth2ErrorCodes.INVALID_REQUEST, "client_id");
        }

        // Tolerant post_logout_redirect_uri match: ignore query/fragment.
        String incoming = logout.getPostLogoutRedirectUri();
        if (StringUtils.hasText(incoming)) {
            boolean matched = false;
            for (String registered : registeredClient.getPostLogoutRedirectUris()) {
                if (uriBaseEquals(incoming, registered)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                LOGGER.warn(
                    "Post-logout redirect refused: incoming={} registered={}",
                    incoming,
                    registeredClient.getPostLogoutRedirectUris()
                );
                throwError(OAuth2ErrorCodes.INVALID_REQUEST, "post_logout_redirect_uri");
            }
        }

        // Deliberately skip the strict `sub` check the default provider would enforce — subrogation
        // legitimately swaps the current principal (surrogate) away from the id_token subject
        // (super-user), and the JWT itself is authentic and issued to the matching client.
        Authentication currentPrincipal = (Authentication) logout.getPrincipal();

        return new OidcLogoutAuthenticationToken(
            idToken,
            currentPrincipal,
            logout.getSessionId(),
            logout.getClientId(),
            logout.getPostLogoutRedirectUri(),
            logout.getState()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OidcLogoutAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static boolean uriBaseEquals(String left, String right) {
        try {
            java.net.URI l = java.net.URI.create(left);
            java.net.URI r = java.net.URI.create(right);
            return (
                nullSafeEqualsIgnoreCase(l.getScheme(), r.getScheme()) &&
                nullSafeEqualsIgnoreCase(l.getHost(), r.getHost()) &&
                l.getPort() == r.getPort() &&
                nullSafeEqualsIgnoreCase(defaulted(l.getPath()), defaulted(r.getPath()))
            );
        } catch (IllegalArgumentException e) {
            return left.equals(right);
        }
    }

    private static String defaulted(String path) {
        return path == null || path.isBlank() ? "/" : path;
    }

    private static boolean nullSafeEqualsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static void throwError(String errorCode, String parameterName) {
        OAuth2Error error = new OAuth2Error(
            errorCode,
            "OpenID Connect 1.0 Logout Request Parameter: " + parameterName,
            "https://openid.net/specs/openid-connect-rpinitiated-1_0.html#ValidationAndErrorHandling"
        );
        throw new OAuth2AuthenticationException(error);
    }
}
