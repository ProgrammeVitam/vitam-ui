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
package fr.gouv.vitamui.referential.common.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.SecurityProfileModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileResponseDto;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileVitamDto;

public class VitamSecurityProfileService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamContextService.class);

    private final AdminExternalClient adminExternalClient;

    private ObjectMapper objectMapper;

    @Autowired
    public VitamSecurityProfileService(AdminExternalClient adminExternalClient, ObjectMapper objectMapper) {
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
    }

    public RequestResponse<?> patchSecurityProfile(final VitamContext vitamSecurityProfile, final String id, JsonNode jsonNode)  throws VitamClientException {
        LOGGER.debug("patch: {}, {}", id, jsonNode);
        LOGGER.info("Patch Security Profile EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
        return adminExternalClient.updateSecurityProfile(vitamSecurityProfile, id, jsonNode);
    }

    public RequestResponse<?> deleteSecurityProfile(final VitamContext vitamContext, final String id)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        LOGGER.info("Delete Security Profile EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        RequestResponse<SecurityProfileModel> requestResponse = adminExternalClient.findSecurityProfiles(vitamContext, new Select().getFinalSelect());
        final List<SecurityProfileModel> actualSecurityProfiles = objectMapper
                .treeToValue(requestResponse.toJsonNode(), SecurityProfileResponseDto.class).getResults();

        return importSecurityProfiles(vitamContext, actualSecurityProfiles.stream()
                .filter( securityProfile -> !id.equals(securityProfile.getIdentifier()) )
                .collect(Collectors.toList()));
    }

    public RequestResponse<SecurityProfileModel> findSecurityProfiles(final VitamContext vitamSecurityProfile, final JsonNode select) throws VitamClientException {
        LOGGER.info("All Security Profiles EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
        final RequestResponse<SecurityProfileModel> response = adminExternalClient.findSecurityProfiles(vitamSecurityProfile, select);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<SecurityProfileModel> findSecurityProfileById(final VitamContext vitamSecurityProfile, final String securityProfileId) throws VitamClientException {
        LOGGER.info("Security Profile EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
        final RequestResponse<SecurityProfileModel> response = adminExternalClient.findSecurityProfileById(vitamSecurityProfile, securityProfileId);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<?> createSecurityProfile(final VitamContext vitamSecurityProfile, SecurityProfileModel newSecurityProfile)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        LOGGER.info("Create Security Profile EvIdAppSession : {} " , vitamSecurityProfile.getApplicationSessionId());
        final List<SecurityProfileModel> actualSecurityProfiles = new ArrayList<>();
        if(StringUtils.isBlank(newSecurityProfile.getIdentifier())) {
            newSecurityProfile.setIdentifier(newSecurityProfile.getName());
        }
        actualSecurityProfiles.add(newSecurityProfile);

        return importSecurityProfiles(vitamSecurityProfile, actualSecurityProfiles);
    }

    private RequestResponse importSecurityProfiles(final VitamContext vitamSecurityProfile, final List<SecurityProfileModel> agenciesModel)
            throws InvalidParseOperationException, AccessExternalClientException, IOException, VitamClientException {
        LOGGER.debug("Reimport securityProfileies {}", agenciesModel);
        return importSecurityProfiles(vitamSecurityProfile, agenciesModel, "SecurityProfiles.json");
    }

    private RequestResponse importSecurityProfiles(final VitamContext vitamSecurityProfile, final List<SecurityProfileModel> securityProfileModels, String fileName)
            throws InvalidParseOperationException, AccessExternalClientException, IOException, VitamClientException {
        try (ByteArrayInputStream byteArrayInputStream = serializeSecurityProfiles(securityProfileModels)) {
            return importSecurityProfiles(vitamSecurityProfile, byteArrayInputStream, fileName);
        }
    }

    private RequestResponse<?> importSecurityProfiles(final VitamContext vitamSecurityProfile, final InputStream agencies, String fileName)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException {
        return adminExternalClient.createSecurityProfiles(vitamSecurityProfile, agencies, fileName);
    }

    /**
     * check if all conditions are Ok to create a securityProfile
     * @param securityProfiles
     * @return true if the securityProfile can be created, false if the ile format already exists
     */
    public boolean checkAbilityToCreateSecurityProfileInVitam(final List<SecurityProfileModel> securityProfiles, VitamContext vitamSecurityProfile) {

        if (securityProfiles != null && !securityProfiles.isEmpty()) {
            try {
                // check if tenant exist in Vitam
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<SecurityProfileModel> response = findSecurityProfiles(vitamSecurityProfile, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new PreconditionFailedException("Can't create file format for the tenant : UNAUTHORIZED");
                }
                else if (response.getStatus() != HttpStatus.OK.value()) {
                    throw new UnavailableServiceException("Can't create file format for this tenant, Vitam response code : " + response.getStatus());
                }

                verifyFileFormatExistence(securityProfiles, response);
            }
            catch (final VitamClientException e) {
                throw new UnavailableServiceException("Can't create access contracts for this tenant, error while calling Vitam : " + e.getMessage());
            }
            return true;
        }
        throw new BadRequestException("The body is not found");
    }

    /**
     * Check if access contract is not already created in Vitam.
     * @param checkFileFormats
     * @param vitamFileFormats
     */
    private void verifyFileFormatExistence(final List<SecurityProfileModel> checkFileFormats, final RequestResponse<SecurityProfileModel> vitamFileFormats) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final SecurityProfileResponseDto securityProfileResponseDto = objectMapper.treeToValue(vitamFileFormats.toJsonNode(), SecurityProfileResponseDto.class);
            final List<String> securityProfileNames = checkFileFormats.stream().map(securityProfile -> securityProfile.getName()).collect(Collectors.toList());
            if (securityProfileResponseDto.getResults().stream().anyMatch(securityProfile -> securityProfileNames.contains(securityProfile.getName()))) {
                throw new ConflictException("Can't create securityProfile, a format with the same name already exist in Vitam");
            }
            final List<String> securityProfileIds = checkFileFormats.stream().map(securityProfile -> securityProfile.getIdentifier()).collect(Collectors.toList());
            if (securityProfileResponseDto.getResults().stream().anyMatch(securityProfile -> securityProfileIds.contains(securityProfile.getIdentifier()))) {
                throw new ConflictException("Can't create securityProfile, a format with the same puid already exist in Vitam");
            }
        }
        catch (final JsonProcessingException e) {
            throw new UnexpectedDataException("Can't create access contracts, Error while parsing Vitam response : " + e.getMessage());
        }
    }

    private ByteArrayInputStream serializeSecurityProfiles(final List<SecurityProfileModel> securityProfileModels) throws IOException {
        final List<SecurityProfileVitamDto> listOfAC = convertSecurityProfilesToModelOfCreation(securityProfileModels);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.convertValue(listOfAC, JsonNode.class);
        LOGGER.debug("The json for creation access contract, sent to Vitam {}", node);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    private List<SecurityProfileVitamDto> convertSecurityProfilesToModelOfCreation(final List<SecurityProfileModel> securityProfileModels) {
        final List<SecurityProfileVitamDto> listOfSP = new ArrayList<>();
        for (final SecurityProfileModel securityProfileModel : securityProfileModels) {
            final SecurityProfileVitamDto securityProfile = new SecurityProfileVitamDto();
            listOfSP.add(VitamUIUtils.copyProperties(securityProfileModel, securityProfile));
        }
        return listOfSP;
    }

}
