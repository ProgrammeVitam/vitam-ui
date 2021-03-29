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
package fr.gouv.vitamui.archives.search.common.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.database.builder.facet.FacetHelper;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.gt;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.gte;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.lt;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.lte;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.match;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

public class VitamQueryHelper {
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamQueryHelper.class);


    /*
    Operators for criteria
     */
    private enum CRITERIA_OPERATORS {
        EQ, MATCH, LT, GT, LE, GE
    }


    private static final int DEFAULT_DEPTH = 30;
    private static final int FACET_SIZE_MILTIPLIER = 10;

    /* Query fields */
    private static final String IDENTIFIER = "Identifier";
    private static final String UNIT_TYPE = "#unitType";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String START_DATE = "StartDate";
    private static final String PRODUCER_SERVICE = "#originating_agency";
    private static final String GUID = "#id";
    private static final String UNITS_UPS = "#allunitups";
    private static final String END_DATE = "EndDate";
    private static final String TITLE_OR_DESCRIPTION = "titleAndDescription";

    /* Query fields */
    private static final String ID = "#id";
    private static final String NAME = "Name";
    private static final String SHORT_NAME = "ShortName";
    private static final String PUID = "PUID";
    private static final String EV_TYPE_PROC = "evTypeProc";
    private static final String STATUS = "Status";
    private static final String EV_TYPE = "evType";
    private static final String EV_DATE_TIME_START = "evDateTime_Start";
    private static final String EV_DATE_TIME_END = "evDateTime_End";

    /**
     * create a valid VITAM DSL Query from a map of criteria
     *
     * @param searchCriteriaMap the input criteria. Should match pattern Map(FieldName, SearchValue)
     * @return The JsonNode required by VITAM external API for a DSL query
     * @throws InvalidParseOperationException
     */
    public static JsonNode createQueryDSL(List<String> unitTypes, List<String> nodes,
        Map<String, List<String>> searchCriteriaMap,
        final Integer pageNumber,
        final Integer size, final Optional<String> orderBy, final Optional<DirectionDto> direction)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        boolean isValid = true;
        final BooleanQuery query = and();
        final SelectMultiQuery select = new SelectMultiQuery();
        //Handle roots
        if (nodes != null && !nodes.isEmpty()) {
            select.addRoots(nodes.toArray(new String[nodes.size()]));
            select.addFacets(
                FacetHelper.terms("COUNT_BY_NODE", UNITS_UPS, nodes.size() * FACET_SIZE_MILTIPLIER, FacetOrder.ASC));
            query.setDepthLimit(DEFAULT_DEPTH);
        }

        if (unitTypes == null || unitTypes.isEmpty()) {
            LOGGER.error("Error on validation of criteria , units types is mandatory ");
            throw new InvalidParseOperationException("Error on validation of criteria,  units types is mandatory ");
        }
        addParameterCriteria(query, CRITERIA_OPERATORS.EQ, UNIT_TYPE, unitTypes);
        // Manage Filters
        if (orderBy.isPresent()) {
            if (direction.isPresent() && DirectionDto.DESC.equals(direction.get())) {
                select.addOrderByDescFilter(orderBy.get());
            } else {
                select.addOrderByAscFilter(orderBy.get());
            }
        }
        select.setLimitFilter(pageNumber * size, size);

        // Manage Query
        if (!searchCriteriaMap.isEmpty()) {
            Set<Map.Entry<String, List<String>>> entrySet = searchCriteriaMap.entrySet();

            for (final Map.Entry<String, List<String>> entry : entrySet) {
                final String searchKey = entry.getKey();
                if (searchKey.startsWith("_")) {
                    LOGGER.error("Criteria with _ prefix is forbidden : {} ", searchKey);
                    throw new InvalidParseOperationException("Criteria with _ prefix is forbidden : " + searchKey);
                }
                switch (searchKey) {
                    case GUID:
                    case IDENTIFIER:
                    case PRODUCER_SERVICE:
                        isValid = addParameterCriteria(query, CRITERIA_OPERATORS.EQ, searchKey, entry.getValue());
                        break;
                    case TITLE:
                    case DESCRIPTION:
                        isValid = addParameterCriteria(query, CRITERIA_OPERATORS.EQ, searchKey, entry.getValue());
                        break;
                    case START_DATE:
                        isValid = addParameterCriteria(query, CRITERIA_OPERATORS.GE, searchKey, entry.getValue());
                        break;
                    case END_DATE:
                        isValid = addParameterCriteria(query, CRITERIA_OPERATORS.LE, searchKey, entry.getValue());
                        break;
                    case TITLE_OR_DESCRIPTION:
                        query.add(buildTitleAndDescriptionQuery(entry.getValue(), CRITERIA_OPERATORS.EQ));
                        break;
                    default:
                        LOGGER.info("adding other field from not listed fields for key: {},", searchKey);
                        isValid = addParameterCriteria(query, CRITERIA_OPERATORS.EQ, searchKey, entry.getValue());
                        break;
                }
            }
            if (!isValid) {
                LOGGER.error("Error on validation of criteria ");
                throw new InvalidParseOperationException("Error on validation of criteria ");
            }
        }
        select.setQuery(query);
        LOGGER.info("Final query: {}", select.getFinalSelect().toPrettyString());
        return select.getFinalSelect();
    }

    private static boolean addParameterCriteria(BooleanQuery query, CRITERIA_OPERATORS operator, String searchKey,
        final List<String> searchValues)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        boolean isValid = true;
        if (StringUtils.isEmpty(searchValues)) {
            isValid = false;
        } else {
            if (searchValues.size() > 1) {
                BooleanQuery subQueryOr = or();
                //The case of multiple values , => Or operator
                for (String value : searchValues) {
                    subQueryOr.add(buildSubQueryByOperator(searchKey, value, operator));
                }
                query.add(subQueryOr);
            } else {
                //the case of one value
                query.add(buildSubQueryByOperator(searchKey, searchValues.stream().findAny().get(), operator));
            }
        }
        return isValid;
    }


    private static Query buildTitleAndDescriptionQuery(final List<String> searchValues, CRITERIA_OPERATORS operator)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        if (searchValues != null && !searchValues.isEmpty()) {
            for (String value : searchValues) {
                BooleanQuery subQueryOr = or();
                subQueryOr.add(buildSubQueryByOperator(DESCRIPTION, value, operator));
                subQueryOr.add(buildSubQueryByOperator(TITLE, value, operator));
                subQueryAnd.add(subQueryOr);
            }
        }
        return subQueryAnd;
    }

    private static Query buildSubQueryByOperator(String searchKey, String value, CRITERIA_OPERATORS operator)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        Query criteriaSubQuery = eq(searchKey, value);
        switch (operator) {
            case MATCH:
                criteriaSubQuery = match(searchKey, value);
                break;
            case GE:
                criteriaSubQuery = gte(searchKey, value);
                break;
            case GT:
                criteriaSubQuery = gt(searchKey, value);
                break;
            case LE:
                criteriaSubQuery = lte(searchKey, value);
                break;
            case LT:
                criteriaSubQuery = lt(searchKey, value);
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
    public static JsonNode createQueryDSL(Map<String, Object> searchCriteriaMap, final Integer pageNumber,
        final Integer size,
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
        select.setLimitFilter(pageNumber * size, size);

        // Manage Query
        if (!searchCriteriaMap.isEmpty()) {
            isEmpty = false;
            Set<Map.Entry<String, Object>> entrySet = searchCriteriaMap.entrySet();

            for (final Map.Entry<String, Object> entry : entrySet) {
                final String searchKey = entry.getKey();

                switch (searchKey) {
                    case NAME:
                    case SHORT_NAME:
                    case IDENTIFIER:
                    case ID:
                    case PUID:
                    case EV_TYPE_PROC:
                        // string equals operation
                        final String stringValue = (String) entry.getValue();
                        queryOr.add(eq(searchKey, stringValue));
                        haveOrParameters = true;
                        break;
                    case EV_TYPE:
                        // Special case EvType can be String or String[]
                        if (entry.getValue() instanceof String) {
                            final String evType = (String) entry.getValue();
                            query.add(eq(searchKey, evType));
                            break;
                        }
                    case STATUS:
                        // in list of string operation
                        final List<String> stringValues = (ArrayList<String>) entry.getValue();
                        query.add(in(searchKey, stringValues.toArray(new String[] {})));
                        break;
                    case EV_DATE_TIME_START:
                        query.add(gt("evDateTime", (String) entry.getValue()));
                        break;
                    case EV_DATE_TIME_END:
                        query.add(lt("evDateTime", (String) entry.getValue()));
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

