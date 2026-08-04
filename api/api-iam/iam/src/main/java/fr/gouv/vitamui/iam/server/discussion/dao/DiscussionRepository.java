package fr.gouv.vitamui.iam.server.discussion.dao;

import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;
import fr.gouv.vitamui.iam.server.discussion.domain.Discussion;
import fr.gouv.vitamui.iam.server.discussion.rest.EntityType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Discussions.
 */
@Repository
public interface DiscussionRepository extends VitamUIRepository<Discussion, String> {
    Optional<Discussion> findByTenantAndId(Integer tenant, String id);
    List<Discussion> findByTenantAndEntitiesEntityIdAndEntitiesEntityType(
        Integer tenant,
        String entityId,
        EntityType entityType
    );
}
