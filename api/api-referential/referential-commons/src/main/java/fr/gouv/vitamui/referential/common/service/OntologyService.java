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
import fr.gouv.vitam.common.model.administration.OntologyModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.referential.common.dto.ContextResponseDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class OntologyService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OntologyService.class);


    private final AdminExternalClient adminExternalClient;

    @Autowired
    public OntologyService(final AdminExternalClient adminExternalClient) {
        this.adminExternalClient = adminExternalClient;
    }

    public RequestResponse<OntologyModel> findOntologyById(VitamContext vitamContext, String identifier) throws VitamClientException {
        LOGGER.info("Ontology EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        RequestResponse<OntologyModel> jsonResponse = adminExternalClient.findOntologyById(vitamContext,identifier);
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    public RequestResponse<OntologyModel> findOntologies(VitamContext vitamContext, JsonNode jsonNode) throws VitamClientException {
        LOGGER.info("All Ontologies EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        RequestResponse<OntologyModel> jsonResponse = adminExternalClient.findOntologies(vitamContext,jsonNode);
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    public RequestResponse<?> importOntologies(VitamContext vitamContext, List<OntologyModel> ontologies) throws InvalidParseOperationException, AccessExternalClientException, IOException {
        try (ByteArrayInputStream byteArrayInputStream = serializeOntologies(ontologies)) {
            LOGGER.info("Import All Ontologies EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            return adminExternalClient.importOntologies(true,vitamContext,byteArrayInputStream);
        }
    }
    
    public RequestResponse<?> importOntologies(VitamContext vitamContext, String fileName, MultipartFile file) 
        	throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        	LOGGER.debug("Import ontology file {}", fileName);
        	return adminExternalClient.importOntologies(false, vitamContext, file.getInputStream());
    }

    private ByteArrayInputStream serializeOntologies(List<OntologyModel> ontologiesModel) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        ontologiesModel.forEach(ontology -> {
            ontology.setId(null);
        });
        final JsonNode node = mapper.convertValue(ontologiesModel, JsonNode.class);
        LOGGER.debug("The json for import ontologies, sent to Vitam {}", node);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    /**
     * check if all conditions are Ok to create a context
     * @param models
     * @return true if the context can be created, false if the ile format already exists
     */
    public boolean checkAbilityToCreateOntologyInVitam(final List<OntologyModel> models, VitamContext vitamContext) {

        if (models != null && !models.isEmpty()) {
            try {
                // check if tenant exist in Vitam
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<OntologyModel> response = findOntologies(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new PreconditionFailedException("Can't create file format for the tenant : UNAUTHORIZED");
                }
                else if (response.getStatus() != HttpStatus.OK.value()) {
                    throw new UnavailableServiceException("Can't create file format for this tenant, Vitam response code : " + response.getStatus());
                }

                verifyFileFormatExistence(models, response);
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
    private void verifyFileFormatExistence(final List<OntologyModel> checkFileFormats, final RequestResponse<OntologyModel> vitamFileFormats) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final ContextResponseDto contextResponseDto = objectMapper.treeToValue(vitamFileFormats.toJsonNode(), ContextResponseDto.class);
            final List<String> contextIds = checkFileFormats.stream().map(context -> context.getIdentifier()).collect(Collectors.toList());
            if (contextResponseDto.getResults().stream().anyMatch(context -> contextIds.contains(context.getIdentifier()))) {
                throw new ConflictException("Can't create ontology, an ontology with the same identifier already exist in Vitam");
            }
        }
        catch (final JsonProcessingException e) {
            throw new UnexpectedDataException("Can't create ontology, Error while parsing Vitam response : " + e.getMessage());
        }
    }
}
