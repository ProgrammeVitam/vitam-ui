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
import fr.gouv.vitam.collect.external.dto.ProjectDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
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
     * @param projectId
     * @param searchQuery
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponseOK<JsonNode> searchUnitsByProjectId(final String projectId, JsonNode searchQuery,
        final VitamContext vitamContext)
        throws VitamClientException {
        LOGGER.debug("projectId : {}", projectId);
        final RequestResponseOK<JsonNode> result =
            collectClient.getUnitsByProjectId(vitamContext, projectId, searchQuery);
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
     * get all created projects from Vitam
     *
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> getProjects(final VitamContext vitamContext)
        throws VitamClientException {
        final RequestResponse<JsonNode> result = collectClient.getProjects(vitamContext);
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
    public Response uploadProjectZip(final VitamContext vitamContext, final String projectId,
        final InputStream inputStream)
        throws VitamClientException {
        LOGGER.debug("projectId : {}", projectId);
        final Response result = collectClient.uploadProjectZip(vitamContext, projectId, inputStream);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    public RequestResponse<JsonNode> getProjectById(final VitamContext vitamContext, final String projectId)
        throws VitamClientException {
        LOGGER.debug("projectId : {}", projectId);
        final RequestResponse<JsonNode> result = collectClient.getProjectById(vitamContext, projectId);
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
     * @param unitId unit id
     * @param usage usage
     * @param version version
     * @param vitamContext security context
     * @return Response
     * @throws VitamClientException Thrown exception
     */
    public Response getObjectStreamByUnitId(final String unitId, final String usage, final int version,
        final VitamContext vitamContext)
        throws VitamClientException {
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

}
