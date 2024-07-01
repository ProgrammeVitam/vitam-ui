/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */
package fr.gouv.vitamui.referential.internal.server.accessionregister;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.AccessionRegisterSearchDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@UtilityClass
public class AccessRegisterVitamQueryHelper {

    private static final DateTimeFormatter INPUT_DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(
        ZoneOffset.UTC
    );
    private static final DateTimeFormatter OUTPUT_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(
        ZoneOffset.UTC
    );

    private static final String ACQUISITION_INFORMATION = "AcquisitionInformation";
    private static final String NON_RENSEIGNE = "Non renseigné";
    private static final String AUTRES = "Autres";
    private static final Collection<String> NON_RENSEIGNE_ET_AUTRES = List.of(NON_RENSEIGNE, AUTRES);
    private static final Collection<String> ACQUISITION_INFORMATIONS_AVAILABLE = List.of(
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
        NON_RENSEIGNE,
        AUTRES
    );

    private static final String OPI = "Opi";
    private static final String ORIGINATING_AGENCY = "OriginatingAgency";
    private static final String STATUS = "Status";
    private static final String ARCHIVAL_AGREEMENT = "ArchivalAgreement";
    private static final String ELIMINATION = "ELIMINATION";
    private static final String ARCHIVAL_PROFILE = "ArchivalProfile";
    private static final String TRANSFER_REPLY = "TRANSFER_REPLY";
    private static final String EVENTS_OPTYPE = "Events.OpType";
    private static final String END_DATE = "EndDate";
    private static final String ALL = "all";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessRegisterVitamQueryHelper.class);

    public static JsonNode createQueryDSL(AccessionRegisterSearchDto criteria)
        throws InvalidCreateOperationException, InvalidParseOperationException {
        return AccessRegisterVitamQueryHelper.createQueryDSL(
            criteria,
            null,
            null,
            criteria.getOrderBy(),
            criteria.getDirection()
        );
    }

    public static JsonNode createQueryDSL(
        AccessionRegisterSearchDto criteria,
        final Integer pageNumber,
        final Integer size,
        final String orderBy,
        final DirectionDto direction
    ) throws InvalidCreateOperationException, InvalidParseOperationException {
        final Select select = new Select();

        BooleanQuery orQuery = or();
        BooleanQuery andQuery = and();
        if (nonNull(pageNumber) && nonNull(size)) {
            select.setLimitFilter((long) pageNumber * size, size);
        }

        addOrderToQuery(select, orderBy, direction);
        addFiltersToQuery(andQuery, criteria.getFilters());
        addEndDateToQuery(andQuery, criteria.getEndDateInterval());
        addEventsToQuery(andQuery, criteria.getElimination(), ELIMINATION);
        addEventsToQuery(andQuery, criteria.getTransferReply(), TRANSFER_REPLY);
        addOpiToQuery(orQuery, criteria.getOpi());
        addOriginatingAgencyToQuery(orQuery, criteria.getOriginatingAgency());

        addFieldInToQuery(orQuery, ORIGINATING_AGENCY, criteria.getOriginatingAgencies());
        addFieldInToQuery(orQuery, ARCHIVAL_AGREEMENT, criteria.getArchivalAgreements());
        addFieldInToQuery(orQuery, ARCHIVAL_PROFILE, criteria.getArchivalProfiles());

        setAcquisitionInformationsToQuery(orQuery, criteria.getAcquisitionInformations());

        setQuery(select, andQuery, orQuery);
        ObjectNode finalSelect = select.getFinalSelect();
        LOGGER.debug("Final query Details: {}", finalSelect.toPrettyString());
        return finalSelect;
    }

    private static void addFieldInToQuery(BooleanQuery query, String field, List<String> list)
        throws InvalidCreateOperationException {
        if (isEmpty(list)) {
            return;
        }
        query.add(in(field, list.toArray(new String[] {})));
    }

    private static void addOriginatingAgencyToQuery(BooleanQuery orQuery, String originatingAgency)
        throws InvalidCreateOperationException {
        if (isNull(originatingAgency)) {
            return;
        }
        orQuery.add(wildcard(ORIGINATING_AGENCY, "*" + originatingAgency + "*"));
    }

    private static void addOpiToQuery(BooleanQuery orQuery, String opi) throws InvalidCreateOperationException {
        if (isNull(opi)) {
            return;
        }
        orQuery.add(wildcard(OPI, "*" + opi + "*"));
    }

    private static void addFiltersToQuery(BooleanQuery andQuery, Map<String, List<String>> filters)
        throws InvalidCreateOperationException {
        if (MapUtils.isEmpty(filters)) {
            return;
        }
        List<String> statusFilters = filters.get(STATUS);
        if (isNotEmpty(statusFilters)) {
            andQuery.add(in(STATUS, statusFilters.toArray(String[]::new)));
        }
    }

    private static void setQuery(Select select, BooleanQuery andQuery, BooleanQuery orQuery)
        throws InvalidCreateOperationException {
        if (isNotEmpty(andQuery.getQueries())) {
            if (isNotEmpty(orQuery.getQueries())) {
                andQuery.add(orQuery);
            }
            select.setQuery(andQuery);
        } else if (isNotEmpty(orQuery.getQueries())) {
            select.setQuery(orQuery);
        }
    }

    private static void addOrderToQuery(Select select, String orderBy, DirectionDto direction)
        throws InvalidParseOperationException {
        if (isNull(orderBy)) {
            return;
        }
        if (DirectionDto.DESC.equals(direction)) {
            select.addOrderByDescFilter(orderBy);
        } else {
            select.addOrderByAscFilter(orderBy);
        }
    }

    private static void addEventsToQuery(BooleanQuery query, String value, String searchKeyUpperCase)
        throws InvalidCreateOperationException {
        if (isNull(value) || value.equals(ALL)) {
            return;
        }
        if (Boolean.parseBoolean(value)) {
            query.add(eq(EVENTS_OPTYPE, searchKeyUpperCase));
        } else {
            query.add(ne(EVENTS_OPTYPE, searchKeyUpperCase));
        }
    }

    private static void setAcquisitionInformationsToQuery(BooleanQuery query, List<String> acquisitionInformationsList)
        throws InvalidCreateOperationException {
        if (CollectionUtils.isEmpty(acquisitionInformationsList)) {
            return;
        }
        List<String> acquisitionInformationsAvailable = new ArrayList<>(ACQUISITION_INFORMATIONS_AVAILABLE);
        acquisitionInformationsAvailable.removeAll(acquisitionInformationsList);
        if (acquisitionInformationsAvailable.isEmpty()) {
            return;
        }
        if (acquisitionInformationsList.contains(NON_RENSEIGNE)) {
            query.add(not().add(exists(ACQUISITION_INFORMATION)));
        }
        if (acquisitionInformationsList.contains(AUTRES)) {
            List<String> othersAcquisitionInformations = new ArrayList<>(ACQUISITION_INFORMATIONS_AVAILABLE);
            othersAcquisitionInformations.removeAll(NON_RENSEIGNE_ET_AUTRES);
            query.add(
                and()
                    .add(exists(ACQUISITION_INFORMATION))
                    .add(nin(ACQUISITION_INFORMATION, othersAcquisitionInformations.toArray(String[]::new)))
            );
        }
        acquisitionInformationsList.removeAll(NON_RENSEIGNE_ET_AUTRES);
        if (!acquisitionInformationsList.isEmpty()) {
            query.add(
                and()
                    .add(exists(ACQUISITION_INFORMATION))
                    .add(in(ACQUISITION_INFORMATION, acquisitionInformationsList.toArray(String[]::new)))
            );
        }
    }

    private static void addEndDateToQuery(
        BooleanQuery query,
        AccessionRegisterSearchDto.EndDateInterval endDateInterval
    ) {
        if (isNull(endDateInterval)) {
            return;
        }
        try {
            String dateMinStr = endDateInterval.getEndDateMin();
            String dateMaxStr = endDateInterval.getEndDateMax();
            if (nonNull(dateMinStr) && isNull(dateMaxStr)) {
                query.add(eq(END_DATE, LocalDate.parse(dateMinStr, INPUT_DTF).format(OUTPUT_DTF)));
            }
            if (isNull(dateMinStr) && nonNull(dateMaxStr)) {
                query.add(eq(END_DATE, LocalDate.parse(dateMaxStr, INPUT_DTF).format(OUTPUT_DTF)));
            }
            if (nonNull(dateMinStr) && nonNull(dateMaxStr)) {
                query.add(
                    range(
                        END_DATE,
                        LocalDate.parse(dateMinStr, INPUT_DTF).format(OUTPUT_DTF),
                        true,
                        LocalDate.parse(dateMaxStr, INPUT_DTF).format(OUTPUT_DTF),
                        true
                    )
                );
            }
        } catch (InvalidCreateOperationException e) {
            LOGGER.error("Can not find binding for EndDate key: \n {}", e);
        }
    }
}
