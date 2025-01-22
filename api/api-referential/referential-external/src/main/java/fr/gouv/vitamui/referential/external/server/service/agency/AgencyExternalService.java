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
package fr.gouv.vitamui.referential.external.server.service.agency;

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
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.common.dto.AgencyResponseDto;
import fr.gouv.vitamui.referential.common.service.VitamAgencyService;
import fr.gouv.vitamui.referential.common.utils.AgencyConverter;
import fr.gouv.vitamui.referential.external.server.service.AbstractService;
import fr.gouv.vitamui.referential.external.server.service.utils.ExportCSVUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.referential.common.utils.AgencyConverter.*;

@Service
public class AgencyExternalService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgencyExternalService.class);

    private AgencyService agencyService;

    private ObjectMapper objectMapper;

    private LogbookService logbookService;

    private VitamAgencyService vitamAgencyService;

    @Autowired
    public AgencyExternalService(
        AgencyService agencyService,
        ObjectMapper objectMapper,
        LogbookService logbookService,
        VitamAgencyService vitamAgencyService,
        ExternalSecurityService externalSecurityService
    ) {
        super(externalSecurityService);
        this.agencyService = agencyService;
        this.objectMapper = objectMapper;
        this.logbookService = logbookService;
        this.vitamAgencyService = vitamAgencyService;
    }

    public AgencyDto getOne(VitamContext vitamContext, String identifier) {
        try {
            RequestResponse<AgenciesModel> requestResponse = agencyService.findAgencyById(vitamContext, identifier);
            final AgencyResponseDto agencyResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                AgencyResponseDto.class
            );
            if (agencyResponseDto.getResults().isEmpty()) {
                return null;
            } else {
                return convertVitamToDto(agencyResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Agency", e);
        }
    }

    public AgencyDto getOne(String id) {
        final VitamContext vitamContext = this.buildVitamContext();

        return this.getOne(vitamContext, id);
    }

    public List<AgencyDto> getAll(VitamContext vitamContext) {
        final RequestResponse<AgenciesModel> requestResponse;
        try {
            requestResponse = agencyService.findAgencies(vitamContext, new Select().getFinalSelect());
            final AgencyResponseDto agencyResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                AgencyResponseDto.class
            );

            return convertVitamsToDtos(agencyResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find agencies", e);
        }
    }

    public List<AgencyDto> getAll(final Optional<String> criteria) {
        final VitamContext vitamContext = this.buildVitamContext();

        return this.getAll(vitamContext);
    }

    public PaginatedValuesDto<AgencyDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        VitamContext vitamContext,
        Optional<String> criteria
    ) {
        if (vitamContext != null) {
            LOGGER.info("All Agencies EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        }

        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find agencies with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        AgencyResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<AgencyDto> valuesDto = convertVitamsToDtos(results.getResults());
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    public PaginatedValuesDto<AgencyDto> getAllPaginated(
        final Integer page,
        final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction
    ) {
        ParameterChecker.checkPagination(size, page);
        final VitamContext vitamContext = this.buildVitamContext();
        return this.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    private AgencyResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<AgenciesModel> requestResponse;
        try {
            requestResponse = agencyService.findAgencies(vitamContext, query);

            return objectMapper.treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class);
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find agencies", e);
        }
    }

    public Boolean check(VitamContext vitamContext, AgencyDto agencyDto) {
        try {
            Integer agencyCheckedTenant = vitamAgencyService.checkAbilityToCreateAgencyInVitam(
                AgencyConverter.convertDtosToVitams(Collections.singletonList(agencyDto)),
                vitamContext.getApplicationSessionId()
            );
            return !vitamContext.getTenantId().equals(agencyCheckedTenant);
        } catch (ConflictException e) {
            return true;
        } catch (VitamUIException e) {
            throw new InternalServerException("Unable to check agency", e);
        }
    }

    public boolean check(AgencyDto agencyDto) {
        final VitamContext vitamContext = this.buildVitamContext();

        return this.check(vitamContext, agencyDto);
    }

    public AgencyDto create(VitamContext vitamContext, AgencyDto agencyDto) {
        try {
            RequestResponse<?> requestResponse = vitamAgencyService.create(vitamContext, convertDtoToVitam(agencyDto));
            final AgenciesModel agencyModelDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                AgenciesModel.class
            );
            return convertVitamToDto(agencyModelDto);
        } catch (
            InvalidParseOperationException | AccessExternalClientException | IOException | VitamClientException e
        ) {
            throw new InternalServerException("Unable to create agency", e);
        }
    }

    public AgencyDto create(final AgencyDto agencyDto) {
        final VitamContext vitamContext = this.buildVitamContext();

        return this.create(vitamContext, agencyDto);
    }

    public AgencyDto patch(VitamContext vitamContext, final AgencyDto agencyDto) {
        AgenciesModel agencyModelDto = convertDtoToVitam(agencyDto);
        try {
            vitamAgencyService.patchAgency(vitamContext, agencyDto.getId(), agencyModelDto);
            return agencyDto;
        } catch (
            InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e
        ) {
            throw new InternalServerException("Unable to patch agency", e);
        }
    }

    public AgencyDto patch(final Map<String, Object> partialDto) {
        final VitamContext vitamContext = this.buildVitamContext();

        return this.patch(vitamContext, objectMapper.convertValue(partialDto, AgencyDto.class));
    }

    public boolean delete(VitamContext context, String id) {
        try {
            return vitamAgencyService.deleteAgency(context, id);
        } catch (
            InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e
        ) {
            throw new InternalServerException("Unable to delete agency", e);
        }
    }

    public ResponseEntity<Boolean> deleteWithResponse(final String id) {
        final VitamContext vitamContext = this.buildVitamContext();

        return new ResponseEntity<Boolean>(this.delete(vitamContext, id), HttpStatus.OK);
    }

    public Response export(VitamContext context) {
        try {
            return vitamAgencyService.export(context);
        } catch (InvalidParseOperationException | InvalidCreateOperationException | VitamClientException e) {
            throw new InternalServerException("Unable to export agencies", e);
        }
    }

    public ResponseEntity<Resource> export() {
        final VitamContext vitamContext = this.buildVitamContext();

        Response response = this.export(vitamContext);
        Object entity = response.getEntity();

        if (entity instanceof InputStream) {
            var mergedBomCsvInputStream = new SequenceInputStream(
                new ByteArrayInputStream(ExportCSVUtils.BOM),
                (InputStream) entity
            );
            Resource resource = new InputStreamResource(mergedBomCsvInputStream);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return null;
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String id) throws VitamClientException {
        try {
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id), vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
            throw new InternalServerException("Unable to fetch history", e);
        }
    }

    public LogbookOperationsResponseDto findHistoryById(final String id) throws VitamClientException {
        final VitamContext vitamContext = this.buildVitamContext();

        final JsonNode body = this.findHistoryByIdentifier(vitamContext, id);
        try {
            return JsonUtils.treeToValue(body, LogbookOperationsResponseDto.class, false);
        } catch (final JsonProcessingException e) {
            throw new InternalServerException(VitamRestUtils.PARSING_ERROR_MSG, e);
        }
    }

    public JsonNode importAgencies(String fileName, MultipartFile file) {
        final VitamContext vitamContext = this.buildVitamContext();
        try {
            return vitamAgencyService.importAgencies(vitamContext, fileName, file).toJsonNode();
        } catch (
            InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e
        ) {
            LOGGER.error("Unable to import agency file {}: {}", fileName, e.getMessage());
            throw new InternalServerException("Unable to import agency file " + fileName + " : ", e);
        }
    }
}
