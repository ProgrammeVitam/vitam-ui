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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import java.io.File;
import java.io.ByteArrayOutputStream;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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

/*    public  String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }*/

 /*   public String test(VitamContext vitamContext, final String id) {
         String s = "";
        Response response = this.exportATR( vitamContext,  id);
        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            Resource resource = new InputStreamResource((InputStream) entity);
         //  s = s + asString(resource);

            return s;
        }
        return null;
    }*/


    public String getManifestAsString(VitamContext vitamContext, final String id) {
        String manifest = "";
        Response response = exportManifest(vitamContext, id);
        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            Resource resource = new InputStreamResource((InputStream) entity);
            manifest = manifest + IngestDocxGenerator.resourceAsString(resource);

            return manifest;
        }
        return null;
    }

    public String getAtrAsString(VitamContext vitamContext, final String id) {
        String atr = "";
        Response response = exportATR( vitamContext,  id);
        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            Resource resource = new InputStreamResource((InputStream) entity);
            atr = atr + IngestDocxGenerator.resourceAsString(resource);

            return atr;
        }
        return null;
    }


    public byte[] generateDocX(VitamContext vitamContext, final String id) throws IOException, JSONException {

        LogbookOperationDto selectedIngest = this.getOne(vitamContext, id) ;
        JSONObject jsonObject = new JSONObject(selectedIngest.getAgIdExt());
        String service = "";
        if(jsonObject.toString().contains("submissionAgency")) {
            service = jsonObject.get("submissionAgency").toString();
        }
        else {service = jsonObject.get("originatingAgency").toString();}

     try {


         //==================================
    /*     XWPFDocument documente = new XWPFDocument(OPCPackage.open("template.docx"));
         for (XWPFParagraph paragraph : documente.getParagraphs()) {
             for (XWPFRun run : paragraph.getRuns()) {
                 String text = run.getText(0);
                 text = text.replace("${name}", "oussama");
                 run.setText(text,0);
                 System.out.println(text);
             }
         }
         documente.write(new FileOutputStream("output.docx"));*/
         //===================================
         Document doc = IngestDocxGenerator.convertStringToXMLDocument( getAtrAsString(vitamContext, id) );
         Document manifest = IngestDocxGenerator.convertStringToXMLDocument( getManifestAsString(vitamContext, id) );
         //Blank Document
         XWPFDocument document = new XWPFDocument();

         //Write the Document in file system
         FileOutputStream out = new FileOutputStream(new File("template.docx"));


         //Generate the header
         IngestDocxGenerator.generateDocHeader(document);





         //create paragraph
         XWPFParagraph heey = document.createParagraph();

         //Set Bold an Italic
         XWPFRun paragraphOneRunOne = heey.createRun();

         paragraphOneRunOne.setText("Bordereau de versement d'archives");
         paragraphOneRunOne.setItalic(true);
         paragraphOneRunOne.setBold(true);
         paragraphOneRunOne.setFontSize(22);
        //  paragraphOneRunOne.setVerticalAlignment(VerticalAlignment.CENTER.toString());
         heey.setAlignment(ParagraphAlignment.CENTER);
         //paragraphOneRunOne.addBreak();


         //create table
         XWPFTable tableOne = document.createTable();

         //create first row

             XWPFTableRow tableOneRowOne = tableOne.getRow(0);
         tableOneRowOne.getCell(0).setText("Service producteur :");
         tableOneRowOne.getCell(0).setWidth("3000");
         tableOneRowOne.addNewTableCell().setText(IngestDocxGenerator.getServiceProducteur(manifest));
         tableOneRowOne.getCell(1).setWidth("7000");

         //create second row
         XWPFTableRow tableOneRowTwo = tableOne.createRow();
         tableOneRowTwo.getCell(0).setText("Service versant : ");
         tableOneRowTwo.getCell(1).setText(IngestDocxGenerator.getServiceVersant(jsonObject));

         XWPFParagraph paragraph1 = document.createParagraph();
         XWPFRun run1 = paragraph1.createRun();
         run1.addBreak();

         //table 2
         XWPFTable tableTwo = document.createTable();

         //row 1
         XWPFTableRow tableTwoRowOne = tableTwo.getRow(0);

         tableTwoRowOne.getCell(0).setText("Numéro du versement :");
         tableTwoRowOne.getCell(0).setWidth("3000");
         tableTwoRowOne.addNewTableCell().setText(IngestDocxGenerator.getNumVersement(manifest));
         tableTwoRowOne.getCell(1).setWidth("7000");

         //row 2
         XWPFTableRow tableTwoRowTwo = tableTwo.createRow();
         tableTwoRowTwo.getCell(0).setText("Présentation du contenu :");
         tableTwoRowTwo.getCell(1).setText(IngestDocxGenerator.getComment(manifest));
         //row 3
         XWPFTableRow tableTwoRowThree = tableTwo.createRow();
         tableTwoRowThree.getCell(0).setText("Dates extremes :");
         tableTwoRowThree.getCell(1).setText("Date de début :" + selectedIngest.getDateTime() + "\n" +" Date fin :" + selectedIngest.getEvents().get(selectedIngest.getEvents().size() - 1).getDateTime());
         //row 4
         XWPFTableRow tableTwoRowFour = tableTwo.createRow();
         tableTwoRowFour.getCell(0).setText("Historique des conservations :");
         tableTwoRowFour.getCell(1).setText(IngestDocxGenerator.getCustodialHistory(manifest));

         XWPFParagraph paragraph2 = document.createParagraph();
         XWPFRun run2 = paragraph2.createRun();
         run2.addBreak();


         //table 3
         XWPFTable tableThree = document.createTable();
         //row 1
         XWPFTableRow tableThreeRowOne = tableThree.getRow(0);

         tableThreeRowOne.getCell(0).setText("Nombre de fichiers binaires:");
         tableThreeRowOne.getCell(0).setWidth("3000");
         tableThreeRowOne.addNewTableCell().setText(IngestDocxGenerator.getBinaryFileNumber(manifest) + " fichiers");
         tableThreeRowOne.getCell(1).setWidth("7000");
         //row 2
         XWPFTableRow tableThreeRowTwo = tableThree.createRow();
         tableThreeRowTwo.getCell(0).setText("Poids :");
         tableThreeRowTwo.getCell(1).setText("Information indisponible");
         //row 3
         XWPFTableRow tableThreeRowThree = tableThree.createRow();
         tableThreeRowThree.getCell(0).setText("Empreinte du fichier émis :");
         tableThreeRowThree.getCell(1).setText("Information indisponible");
         //row 4
         XWPFTableRow tableThreeRowFour = tableThree.createRow();
         tableThreeRowFour.getCell(0).setText("Identifiant de l’opération d’entrée :");
         tableThreeRowFour.getCell(1).setText("GUID : " + id);

         XWPFParagraph paragraph3 = document.createParagraph();
         XWPFRun run3 = paragraph3.createRun();
         run3.addBreak();

         //table 4
         XWPFTable tableFour = document.createTable();
         //row 1
         XWPFTableRow tableFourRowOne = tableFour.getRow(0);
         tableFourRowOne.getCell(0).setText("Date de signature :");
         tableFourRowOne.getCell(0).setWidth("5000");
         tableFourRowOne.addNewTableCell().setText("Date de signature :");
         tableFourRowOne.getCell(1).setWidth("5000");
         tableFourRowOne.setHeight(700);
         //row 2
         XWPFTableRow tableFourRowTwo = tableFour.createRow();
         tableFourRowTwo.getCell(0).setText("Le responsable du versement : ");

         tableFourRowTwo.getCell(1).setText("Le responsable du service d'archives : ");
         tableFourRowTwo.setHeight(700);
        // tableFourRowTwo.setHeight(15);

       /*  //create second table
         XWPFTableRow tableRowthree = tableTwo.getRow(0);
         tableRowOne.getCell(0).setText("Service Producteur");
         tableRowOne.addNewTableCell().setText(jsonObject.get("originatingAgency").toString());

         XWPFTableRow tableRowFour = tableTwo.getRow(0);
         tableRowOne.getCell(0).setText("Service versant");
         tableRowOne.addNewTableCell().setText(service);

         tableRowOne.getCell(0).setText("Service Producteur");
         tableRowOne.addNewTableCell().setText(jsonObject.get("originatingAgency").toString());
         XWPFTableRow tableRowFour = tableTwo.getRow(0);
         tableRowOne.getCell(0).setText("Service versant");
         tableRowOne.addNewTableCell().setText(service);*/
         XWPFParagraph paragraph4 = document.createParagraph();
         XWPFRun run4 = paragraph4.createRun();

         run4.addBreak(BreakType.PAGE);

         XWPFParagraph paragraph1page2 = document.createParagraph();
         XWPFRun runheey = paragraph1page2.createRun();
         runheey.setBold(true);
         runheey.setItalic(true);
         runheey.setText("Détail des unités archivistiques de type répertoire et dossiers :");
         paragraph1page2.setAlignment(ParagraphAlignment.CENTER);
         XWPFParagraph paragraph12 = document.createParagraph();
         XWPFRun run12 = paragraph12.createRun();
         run12.addBreak();


         XWPFParagraph paragraph = document.createParagraph();
         XWPFRun run = paragraph.createRun();

             run.setText("Service producteur " + jsonObject.get("originatingAgency") +
                 " \n\n service versant " + service +
                 "\n" +
                 "\n\n num du versement " + selectedIngest.getObIdIn() +
                 "\n\n présentation du contenu " + new JSONObject(selectedIngest.getData()).get("EvDetailReq") +
                 "\n\n date de début " + selectedIngest.getDateTime() +
                 "\n\n date fin " +  selectedIngest.getEvents().get(selectedIngest.getEvents().size() - 1).getDateTime() +
                 " \n\n Nombre des fichiers binaire "  + selectedIngest.getEvents().size() +
                 "\n\n poids "+
                 "GUID " + id  +
                 " dfdf   sdf sdfdsfds  " +  doc.getFirstChild().getNodeName() // resourcee.toString()//+ input.length()
                 + "les valeurs ID " + getthevalue(doc)


             );


         List<Sea> list;
         list = getValueforTable(vitamContext, id);
         //create dynamique table
         XWPFTable tableDyn2 = document.createTable();
         //create first row
         XWPFTableRow tableDyn2Row1 = tableDyn2.getRow(0);
         tableDyn2Row1.getCell(0).setText("Identifiant SAE VAS ");
         tableDyn2Row1.getCell(0).setWidth("2000");
         tableDyn2Row1.getCell(0).setColor("909399");
         tableDyn2Row1.addNewTableCell().setText("Titre ");
         tableDyn2Row1.getCell(1).setWidth("5000");
         tableDyn2Row1.getCell(1).setColor("909399");
         tableDyn2Row1.addNewTableCell().setText("Date de début ");
         tableDyn2Row1.getCell(2).setWidth("1500");
         tableDyn2Row1.getCell(2).setColor("909399");
         tableDyn2Row1.addNewTableCell().setText("Date de fin");
         tableDyn2Row1.getCell(3).setWidth("1500");
         tableDyn2Row1.getCell(3).setColor("909399");

         list.stream().forEach(x -> {
             XWPFTableRow tableDynRowX = tableDyn2.createRow();
             tableDynRowX.getCell(0).setText(x.getSystemId());
             tableDynRowX.getCell(1).setText(x.getTiltle());
             tableDynRowX.getCell(2).setText(x.getStartDate());
             tableDynRowX.getCell(3).setText(x.getEndDate());

         });

         XWPFParagraph mparagraph = document.createParagraph();
        // XWPFRun mrun = mparagraph.createRun();
        // mparagraph = document.createParagraph();

 document.write(out);
         out.close();

         ByteArrayOutputStream ff = new ByteArrayOutputStream();
              document.write(ff);
             return ff.toByteArray();

     } catch (IOException | InvalidFormatException e) {
         throw new IOException("Unable to generate the report ", e);
     }

    }

int x = 0;
    List<String> tab = new ArrayList<>() ;
    Map<String,String> map = new HashMap<>();
    public String getthevalue(Document doc) {


        String s = "";
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("ArchiveUnit");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            System.out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;
s = s + eElement.getElementsByTagName("SystemId").item(0).getTextContent();
                System.out.println("Staff id : " + eElement.getAttribute("id"));
                System.out.println("First Name : " + eElement.getElementsByTagName("SystemId").item(0).getTextContent());
                map.put(eElement.getAttribute("id"),eElement.getElementsByTagName("SystemId").item(0).getTextContent());
tab.add(eElement.getAttribute("id"));
 x+=1;
            }
        }

        return s;

    }

    public List<Sea> getValueforTable(VitamContext vitamContext, final String id) {
        List<Sea> list = new ArrayList<Sea>();
        Document manifest = IngestDocxGenerator.convertStringToXMLDocument( getManifestAsString(vitamContext, id) );
        manifest.getDocumentElement().normalize();
        NodeList nList = manifest.getElementsByTagName("ArchiveUnit");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Sea sea = new Sea();
                Element eElement = (Element) nNode;
                System.out.println("hada id " + eElement.getAttribute("id"));
     /*           System.out.println("hada title " + eElement.getElementsByTagName("Title").item(0).getTextContent());
                System.out.println("hada startdate " + eElement.getElementsByTagName("StartDate").item(0).getTextContent());
                System.out.println("hada endDate " + eElement.getElementsByTagName("EndDate").item(0).getTextContent());*/
                System.out.println("hada system Id " + map.get(eElement.getAttribute("id")));
                if(map.get(eElement.getAttribute("id")) != null) {

                    sea.setId(eElement.getAttribute("id"));
                    sea.setTiltle(eElement.getElementsByTagName("Title").getLength() == 0 ?
                        "---" :
                        eElement.getElementsByTagName("Title").item(0).getTextContent());
                    sea.setEndDate(eElement.getElementsByTagName("EndDate").getLength() == 0 ?
                        "---" :
                        eElement.getElementsByTagName("EndDate").item(0).getTextContent());
                    sea.setStartDate(eElement.getElementsByTagName("StartDate").getLength() == 0 ?
                        "---" :
                        eElement.getElementsByTagName("StartDate").item(0).getTextContent());
                    sea.setSystemId(map.get(eElement.getAttribute("id")));
                    list.add(sea);
                }
            }
        }
        return list;
    }

/*    private static Document convertStringToXMLDocument(String xmlString)
    {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try
        {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }*/
}
