/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
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
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitamui.commons.api.domain.AgencyDto;
import fr.gouv.vitamui.commons.api.domain.ManagementContractModelDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.ManagementContractResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ManagementContractService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AgencyService.class);

    private final AdminExternalClient adminExternalClient;

    @Autowired
    public ManagementContractService(final AdminExternalClient adminExternalClient) {
        this.adminExternalClient = adminExternalClient;
    }

    public RequestResponse<ManagementContractModel> findManagementContracts(final VitamContext vitamContext, final JsonNode select)
        throws VitamClientException {
        final RequestResponse<ManagementContractModel> response = adminExternalClient.findManagementContracts(vitamContext, select);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<ManagementContractModel> findManagementContractById(final VitamContext vitamContext, final String contractId)
        throws VitamClientException {
        final RequestResponse<ManagementContractModel> response = adminExternalClient.findManagementContractById(vitamContext, contractId);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse createManagementContracts(final VitamContext vitamContext, final List<ManagementContractModelDto> managementContractModelDtos)
        throws InvalidParseOperationException, AccessExternalClientException, IOException {
        try (ByteArrayInputStream byteArrayInputStream = serializeManagementContracts(managementContractModelDtos)) {
            return adminExternalClient.createManagementContracts(vitamContext, byteArrayInputStream);
        }
    }

    private ByteArrayInputStream serializeManagementContracts(final List<ManagementContractModelDto> managementContractModelDtos)
        throws IOException {
        final List<AgencyDto> listOfAgencies = convertManagementContractsToModelOfCreation(managementContractModelDtos);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.convertValue(listOfAgencies, JsonNode.class);
        LOGGER.debug("The json for creation management contract, sent to Vitam {}", node);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    private List<AgencyDto> convertManagementContractsToModelOfCreation(final List<ManagementContractModelDto> managementContractModelDtos) {
        final List<AgencyDto> agencyDtoList = new ArrayList<>();
        for (final ManagementContractModelDto model : managementContractModelDtos) {
            final AgencyDto agency = new AgencyDto();
            // we don't want to inculde the tenant field in the json sent to vitam
            model.setTenant(null);
            agencyDtoList.add(VitamUIUtils.copyProperties(model, agency));
        }
        return agencyDtoList;
    }

    /**
     * check if all conditions are Ok to create an managment contract in the tenant
     * @param managementContractModelDtos
     * @return
     * the tenant where the managment contract will be created
     */
    public Integer checkAbilityToCreateManagementContractInVitam(final List<ManagementContractModelDto> managementContractModelDtos, final String applicationSessionId) {

        if (managementContractModelDtos != null && !managementContractModelDtos.isEmpty()) {
            // check if tenant is ok in the request body
            final Optional<ManagementContractModelDto> managementContractModelDto = managementContractModelDtos.stream().findFirst();
            final Integer tenantIdentifier = managementContractModelDto.isPresent() ? managementContractModelDto.get().getTenant() : null;
            if (tenantIdentifier != null) {
                final boolean sameTenant = managementContractModelDtos.stream().allMatch(mc -> tenantIdentifier.equals(mc.getTenant()));
                if (!sameTenant) {
                    LOGGER.error("All the management contracts must have the same tenant identifier");
                    throw new BadRequestException("All the management contracts must have the same tenant identifier");
                }
            }
            else {
                LOGGER.error("The tenant identifier must be present in the request body");
                throw new BadRequestException("The tenant identifier must be present in the request body");
            }

            try {
                // check if tenant exist in Vitam
                final VitamContext vitamContext = new VitamContext(tenantIdentifier).setApplicationSessionId(applicationSessionId);
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<ManagementContractModel> response = findManagementContracts(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    LOGGER.error("Can't create management contracts for the tenant : " + tenantIdentifier + " not found in Vitam");
                    throw new NotFoundException("Can't create management contracts for the tenant : " + tenantIdentifier + " not found in Vitam");
                }
                else if (response.getStatus() != HttpStatus.OK.value()) {
                    LOGGER.error("Can't create management contracts for this tenant, Vitam response code : " + response.getStatus());
                    throw new UnavailableServiceException("Can't create management contracts for this tenant, Vitam response code : " + response.getStatus());
                }

                verifyManagementContractExistence(managementContractModelDtos, response);
            }
            catch (final VitamClientException e) {
                LOGGER.error("Can't create management contracts for this tenant, error while calling Vitam : " + e.getMessage());
                throw new UnavailableServiceException("Can't create management contracts for this tenant, error while calling Vitam : " + e.getMessage());
            }
            return tenantIdentifier;
        }
        final String msg = "The body is not found";
        LOGGER.error(msg);
        throw new BadRequestException(msg);

    }

    /**
     *
     * Check if management contract is not already created in Vitam.
     * @param managementContractModelDtos
     * @param response
     *
     */
    private void verifyManagementContractExistence(final List<ManagementContractModelDto> managementContractModelDtos , final RequestResponse<ManagementContractModel> response) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final ManagementContractResponseDto managementContractResponseDto = objectMapper.treeToValue(response.toJsonNode(), ManagementContractResponseDto.class);
            final List<String> managementContractsNames = managementContractModelDtos.stream().map(
                ManagementContractModelDto::getName).collect(Collectors.toList());
            boolean alreadyCreated = managementContractResponseDto.getResults().stream().anyMatch(ac -> managementContractsNames.contains(ac.getName()));
            if (alreadyCreated) {
                final String msg = "Can't create management contract, a contract with the same name already exist in Vitam";
                LOGGER.error(msg);
                throw new ConflictException(msg);
            }

            final List<String> managementContractsIdentifiers = managementContractModelDtos.stream().map(
                ManagementContractModelDto::getIdentifier).collect(Collectors.toList());
            alreadyCreated = managementContractResponseDto.getResults().stream().anyMatch(ac -> managementContractsIdentifiers.contains(ac.getIdentifier()));
            if (alreadyCreated) {
                final String msg = "Can't create management contract, a contract with the same identifier already exist in Vitam";
                LOGGER.error(msg);
                throw new ConflictException(msg);
            }
        }
        catch (final JsonProcessingException exception) {
            final String msg = "Can't create management contracts, Error while parsing Vitam response : " + exception.getMessage();
            LOGGER.error(msg);
            throw new UnexpectedDataException(msg, exception);
        }
    }
}
