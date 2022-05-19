/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.facet.FacetHelper;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.service.RulesUpdateCommonService;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.UnitTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper.unitType;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.ACCESS_RULE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.FIELDS;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.NODES;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaMgtRulesCategory;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.DEFAULT_DEPTH;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FACETS_COMPUTE_RULES_AU_NUMBER;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.RULES_COMPUTED;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.WAITING_RECALCULATE;

/**
 * Archive-Search Internal service communication with VITAM.
 */
@Service
public class ArchiveSearchInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalService.class);
    private static final String ARCHIVE_UNIT_DETAILS = "$results";
    public static final String DSL_QUERY_PROJECTION = "$projection";
    public static final String DSL_QUERY_FILTER = "$filter";
    public static final String DSL_QUERY_FACETS = "$facets";
    public static final String OPERATION_IDENTIFIER = "itemId";

    public static final String TITLE_FIELD = "Title";

    private static final String[] FILING_PLAN_PROJECTION =
        new String[] {"#id", TITLE_FIELD, "Title_", "DescriptionLevel", "#unitType", "#unitups", "#allunitups"};
    public static final String FALSE = "false";
    public static final String TRUE = "true";

    private static final String SET = "$set";
    private static final String UNSET = "$unset";
    private static final String ACTION = "$action";

    private final ObjectMapper objectMapper;
    private final UnitService unitService;
    private final ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;
    private final ArchiveSearchRulesInternalService archiveSearchRulesInternalService;
    private final ArchivesSearchManagementRulesQueryBuilderService archivesSearchManagementRulesQueryBuilderService;
    private final ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService;
    private final RulesUpdateCommonService rulesUpdateCommonService;
    private final ArchiveSearchFacetsInternalService archiveSearchFacetsInternalService;


    @Autowired
    public ArchiveSearchInternalService(final ObjectMapper objectMapper, final UnitService unitService,
        final ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService,
        final ArchiveSearchRulesInternalService archiveSearchRulesInternalService,
        final ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService,
        final ArchivesSearchManagementRulesQueryBuilderService archivesSearchManagementRulesQueryBuilderService,
        final RulesUpdateCommonService rulesUpdateCommonService,
        final ArchiveSearchFacetsInternalService archiveSearchFacetsInternalService

    ) {
        this.unitService = unitService;
        this.objectMapper = objectMapper;
        this.archiveSearchAgenciesInternalService = archiveSearchAgenciesInternalService;
        this.archiveSearchRulesInternalService = archiveSearchRulesInternalService;
        this.archivesSearchFieldsQueryBuilderService = archivesSearchFieldsQueryBuilderService;
        this.archivesSearchManagementRulesQueryBuilderService = archivesSearchManagementRulesQueryBuilderService;
        this.rulesUpdateCommonService = rulesUpdateCommonService;
        this.archiveSearchFacetsInternalService = archiveSearchFacetsInternalService;
    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext)
        throws VitamClientException, IOException {
        try {
            LOGGER.debug("calling find archive units by criteria {} ", searchQuery.toString());
            archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
            archiveSearchRulesInternalService.mapManagementRulesTitlesToCodes(searchQuery, vitamContext);

            fillWaitingToComputeCriteria(searchQuery);
            SelectMultiQuery selectMultiQuery = mapRequestToSelectMultiQuery(searchQuery);
            archiveSearchFacetsInternalService.addPositionsNodesFacet(searchQuery, selectMultiQuery);
            if (searchQuery.isComputeFacets()) {
                selectMultiQuery.addFacets(FacetHelper.terms(FACETS_COMPUTE_RULES_AU_NUMBER,
                    SIMPLE_FIELDS_VALUES_MAPPING.get(RULES_COMPUTED), 3,
                    FacetOrder.ASC));
            }
            JsonNode dslQuery = selectMultiQuery.getFinalSelect();
            JsonNode vitamResponse = searchArchiveUnits(dslQuery, vitamContext);
            ArchiveUnitsDto archiveUnitsDto = decorateAndMapResponse(vitamResponse, vitamContext);
            archiveSearchFacetsInternalService.fillFacets(searchQuery, archiveUnitsDto, vitamContext);
            return archiveUnitsDto;
        } catch (InvalidCreateOperationException ioe) {
            throw new VitamClientException("Unable to find archive units with pagination", ioe);
        }
    }

    private void fillWaitingToComputeCriteria(SearchCriteriaDto searchQuery) {
        List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList =
            searchQuery.extractCriteriaListByCategory(APPRAISAL_RULE);
        List<SearchCriteriaEltDto> accessMgtRulesCriteriaList =
            searchQuery.extractCriteriaListByCategory(ACCESS_RULE);
        List<SearchCriteriaEltDto> waitingToRecalculateCriteria = searchQuery
            .extractCriteriaListByCategoryAndFieldNames(FIELDS,
                List.of(WAITING_RECALCULATE));

        if (!CollectionUtils.isEmpty(waitingToRecalculateCriteria) &&
            (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList) ||
                !CollectionUtils.isEmpty(accessMgtRulesCriteriaList))) {
            List<SearchCriteriaEltDto> initialCriteriaList = searchQuery.getCriteriaList().stream().filter(
                searchCriteriaEltDto ->
                    !(FIELDS.equals(searchCriteriaEltDto.getCategory()) &&
                        (searchCriteriaEltDto.getCriteria()
                            .equals(WAITING_RECALCULATE)))
            ).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList)) {
                archiveSearchFacetsInternalService
                    .mergeValidComputedInheritenceCriteriaWithAppraisalCriteria(initialCriteriaList,
                        APPRAISAL_RULE);
            }
            if (!CollectionUtils.isEmpty(accessMgtRulesCriteriaList)) {
                archiveSearchFacetsInternalService
                    .mergeValidComputedInheritenceCriteriaWithAppraisalCriteria(initialCriteriaList,
                        ACCESS_RULE);
            }
            searchQuery.setCriteriaList(initialCriteriaList);
        }
    }

    private ArchiveUnitsDto decorateAndMapResponse(JsonNode vitamResponse, VitamContext vitamContext)
        throws JsonProcessingException, VitamClientException {
        final VitamUISearchResponseDto archivesOriginResponse =
            objectMapper.treeToValue(vitamResponse, VitamUISearchResponseDto.class);
        Set<String> originatingAgenciesCodes = archivesOriginResponse.getResults().stream().map(
            ResultsDto::getOriginatingAgency).
            filter(Objects::nonNull).collect(Collectors.toSet());
        List<AgencyModelDto> originAgenciesFound =
            archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(vitamContext, originatingAgenciesCodes);
        Map<String, AgencyModelDto> agenciesMapByIdentifier =
            originAgenciesFound.stream().collect(Collectors.toMap(AgencyModelDto::getIdentifier, agency -> agency));

        List<ArchiveUnit> archivesFilled = archivesOriginResponse.getResults().stream().map(
            archiveUnit -> archiveSearchAgenciesInternalService
                .fillOriginatingAgencyName(archiveUnit, agenciesMapByIdentifier)
        ).collect(Collectors.toList());
        VitamUIArchiveUnitResponseDto responseFilled = new VitamUIArchiveUnitResponseDto();
        responseFilled.setContext(archivesOriginResponse.getContext());
        responseFilled.setFacetResults(archivesOriginResponse.getFacetResults());
        responseFilled.setResults(archivesFilled);
        responseFilled.setHits(archivesOriginResponse.getHits());
        return new ArchiveUnitsDto(responseFilled);
    }

    public JsonNode mapRequestToDslQuery(SearchCriteriaDto searchQuery) throws VitamClientException {
        SelectMultiQuery selectMultiQuery = mapRequestToSelectMultiQuery(searchQuery);
        return selectMultiQuery.getFinalSelect();
    }

    /**
     * Map search query to DSl Query Json node
     *
     * @param searchQuery
     * @return
     */
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


    public JsonNode searchArchiveUnits(final JsonNode dslQuery, final VitamContext vitamContext)
        throws VitamClientException {
        RequestResponse<JsonNode> response = unitService.searchUnits(dslQuery, vitamContext);
        return response.toJsonNode();
    }

    public JsonNode getFillingHoldingScheme(VitamContext vitamContext) throws VitamClientException {
        final JsonNode fillingHoldingQuery = createQueryForHoldingFillingUnit();
        return searchArchiveUnits(fillingHoldingQuery, vitamContext);
    }

    public ResultsDto findArchiveUnitById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.debug("Archive Unit Id : {}", id);
            String re = StringUtils
                .chop(unitService.findUnitById(id, vitamContext).toJsonNode().get(ARCHIVE_UNIT_DETAILS).toString()
                    .substring(1));
            return objectMapper.readValue(re, ResultsDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can not get the archive unit {} ", e);
            throw new VitamClientException("Unable to find the UA", e);
        }
    }

    public String updateUnitById(String id, UnitDescriptiveMetadataDto unitDescriptiveMetadataDto,
        VitamContext vitamContext) throws VitamClientException {

        if (unitDescriptiveMetadataDto == null) {
            throw new BadRequestException("Error update unit criteria");
        }

        LOGGER.debug("UnitDescriptiveMetadataDto : {}", unitDescriptiveMetadataDto.toString());
        ObjectNode dslQuery = createUpdateQuery(unitDescriptiveMetadataDto);

        LOGGER.debug("updateUnitById query : {}", dslQuery.toPrettyString());
        RequestResponse<JsonNode> updateResponse = unitService.updateUnitById(vitamContext, dslQuery, id);
        String response = null;
        if (updateResponse.isOk()) {
            RequestResponse<JsonNode> unitById = unitService.findUnitById(id, vitamContext);
            try {
                final VitamUISearchResponseDto archivesResponse =
                    objectMapper.treeToValue(unitById.toJsonNode(), VitamUISearchResponseDto.class);
                List<String> operations = archivesResponse.getResults().get(0).getOperations();
                response = operations.get(operations.size() - 1);
            } catch (Exception e) {
                throw new VitamClientException("Error fetching unit from vitam while updating descriptive metadata");
            }
        }
        return response;
    }

    public ObjectNode createUpdateQuery(UnitDescriptiveMetadataDto unitDescriptiveMetadataDto) {

        ObjectNode dslQuery = JsonHandler.createObjectNode();
        ArrayNode arrayAction = JsonHandler.createArrayNode();
        ObjectNode unsetNode = JsonHandler.createObjectNode();
        if (!CollectionUtils.isEmpty(unitDescriptiveMetadataDto.getUnsetAction())) {
            unsetNode.putPOJO(UNSET, unitDescriptiveMetadataDto.getUnsetAction());
            nullifyField(unitDescriptiveMetadataDto);
            unitDescriptiveMetadataDto.setUnsetAction(null);
        }

        ObjectNode setNode = JsonHandler.createObjectNode();
        transformDate(unitDescriptiveMetadataDto);
        unitDescriptiveMetadataDto.setUnsetAction(null);
        setNode.putPOJO(SET, unitDescriptiveMetadataDto);
        if (setNode.get(SET) != null && !Objects.equals(setNode.get(SET).toString(), "{}")) {
            arrayAction.add(setNode);
        }
        if (!unsetNode.isEmpty()) {
            arrayAction.add(unsetNode);
        }
        dslQuery.putArray(ACTION);
        ArrayNode action = (ArrayNode) dslQuery.get(ACTION);

        if (setNode.get(SET) != null && !Objects.equals(setNode.get(SET).toString(), "{}")) {
            action.add(setNode);
        }
        if (!unsetNode.isEmpty()) {
            unitDescriptiveMetadataDto.setUnsetAction(null);
            action.add(unsetNode);
        }
        return dslQuery;
    }

    private void nullifyField(UnitDescriptiveMetadataDto unitDescriptiveMetadataDto) {
        unitDescriptiveMetadataDto.getUnsetAction().forEach(f -> {
            switch (f) {
                case "StartDate":
                    unitDescriptiveMetadataDto.setStartDate(null);
                    break;
                case "EndDate":
                    unitDescriptiveMetadataDto.setEndDate(null);
                    break;
                case "Description":
                    unitDescriptiveMetadataDto.setDescription(null);
                    break;
                case "Description_.fr":
                    unitDescriptiveMetadataDto.setDescription_fr(null);
                    break;
                case "Description_.en":
                    unitDescriptiveMetadataDto.setDescription_en(null);
                    break;
                default:
                    break;
            }
        });
    }

    private void transformDate(UnitDescriptiveMetadataDto unitDescriptiveMetadataDto) {
        if (unitDescriptiveMetadataDto.getStartDate() != null) {
            unitDescriptiveMetadataDto.setStartDate(
                LocalDateUtil.getFormattedDateForMongo(unitDescriptiveMetadataDto.getStartDate()).split("T")[0]);
        }
        if (unitDescriptiveMetadataDto.getEndDate() != null) {
            unitDescriptiveMetadataDto.setEndDate(
                LocalDateUtil.getFormattedDateForMongo(unitDescriptiveMetadataDto.getEndDate()).split("T")[0]);
        }
    }

    public ResultsDto findObjectById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.debug("Get Object Group");
            String re = StringUtils
                .chop(
                    unitService.findObjectMetadataById(id, vitamContext).toJsonNode()
                        .get(ARCHIVE_UNIT_DETAILS)
                        .toString()
                        .substring(1));
            return objectMapper.readValue(re, ResultsDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can not get the object group {} ", e);
            throw new InternalServerException("Unable to find the ObjectGroup", e);
        }
    }

    /**
     * Download the Unit Binary Object
     *
     * @param id
     * @param usage
     * @param version
     * @param vitamContext
     * @throws VitamClientException
     */
    public Response downloadObjectFromUnit(String id, String usage, Integer version, final VitamContext vitamContext)
        throws VitamClientException {
        LOGGER.debug("Download Archive Unit Object with id {} , usage {} and version {}  ", id, usage, version);
        return unitService
            .getObjectStreamByUnitId(id, usage, version, vitamContext);
    }



    public SelectMultiQuery createSelectMultiQuery(List<SearchCriteriaEltDto> criteriaList)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        final BooleanQuery query = and();
        final SelectMultiQuery select = new SelectMultiQuery();
        //Handle roots
        LOGGER.debug("Call create Query DSL for criteriaList {} ", criteriaList);
        List<SearchCriteriaEltDto> mgtRulesCriteriaList = criteriaList.stream().filter(Objects::nonNull)
            .filter(searchCriteriaEltDto -> (CriteriaMgtRulesCategory
                .contains(searchCriteriaEltDto.getCategory().name()))).collect(Collectors.toList());

        List<SearchCriteriaEltDto> simpleCriteriaList = criteriaList.stream().filter(
            Objects::nonNull).filter(searchCriteriaEltDto -> FIELDS
            .equals(searchCriteriaEltDto.getCategory())).collect(Collectors.toList());
        List<String> nodesCriteriaList = criteriaList.stream().filter(
            Objects::nonNull).filter(searchCriteriaEltDto -> NODES
            .equals(searchCriteriaEltDto.getCategory())).flatMap(criteria -> criteria.getValues().stream())
            .map(CriteriaValue::getValue).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(nodesCriteriaList)) {
            select.addRoots(nodesCriteriaList.toArray(new String[nodesCriteriaList.size()]));
            query.setDepthLimit(DEFAULT_DEPTH);
        }
        archivesSearchFieldsQueryBuilderService.fillQueryFromCriteriaList(query, simpleCriteriaList);
        archivesSearchManagementRulesQueryBuilderService.fillQueryFromMgtRulesCriteriaList(query, mgtRulesCriteriaList);
        if (query.isReady()) {
            select.setQuery(query);
        }
        LOGGER.debug("Final query: {}", select.getFinalSelect().toPrettyString());

        return select;
    }



    public JsonNode prepareDslQuery(final SearchCriteriaDto searchQuery, final VitamContext vitamContext)
        throws VitamClientException {
        searchQuery.setPageNumber(0);
        archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
        archiveSearchRulesInternalService.mapManagementRulesTitlesToCodes(searchQuery, vitamContext);
        return mapRequestToDslQuery(searchQuery);
    }



    public JsonNode createQueryForHoldingFillingUnit() {
        try {
            final SelectMultiQuery select = new SelectMultiQuery();
            final Query query =
                in(unitType(), UnitTypeEnum.HOLDING_UNIT.getValue(), UnitTypeEnum.FILING_UNIT.getValue());
            select.addQueries(query);
            ObjectNode orderFilter = JsonHandler.createObjectNode();
            orderFilter.put(TITLE_FIELD, 1);
            ObjectNode filter = JsonHandler.createObjectNode();
            filter.set("$orderby", orderFilter);
            select.setFilter(filter);
            select.addUsedProjection(FILING_PLAN_PROJECTION);
            LOGGER.debug("query =", select.getFinalSelect().toPrettyString());
            return select.getFinalSelect();
        } catch (InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new UnexpectedDataException(
                "Unexpected error occured while building holding dsl query : " + e.getMessage());
        }
    }


    public String computedInheritedRules(final VitamContext vitamContext, final SearchCriteriaDto searchCriteriaDto)
        throws VitamClientException {
        LOGGER.debug("Computed Inherited Rules by criteria {} ", searchCriteriaDto.toString());
        JsonNode jsonNode = mapRequestToDslQuery(searchCriteriaDto);
        ObjectNode dslRequest = (ObjectNode) jsonNode;
        rulesUpdateCommonService
            .deleteAttributesFromObjectNode(dslRequest, DSL_QUERY_PROJECTION, DSL_QUERY_FILTER, DSL_QUERY_FACETS);
        LOGGER.debug("Computed Inherited Rules final dslQuery : {}", dslRequest);
        JsonNode response = computedInheritedRules(vitamContext, dslRequest);
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }

    private JsonNode computedInheritedRules(final VitamContext vitamContext, final JsonNode dslQuery)
        throws VitamClientException {
        RequestResponse<JsonNode> response = unitService.computedInheritedRules(vitamContext, dslQuery);
        return response.toJsonNode();
    }


    public JsonNode selectUnitWithInheritedRules(final JsonNode dslQuery, final VitamContext vitamContext)
        throws VitamClientException {
        RequestResponse<JsonNode> response = unitService.selectUnitsWithInheritedRules(vitamContext, dslQuery);
        return response.toJsonNode();
    }

    public ResultsDto selectUnitWithInheritedRules(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext)
        throws VitamClientException, IOException {
        ResultsDto response = new ResultsDto();
        LOGGER.debug("calling select Units With Inherited Rules by criteria {} ", searchQuery.toString());
        archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
        JsonNode dslQuery = mapRequestToDslQuery(searchQuery);
        rulesUpdateCommonService.deleteAttributesFromObjectNode((ObjectNode) dslQuery, DSL_QUERY_FACETS);
        JsonNode vitamResponse = selectUnitWithInheritedRules(dslQuery, vitamContext);
        ArchiveUnitsDto archiveUnitsDto = decorateAndMapResponse(vitamResponse, vitamContext);
        if (Objects.nonNull(archiveUnitsDto.getArchives()) &&
            !CollectionUtils.isEmpty(archiveUnitsDto.getArchives().getResults())) {
            response = archiveUnitsDto.getArchives().getResults().get(0);
        }
        return response;
    }

    public String reclassification(final VitamContext vitamContext,
        final ReclassificationCriteriaDto reclassificationCriteriaDto)
        throws VitamClientException {
        if (reclassificationCriteriaDto == null) {
            throw new BadRequestException("Error reclassification criteria");
        }
        LOGGER.debug("Reclassification Object : {}", reclassificationCriteriaDto.toString());
        JsonNode dslQuery = mapRequestToDslQuery(reclassificationCriteriaDto.getSearchCriteriaDto());
        ArrayNode array = JsonHandler.createArrayNode();
        ((ObjectNode) dslQuery).putPOJO(ACTION, reclassificationCriteriaDto.getAction());
        Arrays.stream(new String[] {DSL_QUERY_PROJECTION, DSL_QUERY_FILTER, DSL_QUERY_FACETS})
            .forEach(((ObjectNode) dslQuery)::remove);
        array.add(dslQuery);
        LOGGER.debug("Reclassification query : {}", array);
        RequestResponse<JsonNode> jsonNodeRequestResponse = unitService.reclassification(vitamContext, array);
        return jsonNodeRequestResponse.toJsonNode().findValue(OPERATION_IDENTIFIER).textValue();

    }

}
