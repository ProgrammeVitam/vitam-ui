package fr.gouv.vitamui.iam.server.discussion.rest;

import fr.gouv.vitamui.iam.server.discussion.domain.Discussion;
import fr.gouv.vitamui.iam.server.discussion.dto.DiscussionDto;
import fr.gouv.vitamui.iam.server.discussion.service.DiscussionService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * Controller for Discussion API.
 */
@RestController
@RequestMapping("/iam/v1/discussions")
@Slf4j
public class DiscussionController {

    private final DiscussionService discussionService;

    private static final ServerSentEvent<Discussion> PING_EVENT = ServerSentEvent.<Discussion>builder()
        .comment("ping")
        .build();

    public DiscussionController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    @GetMapping
    public List<DiscussionDto> findDiscussions(@RequestParam String entityId, @RequestParam EntityType entityType) {
        return discussionService.findDiscussions(entityId, entityType);
    }

    @PostMapping
    public Discussion createDiscussion(@RequestBody CreateDiscussionRequest request) {
        return discussionService.createDiscussion(request.getTitle(), request.getText(), request.getEntities());
    }

    @PostMapping("/{discussionId}")
    public Discussion renameDiscussion(
        @PathVariable String discussionId,
        @RequestBody RenameDiscussionRequest request
    ) {
        return discussionService.renameDiscussion(discussionId, request.getTitle());
    }

    @PostMapping("/{discussionId}/messages")
    public Discussion addMessage(@PathVariable String discussionId, @RequestBody AddMessageRequest request) {
        return discussionService.addMessage(discussionId, request.getText());
    }

    @PostMapping("/{discussionId}/messages/{messageId}")
    public Discussion updateMessage(
        @PathVariable String discussionId,
        @PathVariable String messageId,
        @RequestBody AddMessageRequest request
    ) {
        return discussionService.updateMessage(discussionId, messageId, request.getText());
    }

    @DeleteMapping("/{discussionId}/messages/{messageId}")
    public Discussion deleteMessage(@PathVariable String discussionId, @PathVariable String messageId) {
        return discussionService.deleteMessage(discussionId, messageId);
    }

    @PutMapping("/{discussionId}/resolve")
    public void resolveDiscussion(@PathVariable String discussionId) {
        discussionService.resolveDiscussion(discussionId);
    }

    @PutMapping("/{discussionId}/read")
    public void markAsRead(@PathVariable String discussionId) {
        discussionService.markAsRead(discussionId);
    }

    @PutMapping("/{discussionId}/unread/{messageId}")
    public void markAsUnread(@PathVariable String discussionId, @PathVariable String messageId) {
        discussionService.markAsUnread(discussionId, messageId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Discussion>> streamDiscussions(
        @RequestParam String entityId,
        @RequestParam EntityType entityType
    ) {
        final Flux<ServerSentEvent<Discussion>> dataFlux = discussionService
            .stream(Discussion.EntityLink.builder().entityId(entityId).entityType(entityType).build())
            .map(discussion -> ServerSentEvent.builder(discussion).build());
        final Flux<ServerSentEvent<Discussion>> heartbeatFlux = Flux.interval(Duration.ofSeconds(15)).map(
            i -> PING_EVENT
        );
        return Flux.merge(dataFlux, heartbeatFlux)
            .doOnSubscribe(sub -> log.info("New SSE client connected for {}={}", entityType, entityId))
            .doOnCancel(() -> log.info("SSE client disconnected for {}={}", entityType, entityId))
            .doOnError(e -> log.warn("Error on SSE stream for {}={}: {}", entityType, entityId, e.getMessage()));
    }

    @Getter
    @Setter
    public static class CreateDiscussionRequest {

        private List<Discussion.EntityLink> entities;
        private String title;
        private String text;
    }

    @Getter
    @Setter
    public static class RenameDiscussionRequest {

        private String title;
    }

    @Getter
    @Setter
    public static class AddMessageRequest {

        private String text;
    }
}
