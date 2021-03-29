/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
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
package fr.gouv.vitamui.ingest.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.external.client.IngestCollection;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.ingest.external.api.exception.IngestExternalException;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import fr.gouv.vitamui.commons.api.exception.IngestFileGenerationException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.ingest.IngestService;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.internal.client.CustomerInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.ingest.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.ingest.common.dto.ArchiveUnitDto;
import fr.gouv.vitamui.ingest.internal.server.rest.IngestInternalController;
import java.io.ByteArrayOutputStream;

import org.odftoolkit.simple.TextDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import org.w3c.dom.Document;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ingest Internal service communication with VITAM.
 *
 *
 */
public class IngestInternalService {
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestInternalController.class);

    private final InternalSecurityService internalSecurityService;

    private final IngestExternalClient ingestExternalClient;

    private final IngestService ingestService;

    private final LogbookService logbookService;

    private final ObjectMapper objectMapper;

    private final CustomerInternalRestClient customerInternalRestClient;

    private final IngestGeneratorODTFile ingestGeneratorODTFile;


    @Autowired
    public IngestInternalService(final InternalSecurityService internalSecurityService,
        final LogbookService logbookService, final ObjectMapper objectMapper,
        final IngestExternalClient ingestExternalClient, final IngestService ingestService,
        final CustomerInternalRestClient customerInternalRestClient,
        final IngestGeneratorODTFile ingestGeneratorODTFile)
    {
        this.internalSecurityService = internalSecurityService;
        this.ingestExternalClient = ingestExternalClient;
        this.logbookService = logbookService;
        this.objectMapper = objectMapper;
        this.ingestService = ingestService;
        this.customerInternalRestClient = customerInternalRestClient;
        this.ingestGeneratorODTFile = ingestGeneratorODTFile;

    }

    public RequestResponseOK upload(MultipartFile path, String contextId, String action)
        throws IngestExternalException {

        final VitamContext vitamContext =
            internalSecurityService.buildVitamContext(internalSecurityService.getTenantIdentifier());

        RequestResponse<Void> ingestResponse = null;
        try {
            LOGGER.info("Upload EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            ingestResponse = ingestService.ingest(vitamContext, path.getInputStream(), contextId, action);
            LOGGER.info("The recieved stream size : " + path.getInputStream().available() + " is sent to Vitam");

            if(ingestResponse.isOk()) {
                LOGGER.debug("Ingest passed successfully : " + ingestResponse.toString());
            }
            else {
                LOGGER.debug("Ingest failed with status : " + ingestResponse.getHttpCode());
            }
        } catch (IOException | IngestExternalException e) {
            LOGGER.debug("Error sending upload to vitam ", e);
            throw new IngestExternalException(e);
        }

        return (RequestResponseOK) ingestResponse;

    }

    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(final Integer pageNumber, final Integer size,
        final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
        Optional<String> criteria) {
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            LOGGER.info(" All ingests EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {
                };
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }
            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find LogbookOperations with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        LogbookOperationsResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();
        List<LogbookOperationDto> valuesDto = IngestConverter.convertVitamsToDtos(results.getResults());
        LOGGER.debug("After Conversion: {}", valuesDto);
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }


    public LogbookOperationDto getOne(VitamContext vitamContext, final String id) {

        final RequestResponse<LogbookOperation> requestResponse;
        try {
            LOGGER.info("Ingest EvIdAppSession : {} " , vitamContext.getApplicationSessionId());

            requestResponse = logbookService.selectOperationbyId(id, vitamContext);

            LOGGER.debug("One Ingest Response: {}: ", requestResponse);

            final LogbookOperationsResponseDto logbookOperationDtos = objectMapper.treeToValue(requestResponse.toJsonNode(), LogbookOperationsResponseDto.class);

            List<LogbookOperationDto> singleLogbookOperationDto =
                IngestConverter.convertVitamsToDtos(logbookOperationDtos.getResults());

            return singleLogbookOperationDto.get(0);
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find LogbookOperations", e);
        }

    }

    private LogbookOperationsResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<LogbookOperation> requestResponse;
        try {
            LOGGER.info("All Ingest EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            requestResponse = logbookService.selectOperations(query, vitamContext);

            LOGGER.debug("Response: {}: ", requestResponse);

            final LogbookOperationsResponseDto logbookOperationsResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), LogbookOperationsResponseDto.class);

            LOGGER.debug("Response DTO: {}: ", logbookOperationsResponseDto);

            return logbookOperationsResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find LogbookOperations", e);
        }
    }

    public String getManifestAsString(VitamContext vitamContext, final String id) {
        try {
            String manifest = "";
            Response response = ingestExternalClient.downloadObjectAsync(vitamContext, id, IngestCollection.MANIFESTS);
            Object entity = response.getEntity();
            if (entity instanceof InputStream) {
                Resource resource = new InputStreamResource((InputStream) entity);
                manifest = ingestGeneratorODTFile.resourceAsString(resource);
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
            Response response =
                ingestExternalClient.downloadObjectAsync(vitamContext, id, IngestCollection.ARCHIVETRANSFERREPLY);
            Object entity = response.getEntity();
            if (entity instanceof InputStream) {
                Resource resource = new InputStreamResource((InputStream) entity);
                atr = ingestGeneratorODTFile.resourceAsString(resource);
            }
            LOGGER.info("ATR EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            return atr;

        } catch (VitamClientException e) {
            LOGGER.error("Unable to find ATR {}", e.getMessage());
            throw new InternalServerException("Unable to find ATR", e);
        }
    }

    public byte[] generateODTReport(VitamContext vitamContext, final String id)
        throws IOException, JSONException, URISyntaxException, IngestFileGenerationException {

        LogbookOperationDto selectedIngest = getOne(vitamContext, id) ;
        JSONObject jsonObject = new JSONObject(selectedIngest.getAgIdExt());
        CustomerDto myCustomer = customerInternalRestClient.getMyCustomer(internalSecurityService.getHttpContext());
        Resource customerLogo = null;

        try {

            Document atr = ingestGeneratorODTFile.convertStringToXMLDocument(getAtrAsString(vitamContext, id));
            Document manifest = ingestGeneratorODTFile.convertStringToXMLDocument(getManifestAsString(vitamContext, id));
            TextDocument document;
            try {
                document = TextDocument.newTextDocument();
            } catch (Exception e) {
                LOGGER.error("Error to initialize the document : {} " , e.getMessage());
                throw new IngestFileGenerationException("Error to initialize the document : {} " , e);
            }

            if(myCustomer.isHasCustomGraphicIdentity()) {
                customerLogo = customerInternalRestClient.getLogo(internalSecurityService.getHttpContext(), myCustomer.getId(), AttachmentType.HEADER).getBody();
            }
            List<ArchiveUnitDto> archiveUnitDtoList = ingestGeneratorODTFile.getValuesForDynamicTable(atr,manifest);

            ingestGeneratorODTFile.generateDocumentHeader(document,myCustomer,customerLogo);

            ingestGeneratorODTFile.generateFirstTitle(document);

            ingestGeneratorODTFile.generateServicesTable(document,manifest,jsonObject);

            ingestGeneratorODTFile.generateDepositDataTable(document,manifest,archiveUnitDtoList);

            ingestGeneratorODTFile.generateOperationDataTable(document,manifest,id);

            ingestGeneratorODTFile.generateResponsibleSignatureTable(document);

            document.addPageBreak();

            ingestGeneratorODTFile.generateSecondtTitle(document);

            ingestGeneratorODTFile.generateArchiveUnitDetailsTable(document,archiveUnitDtoList);

            LOGGER.info("Generate ODT Report EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            try {
                document.save(result);
            } catch (Exception e) {
                LOGGER.error("Error to save the document : {} " , e.getMessage());
                throw new IngestFileGenerationException("Error to save the document : {} " , e);
            }

            return result.toByteArray();

        } catch (IOException | JSONException | URISyntaxException | IngestFileGenerationException e) {
            LOGGER.error("Error with generating Report : {} " , e.getMessage());
            throw new IngestFileGenerationException("Unable to generate the ingest report ", e) ;
        }

    }

}
