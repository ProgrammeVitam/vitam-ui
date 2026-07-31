/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.iam.server.idp.domain;

import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Migrates legacy {@link IdentityProvider} rows that still carry secret fields in the clear (from a
 * time before {@link EncryptedSecretCipher} existed). Called once at IAM startup:
 *
 * <ol>
 *   <li>Reads each provider as a raw {@link Document} — bypasses the converter so we see what's on
 *       disk, not the decrypted view.</li>
 *   <li>If any of the four sensitive fields lacks the {@code {enc:v1}} prefix, re-saves the entity
 *       through the standard repository: the {@code EncryptedSecretConverter} triggers on write and
 *       persists an encrypted value.</li>
 * </ol>
 *
 * <p>Idempotent: on the second run every field carries the prefix and the runner logs "already
 * encrypted".
 */
@Component
public class IdentityProviderSecretMigrationRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityProviderSecretMigrationRunner.class);
    private static final List<String> SENSITIVE_FIELDS = List.of(
        "clientSecret",
        "keystoreBase64",
        "keystorePassword",
        "privateKeyPassword"
    );

    private final MongoTemplate mongoTemplate;
    private final IdentityProviderRepository repository;
    private final EncryptedSecretCipher cipher;

    public IdentityProviderSecretMigrationRunner(
        MongoTemplate mongoTemplate,
        IdentityProviderRepository repository,
        EncryptedSecretCipher cipher
    ) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
        this.cipher = cipher;
    }

    @Override
    public void run(String... args) {
        if (!cipher.isEnabled()) {
            LOGGER.debug("Encryption is disabled (no iam.secrets.key) — skipping IdentityProvider secret migration");
            return;
        }
        List<Document> raws = mongoTemplate.find(new Query(), Document.class, "providers");
        int migrated = 0;
        int alreadyEncrypted = 0;
        for (Document raw : raws) {
            if (needsMigration(raw)) {
                String id = raw.getString("_id");
                repository
                    .findById(id)
                    .ifPresent(idp -> {
                        repository.save(idp);
                        LOGGER.info("IdP {} secrets migrated to encrypted storage", id);
                    });
                migrated++;
            } else {
                alreadyEncrypted++;
            }
        }
        if (migrated > 0) {
            LOGGER.info(
                "IdentityProvider secret migration: {} migrated, {} already encrypted, {} total",
                migrated,
                alreadyEncrypted,
                raws.size()
            );
        } else {
            LOGGER.debug("IdentityProvider secret migration: nothing to do ({} providers)", raws.size());
        }
    }

    private boolean needsMigration(Document raw) {
        for (String field : SENSITIVE_FIELDS) {
            Object value = raw.get(field);
            if (value instanceof String s && !s.isBlank() && !cipher.isEncrypted(s)) {
                return true;
            }
        }
        return false;
    }
}
