/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.util.Assert;

/**
 * Persistent {@link OAuth2AuthorizationService} backed by Mongo — mirrors the reference
 * {@code JdbcOAuth2AuthorizationService}: flat fields per SAS attribute, plus JSON strings for the
 * dynamic {@code attributes} bag and each token's {@code metadata}.
 *
 * <p>The Jackson {@link ObjectMapper} is the same setup as
 * {@code JdbcOAuth2AuthorizationService$Jackson2}: security modules + SAS module. Only
 * {@code Map<String, Object>} payloads pass through it, so no allowlist to fight.
 */
public class MongoOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoOAuth2AuthorizationService.class);
    private static final TypeReference<Map<String, Object>> STRING_OBJECT_MAP = new TypeReference<>() {};

    private final OAuth2AuthorizationDocumentRepository documents;
    private final RegisteredClientRepository registeredClientRepository;
    private final ObjectMapper jsonMapper;

    public MongoOAuth2AuthorizationService(
        OAuth2AuthorizationDocumentRepository documents,
        RegisteredClientRepository registeredClientRepository
    ) {
        this.documents = documents;
        this.registeredClientRepository = registeredClientRepository;
        this.jsonMapper = buildJsonMapper();
    }

    private static ObjectMapper buildJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        ClassLoader classLoader = MongoOAuth2AuthorizationService.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        mapper.registerModules(securityModules);
        mapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        // The SAS module activates an allowlist-based default typing which only knows Spring Security
        // internals — it rejects our VitamuiPrincipal / CustomerIdAuthenticationDetails and Spring's own
        // FactorGrantedAuthority on read. We're a trusted source (SAS reads what it wrote itself into
        // Mongo), so replace the typing with one that accepts anything under `java.` /
        // `org.springframework.` / `fr.gouv.vitamui.`. Serialization keeps writing the @class discriminator.
        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .allowIfSubType("java.")
            .allowIfSubType("org.springframework.")
            .allowIfSubType("fr.gouv.vitamui.")
            .build();
        mapper.activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        // FactorGrantedAuthority has factory methods only — Jackson can't invoke them via a mixin
        // (`@JsonCreator` on a static method inside a mixin is silently ignored because the target
        // class has no matching static method). A custom deserializer sidesteps the whole dance.
        com.fasterxml.jackson.databind.module.SimpleModule factorModule = new com.fasterxml.jackson.databind.module.SimpleModule();
        factorModule.addDeserializer(
            org.springframework.security.core.authority.FactorGrantedAuthority.class,
            new FactorGrantedAuthorityDeserializer()
        );
        mapper.registerModule(factorModule);
        return mapper;
    }

    /** Rebuilds a {@link org.springframework.security.core.authority.FactorGrantedAuthority} from
     *  the {@code {authority, issuedAt}} shape Spring writes out. */
    private static final class FactorGrantedAuthorityDeserializer
        extends com.fasterxml.jackson.databind.JsonDeserializer<org.springframework.security.core.authority.FactorGrantedAuthority> {

        @Override
        public org.springframework.security.core.authority.FactorGrantedAuthority deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext ctx
        ) throws java.io.IOException {
            com.fasterxml.jackson.databind.JsonNode node = parser.getCodec().readTree(parser);
            String authority = node.hasNonNull("authority") ? node.get("authority").asText() : null;
            java.time.Instant issuedAt = readInstant(node.get("issuedAt"));
            if (authority == null) {
                throw ctx.instantiationException(
                    org.springframework.security.core.authority.FactorGrantedAuthority.class,
                    "missing 'authority' field"
                );
            }
            return org.springframework.security.core.authority.FactorGrantedAuthority
                .withAuthority(authority)
                .issuedAt(issuedAt)
                .build();
        }

        /**
         * Spring writes {@code issuedAt} as an epoch-seconds double (e.g. {@code 1.785e9}). Fall back
         * to ISO-8601 parsing for values written by other serialisers.
         */
        private static java.time.Instant readInstant(com.fasterxml.jackson.databind.JsonNode node) {
            if (node == null || node.isNull()) {
                return java.time.Instant.now();
            }
            if (node.isNumber()) {
                double epochSeconds = node.asDouble();
                long secs = (long) epochSeconds;
                long nanos = Math.round((epochSeconds - secs) * 1_000_000_000L);
                return java.time.Instant.ofEpochSecond(secs, nanos);
            }
            return java.time.Instant.parse(node.asText());
        }
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        OAuth2AuthorizationDocument existing = documents.findById(authorization.getId()).orElse(null);
        OAuth2AuthorizationDocument doc = existing != null ? existing : new OAuth2AuthorizationDocument();
        toDocument(authorization, doc);
        documents.save(doc);
        LOGGER.debug("Saved OAuth2Authorization id={} principal={}", authorization.getId(), authorization.getPrincipalName());
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        documents.deleteById(authorization.getId());
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        return documents.findById(id).map(this::toAuthorization).orElse(null);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        Optional<OAuth2AuthorizationDocument> hit;
        if (tokenType == null) {
            hit = documents.findByAnyTokenValue(token);
        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            hit = documents.findByState(token);
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            hit = documents.findByAuthorizationCodeValue(token);
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            hit = documents.findByAccessTokenValue(token);
        } else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            hit = documents.findByOidcIdTokenValue(token);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            hit = documents.findByRefreshTokenValue(token);
        } else {
            // Device grant flow (user_code / device_code) isn't used by vitam-ui — fall back to the
            // generic sweep just in case a custom grant plugs in later.
            hit = documents.findByAnyTokenValue(token);
        }
        return hit.map(this::toAuthorization).orElse(null);
    }

    private void toDocument(OAuth2Authorization src, OAuth2AuthorizationDocument doc) {
        doc.setId(src.getId());
        doc.setRegisteredClientId(src.getRegisteredClientId());
        doc.setPrincipalName(src.getPrincipalName());
        doc.setAuthorizationGrantType(src.getAuthorizationGrantType().getValue());
        doc.setAuthorizedScopes(src.getAuthorizedScopes() != null ? new HashSet<>(src.getAuthorizedScopes()) : Set.of());
        doc.setAttributesJson(writeMap(src.getAttributes()));
        doc.setState(src.getAttribute(OAuth2ParameterNames.STATE));

        setTokenSlot(
            src.getToken(OAuth2AuthorizationCode.class),
            doc::setAuthorizationCodeValue,
            doc::setAuthorizationCodeIssuedAt,
            doc::setAuthorizationCodeExpiresAt,
            doc::setAuthorizationCodeMetadataJson
        );

        OAuth2Authorization.Token<OAuth2AccessToken> accessTokenSlot = src.getToken(OAuth2AccessToken.class);
        if (accessTokenSlot != null) {
            OAuth2AccessToken accessToken = accessTokenSlot.getToken();
            doc.setAccessTokenValue(accessToken.getTokenValue());
            doc.setAccessTokenIssuedAt(accessToken.getIssuedAt());
            doc.setAccessTokenExpiresAt(accessToken.getExpiresAt());
            doc.setAccessTokenMetadataJson(writeMap(accessTokenSlot.getMetadata()));
            doc.setAccessTokenType(accessToken.getTokenType() != null ? accessToken.getTokenType().getValue() : null);
            doc.setAccessTokenScopes(accessToken.getScopes() != null ? new HashSet<>(accessToken.getScopes()) : Set.of());
        } else {
            doc.setAccessTokenValue(null);
            doc.setAccessTokenIssuedAt(null);
            doc.setAccessTokenExpiresAt(null);
            doc.setAccessTokenMetadataJson(null);
            doc.setAccessTokenType(null);
            doc.setAccessTokenScopes(null);
        }

        setTokenSlot(
            src.getToken(OidcIdToken.class),
            doc::setOidcIdTokenValue,
            doc::setOidcIdTokenIssuedAt,
            doc::setOidcIdTokenExpiresAt,
            doc::setOidcIdTokenMetadataJson
        );
        setTokenSlot(
            src.getToken(OAuth2RefreshToken.class),
            doc::setRefreshTokenValue,
            doc::setRefreshTokenIssuedAt,
            doc::setRefreshTokenExpiresAt,
            doc::setRefreshTokenMetadataJson
        );
        // Device-grant slots (user_code / device_code) stay null in vitam-ui — SAS never emits them.
        doc.setUserCodeValue(null);
        doc.setUserCodeIssuedAt(null);
        doc.setUserCodeExpiresAt(null);
        doc.setUserCodeMetadataJson(null);
        doc.setDeviceCodeValue(null);
        doc.setDeviceCodeIssuedAt(null);
        doc.setDeviceCodeExpiresAt(null);
        doc.setDeviceCodeMetadataJson(null);
    }

    private <T extends OAuth2Token> void setTokenSlot(
        OAuth2Authorization.Token<T> slot,
        java.util.function.Consumer<String> value,
        java.util.function.Consumer<Instant> issuedAt,
        java.util.function.Consumer<Instant> expiresAt,
        java.util.function.Consumer<String> metadataJson
    ) {
        if (slot == null) {
            value.accept(null);
            issuedAt.accept(null);
            expiresAt.accept(null);
            metadataJson.accept(null);
            return;
        }
        T token = slot.getToken();
        value.accept(token.getTokenValue());
        issuedAt.accept(token.getIssuedAt());
        expiresAt.accept(token.getExpiresAt());
        metadataJson.accept(writeMap(slot.getMetadata()));
    }

    @SuppressWarnings("unchecked")
    private OAuth2Authorization toAuthorization(OAuth2AuthorizationDocument doc) {
        RegisteredClient client = registeredClientRepository.findById(doc.getRegisteredClientId());
        if (client == null) {
            throw new IllegalStateException(
                "RegisteredClient " + doc.getRegisteredClientId() + " referenced by authorization " + doc.getId() + " no longer exists"
            );
        }

        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(client)
            .id(doc.getId())
            .principalName(doc.getPrincipalName())
            .authorizationGrantType(new AuthorizationGrantType(doc.getAuthorizationGrantType()));

        Set<String> scopes = doc.getAuthorizedScopes() != null ? doc.getAuthorizedScopes() : Set.of();
        builder.authorizedScopes(scopes);

        Map<String, Object> attributes = readMap(doc.getAttributesJson());
        builder.attributes(target -> target.putAll(attributes));

        if (doc.getState() != null && !doc.getState().isBlank()) {
            builder.attribute(OAuth2ParameterNames.STATE, doc.getState());
        }

        if (doc.getAuthorizationCodeValue() != null) {
            OAuth2AuthorizationCode code = new OAuth2AuthorizationCode(
                doc.getAuthorizationCodeValue(),
                doc.getAuthorizationCodeIssuedAt(),
                doc.getAuthorizationCodeExpiresAt()
            );
            Map<String, Object> metadata = readMap(doc.getAuthorizationCodeMetadataJson());
            builder.token(code, m -> m.putAll(metadata));
        }

        if (doc.getAccessTokenValue() != null) {
            OAuth2AccessToken.TokenType type = null;
            if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(doc.getAccessTokenType())) {
                type = OAuth2AccessToken.TokenType.BEARER;
            } else if (OAuth2AccessToken.TokenType.DPOP.getValue().equalsIgnoreCase(doc.getAccessTokenType())) {
                type = OAuth2AccessToken.TokenType.DPOP;
            }
            Set<String> accessScopes = doc.getAccessTokenScopes() != null ? doc.getAccessTokenScopes() : Set.of();
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                type,
                doc.getAccessTokenValue(),
                doc.getAccessTokenIssuedAt(),
                doc.getAccessTokenExpiresAt(),
                accessScopes
            );
            Map<String, Object> metadata = readMap(doc.getAccessTokenMetadataJson());
            builder.token(accessToken, m -> m.putAll(metadata));
        }

        if (doc.getOidcIdTokenValue() != null) {
            Map<String, Object> metadata = readMap(doc.getOidcIdTokenMetadataJson());
            OidcIdToken idToken = new OidcIdToken(
                doc.getOidcIdTokenValue(),
                doc.getOidcIdTokenIssuedAt(),
                doc.getOidcIdTokenExpiresAt(),
                (Map<String, Object>) metadata.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME)
            );
            builder.token(idToken, m -> m.putAll(metadata));
        }

        if (doc.getRefreshTokenValue() != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                doc.getRefreshTokenValue(),
                doc.getRefreshTokenIssuedAt(),
                doc.getRefreshTokenExpiresAt()
            );
            Map<String, Object> metadata = readMap(doc.getRefreshTokenMetadataJson());
            builder.token(refreshToken, m -> m.putAll(metadata));
        }

        // user_code / device_code are not emitted by vitam-ui — no rebuild path needed.

        return builder.build();
    }

    private String writeMap(Map<String, Object> map) {
        try {
            return jsonMapper.writeValueAsString(map != null ? map : Map.of());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise map", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return jsonMapper.readValue(json, STRING_OBJECT_MAP);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialise map", e);
        }
    }

    /** Local mirror of the {@code id_token} parameter name to avoid a stray optional dependency. */
    private static final class OidcParameterNames {
        static final String ID_TOKEN = "id_token";
    }
}
