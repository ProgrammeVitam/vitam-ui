/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
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
 *
 *
 */
package fr.gouv.vitamui.referential.internal.server.managementcontract.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ManagementContractDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.ManagementContractService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.ManagementContractResponseDto;
import fr.gouv.vitamui.referential.common.dto.ManagementContractVitamDto;
import fr.gouv.vitamui.referential.common.service.VitamUIManagementContractService;
import fr.gouv.vitamui.referential.internal.server.managementcontract.ManagementContractDtoToModelConverter;
import fr.gouv.vitamui.referential.internal.server.managementcontract.ManagementContractModelToDtoConverter;
import fr.gouv.vitamui.referential.internal.server.managementcontract.PatchManagementContractModel;
import fr.gouv.vitamui.referential.internal.server.managementcontract.converter.ManagementContractConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ManagementContractInternalService {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ManagementContractInternalService.class);

    private static final String MANAGEMENT_CONTRACT_NOT_FOUND = "Unable to find Management Contracts";
    private static final String MANAGEMENT_CONTRACT_EVENT_ID_APP_SESSION = "Management Contracts EvIdAppSession : {} ";
    private static final String MANAGEMENT_CONTRACT_NOT_PATCH = "Can't patch management contract";

    private final ObjectMapper objectMapper;

    private final ManagementContractConverter converter;

    private final ManagementContractService managementContractService;

    private final VitamUIManagementContractService vitamUIManagementContractService;

    private final LogbookService logbookService;

    private ManagementContractDtoToModelConverter managementContractDtoToModelConverter;
    private ManagementContractModelToDtoConverter managementContractModelToDtoConverter;

    @Autowired
    public ManagementContractInternalService(
        final ManagementContractService managementContractService,
        final VitamUIManagementContractService vitamUIManagementContractService,
        final ObjectMapper objectMapper,
        final ManagementContractConverter converter,
        final LogbookService logbookService
    ) {
        this.managementContractService = managementContractService;
        this.vitamUIManagementContractService = vitamUIManagementContractService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
    }

    public List<ManagementContractDto> getAll(VitamContext vitamContext) {
        final RequestResponse<ManagementContractModel> requestResponse;
        try {
            LOGGER.debug(MANAGEMENT_CONTRACT_EVENT_ID_APP_SESSION, vitamContext.getApplicationSessionId());
            requestResponse =
                managementContractService.findManagementContracts(vitamContext, new Select().getFinalSelect());
            final ManagementContractResponseDto managementContractResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), ManagementContractResponseDto.class);

            return converter.convertVitamListMgtContractToVitamUIMgtContractDtos(
                managementContractResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            LOGGER.error(MANAGEMENT_CONTRACT_NOT_FOUND + e.getMessage());
            throw new InternalServerException(MANAGEMENT_CONTRACT_NOT_FOUND, e);
        }
    }

    public ManagementContractDto getOne(VitamContext vitamContext, String identifier) {
        try {
            LOGGER.debug(MANAGEMENT_CONTRACT_EVENT_ID_APP_SESSION, vitamContext.getApplicationSessionId());
            RequestResponse<ManagementContractModel> requestResponse =
                managementContractService.findManagementContractById(vitamContext, identifier);
            final ManagementContractResponseDto managementContractResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), ManagementContractResponseDto.class);

            return managementContractResponseDto.getResults().isEmpty() ? null
                : converter.convertVitamMgtContractToVitamUiDto(managementContractResponseDto.getResults().get(0));
        } catch (VitamClientException | JsonProcessingException exception) {
            LOGGER.error("Unable to get Management Contract");
            throw new InternalServerException("Unable to get Management Contract", exception);
        }
    }

    public PaginatedValuesDto<ManagementContractDto> getAllPaginated(final Integer pageNumber, final Integer size,
        final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
        Optional<String> criteria) {
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        LOGGER.debug(MANAGEMENT_CONTRACT_EVENT_ID_APP_SESSION, vitamContext.getApplicationSessionId());
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {
                };
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }
            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            LOGGER.error("Can't create dsl query to get paginated management contracts");
            throw new InternalServerException("Can't create dsl query to get paginated management contracts", ioe);
        } catch (IOException e) {
            LOGGER.error("Can't parse criteria as Vitam query");
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        ManagementContractResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<ManagementContractDto> valuesDto = converter.
            convertVitamListMgtContractToVitamUIMgtContractDtos(results.getResults());
        LOGGER.debug("Vitam UI DTO: {}", valuesDto);
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    public ManagementContractResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<ManagementContractModel> requestResponse;
        try {
            LOGGER.debug(MANAGEMENT_CONTRACT_EVENT_ID_APP_SESSION, vitamContext.getApplicationSessionId());
            requestResponse = managementContractService.findManagementContracts(vitamContext, query);
            final ManagementContractResponseDto managementContractResponseDto = objectMapper
                .convertValue(requestResponse.toJsonNode(), ManagementContractResponseDto.class);
            LOGGER.debug("VITAM DTO: {}", managementContractResponseDto);
            return managementContractResponseDto;
        } catch (VitamClientException exception) {
            LOGGER.error(MANAGEMENT_CONTRACT_NOT_FOUND + exception);
            throw new InternalServerException(MANAGEMENT_CONTRACT_NOT_FOUND, exception);
        }
    }

    public Boolean check(VitamContext vitamContext, ManagementContractDto managementContractDto) {
        try {
            LOGGER.debug(MANAGEMENT_CONTRACT_EVENT_ID_APP_SESSION, vitamContext.getApplicationSessionId());
            Integer managementContractCheckedTenant =
                managementContractService.checkAbilityToCreateManagementContractInVitam(converter
                        .convertVitamUiListMgtContractToVitamListMgtContract(
                            Collections.singletonList(managementContractDto)),
                    vitamContext.getApplicationSessionId());
            return !vitamContext.getTenantId().equals(managementContractCheckedTenant);
        } catch (ConflictException e) {
            LOGGER.error("Error while checking management Contract", e.getMessage());
            return true;
        }
    }

    public ManagementContractDto create(VitamContext vitamContext, ManagementContractDto managementContractDto) {
        try {
            LOGGER.debug(MANAGEMENT_CONTRACT_EVENT_ID_APP_SESSION, vitamContext.getApplicationSessionId());
            RequestResponse requestResponse = managementContractService.createManagementContracts(vitamContext,
                Collections.singletonList(converter
                    .convertVitamUiManagementContractToVitamMgt(managementContractDto)));
            final ManagementContractVitamDto managementContractModelDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), ManagementContractVitamDto.class);
            return converter
                .convertVitamMgtContractToVitamUiDto(managementContractModelDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            LOGGER.error("Can't create management contract");
            throw new InternalServerException("Can't create management contract", e);
        }
    }

    public ManagementContractDto patch(VitamContext vitamContext, final ManagementContractDto managementContractDto)
        throws AccessExternalClientException, InvalidParseOperationException, JsonProcessingException {
        final String identifier = managementContractDto.getIdentifier();
        final ManagementContractModel managementContractModel =
            managementContractDtoToModelConverter.convert(managementContractDto);
        final String serializedManagementContractModel = objectMapper.writeValueAsString(managementContractModel);
        final PatchManagementContractModel patchManagementContractModel =
            objectMapper.readValue(serializedManagementContractModel, PatchManagementContractModel.class);
        final JsonNode patchQuery = buildPatchQuery(patchManagementContractModel);
        final RequestResponse<?> requestResponse =
            vitamUIManagementContractService.patchManagementContract(vitamContext, identifier, patchQuery);
        if (Response.Status.OK.getStatusCode() != requestResponse.getHttpCode()) {
            throw new AccessExternalClientException(MANAGEMENT_CONTRACT_NOT_PATCH);
        }

        return getOne(vitamContext, identifier);
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String id) throws VitamClientException {
        try {
            LOGGER.debug(MANAGEMENT_CONTRACT_EVENT_ID_APP_SESSION, vitamContext.getApplicationSessionId());
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id), vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
            LOGGER.error("Unable to fetch history");
            throw new InternalServerException("Unable to fetch history", e);
        }
    }

    @Autowired
    public void setManagementContractDtoToModelConverter(
        ManagementContractDtoToModelConverter managementContractDtoToModelConverter) {
        this.managementContractDtoToModelConverter = managementContractDtoToModelConverter;
    }

    @Autowired
    public void setManagementContractModelToDtoConverter(
        ManagementContractModelToDtoConverter managementContractModelToDtoConverter) {
        this.managementContractModelToDtoConverter = managementContractModelToDtoConverter;
    }

    private JsonNode buildPatchQuery(PatchManagementContractModel patchManagementContractModel) {
        final JsonNode jsonNode = objectMapper.valueToTree(patchManagementContractModel);
        final ObjectNode action = JsonHandler.createObjectNode();
        action.set("$set", jsonNode);

        final ArrayNode actions = JsonHandler.createArrayNode();
        actions.add(action);

        final ObjectNode query = JsonHandler.createObjectNode();
        query.set("$action", actions);

        return query;
    }
}
