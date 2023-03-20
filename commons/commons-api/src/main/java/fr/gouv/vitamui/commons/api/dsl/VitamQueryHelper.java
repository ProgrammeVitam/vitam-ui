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
package fr.gouv.vitamui.commons.api.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.QueryProjection;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.exists;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.gt;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.gte;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.lt;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.lte;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.match;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.matchPhrasePrefix;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.missing;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.ne;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

public class VitamQueryHelper {
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamQueryHelper.class);

    public static void addParameterCriteria(BooleanQuery query,
        ArchiveSearchConsts.CriteriaOperators operator,
        String searchKey,
        final List<String> searchValues
    ) throws InvalidCreateOperationException {
        if (StringUtils.isBlank(searchKey)) {
            throw new InvalidCreateOperationException("searchKey is empty or null ");
        }
        if (CollectionUtils.isEmpty(searchValues)) {
            //the case of empty list
            query.add(buildSubQueryByOperator(searchKey, null, operator));
        } else if (searchValues.size() > 1) {
            BooleanQuery subQueryOr = or();
            BooleanQuery subQueryAnd = and();
            //The case of multiple values
            if (operator == ArchiveSearchConsts.CriteriaOperators.NOT_EQ) {
                for (String value : searchValues) {
                    subQueryAnd.add(buildSubQueryByOperator(searchKey, value, operator));
                }
                query.add(subQueryAnd);
            } else {
                for (String value : searchValues) {
                    subQueryOr.add(buildSubQueryByOperator(searchKey, value, operator));
                }
                query.add(subQueryOr);
            }
        } else if (searchValues.size() == 1) {
            //the case of one value
            query.add(buildSubQueryByOperator(searchKey, searchValues.stream().findAny().get(), operator));
        }
    }

    public static Query buildSubQueryByOperator(String searchKey, String value,
        ArchiveSearchConsts.CriteriaOperators operator)
        throws InvalidCreateOperationException {
        Query criteriaSubQuery;
        switch (operator) {
            case MATCH:
                criteriaSubQuery = match(searchKey, value);
                break;
            case GTE:
                criteriaSubQuery = gte(searchKey, value);
                break;
            case GT:
                criteriaSubQuery = gt(searchKey, value);
                break;
            case LTE:
                criteriaSubQuery = lte(searchKey, value);
                break;
            case LT:
                criteriaSubQuery = lt(searchKey, value);
                break;
            case NOT_EQ:
                criteriaSubQuery = ne(searchKey, value);
                break;
            case EXISTS:
                criteriaSubQuery = exists(searchKey);
                break;
            case MISSING:
                criteriaSubQuery = missing(searchKey);
                break;
            case IN:
                if (!ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_TYPE_VALUES_MAPPING.containsValue(value)) {
                    criteriaSubQuery = in(searchKey, value);
                } else {
                    criteriaSubQuery = eq(searchKey, value);
                }
                break;
            default:
                criteriaSubQuery = eq(searchKey, value);
        }
        return criteriaSubQuery;
    }

    /**
     * create a valid VITAM DSL Query from a map of criteria
     *
     * @param searchCriteriaMap the input criteria. Should match pattern Map(FieldName, SearchValue)
     * @return The JsonNode required by VITAM external API for a DSL query
     * @throws InvalidParseOperationException
     * @throws InvalidCreateOperationException
     */
    public static JsonNode createQueryDSL(Map<String, Object> searchCriteriaMap,
        final Optional<String> orderBy, final Optional<DirectionDto> direction)
        throws InvalidParseOperationException, InvalidCreateOperationException {

        final Select select = new Select();
        final BooleanQuery query = and();
        BooleanQuery queryOr = or();
        boolean isEmpty = true;
        boolean haveOrParameters = false;

        // Manage Filters
        if (orderBy.isPresent()) {
            if (direction.isPresent() && DirectionDto.DESC.equals(direction.get())) {
                select.addOrderByDescFilter(orderBy.get());
            } else {
                select.addOrderByAscFilter(orderBy.get());
            }
        }
        Map<String, Integer> projection = new HashMap<>();
        projection.put("Identifier", 1);
        projection.put("Name", 1);

        QueryProjection queryProjection = new QueryProjection();
        queryProjection.setFields(projection);
        try {
            select.setProjection(JsonHandler.toJsonNode(queryProjection));
        } catch (InvalidParseOperationException e) {
            LOGGER.error("Error constructing vitam query : {}", e);
            throw new InvalidCreateOperationException("Invalid vitam query", e);
        }
        // Manage Query
        if (!searchCriteriaMap.isEmpty()) {
            isEmpty = false;
            Set<Map.Entry<String, Object>> entrySet = searchCriteriaMap.entrySet();

            for (final Map.Entry<String, Object> entry : entrySet) {
                final String searchKey = entry.getKey();

                switch (searchKey) {
                    case ArchiveSearchConsts.IDENTIFIER:
                        if (entry.getValue() instanceof ArrayList) {
                            final List<String> stringsValues = (ArrayList) entry.getValue();
                            for (String elt : stringsValues) {
                                queryOr.add(eq(searchKey, elt));
                            }
                            haveOrParameters = true;
                        } else if (entry.getValue() instanceof String) {
                            // string equals operation
                            final String stringValue = (String) entry.getValue();
                            queryOr.add(eq(searchKey, stringValue));
                            haveOrParameters = true;
                        }

                        break;

                    case ArchiveSearchConsts.NAME:
                    case ArchiveSearchConsts.SHORT_NAME:
                    case ArchiveSearchConsts.ID:
                        if (entry.getValue() instanceof ArrayList) {
                            final List<String> stringsValues = (ArrayList) entry.getValue();
                            for (String name : stringsValues) {
                                queryOr.add(matchPhrasePrefix(searchKey, name));
                            }
                            haveOrParameters = true;
                        } else if (entry.getValue() instanceof String) {
                            final String stringValue = (String) entry.getValue();
                            queryOr.add(matchPhrasePrefix(searchKey, stringValue));
                            haveOrParameters = true;
                        }

                        break;

                    default:
                        LOGGER.error("Can not find binding for key: {}", searchKey);
                        break;
                }
            }
        }

        if (!isEmpty) {
            if (haveOrParameters) {
                query.add(queryOr);
            }

            select.setQuery(query);
        }

        LOGGER.debug("Final query: {}", select.getFinalSelect().toPrettyString());
        return select.getFinalSelect();
    }

}
