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
package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitamui.commons.api.exception.*;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.referential.common.dto.IngestContractResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngestContractService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestContractService.class);


    private final AdminExternalClient adminExternalClient;

    @Autowired
    public IngestContractService(final AdminExternalClient adminExternalClient) {
        this.adminExternalClient = adminExternalClient;
    }

    public RequestResponse patchIngestContract(VitamContext vitamContext, String id, ObjectNode jsonNode) throws InvalidParseOperationException, AccessExternalClientException {
        LOGGER.debug("patch: {}, {}", id, jsonNode);
        LOGGER.info("Patch Ingest Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        return adminExternalClient.updateIngestContract(vitamContext, id, jsonNode);
    }

    public RequestResponse<IngestContractModel> findIngestContractById(final VitamContext vitamContext,
                                                                       final String contractId) throws VitamClientException {

        RequestResponse<IngestContractModel> jsonResponse = adminExternalClient.findIngestContractById(vitamContext,
            contractId);
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    public RequestResponse<IngestContractModel> findIngestContracts(final VitamContext vitamContext,
                                                                    final JsonNode query) throws VitamClientException {

        RequestResponse<IngestContractModel> jsonResponse = adminExternalClient.findIngestContracts(vitamContext,
            query);
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    //TODO: Add attribute to fr.gouv.vitamui.commons.api.domain.IngestContractVitamDto
    private List<IngestContractModel> convertIngestContractsToModelOfCreation(final List<IngestContractModel> ingestContractModels) {
        final List<IngestContractModel> listOfAC = new ArrayList<>();
        for (final IngestContractModel acModel : ingestContractModels) {
            final IngestContractModel ac = new IngestContractModel();
            // we don't want to include the tenant field in the json sent to vitam
            acModel.setTenant(null);

            LOGGER.debug("inputIC: {}", acModel);
            LOGGER.debug("input checkParentID: {}", acModel.getCheckParentId());

            VitamUIUtils.copyProperties(acModel, ac);

            // copyProperties() doesn't handle Boolean properties
            ac.setMasterMandatory(acModel.isMasterMandatory());
            ac.setFormatUnidentifiedAuthorized(acModel.isFormatUnidentifiedAuthorized());
            ac.setEveryFormatType(acModel.isEveryFormatType());
            ac.setEveryDataObjectVersion(acModel.isEveryDataObjectVersion());

            LOGGER.debug("outputIC: {}", ac);
            LOGGER.debug("output checkParentID: {}", ac.getCheckParentId());

            listOfAC.add(ac);
        }
        return listOfAC;
    }

    private ByteArrayInputStream serializeIngestContracts(final List<IngestContractModel> ingestContractModels) throws IOException {
        final List<IngestContractModel> listOfAC = convertIngestContractsToModelOfCreation(ingestContractModels);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.convertValue(listOfAC, JsonNode.class);
 
        LOGGER.debug("The json for creation ingest contract, sent to Vitam {}", node);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    public RequestResponse<?> createIngestContracts(final VitamContext vitamContext, final List<IngestContractModel> ingestContracts) throws InvalidParseOperationException, AccessExternalClientException, IOException {
        try (ByteArrayInputStream byteArrayInputStream = serializeIngestContracts(ingestContracts)) {
            LOGGER.info("Create Ingest Contract EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            return adminExternalClient.createIngestContracts(vitamContext, byteArrayInputStream);
        }
    }

    public Integer checkAbilityToCreateIngestContractInVitam(List<IngestContractModel> ingestContracts, String applicationSessionId) {
        if (ingestContracts != null && !ingestContracts.isEmpty()) {
            // check if tenant is ok in the request body
            final Optional<IngestContractModel> ingestContract = ingestContracts.stream().findFirst();
            final Integer tenantIdentifier = ingestContract.isPresent() ? ingestContract.get().getTenant() : null;
            if (tenantIdentifier != null) {
                final boolean sameTenant = ingestContracts.stream().allMatch(ac -> tenantIdentifier.equals(ac.getTenant()));
                if (!sameTenant) {
                    final String msg = "All the ingest contracts must have the same tenant identifier";
                    LOGGER.error(msg);
                    throw new BadRequestException(msg);
                }
            } else {
                final String msg = "The tenant identifier must be present in the request body";
                LOGGER.error(msg);
                throw new BadRequestException(msg);
            }

            try {
                // check if tenant exist in Vitam
                final VitamContext vitamContext = new VitamContext(tenantIdentifier).setApplicationSessionId(applicationSessionId);
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<IngestContractModel> response = findIngestContracts(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    final String msg = "Can't create ingest contracts for the tenant : " + tenantIdentifier + " not found in Vitam";
                    LOGGER.error(msg);
                    throw new PreconditionFailedException(msg);
                } else if (response.getStatus() != HttpStatus.OK.value()) {
                    final String msg = "Can't create ingest contracts for this tenant, Vitam response code : " + response.getStatus();
                    LOGGER.error(msg);
                    throw new UnavailableServiceException(msg);
                }

                verifyIngestContractExistence(ingestContracts, response);
            } catch (final VitamClientException e) {
                final String msg = "Can't create ingest contracts for this tenant, error while calling Vitam : " + e.getMessage();
                LOGGER.error(msg);
                throw new UnavailableServiceException(msg);
            }
            return tenantIdentifier;
        }
        final String msg = "The body is not found";
        LOGGER.error(msg);
        throw new BadRequestException(msg);
    }

    /**
     * Check if ingest contract is not already created in Vitam.
     *
     * @param ingestContracts
     * @param response
     */
    private void verifyIngestContractExistence(final List<IngestContractModel> ingestContracts, final RequestResponse<IngestContractModel> response) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final IngestContractResponseDto ingestContractResponseDto = objectMapper.treeToValue(response.toJsonNode(), IngestContractResponseDto.class);
            final List<String> ingestContractsNames = ingestContracts.stream().map(ac -> ac.getName()).collect(Collectors.toList());
            boolean alreadyCreated = ingestContractResponseDto.getResults().stream().anyMatch(ac -> ingestContractsNames.contains(ac.getName()));
            if (alreadyCreated) {
                final String msg = "Can't create ingest contract, a contract with the same name already exist in Vitam";
                LOGGER.error(msg);
                throw new ConflictException(msg);
            }

            final List<String> ingestContractsIdentifiers = ingestContracts.stream().map(ac -> ac.getIdentifier()).collect(Collectors.toList());
            alreadyCreated = ingestContractResponseDto.getResults().stream().anyMatch(ac -> ingestContractsIdentifiers.contains(ac.getIdentifier()));
            if (alreadyCreated) {
                final String msg = "Can't create ingest contract, a contract with the same identifier already exist in Vitam";
                LOGGER.error(msg);
                throw new ConflictException(msg);
            }
        } catch (final JsonProcessingException e) {
            final String msg = "Can't create ingest contracts, Error while parsing Vitam response : " + e.getMessage();
            LOGGER.error(msg);
            throw new UnexpectedDataException(msg);
        }
    }
}
