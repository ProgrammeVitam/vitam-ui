/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Configuration
public class RegisteredClientsConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository(AuthServerProperties properties) {
        AuthServerProperties.Token token = properties.getToken();

        List<RegisteredClient> clients = properties
            .getClients()
            .stream()
            .map(cfg -> buildRegisteredClient(cfg, token))
            .toList();

        if (clients.isEmpty()) {
            throw new IllegalStateException(
                "vitamui.auth-server.clients must not be empty. Configure at least one OIDC client in application.yml."
            );
        }

        return new InMemoryRegisteredClientRepository(clients);
    }

    private static RegisteredClient buildRegisteredClient(
        AuthServerProperties.Client cfg,
        AuthServerProperties.Token token
    ) {
        RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(cfg.getClientId())
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .scope(OidcScopes.OPENID)
            .clientSettings(
                ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build()
            )
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(token.getAccessTokenTtlMinutes()))
                    .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                    .build()
            );

        cfg.getRedirectUris().forEach(builder::redirectUri);
        cfg.getPostLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);
        return builder.build();
    }
}
