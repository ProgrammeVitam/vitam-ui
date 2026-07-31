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
 * Mongo document representation of a Spring Authorization Server {@code RegisteredClient} — one document
 * per OAuth2 client (portal, identity, referential, ingest, archive-search, collect, pastis).
 *
 * <p>Layout mirrors the reference {@code JdbcRegisteredClientRepository} table: each SAS field lives in
 * its own document field so the collection stays introspectable from mongosh. Only {@code clientSettings}
 * and {@code tokenSettings} — free-form {@code Map<String, Object>} bags — are stored as nested Mongo
 * documents (BSON handles them natively; no JSON blob).
 *
 * <p>Primary key {@code id} is the SAS internal UUID (derived deterministically from {@code clientId} by
 * the bootstrap so restarts don't create duplicates). {@code clientId} is indexed unique for O(1)
 * lookup by wire identifier.
 */
@Document("registered_clients")
public class RegisteredClientDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clientId;

    private Instant clientIdIssuedAt;
    private String clientSecret;
    private Instant clientSecretExpiresAt;
    private String clientName;

    private Set<String> clientAuthenticationMethods;
    private Set<String> authorizationGrantTypes;
    private Set<String> redirectUris;
    private Set<String> postLogoutRedirectUris;
    private Set<String> scopes;

    // Free-form SAS settings serialised as JSON strings — Mongo forbids dots in field keys and the SAS
    // setting keys (e.g. "settings.client.require-proof-key") are dot-heavy. Storing as a String keeps
    // the driver happy and mirrors what the reference JdbcRegisteredClientRepository does.
    private String clientSettingsJson;
    private String tokenSettingsJson;

    private Instant createdDate;
    private Instant updatedDate;

    public RegisteredClientDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public Instant getClientIdIssuedAt() { return clientIdIssuedAt; }
    public void setClientIdIssuedAt(Instant clientIdIssuedAt) { this.clientIdIssuedAt = clientIdIssuedAt; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public Instant getClientSecretExpiresAt() { return clientSecretExpiresAt; }
    public void setClientSecretExpiresAt(Instant clientSecretExpiresAt) { this.clientSecretExpiresAt = clientSecretExpiresAt; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Set<String> getClientAuthenticationMethods() { return clientAuthenticationMethods; }
    public void setClientAuthenticationMethods(Set<String> clientAuthenticationMethods) { this.clientAuthenticationMethods = clientAuthenticationMethods; }

    public Set<String> getAuthorizationGrantTypes() { return authorizationGrantTypes; }
    public void setAuthorizationGrantTypes(Set<String> authorizationGrantTypes) { this.authorizationGrantTypes = authorizationGrantTypes; }

    public Set<String> getRedirectUris() { return redirectUris; }
    public void setRedirectUris(Set<String> redirectUris) { this.redirectUris = redirectUris; }

    public Set<String> getPostLogoutRedirectUris() { return postLogoutRedirectUris; }
    public void setPostLogoutRedirectUris(Set<String> postLogoutRedirectUris) { this.postLogoutRedirectUris = postLogoutRedirectUris; }

    public Set<String> getScopes() { return scopes; }
    public void setScopes(Set<String> scopes) { this.scopes = scopes; }

    public String getClientSettingsJson() { return clientSettingsJson; }
    public void setClientSettingsJson(String clientSettingsJson) { this.clientSettingsJson = clientSettingsJson; }

    public String getTokenSettingsJson() { return tokenSettingsJson; }
    public void setTokenSettingsJson(String tokenSettingsJson) { this.tokenSettingsJson = tokenSettingsJson; }

    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public Instant getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(Instant updatedDate) { this.updatedDate = updatedDate; }
}
