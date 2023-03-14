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
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

/**
 * Service to build DSL Query for simple fields criteria for extracting archive units
 */
@Service
public class ArchivesSearchFieldsQueryBuilderService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchFieldsQueryBuilderService.class);

    public void fillQueryFromCriteriaList(BooleanQuery queryToFill, List<SearchCriteriaEltDto> criteriaList)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(criteriaList)) {
            String mappedCriteriaName;
            for (SearchCriteriaEltDto searchCriteria : criteriaList) {
                if (searchCriteria.getCriteria() == null) {
                    LOGGER.error("Field not mapped correctly  " + searchCriteria.getCriteria());
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

                    case ArchiveSearchConsts.DESCRIPTION_LEVEL_CRITERIA:
                        queryToFill.add(buildArchiveUnitDescriptionLevelQuery(
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList()),
                            ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                        break;
                    case ArchiveSearchConsts.WAITING_RECALCULATE:
                        Optional<String> validInheritedRulesValueOpt =
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).findAny();
                        if (validInheritedRulesValueOpt.isPresent()) {
                            if (validInheritedRulesValueOpt.get().equals(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)) {
                                VitamQueryHelper.addParameterCriteria(queryToFill,
                                    ArchiveSearchConsts.CriteriaOperators.NOT_EQ,
                                    ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING
                                        .get(ArchiveSearchConsts.RULES_COMPUTED),
                                    List.of(ArchiveSearchConsts.TRUE_CRITERIA_VALUE));
                            } else {
                                VitamQueryHelper.addParameterCriteria(queryToFill,
                                    ArchiveSearchConsts.CriteriaOperators.EQ,
                                    ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING
                                        .get(ArchiveSearchConsts.RULES_COMPUTED),
                                    List.of(ArchiveSearchConsts.TRUE_CRITERIA_VALUE));
                            }
                        }
                        break;
                    case ArchiveSearchConsts.RULES_COMPUTED:
                        Optional<String> computedValidInheritedRulesValueOpt =
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).findAny();
                        if (computedValidInheritedRulesValueOpt.isPresent()) {
                            if (computedValidInheritedRulesValueOpt.get()
                                .equals(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)) {
                                VitamQueryHelper.addParameterCriteria(queryToFill,
                                    ArchiveSearchConsts.CriteriaOperators.EQ,
                                    ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING
                                        .get(ArchiveSearchConsts.RULES_COMPUTED),
                                    List.of(ArchiveSearchConsts.TRUE_CRITERIA_VALUE));
                            } else {
                                VitamQueryHelper.addParameterCriteria(queryToFill,
                                    ArchiveSearchConsts.CriteriaOperators.NOT_EQ,
                                    ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING
                                        .get(ArchiveSearchConsts.RULES_COMPUTED),
                                    List.of(ArchiveSearchConsts.TRUE_CRITERIA_VALUE));
                            }
                        }
                        break;
                    default:
                        mappedCriteriaName =
                            ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING.containsKey(searchCriteria.getCriteria()) ?
                                ArchiveSearchConsts.SIMPLE_FIELDS_VALUES_MAPPING.get(searchCriteria.getCriteria()) :
                                searchCriteria.getCriteria();

                        VitamQueryHelper.addParameterCriteria(queryToFill,
                            ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator()),
                            mappedCriteriaName,
                            searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList()));
                        break;
                }
                if (ArchiveSearchConsts.CriteriaDataType.DATE.name().equals(searchCriteria.getDataType())) {
                    queryToFill.add(buildStartDateEndDateQuery(searchCriteria.getCriteria(),
                        searchCriteria.getValues().stream().map(CriteriaValue::getValue).collect(
                            Collectors.toList()),
                        ArchiveSearchConsts.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                }
            }
        }
    }

    private Query buildTitleAndDescriptionQuery(final List<String> searchValues,
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

    private Query buildTitleQuery(final List<String> searchValues,
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

    private Query buildDescriptionQuery(final List<String> searchValues,
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

    private Query buildEliminationAnalysisSearchQuery(final List<String> searchValues,
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

    private Query buildStartDateEndDateQuery(String searchCriteria, final List<String> searchValues,
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
        LOGGER.info("The search criteria Date is {} ", criteria);
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                LocalDateTime searchDate =
                    LocalDateTime.parse(value, ArchiveSearchConsts.ISO_FRENCH_FORMATER).withHour(0)
                        .withMinute(0).withSecond(0).withNano(0);
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(criteria,
                        ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(searchDate.plusDays(1)), operator));
            }
            subQueryAnd.add(subQueryOr);
        }
        return subQueryAnd;
    }

    private Query buildArchiveUnitTypeQuery(final List<String> searchValues)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        BooleanQuery subQueryOr = or();
        LOGGER.debug("The search criteria is on the unit type");
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                switch (value) {
                    case "ARCHIVE_UNIT_FILING_UNIT":
                        subQueryOr
                            .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES,
                                ArchiveSearchConsts.ARCHIVE_UNIT_FILING_UNIT,
                                ArchiveSearchConsts.CriteriaOperators.EQ));
                        break;
                    case "ARCHIVE_UNIT_HOLDING_UNIT":
                        subQueryOr
                            .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES,
                                ArchiveSearchConsts.ARCHIVE_UNIT_HOLDING_UNIT,
                                ArchiveSearchConsts.CriteriaOperators.EQ));
                        break;
                    case "ARCHIVE_UNIT_WITH_OBJECTS":
                        BooleanQuery subQueryAndForIngestWithObject = and();

                        subQueryAndForIngestWithObject
                            .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES,
                                ArchiveSearchConsts.ARCHIVE_UNIT_INGEST, ArchiveSearchConsts.CriteriaOperators.EQ));
                        subQueryAndForIngestWithObject
                            .add(VitamQueryHelper
                                .buildSubQueryByOperator(ArchiveSearchConsts.ARCHIVE_UNIT_OBJECTS, value,
                                    ArchiveSearchConsts.CriteriaOperators.EXISTS));
                        subQueryOr.add(subQueryAndForIngestWithObject);
                        break;
                    case "ARCHIVE_UNIT_WITHOUT_OBJECTS":
                        BooleanQuery subQueryAndForIngestWithoutObjects = and();

                        subQueryAndForIngestWithoutObjects
                            .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES,
                                ArchiveSearchConsts.ARCHIVE_UNIT_INGEST, ArchiveSearchConsts.CriteriaOperators.EQ));
                        subQueryAndForIngestWithoutObjects
                            .add(VitamQueryHelper
                                .buildSubQueryByOperator(ArchiveSearchConsts.ARCHIVE_UNIT_OBJECTS, value,
                                    ArchiveSearchConsts.CriteriaOperators.MISSING));
                        subQueryOr.add(subQueryAndForIngestWithoutObjects);
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

    private Query buildArchiveUnitDescriptionLevelQuery(final List<String> searchValues,
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

}

