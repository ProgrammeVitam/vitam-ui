/*
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.ContextModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.ContextResponseDto;
import fr.gouv.vitamui.referential.common.dto.ContextVitamDto;
import fr.gouv.vitamui.referential.common.dto.converter.ContextDtoConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VitamContextService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamContextService.class);

    private final AdminExternalClient adminExternalClient;

    private final ObjectMapper objectMapper;

    private static final String INGEST_CONTRACTS = "ingestContracts";
    private static final String ACCESS_CONTRACTS = "accessContracts";

    @Autowired
    public VitamContextService(AdminExternalClient adminExternalClient, ObjectMapper objectMapper) {
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
    }

    public RequestResponse<?> patchContext(final VitamContext vitamContext, final String id, JsonNode jsonNode)
        throws InvalidParseOperationException, AccessExternalClientException {
        LOGGER.debug("patch: {}, {}", id, jsonNode);
        LOGGER.info("Patch Context EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        return adminExternalClient.updateContext(vitamContext, id, jsonNode);
    }

    public RequestResponse<ContextModel> findContexts(final VitamContext vitamContext, final JsonNode select)
        throws VitamClientException {
        LOGGER.info("Context EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        final RequestResponse<ContextModel> response = adminExternalClient.findContexts(vitamContext, select);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<ContextModel> findContextById(final VitamContext vitamContext, final String contextId)
        throws VitamClientException {
        LOGGER.info("Context EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        final RequestResponse<ContextModel> response = adminExternalClient.findContextById(vitamContext, contextId);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<?> createContext(final VitamContext vitamContext, ContextDto newContext)
        throws InvalidParseOperationException, AccessExternalClientException, IOException {
        LOGGER.info("Create Context EvIdAppSession : {} ", vitamContext.getApplicationSessionId());

        final List<ContextDto> actualContexts = new ArrayList<>();
        if (StringUtils.isBlank(newContext.getIdentifier())) {
            newContext.setIdentifier(newContext.getName());
        }
        actualContexts.add(newContext);

        return createContexts(vitamContext, actualContexts);
    }

    private RequestResponse createContexts(final VitamContext vitamContext, final List<ContextDto> contextModels)
        throws InvalidParseOperationException, AccessExternalClientException, IOException {
        LOGGER.debug("Reimport contexties {}", contextModels);
        try (ByteArrayInputStream byteArrayInputStream = serializeContexts(contextModels)) {
            return createContexts(vitamContext, byteArrayInputStream);
        }
    }

    private RequestResponse<?> createContexts(final VitamContext vitamContext, final InputStream contexts)
        throws InvalidParseOperationException, AccessExternalClientException {
        return adminExternalClient.createContexts(vitamContext, contexts);
    }

    /**
     * check if all conditions are Ok to create a context
     * @param contexts : List of Contexts in Vitam
     * @param vitamContext : Vitam Context
     * @return true if the context can be created, false if the ile format already exists
     */
    public boolean checkAbilityToCreateContextInVitam(final List<ContextModel> contexts, VitamContext vitamContext) {
        if (contexts != null && !contexts.isEmpty()) {
            try {
                // check if tenant exist in Vitam
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<ContextModel> response = findContexts(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    LOGGER.error(
                        "Can't create Vitam Contexts for the tenant : {}  not found in Vitam",
                        vitamContext.getTenantId()
                    );
                    throw new NotFoundException("Can't create Vitam Contexts for the tenant : UNAUTHORIZED");
                } else if (response.getStatus() != HttpStatus.OK.value()) {
                    LOGGER.error("Can't create Vitam Context for this tenant");
                    throw new UnavailableServiceException(
                        "Can't create Vitam Context for this tenant, Vitam response code : " + response.getStatus()
                    );
                }

                verifyFileFormatExistence(contexts, response);
            } catch (final VitamClientException exception) {
                LOGGER.error("Can't create Vitam Context for this tenant");
                throw new UnavailableServiceException(
                    "Can't create Vitam Contexts for this tenant, error while calling Vitam : " + exception.getMessage()
                );
            }
            return true;
        }
        throw new BadRequestException("The body is not found");
    }

    /**
     * Check if access contract is not already created in Vitam.
     * @param contextModelList : application context to verify existence
     * @param vitamContextModelList : list of application contexts in vitam
     */
    private void verifyFileFormatExistence(
        final List<ContextModel> contextModelList,
        final RequestResponse<ContextModel> vitamContextModelList
    ) {
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final ContextResponseDto contextResponseDto = objectMapper.treeToValue(
                vitamContextModelList.toJsonNode(),
                ContextResponseDto.class
            );
            final List<String> contextNames = contextModelList
                .stream()
                .map(ContextModel::getName)
                .filter(Objects::nonNull)
                .map(String::strip)
                .collect(Collectors.toList());
            if (
                contextResponseDto.getResults().stream().anyMatch(context -> contextNames.contains(context.getName()))
            ) {
                final String messageError =
                    "Can't create context, an application context with the same name already exist in Vitam";
                LOGGER.error(messageError);
                throw new ConflictException(messageError);
            }
            final List<String> contextIds = contextModelList
                .stream()
                .map(ContextModel::getIdentifier)
                .filter(Objects::nonNull)
                .map(String::strip)
                .collect(Collectors.toList());
            if (
                contextResponseDto
                    .getResults()
                    .stream()
                    .anyMatch(context -> contextIds.contains(context.getIdentifier()))
            ) {
                final String messageError =
                    "Can't create context, an application context with the same puid already exist in Vitam";
                LOGGER.error(messageError);
                throw new ConflictException(messageError);
            }
        } catch (final JsonProcessingException exception) {
            final String messageError = "Can't create application contexts, Error while parsing Vitam response : ";
            LOGGER.error(messageError);
            throw new UnexpectedDataException(messageError + exception.getMessage());
        }
    }

    private ByteArrayInputStream serializeContexts(final List<ContextDto> contextDto) throws IOException {
        final List<ContextVitamDto> listOfContexts = ContextDtoConverterUtil.convertContextsToModelOfCreation(
            contextDto
        );
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.convertValue(listOfContexts, JsonNode.class);

        // The "accessContracts" and "ingestContracts" in the permissions must be rename to "AccessContracts" and "IngestContracts" to be saved in Vitam
        final ArrayNode arrayNode = (ArrayNode) node;
        arrayNode.forEach(contextNode -> {
            final ArrayNode permissionsNode = (ArrayNode) contextNode.get("Permissions");
            if (permissionsNode != null) {
                permissionsNode.forEach(permissionNode -> {
                    final ObjectNode objectNode = (ObjectNode) permissionNode;
                    if (permissionNode.get(ACCESS_CONTRACTS) != null) {
                        objectNode.set("AccessContracts", permissionNode.get(ACCESS_CONTRACTS));
                        objectNode.remove(ACCESS_CONTRACTS);
                    }
                    if (permissionNode.get(INGEST_CONTRACTS) != null) {
                        objectNode.set("IngestContracts", permissionNode.get(INGEST_CONTRACTS));
                        objectNode.remove(INGEST_CONTRACTS);
                    }
                });
            }
        });

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }
}
