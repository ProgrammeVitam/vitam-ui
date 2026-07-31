/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import java.time.Instant;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Mongo document representation of a Spring Authorization Server {@code OAuth2Authorization}.
 * Layout mirrors the reference {@code JdbcOAuth2AuthorizationService} table — 6 token blocks each with
 * value / issued_at / expires_at / metadata. Free-form maps ({@code attributes}, each token's
 * {@code metadata}) are stored as JSON strings to sidestep Mongo's dot-in-key restriction and to
 * survive SAS upgrades that grow the map schema.
 *
 * <p>Each token value gets a sparse index so {@code findByToken} stays O(log n): most rows only carry
 * one or two of the six token slots, and {@code sparse=true} keeps the index footprint proportional.
 */
@Document("oauth2_authorizations")
public class OAuth2AuthorizationDocument {

    @Id
    private String id;

    @Indexed
    private String registeredClientId;

    private String principalName;
    private String authorizationGrantType;
    private Set<String> authorizedScopes;

    /** Serialised {@code Map<String, Object>} — the SAS request attributes bag. */
    private String attributesJson;

    @Indexed(sparse = true)
    private String state;

    // --- Authorization code ---
    @Indexed(sparse = true)
    private String authorizationCodeValue;
    private Instant authorizationCodeIssuedAt;
    private Instant authorizationCodeExpiresAt;
    private String authorizationCodeMetadataJson;

    // --- Access token ---
    @Indexed(sparse = true)
    private String accessTokenValue;
    private Instant accessTokenIssuedAt;
    private Instant accessTokenExpiresAt;
    private String accessTokenMetadataJson;
    private String accessTokenType;
    private Set<String> accessTokenScopes;

    // --- OIDC id token ---
    @Indexed(sparse = true)
    private String oidcIdTokenValue;
    private Instant oidcIdTokenIssuedAt;
    private Instant oidcIdTokenExpiresAt;
    private String oidcIdTokenMetadataJson;

    // --- Refresh token ---
    @Indexed(sparse = true)
    private String refreshTokenValue;
    private Instant refreshTokenIssuedAt;
    private Instant refreshTokenExpiresAt;
    private String refreshTokenMetadataJson;

    // --- Device grant plumbing (unused today; kept for parity with JDBC schema) ---
    @Indexed(sparse = true)
    private String userCodeValue;
    private Instant userCodeIssuedAt;
    private Instant userCodeExpiresAt;
    private String userCodeMetadataJson;

    @Indexed(sparse = true)
    private String deviceCodeValue;
    private Instant deviceCodeIssuedAt;
    private Instant deviceCodeExpiresAt;
    private String deviceCodeMetadataJson;

    public OAuth2AuthorizationDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRegisteredClientId() { return registeredClientId; }
    public void setRegisteredClientId(String registeredClientId) { this.registeredClientId = registeredClientId; }

    public String getPrincipalName() { return principalName; }
    public void setPrincipalName(String principalName) { this.principalName = principalName; }

    public String getAuthorizationGrantType() { return authorizationGrantType; }
    public void setAuthorizationGrantType(String authorizationGrantType) { this.authorizationGrantType = authorizationGrantType; }

    public Set<String> getAuthorizedScopes() { return authorizedScopes; }
    public void setAuthorizedScopes(Set<String> authorizedScopes) { this.authorizedScopes = authorizedScopes; }

    public String getAttributesJson() { return attributesJson; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getAuthorizationCodeValue() { return authorizationCodeValue; }
    public void setAuthorizationCodeValue(String v) { this.authorizationCodeValue = v; }
    public Instant getAuthorizationCodeIssuedAt() { return authorizationCodeIssuedAt; }
    public void setAuthorizationCodeIssuedAt(Instant v) { this.authorizationCodeIssuedAt = v; }
    public Instant getAuthorizationCodeExpiresAt() { return authorizationCodeExpiresAt; }
    public void setAuthorizationCodeExpiresAt(Instant v) { this.authorizationCodeExpiresAt = v; }
    public String getAuthorizationCodeMetadataJson() { return authorizationCodeMetadataJson; }
    public void setAuthorizationCodeMetadataJson(String v) { this.authorizationCodeMetadataJson = v; }

    public String getAccessTokenValue() { return accessTokenValue; }
    public void setAccessTokenValue(String v) { this.accessTokenValue = v; }
    public Instant getAccessTokenIssuedAt() { return accessTokenIssuedAt; }
    public void setAccessTokenIssuedAt(Instant v) { this.accessTokenIssuedAt = v; }
    public Instant getAccessTokenExpiresAt() { return accessTokenExpiresAt; }
    public void setAccessTokenExpiresAt(Instant v) { this.accessTokenExpiresAt = v; }
    public String getAccessTokenMetadataJson() { return accessTokenMetadataJson; }
    public void setAccessTokenMetadataJson(String v) { this.accessTokenMetadataJson = v; }
    public String getAccessTokenType() { return accessTokenType; }
    public void setAccessTokenType(String v) { this.accessTokenType = v; }
    public Set<String> getAccessTokenScopes() { return accessTokenScopes; }
    public void setAccessTokenScopes(Set<String> v) { this.accessTokenScopes = v; }

    public String getOidcIdTokenValue() { return oidcIdTokenValue; }
    public void setOidcIdTokenValue(String v) { this.oidcIdTokenValue = v; }
    public Instant getOidcIdTokenIssuedAt() { return oidcIdTokenIssuedAt; }
    public void setOidcIdTokenIssuedAt(Instant v) { this.oidcIdTokenIssuedAt = v; }
    public Instant getOidcIdTokenExpiresAt() { return oidcIdTokenExpiresAt; }
    public void setOidcIdTokenExpiresAt(Instant v) { this.oidcIdTokenExpiresAt = v; }
    public String getOidcIdTokenMetadataJson() { return oidcIdTokenMetadataJson; }
    public void setOidcIdTokenMetadataJson(String v) { this.oidcIdTokenMetadataJson = v; }

    public String getRefreshTokenValue() { return refreshTokenValue; }
    public void setRefreshTokenValue(String v) { this.refreshTokenValue = v; }
    public Instant getRefreshTokenIssuedAt() { return refreshTokenIssuedAt; }
    public void setRefreshTokenIssuedAt(Instant v) { this.refreshTokenIssuedAt = v; }
    public Instant getRefreshTokenExpiresAt() { return refreshTokenExpiresAt; }
    public void setRefreshTokenExpiresAt(Instant v) { this.refreshTokenExpiresAt = v; }
    public String getRefreshTokenMetadataJson() { return refreshTokenMetadataJson; }
    public void setRefreshTokenMetadataJson(String v) { this.refreshTokenMetadataJson = v; }

    public String getUserCodeValue() { return userCodeValue; }
    public void setUserCodeValue(String v) { this.userCodeValue = v; }
    public Instant getUserCodeIssuedAt() { return userCodeIssuedAt; }
    public void setUserCodeIssuedAt(Instant v) { this.userCodeIssuedAt = v; }
    public Instant getUserCodeExpiresAt() { return userCodeExpiresAt; }
    public void setUserCodeExpiresAt(Instant v) { this.userCodeExpiresAt = v; }
    public String getUserCodeMetadataJson() { return userCodeMetadataJson; }
    public void setUserCodeMetadataJson(String v) { this.userCodeMetadataJson = v; }

    public String getDeviceCodeValue() { return deviceCodeValue; }
    public void setDeviceCodeValue(String v) { this.deviceCodeValue = v; }
    public Instant getDeviceCodeIssuedAt() { return deviceCodeIssuedAt; }
    public void setDeviceCodeIssuedAt(Instant v) { this.deviceCodeIssuedAt = v; }
    public Instant getDeviceCodeExpiresAt() { return deviceCodeExpiresAt; }
    public void setDeviceCodeExpiresAt(Instant v) { this.deviceCodeExpiresAt = v; }
    public String getDeviceCodeMetadataJson() { return deviceCodeMetadataJson; }
    public void setDeviceCodeMetadataJson(String v) { this.deviceCodeMetadataJson = v; }
}
