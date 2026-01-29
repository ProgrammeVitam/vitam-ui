package fr.gouv.vitamui.iam.server.discussion.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitamui.commons.mongo.IdDocument;
import fr.gouv.vitamui.iam.server.discussion.rest.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Discussion document.
 */
@Document(collection = "discussions")
@Getter
@Setter
@ToString
public class Discussion extends IdDocument {

    @JsonIgnore
    private Integer tenant;

    private List<EntityLink> entities = new ArrayList<>();
    private String title;
    private StatusEnum status;
    private Instant createdAt;
    private Instant lastMessageAt;
    private List<Message> messages = new ArrayList<>();

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EntityLink {

        private String entityId;
        private EntityType entityType;
    }

    @Getter
    @Setter
    @ToString
    public static class Message {

        private String id;
        private String userId;
        private String userName;
        private String text;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant deletedAt;
    }

    public enum StatusEnum {
        IN_PROGRESS,
        RESOLVED,
    }
}
