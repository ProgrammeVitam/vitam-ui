/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

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
import fr.gouv.vitam.common.model.administration.profile.ProfileModel;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(VitamProfileService.class);

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
    public RequestResponse<ProfileModel> findArchivalProfiles(final VitamContext vitamContext, final JsonNode select)
        throws VitamClientException {
        LOGGER.info("Archival Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
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
    public RequestResponse<ProfileModel> findArchivalProfileById(
        final VitamContext vitamContext,
        final String contractId
    ) throws VitamClientException {
        LOGGER.info("Archival Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
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
    public Response downloadProfile(VitamContext context, String id)
        throws VitamClientException, AccessExternalClientException, AccessExternalNotFoundException {
        LOGGER.info("Download profile file EvIdAppSession : {} ", context.getApplicationSessionId());
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
    public RequestResponse updateProfileFile(VitamContext context, String id, MultipartFile file)
        throws AccessExternalClientException, InvalidParseOperationException, IOException {
        LOGGER.info("Upload Profile xsd or rng EvIdAppSession : {} ", context.getApplicationSessionId());
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
    public RequestResponse<?> updateProfile(VitamContext vitamContext, String id, JsonNode jsonNode)
        throws AccessExternalClientException {
        LOGGER.debug("patch: {}, {}", id, jsonNode);
        LOGGER.info("Update Archival Unit Profile EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
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
        final List<ProfileModel> profileModelNewList = new ArrayList<>();
        profileModelNewList.add(newArchivalProfile);
        return importArchivalProfiles(vitamContext, profileModelNewList);
    }

    public RequestResponse<?> importArchivalProfiles(
        final VitamContext vitamContext,
        final List<ProfileModel> archivalProfileModels
    ) throws InvalidParseOperationException, AccessExternalClientException, IOException, JAXBException {
        try (ByteArrayInputStream byteArrayInputStream = serializeArchivalProfiles(archivalProfileModels)) {
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
        return adminExternalClient.createProfiles(vitamContext, file.getInputStream());
    }
}
