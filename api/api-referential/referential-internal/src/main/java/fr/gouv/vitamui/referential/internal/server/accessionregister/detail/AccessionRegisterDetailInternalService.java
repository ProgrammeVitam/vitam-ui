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
package fr.gouv.vitamui.referential.internal.server.accessionregister.detail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientNotFoundException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientServerException;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
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
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.administration.RegisterValueDetailModel;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailResponseDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterStatsDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryResponseDto;
import fr.gouv.vitamui.referential.common.dto.AgencyResponseDto;
import fr.gouv.vitamui.referential.common.service.AccessionRegisterService;
import fr.gouv.vitamui.referential.internal.server.accessionregister.converters.AccessionRegisterConverter;
import fr.gouv.vitamui.referential.internal.server.accessionregister.converters.AccessionRegisterDetailConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class AccessionRegisterDetailInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessionRegisterDetailInternalService.class);

    private final AccessionRegisterService accessionRegisterService;

    private final ObjectMapper objectMapper;

    private final AgencyService agencyService;

    private final AdminExternalClient adminExternalClient;

    private static final String MOCKED_DATA = "accession-register-details-mocked.json";

    @Autowired
    AccessionRegisterDetailInternalService(AccessionRegisterService accessionRegisterService, ObjectMapper objectMapper,
        AdminExternalClient adminExternalClient,
        AgencyService agencyService) {
        this.accessionRegisterService = accessionRegisterService;
        this.objectMapper = objectMapper;
        this.agencyService = agencyService;
        this.adminExternalClient = adminExternalClient;
    }


    public PaginatedValuesDto<AccessionRegisterDetailDto> getAllPaginated(final Integer pageNumber, final Integer size,
        final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
        Optional<String> criteria) {
        // FIXME get accessContract from vitamContext
//        vitamContext.setAccessContract("ContratTNR");
        //Constructing json query for Vitam
        JsonNode query = buildAllPaginatedJsonQuery(pageNumber, size, orderBy, direction, vitamContext, criteria);

        //Fetching data from vitam
        AccessionRegisterDetailResponseDto results = fetchingAllPaginatedDataFromVitam(vitamContext, query);

        //Fetch agencies to complete return Dto 'originatingAgencyLabel' property
        Map<String, String> agenciesMap = findAgencies(vitamContext, results);

        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();
        List<AccessionRegisterDetailDto> valuesDto = AccessionRegisterDetailConverter.convertVitamsToDtos(results.getResults());
        valuesDto.forEach(value -> value.setOriginatingAgencyLabel(agenciesMap.get(value.getOriginatingAgency())));

        return new PaginatedValuesDto<>(valuesDto, pageNumber, size, hasMore);
    }

    private JsonNode buildAllPaginatedJsonQuery(Integer pageNumber, Integer size, Optional<String> orderBy,
        Optional<DirectionDto> direction, VitamContext vitamContext, Optional<String> criteria) {
        JsonNode query;
        try {
            Map<String, Object> vitamCriteria = new HashMap<>();
            LOGGER.info("List of Accession Registers EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            if (criteria.isPresent()) {
                vitamCriteria = objectMapper.readValue(criteria.get(), new TypeReference<HashMap<String, Object>>() {});
            }
            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
            LOGGER.debug("jsonQuery: {}", query);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated accession registers ", ioe);
        } catch ( IOException e ) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }
        return query;
    }

    private AccessionRegisterDetailResponseDto fetchingAllPaginatedDataFromVitam(VitamContext vitamContext, JsonNode query) {
        AccessionRegisterDetailResponseDto results;
        // results = this.findAllDetails(vitamContext, query);
        try {
            RequestResponse<AccessionRegisterDetailModel> accessionRegisterDetails = adminExternalClient.findAccessionRegisterDetails(vitamContext, query);
            results = objectMapper.treeToValue(accessionRegisterDetails.toJsonNode(), AccessionRegisterDetailResponseDto.class);
        } catch (VitamClientException e) {
            throw new InternalServerException("Can't fetch data from VITAM ", e);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Can't VITAM ", e);
        }
        return results;
    }

    private Map<String, String> findAgencies(VitamContext vitamContext, AccessionRegisterDetailResponseDto results) {

        JsonNode originatingAgencyQuery;
        List<AgencyModelDto> agencies;
        try {
            originatingAgencyQuery = buildOriginatingAgencyProjectionQuery(results);
            RequestResponse<AgenciesModel> requestResponse = agencyService.findAgencies(vitamContext, originatingAgencyQuery);
            agencies = objectMapper.treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class).getResults();
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Error parsing query ", e);
        } catch (VitamClientException e) {
            throw new InternalServerException("Error fetching agencies from vitam ", e);
        } catch (InvalidCreateOperationException e) {
            throw new InternalServerException("Invalid Select vitam query ", e);
        }

        return agencies.stream()
            .collect(Collectors.toMap(AgencyModelDto::getIdentifier, AgencyModelDto::getName));
    }

    public JsonNode buildOriginatingAgencyProjectionQuery(AccessionRegisterDetailResponseDto results) throws InvalidCreateOperationException {

        List<String> distinctOriginatingAgencies = new ArrayList<>();
        if(results != null) {
            distinctOriginatingAgencies = results
                .getResults()
                .stream()
                .filter(distinctByKey(AccessionRegisterDetailModel::getOriginatingAgency))
                .map(AccessionRegisterDetailModel::getOriginatingAgency)
                .collect(Collectors.toList());
        }

        final Select select = new Select();

        select.setQuery(QueryHelper.in("Identifier", distinctOriginatingAgencies.toArray(new String[0])));
        Map<String, Integer> projection = new HashMap<>();
        projection.put("Identifier", 1);
        projection.put("Name", 1);

        QueryProjection queryProjection = new QueryProjection();
        queryProjection.setFields(projection);
        try {
            select.setProjection(JsonHandler.toJsonNode(queryProjection));
        } catch (InvalidParseOperationException e) {
            LOGGER.error("Error constructing vitam query : {}", e);
            throw new InvalidCreateOperationException ("Invalid vitam query", e);
        }
        LOGGER.debug("agencies query: {}", select.getFinalSelect());
        return select.getFinalSelect();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> originatingAgency) {
        return t -> ConcurrentHashMap.newKeySet().add(originatingAgency.apply(t));
    }

//    public AccessionRegisterDetailResponseDto findAllDetails(VitamContext vitamContext, JsonNode query) {
//        LOGGER.info("List of Accession Register Details EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
//        return getMockedDataFromFile();
//    }

    public AccessionRegisterStatsDto getStats(VitamContext vitamContext) {

        List<AccessionRegisterSummaryDto> customAccessionRegisterSummaries =
            getCustomAccessionRegisterSummaries(vitamContext);

        long totalUnits = customAccessionRegisterSummaries.stream().parallel()
            .map(AccessionRegisterSummaryDto::getTotalUnits)
            .map(RegisterValueDetailModel::getRemained).mapToLong(Long::longValue).sum();

        long totalObjectsGroups = customAccessionRegisterSummaries.stream().parallel()
            .map(AccessionRegisterSummaryDto::getTotalObjectsGroups)
            .map(RegisterValueDetailModel::getRemained).mapToLong(Long::longValue).sum();

        long totalObjects = customAccessionRegisterSummaries.stream().parallel()
            .map(AccessionRegisterSummaryDto::getTotalObjects)
            .map(RegisterValueDetailModel::getRemained).mapToLong(Long::longValue).sum();

        long objectSizes = customAccessionRegisterSummaries.stream().parallel()
            .map(AccessionRegisterSummaryDto::getObjectSize)
            .map(RegisterValueDetailModel::getRemained).mapToLong(Long::longValue).sum();

        return new AccessionRegisterStatsDto(totalUnits, totalObjectsGroups,totalObjects,objectSizes);
    }


    public List<AccessionRegisterSummaryDto> getCustomAccessionRegisterSummaries(VitamContext context) {
        RequestResponse<AccessionRegisterSummaryModel> requestResponse;
        try {
            JsonNode query = buildCustomQuery();
            LOGGER.info("List of Access Register EvIdAppSession : {} " , context.getApplicationSessionId());
            requestResponse = accessionRegisterService.findAccessionRegisterSummary(context, query);
            final AccessionRegisterSummaryResponseDto accessionRegisterSummaryResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessionRegisterSummaryResponseDto.class);
            return AccessionRegisterConverter.convertVitamsToDtos(accessionRegisterSummaryResponseDto.getResults());
        } catch (JsonProcessingException | VitamClientException | InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new InternalServerException("Unable to find accessionRegister Summaries", e);
        }
    }

    private AccessionRegisterDetailResponseDto getMockedDataFromFile() {
        AccessionRegisterDetailResponseDto fromFile;
        try {
            final File file = PropertiesUtils.findFile(MOCKED_DATA);
            fromFile = JsonHandler
                .getFromFile(file, AccessionRegisterDetailResponseDto.class);
        } catch (InvalidParseOperationException | FileNotFoundException e) {
            throw new InternalServerException("Can't load mocked data ", e);
        }

        // hits is not loaded .. didn't analyze why !! so i set what i want
        fromFile.getHits().setOffset(0);
        fromFile.getHits().setTotal(100);
        fromFile.getHits().setSize(20);
        fromFile.getHits().setLimit(10000);
        return fromFile;
    }

    public AccessionRegisterDetailResponseDto getAccessionRegisterDetail(VitamContext context, String id) {
        RequestResponse<?> requestResponse;
        try {
            LOGGER.info("List of Access Register EvIdAppSession : {} " , context.getApplicationSessionId());
            requestResponse = accessionRegisterService.getAccessionRegisterDetail(context, id);
            final AccessionRegisterDetailResponseDto accessionRegisterSymbolicResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessionRegisterDetailResponseDto.class);
            return accessionRegisterSymbolicResponseDto;
            //AccessionRegisterDetailConverter.convertVitamToDtos(accessionRegisterSymbolicResponseDto.getResults());
        } catch (JsonProcessingException | InvalidParseOperationException | AccessExternalClientServerException | AccessExternalClientNotFoundException e) {
            throw new InternalServerException("Unable to find accessionRegisterSymbolic", e);
        }
    }

    public AccessionRegisterDetailResponseDto getAccessionRegisterDetailVitamModel(VitamContext context, String id) {
        RequestResponse<AccessionRegisterDetailModel> requestResponse;
        try {
            LOGGER.info("List of Access Register EvIdAppSession : {} " , context.getApplicationSessionId());
            requestResponse = accessionRegisterService.getAccessionRegisterDetail(context, id);
            final AccessionRegisterDetailResponseDto accessionRegisterSymbolicResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessionRegisterDetailResponseDto.class);
            return accessionRegisterSymbolicResponseDto;
            //AccessionRegisterDetailConverter.convertVitamToDtos(accessionRegisterSymbolicResponseDto.getResults());
        } catch (JsonProcessingException | InvalidParseOperationException | AccessExternalClientServerException | AccessExternalClientNotFoundException e) {
            throw new InternalServerException("Unable to find accessionRegisterSymbolic", e);
        }
    }

    public AccessionRegisterDetailDto getOneAccessionRegisterDetail(VitamContext context, String id) {
        RequestResponse<?> requestResponse;
        try {
            LOGGER.info("List of Access Register EvIdAppSession : {} " , context.getApplicationSessionId());
            requestResponse = accessionRegisterService.getAccessionRegisterDetail(context, id);
            final AccessionRegisterDetailResponseDto accessionRegisterSymbolicResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessionRegisterDetailResponseDto.class);
            return AccessionRegisterDetailConverter.convertVitamToDto(accessionRegisterSymbolicResponseDto.getResults().get(0));
        } catch (JsonProcessingException | InvalidParseOperationException | AccessExternalClientServerException | AccessExternalClientNotFoundException e) {
            throw new InternalServerException("Unable to find accessionRegisterSymbolic", e);
        }
    }

    public static JsonNode buildCustomQuery() throws InvalidCreateOperationException {
        final Select select = new Select();

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
            LOGGER.info("Error constructing vitam query");
            throw new InvalidCreateOperationException ("Error constructing vitam query", e);
        }
        return select.getFinalSelect();

    }
}
