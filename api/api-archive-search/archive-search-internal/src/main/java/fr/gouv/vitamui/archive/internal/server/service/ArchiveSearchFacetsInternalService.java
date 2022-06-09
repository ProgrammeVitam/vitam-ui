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
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.facet.FacetHelper;
import fr.gouv.vitam.common.database.builder.facet.RangeFacetValue;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetBucketDto;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchInternalService.FALSE;
import static fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchInternalService.TRUE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.ACCESS_RULE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaCategory.FIELDS;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaDataType.STRING;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.CriteriaOperators.EQ;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FACETS_COMPUTE_RULES_AU_NUMBER;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FACETS_COUNT_BY_NODE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FACETS_COUNT_WITHOUT_RULES;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FACETS_EXPIRED_RULES_COMPUTED;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FACETS_FINAL_ACTION_COMPUTED;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FACETS_RULES_COMPUTED_NUMBER;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FACET_SIZE_MILTIPLIER;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FINAL_ACTION_CONFLICT_FIELD_VALUE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FINAL_ACTION_DESTROY_FIELD_VALUE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FINAL_ACTION_KEEP_FIELD_VALUE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FINAL_ACTION_TYPE_CONFLICT;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.FR_DATE_FORMAT_WITH_SLASH;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.ISO_FRENCH_FORMATER;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.RULES_COMPUTED;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.RULE_END_DATE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.RULE_FINAL_ACTION_TYPE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.RULE_ORIGIN_CRITERIA;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.TRUE_CRITERIA_VALUE;
import static fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts.UNITS_UPS;

/**
 * Archive-Search facets Internal service .
 */
@Service
public class ArchiveSearchFacetsInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchFacetsInternalService.class);
    public static final String SOME_OLD_DATE = "01/01/0001";


    private final ArchiveSearchInternalService archiveSearchInternalService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ArchiveSearchFacetsInternalService(final @Lazy ArchiveSearchInternalService archiveSearchInternalService,
        final ObjectMapper objectMapper) {
        this.archiveSearchInternalService = archiveSearchInternalService;
        this.objectMapper = objectMapper;

    }

    public void computeExpirationFacetsForComputedRules(Long indexedArchiveUnitCount,
        List<FacetResultsDto> indexedRulesFacets, ArchiveSearchConsts.CriteriaCategory category) {
        Optional<FacetResultsDto> expirationArchiveUnitFacet = indexedRulesFacets.stream()
            .filter(facet -> (FACETS_EXPIRED_RULES_COMPUTED + "_" + category.name()).equals(facet.getName()))
            .findAny();
        if (expirationArchiveUnitFacet.isPresent()) {
            List<FacetBucketDto> expiredArchiveUnitBuckets = expirationArchiveUnitFacet.get().getBuckets();
            if (!CollectionUtils.isEmpty(expiredArchiveUnitBuckets)) {
                FacetBucketDto expiredArchiveUnitBucket = expiredArchiveUnitBuckets.get(0);
                if (expiredArchiveUnitBucket != null) {
                    expirationArchiveUnitFacet.get().getBuckets().add(new FacetBucketDto("UNEXPIRED",
                        indexedArchiveUnitCount - expiredArchiveUnitBucket.getCount()));
                }
            }
        }
    }

    public void mergeValidComputedInheritenceCriteriaWithAppraisalCriteria(
        List<SearchCriteriaEltDto> initialCriteriaList,
        ArchiveSearchConsts.CriteriaCategory criteriaCategory) {
        long originRulesCriteriaCount = initialCriteriaList.stream().filter(
            searchCriteriaEltDto -> criteriaCategory.equals(searchCriteriaEltDto.getCategory()) &&
                (searchCriteriaEltDto.getCriteria().equals(RULE_ORIGIN_CRITERIA)))
            .count();

        if (originRulesCriteriaCount > 0) {
            initialCriteriaList.stream().forEach(searchCriteriaEltDto -> {
                if (criteriaCategory.equals(searchCriteriaEltDto.getCategory()) &&
                    (RULE_ORIGIN_CRITERIA.equals(searchCriteriaEltDto.getCriteria()))) {
                    List<CriteriaValue> values = searchCriteriaEltDto.getValues();
                    values
                        .add(new CriteriaValue(
                            ArchiveSearchConsts.RuleOriginValues.ORIGIN_WAITING_RECALCULATE.name()));
                    searchCriteriaEltDto.setValues(values);
                }
            });
        } else {
            SearchCriteriaEltDto criteria = new SearchCriteriaEltDto();
            criteria.setCriteria(RULE_ORIGIN_CRITERIA);
            criteria.setCategory(criteriaCategory);
            criteria.setOperator(EQ.name());
            criteria.setValues(
                List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_WAITING_RECALCULATE.name())));
            initialCriteriaList.add(criteria);
        }
    }

    private List<FacetResultsDto> computeFacetsForIndexedRulesCriteria(
        List<SearchCriteriaEltDto> initialArchiveUnitsCriteriaList, Long indexedArchiveUnitCount,
        final VitamContext vitamContext)
        throws InvalidCreateOperationException, VitamClientException,
        JsonProcessingException {
        LOGGER.debug("Start finding facets for computed rules  ");
        try {
            List<ArchiveSearchConsts.CriteriaCategory> categories = List.of(APPRAISAL_RULE, ACCESS_RULE);
            List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList = new ArrayList<>(
                initialArchiveUnitsCriteriaList);
            indexedArchiveUnitsCriteriaList.add(new SearchCriteriaEltDto(RULES_COMPUTED, FIELDS, EQ.name(),
                List.of(new CriteriaValue(TRUE)), STRING.name()));
            SelectMultiQuery selectMultiQuery = archiveSearchInternalService
                .createSelectMultiQuery(indexedArchiveUnitsCriteriaList);
            for (ArchiveSearchConsts.CriteriaCategory category : categories) {
                fillRulesFacetsForIndexedRulesByCategory(indexedArchiveUnitsCriteriaList, category,
                    selectMultiQuery);
            }
            JsonNode dslQuery = selectMultiQuery.getFinalSelect();
            JsonNode vitamResponse = archiveSearchInternalService.searchArchiveUnits(dslQuery, vitamContext);
            VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
                VitamUISearchResponseDto.class);
            List<FacetResultsDto> indexedRulesFacets = archivesUnitsResults.getFacetResults();
            List<FacetResultsDto> mergedIndexedRulesFacets = new ArrayList<>(indexedRulesFacets);
            for (ArchiveSearchConsts.CriteriaCategory category : categories) {
                mergedIndexedRulesFacets
                    .add(computeNoRulesFacets(indexedArchiveUnitsCriteriaList, category, vitamContext));
                if (APPRAISAL_RULE.equals(category)) {
                    FacetResultsDto finalActionIndexedFacet = buildComputedAuFinalActionFacet(
                        indexedArchiveUnitsCriteriaList, category,
                        indexedRulesFacets, vitamContext);
                    mergedIndexedRulesFacets = mergedIndexedRulesFacets.stream()
                        .filter(
                            facet -> !(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name())
                                .equals(facet.getName()))
                        .collect(Collectors.toList());
                    mergedIndexedRulesFacets.add(finalActionIndexedFacet);
                }
                computeExpirationFacetsForComputedRules(indexedArchiveUnitCount, mergedIndexedRulesFacets,
                    category);
            }
            return mergedIndexedRulesFacets;
        } catch (InvalidParseOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query" + e.getMessage());
        }
    }

    @NotNull
    private List<FacetBucketDto> computeFinalActionFacetsForComputedRules(
        List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList, List<FacetResultsDto> indexedRulesFacets,
        ArchiveSearchConsts.CriteriaCategory category, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        Map<String, Long> finalActionCountMap = new HashMap<>();
        finalActionCountMap.put(FINAL_ACTION_KEEP_FIELD_VALUE, 0l);
        finalActionCountMap.put(FINAL_ACTION_DESTROY_FIELD_VALUE, 0l);
        finalActionCountMap.put(FINAL_ACTION_CONFLICT_FIELD_VALUE, 0l);
        Optional<FacetResultsDto> facetFinalActionValue = indexedRulesFacets.stream()
            .filter(facet -> (FACETS_FINAL_ACTION_COMPUTED + "_" + category.name()).equals(facet.getName()))
            .findAny();
        facetFinalActionValue.ifPresent(facetResultsDto -> facetResultsDto.getBuckets().stream()
            .forEach(bucket -> finalActionCountMap.put(bucket.getValue(), bucket.getCount())));
        Integer withConflictFinalActionAppraisalRulesUnitsCount = computeFinalActionCountByValue(
            indexedArchiveUnitsCriteriaList,
            FINAL_ACTION_TYPE_CONFLICT, vitamContext);
        finalActionCountMap
            .put(FINAL_ACTION_CONFLICT_FIELD_VALUE,
                Long.valueOf(withConflictFinalActionAppraisalRulesUnitsCount));
        if (withConflictFinalActionAppraisalRulesUnitsCount != 0) {
            Long withKeepFinalActionAppraisalRulesUnitsCount = finalActionCountMap.get(FINAL_ACTION_KEEP_FIELD_VALUE);
            Long withDestroyFinalActionAppraisalRulesUnitsCount = finalActionCountMap
                .get(FINAL_ACTION_DESTROY_FIELD_VALUE);
            if (withKeepFinalActionAppraisalRulesUnitsCount > 0) {
                finalActionCountMap.put(FINAL_ACTION_KEEP_FIELD_VALUE,
                    withKeepFinalActionAppraisalRulesUnitsCount - withConflictFinalActionAppraisalRulesUnitsCount);
            }
            if (withDestroyFinalActionAppraisalRulesUnitsCount > 0) {
                finalActionCountMap.put(FINAL_ACTION_DESTROY_FIELD_VALUE,
                    withDestroyFinalActionAppraisalRulesUnitsCount -
                        withConflictFinalActionAppraisalRulesUnitsCount);
            }
        }
        List<FacetBucketDto> finalActionBuckets = new ArrayList<>();
        for (Map.Entry<String, Long> entry : finalActionCountMap.entrySet()) {
            finalActionBuckets.add(new FacetBucketDto(entry.getKey(), entry.getValue()));
        }
        return finalActionBuckets;
    }

    private FacetResultsDto computeRulesCountByComputedRulesStatusFacets(ArchiveUnitsDto archiveUnitsDto) {
        FacetResultsDto computeAuFacetUpdated = new FacetResultsDto();
        Map<String, Long> countByStatusMap = new HashMap<>();
        countByStatusMap.put(TRUE, 0l);
        countByStatusMap.put(FALSE, 0l);
        if (archiveUnitsDto != null && archiveUnitsDto.getArchives() != null &&
            !CollectionUtils.isEmpty(archiveUnitsDto.getArchives().getFacetResults())) {
            Integer totalCount = archiveUnitsDto.getArchives().getHits().getTotal();
            Optional<FacetResultsDto> computeRulesAuCountFacetOpt = archiveUnitsDto.getArchives().getFacetResults()
                .stream().filter(
                    (facet -> facet.getName().equals(FACETS_COMPUTE_RULES_AU_NUMBER)))
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
            computeAuFacetUpdated.setName(FACETS_COMPUTE_RULES_AU_NUMBER);
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

    @NotNull
    public FacetResultsDto buildComputedAuFinalActionFacet(List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category, List<FacetResultsDto> indexedRulesFacets,
        VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        List<FacetBucketDto> finalActionBuckets = computeFinalActionFacetsForComputedRules(
            indexedArchiveUnitsCriteriaList, indexedRulesFacets,
            category, vitamContext);

        FacetResultsDto finalActionIndexedFacet = new FacetResultsDto();
        finalActionIndexedFacet.setName(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name());
        finalActionIndexedFacet.setBuckets(finalActionBuckets);
        return finalActionIndexedFacet;
    }

    public void fillFacets(SearchCriteriaDto searchQuery, ArchiveUnitsDto archiveUnitsDto, VitamContext vitamContext)
        throws InvalidCreateOperationException, VitamClientException, JsonProcessingException {
        if (searchQuery.isComputeFacets()) {
            List<FacetResultsDto> facetResults = archiveUnitsDto.getArchives().getFacetResults();
            if (CollectionUtils.isEmpty(facetResults)) {
                facetResults = new ArrayList<>();
            }
            FacetResultsDto computedRulesStatusFacet = computeRulesCountByComputedRulesStatusFacets(archiveUnitsDto);
            Optional<FacetBucketDto> indexedAuCountBucket = computedRulesStatusFacet.getBuckets().stream()
                .filter(bucket -> TRUE_CRITERIA_VALUE.equals(bucket.getValue())).findAny();
            Long indexedArchiveUnitCount = 0l;
            if (indexedAuCountBucket.isPresent()) {
                indexedArchiveUnitCount = indexedAuCountBucket.get().getCount();
            }
            facetResults.addAll(
                computeFacetsForIndexedRulesCriteria(searchQuery.getCriteriaList(), indexedArchiveUnitCount,
                    vitamContext));
            archiveUnitsDto.getArchives().setFacetResults(facetResults);
        }
    }

    public FacetResultsDto computeNoRulesFacets(List<SearchCriteriaEltDto> indexedCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        List<SearchCriteriaEltDto> criteriaListFacet = new ArrayList<>(indexedCriteriaList);

        criteriaListFacet.add(new SearchCriteriaEltDto(
            RULE_ORIGIN_CRITERIA,
            category, EQ.name(),
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_NO_ONE.name())),
            STRING.name()));

        FacetResultsDto noRuleFacet = new FacetResultsDto();
        noRuleFacet.setName(FACETS_COUNT_WITHOUT_RULES + "_" + category.name());
        noRuleFacet.setBuckets(
            List.of(new FacetBucketDto(FACETS_COUNT_WITHOUT_RULES,
                Long.valueOf(countArchiveUnitByCriteriaList(criteriaListFacet, vitamContext)))));
        return noRuleFacet;
    }

    public void addPositionsNodesFacet(SearchCriteriaDto searchQuery, SelectMultiQuery selectMultiQuery)
        throws InvalidCreateOperationException {
        List<String> nodesCriteriaList = searchQuery.extractNodesCriteria();
        selectMultiQuery.addFacets(FacetHelper.terms(FACETS_COUNT_BY_NODE, UNITS_UPS,
            (nodesCriteriaList.size() + 1) * FACET_SIZE_MILTIPLIER, FacetOrder.ASC));
    }

    private Integer countArchiveUnitByCriteriaList(List<SearchCriteriaEltDto> criteriaList, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        SearchCriteriaDto facetSearchQuery = new SearchCriteriaDto();
        facetSearchQuery.setCriteriaList(criteriaList);
        facetSearchQuery.setFieldsList(List.of(ArchiveSearchInternalService.TITLE_FIELD));
        JsonNode dslQuery = archiveSearchInternalService.mapRequestToDslQuery(facetSearchQuery);
        JsonNode vitamResponse = archiveSearchInternalService.searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
            VitamUISearchResponseDto.class);
        return archivesUnitsResults.getHits().getTotal();
    }

    private Integer computeFinalActionCountByValue(List<SearchCriteriaEltDto> initialCriteriaList, String value,
        VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {

        List<SearchCriteriaEltDto> criteriaListFacet = new ArrayList<>();
        criteriaListFacet.addAll(initialCriteriaList);
        SearchCriteriaDto countSearchQuery = new SearchCriteriaDto();
        criteriaListFacet.add(new SearchCriteriaEltDto(
            RULE_FINAL_ACTION_TYPE,
            APPRAISAL_RULE, EQ.name(),
            List.of(new CriteriaValue(value)),
            STRING.name()));

        criteriaListFacet.add(new SearchCriteriaEltDto(
            RULES_COMPUTED,
            FIELDS, EQ.name(),
            List.of(new CriteriaValue(TRUE)),
            STRING.name()));

        countSearchQuery.setCriteriaList(criteriaListFacet);
        countSearchQuery.setFieldsList(List.of(ArchiveSearchInternalService.TITLE_FIELD));
        JsonNode dslQuery = archiveSearchInternalService.mapRequestToDslQuery(countSearchQuery);
        JsonNode vitamResponse = archiveSearchInternalService.searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
            VitamUISearchResponseDto.class);
        return archivesUnitsResults.getHits().getTotal();
    }

    public void fillRulesFacetsForIndexedRulesByCategory(List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category, SelectMultiQuery select)
        throws InvalidCreateOperationException {
        try {
            List<SearchCriteriaEltDto> rulesCriteriaList = mgtRulesCriteriaList.stream().filter(Objects::nonNull)
                .filter(searchCriteriaEltDto -> (category.equals(searchCriteriaEltDto.getCategory())))
                .collect(Collectors.toList());
            String computedRulesIdentifierMapping = ArchivesSearchManagementRulesQueryBuilderService.COMPUTED_FIELDS
                +
                ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() +
                ArchivesSearchManagementRulesQueryBuilderService.RULES_RULE_ID_FIELD;
            if (APPRAISAL_RULE.equals(category)) {
                String computedRulesFinalActionMapping =
                    ArchivesSearchManagementRulesQueryBuilderService.COMPUTED_FIELDS
                        +
                        ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() +
                        ArchivesSearchManagementRulesQueryBuilderService.FINAL_ACTION_FIELD;
                select.addFacets(FacetHelper.terms(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name(),
                    computedRulesFinalActionMapping, 3, FacetOrder.ASC));
            }
            select.addFacets(FacetHelper.terms(FACETS_RULES_COMPUTED_NUMBER + "_" + category.name(),
                computedRulesIdentifierMapping, 100, FacetOrder.ASC));
            addExpirationRulesFacet(rulesCriteriaList, category, select);

        } catch (DateTimeParseException e) {
            throw new InvalidCreateOperationException(e);
        }
    }

    private void addExpirationRulesFacet(List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category, SelectMultiQuery select)
        throws InvalidCreateOperationException {
        String strDateExpirationCriteria = extractRuleExpirationDateFromCriteria(mgtRulesCriteriaList, category);
        String managementRuleEndDateMapping = ArchivesSearchManagementRulesQueryBuilderService.COMPUTED_FIELDS +
            ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() +
            ArchivesSearchManagementRulesQueryBuilderService.RULES_END_DATE_FIELD;
        select.addFacets(FacetHelper.dateRange(FACETS_EXPIRED_RULES_COMPUTED + "_" + category.name(),
            managementRuleEndDateMapping, FR_DATE_FORMAT_WITH_SLASH,
            List.of(new RangeFacetValue(SOME_OLD_DATE, strDateExpirationCriteria))));
    }

    @NotNull
    private String extractRuleExpirationDateFromCriteria(List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category) {
        String strDateExpirationCriteria;
        Optional<SearchCriteriaEltDto> endDateCriteria = mgtRulesCriteriaList.stream().filter(
            searchCriteriaEltDto -> (category.equals(searchCriteriaEltDto.getCategory()) &&
                RULE_END_DATE
                    .equals(searchCriteriaEltDto.getCriteria())))
            .findAny();
        if (endDateCriteria.isPresent() &&
            !CollectionUtils.isEmpty(endDateCriteria.get().getValues())) {

            String beginDtStr = endDateCriteria.get().getValues().get(0).getBeginInterval();
            String endDtStr = endDateCriteria.get().getValues().get(0).getEndInterval();
            LocalDateTime beginDt = null;
            if (!StringUtils.isEmpty(beginDtStr)) {
                beginDt = LocalDateTime.parse(beginDtStr, ISO_FRENCH_FORMATER);
            }
            LocalDateTime endDt = null;
            if (!StringUtils.isEmpty(endDtStr)) {
                endDt = LocalDateTime.parse(endDtStr, ISO_FRENCH_FORMATER);
            }
            if (beginDt != null && endDt != null) {
                if (endDt.isAfter(beginDt)) {
                    strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(endDt);
                } else {
                    strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(beginDt);
                }
            } else if (beginDt != null) {
                strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(beginDt);
            } else if (endDt != null) {
                strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(endDt);
            } else {
                strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(LocalDateTime.now());
            }
        } else {
            strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(LocalDateTime.now());
        }
        return strDateExpirationCriteria;
    }
}
