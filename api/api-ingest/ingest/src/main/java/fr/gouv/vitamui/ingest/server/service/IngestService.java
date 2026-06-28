/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.ingest.server.service;

import fr.gouv.vitam.common.GlobalDataRest;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.external.client.IngestCollection;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.ingest.external.api.exception.IngestExternalException;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.AccessContractDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import fr.gouv.vitamui.commons.api.exception.IngestFileGenerationException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsCommonResponseDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.openapiclient.CustomersApi;
import fr.gouv.vitamui.iam.openapiclient.UsersApi;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.ingest.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.ingest.common.dto.ArchiveUnitDto;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.odftoolkit.simple.TextDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Document;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Ingest service communication with VITAM.
 */
public class IngestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestService.class);

    private static final String ILLEGAL_CHARACTERS = "[\uFEFF-\uFFFF]";
    private static final String SELECTED_ORIGINATING_AGENCIES = "SELECTED_ORIGINATING_AGENCIES";

    private final SecurityService securityService;

    private final IngestExternalClient ingestExternalClient;

    private final LogbookService logbookService;

    private final ObjectMapper objectMapper;

    private final CustomersApi customersApi;

    private final UsersApi usersApi;

    private final IngestGeneratorODTFile ingestGeneratorODTFile;

    private final IngestExternalParametersService ingestExternalParametersService;

    private final IngestAccessContractService ingestAccessContractService;

    public IngestService(
        final SecurityService securityService,
        final LogbookService logbookService,
        final ObjectMapper objectMapper,
        final IngestExternalClient ingestExternalClient,
        final CustomersApi customersApi,
        final UsersApi usersApi,
        final IngestGeneratorODTFile ingestGeneratorODTFile,
        final IngestExternalParametersService ingestExternalParametersService,
        final IngestAccessContractService ingestAccessContractService
    ) {
        this.securityService = securityService;
        this.ingestExternalClient = ingestExternalClient;
        this.logbookService = logbookService;
        this.objectMapper = objectMapper;
        this.customersApi = customersApi;
        this.usersApi = usersApi;
        this.ingestGeneratorODTFile = ingestGeneratorODTFile;
        this.ingestExternalParametersService = ingestExternalParametersService;
        this.ingestAccessContractService = ingestAccessContractService;
    }

    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        Optional<String> criteria
    ) {
        VitamContext vitamContext = ingestExternalParametersService.buildVitamContextFromExternalParam();
        final String accessContract = vitamContext.getAccessContract();
        Set<String> originatingAgencies = null;
        Boolean everyOriginatingAgency = false;
        if (accessContract != null) {
            final Optional<AccessContractDto> accessContractDtoOpt = ingestAccessContractService.getOne(
                vitamContext,
                accessContract
            );
            if (accessContractDtoOpt.isPresent()) {
                originatingAgencies = accessContractDtoOpt.get().getOriginatingAgencies();
                everyOriginatingAgency = accessContractDtoOpt.get().getEveryOriginatingAgency();
            }
        }

        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            LOGGER.info(" All ingests EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
                if (!everyOriginatingAgency && originatingAgencies != null) {
                    vitamCriteria.put(SELECTED_ORIGINATING_AGENCIES, new ArrayList<>(originatingAgencies));
                }
            }
            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find LogbookOperations with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        LogbookOperationsCommonResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();
        List<LogbookOperationDto> valuesDto = IngestConverter.convertVitamsToDtos(results.getResults());
        LOGGER.debug("After Conversion: {}", valuesDto);
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    public LogbookOperationDto getOne(final String id) {
        final RequestResponse<LogbookOperation> requestResponse;
        try {
            VitamContext vitamContext = ingestExternalParametersService.buildVitamContextFromExternalParam();
            LOGGER.info("Ingest EvIdAppSession : {} ", vitamContext.getApplicationSessionId());

            requestResponse = logbookService.selectOperationbyId(id, vitamContext);

            LOGGER.debug("One Ingest Response: {}: ", requestResponse);

            final LogbookOperationsCommonResponseDto logbookOperationDtos = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                LogbookOperationsCommonResponseDto.class
            );

            List<LogbookOperationDto> singleLogbookOperationDto = IngestConverter.convertVitamsToDtos(
                logbookOperationDtos.getResults()
            );

            return singleLogbookOperationDto.get(0);
        } catch (VitamClientException | JacksonException e) {
            throw new InternalServerException("Unable to find LogbookOperations", e);
        }
    }

    private LogbookOperationsCommonResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<LogbookOperation> requestResponse;
        try {
            LOGGER.info("All Ingest EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = logbookService.selectOperations(query, vitamContext);

            LOGGER.debug("Response: {}: ", requestResponse);

            final LogbookOperationsCommonResponseDto logbookOperationsCommonResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                LogbookOperationsCommonResponseDto.class
            );

            LOGGER.debug("Response DTO: {}: ", logbookOperationsCommonResponseDto);

            return logbookOperationsCommonResponseDto;
        } catch (VitamClientException | JacksonException e) {
            throw new InternalServerException("Unable to find LogbookOperations", e);
        }
    }

    public String getManifestAsString(VitamContext vitamContext, final String id) {
        try {
            String manifest = "";
            Response response = ingestExternalClient.downloadObjectAsync(vitamContext, id, IngestCollection.MANIFESTS);
            Object entity = response.getEntity();
            if (entity instanceof InputStream stream) {
                Resource resource = new InputStreamResource(stream);
                manifest = ingestGeneratorODTFile.resourceAsString(resource).replaceAll(ILLEGAL_CHARACTERS, "");
            }
            LOGGER.info("Manifest EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            return manifest;
        } catch (VitamClientException e) {
            LOGGER.error("Unable to find the Manifest {}", e.getMessage());
            throw new InternalServerException("Unable to find the Manifest", e);
        }
    }

    public String getAtrAsString(VitamContext vitamContext, final String id) {
        try {
            String atr = "";
            Response response = ingestExternalClient.downloadObjectAsync(
                vitamContext,
                id,
                IngestCollection.ARCHIVETRANSFERREPLY
            );
            Object entity = response.getEntity();
            if (entity instanceof InputStream stream) {
                Resource resource = new InputStreamResource(stream);
                atr = ingestGeneratorODTFile.resourceAsString(resource).replaceAll(ILLEGAL_CHARACTERS, "");
            }
            LOGGER.info("ATR EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            return atr;
        } catch (VitamClientException e) {
            LOGGER.error("Unable to find ATR {}", e.getMessage());
            throw new InternalServerException("Unable to find ATR", e);
        }
    }

    public byte[] generateODTReport(final String id) throws IngestFileGenerationException {
        try {
            LOGGER.info("Generate ODT Report : get Manifest and ATR of the operation ID : {} ", id);
            VitamContext vitamContext = ingestExternalParametersService.buildVitamContextFromExternalParam();
            CustomerDto myCustomer = getMyCustomer();
            return generateODTReport(vitamContext, id, myCustomer);
        } catch (IngestFileGenerationException e) {
            LOGGER.error("Error with generating Report : {} ", e.getMessage());
            throw e;
        }
    }

    public CustomerDto getMyCustomer() throws IngestFileGenerationException {
        AuthUserDto me = usersApi.getMe();
        if (me == null || StringUtils.isEmpty(me.getCustomerId())) {
            throw new IngestFileGenerationException(
                "Could not retrieve current user or his customer id to generate the document"
            );
        }
        return customersApi.getMyCustomer();
    }

    public byte[] generateODTReport(final VitamContext vitamContext, final String id, final CustomerDto myCustomer)
        throws IngestFileGenerationException {
        try {
            LOGGER.info("Generate ODT Report : get Manifest and ATR of the operation ID : {} ", id);
            Resource customerLogo = null;
            Document atr = ingestGeneratorODTFile.convertStringToXMLDocument(getAtrAsString(vitamContext, id));
            Document manifest = ingestGeneratorODTFile.convertStringToXMLDocument(
                getManifestAsString(vitamContext, id)
            );
            TextDocument document;
            try {
                document = TextDocument.newTextDocument();
            } catch (Exception e) {
                LOGGER.error("Error to initialize the document : {} ", e.getMessage());
                throw new IngestFileGenerationException("Error to initialize the document : {} ", e);
            }

            if (myCustomer.isHasCustomGraphicIdentity()) {
                customerLogo = customersApi.getLogo(myCustomer.getId(), AttachmentType.HEADER.value());
            }
            List<ArchiveUnitDto> archiveUnitDtoList = ingestGeneratorODTFile.getValuesForDynamicTable(atr, manifest);

            LOGGER.info("Generate ODT Report : get customer : {} logo ", myCustomer.getId());
            ingestGeneratorODTFile.generateDocumentHeader(document, myCustomer, customerLogo);

            LOGGER.info("Generate ODT Report : generate the first page content ");
            ingestGeneratorODTFile.generateFirstTitle(document);

            ingestGeneratorODTFile.generateServicesTable(document, manifest);

            ingestGeneratorODTFile.generateDepositDataTable(document, manifest, archiveUnitDtoList);

            ingestGeneratorODTFile.generateOperationDataTable(document, manifest, id);

            ingestGeneratorODTFile.generateResponsibleSignatureTable(document);

            document.addPageBreak();

            LOGGER.info("Generate ODT Report : generate the second page content");
            ingestGeneratorODTFile.generateSecondtTitle(document);

            ingestGeneratorODTFile.generateArchiveUnitDetailsTable(document, archiveUnitDtoList);

            LOGGER.info("Generate ODT Report EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            try {
                document.save(result);
            } catch (Exception e) {
                LOGGER.error("Error to save the document : {} ", e.getMessage());
                throw new IngestFileGenerationException("Error to save the document : {} ", e);
            }

            return result.toByteArray();
        } catch (IOException | URISyntaxException | IngestFileGenerationException e) {
            LOGGER.error("Error with generating Report : {} ", e.getMessage());
            throw new IngestFileGenerationException("Unable to generate the ingest report ", e);
        }
    }

    public ResponseEntity<Void> streamingUpload(InputStream inputStream, String contextId, String action)
        throws IngestExternalException {
        try {
            VitamContext vitamContext = new VitamContext(securityService.getTenantIdentifier()).setApplicationSessionId(
                securityService.getApplicationId()
            );

            RequestResponse<Void> ingestResponse = ingestExternalClient.ingest(
                vitamContext,
                inputStream,
                contextId,
                action
            );

            if (!ingestResponse.isOk()) {
                LOGGER.debug("Error on ingest streaming Upload ");
                throw new VitamClientException("Error on ingest streaming Upload");
            }
            final String operationId = ingestResponse.getVitamHeaders().get(GlobalDataRest.X_REQUEST_ID);
            LOGGER.debug("Ingest passed successfully : " + ingestResponse + " with operationId = " + operationId);

            return ResponseEntity.ok().header(CommonConstants.X_OPERATION_ID_HEADER, operationId).build();
        } catch (Exception e) {
            LOGGER.debug("Error sending upload to vitam ", e);
            throw new IngestExternalException(e);
        }
    }
}
