package fr.gouv.vitamui.iam.server.discussion.dao;

import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;
import fr.gouv.vitamui.iam.server.discussion.domain.DiscussionRead;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DiscussionRead.
 */
@Repository
public interface DiscussionReadRepository extends VitamUIRepository<DiscussionRead, String> {
    Optional<DiscussionRead> findByUserIdAndDiscussionId(String userId, String discussionId);

    List<DiscussionRead> findByUserIdAndDiscussionIdIn(String userId, List<String> discussionIds);
}
