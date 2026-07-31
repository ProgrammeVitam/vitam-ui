/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Persistent {@link RegisteredClientRepository} backed by Mongo — mirrors the reference
 * {@code JdbcRegisteredClientRepository} layout: each SAS field maps to its own Mongo field, and only
 * the free-form {@code clientSettings}/{@code tokenSettings} maps are stored as nested BSON documents.
 *
 * <p>Kept intentionally free of Jackson: BSON handles primitive collections and maps natively, so no
 * blob serialisation, no allowlist to configure, no risk of breaking on a SAS upgrade that adds new
 * setting keys.
 */
public class MongoRegisteredClientRepository implements RegisteredClientRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoRegisteredClientRepository.class);
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP =
        new ParameterizedTypeReference<>() {};

    private final RegisteredClientDocumentRepository documents;
    private final JsonMapper settingsMapper;
    private final JavaType mapType;

    public MongoRegisteredClientRepository(RegisteredClientDocumentRepository documents) {
        this.documents = documents;
        this.settingsMapper = buildSettingsMapper();
        this.mapType = this.settingsMapper.getTypeFactory().constructType(STRING_OBJECT_MAP.getType());
    }

    /**
     * Same setup as the reference {@code JdbcRegisteredClientRepository$Jackson3} — needed to
     * round-trip the polymorphic values inside SAS {@code Settings} maps (Duration, OAuth2TokenFormat,
     * SignatureAlgorithm, etc.).
     */
    private static JsonMapper buildSettingsMapper() {
        ClassLoader classLoader = MongoRegisteredClientRepository.class.getClassLoader();
        List<JacksonModule> securityModules = SecurityJacksonModules.getModules(classLoader);
        return JsonMapper.builder().addModules(securityModules).addModule(new OAuth2AuthorizationServerJacksonModule()).build();
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Assert.notNull(registeredClient, "registeredClient cannot be null");
        RegisteredClientDocument existing = documents.findById(registeredClient.getId()).orElse(null);
        RegisteredClientDocument doc = existing != null ? existing : new RegisteredClientDocument();
        toDocument(registeredClient, doc);
        Instant now = Instant.now();
        if (existing == null) {
            doc.setCreatedDate(now);
        }
        doc.setUpdatedDate(now);
        documents.save(doc);
        LOGGER.debug("Saved RegisteredClient id={} clientId={}", registeredClient.getId(), registeredClient.getClientId());
    }

    @Override
    public RegisteredClient findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        return documents.findById(id).map(this::toRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        return documents.findByClientId(clientId).map(this::toRegisteredClient).orElse(null);
    }

    /** Maps a {@link RegisteredClient} onto a target document, preserving the id if it exists. */
    private void toDocument(RegisteredClient client, RegisteredClientDocument doc) {
        doc.setId(client.getId());
        doc.setClientId(client.getClientId());
        doc.setClientIdIssuedAt(client.getClientIdIssuedAt());
        doc.setClientSecret(client.getClientSecret());
        doc.setClientSecretExpiresAt(client.getClientSecretExpiresAt());
        doc.setClientName(client.getClientName());
        doc.setClientAuthenticationMethods(
            client.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::getValue).collect(Collectors.toCollection(HashSet::new))
        );
        doc.setAuthorizationGrantTypes(
            client.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).collect(Collectors.toCollection(HashSet::new))
        );
        doc.setRedirectUris(new HashSet<>(client.getRedirectUris()));
        doc.setPostLogoutRedirectUris(new HashSet<>(client.getPostLogoutRedirectUris()));
        doc.setScopes(new HashSet<>(client.getScopes()));
        doc.setClientSettingsJson(writeSettings(client.getClientSettings().getSettings()));
        doc.setTokenSettingsJson(writeSettings(client.getTokenSettings().getSettings()));
    }

    private RegisteredClient toRegisteredClient(RegisteredClientDocument doc) {
        Set<String> methods = doc.getClientAuthenticationMethods() != null ? doc.getClientAuthenticationMethods() : Set.of();
        Set<String> grants = doc.getAuthorizationGrantTypes() != null ? doc.getAuthorizationGrantTypes() : Set.of();
        Set<String> redirects = doc.getRedirectUris() != null ? doc.getRedirectUris() : Set.of();
        Set<String> postLogout = doc.getPostLogoutRedirectUris() != null ? doc.getPostLogoutRedirectUris() : Set.of();
        Set<String> scopes = doc.getScopes() != null ? doc.getScopes() : Set.of();

        RegisteredClient.Builder builder = RegisteredClient.withId(doc.getId())
            .clientId(doc.getClientId())
            .clientIdIssuedAt(doc.getClientIdIssuedAt())
            .clientSecret(doc.getClientSecret())
            .clientSecretExpiresAt(doc.getClientSecretExpiresAt())
            .clientName(doc.getClientName())
            .clientAuthenticationMethods(target -> methods.forEach(m -> target.add(resolveAuthenticationMethod(m))))
            .authorizationGrantTypes(target -> grants.forEach(g -> target.add(resolveGrantType(g))))
            .redirectUris(target -> target.addAll(redirects))
            .postLogoutRedirectUris(target -> target.addAll(postLogout))
            .scopes(target -> target.addAll(scopes));

        Map<String, Object> clientSettingsMap = readSettings(doc.getClientSettingsJson());
        builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());

        Map<String, Object> tokenSettingsMap = readSettings(doc.getTokenSettingsJson());
        TokenSettings.Builder tokenBuilder = TokenSettings.withSettings(tokenSettingsMap);
        if (!tokenSettingsMap.containsKey(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT)) {
            tokenBuilder.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED);
        }
        builder.tokenSettings(tokenBuilder.build());

        return builder.build();
    }

    private String writeSettings(Map<String, Object> settings) {
        try {
            return settingsMapper.writeValueAsString(settings != null ? settings : Map.of());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise settings", e);
        }
    }

    private Map<String, Object> readSettings(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return settingsMapper.readValue(json, mapType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialise settings", e);
        }
    }

    private static AuthorizationGrantType resolveGrantType(String value) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(value)) return AuthorizationGrantType.AUTHORIZATION_CODE;
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(value)) return AuthorizationGrantType.CLIENT_CREDENTIALS;
        if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(value)) return AuthorizationGrantType.REFRESH_TOKEN;
        return new AuthorizationGrantType(value);
    }

    private static ClientAuthenticationMethod resolveAuthenticationMethod(String value) {
        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(value)) return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(value)) return ClientAuthenticationMethod.CLIENT_SECRET_POST;
        if (ClientAuthenticationMethod.NONE.getValue().equals(value)) return ClientAuthenticationMethod.NONE;
        return new ClientAuthenticationMethod(value);
    }
}
