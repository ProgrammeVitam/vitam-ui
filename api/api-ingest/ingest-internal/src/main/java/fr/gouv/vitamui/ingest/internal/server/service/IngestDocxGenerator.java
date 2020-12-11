/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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


import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.ingest.common.dto.ArchiveUnitDto;
import fr.gouv.vitamui.ingest.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.ingest.internal.server.rest.IngestInternalController;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IngestDocxGenerator {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestDocxGenerator.class);
    public static final String FIRST_TITLE = "Bordereau de versement d'archives";
    public static final String SECOND_TITLE = "Détail des unités archivistiques de type répertoire et dossiers:";

    public Document convertStringToXMLDocument(String xmlString) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
/*
* Méthode pour créer le premier tableau du rapport Ingest
* Le tableau contient 2 informations sur le :
* Le service producteur et le service versant
*
* */
    public void generateTableOne(XWPFDocument document, Document manifest, JSONObject jsonObject)
        throws JSONException {

        XWPFTable tableOne = document.createTable();
        try {

            XWPFTableRow tableOneRowOne = tableOne.getRow(0);

            tableOneRowOne.getCell(0).removeParagraph(0);
            XWPFRun runOne = tableOneRowOne.getCell(0).addParagraph().createRun();
            runOne.setText("Service producteur :");
            runOne.setBold(true);
            tableOneRowOne.getCell(0).setWidth("3000");
            tableOneRowOne.addNewTableCell().setText(getServiceProducteur(manifest));
            tableOneRowOne.getCell(1).setWidth("7000");

            XWPFTableRow tableOneRowTwo = tableOne.createRow();
            tableOneRowTwo.getCell(0).removeParagraph(0);
            XWPFRun runTwo = tableOneRowTwo.getCell(0).addParagraph().createRun();
            runTwo.setText("Service versant :");
            runTwo.setBold(true);
            tableOneRowTwo.getCell(1).setText(getServiceVersant(jsonObject));

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.addBreak();

        } catch (JSONException e) {
            LOGGER.error("Unable to get the data from the JsonObject : {}", e.getMessage());
            throw new JSONException("Unable to get the data from the JsonObject " + e);


        }
    }

    /*
     * Méthode pour créer le 2eme tableau du rapport Ingest
     * Le tableau contient les informations sur le :
     * Numéro du versement, Présentation du contenu, Dates extrêmes et l'historique de conservation
     *
     * */
    public void generateTableTwo(XWPFDocument document, Document manifest, LogbookOperationDto selectedIngest) {

        XWPFTable tableTwo = document.createTable();

        XWPFTableRow tableTwoRowOne = tableTwo.getRow(0);

        tableTwoRowOne.getCell(0).removeParagraph(0);
        XWPFRun runNum = tableTwoRowOne.getCell(0).addParagraph().createRun();
        runNum.setText("Numéro du versement :");
        runNum.setBold(true);
        tableTwoRowOne.getCell(0).setWidth("3000");
        tableTwoRowOne.addNewTableCell().setText(getNumVersement(manifest));
        tableTwoRowOne.getCell(1).setWidth("7000");

        XWPFTableRow tableTwoRowTwo = tableTwo.createRow();
        tableTwoRowTwo.getCell(0).removeParagraph(0);
        XWPFRun runContent = tableTwoRowTwo.getCell(0).addParagraph().createRun();
        runContent.setText("Présentation du contenu :");
        runContent.setBold(true);
        tableTwoRowTwo.getCell(1).setText(getComment(manifest));

        XWPFTableRow tableTwoRowThree = tableTwo.createRow();
        tableTwoRowThree.getCell(0).removeParagraph(0);
        XWPFRun runDate = tableTwoRowThree.getCell(0).addParagraph().createRun();
        runDate.setText("Dates extrêmes :");
        runDate.setBold(true);
        tableTwoRowThree.getCell(1).removeParagraph(0);
        tableTwoRowThree.getCell(1).addParagraph().createRun().setText(
            "date de début : " + selectedIngest.getDateTime().split("T")[0].replace('-', '/'));
        tableTwoRowThree.getCell(1).addParagraph().createRun().setText("date de fin : " +
            selectedIngest.getEvents().get(selectedIngest.getEvents().size() - 1).getDateTime().split("T")[0]
                .replace('-', '/'));

        XWPFTableRow tableTwoRowFour = tableTwo.createRow();
        tableTwoRowFour.getCell(0).removeParagraph(0);
        XWPFRun runHistory = tableTwoRowFour.getCell(0).addParagraph().createRun();
        runHistory.setText("Historique des conservations :");
        runHistory.setBold(true);
        tableTwoRowFour.getCell(1).setText(getCustodialHistory(manifest));

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak();

    }

    /*
     * Méthode pour créer le 3eme tableau du rapport Ingest
     * Le tableau contient les informations sur le :
     * Nombre de fichiers binaires et Identifiant de l’opération d’entrée
     *
     * */
    public void generateTableThree(XWPFDocument document, Document manifest, String id) {

        XWPFTable tableThree = document.createTable();

        XWPFTableRow tableThreeRowOne = tableThree.getRow(0);

        tableThreeRowOne.getCell(0).removeParagraph(0);
        XWPFRun runOne = tableThreeRowOne.getCell(0).addParagraph().createRun();
        runOne.setText("Nombre de fichiers binaires:");
        runOne.setBold(true);
        tableThreeRowOne.getCell(0).setWidth("3000");
        tableThreeRowOne.addNewTableCell().setText(getBinaryFileNumber(manifest) + " fichiers");
        tableThreeRowOne.getCell(1).setWidth("7000");

        XWPFTableRow tableThreeRowFour = tableThree.createRow();
        tableThreeRowFour.getCell(0).removeParagraph(0);
        XWPFRun runTwo = tableThreeRowFour.getCell(0).addParagraph().createRun();
        runTwo.setText("Identifiant de l’opération d’entrée :");
        runTwo.setBold(true);
        tableThreeRowFour.getCell(1).setText(id);

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak();
    }

    /*
     * Méthode pour créer le 4eme tableau du rapport Ingest
     * Le tableau contient les informations sur le :
     * Date de signature, Le responsable du versement et Le responsable du service d'archives
     *
     * */
    public void generateTableFour(XWPFDocument document) {

        XWPFTable table = document.createTable();

        XWPFTableRow tableRowOne = table.getRow(0);
        tableRowOne.getCell(0).setText("Date de signature :");
        tableRowOne.getCell(0).setWidth("5000");
        tableRowOne.addNewTableCell().setText("Date de signature :");
        tableRowOne.getCell(1).setWidth("5000");
        tableRowOne.setHeight(750);

        XWPFTableRow tableRowTwo = table.createRow();
        tableRowTwo.getCell(0).setText("Le responsable du versement : ");
        tableRowTwo.getCell(1).setText("Le responsable du service d'archives : ");
        tableRowTwo.setHeight(750);

        table.getCTTbl().getTblPr().getTblBorders().getRight().setVal(STBorder.NONE);
        table.getCTTbl().getTblPr().getTblBorders().getLeft().setVal(STBorder.NONE);
        table.getCTTbl().getTblPr().getTblBorders().getInsideH().setVal(STBorder.NONE);
        table.getCTTbl().getTblPr().getTblBorders().getInsideV().setVal(STBorder.NONE);
        table.getCTTbl().getTblPr().getTblBorders().getTop().setVal(STBorder.NONE);
        table.getCTTbl().getTblPr().getTblBorders().getBottom().setVal(STBorder.NONE);

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak(BreakType.PAGE);
    }

    /*
     * Méthode pour créer tableau dynamique du rapport Ingest
     * Le tableau contient les informations sur le :
     * Identifiant SAE VAS, Titre, date de début et date de fin
     *
     * */
    public void generateDynamicTable(XWPFDocument document, List<ArchiveUnitDto> list) {

        XWPFTable dynamicTable = document.createTable();

        XWPFTableRow dynamicTableFirstRow = dynamicTable.getRow(0);
        dynamicTableFirstRow.getCell(0).removeParagraph(0);
        XWPFRun runOne = dynamicTableFirstRow.getCell(0).addParagraph().createRun();
        runOne.setText("Identifiant SAE VAS");
        runOne.setBold(true);
        dynamicTableFirstRow.getCell(0).setWidth("2000");
        dynamicTableFirstRow.getCell(0).setColor("909399");

        dynamicTableFirstRow.addNewTableCell();
        dynamicTableFirstRow.getCell(1).removeParagraph(0);
        XWPFRun runTwo = dynamicTableFirstRow.getCell(1).addParagraph().createRun();
        runTwo.setText("Titre");
        runTwo.setBold(true);
        dynamicTableFirstRow.getCell(1).setWidth("5000");
        dynamicTableFirstRow.getCell(1).setColor("909399");

        dynamicTableFirstRow.addNewTableCell();
        dynamicTableFirstRow.getCell(2).removeParagraph(0);
        XWPFRun runThree = dynamicTableFirstRow.getCell(2).addParagraph().createRun();
        runThree.setText("Date de début");
        runThree.setBold(true);
        dynamicTableFirstRow.getCell(2).setWidth("1500");
        dynamicTableFirstRow.getCell(2).setColor("909399");

        dynamicTableFirstRow.addNewTableCell();
        dynamicTableFirstRow.getCell(3).removeParagraph(0);
        XWPFRun runFour = dynamicTableFirstRow.getCell(3).addParagraph().createRun();
        runFour.setText("Date de fin");
        runFour.setBold(true);
        dynamicTableFirstRow.getCell(3).setWidth("1500");
        dynamicTableFirstRow.getCell(3).setColor("909399");

        list.stream().forEach(x -> {
            XWPFTableRow dynamicTableRow = dynamicTable.createRow();

            dynamicTableRow.getCell(0).removeParagraph(0);
            XWPFRun runSystem = dynamicTableRow.getCell(0).addParagraph().createRun();
            runSystem.setText(x.getSystemId());
            runSystem.setFontSize(8);

            dynamicTableRow.getCell(1).removeParagraph(0);
            XWPFRun runTitle = dynamicTableRow.getCell(1).addParagraph().createRun();
            runTitle.setText(x.getTiltle());
            runTitle.setFontSize(8);

            dynamicTableRow.getCell(2).removeParagraph(0);
            XWPFRun runDateD = dynamicTableRow.getCell(2).addParagraph().createRun();
            runDateD.setText(x.getStartDate().split("T")[0].replace('-', '/'));
            runDateD.setFontSize(10);

            dynamicTableRow.getCell(3).removeParagraph(0);
            XWPFRun runDateF = dynamicTableRow.getCell(3).addParagraph().createRun();
            runDateF.setText(x.getEndDate().split("T")[0].replace('-', '/'));
            runDateF.setFontSize(10);

        });
    }

    public void generateFirstTitle(XWPFDocument document) {

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun paragraphRun = paragraph.createRun();
        paragraphRun.setText(FIRST_TITLE);
        paragraphRun.setBold(true);
        paragraphRun.setFontSize(22);
        paragraph.setAlignment(ParagraphAlignment.CENTER);

    }

    public void generateSecondtTitle(XWPFDocument document) {

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun paragraphRun = paragraph.createRun();
        paragraphRun.setBold(true);
        paragraphRun.setText(SECOND_TITLE);
        paragraphRun.setFontSize(12);

    }

    public void generateDocHeader(XWPFDocument document, CustomerDto myCustomer,
        Resource logo) throws IOException {

        String imgFile;
        String imageExtension;

        XWPFTable table = document.createTable();
        try {
            XWPFTableRow tableRow = table.getRow(0);
            tableRow.getCell(0).removeParagraph(0);
            XWPFRun runTable = tableRow.getCell(0).addParagraph().createRun();
            runTable.setFontSize(12);

            runTable.setText(myCustomer.getName());
            runTable.addBreak();
            runTable.setText(myCustomer.getCompanyName());
            runTable.addBreak();
            runTable.addBreak();
            runTable.setText(myCustomer.getAddress().getStreet());
            runTable.addBreak();
            runTable.setText(myCustomer.getAddress().getCity());
            runTable.addBreak();
            runTable.setText(myCustomer.getAddress().getZipCode());
            runTable.addBreak();
            runTable.setText(myCustomer.getAddress().getCountry());
            runTable.addBreak();

            tableRow.getCell(0).setWidth("7000");
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getLeft().setVal(STBorder.NONE);
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getRight().setVal(STBorder.NONE);
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getTop().setVal(STBorder.NONE);
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getBottom().setVal(STBorder.NONE);
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getInsideV().setVal(STBorder.NONE);

            if (myCustomer.isHasCustomGraphicIdentity() && logo != null) {

                byte[] bytes = logo.getInputStream().readAllBytes();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                byte[] decodedBytes = Base64
                    .getDecoder()
                    .decode(base64Image);

                switch (base64Image.charAt(0)) {
                    case '/':
                        imageExtension = "jpg";
                        imgFile = "src/main/resources/logo_ministere." + imageExtension;
                        FileUtils.writeByteArrayToFile(new File(imgFile), decodedBytes);
                        tableRow.addNewTableCell().addParagraph().createRun()
                            .addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_JPEG, imgFile, Units
                                .toEMU(130), Units.toEMU(130));
                        break;

                    case 'i':
                        imageExtension = "png";
                        imgFile = "src/main/resources/logo_ministere." + imageExtension;
                        FileUtils.writeByteArrayToFile(new File(imgFile), decodedBytes);
                        tableRow.addNewTableCell().addParagraph().createRun()
                            .addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_PNG, imgFile, Units
                                .toEMU(130), Units.toEMU(130));
                        break;

                    case 'R':
                        imageExtension = "gif";
                        imgFile = "src/main/resources/logo_ministere." + imageExtension;
                        FileUtils.writeByteArrayToFile(new File(imgFile), decodedBytes);
                        tableRow.addNewTableCell().addParagraph().createRun()
                            .addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_GIF, imgFile, Units
                                .toEMU(130), Units.toEMU(130));
                        break;

                    case 'U':
                        imageExtension = "webp";
                        imgFile = "src/main/resources/logo_ministere." + imageExtension;
                        FileUtils.writeByteArrayToFile(new File(imgFile), decodedBytes);
                        tableRow.addNewTableCell().addParagraph().createRun()
                            .addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_WPG, imgFile, Units
                                .toEMU(130), Units.toEMU(130));
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + base64Image.charAt(0));
                }
                tableRow.getCell(1).removeParagraph(0);
                FileUtils.forceDelete(new File(imgFile));
            }

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.addBreak();

        } catch (IOException | InvalidFormatException exception) {
            throw new IOException("Unable to generate the document header ", exception);
        }
    }

    public String getServiceProducteur(Document document) {
        document.getDocumentElement().normalize();
        return document.getElementsByTagName("OriginatingAgencyIdentifier").item(0).getTextContent();
    }

    public String getNumVersement(Document document) {
        return document.getElementsByTagName("MessageIdentifier").item(0).getTextContent();
    }

    public String getComment(Document document) {
        return document.getElementsByTagName("Comment").item(0).getTextContent();
    }

    public String getCustodialHistory(Document document) {
        return document.getElementsByTagName("CustodialHistory").getLength() == 0 ?
            "historique indisponible" :
            document.getElementsByTagName("CustodialHistory").item(0).getTextContent();
    }

    public String getServiceVersant(JSONObject jsonObject) throws JSONException {
        if (jsonObject.toString().contains("submissionAgency")) {
            return jsonObject.get("submissionAgency").toString();
        }
        return jsonObject.get("originatingAgency").toString();
    }

    public int getBinaryFileNumber(Document document) {
        return document.getElementsByTagName("BinaryDataObject").getLength();
    }

    public String resourceAsString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Map<String, String> getSystemIdValues(Document document) {

        Map<String, String> map = new HashMap<>();
        document.getDocumentElement().normalize();
        NodeList nList = document.getElementsByTagName("ArchiveUnit");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;
                map.put(eElement.getAttribute("id"),
                    eElement.getElementsByTagName("SystemId").item(0).getTextContent());
            }
        }
        return map;
    }

    public List<ArchiveUnitDto> getValuesForDynamicTable(Document atr, Document manifest) {

        List<ArchiveUnitDto> list = new ArrayList<>();
        Map<String, String> map = getSystemIdValues(atr);
        manifest.getDocumentElement().normalize();
        NodeList nList = manifest.getElementsByTagName("ArchiveUnit");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                ArchiveUnitDto sea = new ArchiveUnitDto();
                Element eElement = (Element) nNode;

                if (map.get(eElement.getAttribute("id")) != null) {

                    sea.setId(eElement.getAttribute("id"));
                    sea.setTiltle(eElement.getElementsByTagName("Title").getLength() == 0 ?
                        "_ _ _ _" :
                        eElement.getElementsByTagName("Title").item(0).getTextContent());
                    sea.setEndDate(eElement.getElementsByTagName("EndDate").getLength() == 0 ?
                        "_ _ _ _" :
                        eElement.getElementsByTagName("EndDate").item(0).getTextContent());
                    sea.setStartDate(eElement.getElementsByTagName("StartDate").getLength() == 0 ?
                        "_ _ _ _" :
                        eElement.getElementsByTagName("StartDate").item(0).getTextContent());
                    sea.setSystemId(map.get(eElement.getAttribute("id")));

                    list.add(sea);
                }
            }
        }
        return list;
    }

}
