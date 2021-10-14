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
package fr.gouv.vitamui.referential.internal.server.accessionregister.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.QueryProjection;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessionRegisterDetailModel;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
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
import fr.gouv.vitamui.referential.common.dto.AgencyResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AccessionRegisterDetailInternalService {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(AccessionRegisterDetailInternalService.class);

    private final ObjectMapper objectMapper;

    private final AgencyService agencyService;

    private final AdminExternalClient adminExternalClient;

    @Autowired
    public AccessionRegisterDetailInternalService(ObjectMapper objectMapper, AdminExternalClient adminExternalClient,
        AgencyService agencyService) {
        this.objectMapper = objectMapper;
        this.agencyService = agencyService;
        this.adminExternalClient = adminExternalClient;
    }

    public PaginatedValuesDto<AccessionRegisterDetailDto> getAllPaginated(final Integer pageNumber, final Integer size,
        final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
        Optional<String> criteria) {

        //Constructing json query for Vitam
        JsonNode query = buildAllPaginatedJsonQuery(pageNumber, size, orderBy, direction, vitamContext, criteria);

        //Fetching data from vitam
        AccessionRegisterDetailResponseDto results = fetchingAllPaginatedDataFromVitam(vitamContext, query);
        LOGGER.info("Fetched accession register data : {} ", results);

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
            LOGGER.info("List of Accession Registers EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            if (criteria.isPresent()) {
                vitamCriteria = objectMapper.readValue(criteria.get(), new TypeReference<HashMap<String, Object>>() {});
            }
            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
            LOGGER.info("jsonQuery: {}", query);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated accession registers", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't read value from criteria entries", e);
        }
        return query;
    }

    private AccessionRegisterDetailResponseDto fetchingAllPaginatedDataFromVitam(VitamContext vitamContext, JsonNode query) {
        AccessionRegisterDetailResponseDto results;
        try {
            RequestResponse<AccessionRegisterDetailModel> accessionRegisterDetails = adminExternalClient.findAccessionRegisterDetails(vitamContext, query);
            results = objectMapper.treeToValue(accessionRegisterDetails.toJsonNode(), AccessionRegisterDetailResponseDto.class);
        } catch (VitamClientException e) {
            throw new InternalServerException("Can't fetch data from VITAM", e);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Can't process Json Parsing", e);
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
            throw new InternalServerException("Error parsing query", e);
        } catch (VitamClientException e) {
            throw new InternalServerException("Error fetching agencies from vitam", e);
        } catch (InvalidCreateOperationException e) {
            throw new InternalServerException("Invalid Select vitam query", e);
        }

        return agencies.stream()
            .collect(Collectors.toMap(AgencyModelDto::getIdentifier, AgencyModelDto::getName));
    }

    private JsonNode buildOriginatingAgencyProjectionQuery(AccessionRegisterDetailResponseDto results)
        throws InvalidCreateOperationException {

        List<String> distinctOriginatingAgencies = new ArrayList<>();
        if (results != null) {
            distinctOriginatingAgencies = results.getResults().stream()
                .map(AccessionRegisterDetailModel::getOriginatingAgency)
                .filter(Objects::nonNull)
                .filter(originatingAgency -> ConcurrentHashMap.newKeySet().add(originatingAgency))
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
            throw new InvalidCreateOperationException("Invalid vitam query", e);
        }
        LOGGER.debug("agencies query: {}", select.getFinalSelect());
        return select.getFinalSelect();
    }

}
