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
package fr.gouv.vitamui.referential.internal.server.ingestcontract;

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
import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.dto.IngestContractResponseDto;
import fr.gouv.vitamui.referential.common.service.IngestContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class IngestContractInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestContractInternalService.class);

    private IngestContractService ingestContractService;


    private ObjectMapper objectMapper;

    private IngestContractConverter converter;

    private LogbookService logbookService;

    @Autowired
    public IngestContractInternalService(IngestContractService ingestContractService, ObjectMapper objectMapper, IngestContractConverter converter, LogbookService logbookService) {
        this.ingestContractService = ingestContractService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
    }

    public IngestContractDto getOne(VitamContext vitamContext, String identifier) {
        try {
            LOGGER.info("Ingest Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            RequestResponse<IngestContractModel> requestResponse = ingestContractService.findIngestContractById(vitamContext, identifier);
            final IngestContractResponseDto ingestContractResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), IngestContractResponseDto.class);
            if (ingestContractResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(ingestContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Ingest Contrat", e);
        }
    }

    public List<IngestContractDto> getAll(VitamContext vitamContext) {
        final RequestResponse<IngestContractModel> requestResponse;
        try {
            LOGGER.info("All Ingest Contracts EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            requestResponse = ingestContractService
                .findIngestContracts(vitamContext, new Select().getFinalSelect());
            final IngestContractResponseDto ingestContractResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), IngestContractResponseDto.class);

            return converter.convertVitamsToDtos(ingestContractResponseDto.getResults());
        } catch (JsonProcessingException | VitamClientException e) {
        	throw new InternalServerException("Unable to get Ingest Contrats", e);
        }
    }

    public PaginatedValuesDto<IngestContractDto> getAllPaginated(final Integer pageNumber, final Integer size,
                                                                 final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
                                                                 Optional<String> criteria) {
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query = null;
        LOGGER.info("All Ingest Contracts EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {
                };
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated ingest contracts", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        IngestContractResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<IngestContractDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        LOGGER.debug("Vitam UI DTO: {}", valuesDto);
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    public IngestContractResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<IngestContractModel> requestResponse;
        try {
            LOGGER.info("All Ingest Contracts EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            requestResponse = ingestContractService.findIngestContracts(vitamContext, query);
            LOGGER.debug("VITAM Response: {}", requestResponse.toJsonNode().toPrettyString());
            final IngestContractResponseDto ingestContractResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), IngestContractResponseDto.class);
            LOGGER.debug("VITAM DTO: {}", ingestContractResponseDto);

            return ingestContractResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to find Ingest Contrats", e);
        }
    }

    public Boolean check(VitamContext vitamContext, IngestContractDto ingestContractDto) {
        try {
            LOGGER.info("Ingest Contract Check EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            Integer ingestContractCheckedTenant = ingestContractService.checkAbilityToCreateIngestContractInVitam(converter.convertDtosToVitams(Arrays.asList(ingestContractDto)), vitamContext.getApplicationSessionId());
            return !vitamContext.getTenantId().equals(ingestContractCheckedTenant);
        } catch (ConflictException e) {
            return true;
        }
    }

    public IngestContractDto create(VitamContext vitamContext, IngestContractDto ingestContractDto) {
        try {
            LOGGER.info("Create Ingest Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            RequestResponse requestResponse = ingestContractService.createIngestContracts(vitamContext, converter.convertDtosToVitams(Arrays.asList(ingestContractDto)));
            final IngestContractModel ingestContractVitamDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), IngestContractModel.class);
            return converter.convertVitamToDto(ingestContractVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Can't create ingest contract", e);
        }
    }

    private JsonNode convertMapPartialDtoToUpperCaseVitamFields(Map<String, Object> partialDto) {

    	ObjectNode propertiesToUpdate = JsonHandler.createObjectNode();

        // Transform Vitam-UI fields into Vitam fields
        if (partialDto.get("name") != null) {
            propertiesToUpdate.put("Name", (String) partialDto.get("name"));
        }
        if (partialDto.get("description") != null) {
            propertiesToUpdate.put("Description", (String) partialDto.get("description"));
        }
        if (partialDto.get("checkParentLink") != null) {
            propertiesToUpdate.put("CheckParentLink", (String) partialDto.get("checkParentLink"));
        }
        if (partialDto.get("linkParentId") != null) {
            propertiesToUpdate.put("LinkParentId", (String) partialDto.get("linkParentId"));
        }
        if (partialDto.get("managementContractId") != null) {
            propertiesToUpdate.put("ManagementContractId", (String) partialDto.get("managementContractId"));
        }
        if (partialDto.get("status") != null) {
            propertiesToUpdate.put("Status", (String) partialDto.get("status"));
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
        if (partialDto.get("masterMandatory") != null) {
            propertiesToUpdate.put("MasterMandatory", (boolean) partialDto.get("masterMandatory"));
        }
        if (partialDto.get("formatUnidentifiedAuthorized") != null) {
            propertiesToUpdate.put("FormatUnidentifiedAuthorized", (boolean) partialDto.get("formatUnidentifiedAuthorized"));
        }
        if (partialDto.get("everyFormatType") != null) {
            propertiesToUpdate.put("EveryFormatType", (boolean) partialDto.get("everyFormatType"));
        }
        if (partialDto.get("everyDataObjectVersion") != null) {
            propertiesToUpdate.put("EveryDataObjectVersion", (boolean) partialDto.get("everyDataObjectVersion"));
        }

        if (partialDto.get("checkParentId") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value: (List<String>) partialDto.get("checkParentId")) {
                array.add(value);
            }
            propertiesToUpdate.set("CheckParentId", array);
        }
        if (partialDto.get("formatType") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value: (List<String>) partialDto.get("formatType")) {
                array.add(value);
            }
            propertiesToUpdate.set("FormatType", array);
        }
        if (partialDto.get("archiveProfiles") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value: (List<String>) partialDto.get("archiveProfiles")) {
                array.add(value);
            }
            propertiesToUpdate.set("ArchiveProfiles", array);
        }
        if (partialDto.get("dataObjectVersion") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for(String value: (List<String>) partialDto.get("dataObjectVersion")) {
                array.add(value);
            }
            propertiesToUpdate.set("DataObjectVersion", array);
        }
        return propertiesToUpdate;
    }

    public IngestContractDto patch(VitamContext vitamContext, final Map<String, Object> partialDto) {
        String id = (String) partialDto.get("identifier");
        if (id == null) {
            throw new BadRequestException("id must be one the the update criteria");
        }
        partialDto.remove("id");
        partialDto.remove("identifier");

        try {
            LOGGER.info("Patch Ingest Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            // Fix because Vitam doesn't allow String Array as action value (transformed to a string representation"[value1, value2]"
            // Manual setting instead of updateRequest.addActions( UpdateActionHelper.set(fieldsUpdated));
            JsonNode fieldsUpdated = convertMapPartialDtoToUpperCaseVitamFields(partialDto);

            ObjectNode action = JsonHandler.createObjectNode();
            action.set("$set", fieldsUpdated);

            ArrayNode actions = JsonHandler.createArrayNode();
            actions.add(action);

            ObjectNode query = JsonHandler.createObjectNode();
            query.set("$action", actions);

            LOGGER.debug("Send IngestContract update request: {}", query);

            RequestResponse requestResponse = ingestContractService.patchIngestContract(vitamContext, id, query);
            final IngestContractModel ingestContractVitamDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), IngestContractModel.class);
            return converter.convertVitamToDto(ingestContractVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | JsonProcessingException e) {
            throw new InternalServerException("Can't patch ingest contract", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String id) throws VitamClientException {
        try {
            LOGGER.info("Ingest Contract History EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id),vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
        	throw new InternalServerException("Unable to fetch history", e);
        }
    }
}
