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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.facet.FacetHelper;
import fr.gouv.vitam.common.database.builder.facet.RangeFacetValue;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.dip.DataObjectVersions;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitam.common.model.export.dip.DipExportType;
import fr.gouv.vitam.common.model.export.dip.DipRequest;
import fr.gouv.vitam.common.model.massupdate.MassUpdateUnitRuleRequest;
import fr.gouv.vitam.common.model.massupdate.RuleActions;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.converter.RuleOperationsConverter;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.service.RulesUpdateCommonService;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitCsv;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportSearchResultParam;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidTypeException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.RequestEntityTooLargeException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedSettingsException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.EliminationService;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetBucketDto;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.UnitTypeEnum;
import fr.gouv.vitamui.iam.common.dto.AccessContractsResponseDto;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper.unitType;

/**
 * Archive-Search Internal service communication with VITAM.
 */
@Service
public class ArchiveSearchInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalService.class);
    private static final String INGEST_ARCHIVE_TYPE = "INGEST";
    private static final String ARCHIVE_UNIT_DETAILS = "$results";
    private static final String DSL_QUERY_PROJECTION = "$projection";
    private static final String DSL_QUERY_FILTER = "$filter";
    private static final String DSL_QUERY_FACETS = "$facets";
    private static final Integer EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS = 10000;
    public static final String SEMI_COLON = ";";
    public static final String COMMA = ",";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String SINGLE_QUOTE = "'";
    public static final String NEW_LINE = "\n";
    public static final String NEW_TAB = "\t";
    public static final String NEW_LINE_1 = "\r\n";
    public static final String OPERATION_IDENTIFIER = "itemId";
    public static final String SPACE = " ";
    public static final String TITLE_FIELD = "Title";
    public static final String FILING_UNIT = "FILING_UNIT";
    public static final String HOLDING_UNIT = "HOLDING_UNIT";
    private static final String[] FILING_PLAN_PROJECTION =
        new String[] {"#id", TITLE_FIELD, "Title_", "DescriptionLevel", "#unitType", "#unitups", "#allunitups"};
    public static final String SOME_OLD_DATE = "01/01/0001";
    public static final String FALSE = "false";
    public static final String TRUE = "true";

    private static final String SET = "$set";
    private static final String UNSET = "$unset";
    private static final String ACTION = "$action";

    private final ObjectMapper objectMapper;
    private final UnitService unitService;
    private final EliminationService eliminationService;
    private final ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;
    private final ArchiveSearchRulesInternalService archiveSearchRulesInternalService;
    private final ArchivesSearchManagementRulesQueryBuilderService archivesSearchManagementRulesQueryBuilderService;
    private final ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService;
    private final ExportDipV2Service exportDipV2Service;
    private final RuleOperationsConverter ruleOperationsConverter;
    private final AccessContractService accessContractService;


    @Autowired
    public ArchiveSearchInternalService(final ObjectMapper objectMapper, final UnitService unitService,
        final ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService,
        final ArchiveSearchRulesInternalService archiveSearchRulesInternalService,
        final ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService,
        final ExportDipV2Service exportDipV2Service,
        final ArchivesSearchManagementRulesQueryBuilderService archivesSearchManagementRulesQueryBuilderService,
        final EliminationService eliminationService,
        final RuleOperationsConverter ruleOperationsConverter,
        final AccessContractService accessContractService

    ) {
        this.unitService = unitService;
        this.objectMapper = objectMapper;
        this.archiveSearchAgenciesInternalService = archiveSearchAgenciesInternalService;
        this.archiveSearchRulesInternalService = archiveSearchRulesInternalService;
        this.archivesSearchFieldsQueryBuilderService = archivesSearchFieldsQueryBuilderService;
        this.archivesSearchManagementRulesQueryBuilderService = archivesSearchManagementRulesQueryBuilderService;
        this.exportDipV2Service = exportDipV2Service;
        this.eliminationService = eliminationService;
        this.ruleOperationsConverter = ruleOperationsConverter;
        this.accessContractService = accessContractService;
    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext)
        throws VitamClientException, IOException {
        try {
            LOGGER.debug("calling find archive units by criteria {} ", searchQuery.toString());
            archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
            archiveSearchRulesInternalService.mapManagementRulesTitlesToCodes(searchQuery, vitamContext);
            List<String> nodesCriteriaList = searchQuery.extractNodesCriteria();
            List<SearchCriteriaEltDto> waitingToRecalculateCriteria = searchQuery
                .extractCriteriaListByCategoryAndFieldNames(ArchiveSearchConsts.CriteriaCategory.FIELDS,
                    List.of(ArchiveSearchConsts.WAITING_RECALCULATE));

            List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList =
                searchQuery.extractCriteriaListByCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
            List<SearchCriteriaEltDto> accessMgtRulesCriteriaList =
                searchQuery.extractCriteriaListByCategory(ArchiveSearchConsts.CriteriaCategory.ACCESS_RULE);

            if (!CollectionUtils.isEmpty(waitingToRecalculateCriteria) &&
                (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList) ||
                    !CollectionUtils.isEmpty(accessMgtRulesCriteriaList))) {
                List<SearchCriteriaEltDto> initialCriteriaList = searchQuery.getCriteriaList().stream().filter(
                    searchCriteriaEltDto ->
                        !(ArchiveSearchConsts.CriteriaCategory.FIELDS.equals(searchCriteriaEltDto.getCategory()) &&
                            (searchCriteriaEltDto.getCriteria()
                                .equals(ArchiveSearchConsts.WAITING_RECALCULATE)))
                ).collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList)) {
                    mergeValidComputedInheritenceCriteriaWithAppraisalCriteria(initialCriteriaList,
                        ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
                }
                if (!CollectionUtils.isEmpty(accessMgtRulesCriteriaList)) {
                    mergeValidComputedInheritenceCriteriaWithAppraisalCriteria(initialCriteriaList,
                        ArchiveSearchConsts.CriteriaCategory.ACCESS_RULE);
                }
                searchQuery.setCriteriaList(initialCriteriaList);
            }
            SelectMultiQuery selectMultiQuery = mapRequestToSelectMultiQuery(searchQuery);
            //we choose a count of :  100 * (1+ nb of nodes criteria) for example to compute the number of facets in holding schema
            selectMultiQuery
                .addFacets(FacetHelper.terms(ArchiveSearchConsts.FACETS_COUNT_BY_NODE, ArchiveSearchConsts.UNITS_UPS,
                    (nodesCriteriaList.size() + 1) * ArchiveSearchConsts.FACET_SIZE_MILTIPLIER, FacetOrder.ASC));

            if (computeFacets(searchQuery)) {
                selectMultiQuery.addFacets(
                    FacetHelper.terms(ArchiveSearchConsts.FACETS_AU_COUNT_WITH_COMPUTED_INHERITANCE,
                        ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING.get(ArchiveSearchConsts.RULES_COMPUTED), 3,
                        FacetOrder.ASC));
            }
            JsonNode vitamResponse = searchArchiveUnits(selectMultiQuery.getFinalSelect(), vitamContext);
            ArchiveUnitsDto archiveUnitsDto = decorateAndMapResponse(vitamResponse, vitamContext);
            fillFacets(searchQuery, archiveUnitsDto, vitamContext);

            return archiveUnitsDto;
        } catch (InvalidCreateOperationException ioe) {
            throw new VitamClientException("Unable to find archive units with pagination", ioe);
        }
    }


    public void fillFacets(SearchCriteriaDto searchQuery, ArchiveUnitsDto archiveUnitsDto, VitamContext
        vitamContext) throws InvalidCreateOperationException, VitamClientException, JsonProcessingException {
        if (computeFacets(searchQuery)) {
            try {
                List<FacetResultsDto> facetResults = archiveUnitsDto.getArchives().getFacetResults();
                if (CollectionUtils.isEmpty(facetResults)) {
                    facetResults = new ArrayList<>();
                }
                facetResults.addAll(computeFacetsForIndexedRulesCriteria(searchQuery, vitamContext));
                archiveUnitsDto.getArchives().setFacetResults(facetResults);
            } catch (UnexpectedSettingsException e) {
                LOGGER.info("Could not compute facets,the setting track total hits is not allowed in  vitam ");
            }
        }
    }

    private List<FacetResultsDto> computeFacetsForIndexedRulesCriteria(SearchCriteriaDto searchQuery,
        final VitamContext vitamContext)
        throws InvalidCreateOperationException, VitamClientException, JsonProcessingException {
        LOGGER.debug("Start finding facets for computed rules  ");

        List<FacetResultsDto> globalRulesFacets = new ArrayList<>();
        try {
            List<ArchiveSearchConsts.CriteriaCategory> categories =
                List.of(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
            List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList = new ArrayList<>(
                searchQuery.getCriteriaList());
            indexedArchiveUnitsCriteriaList.add(new SearchCriteriaEltDto(ArchiveSearchConsts.RULES_COMPUTED,
                ArchiveSearchConsts.CriteriaCategory.FIELDS, ArchiveSearchConsts.CriteriaOperators.EQ.name(),
                List.of(new CriteriaValue(TRUE)), ArchiveSearchConsts.CriteriaDataType.STRING.name()));
            SelectMultiQuery selectMultiQuery = createSelectMultiQuery(indexedArchiveUnitsCriteriaList);
            selectMultiQuery.addUsedProjection("#id");
            selectMultiQuery.setLimitFilter(0, 1);
            selectMultiQuery.trackTotalHits(searchQuery.isTrackTotalHits());
            JsonNode vitamResponse =
                this.searchArchiveUnits(selectMultiQuery.getFinalSelect(), vitamContext);
            VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
                VitamUISearchResponseDto.class);
            List<FacetResultsDto> indexedAuFacets = archivesUnitsResults.getFacetResults();
            globalRulesFacets.addAll(indexedAuFacets);

            for (ArchiveSearchConsts.CriteriaCategory category : categories) {
                FacetResultsDto withoutRulesByCategoryFacet =
                    computeNoRulesFacets(searchQuery, indexedArchiveUnitsCriteriaList, category, vitamContext);
                globalRulesFacets.add(withoutRulesByCategoryFacet);
                List<FacetResultsDto> facetsForAuHavingRules =
                    computeFacetsForAuHavingRules(searchQuery, indexedArchiveUnitsCriteriaList, category, vitamContext);
                globalRulesFacets.addAll(facetsForAuHavingRules);
            }
        } catch (InvalidParseOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query" + e.getMessage());
        }
        return globalRulesFacets;
    }

    private List<FacetResultsDto> computeFacetsForAuHavingRules(SearchCriteriaDto searchQuery,
        List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList, ArchiveSearchConsts.CriteriaCategory
        category,
        VitamContext vitamContext)
        throws InvalidParseOperationException, InvalidCreateOperationException, VitamClientException,
        JsonProcessingException {

        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>(indexedArchiveUnitsCriteriaList);

        criteriaList.add(new SearchCriteriaEltDto(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA,
            category, ArchiveSearchConsts.CriteriaOperators.EQ.name(),
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_LOCAL_OR_INHERIT_RULES.name())),
            ArchiveSearchConsts.CriteriaDataType.STRING.name()));

        SelectMultiQuery selectMultiQuery = createSelectMultiQuery(criteriaList);

        selectMultiQuery.setLimitFilter(0, 1);
        selectMultiQuery.trackTotalHits(searchQuery.isTrackTotalHits());

        try {
            List<SearchCriteriaEltDto> rulesCriteriaList =
                indexedArchiveUnitsCriteriaList.stream().filter(Objects::nonNull)
                    .filter(searchCriteriaEltDto -> (category.equals(searchCriteriaEltDto.getCategory())))
                    .collect(Collectors.toList());
            String computedRulesIdentifierMapping =
                ArchivesSearchManagementRulesQueryBuilderService.COMPUTED_FIELDS
                    +
                    ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() +
                    ArchivesSearchManagementRulesQueryBuilderService.RULES_RULE_ID_FIELD;
            if (ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE.equals(category)) {
                String computedRulesFinalActionMapping =
                    ArchivesSearchManagementRulesQueryBuilderService.COMPUTED_FIELDS
                        + ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name())
                        .getFieldMapping() + ArchivesSearchManagementRulesQueryBuilderService.FINAL_ACTION_FIELD;
                selectMultiQuery.addFacets(
                    FacetHelper.terms(ArchiveSearchConsts.FACETS_FINAL_ACTION_COMPUTED + "_" + category.name(),
                        computedRulesFinalActionMapping, 3, FacetOrder.ASC));
            }
            selectMultiQuery.addFacets(
                FacetHelper.terms(ArchiveSearchConsts.FACETS_RULES_COMPUTED_NUMBER + "_" + category.name(),
                    computedRulesIdentifierMapping, 100, FacetOrder.ASC));
            addExpirationRulesFacet(rulesCriteriaList, category, selectMultiQuery);

        } catch (DateTimeParseException e) {
            throw new InvalidCreateOperationException(e);
        }

        JsonNode dslQuery = selectMultiQuery.getFinalSelect();
        LOGGER.debug(dslQuery.toPrettyString());
        JsonNode vitamResponse = searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
            VitamUISearchResponseDto.class);
        List<FacetResultsDto> auWithRulesFacets = archivesUnitsResults.getFacetResults();

        if (ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE.equals(category)) {
            FacetResultsDto finalActionIndexedFacet = buildComputedAuFinalActionFacet(
                indexedArchiveUnitsCriteriaList, category,
                auWithRulesFacets, vitamContext);
            auWithRulesFacets = auWithRulesFacets.stream()
                .filter(facet -> !(ArchiveSearchConsts.FACETS_FINAL_ACTION_COMPUTED + "_" + category.name())
                    .equals(facet.getName()))
                .collect(Collectors.toList());
            auWithRulesFacets.add(finalActionIndexedFacet);
        }
        return auWithRulesFacets;
    }


    public FacetResultsDto buildComputedAuFinalActionFacet
        (List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList,
            ArchiveSearchConsts.CriteriaCategory category, List<FacetResultsDto> indexedRulesFacets,
            VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        FacetResultsDto finalActionIndexedFacet = new FacetResultsDto();
        if (ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE.equals(category)) {
            List<FacetBucketDto> finalActionBuckets = computeFinalActionFacetsForComputedAppraisalRules(
                indexedArchiveUnitsCriteriaList, indexedRulesFacets, category, vitamContext);
            finalActionIndexedFacet.setName(ArchiveSearchConsts.FACETS_FINAL_ACTION_COMPUTED + "_" + category.name());
            finalActionIndexedFacet.setBuckets(finalActionBuckets);
        }
        return finalActionIndexedFacet;
    }

    @NotNull
    private List<FacetBucketDto> computeFinalActionFacetsForComputedAppraisalRules(
        List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList, List<FacetResultsDto>
        indexedRulesFacets,
        ArchiveSearchConsts.CriteriaCategory category, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        Map<String, Long> finalActionCountMap = new HashMap<>();
        finalActionCountMap.put(ArchiveSearchConsts.FINAL_ACTION_KEEP_FIELD_VALUE, 0l);
        finalActionCountMap.put(ArchiveSearchConsts.FINAL_ACTION_DESTROY_FIELD_VALUE, 0l);
        finalActionCountMap.put(ArchiveSearchConsts.FINAL_ACTION_CONFLICT_FIELD_VALUE, 0l);
        Optional<FacetResultsDto> facetFinalActionValue = indexedRulesFacets.stream()
            .filter(facet -> (ArchiveSearchConsts.FACETS_FINAL_ACTION_COMPUTED + "_" + category.name()).equals(
                facet.getName()))
            .findAny();
        facetFinalActionValue.ifPresent(facetResultsDto -> facetResultsDto.getBuckets().stream()
            .forEach(bucket -> finalActionCountMap.put(bucket.getValue(), bucket.getCount())));
        Integer withConflictFinalActionUnitsCount = computeFinalActionCountByValue(
            indexedArchiveUnitsCriteriaList, ArchiveSearchConsts.FINAL_ACTION_TYPE_CONFLICT, category, vitamContext);
        finalActionCountMap
            .put(ArchiveSearchConsts.FINAL_ACTION_CONFLICT_FIELD_VALUE,
                Long.valueOf(withConflictFinalActionUnitsCount));
        if (withConflictFinalActionUnitsCount != 0) {
            Long withKeepFinalActionAppraisalRulesUnitsCount =
                finalActionCountMap.get(ArchiveSearchConsts.FINAL_ACTION_KEEP_FIELD_VALUE);
            Long withDestroyFinalActionAppraisalRulesUnitsCount = finalActionCountMap
                .get(ArchiveSearchConsts.FINAL_ACTION_DESTROY_FIELD_VALUE);
            if (withKeepFinalActionAppraisalRulesUnitsCount > 0) {
                finalActionCountMap.put(ArchiveSearchConsts.FINAL_ACTION_KEEP_FIELD_VALUE,
                    withKeepFinalActionAppraisalRulesUnitsCount - withConflictFinalActionUnitsCount);
            }
            if (withDestroyFinalActionAppraisalRulesUnitsCount > 0) {
                finalActionCountMap.put(ArchiveSearchConsts.FINAL_ACTION_DESTROY_FIELD_VALUE,
                    withDestroyFinalActionAppraisalRulesUnitsCount -
                        withConflictFinalActionUnitsCount);
            }
        }
        List<FacetBucketDto> finalActionBuckets = new ArrayList<>();
        for (Map.Entry<String, Long> entry : finalActionCountMap.entrySet()) {
            finalActionBuckets.add(new FacetBucketDto(entry.getKey(), entry.getValue()));
        }
        return finalActionBuckets;
    }

    private void mergeValidComputedInheritenceCriteriaWithAppraisalCriteria(
        List<SearchCriteriaEltDto> initialCriteriaList,
        ArchiveSearchConsts.CriteriaCategory criteriaCategory) {

        long originRulesCriteriaCount = initialCriteriaList.stream().filter(
            searchCriteriaEltDto -> criteriaCategory.equals(searchCriteriaEltDto.getCategory()) &&
                (searchCriteriaEltDto.getCriteria().equals(ArchiveSearchConsts.RULE_ORIGIN_CRITERIA))
        ).count();

        if (originRulesCriteriaCount > 0) {
            initialCriteriaList.stream().forEach(searchCriteriaEltDto -> {
                if (criteriaCategory.equals(searchCriteriaEltDto.getCategory()) && (
                    ArchiveSearchConsts.RULE_ORIGIN_CRITERIA.equals(searchCriteriaEltDto.getCriteria())
                )) {
                    List<CriteriaValue> values = searchCriteriaEltDto.getValues();
                    values
                        .add(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_WAITING_RECALCULATE.name()));
                    searchCriteriaEltDto.setValues(values);
                }
            });
        } else {
            SearchCriteriaEltDto criteria = new SearchCriteriaEltDto();
            criteria.setCriteria(ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
            criteria.setCategory(criteriaCategory);
            criteria.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
            criteria.setValues(
                List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_WAITING_RECALCULATE.name())));
            initialCriteriaList.add(criteria);
        }
    }

    private FacetResultsDto computeRulesCountByComputedRulesStatusFacets(ArchiveUnitsDto archiveUnitsDto) {
        FacetResultsDto computeAuFacetUpdated = new FacetResultsDto();
        Map<String, Long> countByStatusMap = new HashMap<>();
        countByStatusMap.put(TRUE, 0l);
        countByStatusMap.put(FALSE, 0l);
        if (archiveUnitsDto != null && archiveUnitsDto.getArchives() != null &&
            !CollectionUtils.isEmpty(archiveUnitsDto.getArchives().getFacetResults())) {
            Integer totalCount = archiveUnitsDto.getArchives().getHits().getTotal();
            Optional<FacetResultsDto> computeRulesAuCountFacetOpt =
                archiveUnitsDto.getArchives().getFacetResults().stream().filter(
                        (facet -> facet.getName().equals(ArchiveSearchConsts.FACETS_AU_COUNT_WITH_COMPUTED_INHERITANCE)))
                    .findAny();
            if (computeRulesAuCountFacetOpt.isPresent() &&
                !CollectionUtils.isEmpty(computeRulesAuCountFacetOpt.get().getBuckets())) {
                computeRulesAuCountFacetOpt.get().getBuckets().stream().forEach(
                    bucket -> {
                        if (FALSE.equals(bucket.getValue())) {
                            countByStatusMap.put(FALSE, bucket.getCount());
                        } else if (TRUE.equals(bucket.getValue())) {
                            countByStatusMap.put(TRUE, bucket.getCount());
                        }
                    });
                countByStatusMap
                    .put(FALSE, (totalCount - countByStatusMap.get(TRUE)) + countByStatusMap.get(FALSE));
            } else {
                countByStatusMap.put(FALSE, Long.valueOf(totalCount));
            }
            computeAuFacetUpdated.setName(ArchiveSearchConsts.FACETS_AU_COUNT_WITH_COMPUTED_INHERITANCE);
            List<FacetBucketDto> bucketDtos = new ArrayList<>();
            for (Map.Entry<String, Long> entry : countByStatusMap.entrySet()) {
                FacetBucketDto bucketDto = new FacetBucketDto();
                bucketDto.setCount(entry.getValue());
                bucketDto.setValue(entry.getKey());
                bucketDtos.add(bucketDto);
            }
            computeAuFacetUpdated.setBuckets(bucketDtos);
        }
        return computeAuFacetUpdated;
    }

    public FacetResultsDto computeNoRulesFacets(SearchCriteriaDto searchQuery,
        List<SearchCriteriaEltDto> indexedCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        List<SearchCriteriaEltDto> criteriaListFacet = new ArrayList<>(indexedCriteriaList);

        criteriaListFacet.add(new SearchCriteriaEltDto(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA,
            category, ArchiveSearchConsts.CriteriaOperators.EQ.name(),
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_NO_ONE.name())),
            ArchiveSearchConsts.CriteriaDataType.STRING.name()));

        FacetResultsDto noRuleFacet = new FacetResultsDto();
        noRuleFacet.setName(ArchiveSearchConsts.FACETS_COUNT_WITHOUT_RULES + "_" + category.name());
        noRuleFacet.setBuckets(
            List.of(new FacetBucketDto(ArchiveSearchConsts.FACETS_COUNT_WITHOUT_RULES,
                Long.valueOf(countArchiveUnitByCriteriaList(searchQuery, criteriaListFacet, vitamContext)))));
        return noRuleFacet;
    }

    private Integer countArchiveUnitByCriteriaList(SearchCriteriaDto searchQuery,
        List<SearchCriteriaEltDto> criteriaList, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        SearchCriteriaDto facetSearchQuery = new SearchCriteriaDto();
        facetSearchQuery.setCriteriaList(criteriaList);
        facetSearchQuery.setFieldsList(List.of("#id"));
        facetSearchQuery.setSize(1);
        facetSearchQuery.setTrackTotalHits(searchQuery.isTrackTotalHits());

        facetSearchQuery.setFieldsList(List.of(TITLE_FIELD));
        JsonNode dslQuery = mapRequestToDslQuery(facetSearchQuery);
        JsonNode vitamResponse = searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults =
            objectMapper.treeToValue(vitamResponse, VitamUISearchResponseDto.class);
        return archivesUnitsResults.getHits().getTotal();
    }

    private Integer computeFinalActionCountByValue(List<SearchCriteriaEltDto> initialCriteriaList, String
        value, ArchiveSearchConsts.CriteriaCategory category, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {

        List<SearchCriteriaEltDto> criteriaListFacet = new ArrayList<>();
        criteriaListFacet.addAll(initialCriteriaList);
        SearchCriteriaDto countSearchQuery = new SearchCriteriaDto();
        criteriaListFacet.add(new SearchCriteriaEltDto(
            ArchiveSearchConsts.RULE_FINAL_ACTION_TYPE,
            category, ArchiveSearchConsts.CriteriaOperators.EQ.name(),
            List.of(new CriteriaValue(value)),
            ArchiveSearchConsts.CriteriaDataType.STRING.name()));

        criteriaListFacet.add(new SearchCriteriaEltDto(
            ArchiveSearchConsts.RULES_COMPUTED,
            ArchiveSearchConsts.CriteriaCategory.FIELDS, ArchiveSearchConsts.CriteriaOperators.EQ.name(),
            List.of(new CriteriaValue(TRUE)),
            ArchiveSearchConsts.CriteriaDataType.STRING.name()));

        countSearchQuery.setCriteriaList(criteriaListFacet);
        countSearchQuery.setFieldsList(List.of(ArchiveSearchInternalService.TITLE_FIELD));
        JsonNode dslQuery = mapRequestToDslQuery(countSearchQuery);
        JsonNode vitamResponse = searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
            VitamUISearchResponseDto.class);
        return archivesUnitsResults.getHits().getTotal();
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
                if (direction.isPresent() && DirectionDto.DESC.equals(direction.get())) {
                    selectMultiQuery.addOrderByDescFilter(orderBy.get());
                } else {
                    selectMultiQuery.addOrderByAscFilter(orderBy.get());
                }
            }
            selectMultiQuery.setLimitFilter(searchQuery.getPageNumber() * searchQuery.getSize(), searchQuery.getSize());
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
            LOGGER.info("Archive Unit Id : {}", id);
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
            LOGGER.info("Get Object Group");
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
        LOGGER.info("Download Archive Unit Object with id {} , usage {} and version {}  ", id, usage, version);
        return unitService
            .getObjectStreamByUnitId(id, usage, version, vitamContext);
    }

    /**
     * Export archive unit by criteria into csv file
     *
     * @param searchQuery
     * @param vitamContext
     * @throws VitamClientException
     * @throws IOException
     */
    public Resource exportToCsvSearchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext) throws VitamClientException {
        LOGGER.info("Calling exportToCsvSearchArchiveUnitsByCriteria with query {} ", searchQuery);
        Locale locale = Locale.FRENCH;
        if (Locale.FRENCH.getLanguage().equals(searchQuery.getLanguage()) ||
            Locale.ENGLISH.getLanguage().equals(searchQuery.getLanguage())) {
            locale = Locale.forLanguageTag(searchQuery.getLanguage());
        }
        ExportSearchResultParam exportSearchResultParam = new ExportSearchResultParam(locale);
        return exportToCsvSearchArchiveUnitsByCriteriaAndParams(searchQuery, exportSearchResultParam, vitamContext);
    }


    /**
     * export ToCsv Search ArchiveUnits By Criteria And Params by language
     *
     * @param searchQuery
     * @param exportSearchResultParam
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    private Resource exportToCsvSearchArchiveUnitsByCriteriaAndParams(final SearchCriteriaDto searchQuery, final
    ExportSearchResultParam exportSearchResultParam, final VitamContext vitamContext)
        throws VitamClientException {
        try {
            archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
            List<ArchiveUnitCsv> unitCsvList = exportArchiveUnitsByCriteriaToCsvFile(searchQuery, vitamContext);
            // create a write
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8.name());
            // header record
            String[] headerRecordFr =
                exportSearchResultParam.getHeaders().toArray(new String[exportSearchResultParam.getHeaders().size()]);
            SimpleDateFormat dateFormat = new SimpleDateFormat(exportSearchResultParam.getPatternDate());
            // create a csv writer
            ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(exportSearchResultParam.getSeparator())
                .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                .withEscapeChar(ICSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .withLineEnd(ICSVWriter.DEFAULT_LINE_END)
                .build();
            // write header record
            csvWriter.writeNext(headerRecordFr);

            // write data records
            unitCsvList.stream().forEach(archiveUnitCsv -> {
                String startDt = null;
                String endDt = null;
                if (archiveUnitCsv.getStartDate() != null) {
                    try {
                        startDt = dateFormat.format(LocalDateUtil.getDate(archiveUnitCsv.getStartDate()));
                    } catch (ParseException e) {
                        LOGGER.error("Error parsing starting date {} ", archiveUnitCsv.getStartDate());
                    }
                }
                if (archiveUnitCsv.getEndDate() != null) {
                    try {
                        endDt = dateFormat.format(LocalDateUtil.getDate(archiveUnitCsv.getEndDate()));
                    } catch (ParseException e) {
                        LOGGER.error("Error parsing end date {} ", archiveUnitCsv.getEndDate());
                    }
                }
                csvWriter.writeNext(new String[] {archiveUnitCsv.getId(), archiveUnitCsv.getArchiveUnitType(),
                    archiveUnitCsv.getOriginatingAgencyName(),
                    exportSearchResultParam.getDescriptionLevelMap().get(archiveUnitCsv.getDescriptionLevel()),
                    archiveUnitCsv.getTitle(),
                    startDt, endDt,
                    archiveUnitCsv.getDescription()});
            });
            // close writers
            csvWriter.close();
            writer.close();
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new BadRequestException("Unable to export csv file ", ex);
        }
    }


    private List<ArchiveUnitCsv> exportArchiveUnitsByCriteriaToCsvFile(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.info("Calling exporting  export ArchiveUnits to CSV with criteria {}", searchQuery);
            checkSizeLimit(vitamContext, searchQuery);
            searchQuery.setPageNumber(0);
            searchQuery.setSize(EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
            JsonNode archiveUnitsResult =
                searchArchiveUnits(mapRequestToDslQuery(searchQuery), vitamContext);
            final VitamUISearchResponseDto archivesResponse =
                objectMapper.treeToValue(archiveUnitsResult, VitamUISearchResponseDto.class);
            LOGGER.info("archivesResponse found {} ", archivesResponse.getResults().size());
            Set<String> originesAgenciesCodes = archivesResponse.getResults().stream().map(
                    ResultsDto::getOriginatingAgency).
                filter(Objects::nonNull).collect(Collectors.toSet());

            List<AgencyModelDto> originAgenciesFound =
                archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(vitamContext, originesAgenciesCodes);
            Map<String, AgencyModelDto> agenciesMapByIdentifier =
                originAgenciesFound.stream().collect(Collectors.toMap(AgencyModelDto::getIdentifier, agency -> agency));
            return archivesResponse.getResults().stream().map(
                    archiveUnit -> archiveSearchAgenciesInternalService
                        .fillOriginatingAgencyName(archiveUnit, agenciesMapByIdentifier)
                ).map(archiveUnit -> cleanAndMapArchiveUnitResult(archiveUnit, searchQuery.getLanguage()))
                .collect(Collectors.toList());

        } catch (IOException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query", e);
        }
    }

    private String getArchiveUnitTitle(ArchiveUnit archiveUnit) {
        String title = null;
        if (archiveUnit != null) {
            if (StringUtils.isEmpty(archiveUnit.getTitle()) || StringUtils.isBlank(archiveUnit.getTitle())) {
                if (archiveUnit.getTitle_() != null) {
                    if (!StringUtils.isEmpty(archiveUnit.getTitle_().getFr()) &&
                        !StringUtils.isBlank(archiveUnit.getTitle_().getFr())) {
                        title = archiveUnit.getTitle_().getFr();
                    } else {
                        title = archiveUnit.getTitle_().getEn();
                    }
                }
            } else {
                title = archiveUnit.getTitle();
            }
        }
        return title;
    }


    private ArchiveUnitCsv cleanAndMapArchiveUnitResult(ArchiveUnit archiveUnit, String language) {
        if (archiveUnit == null) {
            return null;
        }
        ArchiveUnitCsv archiveUnitCsv = new ArchiveUnitCsv();
        BeanUtils.copyProperties(archiveUnit, archiveUnitCsv);
        archiveUnitCsv.setDescription(
            archiveUnit.getDescription() != null ? cleanString(archiveUnit.getDescription()) : null);
        archiveUnitCsv.setDescriptionLevel(
            archiveUnit.getDescriptionLevel() != null ? cleanString(archiveUnit.getDescriptionLevel()) : null);
        archiveUnitCsv.setArchiveUnitType(getArchiveUnitType(archiveUnit, language));
        archiveUnitCsv.setTitle(cleanString(getArchiveUnitTitle(archiveUnit)));
        archiveUnitCsv.setOriginatingAgencyName(
            archiveUnit.getOriginatingAgencyName() != null ? cleanString(archiveUnit.getOriginatingAgencyName()) :
                null);
        return archiveUnitCsv;
    }

    private String getArchiveUnitType(ArchiveUnit archiveUnit, String language) {
        String archiveUnitType = null;
        if (archiveUnit != null && !StringUtils.isEmpty(archiveUnit.getUnitType())) {
            switch (archiveUnit.getUnitType()) {
                case FILING_UNIT:
                    archiveUnitType = language.equals(Locale.FRENCH.getLanguage()) ?
                        ExportSearchResultParam.FR_AU_FILING_SCHEME :
                        ExportSearchResultParam.EN_AU_FILING_SCHEME;
                    break;
                case HOLDING_UNIT:
                    archiveUnitType = language.equals(Locale.FRENCH.getLanguage()) ?
                        ExportSearchResultParam.FR_AU_HOLDING_SCHEME :
                        ExportSearchResultParam.EN_AU_HOLDING_SCHEME;
                    break;
                case INGEST_ARCHIVE_TYPE:
                    if (StringUtils.isEmpty(archiveUnit.getUnitObject())) {
                        archiveUnitType = language.equals(Locale.FRENCH.getLanguage()) ?
                            ExportSearchResultParam.FR_AU_WITHOUT_OBJECT :
                            ExportSearchResultParam.EN_AU_WITHOUT_OBJECT;
                    } else {
                        archiveUnitType = language.equals(Locale.FRENCH.getLanguage()) ?
                            ExportSearchResultParam.FR_AU_WITH_OBJECT :
                            ExportSearchResultParam.EN_AU_WITH_OBJECT;
                    }
                    break;
                default:
                    throw new InvalidTypeException("Description Level Type is Unknown !");
            }
        }
        return archiveUnitType;
    }

    private String cleanString(String initialValue) {
        if (initialValue == null)
            return null;
        return initialValue.replace(SEMI_COLON, COMMA).replace(DOUBLE_QUOTE, SINGLE_QUOTE)
            .replace(NEW_LINE, SPACE)
            .replace(NEW_LINE_1, SPACE)
            .replace(NEW_TAB, SPACE);
    }

    /**
     * check limit of results limit
     *
     * @param vitamContext
     * @param searchQuery
     */
    private void checkSizeLimit(VitamContext vitamContext, SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException {
        SearchCriteriaDto searchQueryCounting = new SearchCriteriaDto();
        searchQueryCounting.setCriteriaList(searchQuery.getCriteriaList());
        JsonNode archiveUnitsResult =
            searchArchiveUnits(mapRequestToDslQuery(searchQueryCounting),
                vitamContext);
        final VitamUISearchResponseDto archivesOriginResponse =
            objectMapper.treeToValue(archiveUnitsResult, VitamUISearchResponseDto.class);
        Integer nbResults = archivesOriginResponse.getHits().getTotal();
        if (nbResults >= EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS) {
            LOGGER.error("The archives units result found is greater than allowed {} ",
                EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
            throw new RequestEntityTooLargeException(
                "The archives units result found is greater than allowed:  " + EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
        }
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
            Objects::nonNull).filter(searchCriteriaEltDto -> ArchiveSearchConsts.CriteriaCategory.FIELDS
            .equals(searchCriteriaEltDto.getCategory())).collect(Collectors.toList());
        List<String> nodesCriteriaList = criteriaList.stream().filter(
                Objects::nonNull).filter(searchCriteriaEltDto -> ArchiveSearchConsts.CriteriaCategory.NODES
                .equals(searchCriteriaEltDto.getCategory())).flatMap(criteria -> criteria.getValues().stream())
            .map(CriteriaValue::getValue).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(nodesCriteriaList)) {
            select.addRoots(nodesCriteriaList.toArray(new String[nodesCriteriaList.size()]));
            query.setDepthLimit(ArchiveSearchConsts.DEFAULT_DEPTH);
        }
        archivesSearchFieldsQueryBuilderService.fillQueryFromCriteriaList(query, simpleCriteriaList);
        archivesSearchManagementRulesQueryBuilderService.fillQueryFromMgtRulesCriteriaList(query, mgtRulesCriteriaList);
        if (query.isReady()) {
            select.setQuery(query);
        }
        LOGGER.debug("Final query: {}", select.getFinalSelect().toPrettyString());

        return select;
    }

    private void addExpirationRulesFacet(List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category, SelectMultiQuery select)
        throws InvalidCreateOperationException {
        String strDateExpirationCriteria =
            extractRuleExpirationDateFromCriteria(mgtRulesCriteriaList, category);
        String managementRuleEndDateMapping = ArchivesSearchManagementRulesQueryBuilderService.COMPUTED_FIELDS +
            ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() +
            ArchivesSearchManagementRulesQueryBuilderService.MAX_END_DATE_FIELD;
        select.addFacets(
            FacetHelper.dateRange(ArchiveSearchConsts.FACETS_EXPIRED_RULES_COMPUTED + "_" + category.name(),
                managementRuleEndDateMapping, ArchiveSearchConsts.FR_DATE_FORMAT_WITH_SLASH,
                List.of(new RangeFacetValue(SOME_OLD_DATE, strDateExpirationCriteria))));

        select.addFacets(
            FacetHelper.dateRange(ArchiveSearchConsts.FACETS_UNEXPIRED_RULES_COMPUTED + "_" + category.name(),
                managementRuleEndDateMapping, ArchiveSearchConsts.FR_DATE_FORMAT_WITH_SLASH,
                List.of(new RangeFacetValue(strDateExpirationCriteria, ArchiveSearchConsts.SOME_FUTUR_DATE))));
    }

    @NotNull
    private String extractRuleExpirationDateFromCriteria(List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category) {
        String strDateExpirationCriteria;
        Optional<SearchCriteriaEltDto> endDateCriteria = mgtRulesCriteriaList.stream().filter(
                searchCriteriaEltDto -> (category.equals(searchCriteriaEltDto.getCategory()) &&
                    ArchiveSearchConsts.RULE_END_DATE
                        .equals(searchCriteriaEltDto.getCriteria())))
            .findAny();
        if (endDateCriteria.isPresent() &&
            !CollectionUtils.isEmpty(endDateCriteria.get().getValues())) {

            String beginDtStr = endDateCriteria.get().getValues().get(0).getBeginInterval();
            String endDtStr = endDateCriteria.get().getValues().get(0).getEndInterval();
            LocalDateTime beginDt = null;
            if (!StringUtils.isEmpty(beginDtStr)) {
                beginDt = LocalDateTime.parse(beginDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
            }
            LocalDateTime endDt = null;
            if (!StringUtils.isEmpty(endDtStr)) {
                endDt = LocalDateTime.parse(endDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
            }
            if (beginDt != null && endDt != null) {
                if (endDt.isAfter(beginDt)) {
                    strDateExpirationCriteria = ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(endDt);
                } else {
                    strDateExpirationCriteria =
                        ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(beginDt);
                }
            } else if (beginDt != null) {
                strDateExpirationCriteria = ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(beginDt);
            } else if (endDt != null) {
                strDateExpirationCriteria = ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(endDt);
            } else {
                strDateExpirationCriteria =
                    ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(LocalDateTime.now());
            }
        } else {
            strDateExpirationCriteria =
                ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(LocalDateTime.now());
        }
        return strDateExpirationCriteria;
    }

    public String requestToExportDIP(final ExportDipCriteriaDto exportDipCriteriaDto,
        final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Export DIP by criteria {} ", exportDipCriteriaDto.toString());
        JsonNode dslQuery = prepareDslQuery(exportDipCriteriaDto.getExportDIPSearchCriteria(), vitamContext);
        LOGGER.debug("Export DIP final DSL query {} ", dslQuery);

        DataObjectVersions dataObjectVersionToExport = new DataObjectVersions();
        dataObjectVersionToExport.setDataObjectVersions(exportDipCriteriaDto.getDataObjectVersions());
        DipRequest dipRequest = prepareDipRequestBody(exportDipCriteriaDto, dslQuery);

        JsonNode response = exportDIP(vitamContext, dipRequest);
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }

    public JsonNode startEliminationAnalysis(final SearchCriteriaDto searchQuery, final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Elimination analysis by criteria {} ", searchQuery.toString());
        JsonNode dslQuery = prepareDslQuery(searchQuery, vitamContext);
        EliminationRequestBody eliminationRequestBody = null;
        try {
            eliminationRequestBody = getEliminationRequestBody(dslQuery);
        } catch (InvalidParseOperationException e) {
            throw new PreconditionFailedException("invalid request");
        }

        LOGGER.debug("Elimination analysis final query {} ",
            JsonHandler.prettyPrint(eliminationRequestBody.getDslRequest()));
        RequestResponse<JsonNode> jsonNodeRequestResponse =
            eliminationService.startEliminationAnalysis(vitamContext, eliminationRequestBody);


        return jsonNodeRequestResponse.toJsonNode();
    }

    public JsonNode startEliminationAction(final SearchCriteriaDto searchQuery, final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Elimination action by criteria {} ", searchQuery.toString());
        JsonNode dslQuery = prepareDslQuery(searchQuery, vitamContext);
        EliminationRequestBody eliminationRequestBody = null;
        try {
            eliminationRequestBody = getEliminationRequestBody(dslQuery);
        } catch (InvalidParseOperationException e) {
            throw new PreconditionFailedException("invalid request");
        }
        LOGGER.debug("Elimination action final query {} ",
            JsonHandler.prettyPrint(eliminationRequestBody.getDslRequest()));
        RequestResponse<JsonNode> jsonNodeRequestResponse =
            eliminationService.startEliminationAction(vitamContext, eliminationRequestBody);
        return jsonNodeRequestResponse.toJsonNode();
    }

    public EliminationRequestBody getEliminationRequestBody(JsonNode updateSet) throws InvalidParseOperationException {

        ObjectNode query = JsonHandler.createObjectNode();
        query.set(BuilderToken.GLOBAL.ROOTS.exactToken(), updateSet.get(BuilderToken.GLOBAL.ROOTS.exactToken()));
        query.set(BuilderToken.GLOBAL.QUERY.exactToken(), updateSet.get(BuilderToken.GLOBAL.QUERY.exactToken()));
        EliminationRequestBody requestBody = new EliminationRequestBody();
        requestBody.setDate(new SimpleDateFormat(ArchiveSearchConsts.ONLY_DATE_FORMAT).format(new Date()));
        requestBody.setDslRequest(query);
        return requestBody;
    }

    private JsonNode prepareDslQuery(final SearchCriteriaDto searchQuery, final VitamContext vitamContext)
        throws VitamClientException {
        searchQuery.setPageNumber(0);
        archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
        archiveSearchRulesInternalService.mapManagementRulesTitlesToCodes(searchQuery, vitamContext);
        return mapRequestToDslQuery(searchQuery);
    }

    private DipRequest prepareDipRequestBody(final ExportDipCriteriaDto exportDipCriteriaDto, JsonNode dslQuery) {
        DipRequest dipRequest = new DipRequest();

        if (exportDipCriteriaDto != null) {
            DataObjectVersions dataObjectVersionToExport = new DataObjectVersions();
            dataObjectVersionToExport.setDataObjectVersions(exportDipCriteriaDto.getDataObjectVersions());
            dipRequest.setExportWithLogBookLFC(exportDipCriteriaDto.isLifeCycleLogs());
            dipRequest.setDslRequest(dslQuery);
            dipRequest.setDipExportType(DipExportType.FULL);
            dipRequest.setDataObjectVersionToExport(dataObjectVersionToExport);
            dipRequest.setDipRequestParameters(exportDipCriteriaDto.getDipRequestParameters());
        }
        return dipRequest;
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

    public JsonNode exportDIP(final VitamContext vitamContext, DipRequest dipRequest)
        throws VitamClientException {
        RequestResponse<JsonNode> response = exportDipV2Service.exportDip(vitamContext, dipRequest);
        return response.toJsonNode();
    }

    public String massUpdateUnitsRules(final VitamContext vitamContext, final JsonNode updateQuery)
        throws VitamClientException {
        JsonNode response = unitService.massUpdateUnitsRules(vitamContext, updateQuery).toJsonNode();
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }

    public String updateArchiveUnitsRules(final VitamContext vitamContext,
        final RuleSearchCriteriaDto ruleSearchCriteriaDto)
        throws VitamClientException {

        LOGGER.info("Add Rules to ArchiveUnits using query : {} and DSL actions : {}",
            ruleSearchCriteriaDto.getSearchCriteriaDto().toString(), ruleSearchCriteriaDto.getRuleActions());
        boolean hasAccessContractWritePermission = checkAccessContractWritePermission(vitamContext);

        if (!hasAccessContractWritePermission) {
            LOGGER
                .error("the access contract : {} ,using to update unit rules has no writing permission to update units",
                    vitamContext.getAccessContract());
            throw new ForbiddenException(
                "the access contract using to update unit rules has no writing permission to update units");
        }

        RuleActions ruleActions =
            ruleOperationsConverter.convertToVitamRuleActions(ruleSearchCriteriaDto.getRuleActions());

        MassUpdateUnitRuleRequest massUpdateUnitRuleRequest = new MassUpdateUnitRuleRequest();
        JsonNode dslQuery = mapRequestToDslQuery(ruleSearchCriteriaDto.getSearchCriteriaDto());
        ObjectNode dslRequest = (ObjectNode) dslQuery;
        RulesUpdateCommonService
            .deleteAttributesFromObjectNode(dslRequest, DSL_QUERY_PROJECTION, DSL_QUERY_FILTER, DSL_QUERY_FACETS);

        RulesUpdateCommonService.setMassUpdateUnitRuleRequest(massUpdateUnitRuleRequest, ruleActions, dslRequest);

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JsonNode updateQuery = objectMapper.convertValue(massUpdateUnitRuleRequest, JsonNode.class);
        LOGGER.debug("Add Rules to UA final updateQuery : {}", updateQuery);

        return massUpdateUnitsRules(vitamContext, updateQuery);
    }

    public String computedInheritedRules(final VitamContext vitamContext, final SearchCriteriaDto searchCriteriaDto)
        throws VitamClientException {
        LOGGER.debug("Computed Inherited Rules by criteria {} ", searchCriteriaDto.toString());
        JsonNode jsonNode = mapRequestToDslQuery(searchCriteriaDto);
        ObjectNode dslRequest = (ObjectNode) jsonNode;
        RulesUpdateCommonService
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

    public boolean checkAccessContractWritePermission(final VitamContext vitamContext) {
        LOGGER.debug("Check access contract writing permissions : {}", vitamContext.getAccessContract());
        AccessContractsResponseDto accessContractResponseDto;
        try {
            RequestResponse<AccessContractModel> response =
                this.accessContractService.findAccessContractById(vitamContext, vitamContext.getAccessContract());

            accessContractResponseDto = objectMapper
                .treeToValue(response.toJsonNode(), AccessContractsResponseDto.class);
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Error while parsing Vitam response", e);
        }

        if (Objects.nonNull(accessContractResponseDto) &&
            !CollectionUtils.isEmpty(accessContractResponseDto.getResults())) {
            return accessContractResponseDto.getResults().get(0).getWritingPermission();
        } else {
            LOGGER.error("the access contract {} using to update unit rules is not found in vitam",
                vitamContext.getAccessContract());
            throw new ForbiddenException("the access contract is not found, update unit rules will fail.");
        }
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
        RulesUpdateCommonService.deleteAttributesFromObjectNode((ObjectNode) dslQuery, DSL_QUERY_FACETS);
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
        ((ObjectNode) dslQuery).putPOJO("$action", reclassificationCriteriaDto.getAction());
        Arrays.stream(new String[] {"$projection", "$filter", "$facets"}).forEach(((ObjectNode) dslQuery)::remove);
        array.add(dslQuery);
        LOGGER.debug("Reclassification query : {}", array.toPrettyString());
        RequestResponse<JsonNode> jsonNodeRequestResponse = unitService.reclassification(vitamContext, array);
        return jsonNodeRequestResponse.toJsonNode().findValue(OPERATION_IDENTIFIER).textValue();

    }

    private boolean computeFacets(final SearchCriteriaDto searchQuery) {
        boolean computeFacetFlag = false;
        if (!CollectionUtils.isEmpty(searchQuery.getCriteriaList())) {
            List<SearchCriteriaEltDto> waitingToRecalculateCriteria = searchQuery
                .extractCriteriaListByCategoryAndFieldNames(ArchiveSearchConsts.CriteriaCategory.FIELDS,
                    List.of(ArchiveSearchConsts.WAITING_RECALCULATE));
            computeFacetFlag = !CollectionUtils.isEmpty(waitingToRecalculateCriteria);
            if (!computeFacetFlag) {
                List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList =
                    searchQuery.extractCriteriaListByCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
                computeFacetFlag = !CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList);
            }
            if (!computeFacetFlag) {
                List<SearchCriteriaEltDto> eliminationAnalysisGuidCriteria = searchQuery
                    .extractCriteriaListByCategoryAndFieldNames(ArchiveSearchConsts.CriteriaCategory.FIELDS,
                        List.of(ArchiveSearchConsts.ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE));
                computeFacetFlag = !CollectionUtils.isEmpty(eliminationAnalysisGuidCriteria);
            }
        }
        return computeFacetFlag;
    }
}
