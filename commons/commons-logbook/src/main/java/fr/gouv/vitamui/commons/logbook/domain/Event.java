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
package fr.gouv.vitamui.commons.logbook.domain;

import java.time.OffsetDateTime;

import javax.validation.constraints.NotNull;

import fr.gouv.vitamui.commons.logbook.common.*;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitamui.commons.mongo.IdDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * Represent an event to send to logbook VITAM
 *
 *
 */
@Document(collection = MongoDbCollections.EVENTS)
@TypeAlias(MongoDbCollections.EVENTS)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Event extends IdDocument {

    /**
     * tenant Identifier
     */
    @NotNull
    private Integer tenantIdentifier;

    @NotNull
    private String accessContractLogbookIdentifier;

    /**
     *
     * Id of the principal operation/event
     */
    private String evParentId;

    /**
     * Unique identifier for the processus <br>
     * Operation identifier: unique identifier created through GUIDFactory.newOperationIdGUID(tenant)
     */
    private String evIdProc;

    /**
     * Represent the event Type
     * Exemple : CREATE_USER, CREATE_TENANT
     */
    @NotNull
    private String evType;

    /**
     * Represent the event Proc
     * Exemple : EXTERNE_VITAMUI
     */
    @NotNull
    private EventTypeProc evTypeProc;

    /**
     * Should use Vitam : StatusCode
     */
    @NotNull
    private StatusCode outcome;

    /**
     * Message output
     */
    @NotNull
    private String outMessg;

    /**
     * evType concate with outcome
     */
    @NotNull
    private String outDetail;

    /**
     * W-Request-Id from top request
     * X-Request-Id
     */
    @NotNull
    private String evIdReq;

    /**
     * Set by the Logbook service: date time of the creation of this object
     */
    @NotNull
    private String evDateTime;

    /**
     * Id of the data mongo object
     */
    @NotNull
    private String obId;

    /**
     * Name of the mongo Collections
     */
    @NotNull
    private String obIdReq;

    /**
     * Data of the event
     */
    @NotNull
    private String evDetData;

    /**
     * X-Application-Id
     */
    @NotNull
    private String evIdAppSession;

    /**
     * Date of the creation of this object in nano second
     */
    @NotNull
    private Long creationDate;

    /**
     * By default an event is initialize with a CREATED
     * If event is correctly send to vitam, then the status passed to SUCCESS,
     * otherwise the status is set to ERROR
     */
    @NotNull
    private EventStatus status;

    private String vitamResponse;

    /**
     * Date of the last sent to vitam
     */
    private OffsetDateTime synchronizedVitamDate;

}
