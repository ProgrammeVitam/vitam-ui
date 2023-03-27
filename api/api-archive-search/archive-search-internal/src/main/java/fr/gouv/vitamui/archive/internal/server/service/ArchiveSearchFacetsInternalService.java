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
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
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

import static fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchInternalService.TRUE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.ACCESS_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.DISSEMINATION_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.FIELDS;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.REUSE_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.STORAGE_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaDataType.STRING;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaOperators.EQ;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_COUNT_WITHOUT_RULES;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_EXPIRED_RULES_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_FINAL_ACTION_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_RULES_COMPUTED_NUMBER;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_UNEXPIRED_RULES_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FINAL_ACTION_CONFLICT_FIELD_VALUE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FINAL_ACTION_DESTROY_FIELD_VALUE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FINAL_ACTION_KEEP_FIELD_VALUE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FINAL_ACTION_TYPE_CONFLICT;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FR_DATE_FORMAT_WITH_SLASH;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ISO_FRENCH_FORMATER;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULES_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULE_END_DATE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULE_FINAL_ACTION_TYPE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULE_ORIGIN_CRITERIA;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.createSelectMultiQuery;

/**
 * Archive-Search facets Internal service .
 */
@Service
public class ArchiveSearchFacetsInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchFacetsInternalService.class);
    public static final String SOME_OLD_DATE = "01/01/0001";
    public static final String SOME_FUTUR_DATE = "31/12/9999";

    public static final String COMPUTED_FIELDS = "#computedInheritedRules.";
    public static final String MAX_END_DATE_FIELD = ".MaxEndDate";
    public static final String RULES_RULE_ID_FIELD = ".Rules.Rule";
    public static final String FINAL_ACTION_FIELD = ".FinalAction";


    private final ArchiveSearchInternalService archiveSearchInternalService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ArchiveSearchFacetsInternalService(final @Lazy ArchiveSearchInternalService archiveSearchInternalService,
        final ObjectMapper objectMapper) {
        this.archiveSearchInternalService = archiveSearchInternalService;
        this.objectMapper = objectMapper;
    }

    private List<FacetResultsDto> computeFacetsForAuHavingRules(SearchCriteriaDto searchQuery,
        ArchiveSearchConsts.CriteriaCategory
            category, boolean trackTotalHits,
        VitamContext vitamContext)
        throws InvalidParseOperationException, InvalidCreateOperationException, VitamClientException,
        JsonProcessingException {

        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>(searchQuery.getCriteriaList());
        criteriaList.add(new SearchCriteriaEltDto(RULES_COMPUTED, FIELDS, EQ.name(),
            List.of(new CriteriaValue(TRUE)), STRING.name()));

        criteriaList.add(new SearchCriteriaEltDto(
            RULE_ORIGIN_CRITERIA,
            category, EQ.name(),
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_LOCAL_OR_INHERIT_RULES.name())),
            STRING.name()));

        SelectMultiQuery selectMultiQuery = createSelectMultiQuery(criteriaList);
        selectMultiQuery.setLimitFilter(0, 1);
        selectMultiQuery.trackTotalHits(trackTotalHits);

        try {
            List<SearchCriteriaEltDto> rulesCriteriaList =
                searchQuery.getCriteriaList().stream().filter(Objects::nonNull)
                    .filter(searchCriteriaEltDto -> (category.equals(searchCriteriaEltDto.getCategory())))
                    .collect(Collectors.toList());
            String computedRulesIdentifierMapping = COMPUTED_FIELDS
                    +
                    ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() + RULES_RULE_ID_FIELD;
            if (APPRAISAL_RULE.equals(category) || STORAGE_RULE.equals(category)) {
                String computedRulesFinalActionMapping = COMPUTED_FIELDS
                        + ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name())
                        .getFieldMapping() + FINAL_ACTION_FIELD;
                selectMultiQuery.addFacets(FacetHelper.terms(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name(),
                    computedRulesFinalActionMapping, 3, FacetOrder.ASC));
            }
            selectMultiQuery.addFacets(FacetHelper.terms(FACETS_RULES_COMPUTED_NUMBER + "_" + category.name(),
                computedRulesIdentifierMapping, 100, FacetOrder.ASC));
            addExpirationRulesFacet(rulesCriteriaList, category, selectMultiQuery);

        } catch (DateTimeParseException e) {
            throw new InvalidCreateOperationException(e);
        }

        JsonNode dslQuery = selectMultiQuery.getFinalSelect();
        JsonNode vitamResponse = archiveSearchInternalService.searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
            VitamUISearchResponseDto.class);
        List<FacetResultsDto> auWithRulesFacets = archivesUnitsResults.getFacetResults();

        if (APPRAISAL_RULE.equals(category) || STORAGE_RULE.equals(category)) {
            FacetResultsDto finalActionIndexedFacet =
                buildComputedAuFinalActionFacet(searchQuery.getCriteriaList(), category, auWithRulesFacets,
                    trackTotalHits, vitamContext);
            auWithRulesFacets =
                auWithRulesFacets.stream().filter(facet -> !(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name())
                    .equals(facet.getName())).collect(Collectors.toList());
            auWithRulesFacets.add(finalActionIndexedFacet);
        }
        return auWithRulesFacets;
    }

    @NotNull
    private List<FacetBucketDto> computeFinalActionFacetsForComputedAppraisalRules(
        List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList, List<FacetResultsDto>
        indexedRulesFacets, ArchiveSearchConsts.CriteriaCategory category, boolean trackTotalHits,
        VitamContext vitamContext)
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
        Integer withConflictFinalActionUnitsCount = computeFinalActionCountByValue(
            indexedArchiveUnitsCriteriaList, FINAL_ACTION_TYPE_CONFLICT, category, trackTotalHits, vitamContext);
        finalActionCountMap
            .put(FINAL_ACTION_CONFLICT_FIELD_VALUE,
                Long.valueOf(withConflictFinalActionUnitsCount));
        if (withConflictFinalActionUnitsCount != 0) {
            Long withKeepFinalActionAppraisalRulesUnitsCount =
                finalActionCountMap.get(FINAL_ACTION_KEEP_FIELD_VALUE);
            Long withDestroyFinalActionAppraisalRulesUnitsCount = finalActionCountMap
                .get(FINAL_ACTION_DESTROY_FIELD_VALUE);
            if (withKeepFinalActionAppraisalRulesUnitsCount > 0) {
                finalActionCountMap.put(FINAL_ACTION_KEEP_FIELD_VALUE,
                    withKeepFinalActionAppraisalRulesUnitsCount - withConflictFinalActionUnitsCount);
            }
            if (withDestroyFinalActionAppraisalRulesUnitsCount > 0) {
                finalActionCountMap.put(FINAL_ACTION_DESTROY_FIELD_VALUE,
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

    public FacetResultsDto buildComputedAuFinalActionFacet
        (List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList,
            ArchiveSearchConsts.CriteriaCategory category, List<FacetResultsDto> indexedRulesFacets,
            boolean trackTotalHits, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        FacetResultsDto finalActionIndexedFacet = new FacetResultsDto();
        if (APPRAISAL_RULE.equals(category)) {
            List<FacetBucketDto> finalActionBuckets = computeFinalActionFacetsForComputedAppraisalRules(
                indexedArchiveUnitsCriteriaList, indexedRulesFacets, category, trackTotalHits, vitamContext);
            finalActionIndexedFacet.setName(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name());
            finalActionIndexedFacet.setBuckets(finalActionBuckets);
        } else if (STORAGE_RULE.equals(category)) {
            Optional<FacetResultsDto> finalActionIndexedFacetOpt = indexedRulesFacets.stream()
                .filter(facet -> (FACETS_FINAL_ACTION_COMPUTED + "_" + category.name()).equals(facet.getName()))
                .findAny();
            if (finalActionIndexedFacetOpt.isPresent()) {
                finalActionIndexedFacet = finalActionIndexedFacetOpt.get();
            } else {
                finalActionIndexedFacet = new FacetResultsDto();
                finalActionIndexedFacet.setName(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name());
                finalActionIndexedFacet.setBuckets(List.of());
            }
        }
        return finalActionIndexedFacet;
    }

    public List<FacetResultsDto> fillManagementRulesFacets(SearchCriteriaDto searchQuery, boolean trackTotalHits,
        VitamContext vitamContext)
        throws InvalidCreateOperationException, VitamClientException, JsonProcessingException {
        List<FacetResultsDto> facetResults = new ArrayList<>();
        if (searchQuery.isComputeFacets()) {
            try {
                LOGGER.debug("Start computing facets for units with computed inheritance rules  ");
                List<ArchiveSearchConsts.CriteriaCategory> categories =
                    List.of(APPRAISAL_RULE, ACCESS_RULE, STORAGE_RULE, REUSE_RULE, DISSEMINATION_RULE);
                for (ArchiveSearchConsts.CriteriaCategory category : categories) {
                    LOGGER.debug("Start computing facets for management rules for category {}  ", category.name());
                    FacetResultsDto withoutRulesByCategoryFacet =
                        computeNoRulesFacets(searchQuery, category, trackTotalHits, vitamContext);
                    facetResults.add(withoutRulesByCategoryFacet);
                    List<FacetResultsDto> facetsForAuHavingRules =
                        computeFacetsForAuHavingRules(searchQuery, category, trackTotalHits, vitamContext);
                    facetResults.addAll(facetsForAuHavingRules);
                }
            } catch (InvalidParseOperationException e) {
                throw new BadRequestException("Can't parse criteria as Vitam query" + e.getMessage());
            }
        }
        return facetResults;
    }

    public FacetResultsDto computeNoRulesFacets(SearchCriteriaDto searchQuery,
        ArchiveSearchConsts.CriteriaCategory category, boolean trackTotalHits, VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {
        List<SearchCriteriaEltDto> criteriaListFacet = new ArrayList<>(searchQuery.getCriteriaList());

        criteriaListFacet.add(new SearchCriteriaEltDto(RULES_COMPUTED, FIELDS, EQ.name(),
            List.of(new CriteriaValue(TRUE)), STRING.name()));

        criteriaListFacet.add(new SearchCriteriaEltDto(
            RULE_ORIGIN_CRITERIA,
            category, EQ.name(),
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_NO_ONE.name())),
            STRING.name()));

        FacetResultsDto noRuleFacet = new FacetResultsDto();
        noRuleFacet.setName(FACETS_COUNT_WITHOUT_RULES + "_" + category.name());
        noRuleFacet.setBuckets(
            List.of(new FacetBucketDto(FACETS_COUNT_WITHOUT_RULES,
                Long.valueOf(countArchiveUnitByCriteriaList(criteriaListFacet, trackTotalHits, vitamContext)))));
        return noRuleFacet;
    }

    private Integer countArchiveUnitByCriteriaList(List<SearchCriteriaEltDto> criteriaList, boolean trackTotalHits,
        VitamContext
            vitamContext)
        throws VitamClientException, JsonProcessingException {
        SearchCriteriaDto facetSearchQuery = new SearchCriteriaDto();
        facetSearchQuery.setCriteriaList(criteriaList);
        facetSearchQuery.setSize(1);
        facetSearchQuery.setTrackTotalHits(trackTotalHits);
        facetSearchQuery.setFieldsList(List.of(ArchiveSearchInternalService.TITLE_FIELD));
        JsonNode dslQuery = archiveSearchInternalService.mapRequestToDslQuery(facetSearchQuery);
        JsonNode vitamResponse = archiveSearchInternalService.searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
            VitamUISearchResponseDto.class);
        return archivesUnitsResults.getHits().getTotal();
    }

    private Integer computeFinalActionCountByValue(List<SearchCriteriaEltDto> initialCriteriaList, String
        value,
        ArchiveSearchConsts.CriteriaCategory category, boolean trackTotalHits,
        VitamContext vitamContext)
        throws VitamClientException, JsonProcessingException {

        List<SearchCriteriaEltDto> criteriaListFacet = new ArrayList<>();
        criteriaListFacet.addAll(initialCriteriaList);
        SearchCriteriaDto countSearchQuery = new SearchCriteriaDto();
        criteriaListFacet.add(new SearchCriteriaEltDto(
            RULE_FINAL_ACTION_TYPE,
            category, EQ.name(),
            List.of(new CriteriaValue(value)),
            STRING.name()));

        criteriaListFacet.add(new SearchCriteriaEltDto(
            RULES_COMPUTED,
            FIELDS, EQ.name(),
            List.of(new CriteriaValue(TRUE)),
            STRING.name()));

        countSearchQuery.setCriteriaList(criteriaListFacet);
        countSearchQuery.setFieldsList(List.of(ArchiveSearchInternalService.TITLE_FIELD));
        countSearchQuery.setSize(1);
        countSearchQuery.setTrackTotalHits(trackTotalHits);
        JsonNode dslQuery = archiveSearchInternalService.mapRequestToDslQuery(countSearchQuery);
        JsonNode vitamResponse = archiveSearchInternalService.searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(vitamResponse,
            VitamUISearchResponseDto.class);
        return archivesUnitsResults.getHits().getTotal();
    }


    private void addExpirationRulesFacet(List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category, SelectMultiQuery select)
        throws InvalidCreateOperationException {
        String strDateExpirationCriteria =
            extractRuleExpirationDateFromCriteria(mgtRulesCriteriaList, category);
        String managementRuleEndDateMapping = COMPUTED_FIELDS +
            ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() + MAX_END_DATE_FIELD;
        select.addFacets(FacetHelper.dateRange(FACETS_EXPIRED_RULES_COMPUTED + "_" + category.name(),
            managementRuleEndDateMapping, FR_DATE_FORMAT_WITH_SLASH,
            List.of(new RangeFacetValue(SOME_OLD_DATE, strDateExpirationCriteria))));

        select.addFacets(FacetHelper.dateRange(FACETS_UNEXPIRED_RULES_COMPUTED + "_" + category.name(),
            managementRuleEndDateMapping, FR_DATE_FORMAT_WITH_SLASH,
            List.of(new RangeFacetValue(strDateExpirationCriteria, SOME_FUTUR_DATE))));
    }

    @NotNull
    private String extractRuleExpirationDateFromCriteria(List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category) {
        String strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(LocalDateTime.now());
        Optional<SearchCriteriaEltDto> endDateCriteria = mgtRulesCriteriaList.stream().filter(
            searchCriteriaEltDto -> (category.equals(searchCriteriaEltDto.getCategory()) &&
                RULE_END_DATE.equals(searchCriteriaEltDto.getCriteria()))).findAny();
        if (endDateCriteria.isPresent() &&
            !CollectionUtils.isEmpty(endDateCriteria.get().getValues())) {

            String beginDtStr = endDateCriteria.get().getValues().get(0).getBeginInterval();
            String endDtStr = endDateCriteria.get().getValues().get(0).getEndInterval();

            strDateExpirationCriteria = getFormatedDateFromCriteria(beginDtStr, endDtStr);
        }
        return strDateExpirationCriteria;
    }

    @NotNull
    private static String getFormatedDateFromCriteria(String beginDtStr, String endDtStr) {
        String strDateExpirationCriteria;
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
        return strDateExpirationCriteria;
    }
}
