/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Business layer around {@link PasswordResetTokenRepository}. Encapsulates nonce generation, TTL
 * policy and one-shot consumption so controllers stay dumb.
 */
@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int NONCE_BYTES = 32;

    private final PasswordResetTokenRepository tokens;
    private final Duration ttl;

    public PasswordResetService(
        PasswordResetTokenRepository tokens,
        @Value("${vitamui.auth-server.password-reset.ttl-minutes:30}") long ttlMinutes
    ) {
        this.tokens = tokens;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    /**
     * Purges any pending reset for the (email, customerId) pair (a fresh request supersedes previous
     * ones) then issues a new nonce with the default reset TTL.
     */
    public String issue(String email, String customerId) {
        return issueWithTtl(email, customerId, ttl);
    }

    /**
     * Same as {@link #issue(String, String)} but with a caller-supplied TTL — used by the welcome
     * flow that hands out a much longer window (typically 24 h) so a new user has time to open the
     * email from their real inbox.
     */
    public String issueWithTtl(String email, String customerId, Duration customTtl) {
        tokens.deleteByEmailAndCustomerId(email, customerId);
        String nonce = generateNonce();
        Instant expiresAt = Instant.now().plus(customTtl);
        tokens.save(new PasswordResetTokenDocument(nonce, email, customerId, expiresAt));
        LOGGER.info(
            "Issued password-reset token for email={} customer={} (ttl={}min)",
            email,
            customerId,
            customTtl.toMinutes()
        );
        return nonce;
    }

    /**
     * One-shot consumption: returns the token payload and deletes the row in the same call. Returns
     * empty when the nonce doesn't match a row or the row has expired since (Mongo TTL is best-effort;
     * we double-check the timestamp).
     */
    public Optional<PasswordResetTokenDocument> consume(String nonce) {
        Optional<PasswordResetTokenDocument> row = tokens.findById(nonce);
        if (row.isEmpty()) {
            LOGGER.info("Password-reset token miss (unknown)");
            return Optional.empty();
        }
        PasswordResetTokenDocument doc = row.get();
        tokens.deleteById(nonce);
        if (doc.getExpiresAt() != null && doc.getExpiresAt().isBefore(Instant.now())) {
            LOGGER.info("Password-reset token miss (expired) for email={}", doc.getEmail());
            return Optional.empty();
        }
        return Optional.of(doc);
    }

    public Duration getTtl() {
        return ttl;
    }

    private static String generateNonce() {
        byte[] bytes = new byte[NONCE_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
