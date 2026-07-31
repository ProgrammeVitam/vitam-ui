/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data repository for {@link RegisteredClientDocument}. Kept minimal — the two lookups mirror
 * the {@code RegisteredClientRepository} contract that {@link MongoRegisteredClientRepository} exposes
 * to Spring Authorization Server.
 */
public interface RegisteredClientDocumentRepository extends MongoRepository<RegisteredClientDocument, String> {
    Optional<RegisteredClientDocument> findByClientId(String clientId);
}
