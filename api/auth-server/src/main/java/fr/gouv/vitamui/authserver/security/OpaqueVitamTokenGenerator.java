/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import fr.gouv.vitamui.authserver.config.AuthServerProperties;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

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
@Component
public class OpaqueVitamTokenGenerator implements OAuth2TokenGenerator<OAuth2AccessToken> {

    private final IamClient iamClient;
    private final Duration ttl;

    public OpaqueVitamTokenGenerator(IamClient iamClient, AuthServerProperties properties) {
        this.iamClient = iamClient;
        this.ttl = Duration.ofMinutes(properties.getToken().getAccessTokenTtlMinutes());
    }

    @Override
    public OAuth2AccessToken generate(OAuth2TokenContext context) {
        if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            return null;
        }

        UserDto user = extractUser(context.getPrincipal());
        if (user == null || user.getId() == null) {
            return null;
        }

        String opaqueTokenId = iamClient.createOpaqueAuthToken(user.getId(), false, false);
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
