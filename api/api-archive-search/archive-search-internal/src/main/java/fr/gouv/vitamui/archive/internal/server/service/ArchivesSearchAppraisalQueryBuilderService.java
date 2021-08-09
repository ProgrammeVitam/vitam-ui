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
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.archives.search.common.common.AppraisalRuleOriginRuleCriteria;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConst;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
            criteriaList = preProcessAppraisalRulesSimpleCriteria(criteriaList);
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

            if (appraisalRuleOriginRuleCriteria.getWaitingToRecalculate() != null) {
                VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOrigin,
                    appraisalRuleOriginRuleCriteria.getWaitingToRecalculate() ?
                        ArchiveSearchConst.CriteriaOperators.EQ : ArchiveSearchConst.CriteriaOperators.NOT_EQ,
                    ArchiveSearchConst.APPRAISAL_MGT_RULES_FIELDS_MAPPING
                        .get(ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE
                            .name()), List.of(ArchiveSearchConst.FALSE_CRITERIA_VALUE));
            }
            if (appraisalRuleOriginRuleCriteria.getHasNoNoRule() != null &&
                appraisalRuleOriginRuleCriteria.getHasNoNoRule()) {

                BooleanQuery subQueryAppraisalRuleOriginNoRul = and();
                BooleanQuery subQueryAppraisalRuleOriginNotExitsInherit = not();
                BooleanQuery subQueryAppraisalRuleOriginNoRuleInherit = and();
                VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginNoRuleInherit,
                    ArchiveSearchConst.CriteriaOperators.EXISTS,
                    ArchiveSearchConst.APPRAISAL_RULE_ORIGIN_INHERITED_FIELD, List.of());

                buildAppraisalMgtRulesSimpleCriteriaByOrigin(subQueryAppraisalRuleOriginNoRuleInherit,
                    appraisalMgtRulesCriteriaList, ArchiveSearchConst.AppraisalRuleOrigin.INHERITED);
                subQueryAppraisalRuleOriginNotExitsInherit.add(subQueryAppraisalRuleOriginNoRuleInherit);
                LOGGER.debug("subQueryAppraisalRuleOriginNotExitsInherit query: {}",
                    subQueryAppraisalRuleOriginNotExitsInherit.toString());
                subQueryAppraisalRuleOriginNoRul.add(subQueryAppraisalRuleOriginNotExitsInherit);

                BooleanQuery subQueryAppraisalRuleOriginNotExitsScoped = not();
                BooleanQuery subQueryAppraisalRuleOriginNoRuleScoped = and();

                VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginNoRuleScoped,
                    ArchiveSearchConst.CriteriaOperators.EXISTS,
                    ArchiveSearchConst.APPRAISAL_RULE_ORIGIN_SCOPED_FIELD, List.of());

                buildAppraisalMgtRulesSimpleCriteriaByOrigin(subQueryAppraisalRuleOriginNoRuleScoped,
                    appraisalMgtRulesCriteriaList,
                    ArchiveSearchConst.AppraisalRuleOrigin.SCOPED);
                subQueryAppraisalRuleOriginNotExitsScoped.add(subQueryAppraisalRuleOriginNoRuleScoped);
                LOGGER.debug("subQueryAppraisalRuleOriginNotExitsInherit query: {}",
                    subQueryAppraisalRuleOriginNoRuleScoped.toString());
                subQueryAppraisalRuleOriginNoRul.add(subQueryAppraisalRuleOriginNotExitsScoped);

                subQueryAppraisalRuleOrigin.add(subQueryAppraisalRuleOriginNoRul);

            }
            if (appraisalRuleOriginRuleCriteria.getInheritAtLeastOneRule() != null) {
                if (appraisalRuleOriginRuleCriteria.getInheritAtLeastOneRule()) {
                    if (appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule() != null &&
                        appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule()) {

                        BooleanQuery appraisalMgtRulesSubQuery = and();

                        VitamQueryHelper.addParameterCriteria(appraisalMgtRulesSubQuery,
                            ArchiveSearchConst.CriteriaOperators.EXISTS,
                            ArchiveSearchConst.APPRAISAL_RULE_ORIGIN_INHERITED_FIELD, List.of());

                        buildAppraisalMgtRulesSimpleCriteriaByOrigin(appraisalMgtRulesSubQuery,
                            appraisalMgtRulesCriteriaList, ArchiveSearchConst.AppraisalRuleOrigin.INHERITED);
                        subQueryAppraisalRuleOrigin.add(appraisalMgtRulesSubQuery);

                        BooleanQuery appraisalMgtRulesSubQueryInh = and();
                        VitamQueryHelper.addParameterCriteria(appraisalMgtRulesSubQueryInh,
                            ArchiveSearchConst.CriteriaOperators.EXISTS,
                            ArchiveSearchConst.APPRAISAL_RULE_ORIGIN_SCOPED_FIELD, List.of());
                        buildAppraisalMgtRulesSimpleCriteriaByOrigin(appraisalMgtRulesSubQueryInh,
                            appraisalMgtRulesCriteriaList, ArchiveSearchConst.AppraisalRuleOrigin.SCOPED);
                        subQueryAppraisalRuleOrigin.add(appraisalMgtRulesSubQueryInh);

                        LOGGER.debug("subQueryAppraisalRuleOrigin query: {}", subQueryAppraisalRuleOrigin.toString());
                    } else {
                        BooleanQuery subQueryAppraisalRuleOriginRul = and();
                        BooleanQuery subQueryAppraisalRuleOriginExitsInherit = and();
                        VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginExitsInherit,
                            ArchiveSearchConst.CriteriaOperators.EXISTS,
                            ArchiveSearchConst.APPRAISAL_RULE_ORIGIN_INHERITED_FIELD, List.of());

                        buildAppraisalMgtRulesSimpleCriteriaByOrigin(subQueryAppraisalRuleOriginExitsInherit,
                            appraisalMgtRulesCriteriaList, ArchiveSearchConst.AppraisalRuleOrigin.INHERITED);
                        LOGGER.debug("subQueryAppraisalRuleOriginExitsInherit query: {}",
                            subQueryAppraisalRuleOriginExitsInherit.toString());
                        subQueryAppraisalRuleOriginRul.add(subQueryAppraisalRuleOriginExitsInherit);

                        BooleanQuery subQueryAppraisalRuleOriginNotExitsScoped = not();
                        BooleanQuery subQueryAppraisalRuleOriginNoRuleScoped = and();

                        VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginNoRuleScoped,
                            ArchiveSearchConst.CriteriaOperators.EXISTS,
                            ArchiveSearchConst.APPRAISAL_RULE_ORIGIN_SCOPED_FIELD, List.of());

                        buildAppraisalMgtRulesSimpleCriteriaByOrigin(subQueryAppraisalRuleOriginNoRuleScoped,
                            appraisalMgtRulesCriteriaList,
                            ArchiveSearchConst.AppraisalRuleOrigin.SCOPED);
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
                            ArchiveSearchConst.CriteriaOperators.EXISTS,
                            ArchiveSearchConst.APPRAISAL_RULE_ORIGIN_SCOPED_FIELD, List.of());
                        buildAppraisalMgtRulesSimpleCriteriaByOrigin(appraisalMgtRulesSubQuery,
                            appraisalMgtRulesCriteriaList,
                            ArchiveSearchConst.AppraisalRuleOrigin.SCOPED);
                        subQueryAppraisalRuleOrigin.add(appraisalMgtRulesSubQuery);
                    }
                }
            } else {
                if (appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule() != null) {
                    BooleanQuery appraisalMgtRulesSubQuery = and();

                    VitamQueryHelper.addParameterCriteria(appraisalMgtRulesSubQuery,
                        appraisalRuleOriginRuleCriteria.getHasAtLeastOneRule() ?
                            ArchiveSearchConst.CriteriaOperators.EXISTS : ArchiveSearchConst.CriteriaOperators.MISSING,
                        ArchiveSearchConst.APPRAISAL_MGT_RULES_FIELDS_MAPPING.get(
                            ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE.name()),
                        List.of());

                    buildAppraisalMgtRulesSimpleCriteriaByOrigin(appraisalMgtRulesSubQuery,
                        appraisalMgtRulesCriteriaList,
                        ArchiveSearchConst.AppraisalRuleOrigin.SCOPED);
                    subQueryAppraisalRuleOrigin.add(appraisalMgtRulesSubQuery);
                }
            }
            if (subQueryAppraisalRuleOrigin.isReady()) {
                mainQuery.add(subQueryAppraisalRuleOrigin);
            }
        }

    }


    public AppraisalRuleOriginRuleCriteria extractCriteriaAppraisalBooleanValues(
        List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList) {
        AppraisalRuleOriginRuleCriteria appraisalRuleOriginRuleCriteria = new AppraisalRuleOriginRuleCriteria();

        for (SearchCriteriaEltDto searchCriteria : appraisalMgtRulesCriteriaList) {
            if (searchCriteria.getCriteria()
                .equals(ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE.name())) {
                if (!CollectionUtils.isEmpty(searchCriteria.getValues())) {
                    appraisalRuleOriginRuleCriteria
                        .setHasAtLeastOneRule(
                            ArchiveSearchConst.TRUE_CRITERIA_VALUE.equals(searchCriteria.getValues().get(0)));
                }
            }
            if (searchCriteria.getCriteria().equals(
                ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE.name())) {
                if (!CollectionUtils.isEmpty(searchCriteria.getValues())) {
                    appraisalRuleOriginRuleCriteria
                        .setWaitingToRecalculate(
                            ArchiveSearchConst.TRUE_CRITERIA_VALUE.equals(searchCriteria.getValues().get(0)));
                }
            }
            if (searchCriteria.getCriteria().equals(
                ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name())) {
                if (!CollectionUtils.isEmpty(searchCriteria.getValues())) {
                    appraisalRuleOriginRuleCriteria
                        .setInheritAtLeastOneRule(
                            ArchiveSearchConst.TRUE_CRITERIA_VALUE.equals(searchCriteria.getValues().get(0)));
                }
            }
            if (searchCriteria.getCriteria()
                .equals(ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_NO_ONE.name())) {
                if (!CollectionUtils.isEmpty(searchCriteria.getValues())) {
                    appraisalRuleOriginRuleCriteria
                        .setHasNoNoRule(
                            ArchiveSearchConst.TRUE_CRITERIA_VALUE.equals(searchCriteria.getValues().get(0)));
                }
            }
        }
        return appraisalRuleOriginRuleCriteria;
    }

    private void buildAppraisalMgtRulesSimpleCriteria(BooleanQuery subQuery,
        List<SearchCriteriaEltDto> appraisalMgtRulesSimpleCriteriaList)
        throws InvalidCreateOperationException {

        BooleanQuery simpleCriteriaByOrigin = or();

        buildAppraisalMgtRulesSimpleCriteriaByOrigin(simpleCriteriaByOrigin, appraisalMgtRulesSimpleCriteriaList,
            ArchiveSearchConst.AppraisalRuleOrigin.SCOPED);
        buildAppraisalMgtRulesSimpleCriteriaByOrigin(simpleCriteriaByOrigin, appraisalMgtRulesSimpleCriteriaList,
            ArchiveSearchConst.AppraisalRuleOrigin.INHERITED);
        if (simpleCriteriaByOrigin.isReady()) {
            subQuery.add(simpleCriteriaByOrigin);
        }
    }

    private List<SearchCriteriaEltDto> preProcessAppraisalRulesSimpleCriteria(
        List<SearchCriteriaEltDto> appraisalMgtRulesSimpleCriteriaList) {

        Optional<SearchCriteriaEltDto> appraisalMgtRulesBeginDateOpt = appraisalMgtRulesSimpleCriteriaList.stream()
            .filter(criteria -> criteria.getCriteria().equals(ArchiveSearchConst.APPRAISAL_RULE_END_DATE)).findFirst();
        if (appraisalMgtRulesBeginDateOpt.isPresent()) {
            Optional<SearchCriteriaEltDto> appraisalMgtRulesEndDateOpt = appraisalMgtRulesSimpleCriteriaList.stream()
                .filter(criteria -> criteria.getCriteria().equals(ArchiveSearchConst.APPRAISAL_RULE_END_DATE_END))
                .findFirst();
            SearchCriteriaEltDto appraisalMgtRulesBeginDate = appraisalMgtRulesBeginDateOpt.get();
            if (appraisalMgtRulesEndDateOpt.isPresent()) {//Interval
                appraisalMgtRulesSimpleCriteriaList = appraisalMgtRulesSimpleCriteriaList.stream().map(criteria -> {
                    if (criteria.getCriteria().equals(ArchiveSearchConst.APPRAISAL_RULE_END_DATE)) {
                        criteria.setOperator(ArchiveSearchConst.CriteriaOperators.GTE.name());
                    }
                    return criteria;
                }).collect(Collectors.toList());
            } else {//Equal date
                DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(ArchiveSearchConst.ISO_DATE_FORMAT, Locale.FRENCH);
                LocalDateTime beginDate =
                    LocalDateTime.parse(appraisalMgtRulesBeginDate.getValues().get(0), formatter).withHour(0)
                        .withMinute(0).withSecond(0);

                LocalDateTime endDate = beginDate.plusDays(1).minusSeconds(1);
                appraisalMgtRulesSimpleCriteriaList = appraisalMgtRulesSimpleCriteriaList.stream().filter(
                    criteria -> !criteria.getCriteria().equals(ArchiveSearchConst.APPRAISAL_RULE_END_DATE_END))
                    .map(criteria -> {
                        if (criteria.getCriteria().equals(ArchiveSearchConst.APPRAISAL_RULE_END_DATE)) {
                            criteria.setOperator(ArchiveSearchConst.CriteriaOperators.GTE.name());
                            criteria.setValues(List.of(formatter.format(beginDate)));
                        }
                        return criteria;
                    }).collect(Collectors.toList());
                SearchCriteriaEltDto appraisalMgtRulesEndDate = new SearchCriteriaEltDto();
                appraisalMgtRulesEndDate.setCriteria(ArchiveSearchConst.APPRAISAL_RULE_END_DATE);
                appraisalMgtRulesEndDate.setOperator(ArchiveSearchConst.CriteriaOperators.LTE.name());
                appraisalMgtRulesEndDate.setValues(List.of(formatter.format(endDate)));
                appraisalMgtRulesSimpleCriteriaList.add(appraisalMgtRulesEndDate);
            }
        }
        return appraisalMgtRulesSimpleCriteriaList;
    }

    private void addUnitAppraisalMgtRulesCriteria(List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList,
        BooleanQuery mainQuery) throws InvalidParseOperationException, InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList)) {
            appraisalMgtRulesCriteriaList = preProcessAppraisalRulesSimpleCriteria(appraisalMgtRulesCriteriaList);
            buildAppraisalMgtRulesSimpleCriteria(mainQuery, appraisalMgtRulesCriteriaList);
            AppraisalRuleOriginRuleCriteria appraisalRuleOriginRuleCriteria =
                extractCriteriaAppraisalBooleanValues(appraisalMgtRulesCriteriaList);
            if (appraisalRuleOriginRuleCriteria.containsOriginRule()) {
                addUnitAppraisalMgtRulesOriginCriteria(appraisalMgtRulesCriteriaList, mainQuery,
                    appraisalRuleOriginRuleCriteria);
            }
            addUnitAppraisalFinalActionOriginCriteria(appraisalMgtRulesCriteriaList, mainQuery);
            addUnitAppraisalFinalActionCriteria(appraisalMgtRulesCriteriaList, mainQuery);

        }
    }

    private void addUnitAppraisalFinalActionCriteria(List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList,
        BooleanQuery mainQuery)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList)) {
            List<SearchCriteriaEltDto> appraisalMgtRulesFinalActionCriteria = appraisalMgtRulesCriteriaList.stream()
                .filter(criteria -> ArchiveSearchConst.APPRAISAL_RULE_FINAL_ACTION_TYPE.equals(criteria.getCriteria()))
                .collect(Collectors.toList());
            BooleanQuery subQueryAppraisalRuleOriginFinalAction = or();
            for (SearchCriteriaEltDto finalActionCriteria : appraisalMgtRulesFinalActionCriteria) {
                for (String value : finalActionCriteria.getValues()) {
                    String mappedValue =
                        ArchiveSearchConst.APPRAISAL_MGT_RULES_FINAL_ACTION_TYPE_VALUES_MAPPING.get(value);
                    if (mappedValue != null) {
                        VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginFinalAction,
                            ArchiveSearchConst.CriteriaOperators.EQ,
                            "#management.AppraisalRule.FinalAction", List.of(mappedValue));
                        VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginFinalAction,
                            ArchiveSearchConst.CriteriaOperators.IN,
                            "#computedInheritedRules.AppraisalRule.FinalAction", List.of(mappedValue));
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
                .filter(criteria -> ArchiveSearchConst.APPRAISAL_RULE_FINAL_ACTION.equals(criteria.getCriteria()))
                .collect(Collectors.toList());
            BooleanQuery subQueryAppraisalRuleOriginFinalAction = or();
            for (SearchCriteriaEltDto finalActionCriteria : appraisalMgtRulesFinalActionCriteria) {
                for (String value : finalActionCriteria.getValues()) {
                    VitamQueryHelper.addParameterCriteria(subQueryAppraisalRuleOriginFinalAction,
                        ArchiveSearchConst.CriteriaOperators.EXISTS,
                        ArchiveSearchConst.APPRAISAL_MGT_RULES_FINAL_ACTION_MAPPING.get(value), List.of());
                }
            }
            if (subQueryAppraisalRuleOriginFinalAction.isReady()) {
                query.add(subQueryAppraisalRuleOriginFinalAction);
            }
        }
    }

    private void buildAppraisalMgtRulesSimpleCriteriaByOrigin(BooleanQuery appraisalMgtRulesSubQuery,
        List<SearchCriteriaEltDto> appraisalMgtRulesSimpleCriteriaList, ArchiveSearchConst.AppraisalRuleOrigin origin)
        throws InvalidCreateOperationException {

        for (SearchCriteriaEltDto searchCriteria : appraisalMgtRulesSimpleCriteriaList) {
            String vitamFieldName;
            if (ArchiveSearchConst.AppraisalRuleOrigin.SCOPED.equals(origin)) {
                vitamFieldName = ArchiveSearchConst.SCOPED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING
                    .get(searchCriteria.getCriteria());
            } else {
                vitamFieldName = ArchiveSearchConst.INHERITED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING
                    .get(searchCriteria.getCriteria());
            }
            if (vitamFieldName != null) {
                if (searchCriteria.getCriteria().equals(ArchiveSearchConst.APPRAISAL_RULE_IDENTIFIER) &&
                    ArchiveSearchConst.AppraisalRuleOrigin.INHERITED.equals(origin)) {
                    for (String value : searchCriteria.getValues()) {
                        if (value != null) {
                            VitamQueryHelper.addParameterCriteria(appraisalMgtRulesSubQuery,
                                ArchiveSearchConst.CriteriaOperators.EXISTS,
                                vitamFieldName + "." + value, List.of());
                        }
                    }
                } else {
                    VitamQueryHelper.addParameterCriteria(appraisalMgtRulesSubQuery,
                        ArchiveSearchConst.CriteriaOperators.valueOf(searchCriteria.getOperator()),
                        vitamFieldName, searchCriteria.getValues());
                }
            }
        }
    }
}

