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
package fr.gouv.vitamui.ingest.common.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.gt;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.lt;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

public class VitamQueryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(VitamQueryHelper.class);

    /* Operation types */
    public static final String AGENCY_IMPORT_OPERATION_TYPE = "IMPORT_AGENCIES.OK";

    /* Query fields */
    private static final String ID = "#id";
    private static final String OB_ID_IN = "obIdIn";
    private static final String TRANSFERRING_AGENCY = "events.agIdExt.TransferringAgency";
    private static final String ORIGINATING_AGENCY = "events.agIdExt.originatingAgency";
    private static final String ARCHIVAL_AGENCY = "events.evDetData.ArchivalAgreement";
    private static final String EV_TYPE_PROC = "evTypeProc";
    private static final String STATUS = "Status";
    private static final String EV_TYPE = "evType";
    private static final String EV_DATE_TIME_START = "evDateTime_Start";
    private static final String EV_DATE_TIME_END = "evDateTime_End";
    private static final String COMMENT = "events.evDetData.EvDetailReq";
    private static final String SELECTED_ORIGINATING_AGENCIES = "SELECTED_ORIGINATING_AGENCIES";

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
        BooleanQuery orGroup = or();
        boolean haveOrGroup = false;

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
                    case EV_TYPE_PROC:
                        // string equals operation
                        final String stringValue = (String) entry.getValue();
                        query.add(eq(searchKey, stringValue));
                        break;
                    case OB_ID_IN:
                    case TRANSFERRING_AGENCY:
                    case ARCHIVAL_AGENCY:
                    case ORIGINATING_AGENCY:
                    case ID:
                    case COMMENT:
                        if (entry.getValue() instanceof ArrayList) {
                            final List<String> stringsValues = (ArrayList) entry.getValue();
                            for (String elt : stringsValues) {
                                queryOr.add(eq(searchKey, elt));
                            }
                            haveOrParameters = true;
                        } else {
                            if (entry.getValue() instanceof String) {
                                final String searchValue = (String) entry.getValue();
                                queryOr.add(eq(searchKey, searchValue));
                                haveOrParameters = true;
                            }
                        }
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
                    case SELECTED_ORIGINATING_AGENCIES:
                        if (entry.getValue() instanceof ArrayList) {
                            final List<String> stringsValues = (ArrayList) entry.getValue();
                            for (String elt : stringsValues) {
                                orGroup.add(eq(ORIGINATING_AGENCY, elt));
                            }
                            haveOrGroup = true;
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
            if (haveOrGroup) {
                query.add(orGroup);
            }

            select.setQuery(query);
        }

        LOGGER.debug("Final query: {}", select.getFinalSelect().toPrettyString());
        return select.getFinalSelect();
    }
}
