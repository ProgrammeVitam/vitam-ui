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
package fr.gouv.vitamui.referential.internal.server.securityprofile;

import java.io.IOException;
import java.util.ArrayList;
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

import fr.gouv.vitam.access.external.api.AdminCollections;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.SecurityProfileModel;
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
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileResponseDto;
import fr.gouv.vitamui.referential.common.service.VitamSecurityProfileService;

@Service
public class SecurityProfileInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SecurityProfileInternalService.class);

    private VitamSecurityProfileService vitamSecurityProfileService;

    private ObjectMapper objectMapper;

    private SecurityProfileConverter converter;

    private LogbookService logbookService;

    @Autowired
    public SecurityProfileInternalService(VitamSecurityProfileService vitamSecurityProfileService, ObjectMapper objectMapper, SecurityProfileConverter converter,LogbookService logbookService) {
        this.vitamSecurityProfileService = vitamSecurityProfileService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
    }

    public SecurityProfileDto getOne(VitamContext vitamSecurityProfile, String identifier) {
        try {
            LOGGER.info("Security Profile EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
            RequestResponse<SecurityProfileModel> requestResponse = vitamSecurityProfileService.findSecurityProfileById(vitamSecurityProfile, identifier);
            final SecurityProfileResponseDto securityProfileResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), SecurityProfileResponseDto.class);
            if(securityProfileResponseDto.getResults().size() == 0){
                return null;
            }else {
                return converter.convertVitamToDto(securityProfileResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Security Profile", e);
        }
    }

    public List<SecurityProfileDto> getAll(VitamContext vitamSecurityProfile) {
        final RequestResponse<SecurityProfileModel> requestResponse;
        try {
            LOGGER.info("All Security Profiles EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
            requestResponse = vitamSecurityProfileService
                    .findSecurityProfiles(vitamSecurityProfile, new Select().getFinalSelect());
            final SecurityProfileResponseDto securityProfileResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), SecurityProfileResponseDto.class);

            return converter.convertVitamsToDtos(securityProfileResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Security Profiles", e);
        }
    }

    public PaginatedValuesDto<SecurityProfileDto> getAllPaginated(final Integer pageNumber, final Integer size,
            final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamSecurityProfile,
            Optional<String> criteria) {


        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query = null;
        LOGGER.info("All Security Profiles EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated security profiles", ioe);
        } catch ( IOException e ) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        SecurityProfileResponseDto results = this.findAll(vitamSecurityProfile, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<SecurityProfileDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    public SecurityProfileResponseDto findAll(VitamContext vitamSecurityProfile, JsonNode query) {
        final RequestResponse<SecurityProfileModel> requestResponse;
        try {
            LOGGER.info("All Security Profiles EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
            requestResponse = vitamSecurityProfileService.findSecurityProfiles(vitamSecurityProfile, query);
            return objectMapper.treeToValue(requestResponse.toJsonNode(), SecurityProfileResponseDto.class);

        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Can't find security profiles", e);
        }
    }

    public Boolean check(VitamContext vitamSecurityProfile, SecurityProfileDto securityProfileDto) {
        List<SecurityProfileDto> securityProfileDtoList = new ArrayList<>();
        securityProfileDtoList.add(securityProfileDto);
        LOGGER.info("Security Profile Check EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
        try {
            return !vitamSecurityProfileService
                    .checkAbilityToCreateSecurityProfileInVitam(
                            converter.convertDtosToVitams(securityProfileDtoList),
                            vitamSecurityProfile);
        } catch (ConflictException e) {
            return true;
        } catch (VitamUIException e) {
            throw new InternalServerException("Unable to check security profiles", e);
        }
    }

    public SecurityProfileDto create(VitamContext vitamSecurityProfile, SecurityProfileDto securityProfileDto) {
        try {
            LOGGER.info("Create Security Profiles EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
            RequestResponse<?> requestResponse = vitamSecurityProfileService.createSecurityProfile(vitamSecurityProfile, converter.convertDtoToVitam(securityProfileDto));
            final SecurityProfileModel securityProfileVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), SecurityProfileModel.class);
            return converter.convertVitamToDto(securityProfileVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException | VitamClientException e) {
            throw new InternalServerException("Can't create security profile", e);
        }
    }

    private JsonNode convertMapPartialDtoToUpperCaseVitamFields(Map<String, Object> partialDto) {

        ObjectNode propertiesToUpdate = JsonHandler.createObjectNode();

        // Transform Vitam-UI fields into Vitam fields
        if (partialDto.get("fullAccess") != null) {
            propertiesToUpdate.put("FullAccess", (boolean) partialDto.get("fullAccess"));
        }
        if (partialDto.get("permissions") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String perm: (List<String>) partialDto.get("permissions")) {
                array.add(perm);
            }
            propertiesToUpdate.set("Permissions", array);
        }

        return propertiesToUpdate;
    }

    public SecurityProfileDto patch(VitamContext vitamSecurityProfile,final Map<String, Object> partialDto) {
        // Update updateRequest = new Update();

        String id = (String) partialDto.get("identifier");
        if (id == null) {
            throw new BadRequestException("id must be one the the update criteria");
        }
        LOGGER.info("Patch Security Profiles EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
        // Hard fix updateRequest.addActions( UpdateActionHelper.set( convertMapPartialDtoToUpperCaseVitamFields(partialDto) ) ).getFinalUpdateById
        // Allow to manage List<String> objects as value setted
        JsonNode fieldsUpdated = convertMapPartialDtoToUpperCaseVitamFields(partialDto);

        ObjectNode action = JsonHandler.createObjectNode();
        action.set("$set", fieldsUpdated);

        ArrayNode actions = JsonHandler.createArrayNode();
        actions.add(action);

        ObjectNode query = JsonHandler.createObjectNode();
        query.set("$action", actions);

        try {
            RequestResponse<?> requestResponse =  vitamSecurityProfileService.patchSecurityProfile(vitamSecurityProfile, id, query);
            final SecurityProfileModel securityProfileVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), SecurityProfileModel.class);
            return converter.convertVitamToDto(securityProfileVitamDto);
        } catch (JsonProcessingException | VitamClientException e) {
            throw new InternalServerException("Can't patch security profile", e);
        }
    }

    public boolean delete(VitamContext context, String id) {
        try {
            LOGGER.info("Delete Security Profile EvIdAppSession : {} " , context.getApplicationSessionId());
            RequestResponse<?> requestResponse = vitamSecurityProfileService.deleteSecurityProfile(context, id);
            return requestResponse.isOk();
        } catch (InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e) {
            throw new InternalServerException("Unable to delete agency", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamSecurityProfile, final String identifier) throws VitamClientException {
        LOGGER.debug("findHistoryById for identifier" + identifier);
        LOGGER.info(" Security Profile History EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
        return logbookService.findEventsByIdentifierAndCollectionNames(
                identifier, AdminCollections.ACCESS_CONTRACTS.getName(), vitamSecurityProfile).toJsonNode();
    }
}
