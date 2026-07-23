/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import fr.gouv.vitamui.authserver.config.AuthServerProperties;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;

/**
 * {@link OAuth2TokenGenerator} that produces an opaque access token in the vitam-ui {@code TOK-<UUID>} format,
 * persisted in the shared IAM {@code tokens} collection via {@link IamClient}.
 *
 * <p>The resulting {@link OAuth2AccessToken} value is directly usable as a Bearer against every vitam-ui Resource
 * Server without any modification (the existing {@code iam-security} filter resolves it through {@code /users/me}).
 */
/**
 * Not a {@code @Component} on purpose: SAS resolves its {@link OAuth2TokenGenerator} bean through
 * {@code getBeanProvider(OAuth2TokenGenerator.class).getIfUnique()} which returns {@code null} when
 * multiple beans of the type exist. This generator lives only inside the composite built in
 * {@link fr.gouv.vitamui.authserver.config.AuthorizationServerConfig#tokenGenerator}.
 */
public class OpaqueVitamTokenGenerator implements OAuth2TokenGenerator<OAuth2AccessToken> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpaqueVitamTokenGenerator.class);

    private final IamClient iamClient;
    private final Duration ttl;

    public OpaqueVitamTokenGenerator(IamClient iamClient, AuthServerProperties properties) {
        this.iamClient = iamClient;
        this.ttl = Duration.ofMinutes(properties.getToken().getAccessTokenTtlMinutes());
    }

    @Override
    public OAuth2AccessToken generate(OAuth2TokenContext context) {
        if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            LOGGER.debug("Skipping opaque generator: token type is {} (expected ACCESS_TOKEN)", context.getTokenType());
            return null;
        }

        UserDto user = extractUser(context.getPrincipal());
        if (user == null || user.getId() == null) {
            LOGGER.warn(
                "Skipping opaque generator: could not extract UserDto from principal. principal class={}, inner class={}",
                context.getPrincipal() != null ? context.getPrincipal().getClass().getName() : "null",
                context.getPrincipal() != null && context.getPrincipal().getPrincipal() != null
                    ? context.getPrincipal().getPrincipal().getClass().getName()
                    : "null"
            );
            return null;
        }

        String opaqueTokenId = iamClient.createOpaqueAuthToken(user.getId(), false, false);
        LOGGER.info("Issued opaque TOK for userId={} email={}", user.getId(), user.getEmail());
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ttl);
        return new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            opaqueTokenId,
            issuedAt,
            expiresAt,
            new HashSet<>(context.getAuthorizedScopes())
        );
    }

    private UserDto extractUser(Authentication principal) {
        if (principal == null) {
            return null;
        }
        Object candidate = principal.getPrincipal();
        if (candidate instanceof UserDto userDto) {
            return userDto;
        }
        return null;
    }
}
