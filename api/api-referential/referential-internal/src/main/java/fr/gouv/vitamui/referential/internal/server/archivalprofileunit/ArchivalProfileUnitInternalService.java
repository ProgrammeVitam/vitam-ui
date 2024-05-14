package fr.gouv.vitamui.referential.internal.server.archivalprofileunit;

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
import fr.gouv.vitam.common.error.VitamError;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitDto;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitResponseDto;
import fr.gouv.vitamui.referential.common.service.VitamArchivalProfileUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ArchivalProfileUnitInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        ArchivalProfileUnitInternalService.class
    );

    private ObjectMapper objectMapper;

    private ArchivalProfileUnitConverter converter;

    private LogbookService logbookService;

    private VitamArchivalProfileUnitService vitamArchivalProfileUnitService;

    private InternalSecurityService internalSecurityService;

    @Autowired
    public ArchivalProfileUnitInternalService(
        ObjectMapper objectMapper,
        ArchivalProfileUnitConverter converter,
        LogbookService logbookService,
        VitamArchivalProfileUnitService vitamArchivalProfileUnitService
    ) {
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
        this.vitamArchivalProfileUnitService = vitamArchivalProfileUnitService;
    }

    public ArchivalProfileUnitDto getOne(VitamContext vitamContext, String identifier) {
        try {
            LOGGER.info("Archival Unit Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<ArchiveUnitProfileModel> requestResponse =
                vitamArchivalProfileUnitService.findArchivalProfileById(vitamContext, identifier);
            final ArchivalProfileUnitResponseDto archivalProfileUnitResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                ArchivalProfileUnitResponseDto.class
            );
            if (archivalProfileUnitResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(archivalProfileUnitResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Archival Unit Profile", e);
        }
    }

    public List<ArchivalProfileUnitDto> getAll(VitamContext vitamContext) {
        final RequestResponse<ArchiveUnitProfileModel> requestResponse;
        LOGGER.debug("Get ALL Archival Unit Profiles !");
        try {
            LOGGER.info("All Archival Profiles EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = vitamArchivalProfileUnitService.findArchivalProfiles(
                vitamContext,
                new Select().getFinalSelect()
            );
            LOGGER.debug("Response: {}", requestResponse);
            final ArchivalProfileUnitResponseDto archivalProfileUnitResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                ArchivalProfileUnitResponseDto.class
            );
            return converter.convertVitamsToDtos(archivalProfileUnitResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find archival unit Profiles", e);
        }
    }

    public PaginatedValuesDto<ArchivalProfileUnitDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        VitamContext vitamContext,
        Optional<String> criteria
    ) {
        LOGGER.info("All Archival Unit Profiles EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find archival unit Profiles with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        ArchivalProfileUnitResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<ArchivalProfileUnitDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        LOGGER.debug("Archival Unit Profiles in page: {}", valuesDto);
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    private ArchivalProfileUnitResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<ArchiveUnitProfileModel> requestResponse;
        try {
            LOGGER.info("All Archival Unit Profiles EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = vitamArchivalProfileUnitService.findArchivalProfiles(vitamContext, query);

            final ArchivalProfileUnitResponseDto archivalProfileUnitResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                ArchivalProfileUnitResponseDto.class
            );

            LOGGER.debug("Archival Unit Profiles: {}", archivalProfileUnitResponseDto);

            return archivalProfileUnitResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find archival unit Profiles", e);
        }
    }

    public Boolean check(VitamContext vitamContext, ArchivalProfileUnitDto archivalProfileUnitDto) {
        List<ArchivalProfileUnitDto> archivalProfileUnitDtoList = new ArrayList<>();
        archivalProfileUnitDtoList.add(archivalProfileUnitDto);
        LOGGER.info("Archival Unit Profile Check EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        try {
            return !vitamArchivalProfileUnitService.checkAbilityToCreateArchivalProfileInVitam(
                converter.convertDtosToVitams(archivalProfileUnitDtoList),
                vitamContext
            );
        } catch (ConflictException e) {
            return true;
        } catch (VitamUIException e) {
            throw new InternalServerException("Unable to check archival unit Profile", e);
        }
    }

    /**
     * PUA Update.
     */
    public ArchivalProfileUnitDto update(ArchivalProfileUnitDto dto, VitamContext vitamContext)
        throws InvalidParseOperationException, AccessExternalClientException {
        String id = dto.getIdentifier();
        if (id == null) {
            throw new BadRequestException("id must be one the update criteria");
        }
        LOGGER.info("Update Archival Unit Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        JsonNode fieldsUpdated = convertMapDtoToUpperCaseVitamFields(dto);

        ObjectNode action = JsonHandler.createObjectNode();
        action.set("$set", fieldsUpdated);

        ArrayNode actions = JsonHandler.createArrayNode();
        actions.add(action);

        ObjectNode query = JsonHandler.createObjectNode();
        query.set("$action", actions);

        try {
            RequestResponse<?> requestResponse = vitamArchivalProfileUnitService.updateArchiveUnitProfile(
                vitamContext,
                id,
                query
            );
            LOGGER.info("Request RESPONSE ! {} ", requestResponse);
            if (requestResponse.getStatus() == 500) {
                throw new InternalServerException(
                    "Can't update Archival Unit profile",
                    ((VitamError<?>) requestResponse).getMessage(),
                    List.of(requestResponse.getHeaderString("X-Request-Id"))
                );
            }
            final ArchiveUnitProfileModel archivalUnitProfileVitamDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                ArchiveUnitProfileModel.class
            );
            return converter.convertVitamToDto(archivalUnitProfileVitamDto);
        } catch (JsonProcessingException | VitamClientException e) {
            throw new InternalServerException("Can't update Archival Unit profile", e);
        }
    }

    private JsonNode convertMapDtoToUpperCaseVitamFields(ArchivalProfileUnitDto dto) {
        ObjectNode propertiesToUpdate = JsonHandler.createObjectNode();

        if (dto.getName() != null) {
            propertiesToUpdate.put("Name", dto.getName());
        }
        if (dto.getDescription() != null) {
            propertiesToUpdate.put("Description", dto.getDescription());
        }
        if (dto.getStatus() != null) {
            propertiesToUpdate.put("Status", dto.getStatus().toString());
        }
        if (dto.getCreationDate() != null) {
            propertiesToUpdate.put("CreationDate", dto.getCreationDate());
        }

        if (dto.getLastUpdate() != null) {
            propertiesToUpdate.put("LastUpdate", dto.getLastUpdate());
        }

        if (dto.getActivationDate() != null) {
            propertiesToUpdate.put("ActivationDate", dto.getActivationDate());
        }

        if (dto.getDeactivationDate() != null) {
            propertiesToUpdate.put("DeactivationDate", dto.getDeactivationDate());
        }

        if (dto.getControlSchema() != null) {
            propertiesToUpdate.put("ControlSchema", dto.getControlSchema());
        }
        return propertiesToUpdate;
    }

    public ArchivalProfileUnitDto create(VitamContext context, ArchivalProfileUnitDto archivalProfileUnitDto) {
        LOGGER.debug("Try to create archival unit profile {} {}", archivalProfileUnitDto, context);
        try {
            LOGGER.info("Create Archival Unit Profile EvIdAppSession : {} ", context.getApplicationSessionId());
            RequestResponse<?> requestResponse = vitamArchivalProfileUnitService.create(
                context,
                converter.convertDtoToVitam(archivalProfileUnitDto)
            );
            if (requestResponse.isOk()) {
                final ArchiveUnitProfileModel archivalProfileVitamDto = objectMapper.treeToValue(
                    requestResponse.toJsonNode().get("$results").get(0),
                    ArchiveUnitProfileModel.class
                );
                return converter.convertVitamToDto(archivalProfileVitamDto);
            } else {
                return null;
            }
        } catch (
            InvalidParseOperationException
            | AccessExternalClientException
            | VitamClientException
            | IOException
            | JAXBException exception
        ) {
            LOGGER.error("Error while creating archive Profile", exception);
        }
        return archivalProfileUnitDto;
    }

    public ResponseEntity<JsonNode> importProfile(VitamContext vitamContext, String fileName, MultipartFile file) {
        try {
            RequestResponse<?> response = vitamArchivalProfileUnitService.importArchivalUnitProfileByFile(
                vitamContext,
                fileName,
                file
            );
            if (response.isOk()) {
                return ResponseEntity.ok(response.toJsonNode());
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (
            InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e
        ) {
            LOGGER.error("Unable to import archival unit profile by file {}: {}", fileName, e.getMessage());
            throw new InternalServerException("Unable to import archival unit profile by file " + fileName + " : ", e);
        }
    }
}
