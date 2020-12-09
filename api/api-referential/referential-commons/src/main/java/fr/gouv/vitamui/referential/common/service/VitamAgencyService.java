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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.AgencyCSVDto;
import fr.gouv.vitamui.referential.common.dto.AgencyResponseDto;

public class VitamAgencyService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamAgencyService.class);

    private final AdminExternalClient adminExternalClient;

    private final AccessExternalClient accessExternalClient;

    private final AgencyService agencyService;

    private ObjectMapper objectMapper;

    @Autowired
    public VitamAgencyService(AdminExternalClient adminExternalClient, AgencyService agencyService, ObjectMapper objectMapper, AccessExternalClient accessExternalClient) {
        this.adminExternalClient = adminExternalClient;
        this.agencyService = agencyService;
        this.objectMapper = objectMapper;
        this.accessExternalClient = accessExternalClient;
    }

    /**
     * Ignore vitam internal fields (#id, #version, #tenant) and Agency non mutable fields (Identifier, Name)
      */
    private void patchFields(AgencyModelDto agencyToPatch, AgencyModelDto fieldsToApply) {
        if (fieldsToApply.getName() != null) {
            agencyToPatch.setName(fieldsToApply.getName());
        }

        if (fieldsToApply.getDescription() != null) {
            agencyToPatch.setDescription(fieldsToApply.getDescription());
        }
    }

    public Response export(VitamContext context) throws InvalidParseOperationException, InvalidCreateOperationException, VitamClientException {
        JsonNode query = VitamQueryHelper.getLastOperationQuery(VitamQueryHelper.AGENCY_IMPORT_OPERATION_TYPE);
        RequestResponse<LogbookOperation> lastImportOperationResponse = accessExternalClient.selectOperations(context, query);
        LogbookOperationsResponseDto lastImportOperation = VitamRestUtils.responseMapping(lastImportOperationResponse.toJsonNode(), LogbookOperationsResponseDto.class);

        if (lastImportOperation.getHits().getTotal() == 0) {
            throw new VitamClientException("Can't get a result while selecting lase agency import");
        }
        LOGGER.info("Export Agencies EvIdAppSession : {} " , context.getApplicationSessionId());
        return adminExternalClient.downloadAgenciesCsvAsStream(context, lastImportOperation.getResults().get(0).getEvId());
    }
    
    public RequestResponse<?> importAgencies(VitamContext vitamContext, String fileName, MultipartFile file) 
    	throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
    	LOGGER.debug("Import agency file {}", fileName);
    	return this.importAgencies(vitamContext, file.getInputStream(), fileName);
    }

    public RequestResponse<?> patchAgency(final VitamContext vitamContext, final String id, AgencyModelDto patchAgency)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        LOGGER.info("Patch Agency EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        RequestResponse<AgenciesModel> requestResponse = agencyService.findAgencies(vitamContext, new Select().getFinalSelect());
        final List<AgencyModelDto> actualAgencies = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class).getResults();

        actualAgencies.stream()
            .filter( agency -> id.equals(agency.getId()) )
            .forEach( agency -> this.patchFields(agency, patchAgency) );

        return importAgencies(vitamContext, actualAgencies);
    }

    public boolean deleteAgency(final VitamContext vitamContext, final String id)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {

        LOGGER.info("Delete Agency EvIdAppSession : {} " , vitamContext.getApplicationSessionId());

        RequestResponse<AgenciesModel> requestResponse = agencyService.findAgencies(vitamContext, new Select().getFinalSelect());
        final List<AgencyModelDto> actualAgencies = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class).getResults();

        RequestResponse r = importAgencies(vitamContext, actualAgencies.stream()
                .filter( agency -> !id.equals(agency.getId()) )
                .collect(Collectors.toList()));
        return r.isOk();
    }

    public RequestResponse<?> create(final VitamContext vitamContext, AgencyModelDto newAgency)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {

        LOGGER.info("Create Agency EvIdAppSession : {} " , vitamContext.getApplicationSessionId());

        RequestResponse<AgenciesModel> requestResponse = agencyService.findAgencies(vitamContext, new Select().getFinalSelect());
        final List<AgencyModelDto> actualAgencies = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class).getResults();

        actualAgencies.add(newAgency);

        return importAgencies(vitamContext, actualAgencies);
    }

    private RequestResponse importAgencies(final VitamContext vitamContext, final List<AgencyModelDto> agenciesModel)
            throws InvalidParseOperationException, AccessExternalClientException, IOException {
        LOGGER.info("Import Agencies EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        LOGGER.debug("Reimport agencyies {}", agenciesModel);
        return importAgencies(vitamContext, agenciesModel, "Agencies.json");
    }

    private RequestResponse importAgencies(final VitamContext vitamContext, final List<AgencyModelDto> agencyModels, String fileName)
            throws InvalidParseOperationException, AccessExternalClientException, IOException {
        LOGGER.info("Import Agencies EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        try (ByteArrayInputStream byteArrayInputStream = serializeAgencies(agencyModels)) {
            return importAgencies(vitamContext, byteArrayInputStream, fileName);
        }
    }

    private RequestResponse<?> importAgencies(final VitamContext vitamContext, final InputStream agencies, String fileName)
            throws InvalidParseOperationException, AccessExternalClientException {
        LOGGER.info("Import Agencies EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        return adminExternalClient.createAgencies(vitamContext, agencies, fileName);
    }

    private ByteArrayInputStream serializeAgencies(final List<AgencyModelDto> accessContractModels) throws IOException {
        final List<AgencyCSVDto> listOfAgencies = convertDtosToCsvDtos(accessContractModels);
        LOGGER.debug("The json for creation agencies, sent to Vitam {}", listOfAgencies);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            final CsvMapper csvMapper = new CsvMapper();
            final CsvSchema schema = csvMapper.schemaFor(AgencyCSVDto.class)
                    .withColumnSeparator(',').withHeader();

            final ObjectWriter writer = csvMapper.writer(schema);

            writer.writeValue(byteArrayOutputStream, listOfAgencies);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    /**
     * check if all conditions are Ok to create an access contract in the tenant
     * @param agencies
     * @return
     * the tenant where the access contract will be created
     */
    public Integer checkAbilityToCreateAgencyInVitam(final List<AgencyModelDto> agencies, final String applicationSessionId) {

        if (agencies != null && !agencies.isEmpty()) {
            // check if tenant is ok in the request body
            final Optional<AgencyModelDto> agency = agencies.stream().findFirst();
            final Integer tenantIdentifier = agency.isPresent() ? agency.get().getTenant() : null;
            if (tenantIdentifier != null) {
                final boolean sameTenant = agencies.stream().allMatch(ac -> tenantIdentifier.equals(ac.getTenant()));
                if (!sameTenant) {
                    throw new BadRequestException("All the access contracts must have the same tenant identifier");
                }
            }
            else {
                throw new BadRequestException("The tenant identifier must be present in the request body");
            }

            try {
                // check if tenant exist in Vitam
                final VitamContext vitamContext = new VitamContext(tenantIdentifier).setApplicationSessionId(applicationSessionId);
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<AgenciesModel> response = agencyService.findAgencies(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new PreconditionFailedException("Can't create access contracts for the tenant : " + tenantIdentifier + " not found in Vitam");
                }
                else if (response.getStatus() != HttpStatus.OK.value()) {
                    throw new UnavailableServiceException("Can't create access contracts for this tenant, Vitam response code : " + response.getStatus());
                }

                verifyAgencyExistence(agencies, response);
            }
            catch (final VitamClientException e) {
                throw new UnavailableServiceException("Can't create access contracts for this tenant, error while calling Vitam : " + e.getMessage());
            }
            return tenantIdentifier;
        }
        throw new BadRequestException("The body is not found");
    }

    /**
     * Check if access contract is not already created in Vitam.
     * @param accessContracts
     * @param response
     */
    private void verifyAgencyExistence(final List<AgencyModelDto> accessContracts, final RequestResponse<AgenciesModel> response) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final AgencyResponseDto accessContractResponseDto = objectMapper.treeToValue(response.toJsonNode(), AgencyResponseDto.class);
            final List<String> accessContractsNames = accessContracts.stream().map(ac -> ac.getName()).collect(Collectors.toList());
            boolean alreadyCreated = accessContractResponseDto.getResults().stream().anyMatch(ac -> accessContractsNames.contains(ac.getName()));
            if (alreadyCreated) {
                throw new ConflictException("Can't create access contract, a contract with the same name already exist in Vitam");
            }
            final List<String> accessContractsIds = accessContracts.stream().map(ac -> ac.getIdentifier()).collect(Collectors.toList());
            alreadyCreated = accessContractResponseDto.getResults().stream().anyMatch(ac -> accessContractsIds.contains(ac.getIdentifier()));
            if (alreadyCreated) {
                throw new ConflictException("Can't create access contract, a contract with the same id already exist in Vitam");
            }
        }
        catch (final JsonProcessingException e) {
            throw new UnexpectedDataException("Can't create access contracts, Error while parsing Vitam response : " + e.getMessage());
        }
    }

    private AgencyCSVDto convertDtoToCsvDto(AgencyModelDto agency) {
        AgencyCSVDto csvDto = new AgencyCSVDto();
        csvDto.setName(agency.getName());
        csvDto.setIdentifier(agency.getIdentifier());
        csvDto.setDescription(agency.getDescription());
        return csvDto;
    }

    private List<AgencyCSVDto> convertDtosToCsvDtos(List<AgencyModelDto> agencies) {
        return agencies.stream().map(this::convertDtoToCsvDto).collect(Collectors.toList());
    }
}
