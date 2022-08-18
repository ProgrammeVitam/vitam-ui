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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.exists;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.FIELDS;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.NODES;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.DEFAULT_DEPTH;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.ID;

public class ProjectArchiveUnitInternalService {

    private final CollectService collectService;
    private final ObjectMapper objectMapper;
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectArchiveUnitInternalService.class);

    public ProjectArchiveUnitInternalService(CollectService collectService, ObjectMapper objectMapper) {
        this.collectService = collectService;
        this.objectMapper = objectMapper;
    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(String projectId, SearchCriteriaDto searchQuery,
        VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException, InvalidParseOperationException {

        LOGGER.debug("get units by query {}", searchQuery);
        JsonNode searchQueryToDSL = mapRequestToSelectMultiQuery(searchQuery).getFinalSelect();
        final RequestResponse<JsonNode> result =
            collectService.searchUnitsByProjectId(projectId, searchQueryToDSL, vitamContext);
        VitamRestUtils.checkResponse(result);
        final VitamUISearchResponseDto archivesOriginResponse =
            objectMapper.treeToValue(result.toJsonNode(), VitamUISearchResponseDto.class);

        VitamUIArchiveUnitResponseDto responseFilled = new VitamUIArchiveUnitResponseDto();
        responseFilled.setContext(archivesOriginResponse.getContext());
        responseFilled.setFacetResults(archivesOriginResponse.getFacetResults());
        responseFilled.setResults(
            JsonHandler.getFromJsonNodeList(((RequestResponseOK) result).getResults(), ArchiveUnit.class));
        responseFilled.setHits(archivesOriginResponse.getHits());
        return new ArchiveUnitsDto(responseFilled);
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
        List<String> nodesCriteriaList = criteriaList.stream().filter(
                Objects::nonNull).filter(searchCriteriaEltDto -> NODES
                .equals(searchCriteriaEltDto.getCategory())).flatMap(criteria -> criteria.getValues().stream())
            .map(CriteriaValue::getValue).collect(Collectors.toList());

        // TODO : Handle criteriaList when searching by criteria !
        query.add(exists(ID));

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
}
