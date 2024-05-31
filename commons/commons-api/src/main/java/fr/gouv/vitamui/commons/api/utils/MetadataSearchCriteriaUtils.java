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
package fr.gouv.vitamui.commons.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.database.builder.facet.FacetHelper;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.dsl.VitamQueryHelper;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidCreateOperationVitamUIException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.exists;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ALL_UNIT_UPS;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ARCHIVE_UNIT_FILING_UNIT;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ARCHIVE_UNIT_HOLDING_UNIT;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ARCHIVE_UNIT_WITHOUT_OBJECTS;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ARCHIVE_UNIT_WITH_OBJECTS;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.ACCESS_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.DISSEMINATION_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.FIELDS;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.NODES;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.REUSE_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaOperators.EQ;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaOperators.MISSING;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.DEFAULT_DEPTH;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_COMPUTE_RULES_AU_NUMBER;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_COUNT_BY_NODE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACET_SIZE_MILTIPLIER;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FILING_UNIT_TYPE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.HOLDING_UNIT_TYPE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ID;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULES_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULE_ORIGIN_CRITERIA;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.WAITING_RECALCULATE;

public final class MetadataSearchCriteriaUtils {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(MetadataSearchCriteriaUtils.class);

    public static final String SCOPED_FIELDS = "#management.";
    public static final String COMPUTED_FIELDS = "#computedInheritedRules.";
    public static final String MAX_END_DATE_FIELD = ".MaxEndDate";
    public static final String RULES_END_DATE_FIELD = ".Rules.EndDate";
    public static final String RULES_RULE_ID_FIELD = ".Rules.Rule";
    public static final String INHERITANCE_ORIGIN_FIELD = ".InheritanceOrigin";
    public static final String INHERITED_RULE_IDS_FIELD = ".InheritedRuleIds";
    public static final String FINAL_ACTION_FIELD = ".FinalAction";
    public static final String INHERITED_ORIGIN_TYPE = "Inherited";
    public static final String LOCAL_OR_INHERITED_ORIGIN_TYPE = "LocalAndInherited";
    public static final String LOCAL_ORIGIN_TYPE = "Local";
    public static final String WAITING_TO_COMPUTE_RULES_STATUS = "#validComputedInheritedRules";
    private static final String INVALID_CREATION_OPERATION = "Invalid creation operation exception {}";
    private static final String COULD_NOT_CREATE_OPERATION = "Invalid creation operation exception ";
    private static final String FINAL_QUERY = "Final query: {}";

    public static final String SOME_OLD_DATE = "01/01/0001";
    public static final String SOME_FUTUR_DATE = "31/12/9999";
    public static final String SEMI_COLON = ";";
    public static final String COMMA = ",";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String SINGLE_QUOTE = "'";
    public static final String NEW_LINE = "\n";
    public static final String NEW_TAB = "\t";
    public static final String NEW_LINE_1 = "\r\n";
    public static final String SPACE = " ";

    private MetadataSearchCriteriaUtils() {
    }

    public static JsonNode createDslQueryWithFacets(SearchCriteriaDto searchQuery)
        throws VitamClientException, InvalidCreateOperationException {

        fillWaitingToComputeCriteria(searchQuery);

        SelectMultiQuery selectMultiQuery = mapRequestToSelectMultiQuery(searchQuery);
        addPositionsNodesFacet(searchQuery, selectMultiQuery);

        if (searchQuery.isComputeFacets()) {
            selectMultiQuery.addFacets(FacetHelper.terms(FACETS_COMPUTE_RULES_AU_NUMBER,
                SIMPLE_FIELDS_VALUES_MAPPING.get(RULES_COMPUTED), 3,
                FacetOrder.ASC));
            selectMultiQuery.trackTotalHits(searchQuery.isTrackTotalHits());
            selectMultiQuery.setLimitFilter((long) searchQuery.getPageNumber() * searchQuery.getSize(),
                searchQuery.getSize());
        }
        return selectMultiQuery.getFinalSelect();
    }

    public static SelectMultiQuery getBasicQuery(SearchCriteriaDto searchQuery)
        throws VitamClientException {
        if (searchQuery == null) {
            throw new BadRequestException("Can't parse null criteria");
        }
        final SelectMultiQuery selectMultiQuery = new SelectMultiQuery();
        Optional<String> orderBy = Optional.empty();
        Optional<DirectionDto> direction = Optional.empty();
        try {
            selectMultiQuery.setQuery(exists(ID));

            if (searchQuery.getSortingCriteria() != null) {
                direction = Optional.of(searchQuery.getSortingCriteria().getSorting());
                orderBy = Optional.of(searchQuery.getSortingCriteria().getCriteria());
            }

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
            LOGGER.debug(FINAL_QUERY, selectMultiQuery.getFinalSelect().toPrettyString());

        } catch (InvalidCreateOperationException ioe) {
            throw new VitamClientException("Unable to find archive units with pagination", ioe);
        } catch (InvalidParseOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query" + e.getMessage());
        }
        return selectMultiQuery;
    }

    /**
     * contextCall is an Optional arg to avoid filling it in case it's never used, otherwise it will be handled for
     * specific cases ( ARCHIVE_UNIT_WITH_OBJECTS && ARCHIVE_UNIT_WITHOUT_OBJECTS )
     */
    public static SelectMultiQuery mapRequestToSelectMultiQuery(SearchCriteriaDto searchQuery)
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
            LOGGER.debug(FINAL_QUERY, selectMultiQuery.getFinalSelect().toPrettyString());

            if (searchQuery.getThreshold() != null) {
                selectMultiQuery.setThreshold(searchQuery.getThreshold());
            }
        } catch (InvalidCreateOperationException ioe) {
            throw new VitamClientException("Unable to find archive units with pagination", ioe);
        } catch (InvalidParseOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query" + e.getMessage());
        }
        return selectMultiQuery;
    }

    public static SelectMultiQuery createSelectMultiQuery(List<SearchCriteriaEltDto> criteriaList)
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
        List<String> nodesCriteriaList = criteriaList.stream().filter(
                Objects::nonNull).filter(searchCriteriaEltDto -> NODES
                .equals(searchCriteriaEltDto.getCategory())).flatMap(criteria -> criteria.getValues().stream())
            .map(CriteriaValue::getValue).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(nodesCriteriaList)) {
            select.addRoots(nodesCriteriaList.toArray(new String[0]));
            query.setDepthLimit(DEFAULT_DEPTH);
        }
        fillQueryFromCriteriaList(query, simpleCriteriaList);
        fillQueryFromMgtRulesCriteriaList(query, mgtRulesCriteriaList);
        if (query.isReady()) {
            select.setQuery(query);
        }

        LOGGER.debug(FINAL_QUERY, select.getFinalSelect().toPrettyString());

        return select;
    }

    private static void fillWaitingToComputeCriteria(SearchCriteriaDto searchQuery) {
        List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList =
            searchQuery.extractCriteriaListByCategory(APPRAISAL_RULE);
        List<SearchCriteriaEltDto> accessMgtRulesCriteriaList =
            searchQuery.extractCriteriaListByCategory(ACCESS_RULE);
        List<SearchCriteriaEltDto> reuseMgtRulesCriteriaList =
            searchQuery.extractCriteriaListByCategory(REUSE_RULE);
        List<SearchCriteriaEltDto> disseminationMgtRulesCriteriaList =
            searchQuery.extractCriteriaListByCategory(DISSEMINATION_RULE);
        List<SearchCriteriaEltDto> waitingToRecalculateCriteria = searchQuery
            .extractCriteriaListByCategoryAndFieldNames(FIELDS,
                List.of(WAITING_RECALCULATE));

        boolean hasAppraisalRulesCriteria = !CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList);
        boolean hasAccessRulesCriteria = !CollectionUtils.isEmpty(accessMgtRulesCriteriaList);
        boolean hasReuseRulesCriteria = !CollectionUtils.isEmpty(reuseMgtRulesCriteriaList);
        boolean hasDisseminationRulesCriteria = !CollectionUtils.isEmpty(disseminationMgtRulesCriteriaList);
        boolean hasWaitingToRecalculateCriteria = !CollectionUtils.isEmpty(waitingToRecalculateCriteria);

        if (hasWaitingToRecalculateCriteria &&
            (hasAppraisalRulesCriteria || hasAccessRulesCriteria ||
                hasReuseRulesCriteria ||
                hasDisseminationRulesCriteria)) {
            List<SearchCriteriaEltDto> initialCriteriaList = searchQuery.getCriteriaList().stream().filter(
                searchCriteriaEltDto ->
                    !(FIELDS.equals(searchCriteriaEltDto.getCategory()) &&
                        (searchCriteriaEltDto.getCriteria()
                            .equals(WAITING_RECALCULATE)))
            ).collect(Collectors.toList());

            if (hasAppraisalRulesCriteria) {
                mergeValidComputedInheritenceCriteriaWithMgtRulesCriteria(initialCriteriaList,
                    APPRAISAL_RULE);
            }
            if (hasAccessRulesCriteria) {
                mergeValidComputedInheritenceCriteriaWithMgtRulesCriteria(initialCriteriaList,
                    ACCESS_RULE);
            }
            if (hasReuseRulesCriteria) {
                mergeValidComputedInheritenceCriteriaWithMgtRulesCriteria(initialCriteriaList,
                    REUSE_RULE);
            }
            if (hasDisseminationRulesCriteria) {
                mergeValidComputedInheritenceCriteriaWithMgtRulesCriteria(initialCriteriaList,
                    DISSEMINATION_RULE);
            }
            searchQuery.setCriteriaList(initialCriteriaList);
        }
    }

    public static void mergeValidComputedInheritenceCriteriaWithMgtRulesCriteria(
        List<SearchCriteriaEltDto> initialCriteriaList,
        ArchiveSearchConsts.CriteriaCategory criteriaCategory) {
        long originRulesCriteriaCount = initialCriteriaList.stream().filter(
                searchCriteriaEltDto -> criteriaCategory.equals(searchCriteriaEltDto.getCategory()) &&
                    (searchCriteriaEltDto.getCriteria().equals(RULE_ORIGIN_CRITERIA)))
            .count();

        if (originRulesCriteriaCount > 0) {
            initialCriteriaList.forEach(searchCriteriaEltDto -> {
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

    public static void fillQueryFromMgtRulesCriteriaList(BooleanQuery query,
        List<SearchCriteriaEltDto> mgtRuleCriteriaList)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(mgtRuleCriteriaList)) {
            for (ArchiveSearchConsts.CriteriaMgtRulesCategory mgtRulesCategory : ArchiveSearchConsts.CriteriaMgtRulesCategory
                .values()) {
                List<SearchCriteriaEltDto> mgtRulesByCategoryCriteriaList =
                    mgtRuleCriteriaList.stream().filter(Objects::nonNull)
                        .filter(searchCriteriaEltDto -> (mgtRulesCategory.name()
                            .equals(searchCriteriaEltDto.getCategory().name()))).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(mgtRulesByCategoryCriteriaList)) {
                    fillQueryFromCriteriaListByRule(mgtRulesCategory.getFieldMapping(), query,
                        mgtRulesByCategoryCriteriaList);
                }
            }
        }
    }

    private static void fillQueryFromCriteriaListByRule(String ruleCategory, BooleanQuery query,
        List<SearchCriteriaEltDto> criteriaList) throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(criteriaList)) {

            SearchCriteriaEltDto ruleIdentifiersCriteria = criteriaList.stream().filter(searchCriteriaEltDto
                    -> ArchiveSearchConsts.MANAGEMENT_RULE_IDENTIFIER_CRITERIA.equals(searchCriteriaEltDto.getCriteria()))
                .findFirst()
                .orElse(null);

            if (ruleIdentifiersCriteria != null) {
                List<String> searchValues =
                    ruleIdentifiersCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                        Collectors.toList());
                buildRuleIdentifierQuery(ruleIdentifiersCriteria.getCategory().name(), searchValues,
                    ArchiveSearchConsts.CriteriaOperators.valueOf(ruleIdentifiersCriteria.getOperator()),
                    query);
            }

            SearchCriteriaEltDto appraisalPreventRuleIdentifiersCriteria =
                criteriaList.stream().filter(searchCriteriaEltDto
                        -> ArchiveSearchConsts.APPRAISAL_PREVENT_RULE_IDENTIFIER_CRITERIA.equals(
                        searchCriteriaEltDto.getCriteria())).findFirst()
                    .orElse(null);

            if (appraisalPreventRuleIdentifiersCriteria != null) {
                List<String> searchValues =
                    appraisalPreventRuleIdentifiersCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                        Collectors.toList());
                buildAppraisalPreventRuleIdentifierQuery(searchValues,
                    appraisalPreventRuleIdentifiersCriteria.getCategory(),
                    ArchiveSearchConsts.CriteriaOperators.valueOf(
                        appraisalPreventRuleIdentifiersCriteria.getOperator()), query);
            }

            SearchCriteriaEltDto ruleStarDateCriteria = criteriaList.stream().filter(searchCriteriaEltDto
                    -> ArchiveSearchConsts.MANAGEMENT_RULE_START_DATE.equals(searchCriteriaEltDto.getCriteria()))
                .findFirst()
                .orElse(null);

            if (ruleStarDateCriteria != null) {
                List<String> searchValues =
                    ruleStarDateCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                        Collectors.toList());
                buildRuleStartDateQuery(ruleStarDateCriteria.getCategory().name(),
                    searchValues,
                    ArchiveSearchConsts.CriteriaOperators.valueOf(ruleStarDateCriteria.getOperator()),
                    query);
            }

            SearchCriteriaEltDto ruleInheritanceCriteria = criteriaList.stream().filter(searchCriteriaEltDto
                    -> ArchiveSearchConsts.MANAGEMENT_RULE_INHERITED_CRITERIA.equals(searchCriteriaEltDto.getCriteria()))
                .findFirst()
                .orElse(null);

            if (ruleInheritanceCriteria != null) {
                List<String> searchValues =
                    ruleInheritanceCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                        Collectors.toList());
                buildInheritedCategoryQuery(searchValues, ruleInheritanceCriteria.getCategory(),
                    ArchiveSearchConsts.CriteriaOperators.valueOf(ruleInheritanceCriteria.getOperator()),
                    query);
            }

            List<SearchCriteriaEltDto> identifiersCriteria =
                criteriaList.stream()
                    .filter(searchCriteriaEltDto -> ArchiveSearchConsts.RULE_IDENTIFIER
                        .equals(searchCriteriaEltDto.getCriteria()))
                    .collect(Collectors.toList());

            List<SearchCriteriaEltDto> endDatesCriteria = criteriaList.stream().filter(
                searchCriteriaEltDto -> ArchiveSearchConsts.RULE_END_DATE
                    .equals(searchCriteriaEltDto.getCriteria())).collect(Collectors.toList());

            buildMgtRulesSimpleCriteria(ruleCategory, query, criteriaList);
            List<SearchCriteriaEltDto> mgtRuleOriginCriteria =
                criteriaList.stream()
                    .filter(searchCriteriaEltDto -> searchCriteriaEltDto.getCriteria()
                        .equals(ArchiveSearchConsts.RULE_ORIGIN_CRITERIA))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(mgtRuleOriginCriteria)) {
                if (mgtRuleOriginCriteria.size() > 1) {
                    BooleanQuery multipleQueryOriginRuleQuery = and();
                    for (SearchCriteriaEltDto ruleOriginCriteria : mgtRuleOriginCriteria) {
                        handleSingleOriginCriteria(ruleCategory, identifiersCriteria, endDatesCriteria,
                            ruleOriginCriteria, multipleQueryOriginRuleQuery);
                    }
                    if (multipleQueryOriginRuleQuery.isReady()) {
                        query.add(multipleQueryOriginRuleQuery);
                    }
                } else {
                    handleSingleOriginCriteria(ruleCategory, identifiersCriteria, endDatesCriteria,
                        mgtRuleOriginCriteria.get(0), query);
                }
            }
            buildMgtRulesFinalActionOriginCriteria(ruleCategory, query, criteriaList);
            buildMgtRulesFinalActionCriteria(ruleCategory, query, criteriaList);
        }
    }

    private static void buildMgtRulesSimpleCriteria(String ruleCategory, BooleanQuery subQuery,
        List<SearchCriteriaEltDto> mgtRulesSimpleCriteriaList) throws InvalidCreateOperationException {

        BooleanQuery simpleCriteriaByOrigin = or();
        buildMgtRulesSimpleCriteriaByOrigin(ruleCategory, simpleCriteriaByOrigin,
            mgtRulesSimpleCriteriaList,
            ArchiveSearchConsts.RuleOrigin.SCOPED);
        buildMgtRulesSimpleCriteriaByOrigin(ruleCategory, simpleCriteriaByOrigin,
            mgtRulesSimpleCriteriaList,
            ArchiveSearchConsts.RuleOrigin.INHERITED);
        if (simpleCriteriaByOrigin.isReady()) {
            subQuery.add(simpleCriteriaByOrigin);
        }
    }

    private static void buildMgtRulesFinalActionCriteria(String ruleCategory, BooleanQuery mainQuery,
        List<SearchCriteriaEltDto> mgtRulesCriteriaList)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(mgtRulesCriteriaList)) {
            List<SearchCriteriaEltDto> mgtRulesFinalActionCriteria = mgtRulesCriteriaList.stream()
                .filter(criteria -> ArchiveSearchConsts.RULE_FINAL_ACTION_TYPE.equals(criteria.getCriteria()))
                .collect(Collectors.toList());
            BooleanQuery subQueryMgtRuleOriginFinalAction = or();
            for (SearchCriteriaEltDto finalActionCriteria : mgtRulesFinalActionCriteria) {
                for (CriteriaValue value : finalActionCriteria.getValues()) {
                    if (value.getValue() != null &&
                        ArchiveSearchConsts.FINAL_ACTION_TYPE_CONFLICT.equals(value.getValue())) {
                        BooleanQuery subQueryMgtRuleConflictFinalAction = and();
                        VitamQueryHelper.addParameterCriteria(subQueryMgtRuleConflictFinalAction,
                            ArchiveSearchConsts.CriteriaOperators.EQ,
                            COMPUTED_FIELDS + ruleCategory + FINAL_ACTION_FIELD,
                            List.of(ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_TYPE_VALUES_MAPPING
                                .get(ArchiveSearchConsts.FINAL_ACTION_TYPE_KEEP)));
                        VitamQueryHelper.addParameterCriteria(subQueryMgtRuleConflictFinalAction,
                            ArchiveSearchConsts.CriteriaOperators.EQ,
                            COMPUTED_FIELDS + ruleCategory + FINAL_ACTION_FIELD,
                            List.of(ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_TYPE_VALUES_MAPPING
                                .get(ArchiveSearchConsts.FINAL_ACTION_TYPE_ELIMINATION)));
                        if (subQueryMgtRuleConflictFinalAction.isReady()) {
                            mainQuery.add(subQueryMgtRuleConflictFinalAction);
                        }
                    } else {
                        String mappedValue =
                            ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_TYPE_VALUES_MAPPING
                                .get(value.getValue());
                        if (mappedValue != null) {
                            VitamQueryHelper.addParameterCriteria(subQueryMgtRuleOriginFinalAction,
                                ArchiveSearchConsts.CriteriaOperators.EQ,
                                SCOPED_FIELDS + ruleCategory + FINAL_ACTION_FIELD, List.of(mappedValue));
                            VitamQueryHelper.addParameterCriteria(subQueryMgtRuleOriginFinalAction,
                                ArchiveSearchConsts.CriteriaOperators.IN,
                                COMPUTED_FIELDS + ruleCategory + FINAL_ACTION_FIELD, List.of(mappedValue));
                        }
                    }
                }
            }
            if (subQueryMgtRuleOriginFinalAction.isReady()) {
                mainQuery.add(subQueryMgtRuleOriginFinalAction);
            }
        }
    }

    private static void buildMgtRulesSimpleCriteriaByOrigin(String ruleCategory,
        BooleanQuery mgtRulesSubQuery, List<SearchCriteriaEltDto> mgtRulesSimpleCriteriaList,
        ArchiveSearchConsts.RuleOrigin origin)
        throws InvalidCreateOperationException {
        handleSearchCriteriaByRuleIdentifier(ruleCategory, mgtRulesSubQuery,
            mgtRulesSimpleCriteriaList, origin);
        handleSearchCriterieByRuleMaxEndDates(ruleCategory, mgtRulesSubQuery,
            mgtRulesSimpleCriteriaList, origin);
    }

    private static void handleSearchCriterieByRuleMaxEndDates(String ruleCategory, BooleanQuery mgtRulesSubQuery,
        List<SearchCriteriaEltDto> mgtRulesSimpleCriteriaList,
        ArchiveSearchConsts.RuleOrigin origin)
        throws InvalidCreateOperationException {
        List<SearchCriteriaEltDto> mgtRuleEndDatesCriteria = mgtRulesSimpleCriteriaList.stream().filter(
            searchCriteriaEltDto -> ArchiveSearchConsts.RULE_END_DATE
                .equals(searchCriteriaEltDto.getCriteria())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(mgtRuleEndDatesCriteria)) {
            String endDtVitamFieldName;
            if (ArchiveSearchConsts.RuleOrigin.SCOPED.equals(origin)) {
                endDtVitamFieldName = SCOPED_FIELDS + ruleCategory + RULES_END_DATE_FIELD;
            } else {
                endDtVitamFieldName = COMPUTED_FIELDS + ruleCategory + MAX_END_DATE_FIELD;
            }
            buildMgtRulesEndDatesCriteria(mgtRulesSubQuery, mgtRuleEndDatesCriteria,
                endDtVitamFieldName);
        }
    }

    private static void handleSearchCriteriaByRuleIdentifier(String ruleCategory, BooleanQuery mgtRulesSubQuery,
        List<SearchCriteriaEltDto> mgtRulesSimpleCriteriaList, ArchiveSearchConsts.RuleOrigin origin)
        throws InvalidCreateOperationException {
        List<SearchCriteriaEltDto> identifierCriteria =
            mgtRulesSimpleCriteriaList.stream().filter(
                searchCriteriaEltDto -> ArchiveSearchConsts.RULE_IDENTIFIER
                    .equals(searchCriteriaEltDto.getCriteria())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(identifierCriteria)) {
            String ruleIdVitamFieldName;
            if (ArchiveSearchConsts.RuleOrigin.SCOPED.equals(origin)) {
                ruleIdVitamFieldName = SCOPED_FIELDS + ruleCategory + RULES_RULE_ID_FIELD;
            } else {
                ruleIdVitamFieldName = COMPUTED_FIELDS + ruleCategory + RULES_RULE_ID_FIELD;
            }
            BooleanQuery identifierQuery = or();
            for (SearchCriteriaEltDto criteria : identifierCriteria) {
                for (CriteriaValue valueIdentifier : criteria.getValues()) {
                    VitamQueryHelper.addParameterCriteria(identifierQuery, ArchiveSearchConsts.CriteriaOperators.EQ,
                        ruleIdVitamFieldName, List.of(valueIdentifier.getValue()));
                }
            }
            if (identifierQuery.isReady()) {
                mgtRulesSubQuery.add(identifierQuery);
            }
        }
    }

    private static void buildMgtRulesFinalActionOriginCriteria(String ruleCategory, BooleanQuery query,
        List<SearchCriteriaEltDto> mgtRulesCriteriaList)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(mgtRulesCriteriaList)) {
            List<SearchCriteriaEltDto> mgtRulesFinalActionCriteria = mgtRulesCriteriaList.stream()
                .filter(criteria -> ArchiveSearchConsts.RULE_FINAL_ACTION.equals(criteria.getCriteria()))
                .collect(Collectors.toList());
            BooleanQuery mgtRuleOriginFinalActionSubQuery = or();
            for (SearchCriteriaEltDto finalActionCriteria : mgtRulesFinalActionCriteria) {
                for (CriteriaValue value : finalActionCriteria.getValues()) {
                    String fieldName = null;
                    if (ArchiveSearchConsts.FINAL_ACTION_INHERITE_FINAL_ACTION
                        .equals(value.getValue())) {
                        fieldName = COMPUTED_FIELDS + ruleCategory + FINAL_ACTION_FIELD;
                    } else if (ArchiveSearchConsts.FINAL_ACTION_HAS_FINAL_ACTION
                        .equals(value.getValue())) {
                        fieldName = SCOPED_FIELDS + ruleCategory + FINAL_ACTION_FIELD;
                    }
                    VitamQueryHelper.addParameterCriteria(mgtRuleOriginFinalActionSubQuery,
                        ArchiveSearchConsts.CriteriaOperators.EXISTS,
                        fieldName, List.of());
                }
            }
            if (mgtRuleOriginFinalActionSubQuery.isReady()) {
                query.add(mgtRuleOriginFinalActionSubQuery);
            }
        }
    }

    private static void buildMgtRulesEndDatesCriteria(BooleanQuery mgtRulesSubQuery,
        List<SearchCriteriaEltDto> mgtRuleEndDatesCriteria,
        String fieldName) throws InvalidCreateOperationException {
        BooleanQuery ruleEndQuery = or();
        for (SearchCriteriaEltDto criteria : mgtRuleEndDatesCriteria) {
            for (CriteriaValue valueEndDate : criteria.getValues()) {
                BooleanQuery intervalQueryByInterval = and();
                String beginDtStr = valueEndDate.getBeginInterval();
                String endDtStr = valueEndDate.getEndInterval();

                if (!ObjectUtils.isEmpty(beginDtStr)) {
                    LocalDateTime beginDt =
                        LocalDateTime.parse(beginDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
                    VitamQueryHelper
                        .addParameterCriteria(intervalQueryByInterval, ArchiveSearchConsts.CriteriaOperators.GTE,
                            fieldName, List.of(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(beginDt)));
                }

                if (!ObjectUtils.isEmpty(endDtStr)) {
                    LocalDateTime endDt =
                        LocalDateTime.parse(endDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
                    VitamQueryHelper.addParameterCriteria(intervalQueryByInterval,
                        ArchiveSearchConsts.CriteriaOperators.LTE, fieldName,
                        List.of(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(endDt)));
                }
                if (intervalQueryByInterval.isReady()) {
                    ruleEndQuery.add(intervalQueryByInterval);
                }
            }
        }
        if (ruleEndQuery.isReady()) {
            mgtRulesSubQuery.add(ruleEndQuery);
        }
    }

    private static void buildRuleIdentifierQuery(String ruleCategory, final List<String> searchValues,
        ArchiveSearchConsts.CriteriaOperators operator, BooleanQuery subQueryAnd)
        throws InvalidCreateOperationException {
        String searchKey = null;
        switch (ruleCategory) {
            case "APPRAISAL_RULE":
                searchKey = ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER;
                break;
            case "ACCESS_RULE":
                searchKey = ArchiveSearchConsts.ACCESS_RULE_IDENTIFIER;
                break;
            case "STORAGE_RULE":
                searchKey = ArchiveSearchConsts.STORAGE_RULE_IDENTIFIER;
                break;
            case "HOLD_RULE":
                searchKey = ArchiveSearchConsts.HOLD_RULE_IDENTIFIER;
                break;
            case "DISSEMINATION_RULE":
                searchKey = ArchiveSearchConsts.DISSEMINATION_RULE_IDENTIFIER;
                break;
            case "REUSE_RULE":
                searchKey = ArchiveSearchConsts.REUSE_RULE_IDENTIFIER;
                break;
            case "CLASSIFICATION_RULE":
                searchKey = ArchiveSearchConsts.CLASSIFICATION_RULE_IDENTIFIER;
                break;
            default:
        }
        BooleanQuery subQueryOr = or();
        if (!CollectionUtils.isEmpty(searchValues)) {
            String finalSearchKey = searchKey;
            searchValues.forEach(value -> {
                try {
                    subQueryOr
                        .add(VitamQueryHelper.buildSubQueryByOperator(finalSearchKey, value, operator));
                } catch (InvalidCreateOperationException exception) {
                    LOGGER.error(INVALID_CREATION_OPERATION, exception);
                    throw new InvalidCreateOperationVitamUIException(COULD_NOT_CREATE_OPERATION,
                        exception);
                }
            });
            subQueryAnd.add(subQueryOr);
        }
    }

    private static void buildAppraisalPreventRuleIdentifierQuery(final List<String> searchValues,
        ArchiveSearchConsts.CriteriaCategory category,
        ArchiveSearchConsts.CriteriaOperators operator, BooleanQuery subQueryAnd)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryOr = or();
        if (!CollectionUtils.isEmpty(searchValues)) {
            searchValues.forEach(value -> {
                try {
                    subQueryOr
                        .add(VitamQueryHelper.buildSubQueryByOperator(
                            buildPreventRuleIdentifierFromCategory(category), value, operator));
                } catch (InvalidCreateOperationException exception) {
                    LOGGER.error(INVALID_CREATION_OPERATION, exception);
                    throw new InvalidCreateOperationVitamUIException(COULD_NOT_CREATE_OPERATION,
                        exception);
                }
            });
            subQueryAnd.add(subQueryOr);
        }
    }

    private static void buildInheritedCategoryQuery(final List<String> searchValues,
        ArchiveSearchConsts.CriteriaCategory category, ArchiveSearchConsts.CriteriaOperators operator,
        BooleanQuery subQueryAnd)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryOr = or();
        if (!CollectionUtils.isEmpty(searchValues)) {
            searchValues.forEach(searchValue ->
            {
                try {
                    subQueryOr
                        .add(VitamQueryHelper
                            .buildSubQueryByOperator(buildInheritedValueFromCategory(category), searchValue,
                                operator));
                } catch (InvalidCreateOperationException exception) {
                    LOGGER.error(INVALID_CREATION_OPERATION, exception);
                    throw new InvalidCreateOperationVitamUIException(COULD_NOT_CREATE_OPERATION,
                        exception);
                }
            });
        }
        subQueryAnd.add(subQueryOr);
    }

    private static String buildPreventRuleIdentifierFromCategory(ArchiveSearchConsts.CriteriaCategory category)
        throws InvalidCreateOperationException {

        String preventRuleIdentifier = "";

        switch (category.name()) {
            case "APPRAISAL_RULE":
                preventRuleIdentifier = ArchiveSearchConsts.APPRAISAL_PREVENT_RULE_IDENTIFIER;
                break;
            case "ACCESS_RULE":
                preventRuleIdentifier = ArchiveSearchConsts.ACCESS_PREVENT_RULE_IDENTIFIER;
                break;
            case "STORAGE_RULE":
                preventRuleIdentifier = ArchiveSearchConsts.STORAGE_PREVENT_RULE_IDENTIFIER;
                break;
            case "HOLD_RULE":
                preventRuleIdentifier = ArchiveSearchConsts.HOLD_PREVENT_RULE_IDENTIFIER;
                break;
            case "DISSEMINATION_RULE":
                preventRuleIdentifier = ArchiveSearchConsts.DISSEMINATION_PREVENT_RULE_IDENTIFIER;
                break;
            case "REUSE_RULE":
                preventRuleIdentifier = ArchiveSearchConsts.REUSE_PREVENT_RULE_IDENTIFIER;
                break;
            case "CLASSIFICATION_RULE":
                preventRuleIdentifier = ArchiveSearchConsts.CLASSIFICATION_PREVENT_RULE_IDENTIFIER;
                break;
            default:
        }

        if (preventRuleIdentifier.isEmpty()) {
            throw new InvalidCreateOperationException("inheritedValue is empty or null ");
        }

        return preventRuleIdentifier;
    }

    private static String buildInheritedValueFromCategory(ArchiveSearchConsts.CriteriaCategory category)
        throws InvalidCreateOperationException {

        String inheritedValue = "";

        ArchiveSearchConsts.CriteriaCategory criteriaCategory = null;
        try {
            criteriaCategory = ArchiveSearchConsts.CriteriaCategory.valueOf(category.name());
        } catch (IllegalArgumentException e) {
            throw new InvalidCreateOperationException("category name is invalid ");
        }

        switch (criteriaCategory) {
            case APPRAISAL_RULE:
                inheritedValue = ArchiveSearchConsts.APPRAISAL_RULE_INHERITED;
                break;
            case ACCESS_RULE:
                inheritedValue = ArchiveSearchConsts.ACCESS_RULE_INHERITED;
                break;
            case STORAGE_RULE:
                inheritedValue = ArchiveSearchConsts.STORAGE_RULE_INHERITED;
                break;
            case HOLD_RULE:
                inheritedValue = ArchiveSearchConsts.HOLD_RULE_INHERITED;
                break;
            case DISSEMINATION_RULE:
                inheritedValue = ArchiveSearchConsts.DISSEMINATION_RULE_INHERITED;
                break;
            case REUSE_RULE:
                inheritedValue = ArchiveSearchConsts.REUSE_RULE_INHERITED;
                break;
            case CLASSIFICATION_RULE:
                inheritedValue = ArchiveSearchConsts.CLASSIFICATION_RULE_INHERITED;
                break;
            default:
        }

        if (inheritedValue.isEmpty()) {
            throw new InvalidCreateOperationException("inheritedValue is empty or null ");
        }

        return inheritedValue;
    }

    private static void buildRuleStartDateQuery(String ruleCategory, final List<String> searchValues,
        ArchiveSearchConsts.CriteriaOperators operator, BooleanQuery subQueryAnd)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryOr = or();

        String searchKey = null;

        ArchiveSearchConsts.CriteriaCategory criteriaCategory = null;
        try {
            criteriaCategory = ArchiveSearchConsts.CriteriaCategory.valueOf(ruleCategory);
        } catch (IllegalArgumentException e) {
            throw new InvalidCreateOperationException("category name is invalid ");
        }

        switch (criteriaCategory) {
            case APPRAISAL_RULE:
                searchKey = ArchiveSearchConsts.APPRAISAL_RULE_START_DATE_FIELD;
                break;
            case ACCESS_RULE:
                searchKey = ArchiveSearchConsts.ACCESS_RULE_START_DATE_FIELD;
                break;
            case STORAGE_RULE:
                searchKey = ArchiveSearchConsts.STORAGE_RULE_START_DATE_FIELD;
                break;
            case HOLD_RULE:
                searchKey = ArchiveSearchConsts.HOLD_RULE_START_DATE_FIELD;
                break;
            case DISSEMINATION_RULE:
                searchKey = ArchiveSearchConsts.DISSEMINATION_RULE_START_DATE_FIELD;
                break;
            case REUSE_RULE:
                searchKey = ArchiveSearchConsts.REUSE_RULE_START_DATE_FIELD;
                break;
            case CLASSIFICATION_RULE:
                searchKey = ArchiveSearchConsts.CLASSIFICATION_RULE_START_DATE_FIELD;
                break;
            default:
        }

        if (!CollectionUtils.isEmpty(searchValues)) {
            String finalSearchKey = searchKey;
            searchValues.forEach(searchValue -> {
                LocalDateTime startDate =
                    LocalDateTime.parse(searchValue, ArchiveSearchConsts.ISO_FRENCH_FORMATER).withHour(0)
                        .withMinute(0).withSecond(0).withNano(0);
                try {
                    subQueryOr
                        .add(VitamQueryHelper.buildSubQueryByOperator(
                            finalSearchKey,
                            ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(startDate.plusDays(1)), operator));
                } catch (InvalidCreateOperationException exception) {
                    LOGGER.error("Invalid create operation {}", exception);
                    throw new InvalidCreateOperationVitamUIException(COULD_NOT_CREATE_OPERATION,
                        exception);
                }
            });
            subQueryAnd.add(subQueryOr);
        }
    }

    private static void handleSingleOriginCriteria(String ruleCategory, List<SearchCriteriaEltDto> identifiersCriteria,
        List<SearchCriteriaEltDto> endDatesCriteria, SearchCriteriaEltDto mgtRuleOriginCriteria,
        BooleanQuery mainQuery) throws InvalidCreateOperationException {
        BooleanQuery originRuleQuery = or();

        Optional<CriteriaValue> inheritOrLocale =
            mgtRuleOriginCriteria.getValues().stream().filter(
                value -> ArchiveSearchConsts.RuleOriginValues.ORIGIN_LOCAL_OR_INHERIT_RULES.name()
                    .equals(value.getValue())).findAny();

        if (inheritOrLocale.isPresent()) {
            buildQueryForInheritOrLocalRulesCriteria(ruleCategory, originRuleQuery);
        }

        Optional<CriteriaValue> waitingToRecalculate =
            mgtRuleOriginCriteria.getValues().stream().filter(
                value -> ArchiveSearchConsts.RuleOriginValues.ORIGIN_WAITING_RECALCULATE.name()
                    .equals(value.getValue())).findAny();
        if (waitingToRecalculate.isPresent()) {
            handleWaitingToComputeInheritance(originRuleQuery);
        }

        Optional<CriteriaValue> hasAtLeastOneRule =
            mgtRuleOriginCriteria.getValues().stream().filter(
                value -> ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_AT_LEAST_ONE.name()
                    .equals(value.getValue())).findAny();
        if (hasAtLeastOneRule.isPresent()) {
            buildQueryForHasAtLeastRulesCriteria(ruleCategory, originRuleQuery,
                identifiersCriteria, endDatesCriteria);
        }

        Optional<CriteriaValue> inheritAtLeastOneRule =
            mgtRuleOriginCriteria.getValues().stream().filter(
                value -> ArchiveSearchConsts.RuleOriginValues.ORIGIN_INHERITE_AT_LEAST_ONE.name()
                    .equals(value.getValue())).findAny();
        if (inheritAtLeastOneRule.isPresent()) {
            buildQueryForInheritRulesCriteria(ruleCategory, originRuleQuery, identifiersCriteria,
                endDatesCriteria);
        }

        Optional<CriteriaValue> hasNoRule =
            mgtRuleOriginCriteria.getValues().stream().filter(
                value -> ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_NO_ONE.name()
                    .equals(value.getValue())).findAny();
        if (hasNoRule.isPresent()) {
            buildQueryForNoRulesCriteria(ruleCategory, originRuleQuery, identifiersCriteria);
        }

        if (originRuleQuery.isReady()) {
            mainQuery.add(originRuleQuery);
        }
    }

    private static void buildQueryForHasAtLeastRulesCriteria(String ruleCategory, BooleanQuery mainQuery,
        List<SearchCriteriaEltDto> rulesIdentifiersCriteria, List<SearchCriteriaEltDto> rulesEndDatesCriteria)
        throws InvalidCreateOperationException {
        LOGGER.debug("Start handling query for has At least {} rules", ruleCategory);
        if (CollectionUtils.isEmpty(rulesIdentifiersCriteria)) {
            //manage just criteria on category
            VitamQueryHelper.addParameterCriteria(mainQuery, ArchiveSearchConsts.CriteriaOperators.EXISTS,
                SCOPED_FIELDS + ruleCategory + RULES_RULE_ID_FIELD, List.of());
        } else {
            List<String> rulesIdentifiers =
                rulesIdentifiersCriteria.stream().map(SearchCriteriaEltDto::getValues).flatMap(Collection::stream)
                    .map(CriteriaValue::getValue).collect(Collectors.toList());
            VitamQueryHelper.addParameterCriteria(mainQuery,
                ArchiveSearchConsts.CriteriaOperators.EQ, SCOPED_FIELDS + ruleCategory + RULES_RULE_ID_FIELD,
                rulesIdentifiers);
        }

        if (!CollectionUtils.isEmpty(rulesEndDatesCriteria)) { //manage criteria just on max End Dates on category
            buildMgtRulesEndDatesCriteria(mainQuery, rulesEndDatesCriteria,
                SCOPED_FIELDS + ruleCategory + RULES_END_DATE_FIELD);
        }
    }

    private static void buildQueryForInheritRulesCriteria(String ruleCategory, BooleanQuery mainQuery,
        List<SearchCriteriaEltDto> rulesIdentifiersCriteria, List<SearchCriteriaEltDto> rulesEndDatesCriteria)
        throws InvalidCreateOperationException {
        LOGGER.debug("Start handling query for inherit At least {} rules", ruleCategory);
        if (CollectionUtils.isEmpty(rulesIdentifiersCriteria)) {
            //manage just criteria on category
            VitamQueryHelper.addParameterCriteria(mainQuery, ArchiveSearchConsts.CriteriaOperators.EQ,
                COMPUTED_FIELDS + ruleCategory + INHERITANCE_ORIGIN_FIELD, List.of(INHERITED_ORIGIN_TYPE));
        } else {
            List<String> rulesIdentifiers =
                rulesIdentifiersCriteria.stream().map(SearchCriteriaEltDto::getValues).flatMap(Collection::stream)
                    .map(CriteriaValue::getValue).collect(Collectors.toList());
            VitamQueryHelper.addParameterCriteria(mainQuery, ArchiveSearchConsts.CriteriaOperators.EQ,
                COMPUTED_FIELDS + ruleCategory + INHERITED_RULE_IDS_FIELD, rulesIdentifiers);
        }

        if (!CollectionUtils.isEmpty(rulesEndDatesCriteria)) { //manage criteria just on max End Dates on category
            buildMgtRulesEndDatesCriteria(mainQuery, rulesEndDatesCriteria,
                COMPUTED_FIELDS + ruleCategory + MAX_END_DATE_FIELD);
        }
    }

    private static void buildQueryForInheritOrLocalRulesCriteria(String ruleCategory, BooleanQuery mainQuery)
        throws InvalidCreateOperationException {
        LOGGER.debug("Start handling query for inherit or hold at least {} rules", ruleCategory);
        //manage just criteria on category
        BooleanQuery orQuery = or();
        VitamQueryHelper.addParameterCriteria(orQuery, ArchiveSearchConsts.CriteriaOperators.EQ,
            COMPUTED_FIELDS + ruleCategory + INHERITANCE_ORIGIN_FIELD, List.of(INHERITED_ORIGIN_TYPE));

        VitamQueryHelper.addParameterCriteria(orQuery, ArchiveSearchConsts.CriteriaOperators.EQ,
            COMPUTED_FIELDS + ruleCategory + INHERITANCE_ORIGIN_FIELD, List.of(LOCAL_OR_INHERITED_ORIGIN_TYPE));

        VitamQueryHelper.addParameterCriteria(mainQuery, ArchiveSearchConsts.CriteriaOperators.EQ,
            COMPUTED_FIELDS + ruleCategory + INHERITANCE_ORIGIN_FIELD, List.of(LOCAL_ORIGIN_TYPE));
        mainQuery.add(orQuery);
    }

    private static void buildQueryForNoRulesCriteria(String ruleCategory, BooleanQuery mainQuery,
        List<SearchCriteriaEltDto> rulesIdentifiersCriteria) throws InvalidCreateOperationException {
        LOGGER.debug("Start handling query for none {} rules", ruleCategory);
        if (CollectionUtils.isEmpty(rulesIdentifiersCriteria)) {
            //No rule found on management and computed
            BooleanQuery mgtRulesSubQuery = and();
            VitamQueryHelper
                .addParameterCriteria(mgtRulesSubQuery, ArchiveSearchConsts.CriteriaOperators.MISSING,
                    SCOPED_FIELDS + ruleCategory + RULES_RULE_ID_FIELD, List.of());
            VitamQueryHelper
                .addParameterCriteria(mgtRulesSubQuery, ArchiveSearchConsts.CriteriaOperators.MISSING,
                    COMPUTED_FIELDS + ruleCategory + INHERITANCE_ORIGIN_FIELD, List.of());
            mainQuery.add(mgtRulesSubQuery);
            mainQuery.setDepthLimit(1);
        } else {
            BooleanQuery mgtRulesSubQuery = and();
            List<String> rulesIdentifiers =
                rulesIdentifiersCriteria.stream().map(SearchCriteriaEltDto::getValues).flatMap(Collection::stream)
                    .map(CriteriaValue::getValue).collect(
                        Collectors.toList());
            VitamQueryHelper
                .addParameterCriteria(mgtRulesSubQuery, ArchiveSearchConsts.CriteriaOperators.NOT_EQ,
                    COMPUTED_FIELDS + ruleCategory + INHERITED_RULE_IDS_FIELD, rulesIdentifiers);
            VitamQueryHelper
                .addParameterCriteria(mgtRulesSubQuery, ArchiveSearchConsts.CriteriaOperators.NOT_EQ,
                    SCOPED_FIELDS + ruleCategory + RULES_RULE_ID_FIELD, rulesIdentifiers);
            mainQuery.add(mgtRulesSubQuery);
        }
    }

    private static void handleWaitingToComputeInheritance(BooleanQuery mgtRuleOriginQuery)
        throws InvalidCreateOperationException {
        VitamQueryHelper.addParameterCriteria(mgtRuleOriginQuery,
            ArchiveSearchConsts.CriteriaOperators.EQ, WAITING_TO_COMPUTE_RULES_STATUS,
            List.of(ArchiveSearchConsts.FALSE_CRITERIA_VALUE));
    }

    public static void fillQueryFromCriteriaList(BooleanQuery queryToFill, List<SearchCriteriaEltDto> criteriaList)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(criteriaList)) {
            for (SearchCriteriaEltDto searchCriteria : criteriaList) {
                if (searchCriteria.getCriteria() == null) {
                    throw new IllegalArgumentException("Field not mapped correctly  ");
                }
                switch (searchCriteria.getCriteria()) {
                    case ArchiveSearchConsts.TITLE_OR_DESCRIPTION:
                        queryToFill.add(buildTitleAndDescriptionQuery(
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList()),
                            ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                        break;

                    case ArchiveSearchConsts.ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE:
                        queryToFill.add(buildEliminationAnalysisSearchQuery(
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList()),
                            ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                        break;

                    case ArchiveSearchConsts.TITLE_CRITERIA:
                        queryToFill.add(buildTitleQuery(
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList()),
                            ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                        break;
                    case ArchiveSearchConsts.DESCRIPTION_CRITERIA:
                        queryToFill.add(buildDescriptionQuery(
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList()),
                            ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                        break;

                    case ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA:
                        queryToFill.add(buildArchiveUnitTypeQuery(
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList())));
                        break;
                    case ArchiveSearchConsts.ORPHANS_NODE_CRITERIA:
                        queryToFill.add(buildOrphansNodeQuery());
                        break;

                    case ArchiveSearchConsts.DESCRIPTION_LEVEL_CRITERIA:
                        queryToFill.add(buildArchiveUnitDescriptionLevelQuery(
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList()),
                            ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                        break;
                    case ArchiveSearchConsts.WAITING_RECALCULATE:
                        handleWaitingToRecalculateFlag(queryToFill, searchCriteria,
                            ArchiveSearchConsts.CriteriaOperators.NOT_EQ, ArchiveSearchConsts.CriteriaOperators.EQ);
                        break;
                    case ArchiveSearchConsts.RULES_COMPUTED:
                        handleWaitingToRecalculateFlag(queryToFill, searchCriteria,
                            ArchiveSearchConsts.CriteriaOperators.EQ, ArchiveSearchConsts.CriteriaOperators.NOT_EQ);
                        break;
                    default:
                        handleSimpleFieldCriteria(queryToFill, searchCriteria);
                        break;
                }
                if (isADate(searchCriteria) && !isADateToReplace(searchCriteria)) {
                    queryToFill.add(buildStartDateEndDateQuery(searchCriteria.getCriteria(),
                        searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                            Collectors.toList()),
                        ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                }
            }
        }
    }

    private static Query buildTitleAndDescriptionQuery(final List<String> searchValues,
        ArchiveSearchConsts.CriteriaOperators operator)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        BooleanQuery subQueryOr = or();
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.TITLE, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.TITLE_FR, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.TITLE_EN, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.DESCRIPTION, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.DESCRIPTION_EN, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.DESCRIPTION_FR, value, operator));
            }
            subQueryAnd.add(subQueryOr);
        }
        return subQueryAnd;
    }

    private static Query buildTitleQuery(final List<String> searchValues,
        ArchiveSearchConsts.CriteriaOperators operator)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        BooleanQuery subQueryOr = or();
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.TITLE, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.TITLE_FR, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.TITLE_EN, value, operator));
            }
            subQueryAnd.add(subQueryOr);
        }
        return subQueryAnd;
    }

    private static Query buildDescriptionQuery(final List<String> searchValues,
        ArchiveSearchConsts.CriteriaOperators operator)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        BooleanQuery subQueryOr = or();
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.DESCRIPTION, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.DESCRIPTION_EN, value, operator));
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.DESCRIPTION_FR, value, operator));
            }
            subQueryAnd.add(subQueryOr);
        }
        return subQueryAnd;
    }

    private static Query buildEliminationAnalysisSearchQuery(final List<String> searchValues,
        ArchiveSearchConsts.CriteriaOperators operator)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        BooleanQuery subQueryOr = or();
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                subQueryOr
                    .add(VitamQueryHelper
                        .buildSubQueryByOperator(ArchiveSearchConsts.ELIMINATION_GUID, value, operator));
            }
            subQueryAnd.add(subQueryOr);
        }
        return subQueryAnd;
    }

    private static Query buildStartDateEndDateQuery(String searchCriteria, final List<String> searchValues,
        ArchiveSearchConsts.CriteriaOperators operator)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        BooleanQuery subQueryOr = or();
        String criteria;
        switch (searchCriteria) {
            case ArchiveSearchConsts.START_DATE_CRITERIA:
                criteria = ArchiveSearchConsts.START_DATE;
                break;
            case ArchiveSearchConsts.END_DATE_CRITERIA:
                criteria = ArchiveSearchConsts.END_DATE;
                break;
            default:
                criteria = searchCriteria;
        }
        LOGGER.debug("The search criteria Date is {} ", criteria);
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                LocalDateTime searchDate = LocalDateTime.parse(value, ArchiveSearchConsts.ISO_FRENCH_FORMATER)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
                subQueryOr.add(VitamQueryHelper.buildSubQueryByOperator(criteria,
                    ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(searchDate.plusDays(1)), operator));
            }
            subQueryAnd.add(subQueryOr);
        }
        return subQueryAnd;
    }

    private static Query buildOrphansNodeQuery()
        throws InvalidCreateOperationException {
        return and().add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.UNIT_UPS, null, MISSING));
    }

    private static Query buildArchiveUnitTypeQuery(final List<String> searchValues)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        BooleanQuery subQueryOr = or();
        final Query queryForIngestUnitType =
            VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES,
                ArchiveSearchConsts.ARCHIVE_UNIT_INGEST,
                EQ);
        LOGGER.debug("The search criteria is on the unit type");
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                switch (value) {
                    case ARCHIVE_UNIT_FILING_UNIT:
                        subQueryOr
                            .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES,
                                FILING_UNIT_TYPE,
                                ArchiveSearchConsts.CriteriaOperators.EQ));
                        break;
                    case ARCHIVE_UNIT_HOLDING_UNIT:
                        subQueryOr
                            .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES,
                                HOLDING_UNIT_TYPE,
                                ArchiveSearchConsts.CriteriaOperators.EQ));
                        break;

                    case ARCHIVE_UNIT_WITH_OBJECTS:
                        final Query queryForExistedObjects = VitamQueryHelper
                            .buildSubQueryByOperator(ArchiveSearchConsts.ARCHIVE_UNIT_OBJECTS, value,
                                ArchiveSearchConsts.CriteriaOperators.EXISTS);
                        subQueryOr.add(and().add(queryForIngestUnitType, queryForExistedObjects));
                        break;
                    case ARCHIVE_UNIT_WITHOUT_OBJECTS:
                        final Query queryForMissingObjects = VitamQueryHelper
                            .buildSubQueryByOperator(ArchiveSearchConsts.ARCHIVE_UNIT_OBJECTS, value,
                                ArchiveSearchConsts.CriteriaOperators.MISSING);
                        subQueryOr.add(and().add(queryForIngestUnitType, queryForMissingObjects));
                        break;
                    default:
                        LOGGER.error("Can not find binding for value: {}", searchValues);
                        break;

                }
            }
            subQueryAnd.add(subQueryOr);
        }
        return subQueryAnd;
    }

    private static Query buildArchiveUnitDescriptionLevelQuery(final List<String> searchValues,
        ArchiveSearchConsts.CriteriaOperators operator)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        BooleanQuery subQueryOr = or();
        LOGGER.debug("The search criteria is {} ", ArchiveSearchConsts.DESCRIPTION_LEVEL);
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                subQueryOr
                    .add(VitamQueryHelper
                        .buildSubQueryByOperator(ArchiveSearchConsts.DESCRIPTION_LEVEL, value, operator));
            }
            subQueryAnd.add(subQueryOr);
        }
        return subQueryAnd;
    }

    private static void handleWaitingToRecalculateFlag(BooleanQuery queryToFill, SearchCriteriaEltDto searchCriteria,
        ArchiveSearchConsts.CriteriaOperators notEq, ArchiveSearchConsts.CriteriaOperators eq)
        throws InvalidCreateOperationException {
        Optional<String> validInheritedRulesValueOpt =
            searchCriteria.getValues().stream().map(CriteriaValue::getValue).findAny();
        if (validInheritedRulesValueOpt.isPresent()) {
            if (validInheritedRulesValueOpt.get().equals(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)) {
                VitamQueryHelper.addParameterCriteria(queryToFill,
                    notEq,
                    SIMPLE_FIELDS_VALUES_MAPPING
                        .get(ArchiveSearchConsts.RULES_COMPUTED),
                    List.of(ArchiveSearchConsts.TRUE_CRITERIA_VALUE));
            } else {
                VitamQueryHelper.addParameterCriteria(queryToFill,
                    eq,
                    SIMPLE_FIELDS_VALUES_MAPPING
                        .get(ArchiveSearchConsts.RULES_COMPUTED),
                    List.of(ArchiveSearchConsts.TRUE_CRITERIA_VALUE));
            }
        }
    }

    public static void handleSimpleFieldCriteria(BooleanQuery queryToFill, SearchCriteriaEltDto searchCriteria)
        throws InvalidCreateOperationException {
        String mappedCriteriaName = SIMPLE_FIELDS_VALUES_MAPPING.containsKey(searchCriteria.getCriteria()) ?
            SIMPLE_FIELDS_VALUES_MAPPING.get(searchCriteria.getCriteria()) :
            searchCriteria.getCriteria();

        List<String> stringValues =
            searchCriteria.getValues().stream().filter(Objects::nonNull)
                .map(CriteriaValue::getValue).filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (isADateToReplace(searchCriteria)) {
            String stringDate = searchCriteria.getValues().get(0).getValue();
            LocalDateTime date = LocalDateTime.parse(stringDate, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
            String theDay = ArchiveSearchConsts.ISO_FRENCH_FORMATER.format(date);
            String theNextDay = ArchiveSearchConsts.ISO_FRENCH_FORMATER.format(date.plusDays(1));
            BooleanQuery andQuery = and();
            VitamQueryHelper.addParameterCriteria(andQuery,
                ArchiveSearchConsts.CriteriaOperators.GTE,
                mappedCriteriaName, List.of(theDay));
            VitamQueryHelper.addParameterCriteria(andQuery,
                ArchiveSearchConsts.CriteriaOperators.LT,
                mappedCriteriaName, List.of(theNextDay));
            queryToFill.add(andQuery);
        } else {
            VitamQueryHelper.addParameterCriteria(queryToFill,
                ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator()),
                mappedCriteriaName, stringValues);
        }
    }

    public static boolean isADate(SearchCriteriaEltDto searchCriteria) {
        return ArchiveSearchConsts.CriteriaDataType.DATE.name().equals(searchCriteria.getDataType());
    }

    public static boolean isADateToReplace(SearchCriteriaEltDto searchCriteria) {
        return isADate(searchCriteria)
            && ArchiveSearchConsts.CriteriaOperators.EQ.name().equals(searchCriteria.getOperator())
            && searchCriteria.getValues().size() == 1
            && StringUtils.isNotEmpty(searchCriteria.getValues().get(0).getValue());
    }

    private static void addPositionsNodesFacet(SearchCriteriaDto searchQuery, SelectMultiQuery selectMultiQuery)
        throws InvalidCreateOperationException {
        List<String> nodesCriteriaList = searchQuery.extractNodesCriteria();
        selectMultiQuery.addFacets(FacetHelper.terms(FACETS_COUNT_BY_NODE, ALL_UNIT_UPS,
            (nodesCriteriaList.size() + 1) * FACET_SIZE_MILTIPLIER, FacetOrder.ASC));
    }

    public static String cleanString(String initialValue) {
        if (initialValue == null)
            return null;
        return initialValue.replace(SEMI_COLON, COMMA).replace(DOUBLE_QUOTE, SINGLE_QUOTE)
            .replace(NEW_LINE, SPACE)
            .replace(NEW_LINE_1, SPACE)
            .replace(NEW_TAB, SPACE);
    }
}
