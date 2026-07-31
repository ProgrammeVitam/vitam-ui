/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.iam.server.idp.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM cipher for the IdP secret fields (client secret, keystore, passwords). Format on disk:
 *
 * <pre>
 *   {enc:v1}&lt;base64url(iv12 || ciphertext || tag16)&gt;
 * </pre>
 *
 * <p>Reads tolerate a value without the prefix and return it as-is — this makes the change safe to
 * roll out on top of an existing collection: legacy CAS on {@code develop} still reads clear values
 * through the same IAM layer, and the {@link IdentityProviderSecretMigrationRunner} bootstraps the
 * migration by re-saving the row (which triggers the writing converter).
 *
 * <p>Key material is read once at startup from {@code iam.secrets.key} (base64, 32 raw bytes). When
 * the property is absent or blank the cipher enters <b>passthrough mode</b>: {@link #encrypt} returns
 * its input unchanged (no prefix) and no encryption takes place. This is deliberate — a hard-coded
 * dev key would give a false sense of security since anyone with source access could decrypt.
 * Instead the operator must explicitly opt in by setting a real key.
 */
@Component
public class EncryptedSecretCipher {

    public static final String PREFIX = "{enc:v1}";
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedSecretCipher.class);
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** {@code null} means passthrough mode — encrypt is a no-op, decrypt still handles legacy prefixed data. */
    private final SecretKey key;

    public EncryptedSecretCipher(@Value("${secrets.idp-key:#{null}}") String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            this.key = null;
            LOGGER.warn(
                "EncryptedSecretCipher: no iam.secrets.key configured — IdP secrets are stored IN THE CLEAR. " +
                "Set the IAM_SECRETS_KEY env var (base64, 32 raw bytes) to enable encryption at rest."
            );
            return;
        }
        byte[] raw = Base64.getDecoder().decode(base64Key);
        if (raw.length != 32) {
            throw new IllegalStateException("iam.secrets.key must decode to 32 bytes, got " + raw.length);
        }
        this.key = new SecretKeySpec(raw, "AES");
        LOGGER.info("EncryptedSecretCipher initialised — IdP secret fields will be encrypted at rest.");
    }

    /** True when a real key is configured; false when running in passthrough mode. */
    public boolean isEnabled() {
        return key != null;
    }

    /**
     * Encrypts and returns the prefixed value. In passthrough mode (no key configured), returns
     * {@code plaintext} unchanged so downstream code stores it in the clear. Returns {@code null} for
     * a null input.
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        if (key == null) return plaintext;
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ct = c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ct, 0, combined, iv.length, ct.length);
            return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt secret", e);
        }
    }

    /**
     * Reads a value that may or may not be prefixed. Non-prefixed values are returned as-is so old
     * data written by legacy CAS keeps loading without a schema migration. Prefixed values are
     * decrypted with the configured key.
     */
    public String decrypt(String stored) {
        if (stored == null) return null;
        if (!stored.startsWith(PREFIX)) return stored;
        try {
            byte[] combined = Base64.getUrlDecoder().decode(stored.substring(PREFIX.length()));
            if (combined.length < IV_LENGTH + 1) {
                throw new IllegalStateException("Encrypted payload too short");
            }
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            byte[] ct = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, ct, 0, ct.length);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(c.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt secret", e);
        }
    }

    /** True when the value is already in the encrypted format — used by the migration to skip rows. */
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }
}
