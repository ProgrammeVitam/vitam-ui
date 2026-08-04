package fr.gouv.vitamui.iam.server.discussion.service;

import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.discussion.dao.DiscussionReadRepository;
import fr.gouv.vitamui.iam.server.discussion.dao.DiscussionRepository;
import fr.gouv.vitamui.iam.server.discussion.domain.Discussion;
import fr.gouv.vitamui.iam.server.discussion.domain.DiscussionRead;
import fr.gouv.vitamui.iam.server.discussion.rest.EntityType;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveChangeStreamOperation;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscussionServiceTest {

    private static final Integer TENANT = 42;
    private static final String DISCUSSION_ID = "discussion-id";
    private static final String MESSAGE_ID = "message-id";
    private static final String USER_ID = "user-id";

    @Mock
    private DiscussionRepository discussionRepository;

    @Mock
    private DiscussionReadRepository discussionReadRepository;

    @Mock
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Mock
    private ReactiveChangeStreamOperation.ReactiveChangeStream<Discussion> changeStream;

    @Mock
    private SecurityService securityService;

    @Mock
    private UserRepository userRepository;

    private DiscussionService discussionService;

    @BeforeEach
    void setUp() {
        when(reactiveMongoTemplate.changeStream(Discussion.class)).thenReturn(changeStream);
        when(changeStream.watchCollection("discussions")).thenReturn(changeStream);
        when(changeStream.listen()).thenReturn(Flux.never());
        discussionService = new DiscussionService(
            discussionRepository,
            discussionReadRepository,
            reactiveMongoTemplate,
            securityService,
            userRepository
        );
    }

    @Test
    void createDiscussionRequiresEveryLinkedEntityRoleAndSetsCurrentTenant() {
        when(securityService.hasRole(EntityType.PROJECT.getRole())).thenReturn(true);
        when(securityService.hasRole(EntityType.TRANSACTION.getRole())).thenReturn(true);
        when(securityService.getTenantIdentifier()).thenReturn(TENANT);
        when(securityService.getUser()).thenReturn(authenticatedUser());
        when(discussionRepository.save(any(Discussion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        discussionService.createDiscussion(
            "Title",
            "Text",
            List.of(entity(EntityType.PROJECT, "project-id"), entity(EntityType.TRANSACTION, "transaction-id"))
        );

        ArgumentCaptor<Discussion> discussionCaptor = ArgumentCaptor.forClass(Discussion.class);
        verify(discussionRepository).save(discussionCaptor.capture());
        assertThat(discussionCaptor.getValue().getTenant()).isEqualTo(TENANT);
    }

    @Test
    void createDiscussionIsForbiddenWhenOneLinkedEntityRoleIsMissing() {
        when(securityService.hasRole(EntityType.PROJECT.getRole())).thenReturn(true);
        when(securityService.hasRole(EntityType.TRANSACTION.getRole())).thenReturn(false);

        assertThatThrownBy(
            () ->
                discussionService.createDiscussion(
                    "Title",
                    "Text",
                    List.of(entity(EntityType.PROJECT, "project-id"), entity(EntityType.TRANSACTION, "transaction-id"))
                )
        ).isInstanceOf(ForbiddenException.class);

        verify(discussionRepository, never()).save(any());
    }

    @Test
    void findDiscussionsRequiresRoleAndQueriesOnlyCurrentTenant() {
        when(securityService.hasRole(EntityType.TRANSACTION.getRole())).thenReturn(true);
        when(securityService.getTenantIdentifier()).thenReturn(TENANT);
        when(securityService.getUser()).thenReturn(authenticatedUser());
        when(
            discussionRepository.findByTenantAndEntitiesEntityIdAndEntitiesEntityType(
                TENANT,
                "transaction-id",
                EntityType.TRANSACTION
            )
        ).thenReturn(List.of());
        when(userRepository.findByIdIn(anySet())).thenReturn(List.of());
        when(discussionReadRepository.findByUserIdAndDiscussionIdIn(USER_ID, List.of())).thenReturn(List.of());

        assertThat(discussionService.findDiscussions("transaction-id", EntityType.TRANSACTION)).isEmpty();

        verify(discussionRepository).findByTenantAndEntitiesEntityIdAndEntitiesEntityType(
            TENANT,
            "transaction-id",
            EntityType.TRANSACTION
        );
    }

    @Test
    void findDiscussionsIsForbiddenWithoutEntityRole() {
        when(securityService.hasRole(EntityType.TRANSACTION.getRole())).thenReturn(false);

        assertThatThrownBy(
            () -> discussionService.findDiscussions("transaction-id", EntityType.TRANSACTION)
        ).isInstanceOf(ForbiddenException.class);

        verifyNoInteractions(discussionRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = { "rename", "add", "update", "delete", "resolve" })
    void modifyingDiscussionIsForbiddenWithoutAnyLinkedEntityRoleAndQueriesOnlyCurrentTenant(String operation) {
        when(securityService.getTenantIdentifier()).thenReturn(TENANT);
        when(discussionRepository.findByTenantAndId(TENANT, DISCUSSION_ID)).thenReturn(Optional.of(discussion()));
        when(securityService.hasRole(EntityType.TRANSACTION.getRole())).thenReturn(false);

        assertThatThrownBy(() -> invokeModification(operation)).isInstanceOf(ForbiddenException.class);

        verify(discussionRepository).findByTenantAndId(TENANT, DISCUSSION_ID);
        verify(discussionRepository, never()).save(any());
    }

    @Test
    void markAsReadUsesOnlyTheConnectedUsersReadStatus() {
        when(securityService.getUser()).thenReturn(authenticatedUser());
        when(discussionReadRepository.findByUserIdAndDiscussionId(USER_ID, DISCUSSION_ID)).thenReturn(Optional.empty());

        discussionService.markAsRead(DISCUSSION_ID);

        ArgumentCaptor<DiscussionRead> readCaptor = ArgumentCaptor.forClass(DiscussionRead.class);
        verify(discussionReadRepository).save(readCaptor.capture());
        assertThat(readCaptor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(readCaptor.getValue().getDiscussionId()).isEqualTo(DISCUSSION_ID);
        verifyNoInteractions(discussionRepository);
    }

    @Test
    void markAsUnreadChecksTenantAndLinkedEntityRoleBeforeUpdatingReadStatus() {
        when(securityService.getUser()).thenReturn(authenticatedUser());
        when(securityService.getTenantIdentifier()).thenReturn(TENANT);
        when(discussionReadRepository.findByUserIdAndDiscussionId(USER_ID, DISCUSSION_ID)).thenReturn(Optional.empty());
        when(discussionRepository.findByTenantAndId(TENANT, DISCUSSION_ID)).thenReturn(Optional.of(discussion()));
        when(securityService.hasRole(EntityType.TRANSACTION.getRole())).thenReturn(false);

        assertThatThrownBy(() -> discussionService.markAsUnread(DISCUSSION_ID, MESSAGE_ID)).isInstanceOf(
            ForbiddenException.class
        );

        verify(discussionRepository).findByTenantAndId(TENANT, DISCUSSION_ID);
        verify(discussionReadRepository, never()).save(any());
    }

    @Test
    void streamRequiresEntityRole() {
        when(securityService.getTenantIdentifier()).thenReturn(TENANT);
        when(securityService.hasRole(EntityType.TRANSACTION.getRole())).thenReturn(false);

        assertThatThrownBy(
            () -> discussionService.stream(entity(EntityType.TRANSACTION, "transaction-id"))
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void streamOnlyEmitsDiscussionsForCurrentTenantAndRequestedEntity() {
        Discussion matchingDiscussion = discussion(TENANT, entity(EntityType.TRANSACTION, "transaction-id"));
        Discussion otherTenantDiscussion = discussion(43, entity(EntityType.TRANSACTION, "transaction-id"));
        Discussion otherEntityDiscussion = discussion(TENANT, entity(EntityType.TRANSACTION, "another-transaction"));
        Discussion otherEntityTypeDiscussion = discussion(TENANT, entity(EntityType.PROJECT, "transaction-id"));
        ChangeStreamEvent<Discussion> otherTenantEvent = changeEvent(otherTenantDiscussion);
        ChangeStreamEvent<Discussion> otherEntityEvent = changeEvent(otherEntityDiscussion);
        ChangeStreamEvent<Discussion> otherEntityTypeEvent = changeEvent(otherEntityTypeDiscussion);
        ChangeStreamEvent<Discussion> matchingEvent = changeEvent(matchingDiscussion);
        when(changeStream.listen()).thenReturn(
            Flux.just(otherTenantEvent, otherEntityEvent, otherEntityTypeEvent, matchingEvent)
        );
        discussionService = new DiscussionService(
            discussionRepository,
            discussionReadRepository,
            reactiveMongoTemplate,
            securityService,
            userRepository
        );
        when(securityService.getTenantIdentifier()).thenReturn(TENANT);
        when(securityService.hasRole(EntityType.TRANSACTION.getRole())).thenReturn(true);

        assertThat(
            discussionService.stream(entity(EntityType.TRANSACTION, "transaction-id")).collectList().block()
        ).containsExactly(matchingDiscussion);
    }

    private void invokeModification(String operation) {
        switch (operation) {
            case "rename" -> discussionService.renameDiscussion(DISCUSSION_ID, "New title");
            case "add" -> discussionService.addMessage(DISCUSSION_ID, "Text");
            case "update" -> discussionService.updateMessage(DISCUSSION_ID, MESSAGE_ID, "Text");
            case "delete" -> discussionService.deleteMessage(DISCUSSION_ID, MESSAGE_ID);
            case "resolve" -> discussionService.resolveDiscussion(DISCUSSION_ID);
            default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }

    private Discussion discussion() {
        return discussion(TENANT, entity(EntityType.TRANSACTION, "transaction-id"));
    }

    private Discussion discussion(Integer tenant, Discussion.EntityLink entity) {
        Discussion discussion = new Discussion();
        discussion.setTenant(tenant);
        discussion.setEntities(List.of(entity));
        Discussion.Message message = new Discussion.Message();
        message.setId(MESSAGE_ID);
        message.setUserId(USER_ID);
        discussion.setMessages(List.of(message));
        return discussion;
    }

    private Discussion.EntityLink entity(EntityType entityType, String entityId) {
        return Discussion.EntityLink.builder().entityType(entityType).entityId(entityId).build();
    }

    @SuppressWarnings("unchecked")
    private ChangeStreamEvent<Discussion> changeEvent(Discussion discussion) {
        ChangeStreamEvent<Discussion> event = mock(ChangeStreamEvent.class);
        when(event.getBody()).thenReturn(discussion);
        return event;
    }

    private AuthUserDto authenticatedUser() {
        AuthUserDto user = new AuthUserDto();
        user.setId(USER_ID);
        return user;
    }
}
