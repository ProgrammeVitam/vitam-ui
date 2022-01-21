package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalNotFoundException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.ProfileModel;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VitamProfileService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamProfileService.class);

    private final AdminExternalClient adminExternalClient;

    private ObjectMapper objectMapper;

    @Autowired
    public VitamProfileService(AdminExternalClient adminExternalClient, ObjectMapper objectMapper) {
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Liste le contenu du référentiel des profils
     *
     * @param vitamContext
     * @param select
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<ProfileModel> findArchivalProfiles(final VitamContext vitamContext, final JsonNode select) throws VitamClientException {
        LOGGER.info("Archival Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        vitamContext.setTenantId(0);
        final RequestResponse<ProfileModel> response = adminExternalClient.findProfiles(vitamContext, select);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    /**
     * Lire un profil donné
     *
     * @param vitamContext
     * @param contractId
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<ProfileModel> findArchivalProfileById(final VitamContext vitamContext, final String contractId) throws VitamClientException {
        LOGGER.info("Archival Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        vitamContext.setTenantId(0);
        final RequestResponse<ProfileModel> response = adminExternalClient.findProfileById(vitamContext, contractId);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    /**
     * Télécharger le fichier xsd ou rng dans un profil
     *
     * @param context
     * @param id
     * @return
     * @throws VitamClientException
     * @throws AccessExternalClientException
     * @throws AccessExternalNotFoundException
     */
    public Response downloadProfile(VitamContext context, String id) throws VitamClientException, AccessExternalClientException, AccessExternalNotFoundException {
        LOGGER.info("Download profile file EvIdAppSession : {} ", context.getApplicationSessionId());
        context.setTenantId(0);
        return adminExternalClient.downloadProfileFile(context, id);
    }

    /**
     * Importer un fichier xsd ou rng dans un profil
     *
     * @param context
     * @param id
     * @param file
     * @return
     * @throws AccessExternalClientException
     * @throws InvalidParseOperationException
     */
    public RequestResponse updateProfileFile(VitamContext context, String id, MultipartFile file) throws AccessExternalClientException, InvalidParseOperationException, IOException {
        LOGGER.info("Upload Profile xsd or rng EvIdAppSession : {} ", context.getApplicationSessionId());
        context.setTenantId(0);
        return adminExternalClient.createProfileFile(context, id, file.getInputStream());
    }

    /**
     * Update Profile
     *
     * @param vitamContext
     * @param id
     * @param jsonNode
     * @return
     * @throws AccessExternalClientException
     */
    public RequestResponse<?> updateProfile(VitamContext vitamContext, String id, JsonNode jsonNode) throws AccessExternalClientException {
        LOGGER.debug("patch: {}, {}", id, jsonNode);
        LOGGER.info("Update Archival Unit Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        vitamContext.setTenantId(0);
        return adminExternalClient.updateProfile(vitamContext, id, jsonNode);
    }

    /**
     * Create Profile Json in Referential
     *
     * @param vitamContext
     * @param newArchivalProfile
     * @return
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientException
     * @throws VitamClientException
     * @throws IOException
     * @throws JAXBException
     */
    public RequestResponse<?> create(final VitamContext vitamContext, ProfileModel newArchivalProfile)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {
        LOGGER.info("Create Archival Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        vitamContext.setTenantId(0);
        final List<ProfileModel> profileModelNewList = new ArrayList<>();
        profileModelNewList.add(newArchivalProfile);
        return importArchivalProfiles(vitamContext, profileModelNewList);
    }


    public RequestResponse<?> importArchivalProfiles(final VitamContext vitamContext, final List<ProfileModel> archivalProfileModels)
        throws InvalidParseOperationException, AccessExternalClientException, IOException, JAXBException {
        try (ByteArrayInputStream byteArrayInputStream = serializeArchivalProfiles(archivalProfileModels)) {
            vitamContext.setTenantId(0);
            return adminExternalClient.createProfiles(vitamContext, byteArrayInputStream);
        }
    }

    private ByteArrayInputStream serializeArchivalProfiles(final List<ProfileModel> profileModels) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.convertValue(profileModels, JsonNode.class);
        LOGGER.debug("The json for creation profile, sent to Vitam {}", node);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    /**
     * Importer des profils dans le référentiel via un fichier
     *
     * @param vitamContext
     * @param fileName
     * @param file
     * @return
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientException
     * @throws VitamClientException
     * @throws IOException
     */
    public RequestResponse<?> importProfileByFile(VitamContext vitamContext, String fileName, MultipartFile file)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        LOGGER.debug("Import profile by file {}", fileName);
        vitamContext.setTenantId(0);
        return adminExternalClient.createProfiles(vitamContext, file.getInputStream());
    }










    /*

     *//**
     * Ignore vitam internal fields (#id, #version, #tenant) and Profile non mutable fields (Identifier, Name)
     *//*
    private void patchFields(ProfileModel archivalProfileToPatch, ProfileModel fieldsToApply) {
        if (fieldsToApply.getVersion() != null) {
            archivalProfileToPatch.setVersion(fieldsToApply.getVersion());
        }
    }

    *//**
     * check if all conditions are Ok to create an access contract in the tenant
     *
     * @param profiles
     * @return true if the format can be created, false if the ile format already exists
     *//*
    public boolean checkAbilityToCreateArchivalProfileInVitam(final List<ProfileModel> profiles, VitamContext vitamContext) {

        if (profiles != null && !profiles.isEmpty()) {
            try {
                // check if tenant exist in Vitam
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<ProfileModel> response = findArchivalProfiles(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new PreconditionFailedException("Can't create archival profile for the tenant : UNAUTHORIZED");
                } else if (response.getStatus() != HttpStatus.OK.value()) {
                    throw new UnavailableServiceException("Can't create archival profile for this tenant, Vitam response code : " + response.getStatus());
                }

                verifyArchivalProfileExistence(profiles, response);
            } catch (final VitamClientException e) {
                throw new UnavailableServiceException("Can't create access contracts for this tenant, error while calling Vitam : " + e.getMessage());
            }
            return true;
        }
        throw new BadRequestException("The body is not found");
    }

    *//**
     * Check if access contract is not already created in Vitam.
     *
     * @param checkArchivalProfiles
     * @param vitamArchivalProfiles
     *//*
    private void verifyArchivalProfileExistence(final List<ProfileModel> checkArchivalProfiles, final RequestResponse<ProfileModel> vitamArchivalProfiles) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final ProfileResponseDto accessContractResponseDto = objectMapper.treeToValue(vitamArchivalProfiles.toJsonNode(), ProfileResponseDto.class);
            final List<String> formatsNames = checkArchivalProfiles.stream().map(ac -> ac.getName()).collect(Collectors.toList());
            if (accessContractResponseDto.getResults().stream().anyMatch(ac -> formatsNames.contains(ac.getName()))) {
                throw new ConflictException("Can't create archival profile, a format with the same name already exist in Vitam");
            }
            final List<String> formatsPuids = checkArchivalProfiles.stream().map(ac -> ac.getIdentifier()).collect(Collectors.toList());
            if (accessContractResponseDto.getResults().stream().anyMatch(ac -> formatsPuids.contains(ac.getIdentifier()))) {
                throw new ConflictException("Can't create archival profile, a format with the same puid already exist in Vitam");
            }
        } catch (final JsonProcessingException e) {
            throw new UnexpectedDataException("Can't create access contracts, Error while parsing Vitam response : " + e.getMessage());
        }
    }*/
}
