/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.config;

import fr.gouv.vitamui.authserver.security.MongoRegisteredClientRepository;
import fr.gouv.vitamui.authserver.security.RegisteredClientDocumentRepository;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration
@EnableMongoRepositories(basePackageClasses = RegisteredClientDocumentRepository.class)
public class RegisteredClientsConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredClientsConfig.class);

    @Bean
    public RegisteredClientRepository registeredClientRepository(RegisteredClientDocumentRepository documents) {
        return new MongoRegisteredClientRepository(documents);
    }

    /**
     * On startup, upsert into Mongo every client declared under {@code vitamui.auth-server.clients}. The
     * yaml stays the versioned source of truth for the initial fleet (portal, identity, referential, …);
     * later additions can be made by inserting documents directly with mongosh — no auth-server redeploy
     * needed. The {@code RegisteredClient.id} is derived deterministically from the {@code clientId} so
     * restarts don't create duplicate documents.
     */
    @Bean
    public CommandLineRunner registeredClientsBootstrap(
        RegisteredClientRepository repository,
        AuthServerProperties properties
    ) {
        return args -> {
            AuthServerProperties.Token token = properties.getToken();
            List<AuthServerProperties.Client> configured = properties.getClients();
            if (configured.isEmpty()) {
                LOGGER.info("No vitamui.auth-server.clients declared — skipping bootstrap");
                return;
            }
            for (AuthServerProperties.Client cfg : configured) {
                RegisteredClient client = buildRegisteredClient(cfg, token);
                RegisteredClient existing = repository.findByClientId(cfg.getClientId());
                if (existing != null && sameContent(existing, client)) {
                    LOGGER.debug("Client {} already up-to-date in Mongo — skipping upsert", cfg.getClientId());
                    continue;
                }
                repository.save(client);
                LOGGER.info(
                    "Bootstrapped RegisteredClient {} (id={}) into Mongo",
                    cfg.getClientId(),
                    client.getId()
                );
            }
        };
    }

    /**
     * Cheap change detector — avoids rewriting the document on every restart when the yaml is unchanged.
     * Compares the wire-visible pieces (redirect URIs, post-logout URIs, TTL, scopes). If SAS grows more
     * settings we care about, extend this — it's OK to be conservative and re-save on doubt.
     */
    private static boolean sameContent(RegisteredClient existing, RegisteredClient candidate) {
        return existing.getRedirectUris().equals(candidate.getRedirectUris())
            && existing.getPostLogoutRedirectUris().equals(candidate.getPostLogoutRedirectUris())
            && existing.getScopes().equals(candidate.getScopes())
            && existing.getTokenSettings().getAccessTokenTimeToLive()
                .equals(candidate.getTokenSettings().getAccessTokenTimeToLive());
    }

    private static RegisteredClient buildRegisteredClient(
        AuthServerProperties.Client cfg,
        AuthServerProperties.Token token
    ) {
        // Deterministic id derived from clientId: guarantees the same RegisteredClient.id across restarts,
        // so downstream OAuth2Authorization rows (chantier #5) don't break when SAS restarts.
        String id = UUID.nameUUIDFromBytes(("auth-server:" + cfg.getClientId()).getBytes(StandardCharsets.UTF_8))
            .toString();

        RegisteredClient.Builder builder = RegisteredClient.withId(id)
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
