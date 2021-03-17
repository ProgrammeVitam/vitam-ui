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
package fr.gouv.vitamui.referential.internal.server.accesscontract;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.AccessContractDto;
import fr.gouv.vitamui.referential.common.dto.AccessContractResponseDto;
import fr.gouv.vitamui.referential.common.dto.AccessContractVitamDto;
import fr.gouv.vitamui.referential.common.service.VitamUIAccessContractService;

@Service
public class AccessContractInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessContractInternalService.class);

    private AccessContractService accessContractService;

    private VitamUIAccessContractService vitamUIAccessContractService;

    private ObjectMapper objectMapper;

    private AccessContractConverter converter;

    private LogbookService logbookService;

    @Autowired
    public AccessContractInternalService(AccessContractService accessContractService, VitamUIAccessContractService vitamUIAccessContractService, ObjectMapper objectMapper, AccessContractConverter converter, LogbookService logbookService) {
        this.accessContractService = accessContractService;
        this.vitamUIAccessContractService=vitamUIAccessContractService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
    }

    public AccessContractDto getOne(VitamContext vitamContext, String identifier) {
        try {

            LOGGER.info("Access Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            RequestResponse<AccessContractModel> requestResponse = accessContractService.findAccessContractById(vitamContext, identifier);
            final AccessContractResponseDto accessContractResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class);
            if (accessContractResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(accessContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Access Contrat", e);
        }
    }

    public List<AccessContractDto> getAll(VitamContext vitamContext) {
        final RequestResponse<AccessContractModel> requestResponse;
        try {

            LOGGER.info("List of Access Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            requestResponse = accessContractService
                    .findAccessContracts(vitamContext, new Select().getFinalSelect());
            final AccessContractResponseDto accessContractResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class);

            return converter.convertVitamsToDtos(accessContractResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Access Contrats", e);
        }
    }

    public PaginatedValuesDto<AccessContractDto> getAllPaginated(final Integer pageNumber, final Integer size,
            final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
            Optional<String> criteria) {

        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query = null;
        try {
            LOGGER.info("List of Access Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
            LOGGER.debug("jsonQuery: {}", query);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated access contracts", ioe);
        } catch ( IOException e ) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        AccessContractResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<AccessContractDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    public AccessContractResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<AccessContractModel> requestResponse;
        try {
            LOGGER.info("List of Access Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            requestResponse = accessContractService.findAccessContracts(vitamContext, query);
            return objectMapper.treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class);

        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Can't find access contracts", e);
        }
    }

    public Boolean check(VitamContext vitamContext, AccessContractDto accessContractDto) {
        try {
            LOGGER.info("Access Contract Check EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            Integer accessContractCheckedTenant = accessContractService.checkAbilityToCreateAccessContractInVitam(converter.convertDtosToVitams(Arrays.asList(accessContractDto)), vitamContext.getApplicationSessionId());
            return !vitamContext.getTenantId().equals(accessContractCheckedTenant);
        } catch (ConflictException e) {
            return true;
        }
    }

    public AccessContractDto create(VitamContext vitamContext, AccessContractDto accessContractDto) {
        try {
            LOGGER.info("Create Access Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            RequestResponse requestResponse = accessContractService.createAccessContracts(vitamContext, converter.convertDtosToVitams(Arrays.asList(accessContractDto)));
            final AccessContractVitamDto accessContractVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AccessContractVitamDto.class);
            return converter.convertVitamToDto(accessContractVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Can't create access contract", e);
        }
    }

    // FIXME ? Do an automatic transformation without passing from Map<lowerCase, Value> to vitamUIDto to vitamDto to Map<UpperCase, Value>
    private JsonNode convertMapPartialDtoToUpperCaseVitamFields(Map<String, Object> partialDto) {

        ObjectNode propertiesToUpdate = JsonHandler.createObjectNode();

        // Transform Vitam-UI fields into Vitam fields
        if (partialDto.get("everyOriginatingAgency") != null) {
            propertiesToUpdate.put("EveryOriginatingAgency", (boolean) partialDto.get("everyOriginatingAgency"));
        }
        if (partialDto.get("everyDataObjectVersion") != null) {
            propertiesToUpdate.put("EveryDataObjectVersion", (boolean) partialDto.get("everyDataObjectVersion"));
        }
        if (partialDto.get("writingPermission") != null) {
            propertiesToUpdate.put("WritingPermission", (boolean) partialDto.get("writingPermission"));
        }
        if(partialDto.get("writingRestrictedDesc") != null) {
            propertiesToUpdate.put("WritingRestrictedDesc", (boolean) partialDto.get("writingRestrictedDesc"));
        }
        if (partialDto.get("description") != null) {
            propertiesToUpdate.put("Description", (String) partialDto.get("description"));
        }
        if (partialDto.get("accessLog") != null) {
            propertiesToUpdate.put("AccessLog", (String) partialDto.get("accessLog"));
        }
        if (partialDto.get("activationDate") != null) {
            propertiesToUpdate.put("ActivationDate", (String) partialDto.get("activationDate"));
        }
        if (partialDto.get("deactivationDate") != null) {
            propertiesToUpdate.put("DeactivationDate", (String) partialDto.get("deactivationDate"));
        }
        if (partialDto.get("lastUpdate") != null) {
            propertiesToUpdate.put("LastUpdate", (String) partialDto.get("lastUpdate"));
        }
        if (partialDto.get("creationDate") != null) {
            propertiesToUpdate.put("CreationDate", (String) partialDto.get("creationDate"));
        }
        if (partialDto.get("status") != null) {
            propertiesToUpdate.put("Status", (String) partialDto.get("status"));
        }

        if (partialDto.get("rootUnits") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value: (List<String>) partialDto.get("rootUnits")) {
                array.add(value);
            }
            propertiesToUpdate.set("RootUnits", array);
        }

        if (partialDto.get("excludedRootUnits") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value: (List<String>) partialDto.get("excludedRootUnits")) {
                array.add(value);
            }
            propertiesToUpdate.set("ExcludedRootUnits", array);
        }

        if (partialDto.get("ruleCategoryToFilter") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value: (List<String>) partialDto.get("ruleCategoryToFilter")) {
                array.add(value);
            }
            propertiesToUpdate.set("RuleCategoryToFilter", array);
        }

        if (partialDto.get("originatingAgencies") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value: (List<String>) partialDto.get("originatingAgencies")) {
                array.add(value);
            }
            propertiesToUpdate.set("OriginatingAgencies", array);
        }


        if (partialDto.get("dataObjectVersion") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value: (List<String>) partialDto.get("dataObjectVersion")) {
                array.add(value);
            }
            propertiesToUpdate.set("DataObjectVersion", array);
        }

        return propertiesToUpdate;
    }

    public AccessContractDto patch(VitamContext vitamContext,final Map<String, Object> partialDto) {
        String id = (String) partialDto.get("identifier");
        if (id == null) {
            throw new BadRequestException("id must be one the the update criteria");
        }
        partialDto.remove("id");
        partialDto.remove("identifier");

        try {
            LOGGER.info("Patch Access Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            // Fix because Vitam doesn't allow String Array as action value (transformed to a string representation"[value1, value2]"
            // Manual setting instead of updateRequest.addActions( UpdateActionHelper.set(fieldsUpdated));
            JsonNode fieldsUpdated = convertMapPartialDtoToUpperCaseVitamFields(partialDto);

            ObjectNode action = JsonHandler.createObjectNode();
            action.set("$set", fieldsUpdated);

            ArrayNode actions = JsonHandler.createArrayNode();
            actions.add(action);

            ObjectNode query = JsonHandler.createObjectNode();
            query.set("$action", actions);

            LOGGER.debug("Send AccessContract update request: {}", query);

            RequestResponse requestResponse =  vitamUIAccessContractService.patchAccessContract(vitamContext, id, query);
            final AccessContractVitamDto accessContractVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AccessContractVitamDto.class);
            return converter.convertVitamToDto(accessContractVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | JsonProcessingException e) {
            throw new InternalServerException("Can't patch access contract", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String id) throws VitamClientException {
             LOGGER.debug("Find History Access Contract By ID {}, EvIdAppSession : {}", id,vitamContext.getApplicationSessionId());

        try {
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id),vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
        	throw new InternalServerException("Unable to fetch history", e);
        }
    }
}
