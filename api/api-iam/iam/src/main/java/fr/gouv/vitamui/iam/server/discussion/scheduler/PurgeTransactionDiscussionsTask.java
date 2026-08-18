/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.iam.server.discussion.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.collect.common.dto.TransactionDto;
import fr.gouv.vitam.collect.common.enums.TransactionStatus;
import fr.gouv.vitam.collect.external.exception.CollectExternalClientInvalidRequestException;
import fr.gouv.vitam.collect.external.exception.CollectExternalClientNotFoundException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.mongo.IdDocument;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.iam.server.discussion.dao.DiscussionReadRepository;
import fr.gouv.vitamui.iam.server.discussion.dao.DiscussionRepository;
import fr.gouv.vitamui.iam.server.discussion.domain.Discussion;
import fr.gouv.vitamui.iam.server.discussion.domain.DiscussionRead;
import fr.gouv.vitamui.iam.server.discussion.rest.EntityType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class PurgeTransactionDiscussionsTask {

    private final DiscussionRepository discussionRepository;
    private final DiscussionReadRepository discussionReadRepository;
    private final CollectService collectService;
    private final MongoTemplate mongoTemplate;

    @Value("${discussion.scheduling.purgeTransactionDiscussions.cronExpression}")
    private String cronExpression;

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeTransactionDiscussionsTask.class);

    public PurgeTransactionDiscussionsTask(
        final DiscussionRepository discussionRepository,
        final DiscussionReadRepository discussionReadRepository,
        final CollectService collectService,
        final MongoTemplate mongoTemplate
    ) {
        this.discussionRepository = discussionRepository;
        this.discussionReadRepository = discussionReadRepository;
        this.collectService = collectService;
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug(
            "purgeTransactionDiscussionsTask is running with cron expression {}, deleting discussions and their read status related to closed transactions",
            cronExpression
        );
    }

    @Scheduled(cron = "${discussion.scheduling.purgeTransactionDiscussions.cronExpression}")
    public void run() {
        LOGGER.debug("purgeTransactionDiscussions is started");

        deleteDiscussionsLinkedToClosedTransactions();

        deleteDiscussionReadOrphans();

        LOGGER.debug("purgeTransactionDiscussions is done");
    }

    private void deleteDiscussionsLinkedToClosedTransactions() {
        record TenantAndTransactionId(Integer tenant, String transactionId) {}

        final Map<TenantAndTransactionId, List<Discussion>> discussionsByTenantAndTransactionId = discussionRepository
            .findByEntitiesEntityType(EntityType.TRANSACTION)
            .collect(
                Collectors.groupingBy(
                    discussion -> {
                        final Integer tenant = discussion.getTenant();
                        final String transactionId = discussion
                            .getEntities()
                            .stream()
                            .filter(entity -> entity.getEntityType() == EntityType.TRANSACTION)
                            .findFirst()
                            .orElseThrow()
                            .getEntityId();
                        return new TenantAndTransactionId(tenant, transactionId);
                    },
                    Collectors.mapping(Function.identity(), Collectors.toList())
                )
            );

        LOGGER.debug("Found {} transactions with discussions", discussionsByTenantAndTransactionId.size());

        discussionsByTenantAndTransactionId.forEach((tenantAndTransactionId, discussions) -> {
            final Integer tenant = tenantAndTransactionId.tenant;
            final String transactionId = tenantAndTransactionId.transactionId;
            LOGGER.debug("Checking status of transaction {}", transactionId);

            final VitamContext vitamContext = new VitamContext(tenant);
            vitamContext.setApplicationSessionId(VitamUIUtils.generateRequestId());

            try {
                final RequestResponse<JsonNode> requestResponse = collectService.getTransactionById(
                    vitamContext,
                    transactionId
                );

                final TransactionDto transactionDto = JsonHandler.getFromString(
                    ((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    TransactionDto.class
                );

                final boolean discussionsShouldBeDeleted = Stream.of(
                    TransactionStatus.ACK_WARNING,
                    TransactionStatus.ACK_OK,
                    TransactionStatus.ABORTED
                )
                    .map(Enum::name)
                    .toList()
                    .contains(transactionDto.getStatus());

                LOGGER.debug("Transaction {} is in status {}", transactionId, transactionDto.getStatus());

                if (discussionsShouldBeDeleted) {
                    LOGGER.debug(
                        "Deleting {} discussions related to transaction {} in status {}",
                        discussions.size(),
                        transactionId,
                        transactionDto.getStatus()
                    );
                    discussionRepository.deleteAll(discussions);
                }
            } catch (CollectExternalClientNotFoundException | CollectExternalClientInvalidRequestException e) {
                // FIXME: Expected exception should be CollectExternalClientNotFoundException, but current API returns a 400 (instead of 404), throwing a CollectExternalClientInvalidRequestException instead. We temporarily catch also CollectExternalClientInvalidRequestException and check the code and message to handle "not found" with current API misbehaviour.
                if (
                    e instanceof CollectExternalClientNotFoundException ||
                    (e.getVitamError().getHttpCode() == 400 &&
                        e.getVitamError().getMessage().startsWith("No such transaction"))
                ) {
                    LOGGER.debug(
                        "Transaction {} does not exist. Deleting {} discussions related to that transaction",
                        transactionId,
                        discussions.size()
                    );
                    discussionRepository.deleteAll(discussions);
                }
            } catch (VitamClientException | InvalidParseOperationException e) {
                LOGGER.error("Couldn't retrieve transaction with id {} on tenant {}", transactionId, tenant, e);
            }
        });
    }

    private void deleteDiscussionReadOrphans() {
        final AggregationOperation lookupStage = context ->
            Document.parse(
                """
                {
                  "$lookup": {
                    "from": "discussions",
                    "let": { "discId": "$discussionId" },
                    "pipeline": [
                      { "$match": { "$expr": { "$eq": ["$_id", { "$toObjectId": "$$discId" }] } } }
                    ],
                    "as": "matches"
                  }
                }
                """
            );
        final Aggregation aggregation = Aggregation.newAggregation(
            lookupStage,
            Aggregation.match(Criteria.where("matches").size(0)),
            Aggregation.project("_id")
        );

        final List<String> discussionReadIdsToDelete = mongoTemplate
            .aggregateStream(aggregation, DiscussionRead.class, DiscussionRead.class)
            .map(IdDocument::getId)
            .toList();

        if (CollectionUtils.isNotEmpty(discussionReadIdsToDelete)) {
            LOGGER.debug("Found {} orphan discussions_read. Deleting them.", discussionReadIdsToDelete.size());
            discussionReadRepository.deleteAllById(discussionReadIdsToDelete);
        }
    }
}
