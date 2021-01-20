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
package fr.gouv.vitamui.referential.internal.server.agency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.common.dto.AgencyResponseDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.common.service.VitamAgencyService;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


@Service
public class AgencyInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AgencyInternalService.class);

    private AgencyService agencyService;

    private ObjectMapper objectMapper;

    private AgencyConverter converter;

    private LogbookService logbookService;

    private VitamAgencyService vitamAgencyService;

    @Autowired
    public AgencyInternalService(AgencyService agencyService, ObjectMapper objectMapper, AgencyConverter converter,
            LogbookService logbookService, VitamAgencyService vitamAgencyService) {
        this.agencyService = agencyService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
        this.vitamAgencyService = vitamAgencyService;
    }

    public AgencyDto getOne(VitamContext vitamContext, String identifier) {
        try {
              RequestResponse<AgenciesModel> requestResponse = agencyService.findAgencyById(vitamContext, identifier);
            final AgencyResponseDto accessContractResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class);
            if(accessContractResponseDto.getResults().size() == 0){
                return null;
            } else {
                return converter.convertVitamToDto(accessContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Agency", e);
        }
    }

    public List<AgencyDto> getAll(VitamContext vitamContext) {
        final RequestResponse<AgenciesModel> requestResponse;
        try {
            requestResponse = agencyService.findAgencies(vitamContext, new Select().getFinalSelect());
            final AgencyResponseDto agencyResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class);

            return converter.convertVitamsToDtos(agencyResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find agencies", e);
        }
    }

    public PaginatedValuesDto<AgencyDto> getAllPaginated(final Integer pageNumber, final Integer size,
            final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
            Optional<String> criteria) {
        if(vitamContext != null) {
            LOGGER.info("All Agencies EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        }

        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find agencies with pagination", ioe);
        } catch ( IOException e ) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        AgencyResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<AgencyDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    private AgencyResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<AgenciesModel> requestResponse;
        try {
            requestResponse = agencyService.findAgencies(vitamContext, query);

            final AgencyResponseDto agencyResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class);

            return agencyResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find agencies", e);
        }
    }

    public Boolean check(VitamContext vitamContext, AgencyDto agencyDto) {
        try {

            Integer agencyCheckedTenant = vitamAgencyService.checkAbilityToCreateAgencyInVitam(converter.convertDtosToVitams(Arrays.asList(agencyDto)), vitamContext.getApplicationSessionId());
            return !vitamContext.getTenantId().equals(agencyCheckedTenant);
        } catch (ConflictException e) {
            return true;
        }catch (VitamUIException e) {
            throw new InternalServerException("Unable to check agency", e);
        }
    }

    public AgencyDto create(VitamContext vitamContext, AgencyDto accessContractDto) {
        try {
            RequestResponse<?> requestResponse = vitamAgencyService.create(vitamContext, converter.convertDtoToVitam(accessContractDto));
            final AgencyModelDto accessContractVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AgencyModelDto.class);
            return converter.convertVitamToDto(accessContractVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException | VitamClientException e) {
            throw new InternalServerException("Unable to create agency", e);
        }
    }

    public AgencyDto patch(VitamContext vitamContext,final Map<String, Object> partialDto){
        final AgencyDto accessContractDto = this.getOne(vitamContext, (String) partialDto.get("identifier"));

        partialDto.forEach((key,value) ->
        {
            if (!"id".equals(key)) {
                try {
                    BeanUtilsBean.getInstance().copyProperty(accessContractDto, key, value);
                } catch (InvocationTargetException | IllegalAccessException e) {
                   	LOGGER.warn(e.getMessage());
                }
            }
        });
        AgencyModelDto accessContractVitam = converter.convertDtoToVitam(accessContractDto);
        try {
            RequestResponse<?> requestResponse = vitamAgencyService.patchAgency(vitamContext, (String) partialDto.get("id"), accessContractVitam);
            final AgencyModelDto accessContractVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AgencyModelDto.class);
            return converter.convertVitamToDto(accessContractVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e) {
            throw new InternalServerException("Unable to patch agency", e);
        }
    }

    public boolean delete(VitamContext context, String id) {
        try {
            
            return vitamAgencyService.deleteAgency(context, id);
        } catch (InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e) {
            throw new InternalServerException("Unable to delete agency", e);
        }
    }

    public Response export(VitamContext context) {
        try {
            return vitamAgencyService.export(context);
        } catch (InvalidParseOperationException | InvalidCreateOperationException | VitamClientException e) {
            throw new InternalServerException("Unable to export agencies", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String id) throws VitamClientException {
        try {
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id),vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
        	throw new InternalServerException("Unable to fetch history", e);
        }
    }

    public JsonNode importAgencies(VitamContext context, String fileName, MultipartFile file) {
        try {
            return vitamAgencyService.importAgencies(context, fileName, file).toJsonNode();
        } catch (InvalidParseOperationException |AccessExternalClientException |VitamClientException | IOException e) {
            LOGGER.error("Unable to import agency file {}: {}", fileName, e.getMessage());
            throw new InternalServerException("Unable to import agency file " + fileName + " : ", e);
        }
    }

}
