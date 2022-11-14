/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.commons.vitam.api.collect;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.collect.external.client.CollectClient;
import fr.gouv.vitam.collect.external.dto.CriteriaProjectDto;
import fr.gouv.vitam.collect.external.dto.ProjectDto;
import fr.gouv.vitam.collect.external.dto.TransactionDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public class CollectService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CollectService.class);

    private final CollectClient collectClient;

    public CollectService(final CollectClient collectClient) {
        this.collectClient = collectClient;
    }

    /**
     * Search units by projectId.
     *
     * @param transactionId
     * @param searchQuery
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponseOK<JsonNode> searchUnitsByTransactionId(final String transactionId, JsonNode searchQuery,
        final VitamContext vitamContext) throws VitamClientException {
        LOGGER.debug("transactionId : {}", transactionId);
        final RequestResponseOK<JsonNode> result =
            collectClient.getUnitsByTransaction(vitamContext, transactionId, searchQuery);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    public RequestResponseOK<JsonNode> searchUnitsByTransaction(final String transactionId, JsonNode searchQuery,
        final VitamContext vitamContext) throws VitamClientException {
        LOGGER.debug("projectId : {}", transactionId);
        final RequestResponseOK<JsonNode> result =
            collectClient.getUnitsByTransaction(vitamContext, transactionId, searchQuery);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    /**
     * initialize new project.
     *
     * @param projectDto
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> initProject(final VitamContext vitamContext, final ProjectDto projectDto)
        throws VitamClientException {
        LOGGER.debug("projectId : {}", projectDto.getId());
        final RequestResponse<JsonNode> result = collectClient.initProject(vitamContext, projectDto);
        VitamRestUtils.checkResponse(result);
        return result;
    }
    /**
     * initialize new transaction.
     *
     * @param vitamContext
     * @param transactionDto
     * @param projectId
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> initTransaction(final VitamContext vitamContext, final TransactionDto transactionDto, String projectId) throws VitamClientException {
        LOGGER.debug("transactionId : {}", transactionDto.getId());
        final RequestResponse<JsonNode> result = collectClient.initTransaction(vitamContext, transactionDto, projectId);
        VitamRestUtils.checkResponse(result);
        return result;
    }
    /**
     * update existing Project.
     *
     * @param projectDto
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> updateProject(final VitamContext vitamContext, final ProjectDto projectDto)
        throws VitamClientException {
        LOGGER.debug("projectId : {}", projectDto.getId());
        final RequestResponse<JsonNode> result = collectClient.updateProject(vitamContext, projectDto);
        VitamRestUtils.checkResponse(result);
        return result;
    }


    /**
     * update existing Transaction.
     *
     * @param vitamContext
     * @param transactionDto
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> updateTransaction(final VitamContext vitamContext, final TransactionDto transactionDto) throws VitamClientException {
        LOGGER.debug("transactionId : {}", transactionDto.getId());
        final RequestResponse<JsonNode> result = collectClient.updateTransaction(vitamContext, transactionDto);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    /**
     * get all created projects from Vitam
     *
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> getProjects(final VitamContext vitamContext) throws VitamClientException {
        final RequestResponse<JsonNode> result = collectClient.getProjects(vitamContext);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    /**
     * get all created transactions from Vitam
     *
     * @param vitamContext
     * @param projectId
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> getTransactionsByProject(final String projectId, final VitamContext vitamContext)
        throws VitamClientException {
        final RequestResponse<JsonNode> result = collectClient.getTransactionByProjectId(vitamContext, projectId);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    /**
     * get all created projects from Vitam
     *
     * @param vitamContext
     * @param criteriaProjectDto
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> searchProject(final VitamContext vitamContext,
        CriteriaProjectDto criteriaProjectDto)
        throws VitamClientException {


        final RequestResponse<JsonNode> result = collectClient.searchProject(vitamContext, criteriaProjectDto);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    /**
     * Upload the project zip file
     *
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public Response uploadProjectZip(final VitamContext vitamContext, final String transactionId, final InputStream inputStream) throws VitamClientException {
        LOGGER.debug("upload zip by transaction id : {}", transactionId);
        final Response result = collectClient.uploadProjectZip(vitamContext, transactionId, inputStream);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    public RequestResponse<JsonNode> getProjectById(final VitamContext vitamContext, final String projectId) throws VitamClientException {
        LOGGER.debug("get project by id : {}", projectId);
        final RequestResponse<JsonNode> result = collectClient.getProjectById(vitamContext, projectId);
        VitamRestUtils.checkResponse(result);
        return result;
    }


    public RequestResponse<JsonNode> getLastTransactionForProjectId(final VitamContext vitamContext, final String projectId) throws VitamClientException {
        LOGGER.debug("get last transaction by project id : {}", projectId);
        final RequestResponse<JsonNode> result = collectClient.getTransactionByProjectId(vitamContext, projectId);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    /**
     * Get object by id the project zip file
     *
     * @param vitamContext security context
     * @param objectId object id
     * @return RequestResponse<JsonNode>
     * @throws VitamClientException thrown exception
     */
    public RequestResponse<JsonNode> getObjectById(final VitamContext vitamContext, final String objectId)
        throws VitamClientException {
        LOGGER.debug("objectId : {}", objectId);
        final RequestResponse<JsonNode> result = collectClient.getObjectById(vitamContext, objectId);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    /**
     * Download object by unitId/usage/version
     *
     * @param unitId       unit id
     * @param usage        usage
     * @param version      version
     * @param vitamContext security context
     * @return Response
     * @throws VitamClientException Thrown exception
     */
    public Response getObjectStreamByUnitId(final String unitId, final String usage, final int version,
        final VitamContext vitamContext) throws VitamClientException {
        final Response response = collectClient.getObjectStreamByUnitId(vitamContext, unitId, usage, version);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<JsonNode> deleteProjectById(final VitamContext vitamContext, final String projectId)
        throws VitamClientException {
        LOGGER.debug("projectId : {}", projectId);
        final RequestResponse<JsonNode> result = collectClient.deleteProjectById(vitamContext, projectId);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    public Response validateTransaction(final VitamContext vitamContext, final String idTransaction)
        throws VitamClientException {
        LOGGER.debug("transactionId : {}", idTransaction);
        final Response response = collectClient.closeTransaction(vitamContext, idTransaction);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponseOK<JsonNode> sendTransaction(final VitamContext vitamContext, final String idTransaction)
        throws VitamClientException {
        LOGGER.debug("transactionId : {}", idTransaction);
        final RequestResponseOK<JsonNode> response = collectClient.ingest(vitamContext, idTransaction);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public Response reopenTransaction(final VitamContext vitamContext, final String idTransaction)
        throws VitamClientException {
        LOGGER.debug("transactionId : {}", idTransaction);
        final Response response = collectClient.reopenTransaction(vitamContext, idTransaction);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public Response abortTransaction(final VitamContext vitamContext, final String idTransaction)
        throws VitamClientException {
        LOGGER.debug("transactionId : {}", idTransaction);
        final Response response = collectClient.abortTransaction(vitamContext, idTransaction);
        VitamRestUtils.checkResponse(response);
        return response;
    }


    public RequestResponse<JsonNode> getTransactionById(VitamContext vitamContext, String transactionId)
        throws VitamClientException {
        LOGGER.debug("transactionId : {}", transactionId);
        final RequestResponse<JsonNode> result = collectClient.getTransactionById(vitamContext, transactionId);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    public RequestResponse<JsonNode> findUnitById(String unitId, VitamContext vitamContext) throws VitamClientException {
        LOGGER.debug("Unit ID : {}", unitId);
        RequestResponseOK<JsonNode> result = collectClient.getUnitById(vitamContext, unitId);
        VitamRestUtils.checkResponse(result);
        return result;
    }
}
