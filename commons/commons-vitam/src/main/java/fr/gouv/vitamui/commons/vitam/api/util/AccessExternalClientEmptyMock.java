package fr.gouv.vitamui.commons.vitam.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.external.client.AbstractMockClient;
import fr.gouv.vitam.common.model.JsonLineIterator;
import fr.gouv.vitam.common.model.PreservationRequest;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitam.common.model.export.transfer.TransferRequest;
import fr.gouv.vitam.common.model.logbook.LogbookLifecycle;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.common.model.storage.AccessRequestReference;
import fr.gouv.vitam.common.model.storage.StatusByAccessRequest;
import org.apache.commons.lang3.NotImplementedException;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class AccessExternalClientEmptyMock extends AbstractMockClient implements AccessExternalClient {

    public RequestResponse<LogbookOperation> selectOperations(VitamContext vitamContext, JsonNode select) {
        return (new RequestResponseOK<LogbookOperation>()).addAllResults(new ArrayList<>()).setHttpCode(Response.Status.OK.getStatusCode());
    }

    public RequestResponse<JsonNode> selectUnits(VitamContext vitamContext, JsonNode selectQuery) {
        throw new NotImplementedException("");
    }

    @Override
    public RequestResponse<JsonNode> selectUnitsByUnitPersistentIdentifier(VitamContext vitamContext, JsonNode jsonNode, String s)
            throws VitamClientException {
        throw new NotImplementedException("");
    }

    public JsonLineIterator<JsonNode> streamUnits(VitamContext vitamContext, JsonNode selectQuery) {
        throw new NotImplementedException("");
    }

    @Override
    public JsonLineIterator<JsonNode> streamObjects(VitamContext vitamContext, JsonNode jsonNode) throws VitamClientException {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> selectUnitbyId(VitamContext vitamContext, JsonNode selectQuery, String unitId) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> updateUnitbyId(VitamContext vitamContext, JsonNode updateQuery, String unitId) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> selectObjectMetadatasByUnitId(VitamContext vitamContext, JsonNode selectQuery, String unitId) {
        throw new NotImplementedException("");
    }

    public RequestResponse<LogbookOperation> selectOperationbyId(VitamContext vitamContext, String processId, JsonNode select) {
        throw new NotImplementedException("");
    }

    public RequestResponse<LogbookLifecycle> selectUnitLifeCycleById(VitamContext vitamContext, String idUnit, JsonNode select) {
        throw new NotImplementedException("");
    }

    public RequestResponse<LogbookLifecycle> selectObjectGroupLifeCycleById(VitamContext vitamContext, String idObject, JsonNode select) {
        throw new NotImplementedException("");
    }

    public Response getObjectStreamByUnitId(VitamContext vitamContext, String unitId, String usage, int version) {
        throw new NotImplementedException("");
    }

    public RequestResponse<AccessRequestReference> createObjectAccessRequestByUnitId(VitamContext vitamContext, String unitId, String usage, int version) {
        throw new NotImplementedException("");
    }

    public RequestResponse<StatusByAccessRequest> checkAccessRequestStatuses(VitamContext vitamContext, Collection<AccessRequestReference> accessRequestReferences) {
        throw new NotImplementedException("");
    }

    public RequestResponse<Void> removeAccessRequest(VitamContext vitamContext, AccessRequestReference accessRequestReference) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> exportDIP(VitamContext vitamContext, JsonNode dslRequest) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> transfer(VitamContext vitamContext, TransferRequest transferRequest) {
        throw new NotImplementedException("");
    }

    public Response getTransferById(VitamContext vitamContext, String transferId) {
        throw new NotImplementedException("");
    }

    public Response getDIPById(VitamContext vitamContext, String dipId) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> reclassification(VitamContext vitamContext, JsonNode reclassificationRequest) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> massUpdateUnits(VitamContext vitamContext, JsonNode updateQuery) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> bulkAtomicUpdateUnits(VitamContext vitamContext, JsonNode updateQuery) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> massUpdateUnitsRules(VitamContext vitamContext, JsonNode queryJson) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> revertUpdateUnits(VitamContext vitamContext, JsonNode revertUpdateQuery) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> selectObjects(VitamContext vitamContext, JsonNode selectQuery) {
        throw new NotImplementedException("");
    }

    @Override
    public Response getObjectByUnitPersistentIdentifier(VitamContext vitamContext, String s, String s1, String s2) throws VitamClientException {
        throw new NotImplementedException("");
    }

    @Override
    public RequestResponse<JsonNode> getObjectByObjectPersistentIdentifier(VitamContext vitamContext, JsonNode jsonNode, String s) throws VitamClientException {
        throw new NotImplementedException("");
    }

    @Override
    public Response downloadObjectByObjectPersistentIdentifier(VitamContext vitamContext, String s) throws VitamClientException {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> selectUnitsWithInheritedRules(VitamContext vitamContext, JsonNode selectQuery) {
        throw new NotImplementedException("");
    }

    public Response getAccessLog(VitamContext vitamContext, JsonNode params) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> computedInheritedRules(VitamContext vitamContext, JsonNode updateRulesQuery) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> deleteComputedInheritedRules(VitamContext vitamContext, JsonNode deleteComputedInheritedRulesQuery) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> launchPreservation(VitamContext vitamContext, PreservationRequest preservationRequest) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> startEliminationAnalysis(VitamContext vitamContext, EliminationRequestBody eliminationRequestBody) {
        throw new NotImplementedException("");
    }

    @Override
    public RequestResponse<JsonNode> startEliminationAction(VitamContext vitamContext, EliminationRequestBody eliminationRequestBody) {
        throw new NotImplementedException("");
    }

    public RequestResponse<JsonNode> transferReply(VitamContext vitamContext, InputStream transferReply) {
        throw new NotImplementedException("");
    }

}
