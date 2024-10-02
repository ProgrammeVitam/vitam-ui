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
package fr.gouv.vitamui.commons.vitam.api.administration;

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
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.vitam.api.dto.AccessContractResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AccessContractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessContractService.class);

    private final AdminExternalClient adminExternalClient;

    @Autowired
    public AccessContractService(final AdminExternalClient adminExternalClient) {
        this.adminExternalClient = adminExternalClient;
    }

    public RequestResponse<AccessContractModel> findAccessContracts(
        final VitamContext vitamContext,
        final JsonNode select
    ) throws VitamClientException {
        final RequestResponse<AccessContractModel> response = adminExternalClient.findAccessContracts(
            vitamContext,
            select
        );
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<AccessContractModel> findAccessContractById(
        final VitamContext vitamContext,
        final String contractId
    ) throws VitamClientException {
        final RequestResponse<AccessContractModel> response = adminExternalClient.findAccessContractById(
            vitamContext,
            contractId
        );
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse createAccessContracts(
        final VitamContext vitamContext,
        final List<AccessContractModel> accessContractModels
    ) throws InvalidParseOperationException, AccessExternalClientException, IOException {
        try (ByteArrayInputStream byteArrayInputStream = serializeAccessContracts(accessContractModels)) {
            return adminExternalClient.createAccessContracts(vitamContext, byteArrayInputStream);
        }
    }

    ByteArrayInputStream serializeAccessContracts(final List<AccessContractModel> accessContractModels)
        throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.convertValue(accessContractModels, JsonNode.class);
        LOGGER.debug("The json for creation access contract, sent to Vitam {}", node);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    public RequestResponse<?> createAccessContracts(final VitamContext vitamContext, final InputStream accessContracts)
        throws InvalidParseOperationException, AccessExternalClientException {
        return adminExternalClient.createAccessContracts(vitamContext, accessContracts);
    }

    /**
     * check if all conditions are Ok to create an access contract in the tenant
     *
     * @param accessContracts : access Contracts to verify existence
     * @return the tenant where the access contract will be created
     */
    public void checkAbilityToCreateAccessContractInVitam(
        final VitamContext vitamContext,
        final List<AccessContractModel> accessContracts
    ) {
        if (accessContracts == null || accessContracts.isEmpty()) {
            final String msg = "The body is not found";
            LOGGER.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            final JsonNode select = new Select().getFinalSelect();
            final RequestResponse<AccessContractModel> response = findAccessContracts(vitamContext, select);
            if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                final String msg =
                    "Can't create access contracts for the tenant : " +
                    vitamContext.getTenantId() +
                    " not found in Vitam";
                LOGGER.error(msg);
                throw new PreconditionFailedException(msg);
            } else if (response.getStatus() != HttpStatus.OK.value()) {
                final String msg =
                    "Can't create access contracts for this tenant, Vitam response code : " + response.getStatus();
                LOGGER.error(msg);
                throw new UnavailableServiceException(msg);
            }
            verifyAccessContractExistence(accessContracts, response);
        } catch (final VitamClientException e) {
            final String msg =
                "Can't create access contracts for this tenant, error while calling Vitam : " + e.getMessage();
            LOGGER.error(msg);
            throw new UnavailableServiceException(msg);
        }
    }

    /**
     * Check if access contract is not already created in Vitam.
     *
     * @param accessContracts : access Contracts to verify existence
     * @param response : list of access Contracts in vitam
     */
    private void verifyAccessContractExistence(
        final List<AccessContractModel> accessContracts,
        final RequestResponse<AccessContractModel> response
    ) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final AccessContractResponseDto accessContractResponseDto = objectMapper.treeToValue(
                response.toJsonNode(),
                AccessContractResponseDto.class
            );
            final List<String> accessContractsNames = accessContracts
                .stream()
                .map(AccessContractModel::getName)
                .filter(Objects::nonNull)
                .map(String::strip)
                .collect(Collectors.toList());
            boolean alreadyCreated = accessContractResponseDto
                .getResults()
                .stream()
                .anyMatch(ac -> accessContractsNames.contains(ac.getName()));
            if (alreadyCreated) {
                final String msg = "Can't create access contract, a contract with the same name already exist in Vitam";
                LOGGER.error(msg);
                throw new ConflictException(msg);
            }

            final List<String> accessContractsIdentifiers = accessContracts
                .stream()
                .map(AccessContractModel::getIdentifier)
                .filter(Objects::nonNull)
                .map(String::strip)
                .collect(Collectors.toList());
            alreadyCreated = accessContractResponseDto
                .getResults()
                .stream()
                .anyMatch(ac -> accessContractsIdentifiers.contains(ac.getIdentifier()));
            if (alreadyCreated) {
                final String msg =
                    "Can't create access contract, a contract with the same identifier already exist in Vitam";
                LOGGER.error(msg);
                throw new ConflictException(msg);
            }
        } catch (final JsonProcessingException e) {
            final String msg = "Can't create access contracts, Error while parsing Vitam response : " + e.getMessage();
            LOGGER.error(msg);
            throw new UnexpectedDataException(msg);
        }
    }
}
