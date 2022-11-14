/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
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
import fr.gouv.vitamui.archives.search.common.common.MgtRuleOriginRuleCriteria;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

/**
 * * Service to build DSL Query for management rules criteria for extracting archive units
 */
@Service
public class ArchivesSearchManagementRulesQueryBuilderService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchManagementRulesQueryBuilderService.class);
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



    public void handleSingleOriginCriteria(String ruleCategory, List<SearchCriteriaEltDto> identifiersCriteria,
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

    public void buildQueryForHasAtLeastRulesCriteria(String ruleCategory, BooleanQuery mainQuery,
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

    public void buildQueryForInheritRulesCriteria(String ruleCategory, BooleanQuery mainQuery,
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

    private void buildQueryForInheritOrLocalRulesCriteria(String ruleCategory, BooleanQuery mainQuery)
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

    public void buildQueryForNoRulesCriteria(String ruleCategory, BooleanQuery mainQuery,
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

    private void handleWaitingToComputeInheritance(BooleanQuery mgtRuleOriginQuery)
        throws InvalidCreateOperationException {
        VitamQueryHelper.addParameterCriteria(mgtRuleOriginQuery,
            ArchiveSearchConsts.CriteriaOperators.EQ, WAITING_TO_COMPUTE_RULES_STATUS,
            List.of(ArchiveSearchConsts.FALSE_CRITERIA_VALUE));
    }



    public MgtRuleOriginRuleCriteria extractMgtRulesCriteriaBooleanValues(
        List<SearchCriteriaEltDto> mgtRulesCriteriaList) {
        MgtRuleOriginRuleCriteria mgtRuleOriginRuleCriteria = new MgtRuleOriginRuleCriteria();

        for (SearchCriteriaEltDto searchCriteria : mgtRulesCriteriaList) {

            if (searchCriteria.getCriteria()
                .equals(ArchiveSearchConsts.RuleOriginValues.ORIGIN_LOCAL_OR_INHERIT_RULES.name()) &&
                !CollectionUtils.isEmpty(searchCriteria.getValues())) {
                mgtRuleOriginRuleCriteria.setHasLocalOrInheritedRule(
                    ArchiveSearchConsts.TRUE_CRITERIA_VALUE.equals(searchCriteria.getValues().get(0).getValue()));
            }


            if (searchCriteria.getCriteria()
                .equals(ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_AT_LEAST_ONE.name()) &&
                !CollectionUtils.isEmpty(searchCriteria.getValues())) {
                mgtRuleOriginRuleCriteria.setHasAtLeastOneRule(
                    ArchiveSearchConsts.TRUE_CRITERIA_VALUE.equals(searchCriteria.getValues().get(0).getValue()));
            }

            if (searchCriteria.getCriteria().equals(
                ArchiveSearchConsts.RuleOriginValues.ORIGIN_INHERITE_AT_LEAST_ONE.name()) &&
                !CollectionUtils.isEmpty(searchCriteria.getValues())) {
                mgtRuleOriginRuleCriteria
                    .setInheritAtLeastOneRule(
                        ArchiveSearchConsts.TRUE_CRITERIA_VALUE
                            .equals(searchCriteria.getValues().get(0).getValue()));
            }
            if (searchCriteria.getCriteria()
                .equals(ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_NO_ONE.name()) &&
                !CollectionUtils.isEmpty(searchCriteria.getValues())) {
                mgtRuleOriginRuleCriteria
                    .setHasNoRule(
                        ArchiveSearchConsts.TRUE_CRITERIA_VALUE
                            .equals(searchCriteria.getValues().get(0).getValue()));
            }
        }
        return mgtRuleOriginRuleCriteria;
    }

    private void buildMgtRulesEndDatesCriteria(BooleanQuery mgtRulesSubQuery,
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
}

