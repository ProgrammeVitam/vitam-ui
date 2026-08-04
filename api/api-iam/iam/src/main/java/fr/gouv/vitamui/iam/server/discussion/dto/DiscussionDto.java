package fr.gouv.vitamui.iam.server.discussion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.gouv.vitamui.iam.server.discussion.domain.Discussion;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * DTO for Discussion with Read Status.
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DiscussionDto {

    private Discussion discussion;
    private Instant lastReadAt;
    private boolean isUnread; // Helper flag for UI

    public DiscussionDto(Discussion discussion, Instant lastReadAt) {
        this.discussion = discussion;
        this.lastReadAt = lastReadAt;
        this.isUnread = calculateUnread(discussion, lastReadAt);
    }

    private boolean calculateUnread(Discussion discussion, Instant lastReadAt) {
        if (discussion.getLastMessageAt() == null) {
            return false;
        }
        if (lastReadAt == null) {
            return true;
        }
        return discussion.getLastMessageAt().isAfter(lastReadAt);
    }
}
