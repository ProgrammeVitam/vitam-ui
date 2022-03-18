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
package fr.gouv.vitamui.referential.internal.server.accessionregister;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.exists;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.ne;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.nin;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.not;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.range;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.wildcard;

public class AccessRegisterVitamQueryHelper {

    private static final String ACQUISITION_INFORMATIONS = "acquisitionInformations";
    private static final String ACQUISITION_INFORMATION = "AcquisitionInformation";
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String DATE_FORMAT_DB_PATTERN = "yyyy-MM-dd";
    private static final Collection<String> staticAcquisitionInformations = List.of("Versement", "Protocole", "Achat",
        "Copie", "Dation", "Dépôt", "Dévolution", "Don", "Legs", "Réintégration", AccessRegisterVitamQueryHelper.ACQUISITION_INFORMATION_NON_RENSEIGNE,
        AccessRegisterVitamQueryHelper.ACQUISITION_INFORMATION_NON_RENSEIGNE);
    private static final String ACQUISITION_INFORMATION_NON_RENSEIGNE = "Non renseigné";
    private static final String ACQUISITION_INFORMATION_NON_AUTRES = "Autres";
    private static final String ORIGINATING_AGENCIES = "originatingAgencies";
    private static final String ORIGINATING_AGENCY = "OriginatingAgency";
    private static final String STATUS = "Status";
    private static final String ARCHIVAL_AGREEMENTS = "archivalAgreements";
    private static final String ARCHIVAL_AGREEMENT = "ArchivalAgreement";
    private static final String ELIMINATION = "elimination";
    private static final String ARCHIVAL_PROFILES = "archivalProfiles";
    private static final String ARCHIVAL_PROFILE = "ArchivalProfile";
    private static final String TRANSFER_REPLY = "transfer_reply";
    private static final String EVENTS_OPTYPE = "Events.OpType";
    private static final String END_DATE = "EndDate";
    private static final String END_DATE_MIN = "endDateMin";
    private static final String END_DATE_MAX = "endDateMax";
    private static final String ALL = "all";

    private AccessRegisterVitamQueryHelper() {
        throw new UnsupportedOperationException("Utility class !");
    }

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessRegisterVitamQueryHelper.class);

    public static JsonNode createQueryDSL(Map<String, Object> searchCriteriaMap, final Integer pageNumber, final Integer size,
        final Optional<String> orderBy, final Optional<DirectionDto> direction)
        throws InvalidCreateOperationException, InvalidParseOperationException {

        final Select select = new Select();
        BooleanQuery queryOr = or();
        BooleanQuery queryAnd = and();
        boolean haveOrParameters = false;
        boolean haveAndParameters = false;
        boolean isEmpty = searchCriteriaMap.isEmpty();

        manageFilters(orderBy, direction, select);

        if (!isEmpty) {

            Set<Map.Entry<String, Object>> entrySet = searchCriteriaMap.entrySet();

            for (final Map.Entry<String, Object> entry : entrySet) {
                final String searchKey = entry.getKey();
                switch (searchKey) {
                    case ORIGINATING_AGENCY: {
                        String stringValue = (String) entry.getValue();
                        queryAnd.add(wildcard(searchKey, "*"+stringValue+"*"));
                        haveAndParameters = true;
                        break;
                    }
                    case STATUS: {
                        List<String> stringValues = (ArrayList<String>) entry.getValue();
                        queryAnd.add(in(searchKey, stringValues.toArray(new String[] {})));
                        haveAndParameters = true;
                        break;
                    }
                    case END_DATE: {
                        addEndDateToQuery(queryAnd, entry.getValue());
                        haveAndParameters = true;
                        break;
                    }
                    case ORIGINATING_AGENCIES: {
                        List<String> originatingAgencies = (ArrayList<String>) entry.getValue();
                        queryOr.add(in(ORIGINATING_AGENCY, originatingAgencies.toArray(new String[] {})));
                        haveOrParameters = true;
                        break;
                    }
                    case ARCHIVAL_AGREEMENTS: {
                        List<String> archivalAgreements = (ArrayList<String>) entry.getValue();
                        queryOr.add(in(ARCHIVAL_AGREEMENT, archivalAgreements.toArray(new String[] {})));
                        haveOrParameters = true;
                        break;
                    }
                    case ARCHIVAL_PROFILES: {
                        List<String> archivalProfiles = (ArrayList<String>) entry.getValue();
                        queryOr.add(in(ARCHIVAL_PROFILE, archivalProfiles.toArray(new String[] {})));
                        haveOrParameters = true;
                        break;
                    }
                    case ACQUISITION_INFORMATIONS: {
                        boolean queryHaveBeenModified = addAcquisitionInformationsToQuery(queryOr, (ArrayList<String>) entry.getValue());
                        if (queryHaveBeenModified) {
                            haveOrParameters = true;
                        }
                        break;
                    }
                    case ELIMINATION:
                    case TRANSFER_REPLY: {
                        boolean queryHaveBeenModified = addEventsToQuery(queryOr, (String) entry.getValue(), searchKey.toUpperCase());
                         if (queryHaveBeenModified) {
                             haveOrParameters = true;
                         }
                        break;
                    }
                    default:
                        LOGGER.error("Can not find binding for key: {}", searchKey);
                        break;
                }
            }
        }

        setQuery(select, isEmpty, queryAnd, queryOr, haveOrParameters, haveAndParameters);

        LOGGER.debug("Final query Details: {}", select.getFinalSelect().toPrettyString());

        return select.getFinalSelect();
    }

    private static void setQuery(Select select, boolean isEmpty, BooleanQuery queryAnd, BooleanQuery queryOr,
        boolean haveOrParameters, boolean haveAndParameters) throws InvalidCreateOperationException {
        if (!isEmpty) {
            if (haveAndParameters && haveOrParameters) {
                queryAnd.add(queryOr);
                select.setQuery(queryAnd);
            } else if(haveAndParameters) {
                select.setQuery(queryAnd);
            } else if(haveOrParameters) {
                select.setQuery(queryOr);
            }
        }
    }

    private static void manageFilters(Optional<String> orderBy, Optional<DirectionDto> direction, Select select) throws
        InvalidParseOperationException {
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

    private static boolean addEventsToQuery(BooleanQuery query, String value, String searchKeyUpperCase) throws InvalidCreateOperationException {
        if(!value.equals(ALL)) {
            boolean cond = Boolean.parseBoolean(value);
            if(cond) {
                query.add(eq(EVENTS_OPTYPE, searchKeyUpperCase));
            } else {
                query.add(ne(EVENTS_OPTYPE, searchKeyUpperCase));
            }
            return true;
        }
        return false;
    }

    private static boolean addAcquisitionInformationsToQuery(BooleanQuery query, List<String> data) throws InvalidCreateOperationException {
        List<String> acquisitionInformations = new ArrayList<>(staticAcquisitionInformations);
        acquisitionInformations.removeAll(data);
        if(!acquisitionInformations.isEmpty()) {
            if(data.contains(ACQUISITION_INFORMATION_NON_RENSEIGNE)) {
                query.add(not().add(exists(ACQUISITION_INFORMATION)));
            }
            if(data.contains(ACQUISITION_INFORMATION_NON_AUTRES)) {
                List<String> othersAcquisitionInformations = new ArrayList<>(staticAcquisitionInformations);
                othersAcquisitionInformations.removeAll(List.of(ACQUISITION_INFORMATION_NON_RENSEIGNE, ACQUISITION_INFORMATION_NON_AUTRES));
                query.add(and()
                    .add(exists(ACQUISITION_INFORMATION))
                    .add(nin(ACQUISITION_INFORMATION, othersAcquisitionInformations.toArray(new String[] {})))
                );
            }
            data.removeAll(List.of(ACQUISITION_INFORMATION_NON_RENSEIGNE, ACQUISITION_INFORMATION_NON_AUTRES));
            if(!data.isEmpty()) {
                query.add(and()
                    .add(exists(ACQUISITION_INFORMATION))
                    .add(in(ACQUISITION_INFORMATION, data.toArray(new String[] {})))
                );
            }
            return true;
        }
        return false;
    }

    private static void addEndDateToQuery(BooleanQuery query, Object value) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dateInterval = mapper.convertValue(value, JsonNode.class);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DB_PATTERN).withZone(ZoneOffset.UTC);
            String dateMinStr = "null".equals(dateInterval.get(END_DATE_MIN).asText()) ? null : dateInterval.get(
                END_DATE_MIN).asText();
            String dateMaxStr = "null".equals(dateInterval.get(END_DATE_MAX).asText()) ? null : dateInterval.get(
                END_DATE_MAX).asText();

            if (dateMinStr != null && dateMaxStr == null) {
                query.add(eq(END_DATE, LocalDate.parse(dateMinStr, dtf).format(formatter)));
            }

            if (dateMinStr == null && dateMaxStr != null) {
                query.add(eq(END_DATE, LocalDate.parse(dateMaxStr, dtf).format(formatter)));
            }

            if (dateMinStr != null && dateMaxStr != null) {
                query.add(range(END_DATE, LocalDate.parse(dateMinStr, dtf).format(formatter), true, LocalDate.parse(dateMaxStr, dtf).format(formatter), true));
            }

        } catch (InvalidCreateOperationException e) {
            LOGGER.error("Can not find binding for EndDate key: \n {}", e);
        }
    }


}
