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

import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitamui.archives.search.common.common.AppraisalRuleOriginRuleCriteria;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.not;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

/**
 * * Service to build DSL Query for appraisal rules criteria for extracting archive units
 */
@Service
public class ArchivesSearchAppraisalQueryBuilderService implements IArchivesSearchAppraisalQueryBuilderService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchAppraisalQueryBuilderService.class);



    @Override
    public void fillQueryFromCriteriaList(BooleanQuery query, List<SearchCriteriaEltDto> criteriaList)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(criteriaList)) {
            buildAppraisalMgtRulesSimpleCriteria(query, criteriaList);
            AppraisalRuleOriginRuleCriteria appraisalRuleOriginRuleCriteria =
                extractCriteriaAppraisalBooleanValues(criteriaList);
            if (appraisalRuleOriginRuleCriteria.containsOriginRule()) {
                addUnitAppraisalMgtRulesOriginCriteria(criteriaList, query,
                    appraisalRuleOriginRuleCriteria);
            }
            addUnitAppraisalFinalActionOriginCriteria(criteriaList, query);
            addUnitAppraisalFinalActionCriteria(criteriaList, query);

        }
    }


    private void addUnitAppraisalMgtRulesOriginCriteria(List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList,
        BooleanQuery mainQuery, AppraisalRuleOriginRuleCriteria appraisalRuleOriginRuleCriteria)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAppraisalRuleOrigin = or();

        if (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList) &&
            appraisalRuleOriginRuleCriteria.containsOriginRule()) {

            handleWaitingToRecalculateInheritage(appraisalRuleOriginRuleCriteria, subQueryAppraisalRuleOrigin);
            handleInheritedRules(appraisalMgtRulesCriteriaList, appraisalRuleOriginRuleCriteria,
                subQueryAppraisalRuleOrigin);
            handleScopedRules(appraisalMgtRulesCriteriaList, appraisalRuleOriginRuleCriteria,
                subQueryAppraisalRuleOrigin);
            if (subQueryAppraisalRuleOrigin.isReady()) {
                mainQuery.add(subQueryAppraisalRuleOrigin);
            }
        }

    }

    private void handleScopedRules(List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList,
        AppraisalRuleOriginRuleCriteria appraisalRuleOriginRuleCriteria, BooleanQuery subQueryAppraisalRuleOrigin)
        throws InvalidCreateOperationException {
        if (appraisalRuleOriginRuleCriteria.getInheritAtLeastOneRule() != null) {
            if (Boolean.TRUE.equals(appraisalRuleOriginRuleCriteria.getInheritAtLeastOneRule())) {
                if (appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule() != null &&
                    appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule()) {

                    BooleanQuery appraisalMgtRulesSubQuery = and();

                    VitamQueryHelper.addParameterCriteria(appraisalMgtRulesSubQuery,
                        ArchiveSearchConsts.CriteriaOperators.EXISTS,
                        ArchiveSearchConsts.APPRAISAL_RULE_ORIGIN_INHERITED_FIELD, List.of());

                    buildAppraisalMgtRulesSimpleCriteriaByOrigin(appraisalMgtRulesSubQuery,
                        appraisalMgtRulesCriteriaList, ArchiveSearchConsts.AppraisalRuleOrigin.INHERITED);
                    subQueryAppraisalRuleOrigin.add(appraisalMgtRulesSubQuery);

                    BooleanQuery appraisalMgtRulesSubQueryInh = and();
                    VitamQueryHelper.addParameterCriteria(appraisalMgtRulesSubQueryInh,
                        ArchiveSearchConsts.CriteriaOperators.EXISTS,
                        ArchiveSearchConsts.APPRAISAL_RULE_ORIGIN_SCOPED_FIELD, List.of());
                    buildAppraisalMgtRulesSimpleCriteriaByOrigin(appraisalMgtRulesSubQueryInh,
                        appraisalMgtRulesCriteriaList, ArchiveSearchConsts.AppraisalRuleOrigin.SCOPED);
                    subQueryAppraisalRuleOrigin.add(appraisalMgtRulesSubQueryInh);

                    LOGGER.debug("subQueryAppraisalRuleOrigin query: {}", subQueryAppraisalRuleOrigin.toString());
                } else {
                    BooleanQuery subQueryAppraisalRuleOriginRul = and();
                    BooleanQuery subQueryAppraisalRuleOriginExitsInherit = and();
                    VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginExitsInherit,
                        ArchiveSearchConsts.CriteriaOperators.EXISTS,
                        ArchiveSearchConsts.APPRAISAL_RULE_ORIGIN_INHERITED_FIELD, List.of());

                    buildAppraisalMgtRulesSimpleCriteriaByOrigin(subQueryAppraisalRuleOriginExitsInherit,
                        appraisalMgtRulesCriteriaList, ArchiveSearchConsts.AppraisalRuleOrigin.INHERITED);
                    LOGGER.debug("subQueryAppraisalRuleOriginExitsInherit query: {}",
                        subQueryAppraisalRuleOriginExitsInherit.toString());
                    subQueryAppraisalRuleOriginRul.add(subQueryAppraisalRuleOriginExitsInherit);

                    BooleanQuery subQueryAppraisalRuleOriginNotExitsScoped = not();
                    BooleanQuery subQueryAppraisalRuleOriginNoRuleScoped = and();

                    VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginNoRuleScoped,
                        ArchiveSearchConsts.CriteriaOperators.EXISTS,
                        ArchiveSearchConsts.APPRAISAL_RULE_ORIGIN_SCOPED_FIELD, List.of());

                    buildAppraisalMgtRulesSimpleCriteriaByOrigin(subQueryAppraisalRuleOriginNoRuleScoped,
                        appraisalMgtRulesCriteriaList,
                        ArchiveSearchConsts.AppraisalRuleOrigin.SCOPED);
                    subQueryAppraisalRuleOriginNotExitsScoped.add(subQueryAppraisalRuleOriginNoRuleScoped);
                    LOGGER.debug("subQueryAppraisalRuleOriginNotExitsScoped query: {}",
                        subQueryAppraisalRuleOriginNotExitsScoped.toString());

                    subQueryAppraisalRuleOriginRul.add(subQueryAppraisalRuleOriginNotExitsScoped);

                    subQueryAppraisalRuleOrigin.add(subQueryAppraisalRuleOriginRul);
                }
            } else {
                if (appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule() != null &&
                    appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule()) {
                    BooleanQuery appraisalMgtRulesSubQuery = and();
                    VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOrigin,
                        ArchiveSearchConsts.CriteriaOperators.EXISTS,
                        ArchiveSearchConsts.APPRAISAL_RULE_ORIGIN_SCOPED_FIELD, List.of());
                    buildAppraisalMgtRulesSimpleCriteriaByOrigin(appraisalMgtRulesSubQuery,
                        appraisalMgtRulesCriteriaList,
                        ArchiveSearchConsts.AppraisalRuleOrigin.SCOPED);
                    subQueryAppraisalRuleOrigin.add(appraisalMgtRulesSubQuery);
                }
            }
        } else {
            if (appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule() != null) {
                BooleanQuery appraisalMgtRulesSubQuery = and();

                VitamQueryHelper.addParameterCriteria(appraisalMgtRulesSubQuery,
                    Boolean.TRUE.equals(appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule()) ?
                        ArchiveSearchConsts.CriteriaOperators.EXISTS :
                        ArchiveSearchConsts.CriteriaOperators.MISSING,
                    ArchiveSearchConsts.APPRAISAL_MGT_RULES_FIELDS_MAPPING.get(
                        ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE
                            .name()),
                    List.of());

                buildAppraisalMgtRulesSimpleCriteriaByOrigin(appraisalMgtRulesSubQuery,
                    appraisalMgtRulesCriteriaList,
                    ArchiveSearchConsts.AppraisalRuleOrigin.SCOPED);
                subQueryAppraisalRuleOrigin.add(appraisalMgtRulesSubQuery);
            }
        }
    }

    private void handleInheritedRules(List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList,
        AppraisalRuleOriginRuleCriteria appraisalRuleOriginRuleCriteria, BooleanQuery subQueryAppraisalRuleOrigin)
        throws InvalidCreateOperationException {
        if (appraisalRuleOriginRuleCriteria.getHasNoRule() != null &&
            appraisalRuleOriginRuleCriteria.getHasNoRule()) {

            BooleanQuery subQueryAppraisalRuleOriginNoRul = and();
            BooleanQuery subQueryAppraisalRuleOriginNotExitsInherit = not();
            BooleanQuery subQueryAppraisalRuleOriginNoRuleInherit = and();
            VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginNoRuleInherit,
                ArchiveSearchConsts.CriteriaOperators.EXISTS,
                ArchiveSearchConsts.APPRAISAL_RULE_ORIGIN_INHERITED_FIELD, List.of());

            buildAppraisalMgtRulesSimpleCriteriaByOrigin(subQueryAppraisalRuleOriginNoRuleInherit,
                appraisalMgtRulesCriteriaList, ArchiveSearchConsts.AppraisalRuleOrigin.INHERITED);
            subQueryAppraisalRuleOriginNotExitsInherit.add(subQueryAppraisalRuleOriginNoRuleInherit);
            LOGGER.debug("subQueryAppraisalRuleOriginNotExitsInherit query: {}",
                subQueryAppraisalRuleOriginNotExitsInherit.toString());
            subQueryAppraisalRuleOriginNoRul.add(subQueryAppraisalRuleOriginNotExitsInherit);

            BooleanQuery subQueryAppraisalRuleOriginNotExitsScoped = not();
            BooleanQuery subQueryAppraisalRuleOriginNoRuleScoped = and();

            VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginNoRuleScoped,
                ArchiveSearchConsts.CriteriaOperators.EXISTS,
                ArchiveSearchConsts.APPRAISAL_RULE_ORIGIN_SCOPED_FIELD, List.of());

            buildAppraisalMgtRulesSimpleCriteriaByOrigin(subQueryAppraisalRuleOriginNoRuleScoped,
                appraisalMgtRulesCriteriaList,
                ArchiveSearchConsts.AppraisalRuleOrigin.SCOPED);
            subQueryAppraisalRuleOriginNotExitsScoped.add(subQueryAppraisalRuleOriginNoRuleScoped);
            LOGGER.debug("subQueryAppraisalRuleOriginNotExitsInherit query: {}",
                subQueryAppraisalRuleOriginNoRuleScoped.toString());
            subQueryAppraisalRuleOriginNoRul.add(subQueryAppraisalRuleOriginNotExitsScoped);

            subQueryAppraisalRuleOrigin.add(subQueryAppraisalRuleOriginNoRul);

        }
    }

    private void handleWaitingToRecalculateInheritage(AppraisalRuleOriginRuleCriteria appraisalRuleOriginRuleCriteria,
        BooleanQuery subQueryAppraisalRuleOrigin) throws InvalidCreateOperationException {
        if (appraisalRuleOriginRuleCriteria.getWaitingToRecalculate() != null) {
            VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOrigin,
                Boolean.TRUE.equals(appraisalRuleOriginRuleCriteria.getWaitingToRecalculate()) ?
                    ArchiveSearchConsts.CriteriaOperators.EQ : ArchiveSearchConsts.CriteriaOperators.NOT_EQ,
                ArchiveSearchConsts.APPRAISAL_MGT_RULES_FIELDS_MAPPING
                    .get(ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE
                        .name()), List.of(ArchiveSearchConsts.FALSE_CRITERIA_VALUE));
        }
    }


    public AppraisalRuleOriginRuleCriteria extractCriteriaAppraisalBooleanValues(
        List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList) {
        AppraisalRuleOriginRuleCriteria appraisalRuleOriginRuleCriteria = new AppraisalRuleOriginRuleCriteria();

        for (SearchCriteriaEltDto searchCriteria : appraisalMgtRulesCriteriaList) {
            if (searchCriteria.getCriteria()
                .equals(ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE.name()) &&
                !CollectionUtils.isEmpty(searchCriteria.getValues())) {
                appraisalRuleOriginRuleCriteria
                    .setHasAtLeastOneRule(
                        ArchiveSearchConsts.TRUE_CRITERIA_VALUE
                            .equals(searchCriteria.getValues().get(0).getValue()));
            }
            if (searchCriteria.getCriteria().equals(
                ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE.name()) &&
                !CollectionUtils.isEmpty(searchCriteria.getValues())) {
                appraisalRuleOriginRuleCriteria
                    .setWaitingToRecalculate(
                        ArchiveSearchConsts.TRUE_CRITERIA_VALUE
                            .equals(searchCriteria.getValues().get(0).getValue()));

            }
            if (searchCriteria.getCriteria().equals(
                ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name()) &&
                !CollectionUtils.isEmpty(searchCriteria.getValues())) {
                appraisalRuleOriginRuleCriteria
                    .setInheritAtLeastOneRule(
                        ArchiveSearchConsts.TRUE_CRITERIA_VALUE
                            .equals(searchCriteria.getValues().get(0).getValue()));

            }
            if (searchCriteria.getCriteria()
                .equals(ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_NO_ONE.name()) &&
                !CollectionUtils.isEmpty(searchCriteria.getValues())) {
                appraisalRuleOriginRuleCriteria
                    .setHasNoRule(
                        ArchiveSearchConsts.TRUE_CRITERIA_VALUE
                            .equals(searchCriteria.getValues().get(0).getValue()));
            }
        }
        return appraisalRuleOriginRuleCriteria;
    }

    private void buildAppraisalMgtRulesSimpleCriteria(BooleanQuery subQuery,
        List<SearchCriteriaEltDto> appraisalMgtRulesSimpleCriteriaList)
        throws InvalidCreateOperationException {

        BooleanQuery simpleCriteriaByOrigin = or();

        buildAppraisalMgtRulesSimpleCriteriaByOrigin(simpleCriteriaByOrigin, appraisalMgtRulesSimpleCriteriaList,
            ArchiveSearchConsts.AppraisalRuleOrigin.SCOPED);
        buildAppraisalMgtRulesSimpleCriteriaByOrigin(simpleCriteriaByOrigin, appraisalMgtRulesSimpleCriteriaList,
            ArchiveSearchConsts.AppraisalRuleOrigin.INHERITED);
        if (simpleCriteriaByOrigin.isReady()) {
            subQuery.add(simpleCriteriaByOrigin);
        }
    }

    private void addUnitAppraisalFinalActionCriteria(List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList,
        BooleanQuery mainQuery)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList)) {
            List<SearchCriteriaEltDto> appraisalMgtRulesFinalActionCriteria = appraisalMgtRulesCriteriaList.stream()
                .filter(criteria -> ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION_TYPE.equals(criteria.getCriteria()))
                .collect(Collectors.toList());
            BooleanQuery subQueryAppraisalRuleOriginFinalAction = or();
            for (SearchCriteriaEltDto finalActionCriteria : appraisalMgtRulesFinalActionCriteria) {
                for (CriteriaValue value : finalActionCriteria.getValues()) {
                    String mappedValue =
                        ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_TYPE_VALUES_MAPPING.get(value.getValue());
                    if (mappedValue != null) {
                        VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginFinalAction,
                            ArchiveSearchConsts.CriteriaOperators.EQ,
                            ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_MAPPING
                                .get(ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION),
                            List.of(mappedValue));
                        VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginFinalAction,
                            ArchiveSearchConsts.CriteriaOperators.IN,
                            ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_MAPPING
                                .get(ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION),
                            List.of(mappedValue));
                    }
                }
            }
            if (subQueryAppraisalRuleOriginFinalAction.isReady()) {
                mainQuery.add(subQueryAppraisalRuleOriginFinalAction);
            }
        }
    }

    private void addUnitAppraisalFinalActionOriginCriteria(
        List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList, BooleanQuery query)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList)) {
            List<SearchCriteriaEltDto> appraisalMgtRulesFinalActionCriteria = appraisalMgtRulesCriteriaList.stream()
                .filter(criteria -> ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION.equals(criteria.getCriteria()))
                .collect(Collectors.toList());
            BooleanQuery subQueryAppraisalRuleOriginFinalAction = or();
            for (SearchCriteriaEltDto finalActionCriteria : appraisalMgtRulesFinalActionCriteria) {
                for (CriteriaValue value : finalActionCriteria.getValues()) {
                    VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginFinalAction,
                        ArchiveSearchConsts.CriteriaOperators.EXISTS,
                        ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_MAPPING.get(value.getValue()), List.of());
                }
            }
            if (subQueryAppraisalRuleOriginFinalAction.isReady()) {
                query.add(subQueryAppraisalRuleOriginFinalAction);
            }
        }
    }

    private void buildAppraisalMgtRulesSimpleCriteriaByOrigin(BooleanQuery appraisalMgtRulesSubQuery,
        List<SearchCriteriaEltDto> appraisalMgtRulesSimpleCriteriaList, ArchiveSearchConsts.AppraisalRuleOrigin origin)
        throws InvalidCreateOperationException {

        Optional<SearchCriteriaEltDto> appraisalIdentifierCriteria =
            appraisalMgtRulesSimpleCriteriaList.stream().filter(
                searchCriteriaEltDto -> ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER
                    .equals(searchCriteriaEltDto.getCriteria())).findAny();
        Optional<SearchCriteriaEltDto> appraisalEndDatesCriteria = appraisalMgtRulesSimpleCriteriaList.stream().filter(
            searchCriteriaEltDto -> ArchiveSearchConsts.APPRAISAL_RULE_END_DATE
                .equals(searchCriteriaEltDto.getCriteria())).findAny();
        String ruleIdVitamFieldName =
            ArchiveSearchConsts.INHERITED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING
                .get(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        String endDtVitamFieldName =
            ArchiveSearchConsts.INHERITED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING
                .get(ArchiveSearchConsts.APPRAISAL_RULE_END_DATE);
        String ruleIdVitamFieldNameScoped = ArchiveSearchConsts.SCOPED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING
            .get(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        String endDtVitamFieldNameScoped =
            ArchiveSearchConsts.SCOPED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING
                .get(ArchiveSearchConsts.APPRAISAL_RULE_END_DATE);

        if (ArchiveSearchConsts.AppraisalRuleOrigin.SCOPED.equals(origin)) {
            if (appraisalIdentifierCriteria.isPresent()) {
                BooleanQuery identifierQuery = or();
                for (CriteriaValue valueIdentifier : appraisalIdentifierCriteria.get().getValues()) {
                    VitamQueryHelper.addParameterCriteria(identifierQuery, ArchiveSearchConsts.CriteriaOperators.EQ,
                        ruleIdVitamFieldNameScoped, List.of(valueIdentifier.getValue()));
                }
                if (identifierQuery.isReady()) {
                    appraisalMgtRulesSubQuery.add(identifierQuery);
                }
            }
            if (appraisalEndDatesCriteria.isPresent()) {
                BooleanQuery appraisalEndQuery = or();
                for (CriteriaValue valueEndDate : appraisalEndDatesCriteria.get().getValues()) {
                    BooleanQuery intervalQueryByInterval = and();
                    String beginDtStr = valueEndDate.getBeginInterval();
                    String endDtStr = valueEndDate.getEndInterval();

                    if (!ObjectUtils.isEmpty(beginDtStr)) {
                        LocalDateTime beginDt =
                            LocalDateTime.parse(beginDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
                        VitamQueryHelper
                            .addParameterCriteria(intervalQueryByInterval, ArchiveSearchConsts.CriteriaOperators.GTE,
                                endDtVitamFieldNameScoped,
                                List.of(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(beginDt)));
                    }

                    if (!ObjectUtils.isEmpty(endDtStr)) {
                        LocalDateTime endDt =
                            LocalDateTime.parse(endDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
                        VitamQueryHelper.addParameterCriteria(intervalQueryByInterval,
                            ArchiveSearchConsts.CriteriaOperators.LTE, endDtVitamFieldNameScoped,
                            List.of(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(endDt)));
                    }
                    if (intervalQueryByInterval.isReady()) {
                        appraisalEndQuery.add(intervalQueryByInterval);
                    }
                }
                if (appraisalEndQuery.isReady()) {
                    appraisalMgtRulesSubQuery.add(appraisalEndQuery);
                }
            }
        } else if (ArchiveSearchConsts.AppraisalRuleOrigin.INHERITED.equals(origin)) {
            if (appraisalIdentifierCriteria.isPresent()) {
                BooleanQuery identifierQuery = or();
                for (CriteriaValue valueIdentifier : appraisalIdentifierCriteria.get().getValues()) {
                    VitamQueryHelper.addParameterCriteria(identifierQuery, ArchiveSearchConsts.CriteriaOperators.EQ,
                        ruleIdVitamFieldName, List.of(valueIdentifier.getValue()));
                }
                if (identifierQuery.isReady()) {
                    appraisalMgtRulesSubQuery.add(identifierQuery);
                }
            }
            if (appraisalEndDatesCriteria.isPresent()) {
                BooleanQuery appraisalEndQuery = or();
                for (CriteriaValue valueEndDate : appraisalEndDatesCriteria.get().getValues()) {
                    BooleanQuery intervalQueryByInterval = and();
                    String beginDtStr = valueEndDate.getBeginInterval();
                    String endDtStr = valueEndDate.getEndInterval();

                    if (!ObjectUtils.isEmpty(beginDtStr)) {
                        LocalDateTime beginDt =
                            LocalDateTime.parse(beginDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
                        VitamQueryHelper
                            .addParameterCriteria(intervalQueryByInterval, ArchiveSearchConsts.CriteriaOperators.GTE,
                                endDtVitamFieldName,
                                List.of(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(beginDt)));
                    }

                    if (!ObjectUtils.isEmpty(endDtStr)) {
                        LocalDateTime endDt =
                            LocalDateTime.parse(endDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
                        VitamQueryHelper.addParameterCriteria(intervalQueryByInterval,
                            ArchiveSearchConsts.CriteriaOperators.LTE, endDtVitamFieldName,
                            List.of(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(endDt)));
                    }
                    if (intervalQueryByInterval.isReady()) {
                        appraisalEndQuery.add(intervalQueryByInterval);
                    }
                }
                if (appraisalEndQuery.isReady()) {
                    appraisalMgtRulesSubQuery.add(appraisalEndQuery);
                }
            }
        }

    }
}

