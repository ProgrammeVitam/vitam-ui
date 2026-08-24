package fr.gouv.vitamui.iam.server.discussion.service;

import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.discussion.dao.DiscussionReadRepository;
import fr.gouv.vitamui.iam.server.discussion.dao.DiscussionRepository;
import fr.gouv.vitamui.iam.server.discussion.domain.Discussion;
import fr.gouv.vitamui.iam.server.discussion.domain.Discussion.StatusEnum;
import fr.gouv.vitamui.iam.server.discussion.domain.DiscussionRead;
import fr.gouv.vitamui.iam.server.discussion.dto.DiscussionDto;
import fr.gouv.vitamui.iam.server.discussion.rest.EntityType;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for Discussion Management.
 */
@Service
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final DiscussionReadRepository discussionReadRepository;
    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final Flux<Discussion> discussionChangeStream;

    public DiscussionService(
        DiscussionRepository discussionRepository,
        DiscussionReadRepository discussionReadRepository,
        @Qualifier("discussionReactiveMongoTemplate") ReactiveMongoTemplate reactiveMongoTemplate,
        SecurityService securityService,
        UserRepository userRepository
    ) {
        this.discussionRepository = discussionRepository;
        this.discussionReadRepository = discussionReadRepository;
        this.securityService = securityService;
        this.userRepository = userRepository;

        this.discussionChangeStream = reactiveMongoTemplate
            .changeStream(Discussion.class)
            .watchCollection("discussions")
            .listen()
            .mapNotNull(ChangeStreamEvent::getBody)
            .publishOn(Schedulers.boundedElastic())
            .map(discussion -> {
                final Set<String> userIds = discussion
                    .getMessages()
                    .stream()
                    .map(Discussion.Message::getUserId)
                    .collect(Collectors.toSet());
                final Map<String, String> userNamesById = userRepository
                    .findByIdIn(userIds)
                    .stream()
                    .collect(Collectors.toMap(User::getId, user -> user.getFirstname() + " " + user.getLastname()));
                discussion
                    .getMessages()
                    .forEach(message -> message.setUserName(userNamesById.getOrDefault(message.getUserId(), null)));
                return discussion;
            });
    }

    public Flux<Discussion> stream(Discussion.EntityLink entityLink) {
        final Integer tenantIdentifier = securityService.getTenantIdentifier();
        assertHasPermission(entityLink.getEntityType());
        return discussionChangeStream
            .filter(discussion -> tenantIdentifier.equals(discussion.getTenant()))
            .filter(
                discussion ->
                    discussion
                        .getEntities()
                        .stream()
                        .anyMatch(
                            entity ->
                                entityLink.getEntityId().equals(entity.getEntityId()) &&
                                entityLink.getEntityType().equals(entity.getEntityType())
                        )
            );
    }

    public Discussion createDiscussion(String title, String text, List<Discussion.EntityLink> entities) {
        // Connected user must have permission on every linked entity type when creating a new discussion
        entities.stream().map(Discussion.EntityLink::getEntityType).forEach(this::assertHasPermission);

        final Discussion discussion = new Discussion();
        discussion.setTenant(securityService.getTenantIdentifier());
        discussion.setEntities(entities);
        discussion.setTitle(title);

        final Discussion.Message message = createMessage(text);
        discussion.setMessages(List.of(message));

        discussion.setStatus(StatusEnum.IN_PROGRESS);

        final Instant now = Instant.now();
        discussion.setCreatedAt(now);
        discussion.setLastMessageAt(now);
        return discussionRepository.save(discussion);
    }

    public Discussion renameDiscussion(String discussionId, String title) {
        final Discussion discussion = findDiscussionById(discussionId);

        discussion.setTitle(title);

        return discussionRepository.save(discussion);
    }

    public Discussion addMessage(String discussionId, String text) {
        final Discussion discussion = findDiscussionById(discussionId);

        final Discussion.Message message = createMessage(text);
        discussion.getMessages().add(message);
        discussion.setLastMessageAt(message.getCreatedAt());
        discussion.setStatus(StatusEnum.IN_PROGRESS);

        final Discussion save = discussionRepository.save(discussion);
        markAsRead(discussionId);
        return save;
    }

    public Discussion updateMessage(String discussionId, String messageId, String text) {
        final Discussion discussion = findDiscussionById(discussionId);

        final Discussion.Message message = findMessageById(discussion, messageId);

        message.setText(text);
        message.setUpdatedAt(Instant.now());

        return discussionRepository.save(discussion);
    }

    public Discussion deleteMessage(String discussionId, String messageId) {
        final Discussion discussion = findDiscussionById(discussionId);

        final Discussion.Message message = findMessageById(discussion, messageId);

        message.setText(null);
        message.setDeletedAt(Instant.now());

        return discussionRepository.save(discussion);
    }

    public void resolveDiscussion(String discussionId) {
        final Discussion discussion = findDiscussionById(discussionId);
        discussion.setStatus(StatusEnum.RESOLVED);
        discussionRepository.save(discussion);
    }

    public List<DiscussionDto> findDiscussions(String entityId, EntityType entityType) {
        assertHasPermission(entityType);

        final List<Discussion> discussions = discussionRepository.findByTenantAndEntitiesEntityIdAndEntitiesEntityType(
            securityService.getTenantIdentifier(),
            entityId,
            entityType
        );

        final String userId = securityService.getUser().getId();

        final Set<String> userIds = discussions
            .stream()
            .map(Discussion::getMessages)
            .flatMap(List::stream)
            .map(Discussion.Message::getUserId)
            .collect(Collectors.toSet());
        final Map<String, String> userNamesById = userRepository
            .findByIdIn(userIds)
            .stream()
            .collect(Collectors.toMap(User::getId, user -> user.getFirstname() + " " + user.getLastname()));

        final List<String> discussionIds = discussions.stream().map(Discussion::getId).collect(Collectors.toList());
        final Map<String, DiscussionRead> readStatusMap = discussionReadRepository
            .findByUserIdAndDiscussionIdIn(userId, discussionIds)
            .stream()
            .collect(Collectors.toMap(DiscussionRead::getDiscussionId, Function.identity()));

        return discussions
            .stream()
            .map(discussion -> {
                discussion
                    .getMessages()
                    .forEach(message -> message.setUserName(userNamesById.getOrDefault(message.getUserId(), null)));

                final DiscussionRead read = readStatusMap.get(discussion.getId());
                final Instant lastReadAt = read != null ? read.getLastReadAt() : null;
                return new DiscussionDto(discussion, lastReadAt);
            })
            .collect(Collectors.toList());
    }

    private void assertHasPermission(EntityType entityType) {
        if (!hasPermission(entityType)) {
            throw new ForbiddenException(String.format("User is missing role %s", entityType.getRole()));
        }
    }

    private boolean hasPermission(EntityType entityType) {
        return securityService.hasRole(entityType.getRole());
    }

    public void markAsRead(String discussionId) {
        String userId = securityService.getUser().getId();
        DiscussionRead discussionRead = discussionReadRepository
            .findByUserIdAndDiscussionId(userId, discussionId)
            .orElseGet(() -> {
                DiscussionRead newRead = new DiscussionRead();
                newRead.setUserId(userId);
                newRead.setDiscussionId(discussionId);
                return newRead;
            });
        discussionRead.setLastReadAt(Instant.now());
        discussionReadRepository.save(discussionRead);
    }

    public void markAsUnread(String discussionId, String messageId) {
        String userId = securityService.getUser().getId();
        DiscussionRead discussionRead = discussionReadRepository
            .findByUserIdAndDiscussionId(userId, discussionId)
            .orElseGet(() -> {
                DiscussionRead newRead = new DiscussionRead();
                newRead.setUserId(userId);
                newRead.setDiscussionId(discussionId);
                return newRead;
            });

        final Discussion discussion = findDiscussionById(discussionId);
        final Discussion.Message message = findMessageByIdNoUserCheckSecurity(discussion, messageId);

        discussionRead.setLastReadAt(message.getCreatedAt().minusMillis(1));
        discussionReadRepository.save(discussionRead);
    }

    private @NonNull Discussion findDiscussionById(String discussionId) {
        final Discussion discussion = discussionRepository
            .findByTenantAndId(securityService.getTenantIdentifier(), discussionId)
            .orElseThrow(() -> new IllegalArgumentException("Discussion not found"));

        // Connected user must have permission on at least one linked entity type
        final Set<EntityType> entityTypes = discussion
            .getEntities()
            .stream()
            .map(Discussion.EntityLink::getEntityType)
            .collect(Collectors.toSet());
        if (entityTypes.stream().noneMatch(this::hasPermission)) {
            throw new ForbiddenException(
                String.format(
                    "User is missing a role matching discussion linked entities: %s",
                    StringUtils.joinWith(
                        ", ",
                        entityTypes.stream().map(EntityType::getRole).collect(Collectors.toSet())
                    )
                )
            );
        }

        return discussion;
    }

    private Discussion.@NonNull Message findMessageById(Discussion discussion, String messageId) {
        String userId = securityService.getUser().getId();
        return findMessageById(
            discussion,
            messageId,
            (Discussion.Message message) -> userId.equals(message.getUserId())
        );
    }

    private Discussion.@NonNull Message findMessageByIdNoUserCheckSecurity(Discussion discussion, String messageId) {
        return findMessageById(discussion, messageId, (Discussion.Message message) -> true);
    }

    private Discussion.@NonNull Message findMessageById(
        Discussion discussion,
        String messageId,
        Function<Discussion.Message, Boolean> securityFunction
    ) {
        return discussion
            .getMessages()
            .stream()
            .filter(m -> messageId.equals(m.getId()) && securityFunction.apply(m))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Message not found"));
    }

    private Discussion.@NonNull Message createMessage(String text) {
        String userId = securityService.getUser().getId();

        Discussion.Message message = new Discussion.Message();
        message.setId(UUID.randomUUID().toString());
        message.setUserId(userId);
        message.setText(text);
        message.setCreatedAt(Instant.now());
        return message;
    }
}
