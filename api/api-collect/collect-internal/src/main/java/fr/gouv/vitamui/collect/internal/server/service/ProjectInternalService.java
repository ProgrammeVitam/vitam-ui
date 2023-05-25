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

package fr.gouv.vitamui.collect.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.collect.common.dto.CriteriaProjectDto;
import fr.gouv.vitam.collect.common.dto.ProjectDto;
import fr.gouv.vitam.collect.common.dto.TransactionDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.internal.server.service.converters.ProjectConverter;
import fr.gouv.vitamui.collect.internal.server.service.converters.TransactionConverter;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static fr.gouv.vitamui.collect.internal.server.service.converters.ProjectConverter.toVitamuiCollectProjectDto;

public class ProjectInternalService {

    public static final int MAX_RESULTS = 10000;
    public static final String UNABLE_TO_CREATE_PROJECT = "Unable to create project";
    public static final String UNABLE_TO_CREATE_TRANSACTION = "Unable to create transaction";
    public static final String UNABLE_TO_PROCESS_RESPONSE = "Unable to process response";
    public static final String UNABLE_TO_UPDATE_PROJECT = "Unable to update project";
    public static final String UNABLE_TO_UPLOAD_PROJECT_ZIP_FILE = "Unable to upload project zip file";
    public static final String UNABLE_TO_RETRIEVE_PROJECT = "Unable to retrieve project";
    private final CollectService collectService;
    private final ObjectMapper objectMapper;
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectInternalService.class);

    public ProjectInternalService(CollectService collectService, ObjectMapper objectMapper) {
        this.collectService = collectService;
        this.objectMapper = objectMapper;
    }

    public CollectProjectDto createProject(CollectProjectDto collectProjectDto, VitamContext vitamContext) {
        LOGGER.debug("CollectProjectDto: {}", collectProjectDto);
        try {
            ProjectDto projectDto = ProjectConverter.toVitamProjectDto(collectProjectDto);
            RequestResponse<JsonNode> requestResponse = collectService.initProject(vitamContext, projectDto);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when retrieving projects!");
            }
            return toVitamuiCollectProjectDto(
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    ProjectDto.class));
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_CREATE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_CREATE_PROJECT, e);
        } catch (InvalidParseOperationException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public CollectTransactionDto createTransactionForProject(
        CollectTransactionDto collectTransactionDto, String projectId, VitamContext vitamContext) {
        LOGGER.debug("CollectTransactionDto: ", collectTransactionDto);
        try {
            SanityChecker.checkSecureParameter(projectId);
            TransactionDto transactionDto = TransactionConverter.toVitamDto(collectTransactionDto);
            RequestResponse<JsonNode> requestResponse =
                collectService.initTransaction(vitamContext, transactionDto, projectId);
            if (!requestResponse.isOk()) {
                LOGGER.error("Error occurs when creating transaction");
                throw new VitamClientException("Error occurs when creating transaction");
            }
            return TransactionConverter.toVitamUiDto(
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    TransactionDto.class));
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_CREATE_TRANSACTION + ": {}", e);
            throw new InternalServerException(UNABLE_TO_CREATE_TRANSACTION, e);
        } catch (InvalidParseOperationException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public PaginatedValuesDto<CollectProjectDto> getAllProjectsPaginated(Integer page,
        Integer size, Optional<String> orderBy, Optional<DirectionDto> direction, Optional<String> criteria,
        VitamContext vitamContext) {
        LOGGER.debug("Page: ", page);
        LOGGER.debug("Size: ", size);
        LOGGER.debug("OrderBy: ", orderBy.orElse(null));
        LOGGER.debug("Direction: ", direction.orElse(null));
        LOGGER.debug("Criteria: ", criteria.orElse(null));
        try {
            RequestResponse<JsonNode> requestResponse;
            if (criteria.isPresent()) {
                HashMap<String, String> vitamCriteria = objectMapper.readValue(criteria.get(), new TypeReference<>() {
                });
                CriteriaProjectDto criteriaProjectDto = new CriteriaProjectDto();
                criteriaProjectDto.setQuery(vitamCriteria.get("query"));
                requestResponse = collectService.searchProject(vitamContext, criteriaProjectDto);
            } else {
                requestResponse = collectService.getProjects(vitamContext);
            }

            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when retrieving projects!");
            }
            final List<JsonNode> results = ((RequestResponseOK<JsonNode>) requestResponse).getResults();
            List<ProjectDto> projectDtos = new ArrayList<>();
            for (JsonNode result : results) {
                projectDtos.add(objectMapper.treeToValue(result, ProjectDto.class));
            }
            List<CollectProjectDto> collectProjectDtos = ProjectConverter.toVitamuiCollectProjectDtos(projectDtos);
            return new PaginatedValuesDto<>(collectProjectDtos, 1, MAX_RESULTS, false);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_RETRIEVE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_RETRIEVE_PROJECT, e);
        } catch (JsonProcessingException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public void streamingUpload(InputStream inputStream, String transactionId,
        String originalFileName, VitamContext vitamContext) {
        LOGGER.debug("TransactionId: ", transactionId);
        LOGGER.debug("OriginalFileName: ", originalFileName);
        try {
            collectService.uploadProjectZip(vitamContext, transactionId, inputStream);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_UPLOAD_PROJECT_ZIP_FILE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_UPLOAD_PROJECT_ZIP_FILE, e);
        }
    }

    public CollectProjectDto update(String id, CollectProjectDto collectProjectDto, VitamContext vitamContext) {
        LOGGER.debug("Id: ", id);
        LOGGER.debug("CollectProjectDto: ", collectProjectDto);
        try {
            ProjectDto projectDto = ProjectConverter.toVitamProjectDto(collectProjectDto);
            RequestResponse<JsonNode> requestResponse = collectService.updateProject(vitamContext, projectDto);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when updating project!");
            }
            ProjectDto responseProjectDto =
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    ProjectDto.class);
            return toVitamuiCollectProjectDto(responseProjectDto);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_UPDATE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_UPDATE_PROJECT, e);
        } catch (InvalidParseOperationException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public CollectProjectDto getProjectById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            RequestResponse<JsonNode> requestResponse = collectService.getProjectById(vitamContext, id);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when getting project!");
            }
            return toVitamuiCollectProjectDto(
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    ProjectDto.class));
        } catch (VitamClientException | InvalidParseOperationException e) {
            throw new VitamClientException("Unable to find project : ", e);
        }
    }

    public void deleteProjectById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            RequestResponse<JsonNode> requestResponse = collectService.deleteProjectById(vitamContext, id);
            if (requestResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new VitamClientException("Error occurs when deleteing project!");
            }
        } catch (VitamClientException e) {
            throw new VitamClientException("Unable to delete project : ", e);
        }
    }


    public PaginatedValuesDto<CollectTransactionDto> getTransactionsByProjectPaginated(String projectId, Integer page,
        Integer size,
        Optional<String> orderBy, Optional<DirectionDto> direction, VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Page: ", page);
        LOGGER.debug("Size: ", size);
        LOGGER.debug("OrderBy: ", orderBy.orElse(null));
        LOGGER.debug("Direction: ", direction.orElse(null));
        try {
            RequestResponse<JsonNode> requestResponse =
                collectService.getTransactionsByProject(projectId, vitamContext);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when getting transaction!");
            }

            final List<JsonNode> results = ((RequestResponseOK<JsonNode>) requestResponse).getResults();
            List<TransactionDto> transactionDtos = new ArrayList<>();
            for (JsonNode result : results) {
                TransactionDto transactionDto = JsonHandler.getFromString(result.toString(),
                    TransactionDto.class);
                transactionDtos.add(transactionDto);
            }
            List<CollectTransactionDto> collectTransactionDtos = TransactionConverter.toVitamuiDtos(transactionDtos);

            return new PaginatedValuesDto<>(collectTransactionDtos, 1, MAX_RESULTS, false);

        } catch (VitamClientException | InvalidParseOperationException e) {
            throw new VitamClientException("Unable to find transaction : ", e);
        }


    }

    public CollectTransactionDto getLastTransactionForProjectId(String projectId, VitamContext vitamContext)
        throws VitamClientException {
        try {
            RequestResponse<JsonNode> requestResponse =
                collectService.getLastTransactionForProjectId(vitamContext, projectId);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when getting last transaction by project!");
            }
            List<TransactionDto> transactionDtos =
                objectMapper.readValue(((RequestResponseOK) requestResponse).getResults().toString(),
                    new TypeReference<>() {
                    });
            List<CollectTransactionDto> collectTransactionDtos = TransactionConverter.toVitamuiDtos(transactionDtos);
            if (collectTransactionDtos.isEmpty()) {
                return null;
            }
            return collectTransactionDtos.get(collectTransactionDtos.size() - 1);
        } catch (VitamClientException | JsonProcessingException e) {
            throw new VitamClientException("Unable to find transactions by project : ", e);
        }
    }
}
