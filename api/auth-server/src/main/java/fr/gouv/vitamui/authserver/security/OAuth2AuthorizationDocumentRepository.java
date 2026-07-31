/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data repository for {@link OAuth2AuthorizationDocument}. Each finder mirrors one of the
 * token-typed lookups a SAS {@code OAuth2AuthorizationService} needs — the sparse indexes on the
 * various token value fields make these O(log n).
 */
public interface OAuth2AuthorizationDocumentRepository extends MongoRepository<OAuth2AuthorizationDocument, String> {

    Optional<OAuth2AuthorizationDocument> findByState(String state);

    Optional<OAuth2AuthorizationDocument> findByAuthorizationCodeValue(String value);

    Optional<OAuth2AuthorizationDocument> findByAccessTokenValue(String value);

    Optional<OAuth2AuthorizationDocument> findByOidcIdTokenValue(String value);

    Optional<OAuth2AuthorizationDocument> findByRefreshTokenValue(String value);

    Optional<OAuth2AuthorizationDocument> findByUserCodeValue(String value);

    Optional<OAuth2AuthorizationDocument> findByDeviceCodeValue(String value);

    /**
     * Fallback used when the caller doesn't know the token type — matches any of the six token slots
     * plus {@code state}. The sparse indexes keep this cheap in practice (Mongo picks whichever index
     * has a non-null hit).
     */
    @org.springframework.data.mongodb.repository.Query(
        "{ $or: [ " +
        "  { state: ?0 }, " +
        "  { authorizationCodeValue: ?0 }, " +
        "  { accessTokenValue: ?0 }, " +
        "  { oidcIdTokenValue: ?0 }, " +
        "  { refreshTokenValue: ?0 }, " +
        "  { userCodeValue: ?0 }, " +
        "  { deviceCodeValue: ?0 } " +
        "] }"
    )
    Optional<OAuth2AuthorizationDocument> findByAnyTokenValue(String value);
}
