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
package fr.gouv.vitamui.commons.vitam.api.access;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientServerException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.CompareQuery;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.AccessUnauthorizedException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.external.client.IngestCollection;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.logbook.LogbookLifecycle;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.enums.ContentDispositionType;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import javax.ws.rs.core.Response;
import java.util.Arrays;

import static fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils.responseMapping;

public class LogbookService {

    @SuppressWarnings("unused")
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(LogbookService.class);

    private static final String INGEST_TYPE = "INGEST";

    private final AccessExternalClient accessExternalClient;

    private final IngestExternalClient ingestExternalClient;

    private final AdminExternalClient adminExternalClient;

    @Autowired
    public LogbookService(final AccessExternalClient accessExternalClient, final IngestExternalClient ingestExternalClient, final AdminExternalClient adminExternalClient) {
        this.accessExternalClient = accessExternalClient;
        this.ingestExternalClient = ingestExternalClient;
        this.adminExternalClient = adminExternalClient;
    }

    /**
     * Gets an operation by id.
     * @param operationId
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<LogbookOperation> selectOperationbyId(final String operationId, final VitamContext vitamContext) throws VitamClientException {
        final RequestResponse<LogbookOperation> response = accessExternalClient.selectOperationbyId(vitamContext, operationId,
                new Select().getFinalSelectById());
        VitamRestUtils.checkResponse(response, HttpStatus.SC_OK, HttpStatus.SC_ACCEPTED);
        return response;
    }

    /**
     * Finds {@link LogbookLifecycle} by archive unit id.
     * @param unitId
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<LogbookLifecycle> findUnitLifeCyclesByUnitId(final String unitId, final VitamContext vitamContext) throws VitamClientException {
        final RequestResponse<LogbookLifecycle> jsonResponse = accessExternalClient.selectUnitLifeCycleById(vitamContext, unitId,
                new Select().getFinalSelectById());
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    /**
     * Finds {@link LogbookLifecycle} by archive unit id.
     * @param unitId
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<LogbookLifecycle> findObjectGroupLifeCyclesByUnitId(final String unitId, final VitamContext vitamContext)
            throws VitamClientException {
        final RequestResponse<LogbookLifecycle> jsonResponse = accessExternalClient.selectObjectGroupLifeCycleById(vitamContext, unitId,
                new Select().getFinalSelectById());
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    public RequestResponse<LogbookOperation> selectOperations(final JsonNode select, final VitamContext vitamContext) throws VitamClientException {
        final RequestResponse<LogbookOperation> response = accessExternalClient.selectOperations(vitamContext, select);
        VitamRestUtils.checkResponse(response, HttpStatus.SC_OK, HttpStatus.SC_ACCEPTED);
        return response;
    }

    /**
     *
     * @param identifier
     * @param collectionNames
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<LogbookOperation> findEventsByIdentifierAndCollectionNames(final String identifier, final String collectionNames,
            final VitamContext vitamContext) throws VitamClientException {
        LOGGER.debug("findEventsByIdentifierAndCollectionNames for : identifier {}, collection {}, vitamContext {}", identifier, collectionNames,
                vitamContext);
        final ObjectNode select = buildOperationQuery(identifier, collectionNames);
        LOGGER.debug("selectOperations : select query {}, vitamContext {}", select, vitamContext);
        final RequestResponse<LogbookOperation> response = accessExternalClient.selectOperations(vitamContext, select);
        VitamRestUtils.checkResponse(response, HttpStatus.SC_OK, HttpStatus.SC_ACCEPTED);
        return response;
    }

    private ObjectNode buildOperationQuery(final String obId, final String obIdReq) {
        final Select select = new Select();
        final BooleanQuery andQuery;
        final CompareQuery obIdQuery;
        final CompareQuery obIdReqQuery;
        try {
            select.addUsedProjection("events");
            andQuery = QueryHelper.and();
            obIdQuery = QueryHelper.eq("events.obId", obId);
            obIdReqQuery = QueryHelper.eq("events.obIdReq", obIdReq);
            andQuery.add(obIdQuery, obIdReqQuery);
            select.setQuery(andQuery);
        } catch (final InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new ApplicationServerException("An error occured while creating vitam query", e);
        }

        return select.getFinalSelect();
    }

    public RequestResponse checkTraceability(final VitamContext context, final JsonNode query) throws AccessExternalClientServerException, InvalidParseOperationException, AccessUnauthorizedException {
        return adminExternalClient.checkTraceabilityOperation(context, query);
    }

    /**
     * Download the manifest of an ingest operation
     *
     * @param id           The id of the operation
     * @param vitamContext The Vitam context
     * @return
     */
    public Response downloadManifest(final String id, final VitamContext vitamContext) {
        return downloadIngestOperationObject(id, vitamContext, IngestCollection.MANIFESTS);
    }

    /**
     * Download the Archive Transfer Reply of an ingest operation
     *
     * @param id           The id of the operation
     * @param vitamContext The Vitam context
     * @return
     */
    public Response downloadAtr(final String id, final VitamContext vitamContext) {
        return downloadIngestOperationObject(id, vitamContext, IngestCollection.ARCHIVETRANSFERREPLY);
    }

    public Response downloadIngestOperationObject(final String id, final VitamContext vitamContext, final IngestCollection collection) {
        try {
            // Check operation type
            final LogbookOperationsResponseDto operation =
                    responseMapping(selectOperationbyId(id, vitamContext).toJsonNode(), LogbookOperationsResponseDto.class);
            if (operation == null || operation.getResults() == null || operation.getResults().size() == 0) {
                throw new IllegalArgumentException("Unable to download object of operation " + id + ": the operation does not exist");
            }
            if (!INGEST_TYPE.equals(operation.getResults().get(0).getEvTypeProc())) {
                throw new IllegalArgumentException("Unable to download object of operation " + id + ": the operation is not an ingest one");
            }

            final Response response = ingestExternalClient.downloadObjectAsync(vitamContext, id, collection);
            VitamRestUtils.checkResponse(response, Response.Status.OK.getStatusCode());

            final ContentDisposition.Builder contentDispositionBuilder = ContentDisposition.builder(ContentDispositionType.ATTACHMENT.getValue());
            contentDispositionBuilder.filename(String.format(collection + "_%s.xml", id));
            response.getHeaders().put(HttpHeaders.CONTENT_DISPOSITION, Arrays.asList(contentDispositionBuilder.build().toString()));

            return response;
        } catch (final VitamClientException exception) {
            LOGGER.error("Download {} id={}", collection, id, exception);
            throw new ApplicationServerException(exception.getMessage(), exception);
        }
    }

    public Response downloadReport(final String id, final String downloadType, final VitamContext vitamContext) throws VitamClientException {
        Response response;
        switch (downloadType) {
            case "transfersip":
                response = accessExternalClient.getTransferById(vitamContext, id);
                break;
            case "dip":
                response = accessExternalClient.getDIPById(vitamContext, id);
                break;
            case "batchreport":
                response = adminExternalClient.downloadBatchReport(vitamContext, id);
                break;
            case "report":
                response = adminExternalClient.downloadRulesReport(vitamContext, id);
                break;
            case "object":
                response = downloadAtr(id, vitamContext);
                break;
            default:
                response = null;
        }

        return response;
    }

}
