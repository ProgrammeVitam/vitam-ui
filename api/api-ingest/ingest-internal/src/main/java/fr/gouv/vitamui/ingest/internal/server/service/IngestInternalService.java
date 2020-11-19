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
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.ingest.IngestService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.ingest.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.ingest.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.ingest.common.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.ingest.internal.server.rest.IngestInternalController;
/*import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.template.TemplateEngineKind;*/
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import java.io.File;
import java.io.ByteArrayOutputStream;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    final private LogbookService logbookService;

    private ObjectMapper objectMapper;

    private String message = "Generation docx from here";

    @Autowired
    public IngestInternalService(final InternalSecurityService internalSecurityService,
            final LogbookService logbookService, final ObjectMapper objectMapper, final IngestService ingestService) {
        this.internalSecurityService = internalSecurityService;
        this.logbookService = logbookService;
        this.objectMapper = objectMapper;
        this.ingestService = ingestService;
    }

    public RequestResponseOK upload(MultipartFile path, String contextId, String action)
            throws IngestExternalException {

        final VitamContext vitamContext =
                internalSecurityService.buildVitamContext(internalSecurityService.getTenantIdentifier());

        RequestResponse<Void> ingestResponse = null;
        try {
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

    public Response exportManifest(VitamContext context, String id) {
        try {
            return ingestExternalClient.downloadObjectAsync(context, id, IngestCollection.MANIFESTS);
        } catch (VitamClientException e) {
            throw new InternalServerException("Unable to find Manifest", e);
        }
    }

    public Response exportATR(VitamContext context, String id) {
        try {
            return ingestExternalClient.downloadObjectAsync(context, id, IngestCollection.ARCHIVETRANSFERREPLY);
        } catch (VitamClientException e) {
            throw new InternalServerException("Unable to find ATR", e);
        }
    }

    public byte[] generateDocX(VitamContext vitamContext, final String id) throws IOException, JSONException {

        LogbookOperationDto selectedIngest = this.getOne(vitamContext, id) ;
        JSONObject jsonObject = new JSONObject(selectedIngest.getAgIdExt());

        Resource resourcee = null;
        Response response = this.exportATR(vitamContext, id);
        Object entitye = response.getEntity();
        if (entitye instanceof InputStream) {
             resourcee = new InputStreamResource((InputStream) entitye);
         
        }
     try {

         //Blank Document
         XWPFDocument document = new XWPFDocument();

         //Write the Document in file system
         FileOutputStream out = new FileOutputStream(new File("template.docx"));

         //create table
         XWPFTable table = document.createTable();

         //create first row
         XWPFTableRow tableRowOne = table.getRow(0);
         tableRowOne.getCell(0).setText("col one, row one");
         tableRowOne.addNewTableCell().setText("col two, row one");
         tableRowOne.addNewTableCell().setText("col three, row one");

         XWPFParagraph paragraph = document.createParagraph();
         XWPFRun run = paragraph.createRun();
         String service = "";
if(jsonObject.toString().contains("submissionAgency")) {
    service = jsonObject.get("submissionAgency").toString();
}
else {service = "pas de service";}

         Response x = this.exportManifest(vitamContext, id);
        // x.getMetadata();

         /*DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = db.parse(new URL("/atr" + CommonConstants.PATH_ID).openStream());*/
         Object entity = x.getEntity();
         if (entity instanceof InputStream) {
             Resource resource = new InputStreamResource((InputStream) entity);

            // File input = resource.getFile();
  /*           DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(input);
             doc.getDocumentElement().normalize();

             NodeList nodes = doc.getElementsByTagName("ArchiveUnit");

             System.out.println("==========================");

             for (int i = 0; i < nodes.getLength(); i++) {
                 Node node = nodes.item(i);

                 if (node.getNodeType() == Node.ELEMENT_NODE) {

                     Element element = (Element) node;
                     System.out.println("Port number: " + getValue("SystemId", element));
                     System.out.println("Protocol: " + getValue("SystemId", element));
                     System.out.println("Description: " + getValue("SystemId", element));

                 }

             }*/

             run.setText("Service producteur " + jsonObject.get("originatingAgency") +
                 " \n\n service versant " + service +
                 "\n\n num du versement " + selectedIngest.getObIdIn() +
                 "\n\n présentation du contenu " + new JSONObject(selectedIngest.getData()).get("EvDetailReq") +
                 "\n\n date de début " + selectedIngest.getDateTime() +
                 "\n\n date fin " +  selectedIngest.getEvents().get(selectedIngest.getEvents().size() - 1).getDateTime() +
                 " \n\n Nombre des fichiers binaire "  + selectedIngest.getEvents().size() +
                 "\n\n poids "+
                 "GUID " + id  +
                 " dfdf   sdf sdfdsfds  " + resourcee.toString()//+ input.length()


             );


         }

 document.write(out);
         out.close();

         ByteArrayOutputStream ff = new ByteArrayOutputStream();
              document.write(ff);
             return ff.toByteArray();

     } catch (IOException  e) {
         throw new IOException("Unable to generate the report ", e);
     }

    }

    private static String getValue(String tag, Element element) {

        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();

    }
}
