/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data repository for {@link PasswordResetTokenDocument}. */
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetTokenDocument, String> {
    long deleteByEmailAndCustomerId(String email, String customerId);
}
