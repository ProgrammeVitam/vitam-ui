package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileModel;
import fr.gouv.vitamui.commons.api.exception.*;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VitamArchivalProfileUnitService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamArchivalProfileUnitService.class);

    private final AdminExternalClient adminExternalClient;

    private final AccessExternalClient accessExternalClient;

    private ObjectMapper objectMapper;

    @Autowired
    public VitamArchivalProfileUnitService(
        AdminExternalClient adminExternalClient,
        ObjectMapper objectMapper,
        AccessExternalClient accessExternalClient
    ) {
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
        this.accessExternalClient = accessExternalClient;
    }

    /**
     * Lister le contenu du référentiel des profils d'unité archivistique
     *
     * @param vitamContext
     * @param select
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<ArchiveUnitProfileModel> findArchivalProfiles(
        final VitamContext vitamContext,
        final JsonNode select
    ) throws VitamClientException {
        LOGGER.info("Archival Unit Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        final RequestResponse<ArchiveUnitProfileModel> response = adminExternalClient.findArchiveUnitProfiles(
            vitamContext,
            select
        );
        VitamRestUtils.checkResponse(response);
        return response;
    }

    /**
     * Lire un profil d'unité archivistique donné
     *
     * @param vitamContext
     * @param contractId
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<ArchiveUnitProfileModel> findArchivalProfileById(
        final VitamContext vitamContext,
        final String contractId
    ) throws VitamClientException {
        LOGGER.info("Archival Unit Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        final RequestResponse<ArchiveUnitProfileModel> response = adminExternalClient.findArchiveUnitProfileById(
            vitamContext,
            contractId
        );
        VitamRestUtils.checkResponse(response);
        return response;
    }

    /**
     * Effectuer une mise à jour sur un profil d'unité archivistique
     *
     * @param vitamContext
     * @param id
     * @param jsonNode
     * @return
     * @throws VitamClientException
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientException
     */
    public RequestResponse<?> updateArchiveUnitProfile(
        final VitamContext vitamContext,
        final String id,
        JsonNode jsonNode
    ) throws VitamClientException, InvalidParseOperationException, AccessExternalClientException {
        LOGGER.debug("patch: {}, {}", id, jsonNode);
        LOGGER.info("Update Archival Unit Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        return adminExternalClient.updateArchiveUnitProfile(vitamContext, id, jsonNode);
    }

    /**
     * Écrire un ou plusieurs profils d'unité archivistique dans le référentiel format json
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
    public RequestResponse<?> create(final VitamContext vitamContext, ArchiveUnitProfileModel newArchivalProfile)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {
        LOGGER.info("Create Archival Unit Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        final List<ArchiveUnitProfileModel> archiveUnitProfileModelsList = new ArrayList<>();
        archiveUnitProfileModelsList.add(newArchivalProfile);
        return importArchivalProfiles(vitamContext, archiveUnitProfileModelsList);
    }

    private RequestResponse<?> importArchivalProfiles(
        final VitamContext vitamContext,
        final List<ArchiveUnitProfileModel> archivalProfileModels
    ) throws InvalidParseOperationException, AccessExternalClientException, IOException, JAXBException {
        try (ByteArrayInputStream byteArrayInputStream = serializeArchivalProfiles(archivalProfileModels)) {
            return adminExternalClient.createArchiveUnitProfile(vitamContext, byteArrayInputStream);
        }
    }

    private ByteArrayInputStream serializeArchivalProfiles(
        final List<ArchiveUnitProfileModel> archiveUnitProfileModels
    ) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.convertValue(archiveUnitProfileModels, JsonNode.class);
        LOGGER.debug("The json for creation profile, sent to Vitam {}", node);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    /**
     * Importer un ou plusieurs profils d'unité archivistique dans le référentiel
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
    public RequestResponse<?> importArchivalUnitProfileByFile(
        VitamContext vitamContext,
        String fileName,
        MultipartFile file
    ) throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        {
            LOGGER.debug("Import archival unit profile by file {}", fileName);
            return adminExternalClient.createArchiveUnitProfile(vitamContext, file.getInputStream());
        }
    }

    /**
     * Ignore vitam internal fields (#id, #version, #tenant) and ArchivalProfile non mutable fields (Identifier, Name)
     */
    private void patchFields(ArchiveUnitProfileModel archivalProfileToPatch, ArchiveUnitProfileModel fieldsToApply) {
        if (fieldsToApply.getVersion() != null) {
            archivalProfileToPatch.setVersion(fieldsToApply.getVersion());
        }
    }

    /**
     * check if all conditions are Ok to create an archival Profile Unit in the tenant
     *
     * @param archivalProfiles
     * @return true if the archival Profile Unit can be created, false if the ile archival Profile Unit already exists
     */
    public boolean checkAbilityToCreateArchivalProfileInVitam(
        final List<ArchiveUnitProfileModel> archivalProfiles,
        VitamContext vitamContext
    ) {
        if (archivalProfiles != null && !archivalProfiles.isEmpty()) {
            try {
                // check if tenant exist in Vitam
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<ArchiveUnitProfileModel> response = findArchivalProfiles(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new PreconditionFailedException(
                        "Can't create archival profile for the tenant : UNAUTHORIZED"
                    );
                } else if (response.getStatus() != HttpStatus.OK.value()) {
                    throw new UnavailableServiceException(
                        "Can't create archival profile for this tenant, Vitam response code : " + response.getStatus()
                    );
                }

                verifyArchivalProfileExistence(archivalProfiles, response);
            } catch (final VitamClientException e) {
                throw new UnavailableServiceException(
                    "Can't create access contracts for this tenant, error while calling Vitam : " + e.getMessage()
                );
            }
            return true;
        }
        throw new BadRequestException("The body is not found");
    }

    /**
     * Check if access contract is not already created in Vitam.
     *
     * @param checkArchivalProfiles
     * @param vitamArchivalProfiles
     */
    private void verifyArchivalProfileExistence(
        final List<ArchiveUnitProfileModel> checkArchivalProfiles,
        final RequestResponse<ArchiveUnitProfileModel> vitamArchivalProfiles
    ) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final ArchivalProfileUnitResponseDto accessContractResponseDto = objectMapper.treeToValue(
                vitamArchivalProfiles.toJsonNode(),
                ArchivalProfileUnitResponseDto.class
            );
            final List<String> formatsNames = checkArchivalProfiles
                .stream()
                .map(ac -> ac.getName())
                .collect(Collectors.toList());
            if (accessContractResponseDto.getResults().stream().anyMatch(ac -> formatsNames.contains(ac.getName()))) {
                throw new ConflictException(
                    "Can't create archival profile, a format with the same name already exist in Vitam"
                );
            }
            final List<String> formatsPuids = checkArchivalProfiles
                .stream()
                .map(ac -> ac.getIdentifier())
                .collect(Collectors.toList());
            if (
                accessContractResponseDto
                    .getResults()
                    .stream()
                    .anyMatch(ac -> formatsPuids.contains(ac.getIdentifier()))
            ) {
                throw new ConflictException(
                    "Can't create archival profile, a format with the same puid already exist in Vitam"
                );
            }
        } catch (final JsonProcessingException e) {
            throw new UnexpectedDataException(
                "Can't create access contracts, Error while parsing Vitam response : " + e.getMessage()
            );
        }
    }
}
