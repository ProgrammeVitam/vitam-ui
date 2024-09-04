/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.common.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.CompareQuery;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.gt;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.lt;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.matchPhrasePrefix;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.wildcard;

public class VitamQueryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(VitamQueryHelper.class);

    /* Operation types */
    public static final String AGENCY_IMPORT_OPERATION_TYPE = "IMPORT_AGENCIES.OK";
    public static final String RULE_IMPORT_OPERATION_TYPE = "STP_IMPORT_RULES.OK";

    /* Query fields */
    private static final String IDENTIFIER = "Identifier";
    private static final String ID = "#id";
    private static final String NAME = "Name";
    private static final String SHORT_NAME = "ShortName";
    private static final String PUID = "PUID";
    private static final String RULE_ID = "RuleId";
    private static final String RULE_VALUE = "RuleValue";
    private static final String RULE_TYPE = "RuleType";
    private static final String EV_TYPE_PROC = "evTypeProc";
    private static final String STATUS = "Status";
    private static final String EV_TYPE = "evType";
    private static final String EV_DATE_TIME_START = "evDateTime_Start";
    private static final String EV_DATE_TIME_END = "evDateTime_End";
    private static final String OPI = "Opi";
    private static final String ORIGINATING_AGENCY = "OriginatingAgency";
    private static final String SEDAVERSION = "SedaVersion";

    public static final Collection<String> staticAcquisitionInformations = List.of(
        "Versement",
        "Protocole",
        "Achat",
        "Copie",
        "Dation",
        "Dépôt",
        "Dévolution",
        "Don",
        "Legs",
        "Réintégration",
        "Autres",
        VitamQueryHelper.ACQUISITION_INFORMATION_NON_RENSEIGNE
    );
    public static final String ACQUISITION_INFORMATION_NON_RENSEIGNE = "Non renseigné";

    private VitamQueryHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * create a valid VITAM DSL Query from a map of criteria
     *
     * @param searchCriteriaMap the input criteria. Should match pattern Map(FieldName, SearchValue)
     * @return The JsonNode required by VITAM external API for a DSL query
     * @throws InvalidParseOperationException
     * @throws InvalidCreateOperationException
     */
    public static JsonNode createQueryDSL(Map<String, Object> searchCriteriaMap)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        return createQueryDSL(searchCriteriaMap, null, null, Optional.empty(), Optional.empty());
    }

    /**
     * create a valid VITAM DSL Query from a map of criteria
     *
     * @param searchCriteriaMap the input criteria. Should match pattern Map(FieldName, SearchValue)
     * @return The JsonNode required by VITAM external API for a DSL query
     * @throws InvalidParseOperationException
     * @throws InvalidCreateOperationException
     */
    public static JsonNode createQueryDSL(
        Map<String, Object> searchCriteriaMap,
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction
    ) throws InvalidParseOperationException, InvalidCreateOperationException {
        final Select select = new Select();
        final BooleanQuery query = and();
        BooleanQuery queryOr = or();
        boolean isEmpty = true;
        boolean haveOrParameters = false;

        manageFilters(orderBy, direction, select);
        if (pageNumber != null && size != null) {
            select.setLimitFilter(pageNumber * size, size);
        }

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
                    case SEDAVERSION:
                        // string equals operation
                        final String stringValue = (String) entry.getValue();
                        queryOr.add(eq(searchKey, stringValue));
                        haveOrParameters = true;
                        break;
                    case EV_TYPE_PROC:
                    case RULE_TYPE:
                        // string equals operation filter as a and
                        final String ruleType = (String) entry.getValue();
                        query.add(eq(searchKey, ruleType));
                        break;
                    case RULE_ID:
                    case OPI:
                    case ORIGINATING_AGENCY:
                        // string wildward operation
                        final String ruleId = (String) entry.getValue();
                        queryOr.add(wildcard(searchKey, "*" + ruleId + "*"));
                        haveOrParameters = true;
                        break;
                    case RULE_VALUE:
                        // string match phrase prefix operation
                        final String ruleValue = (String) entry.getValue();
                        queryOr.add(matchPhrasePrefix(searchKey, ruleValue));
                        haveOrParameters = true;
                        break;
                    case EV_TYPE:
                        // Special case EvType can be String or String[]
                        if (entry.getValue() instanceof String) {
                            final String evType = (String) entry.getValue();
                            query.add(eq(searchKey, evType));
                            break;
                        }
                        // in list of string EvType
                        if (entry.getValue() instanceof List) {
                            final List<String> stringValues = (ArrayList<String>) entry.getValue();
                            query.add(in(searchKey, stringValues.stream().toArray(String[]::new)));
                            break;
                        }

                        break;
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

        setQuery(select, query, queryOr, isEmpty, haveOrParameters);

        LOGGER.debug("Final query Details: {}", select.getFinalSelect().toPrettyString());
        return select.getFinalSelect();
    }

    private static void manageFilters(Optional<String> orderBy, Optional<DirectionDto> direction, Select select)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        // Manage Filters
        if (orderBy.isPresent()) {
            String order = orderBy.get();
            if (direction.isPresent() && DirectionDto.DESC.equals(direction.get())) {
                select.addOrderByDescFilter(order);
            } else {
                select.addOrderByAscFilter(order);
            }
        }
    }

    private static void setQuery(
        Select select,
        BooleanQuery query,
        BooleanQuery queryOr,
        boolean isEmpty,
        boolean haveOrParameters
    ) throws InvalidCreateOperationException {
        if (!isEmpty) {
            if (haveOrParameters) {
                query.add(queryOr);
            }
            if (!query.getQueries().isEmpty()) {
                select.setQuery(query);
            }
        }
    }

    public static JsonNode getLastOperationQuery(String operationType)
        throws InvalidCreateOperationException, InvalidParseOperationException {
        Select select = new Select();

        BooleanQuery query = and();
        query.add(in("events.outDetail", operationType));

        select.setQuery(query);
        select.setLimitFilter(0, 1);
        select.addOrderByDescFilter("evDateTime");

        return select.getFinalSelect();
    }

    public static JsonNode buildOperationQuery(final String obId) throws InvalidCreateOperationException {
        final Select select = new Select();
        final CompareQuery obIdQuery;
        obIdQuery = QueryHelper.eq("obId", obId);
        select.setQuery(obIdQuery);
        return select.getFinalSelect();
    }
}
