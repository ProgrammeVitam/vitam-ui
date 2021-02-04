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
package fr.gouv.vitamui.commons.logbook.service;

import java.time.OffsetDateTime;
import java.util.Collection;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.logbook.common.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.gouv.vitam.common.guid.GUID;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.logbook.scheduler.SendEventToVitamTasks;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;

/**
 * Service for CRUD operation on events Collection.
 *
 *
 */
public class EventService {

    private final EventRepository repository;

    private final EventMessages messages;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(EventService.class);

    @Autowired
    public EventService(final EventRepository repository, final EventMessages messages) {
        this.repository = repository;
        this.messages = messages;
    }

    /**
     * Use this method for create a event associated to a patch operation
     * Event status is OK by default. To use this method, you must be in a transaction.
     * @param context
     * @param collectionNames
     * @param obId
     * @param evType
     * @param evTypeProc
     * @param evDetData should be as JSON format
     * @param outcome
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Event logUpdate(final InternalHttpContext context, final String accessContractLogbookIdentifier,
                           final Integer tenantIdentifier, final String objectIdentifier, final String collectionNames,
                           final EventLogable evType, final Collection<EventDiffDto> evDetData) {
        LOGGER.info(" EvIdAppSession : {} " , context.getApplicationId());
        LOGGER.debug("------------- context : {}", context);
        return create(context.getRequestId(), context.getApplicationId(), accessContractLogbookIdentifier,
                tenantIdentifier, objectIdentifier, collectionNames, evType, EventTypeProc.EXTERNAL_LOGBOOK,
                LogbookUtils.getEvData(evDetData).toString(), StatusCode.OK);
    }

    /**
     * Use this method for create a event
     * Event status is OK by default. To use this method, you must be in a transaction.
     * <br>
     * This event is send to vitam by the process : {@link SendEventToVitamTasks}
     * <br>
     * @param context
     * @param obId
     * @param evType
     * @param evTypeProc
     * @param evDetData should be as JSON format
     * @param outcome
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Event logCreate(final InternalHttpContext context, final String accessContractLogbookIdentifier,
            final Integer tenantIdentifier, final String objectIdentifier, final String collectionNames,
            final EventLogable evType, final String evDetData) {
        LOGGER.debug("------------- context : {}", context);
        return create(context.getRequestId(), context.getApplicationId(), accessContractLogbookIdentifier,
                tenantIdentifier, objectIdentifier, collectionNames, evType, EventTypeProc.EXTERNAL_LOGBOOK, evDetData,
                StatusCode.OK);
    }

    /**
     * Log event for an export action.
     * Event status is OK by default.
     * @param context
     * @param accessContractLogbookIdentifier
     * @param tenantIdentifier
     * @param objectIdentifier
     * @param collectionNames
     * @param evType
     * @param evDetData
     * @return
     */
    public Event logAccess(final InternalHttpContext context, final String accessContractLogbookIdentifier,
            final Integer tenantIdentifier, final String objectIdentifier, final String collectionNames,
            final EventLogable evType, final String evDetData) {
        return create(context.getRequestId(), context.getApplicationId(), accessContractLogbookIdentifier,
                tenantIdentifier, objectIdentifier, collectionNames, evType, EventTypeProc.EXTERNAL_LOGBOOK, evDetData,
                StatusCode.OK);
    }

    private Event create(final String evIdReq, final String evIdAppSession,
            final String accessContractLogbookIdentifier, final Integer tenantIdentifier, final String obId,
            final String obIdReq, final EventLogable evType, final EventTypeProc evTypeProc, final String evDetData,
            final StatusCode outcome) {
        final Event event = new Event();
        final GUID guid = GUIDFactory.newOperationLogbookGUID(tenantIdentifier);
        event.setId(guid.getId());
        event.setStatus(EventStatus.CREATED);
        event.setTenantIdentifier(tenantIdentifier);
        event.setAccessContractLogbookIdentifier(accessContractLogbookIdentifier);
        event.setObId(obId);
        event.setObIdReq(obIdReq);
        event.setEvTypeProc(evTypeProc);
        if(evType != null) {
            event.setEvType(evType.toString());
        }else{
            throw new ApplicationServerException("Event type must not be null");
        }
        event.setEvDetData(evDetData);
        event.setOutcome(outcome);
        final String outDetail = "" + evType + "." + outcome;
        event.setOutDetail(outDetail);
        event.setOutMessg(messages.getOutMessg().get(outDetail));
        event.setEvIdReq(evIdReq);
        event.setEvIdAppSession(evIdAppSession);
        if (StringUtils.isBlank(event.getEvDateTime())) {
            event.setEvDateTime(OffsetDateTime.now().toString());
        }

        event.setCreationDate(System.nanoTime());
        return repository.save(event);
    }
}
