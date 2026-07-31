/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * One-shot nonce mailed to a user who asked to reset their password. The document {@code _id} is the
 * nonce itself (URL-safe random string), so a lookup is a primary-key hit.
 *
 * <p>Identity is stored as {@code (email, customerId)} because that's what IAM's password-change
 * endpoint accepts; we intentionally don't carry a user id (the HRD lookup only surfaces
 * {@code customerId}, and refetching the user id would add a round trip for no functional gain).
 *
 * <p>{@code expiresAt} carries a Mongo TTL index — the driver purges stale rows automatically, so no
 * background sweep is needed. Consumption also deletes the row explicitly to give a strict one-shot
 * guarantee: replaying the same nonce fails even if the TTL hasn't kicked in yet.
 */
@Document("password_reset_tokens")
public class PasswordResetTokenDocument {

    /** The URL-safe nonce sent in the reset email — used as-is as the Mongo primary key. */
    @Id
    private String token;

    private String email;
    private String customerId;
    private Instant createdAt;

    /** {@code expireAfterSeconds=0} lets Mongo purge rows the moment {@code expiresAt} is reached. */
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    public PasswordResetTokenDocument() {}

    public PasswordResetTokenDocument(String token, String email, String customerId, Instant expiresAt) {
        this.token = token;
        this.email = email;
        this.customerId = customerId;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
