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
import fr.gouv.vitam.collect.external.dto.CriteriaProjectDto;
import fr.gouv.vitam.collect.external.dto.ProjectDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.internal.server.service.converters.ProjectConverter;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.FIELDS;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.NODES;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.DEFAULT_DEPTH;
import static fr.gouv.vitamui.collect.internal.server.service.converters.ProjectConverter.toVitamuiDto;

public class ProjectInternalService {

    public static final int MAX_RESULTS = 10000;
    public static final String UNABLE_TO_CREATE_PROJECT = "Unable to create project";
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

    public CollectProjectDto createProject(VitamContext vitamContext, CollectProjectDto collectProjectDto) {
        LOGGER.debug("CollectProjectDto: ", collectProjectDto);
        try {
            ProjectDto projectDto = ProjectConverter.toVitamDto(collectProjectDto);
            RequestResponse<JsonNode> requestResponse = collectService.initProject(vitamContext, projectDto);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when retrieving projects!");
            }
            return toVitamuiDto(JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                ProjectDto.class));
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_CREATE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_CREATE_PROJECT, e);
        } catch (InvalidParseOperationException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public PaginatedValuesDto<CollectProjectDto> getAllProjectsPaginated(VitamContext vitamContext, Integer page,
        Integer size,
        Optional<String> orderBy, Optional<DirectionDto> direction, Optional<String> criteria) {
        LOGGER.debug("Page: ", page);
        LOGGER.debug("Size: ", size);
        LOGGER.debug("OrderBy: ", orderBy.orElse(null));
        LOGGER.debug("Direction: ", direction.orElse(null));
        LOGGER.debug("Criteria: ", criteria.orElse(null));
        try {
            RequestResponse<JsonNode> requestResponse;
            if (criteria.isPresent()) {

                TypeReference<HashMap<String, String>> typRef = new TypeReference<>() {
                };
                HashMap<String, String> vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
                var criteriaProjectDto = new CriteriaProjectDto();
                criteriaProjectDto.setQuery(vitamCriteria.get("query"));
                requestResponse = collectService.searchProject(vitamContext, criteriaProjectDto);
            } else {

                requestResponse = collectService.getProjects(vitamContext);
            }

            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when retrieving projects!");
            }
            List<ProjectDto> projectDtos =
                objectMapper.readValue(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    new TypeReference<>() {
                    });
            List<CollectProjectDto> collectProjectDtos = ProjectConverter.toVitamuiDtos(projectDtos);
            return new PaginatedValuesDto<>(collectProjectDtos, 1, MAX_RESULTS, false);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_RETRIEVE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_RETRIEVE_PROJECT, e);
        } catch (JsonProcessingException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public void streamingUpload(VitamContext vitamContext, InputStream inputStream, String projectId,
        String originalFileName) {
        LOGGER.debug("ProjectId: ", projectId);
        LOGGER.debug("OriginalFileName: ", originalFileName);
        try {
            collectService.uploadProjectZip(vitamContext, projectId, inputStream);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_UPLOAD_PROJECT_ZIP_FILE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_UPLOAD_PROJECT_ZIP_FILE, e);
        }
    }

    public CollectProjectDto update(VitamContext vitamContext, String id, CollectProjectDto collectProjectDto) {
        LOGGER.debug("Id: ", id);
        LOGGER.debug("CollectProjectDto: ", collectProjectDto);
        try {
            ProjectDto projectDto = ProjectConverter.toVitamDto(collectProjectDto);
            RequestResponse<JsonNode> requestResponse = collectService.updateProject(vitamContext, projectDto);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when updating project!");
            }
            ProjectDto responseProjectDto =
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    ProjectDto.class);
            return toVitamuiDto(responseProjectDto);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_UPDATE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_UPDATE_PROJECT, e);
        } catch (InvalidParseOperationException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public SelectMultiQuery mapRequestToSelectMultiQuery(SearchCriteriaDto searchQuery)
        throws VitamClientException {
        if (searchQuery == null) {
            throw new BadRequestException("Can't parse null criteria");
        }
        SelectMultiQuery selectMultiQuery;
        Optional<String> orderBy = Optional.empty();
        Optional<DirectionDto> direction = Optional.empty();
        try {
            if (searchQuery.getSortingCriteria() != null) {
                direction = Optional.of(searchQuery.getSortingCriteria().getSorting());
                orderBy = Optional.of(searchQuery.getSortingCriteria().getCriteria());
            }
            selectMultiQuery = createSelectMultiQuery(searchQuery.getCriteriaList());
            if (orderBy.isPresent()) {
                if (DirectionDto.DESC.equals(direction.get())) {
                    selectMultiQuery.addOrderByDescFilter(orderBy.get());
                } else {
                    selectMultiQuery.addOrderByAscFilter(orderBy.get());
                }
            }
            selectMultiQuery
                .setLimitFilter((long) searchQuery.getPageNumber() * searchQuery.getSize(), searchQuery.getSize());
            selectMultiQuery.trackTotalHits(searchQuery.isTrackTotalHits());
            LOGGER.debug("Final query: {}", selectMultiQuery.getFinalSelect().toPrettyString());

        } catch (InvalidCreateOperationException ioe) {
            throw new VitamClientException("Unable to find archive units with pagination", ioe);
        } catch (InvalidParseOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query" + e.getMessage());
        }
        return selectMultiQuery;
    }

    public SelectMultiQuery createSelectMultiQuery(List<SearchCriteriaEltDto> criteriaList)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        final BooleanQuery query = and();
        final SelectMultiQuery select = new SelectMultiQuery();
        //Handle roots
        LOGGER.debug("Call create Query DSL for criteriaList {} ", criteriaList);
        List<SearchCriteriaEltDto> mgtRulesCriteriaList = criteriaList.stream().filter(Objects::nonNull)
            .filter(searchCriteriaEltDto -> (ArchiveSearchConsts.CriteriaMgtRulesCategory
                .contains(searchCriteriaEltDto.getCategory().name()))).collect(Collectors.toList());

        List<SearchCriteriaEltDto> simpleCriteriaList = criteriaList.stream().filter(
            Objects::nonNull).filter(searchCriteriaEltDto -> FIELDS
            .equals(searchCriteriaEltDto.getCategory())).collect(Collectors.toList());

        LOGGER.debug("management rules criteria list {}", mgtRulesCriteriaList);
        LOGGER.debug("sample criteria list {}", simpleCriteriaList);
        List<String> nodesCriteriaList = criteriaList.stream().filter(
                Objects::nonNull).filter(searchCriteriaEltDto -> NODES
                .equals(searchCriteriaEltDto.getCategory())).flatMap(criteria -> criteria.getValues().stream())
            .map(CriteriaValue::getValue).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(nodesCriteriaList)) {
            select.addRoots(nodesCriteriaList.toArray(new String[nodesCriteriaList.size()]));
            query.setDepthLimit(DEFAULT_DEPTH);
        }

        if (query.isReady()) {
            select.setQuery(query);
        }

        LOGGER.debug("Final query: {}", select.getFinalSelect().toPrettyString());

        return select;
    }

    public CollectProjectDto getProjectById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            RequestResponse<JsonNode> requestResponse = collectService.getProjectById(vitamContext, id);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when getting project!");
            }
            return toVitamuiDto(
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
}
