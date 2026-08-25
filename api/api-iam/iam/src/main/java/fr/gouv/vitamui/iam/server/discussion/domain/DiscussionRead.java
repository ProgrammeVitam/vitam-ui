package fr.gouv.vitamui.iam.server.discussion.domain;

import fr.gouv.vitamui.commons.mongo.IdDocument;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Discussion Read status document.
 */
@Document(collection = "discussions_read")
@CompoundIndex(name = "idx_discussions_read_user_discussion", def = "{'userId': 1, 'discussionId': 1}", unique = true)
@Getter
@Setter
@ToString
public class DiscussionRead extends IdDocument {

    private String userId;
    private String discussionId;
    private Instant lastReadAt;
}
