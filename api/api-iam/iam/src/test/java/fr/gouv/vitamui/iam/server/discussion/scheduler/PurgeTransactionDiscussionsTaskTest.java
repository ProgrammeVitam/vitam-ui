package fr.gouv.vitamui.iam.server.discussion.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.collect.common.dto.TransactionDto;
import fr.gouv.vitam.collect.common.enums.TransactionStatus;
import fr.gouv.vitam.collect.external.exception.CollectExternalClientInvalidRequestException;
import fr.gouv.vitam.collect.external.exception.CollectExternalClientNotFoundException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.error.VitamError;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.iam.server.discussion.dao.DiscussionReadRepository;
import fr.gouv.vitamui.iam.server.discussion.dao.DiscussionRepository;
import fr.gouv.vitamui.iam.server.discussion.domain.Discussion;
import fr.gouv.vitamui.iam.server.discussion.domain.DiscussionRead;
import fr.gouv.vitamui.iam.server.discussion.rest.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurgeTransactionDiscussionsTaskTest {

    private static final Integer TENANT = 42;
    private static final String TRANSACTION_ID = "transaction-id";
    private static final String OTHER_TRANSACTION_ID = "other-transaction-id";
    private static final String PROJECT_ID = "project-id";

    @Mock
    private DiscussionRepository discussionRepository;

    @Mock
    private DiscussionReadRepository discussionReadRepository;

    @Mock
    private CollectService collectService;

    @Mock
    private MongoTemplate mongoTemplate;

    @Captor
    private ArgumentCaptor<List<Discussion>> discussionsCaptor;

    @Captor
    private ArgumentCaptor<List<String>> discussionReadIdsCaptor;

    private PurgeTransactionDiscussionsTask task;

    @BeforeEach
    void setUp() {
        task = new PurgeTransactionDiscussionsTask(
            discussionRepository,
            discussionReadRepository,
            collectService,
            mongoTemplate
        );
    }

    // ------------------------------------------------------------------
    // Requirement 1: delete only TRANSACTION-linked discussions whose
    // transaction is in ACK_WARNING / ACK_OK / ABORTED status or no longer exists
    // ------------------------------------------------------------------

    @Test
    void runOnlyQueriesDiscussionsLinkedToTransactionEntityType() {
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of());
        when(
            mongoTemplate.aggregateStream(any(Aggregation.class), eq(DiscussionRead.class), eq(DiscussionRead.class))
        ).thenReturn(Stream.of());

        task.run();

        verify(discussionRepository).findByEntitiesEntityType(EntityType.TRANSACTION);
        verify(discussionRepository, never()).findByEntitiesEntityType(EntityType.PROJECT);
        verify(discussionReadRepository, never()).deleteAllById(any());
    }

    @Test
    void deletesDiscussionsWhenTransactionIsInAckOkStatus() throws Exception {
        final Discussion discussion = transactionDiscussion(TRANSACTION_ID);
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenReturn(
            transactionResponse(TransactionStatus.ACK_OK.name())
        );

        task.run();

        verify(discussionRepository).deleteAll(discussionsCaptor.capture());
        assertThat(discussionsCaptor.getValue()).containsExactly(discussion);
    }

    @Test
    void deletesDiscussionsWhenTransactionIsInAckWarningStatus() throws Exception {
        final Discussion discussion = transactionDiscussion(TRANSACTION_ID);
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenReturn(
            transactionResponse(TransactionStatus.ACK_WARNING.name())
        );

        task.run();

        verify(discussionRepository).deleteAll(discussionsCaptor.capture());
        assertThat(discussionsCaptor.getValue()).containsExactly(discussion);
    }

    @Test
    void deletesDiscussionsWhenTransactionIsInAbortedStatus() throws Exception {
        final Discussion discussion = transactionDiscussion(TRANSACTION_ID);
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenReturn(
            transactionResponse(TransactionStatus.ABORTED.name())
        );

        task.run();

        verify(discussionRepository).deleteAll(discussionsCaptor.capture());
        assertThat(discussionsCaptor.getValue()).containsExactly(discussion);
    }

    @Test
    void doesNotDeleteDiscussionsWhenTransactionIsStillOpen() throws Exception {
        final Discussion discussion = transactionDiscussion(TRANSACTION_ID);
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenReturn(
            transactionResponse(TransactionStatus.OPEN.name())
        );

        task.run();

        verify(discussionRepository, never()).deleteAll(any());
    }

    @Test
    void doesNotDeleteDiscussionsWhenTransactionIsInTransientStatus() throws Exception {
        final Discussion discussion = transactionDiscussion(TRANSACTION_ID);
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenReturn(
            transactionResponse(TransactionStatus.SENDING.name())
        );

        task.run();

        verify(discussionRepository, never()).deleteAll(any());
    }

    @Test
    void deletesDiscussionsWhenTransactionNoLongerExistsViaNotFoundException() throws Exception {
        final Discussion discussion = transactionDiscussion(TRANSACTION_ID);
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenThrow(
            new CollectExternalClientNotFoundException("No such transaction")
        );

        task.run();

        verify(discussionRepository).deleteAll(discussionsCaptor.capture());
        assertThat(discussionsCaptor.getValue()).containsExactly(discussion);
    }

    @Test
    void doesNotDeleteDiscussionsWhenInvalidRequestExceptionIsNotANotFound() throws Exception {
        final Discussion discussion = transactionDiscussion(TRANSACTION_ID);
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenThrow(
            new CollectExternalClientInvalidRequestException(
                "bad request",
                new VitamError<>("0").setHttpCode(400).setMessage("Some unrelated error")
            )
        );

        task.run();

        verify(discussionRepository, never()).deleteAll(any());
    }

    @Test
    void doesNotDeleteDiscussionsWhenTransactionRetrievalFailsWithClientException() throws Exception {
        final Discussion discussion = transactionDiscussion(TRANSACTION_ID);
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenThrow(
            new VitamClientException("connection error")
        );

        task.run();

        verify(discussionRepository, never()).deleteAll(any());
    }

    @Test
    void usesTransactionEntityIdEvenWhenDiscussionLinksSeveralEntities() throws Exception {
        final Discussion discussion = new Discussion();
        discussion.setId("discussion-id");
        discussion.setTenant(TENANT);
        discussion.setEntities(
            List.of(
                Discussion.EntityLink.builder().entityType(EntityType.PROJECT).entityId(PROJECT_ID).build(),
                Discussion.EntityLink.builder().entityType(EntityType.TRANSACTION).entityId(TRANSACTION_ID).build()
            )
        );
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(Stream.of(discussion));
        when(collectService.getTransactionById(any(VitamContext.class), eq(TRANSACTION_ID))).thenReturn(
            transactionResponse(TransactionStatus.ACK_OK.name())
        );

        task.run();

        verify(collectService).getTransactionById(any(VitamContext.class), eq(TRANSACTION_ID));
        verify(collectService, never()).getTransactionById(any(VitamContext.class), eq(PROJECT_ID));
        verify(discussionRepository).deleteAll(discussionsCaptor.capture());
        assertThat(discussionsCaptor.getValue()).containsExactly(discussion);
    }

    @Test
    void deletesOnlyClosedTransactionDiscussionsAmongMixedStatuses() throws Exception {
        final Discussion ackOk = transactionDiscussion(TRANSACTION_ID);
        final Discussion aborted = transactionDiscussion(OTHER_TRANSACTION_ID);
        final Discussion open = transactionDiscussion("open-transaction-id");
        when(discussionRepository.findByEntitiesEntityType(EntityType.TRANSACTION)).thenReturn(
            Stream.of(ackOk, aborted, open)
        );
        when(collectService.getTransactionById(any(VitamContext.class), anyString())).thenAnswer(invocation -> {
            final String transactionId = invocation.getArgument(1);
            final String status =
                switch (transactionId) {
                    case TRANSACTION_ID -> TransactionStatus.ACK_OK.name();
                    case OTHER_TRANSACTION_ID -> TransactionStatus.ABORTED.name();
                    default -> TransactionStatus.OPEN.name();
                };
            return transactionResponse(status);
        });
        when(
            mongoTemplate.aggregateStream(any(Aggregation.class), eq(DiscussionRead.class), eq(DiscussionRead.class))
        ).thenReturn(Stream.of());

        task.run();

        verify(discussionRepository, org.mockito.Mockito.times(2)).deleteAll(discussionsCaptor.capture());
        assertThat(discussionsCaptor.getAllValues()).containsExactlyInAnyOrder(List.of(ackOk), List.of(aborted));
    }

    // ------------------------------------------------------------------
    // Requirement 2: delete every discussion_read no longer linked to an
    // existing discussion
    // ------------------------------------------------------------------

    @Test
    void deletesOrphanDiscussionReads() {
        final DiscussionRead orphan1 = new DiscussionRead();
        orphan1.setId("read-1");
        final DiscussionRead orphan2 = new DiscussionRead();
        orphan2.setId("read-2");
        when(
            mongoTemplate.aggregateStream(any(Aggregation.class), eq(DiscussionRead.class), eq(DiscussionRead.class))
        ).thenReturn(Stream.of(orphan1, orphan2));

        task.run();

        verify(discussionReadRepository).deleteAllById(discussionReadIdsCaptor.capture());
        assertThat(discussionReadIdsCaptor.getValue()).containsExactly("read-1", "read-2");
    }

    @Test
    void doesNotDeleteAnythingWhenThereIsNoOrphanDiscussionRead() {
        when(
            mongoTemplate.aggregateStream(any(Aggregation.class), eq(DiscussionRead.class), eq(DiscussionRead.class))
        ).thenReturn(Stream.of());

        task.run();

        verify(discussionReadRepository, never()).deleteAllById(any());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Discussion transactionDiscussion(final String transactionId) {
        final Discussion discussion = new Discussion();
        discussion.setId("discussion-" + transactionId);
        discussion.setTenant(TENANT);
        discussion.setEntities(
            List.of(Discussion.EntityLink.builder().entityType(EntityType.TRANSACTION).entityId(transactionId).build())
        );
        return discussion;
    }

    private RequestResponse<JsonNode> transactionResponse(final String status) throws InvalidParseOperationException {
        final TransactionDto transactionDto = new TransactionDto();
        transactionDto.setId(TRANSACTION_ID);
        transactionDto.setStatus(status);
        final RequestResponseOK<JsonNode> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.addResult(JsonHandler.toJsonNode(transactionDto));
        return response;
    }
}
