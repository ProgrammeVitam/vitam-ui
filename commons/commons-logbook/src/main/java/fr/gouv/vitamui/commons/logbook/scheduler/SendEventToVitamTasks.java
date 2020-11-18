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
package fr.gouv.vitamui.commons.logbook.scheduler;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.logbook.common.parameters.LogbookOperationParameters;
import fr.gouv.vitam.logbook.common.parameters.LogbookParameterName;
import fr.gouv.vitam.logbook.common.parameters.LogbookParameters;
import fr.gouv.vitam.logbook.common.parameters.LogbookParametersFactory;
import fr.gouv.vitam.logbook.common.parameters.LogbookTypeProcess;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.common.EventStatus;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Task for send to vitam vitamui's events
 * <br>
 * Events are transform for be compatbile with vitam's logbook operation
 */
@Getter
@Setter
public class SendEventToVitamTasks {

    private static final String EVENT_KEY_STATUS = "status";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SendEventToVitamTasks.class);

    private static final String EVENT_DATE_TIME_KEY = "Date d'opération";

    /**
     * By default the scheduler working every minutes
     */
    @Value("${logbook.scheduling.sendEventToVitamTasks.delay}")
    private Long delay;

    @Value("${logbook.scheduling.sendEventToVitamTasks.delay.retryErrorEventInMinutes:60}")
    private Long retryErrorEventInMinutes;

    private final EventRepository eventRepository;

    private final AdminExternalClient adminExternalClient;

    public SendEventToVitamTasks(final EventRepository eventRepository, final AdminExternalClient adminExternalClient) {
        this.eventRepository = eventRepository;
        this.adminExternalClient = adminExternalClient;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("sendEventToVitamTasks is running with a delay of {} ms ", delay);
    }

    @Scheduled(fixedDelayString = "${logbook.scheduling.sendEventToVitamTasks.delay}")
    public void run() {
        LOGGER.debug("sendEventToVitamTasks is started");
        // Retrieve all events who are not already send to vitam or in error status
        final List<Event> events = getEventsElligibleToBeSentToVitam();
        final Map<String, TreeSet<Event>> eventsToSend = new LinkedHashMap<>();
        final Comparator<Event> byPersistedDate = (final Event e1, final Event e2) -> e1.getCreationDate().compareTo(e2.getCreationDate());
        // We stack together event by X-Request-Id
        // The first Event is the 'Master' event and the others are sub-event
        for (final Event e : events) {
            final TreeSet<Event> eventsSet = eventsToSend.getOrDefault(e.getEvIdReq(), new TreeSet<>(byPersistedDate));
            eventsSet.add(e);
            eventsToSend.putIfAbsent(e.getEvIdReq(), eventsSet);
        }

        // SEND TO VITAM
        for (final Entry<String, TreeSet<Event>> evts : eventsToSend.entrySet()) {
            try {
                sendToVitam(evts.getValue());
            } catch (final Exception e) {
                LOGGER.error("Failed to send events to vitam : {}", evts, e);
                LOGGER.error(e.getMessage(), e);

            }
        }
        LOGGER.debug("sendEventToVitamTasks is done");
    }

    protected List<Event> getEventsElligibleToBeSentToVitam() {
        final Criteria criteriaStatusCreated = Criteria.where(EVENT_KEY_STATUS).is(EventStatus.CREATED);
        final Criteria criteriaStatusError = Criteria.where(EVENT_KEY_STATUS).is(EventStatus.ERROR).and("synchronizedVitamDate")
                .lte(OffsetDateTime.now().minusMinutes(retryErrorEventInMinutes));
        final CriteriaDefinition criteria = new Criteria().orOperator(criteriaStatusCreated, criteriaStatusError);
        final Query query = Query.query(criteria);
        final Sort sort = Sort.by(Direction.ASC, "creationDate");
        return eventRepository.findAll(query.with(sort));
    }

    /**
     * Method in charged to send to vitam
     *
     * @param events
     * @throws InvalidParseOperationException
     */
    protected void sendToVitam(final TreeSet<Event> events) throws InvalidParseOperationException {
        LOGGER.trace("Events to send : {}", events);
        final Event eventParent = events.first();
        final Integer tenantIdentifier = events.first().getTenantIdentifier();
        eventParent.setEvIdProc(eventParent.getId());
        final String evParentId = eventParent.getId();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier);
        vitamContext.setApplicationSessionId(VitamUIUtils.generateRequestId());
        final boolean hasSubEvent = CollectionUtils.isNotEmpty(events) && events.size() > 1;
        if (hasSubEvent) {
            events.forEach(ev -> {
                if (!ev.getId().equals(evParentId)) {
                    ev.setEvIdProc(evParentId);
                }
            });
        }

        RequestResponse<?> response = null;
        boolean hasError = false;
        LogbookOperationParameters logbookOperationParams = null;
        try {
            logbookOperationParams = convertEventToMaster(eventParent);

            final Set<LogbookParameters> subEvents = new LinkedHashSet<>();

            if (hasSubEvent) {
                for (final Event ev : events) {
                    if (!ev.getId().equals(evParentId)) {
                        subEvents.add(convertEventToLogbookOperationParams(ev));
                    }
                }
                logbookOperationParams.setEvents(subEvents);
            }

            subEvents.add(convertEventToLogbookOperationParams(eventParent));
            logbookOperationParams.setEvents(subEvents);
            final Long start = System.currentTimeMillis();
            LOGGER.trace("Send to vitam ...");
            response = adminExternalClient.createExternalOperation(vitamContext, logbookOperationParams);
            LOGGER.trace("Send to vitam in {} ms", System.currentTimeMillis() - start);
            final int httpCode = response.getStatus();
            if (Status.CREATED.getStatusCode() == httpCode) {
                LOGGER.trace("Event :{} send with success to vitam, httpCode :{}", logbookOperationParams, httpCode);
            } else {
                hasError = true;
                LOGGER.error("Failed to create events {}, reponse: {}", logbookOperationParams, response);
            }

        } catch (final Exception e) {
            hasError = true;
            if (response != null && response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                LOGGER.warn("Event already send to vitam", e);
            } else {
                LOGGER.error("Failed to send event {} to vitam", logbookOperationParams, e);
            }

        } finally {
            final EventStatus status = hasError ? EventStatus.ERROR : EventStatus.SUCCESS;
            updateEventStatus(events, status, response != null ? response.toString() : "");
        }

    }

    /**
     * @param events
     * @param status
     * @param vitamResponse
     */
    protected void updateEventStatus(final TreeSet<Event> events, final EventStatus status, final String vitamResponse) {
        final Collection<String> ids = events.stream().map(e -> e.getId()).collect(Collectors.toList());
        final Query query = new Query(Criteria.where("id").in(ids));
        final Update update = new Update();
        update.set(EVENT_KEY_STATUS, status);
        update.set("vitamResponse", vitamResponse);
        update.set("synchronizedVitamDate", OffsetDateTime.now());
        eventRepository.updateMulti(query, update);
    }

    /**
     * Method for converting eventParent as Master event
     *
     * @param event
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    protected LogbookOperationParameters convertEventToMaster(final Event event) throws IllegalArgumentException, IOException {
        final LogbookOperationParameters logbookOperationParameters = LogbookParametersFactory.newLogbookOperationParameters();

        logbookOperationParameters.putParameterValue(LogbookParameterName.eventIdentifier, event.getId())
            .putParameterValue(LogbookParameterName.eventType, event.getEvType().toString())
            .putParameterValue(LogbookParameterName.eventIdentifierProcess, event.getEvIdProc())
            .setTypeProcess(LogbookTypeProcess.getLogbookTypeProcess(event.getEvTypeProc().toString()))
            .setStatus(StatusCode.OK)
            .putParameterValue(LogbookParameterName.outcomeDetailMessage, event.getOutMessg())
            .putParameterValue(LogbookParameterName.agentIdentifierApplicationSession, event.getEvIdAppSession())
            .putParameterValue(LogbookParameterName.eventIdentifierRequest, event.getEvIdReq());

        return logbookOperationParameters;
    }

    /**
     * @param event
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    protected LogbookOperationParameters convertEventToLogbookOperationParams(final Event event)
            throws IllegalArgumentException, IOException {
        final LogbookOperationParameters logbookOperationParameters = LogbookParametersFactory.newLogbookOperationParameters();

        logbookOperationParameters.putParameterValue(LogbookParameterName.eventIdentifier, event.getId())
            .putParameterValue(LogbookParameterName.eventType, event.getEvType().toString())
            .putParameterValue(LogbookParameterName.eventIdentifierProcess, event.getEvIdProc())
            .setTypeProcess(LogbookTypeProcess.getLogbookTypeProcess(event.getEvTypeProc().toString()))
            .setStatus(event.getOutcome())
            .putParameterValue(LogbookParameterName.outcome, event.getOutcome().toString())
            .putParameterValue(LogbookParameterName.outcomeDetail, event.getOutDetail())
            .putParameterValue(LogbookParameterName.outcomeDetailMessage, event.getOutMessg())
            .putParameterValue(LogbookParameterName.eventDetailData,
                addDateInformation(event.getEvDetData(), event.getEvDateTime()))
            .putParameterValue(LogbookParameterName.eventDateTime, event.getEvDateTime())
            .putParameterValue(LogbookParameterName.objectIdentifier, event.getObId())
            .putParameterValue(LogbookParameterName.eventIdentifierRequest, event.getEvIdReq())
            .putParameterValue(LogbookParameterName.objectIdentifierRequest, event.getObIdReq());
        return logbookOperationParameters;
    }

    protected String addDateInformation(final String evDetData, final String evDateTime) throws IOException {
        Assert.isTrue(evDetData != null, "evDetData should not be null");
        Assert.isTrue(evDateTime != null, "evDateTime should not be null");
        try {
            JsonNode json = JsonUtils.readTree(evDetData);
            if (json == null || json.isMissingNode()) {
                json = JsonUtils.readTree("{}");
            }

            ((ObjectNode) json).put(EVENT_DATE_TIME_KEY, evDateTime);
            return ApiUtils.toJson(json);
        } catch (final IOException e) {
            LOGGER.error("cann't convert {} to json node ", evDetData, e);
            throw e;
        }

    }

}
