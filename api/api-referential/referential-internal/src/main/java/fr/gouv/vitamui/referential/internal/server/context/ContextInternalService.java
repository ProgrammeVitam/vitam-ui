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
package fr.gouv.vitamui.referential.internal.server.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.access.external.api.AdminCollections;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.action.UpdateActionHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.database.builder.request.single.Update;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.ContextModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.ContextResponseDto;
import fr.gouv.vitamui.referential.common.dto.PermissionDto;
import fr.gouv.vitamui.referential.common.service.VitamContextService;

@Service
public class ContextInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ContextInternalService.class);

    private VitamContextService vitamContextService;

    private ObjectMapper objectMapper;

    private ContextConverter converter;

    private LogbookService logbookService;

    @Autowired
    public ContextInternalService(VitamContextService vitamContextService, ObjectMapper objectMapper, ContextConverter converter,LogbookService logbookService) {
        this.vitamContextService = vitamContextService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
    }

    public ContextDto getOne(VitamContext vitamContext, String identifier) {
        try {
            LOGGER.info("Context EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            RequestResponse<ContextModel> requestResponse = vitamContextService.findContextById(vitamContext, identifier);
            final ContextResponseDto contextResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), ContextResponseDto.class);
            if(contextResponseDto.getResults().size() == 0){
                return null;
            }else {
                return converter.convertVitamToDto(contextResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Context", e);
        }
    }

    public List<ContextDto> getAll(VitamContext vitamContext) {
        final RequestResponse<ContextModel> requestResponse;
        try {
            LOGGER.info("All Contexts EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            requestResponse = vitamContextService
                    .findContexts(vitamContext, new Select().getFinalSelect());
            final ContextResponseDto contextResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), ContextResponseDto.class);

            return converter.convertVitamsToDtos(contextResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Contexts", e);
        }
    }

    public PaginatedValuesDto<ContextDto> getAllPaginated(final Integer pageNumber, final Integer size,
            final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
            Optional<String> criteria) {

        LOGGER.info("All Contexts EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query = null;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated contexts", ioe);
        } catch ( IOException e ) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        LOGGER.debug("Query: {}", query);

        ContextResponseDto results = this.findAll(vitamContext, query);

        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();
        LOGGER.debug("Contexts: {}", results.getResults());

        final List<ContextDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        LOGGER.debug("Contexts: {}", valuesDto);


        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    public ContextResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<ContextModel> requestResponse;
        try {
            LOGGER.info("All Contexts EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            requestResponse = vitamContextService.findContexts(vitamContext, query);
            LOGGER.debug("Response: {}", requestResponse);
            LOGGER.debug("Response DTO: {}", objectMapper.treeToValue(requestResponse.toJsonNode(), ContextResponseDto.class));

            return objectMapper.treeToValue(requestResponse.toJsonNode(), ContextResponseDto.class);

        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Can't find contexts", e);
        }
    }

    public Boolean check(VitamContext vitamContext, ContextDto contextDto) {
        List<ContextDto> contextDtoList = new ArrayList<>();
        contextDtoList.add(contextDto);
        LOGGER.info("Context Check EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        try {
            return !vitamContextService
                    .checkAbilityToCreateContextInVitam(
                            converter.convertDtosToVitams(contextDtoList),
                            vitamContext);
        } catch (ConflictException e) {
            return true;
        }catch (VitamUIException e) {
            throw new InternalServerException("Unable to check context", e);
        }
    }

    public ContextDto create(VitamContext vitamContext, ContextDto contextDto) {
        try {
            LOGGER.info("Create Context EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            vitamContextService.createContext(vitamContext, contextDto);
            return contextDto;
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Can't create context", e);
        }
    }

    // FIXME ? Do an automatic transformation without passing from Map<lowerCase, Value> to vitamUIDto to vitamDto to Map<UpperCase, Value>
    private ObjectNode convertPartialDtoToUpperCaseVitamFields(ContextDto partialDto) throws InvalidParseOperationException {
        ObjectNode propertiesToUpdate = JsonHandler.createObjectNode();

        // Transform Vitam-UI fields into Vitam fields
        if (partialDto.getStatus() != null) {
            propertiesToUpdate.put("Status", partialDto.getStatus());
        }
        if (partialDto.getCreationDate() != null) {
            propertiesToUpdate.put("CreationDate", partialDto.getCreationDate());
        }
        if (partialDto.getLastUpdate() != null) {
            propertiesToUpdate.put("LastUpdate", partialDto.getLastUpdate());
        }
        if (partialDto.getActivationDate() != null) {
            propertiesToUpdate.put("ActivationDate", partialDto.getActivationDate());
        }
        if (partialDto.getDeactivationDate() != null) {
            propertiesToUpdate.put("DeactivationDate", partialDto.getDeactivationDate());
        }
        if (partialDto.getEnableControl() != null) {
            propertiesToUpdate.put("EnableControl", partialDto.getEnableControl());
        }
        if (partialDto.getSecurityProfile() != null) {
            propertiesToUpdate.put("SecurityProfile", partialDto.getSecurityProfile());
        }
        if (partialDto.getPermissions() != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (PermissionDto value: partialDto.getPermissions()) {
                JsonNode node = JsonHandler.toJsonNode(value);
                ObjectNode permission = JsonHandler.createObjectNode();
                permission.set("tenant", new IntNode(Integer.valueOf(node.get("tenant").textValue())));
                permission.set("AccessContracts", node.get("accessContracts"));
                permission.set("IngestContracts", node.get("ingestContracts"));
                array.add(permission);
            }
            propertiesToUpdate.set("Permissions", array);
        }

        return propertiesToUpdate;
    }

    public ContextDto patch(VitamContext vitamContext,final ContextDto partialDto) {
        String id = partialDto.getIdentifier();
        if (id == null) {
            throw new BadRequestException("id must be one the the update criteria");
        }

        try {
            LOGGER.info("Patch Context EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            JsonNode fieldsUpdated = convertPartialDtoToUpperCaseVitamFields(partialDto);
            ObjectNode action = JsonHandler.createObjectNode();
            action.set("$set", fieldsUpdated);
            ArrayNode actions = JsonHandler.createArrayNode();
            actions.add(action);
            ObjectNode query = JsonHandler.createObjectNode();
            query.set("$action", actions);
            RequestResponse<?> requestResponse =  vitamContextService.patchContext(vitamContext, id, query);
            final ContextModel contextVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), ContextModel.class);
            return converter.convertVitamToDto(contextVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | JsonProcessingException e) {
            throw new InternalServerException("Can't patch the context", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String identifier) throws VitamClientException {
        LOGGER.debug("findHistoryById for identifier" + identifier);
        LOGGER.info("Find Context History EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        return logbookService.findEventsByIdentifierAndCollectionNames(
                identifier, AdminCollections.ACCESS_CONTRACTS.getName(), vitamContext).toJsonNode();
    }

}
