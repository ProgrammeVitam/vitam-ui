/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.collect.internal.server.service.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.collect.external.client.CollectClient;
import fr.gouv.vitam.collect.external.dto.ProjectDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.collect.common.dto.CollectListProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class ProjectInternalService {

    public static final int MAX_RESULTS = 10000;
    public static final String UNABLE_TO_CREATE_PROJECT = "Unable to create project";
    public static final String UNABLE_TO_PROCESS_RESPONSE = "Unable to process response";
    public static final String UNABLE_TO_UPDATE_PROJECT = "Unable to update project";
    public static final String UNABLE_TO_UPLOAD_PROJECT_ZIP_FILE = "Unable to upload project zip file";
    public static final String UNABLE_TO_RETRIEVE_PROJECT = "Unable to retrieve project";
    private final CollectClient collectClient;
    private final ObjectMapper objectMapper;
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectInternalService.class);

    public ProjectInternalService(CollectClient collectClient, ObjectMapper objectMapper) {
        this.collectClient = collectClient;
        this.objectMapper = objectMapper;
    }

    public CollectProjectDto createProject(VitamContext vitamContext, CollectProjectDto collectProjectDto) {
        LOGGER.debug("CollectProjectDto: ", collectProjectDto);
        try {
            ProjectDto projectDto = CollectProjectConverter.toVitamDto(collectProjectDto);
            RequestResponse<JsonNode> requestResponse = collectClient.initProject(vitamContext, projectDto);
            if(!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when retrieving projects!");
            }
            ProjectDto responseProjectDto = objectMapper.readValue(((RequestResponseOK) requestResponse).getFirstResult().toString(), ProjectDto.class);
            return CollectProjectConverter.toVitamuiDto(responseProjectDto);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_CREATE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_CREATE_PROJECT, e);
        } catch (JsonProcessingException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public PaginatedValuesDto<CollectProjectDto> getAllProjectsPaginated(VitamContext vitamContext, Integer page, Integer size,
        Optional<String> orderBy, Optional<DirectionDto> direction, Optional<String> criteria) {
        LOGGER.debug("Page: ", page);
        LOGGER.debug("Size: ", size);
        LOGGER.debug("OrderBy: ", orderBy.stream().findFirst().orElse(null));
        LOGGER.debug("Direction: ", direction.stream().findFirst().orElse(null));
        LOGGER.debug("Criteria: ", criteria.stream().findFirst().orElse(null));
        try {
            RequestResponse<JsonNode> requestResponse = collectClient.getProjects(vitamContext);
            if(!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when retrieving projects!");
            }
            List<CollectListProjectDto> collectListProjectDtos = objectMapper.readValue(((RequestResponseOK) requestResponse).getFirstResult().toString(), new TypeReference<>(){});
            List<CollectProjectDto> collectProjectDtos = CollectProjectConverter.toVitamuiDtos(collectListProjectDtos);
            return new PaginatedValuesDto<>(collectProjectDtos, 1, MAX_RESULTS, false);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_RETRIEVE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_RETRIEVE_PROJECT, e);
        } catch (JsonProcessingException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public void streamingUpload(VitamContext vitamContext, InputStream inputStream, String projectId, String originalFileName) {
        LOGGER.debug("ProjectId: ", projectId);
        LOGGER.debug("OriginalFileName: ", originalFileName);
        try {
            collectClient.uploadProjectZip(vitamContext, projectId, inputStream);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_UPLOAD_PROJECT_ZIP_FILE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_UPLOAD_PROJECT_ZIP_FILE, e);
        }
    }

    public CollectProjectDto update(VitamContext vitamContext, String id, CollectProjectDto collectProjectDto) {
        LOGGER.debug("Id: ", id);
        LOGGER.debug("CollectProjectDto: ", collectProjectDto);
        try {
            ProjectDto projectDto = CollectProjectConverter.toVitamDto(collectProjectDto);
            RequestResponse<JsonNode> requestResponse = collectClient.updateProject(vitamContext, projectDto);
            if(!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when updating project!");
            }
            ProjectDto responseProjectDto = objectMapper.readValue(((RequestResponseOK) requestResponse).getFirstResult().toString(), ProjectDto.class);
            return CollectProjectConverter.toVitamuiDto(responseProjectDto);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_UPDATE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_UPDATE_PROJECT, e);
        } catch (JsonProcessingException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }
}
