/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 * <p>
 * contact@programmevitam.fr
 * <p>
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 * <p>
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
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
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.internal.server.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientServerException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalNotFoundException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.profile.ProfileModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.ProfileDto;
import fr.gouv.vitamui.referential.common.dto.ProfileResponseDto;
import fr.gouv.vitamui.referential.common.service.VitamProfileService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProfileInternalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileInternalService.class);

    private ObjectMapper objectMapper;

    private ProfileConverter converter;

    private VitamProfileService vitamProfileService;

    private InternalSecurityService internalSecurityService;

    @Autowired
    public ProfileInternalService(
        ObjectMapper objectMapper,
        ProfileConverter converter,
        VitamProfileService vitamProfileService
    ) {
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.vitamProfileService = vitamProfileService;
    }

    public List<ProfileDto> getAll(VitamContext vitamContext, Optional<String> criteria) {
        final RequestResponse<ProfileModel> requestResponse;
        LOGGER.info("All Profiles EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }
            query = VitamQueryHelper.createQueryDSL(vitamCriteria);
            requestResponse = vitamProfileService.findArchivalProfiles(vitamContext, query);
            final ProfileResponseDto profileResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                ProfileResponseDto.class
            );
            return converter.convertVitamsToDtos(profileResponseDto.getResults());
        } catch (InvalidParseOperationException | InvalidCreateOperationException e) {
            throw new InternalServerException("Unable to find profiles with criteria", e);
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find Profiles", e);
        }
    }

    public ProfileDto getOne(VitamContext vitamContext, String identifier) {
        try {
            LOGGER.info("Archival Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<ProfileModel> requestResponse = vitamProfileService.findArchivalProfileById(
                vitamContext,
                identifier
            );
            final ProfileResponseDto profileResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                ProfileResponseDto.class
            );
            if (profileResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(profileResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get ArchivalProfile", e);
        }
    }

    public PaginatedValuesDto<ProfileDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        VitamContext vitamContext,
        Optional<String> criteria
    ) {
        LOGGER.info("All Archival Profiles EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find archivalProfiles with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        ProfileResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<ProfileDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        LOGGER.debug("Profiles in page: {}", valuesDto);
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    public Response download(VitamContext context, String id)
        throws AccessExternalNotFoundException, AccessExternalClientException {
        try {
            LOGGER.info("Download EvIdAppSession : {} ", context.getApplicationSessionId());

            return vitamProfileService.downloadProfile(context, id);
        } catch (VitamClientException | AccessExternalClientServerException e) {
            throw new InternalServerException("Unable to download Profile operation report", e);
        }
    }

    public JsonNode updateProfileFile(VitamContext context, String id, MultipartFile file)
        throws AccessExternalClientException {
        try {
            LOGGER.info("Upload Profile File EvIdAppSession : {} ", context.getApplicationSessionId());
            RequestResponse requestResponse = vitamProfileService.updateProfileFile(context, id, file);
            if (!requestResponse.isOk()) {
                throw new BadRequestException("Error uploading profile file");
            }
            return requestResponse.toJsonNode();
        } catch (AccessExternalClientServerException | InvalidParseOperationException | IOException e) {
            throw new InternalServerException("Unable to Upload profile file", e);
        }
    }

    public JsonNode updateProfile(ProfileDto dto, VitamContext vitamContext) {
        String id = dto.getIdentifier();
        if (id == null) {
            throw new BadRequestException("id must be one the update criteria");
        }
        LOGGER.info("Update Archival Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        JsonNode fieldsUpdated = convertMapDtoToUpperCaseVitamFields(dto);
        ObjectNode action = JsonHandler.createObjectNode();
        action.set("$set", fieldsUpdated);
        ArrayNode actions = JsonHandler.createArrayNode();
        actions.add(action);
        ObjectNode query = JsonHandler.createObjectNode();
        query.set("$action", actions);
        try {
            RequestResponse<?> requestResponse = vitamProfileService.updateProfile(vitamContext, id, query);
            List results = ((RequestResponseOK) requestResponse).getResults();
            if (CollectionUtils.isNotEmpty(results)) {
                Object firstResult = results.get(0);
                if (Objects.nonNull(firstResult)) {
                    return ((JsonNode) firstResult).get("diffs");
                }
            }
            return null;
        } catch (AccessExternalClientException e) {
            throw new InternalServerException("Can't update Archival Profile", e);
        }
    }

    private JsonNode convertMapDtoToUpperCaseVitamFields(ProfileDto dto) {
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
        if (dto.getFormat() != null) {
            propertiesToUpdate.put("Format", dto.getFormat().toString());
        }
        if (dto.getPath() != null) {
            propertiesToUpdate.put("Path", dto.getPath());
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
        return propertiesToUpdate;
    }

    public ProfileDto create(VitamContext context, ProfileDto archivalProfileDto) {
        LOGGER.debug("Try to create profile {} {}", archivalProfileDto, context);
        try {
            LOGGER.info("Create Profile EvIdAppSession : {} ", context.getApplicationSessionId());

            RequestResponse<?> requestResponse = vitamProfileService.create(
                context,
                converter.convertDtoToVitam(archivalProfileDto)
            );
            if (requestResponse.isOk()) {
                final ProfileModel archivalProfileVitamDto = objectMapper.treeToValue(
                    requestResponse.toJsonNode().get("$results").get(0),
                    ProfileModel.class
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
        return null;
    }

    public ResponseEntity<JsonNode> importProfile(VitamContext vitamContext, String fileName, MultipartFile file) {
        try {
            RequestResponse<?> response = vitamProfileService.importProfileByFile(vitamContext, fileName, file);
            if (response.isOk()) {
                return ResponseEntity.ok(response.toJsonNode());
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (
            InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e
        ) {
            LOGGER.error("Unable to import archival profile by file {}: {}", fileName, e.getMessage());
            throw new InternalServerException("Unable to import archival profile by file " + fileName + " : ", e);
        }
    }

    private ProfileResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<ProfileModel> requestResponse;
        try {
            LOGGER.info("All Archival Profiles EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = vitamProfileService.findArchivalProfiles(vitamContext, query);

            final ProfileResponseDto archivalProfileResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                ProfileResponseDto.class
            );

            LOGGER.debug("Profiles: {}", archivalProfileResponseDto);

            return archivalProfileResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find archivalProfiles", e);
        }
    }

    public boolean checkProfileIdExist(VitamContext vitamContext, String identifier) {
        try {
            return Objects.nonNull(getOne(vitamContext, identifier));
        } catch (NotFoundException notFoundException) {
            return false;
        }
    }
}
