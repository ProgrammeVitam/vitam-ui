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
package fr.gouv.vitamui.referential.internal.server.accessionregister.summary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.QueryProjection;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessionRegisterDetailModel;
import fr.gouv.vitam.common.model.administration.AccessionRegisterSummaryModel;
import fr.gouv.vitam.common.model.administration.RegisterValueDetailModel;
import fr.gouv.vitamui.commons.api.domain.AccessionRegisterDetailsSearchStatsDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailResponseDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterStatsDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryResponseDto;
import fr.gouv.vitamui.referential.common.service.AccessionRegisterService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.ne;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.nin;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.range;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.wildcard;

@Service
public class AccessionRegisterSummaryInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        AccessionRegisterSummaryInternalService.class);

    private final AccessionRegisterService accessionRegisterService;

    private final ObjectMapper objectMapper;

    private final AdminExternalClient adminExternalClient;

    private static final String STATUS = "Status";
    private static final String ORIGINATING_AGENCY = "OriginatingAgency";
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String END_DATE = "EndDate";
    private static final String ARCHIVAL_AGREEMENT = "ArchivalAgreement";
    private static final String ARCHIVAL_PROFILE = "ArchivalProfile";
    private static final String ACQUISITION_INFORMATION = "AcquisitionInformation";
    private static final String EVENTS_OPTYPE = "Events.OpType";
    private static final String ELIMINATION = "ELIMINATION";
    private static final String TRANSFER = "TRANSFER";

    @Autowired
    AccessionRegisterSummaryInternalService(AccessionRegisterService accessionRegisterService,
                                            ObjectMapper objectMapper,
                                            AdminExternalClient adminExternalClient) {
        this.accessionRegisterService = accessionRegisterService;
        this.objectMapper = objectMapper;
        this.adminExternalClient = adminExternalClient;
    }


    public List<AccessionRegisterSummaryDto> getAll(VitamContext context) {
        RequestResponse<AccessionRegisterSummaryModel> requestResponse;
        try {
            LOGGER.debug("List of Accession Register EvIdAppSession : {} " , context.getApplicationSessionId());
            requestResponse = accessionRegisterService.findAccessionRegisterSummary(context);
            final AccessionRegisterSummaryResponseDto accessionRegisterSymbolicResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessionRegisterSummaryResponseDto.class);
            return AccessionRegisterSummaryConverter.convertVitamsToDtos(accessionRegisterSymbolicResponseDto.getResults());
        } catch (JsonProcessingException | VitamClientException e) {
            LOGGER.error("Error when getting Accession Register summaries : {} " , e);
            throw new InternalServerException("Unable to find accessionRegisterSymbolic", e);
        }
    }

    public AccessionRegisterStatsDto getAccessionRegisterDetailStats(VitamContext vitamContext, AccessionRegisterDetailsSearchStatsDto detailsSearchDto) {

        List<AccessionRegisterSummaryDto> customAccessionRegisterSummaries =
            getCustomAccessionRegisterSummaries(vitamContext, detailsSearchDto);

        if(customAccessionRegisterSummaries == null) {
            LOGGER.debug("Unable to find accessionRegister Summaries stats");
            throw new InternalServerException("Unable to find accessionRegister Summaries stats");
        }

        long totalUnits = customAccessionRegisterSummaries.stream().parallel()
            .map(AccessionRegisterSummaryDto::getTotalUnits)
            .map(RegisterValueDetailModel::getIngested).mapToLong(Long::longValue).sum();

        long totalObjectsGroups = customAccessionRegisterSummaries.stream().parallel()
            .map(AccessionRegisterSummaryDto::getTotalObjectsGroups)
            .map(RegisterValueDetailModel::getIngested).mapToLong(Long::longValue).sum();

        long totalObjects = customAccessionRegisterSummaries.stream().parallel()
            .map(AccessionRegisterSummaryDto::getTotalObjects)
            .map(RegisterValueDetailModel::getIngested).mapToLong(Long::longValue).sum();

        long objectSizes = customAccessionRegisterSummaries.stream().parallel()
            .map(AccessionRegisterSummaryDto::getObjectSize)
            .map(RegisterValueDetailModel::getIngested).mapToLong(Long::longValue).sum();

        return new AccessionRegisterStatsDto(totalUnits, totalObjectsGroups, totalObjects, objectSizes);
    }

    public List<AccessionRegisterSummaryDto> getCustomAccessionRegisterSummaries(VitamContext context, AccessionRegisterDetailsSearchStatsDto detailsSearchDto) {
        RequestResponse<AccessionRegisterSummaryModel> requestResponse;
        try {
            LOGGER.debug("Context application Session ID : {} ", context.getApplicationSessionId());

            JsonNode detailsQuery = buildCustomAccessionRegisterDetailsQuery(detailsSearchDto);

            RequestResponse<AccessionRegisterDetailModel> accessionRegisterDetails = adminExternalClient
                .findAccessionRegisterDetails(context, detailsQuery);

            AccessionRegisterDetailResponseDto results = objectMapper
                .treeToValue(accessionRegisterDetails.toJsonNode(), AccessionRegisterDetailResponseDto.class);


            List<String> distinctOriginatingAgencies = new ArrayList<>();
            if (results != null) {
                distinctOriginatingAgencies = results.getResults().stream()
                    .map(AccessionRegisterDetailModel::getOriginatingAgency)
                    .filter(Objects::nonNull)
                    .filter(originatingAgency -> ConcurrentHashMap.newKeySet().add(originatingAgency))
                    .collect(Collectors.toList());
            }
            LOGGER.debug("Distinct Originating Agencies : {} ", distinctOriginatingAgencies);

            JsonNode query = buildCustomAccessionRegisterSummariesQuery(distinctOriginatingAgencies);
            requestResponse = accessionRegisterService.findAccessionRegisterSummaryByQuery(context, query);
            final AccessionRegisterSummaryResponseDto accessionRegisterSummaryResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessionRegisterSummaryResponseDto.class);

            return AccessionRegisterSummaryConverter.convertVitamsToDtos(accessionRegisterSummaryResponseDto.getResults());
        } catch (JsonProcessingException | VitamClientException | InvalidCreateOperationException | ParseException e) {
            LOGGER.error("Unable to find accessionRegister Summaries : {} " , e);
            throw new InternalServerException("Unable to find accessionRegister Summaries", e);
        }
    }

    public static JsonNode buildCustomAccessionRegisterDetailsQuery(AccessionRegisterDetailsSearchStatsDto detailsSearchDto)
        throws InvalidCreateOperationException, ParseException {

        final Select select = new Select();
        BooleanQuery query = and();

        if(detailsSearchDto.getSearchText() != null) {
            query.add(wildcard(ORIGINATING_AGENCY, "*"+detailsSearchDto.getSearchText()+"*"));
        }

        if(detailsSearchDto.getStatusFilter() != null && !detailsSearchDto.getStatusFilter().isEmpty()
            && !detailsSearchDto.getStatusFilter().get(STATUS).isEmpty()) {
            List<String> stringValues = detailsSearchDto.getStatusFilter().get(STATUS);
            query.add(in(STATUS, stringValues.toArray(new String[] {})));
        }

        addEndDateToQuery(query, detailsSearchDto.getDateInterval());

        if(detailsSearchDto.getAdvancedSearch() != null) {
            AccessionRegisterDetailsSearchStatsDto.AdvancedSearchData advancedSearch = detailsSearchDto.getAdvancedSearch();

            addQueryFrom(query, advancedSearch.getOriginatingAgencies(), ORIGINATING_AGENCY);
            addQueryFrom(query, advancedSearch.getArchivalAgreements(), ARCHIVAL_AGREEMENT);
            addQueryFrom(query, advancedSearch.getArchivalProfiles(), ARCHIVAL_PROFILE);

            addAcquisitionInformationsToQuery(query, advancedSearch);

            addEventOpTypeQuery(query, advancedSearch.getElimination(), ELIMINATION);
            addEventOpTypeQuery(query, advancedSearch.getElimination(), TRANSFER);

        }

        if(!query.getQueries().isEmpty()) {
            select.setQuery(query);
        }

        return select.getFinalSelect();
    }

    private static void addAcquisitionInformationsToQuery(BooleanQuery query,
        AccessionRegisterDetailsSearchStatsDto.AdvancedSearchData advancedSearch)
        throws InvalidCreateOperationException {
        List<String> acquisitionInformationsFromIhm = advancedSearch.getAcquisitionInformations();
        if(CollectionUtils.isNotEmpty(acquisitionInformationsFromIhm)) {
            List<String> acquisitionInformations = new ArrayList<>(VitamQueryHelper.staticAcquisitionInformations);
            acquisitionInformations.removeAll(acquisitionInformationsFromIhm);
            if(!acquisitionInformations.isEmpty()) {
                if(acquisitionInformationsFromIhm.contains(VitamQueryHelper.ACQUISITION_INFORMATION_NON_RENSEIGNE) ) {
                    query.add(nin(ACQUISITION_INFORMATION, acquisitionInformations.toArray(new String[] {})));
                } else {
                    query.add(in(ACQUISITION_INFORMATION, acquisitionInformationsFromIhm.toArray(new String[] {})));
                }
            }
        }
    }

    private static void addEndDateToQuery(BooleanQuery query, AccessionRegisterDetailsSearchStatsDto.EndDateInterval dateInterval)
        throws InvalidCreateOperationException {

        if(dateInterval != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneOffset.UTC);
            String dateMinStr = dateInterval.getEndDateMin();
            String dateMaxStr = dateInterval.getEndDateMax();

            if (dateMinStr != null && dateMaxStr == null) {
                query.add(range(END_DATE, LocalDate.parse(dateMinStr, dtf).toString(), true, LocalDate.now().toString(), true));
            }

            if (dateMinStr == null && dateMaxStr != null) {
                query.add(range(END_DATE, LocalDate.now().toString(), true, LocalDate.parse(dateMaxStr, dtf).toString(), true));
            }

            if (dateMinStr != null && dateMaxStr != null) {
                query.add(range(END_DATE, LocalDate.parse(dateMinStr, dtf).toString(), true, LocalDate.parse(dateMaxStr, dtf).toString(), true));
            }
        }
    }

    private static void addQueryFrom(BooleanQuery query, List<String> values, String searchKeyUpperCase)
        throws InvalidCreateOperationException {
        if(CollectionUtils.isNotEmpty(values)) {
            query.add(in(searchKeyUpperCase, values.toArray(new String[] {})));
        }
    }

    private static void addEventOpTypeQuery(BooleanQuery query, String value, String searchKeyUpperCase)
        throws InvalidCreateOperationException {
        if(value != null && !value.equals("all")) {
            boolean cond = Boolean.parseBoolean(value);
            if(cond) {
                query.add(eq(EVENTS_OPTYPE, searchKeyUpperCase));
            } else {
                query.add(ne(EVENTS_OPTYPE, searchKeyUpperCase));
            }
        }
    }

    public static JsonNode buildCustomAccessionRegisterSummariesQuery(List<String> distinctOriginatingAgencies)
        throws InvalidCreateOperationException {
        final Select select = new Select();

        select.setQuery(in(ORIGINATING_AGENCY, distinctOriginatingAgencies.toArray(new String[0])));

        Map<String, Integer> projection = new HashMap<>();
        projection.put(VitamFieldsHelper.id(), 1);
        projection.put(AccessionRegisterDetailModel.TOTAL_OBJECT_GROUPS, 1);
        projection.put(AccessionRegisterDetailModel.TOTAL_UNITS, 1);
        projection.put(AccessionRegisterDetailModel.TOTAL_OBJECTS, 1);
        projection.put(AccessionRegisterDetailModel.OBJECT_SIZE, 1);

        QueryProjection queryProjection = new QueryProjection();
        queryProjection.setFields(projection);
        try {
            select.setProjection(JsonHandler.toJsonNode(queryProjection));
        } catch (InvalidParseOperationException e) {
            LOGGER.error("Error constructing vitam query");
            throw new InvalidCreateOperationException("Error constructing vitam query", e);
        }
        return select.getFinalSelect();
    }

}
