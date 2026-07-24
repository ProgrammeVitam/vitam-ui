/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import java.security.Principal;
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
 * Custom {@code AuthenticationProvider} for {@code /connect/logout}. Copies the essential validations
 * of Spring's {@code OidcLogoutAuthenticationProvider} (audience, post_logout_redirect_uri via the
 * registered client) but <b>skips the {@code sub} mismatch check</b>.
 *
 * <p>Rationale: when a user has been subrogated, the current server-side {@code Authentication} carries
 * the surrogate id whereas the {@code id_token_hint} still comes from a super-user token cached in some
 * other tab (portal, ingest, …). Spring's default provider rejects that as {@code invalid_token / sub}
 * and lands on a whitelabel 400. Here we accept it as long as the token itself is authentic and the
 * client identity + post-logout URI are consistent — the surrogate is a legitimate outcome of the
 * subrogation flow initiated from the auth-server.
 *
 * <p>Ordered before the default provider through {@code OidcLogoutEndpointConfigurer.authenticationProvider(...)},
 * so successful validation returns immediately and the default provider is not consulted.
 */
public class SubrogationTolerantOidcLogoutAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        SubrogationTolerantOidcLogoutAuthenticationProvider.class
    );
    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType(OidcIdToken.class.getSimpleName());

    private final RegisteredClientRepository registeredClientRepository;
    // Lazy supplier — SAS sets the shared OAuth2AuthorizationService AFTER our configurer lambda runs,
    // so grabbing it eagerly at construction time yields null. We resolve it on the first authenticate().
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
            // Cannot validate → let the default provider try (it will fail identically but with its standard
            // error path). Better than short-circuiting the chain with an incomplete answer.
            return null;
        }

        // Delegate to the default provider when the current principal matches the id_token subject —
        // that's the vast majority of logouts and we want its full behaviour (session termination,
        // OAuth2Authorization invalidation, back-channel notification, etc.).
        Authentication currentForDelegateCheck = (Authentication) logout.getPrincipal();
        Authentication authorizedForDelegateCheck = authorization.getAttribute(Principal.class.getName());
        if (
            currentForDelegateCheck != null &&
            authorizedForDelegateCheck != null &&
            currentForDelegateCheck.getName().equals(authorizedForDelegateCheck.getName())
        ) {
            LOGGER.debug("Delegating to default provider: sub matches current principal");
            return null;
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

        // post_logout_redirect_uri: reuse the tolerant validator we already registered elsewhere.
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

        // Sub mismatch confirmed → this is a subrogation transition. Accept and complete auth here.
        Authentication currentPrincipal = (Authentication) logout.getPrincipal();
        LOGGER.info(
            "Accepting subrogation-tolerant logout: current={} authorized={}",
            currentPrincipal != null ? currentPrincipal.getName() : "null",
            authorization.getAttribute(Principal.class.getName()) != null
                ? ((Authentication) authorization.getAttribute(Principal.class.getName())).getName()
                : "null"
        );

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
