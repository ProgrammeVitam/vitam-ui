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

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.ingest.common.dto.LogbookOperationDto;
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
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class IngestDocxGenerator {

    public static final String FIRST_TITLE = "Bordereau de versement d'archives";
    public static final String SECOND_TITLE = "Détail des unités archivistiques de type répertoire et dossiers:";

    public static Document convertStringToXMLDocument(String xmlString) {

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

    public static void generateTableOne(XWPFDocument document, Document manifest, JSONObject jsonObject)
        throws JSONException {

        XWPFTable tableOne = document.createTable();
        try {

            XWPFTableRow tableOneRowOne = tableOne.getRow(0);
            tableOneRowOne.getCell(0).setText("Service producteur :");
            tableOneRowOne.getCell(0).setWidth("3000");
            tableOneRowOne.addNewTableCell().setText(getServiceProducteur(manifest));
            tableOneRowOne.getCell(1).setWidth("7000");

            XWPFTableRow tableOneRowTwo = tableOne.createRow();
            tableOneRowTwo.getCell(0).setText("Service versant : ");
            tableOneRowTwo.getCell(1).setText(getServiceVersant(jsonObject));

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.addBreak();

        } catch (JSONException e) {
            throw new JSONException("Unable to get the data from the JsonObject " + e);
        }
    }

    public static void generateTableTwo(XWPFDocument document, Document manifest, LogbookOperationDto selectedIngest) {

        XWPFTable tableTwo = document.createTable();

        XWPFTableRow tableTwoRowOne = tableTwo.getRow(0);

        tableTwoRowOne.getCell(0).setText("Numéro du versement :");
        tableTwoRowOne.getCell(0).setWidth("3000");
        tableTwoRowOne.addNewTableCell().setText(getNumVersement(manifest));
        tableTwoRowOne.getCell(1).setWidth("7000");

        XWPFTableRow tableTwoRowTwo = tableTwo.createRow();
        tableTwoRowTwo.getCell(0).setText("Présentation du contenu :");
        tableTwoRowTwo.getCell(1).setText(getComment(manifest));

        XWPFTableRow tableTwoRowThree = tableTwo.createRow();
        tableTwoRowThree.getCell(0).setText("Dates extremes :");
        tableTwoRowThree.getCell(1).setText("Date de début :" + selectedIngest.getDateTime() + "\n" + " Date fin :" +
            selectedIngest.getEvents().get(selectedIngest.getEvents().size() - 1).getDateTime());

        XWPFTableRow tableTwoRowFour = tableTwo.createRow();
        tableTwoRowFour.getCell(0).setText("Historique des conservations :");
        tableTwoRowFour.getCell(1).setText(getCustodialHistory(manifest));

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak();
    }

    public static void generateTableThree(XWPFDocument document, Document manifest, String id) {

        XWPFTable tableThree = document.createTable();

        XWPFTableRow tableThreeRowOne = tableThree.getRow(0);

        tableThreeRowOne.getCell(0).setText("Nombre de fichiers binaires:");
        tableThreeRowOne.getCell(0).setWidth("3000");
        tableThreeRowOne.addNewTableCell().setText(getBinaryFileNumber(manifest) + " fichiers");
        tableThreeRowOne.getCell(1).setWidth("7000");

        XWPFTableRow tableThreeRowFour = tableThree.createRow();
        tableThreeRowFour.getCell(0).setText("Identifiant de l’opération d’entrée :");
        tableThreeRowFour.getCell(1).setText("GUID : " + id);

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak();
    }

    public static void generateTableFour(XWPFDocument document) {

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

    public static void generateFirstTitle(XWPFDocument document) {

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun paragraphRun = paragraph.createRun();
        paragraphRun.setText(FIRST_TITLE);
        paragraphRun.setBold(true);
        paragraphRun.setFontSize(22);
        paragraph.setAlignment(ParagraphAlignment.CENTER);

    }

    public static void generateSecondtTitle(XWPFDocument document) {

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun paragraphRun = paragraph.createRun();
        paragraphRun.setBold(true);
        paragraphRun.setText(SECOND_TITLE);
        paragraphRun.setFontSize(12);

    }

    public static void generateDocHeader(XWPFDocument document) throws IOException, InvalidFormatException {

        String imgFile = "src/main/resources/logo_ministere.png";
        String adresses = " Ministère des solidarités et de la santé " +
            " Ministère du travail\n" +
            " Ministère des sports\n" +
            " Secrétariat général \n" +
            " Direction des finances, des achats et des services\n" +
            " Sous-direction des services généraux et de l'immobilier (SDSGI)\n" +
            " Bureau des archives (ARCH) – Mission des archives de France\n" +
            " 14, avenue Duquesne\n" +
            " 75350 PARIS 07 SP";

        String[] adressesSplited = adresses.split("\n");

        XWPFTable table = document.createTable();
        try {
            XWPFTableRow tableRow = table.getRow(0);
            XWPFRun runTable = tableRow.getCell(0).addParagraph().createRun();
            runTable.setFontSize(10);
            Arrays.stream(adressesSplited).forEach(x -> {
                runTable.setText(x);
                runTable.addBreak();

            });

            tableRow.getCell(0).setWidth("7000");
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getLeft().setVal(STBorder.NONE);
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getRight().setVal(STBorder.NONE);
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getTop().setVal(STBorder.NONE);
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getBottom().setVal(STBorder.NONE);
            tableRow.getTable().getCTTbl().getTblPr().getTblBorders().getInsideV().setVal(STBorder.NONE);

            tableRow.addNewTableCell().addParagraph().createRun()
                .addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_PNG, imgFile, Units
                    .toEMU(120), Units.toEMU(100));


        } catch (IOException | InvalidFormatException exception) {
            throw new IOException("Unable to generate the document header ", exception);
        }
    }

    public static String getServiceProducteur(Document document) {
        document.getDocumentElement().normalize();
        return document.getElementsByTagName("OriginatingAgencyIdentifier").item(0).getTextContent();
    }

    public static String getNumVersement(Document document) {
        return document.getElementsByTagName("MessageIdentifier").item(0).getTextContent();
    }

    public static String getComment(Document document) {
        return document.getElementsByTagName("Comment").item(0).getTextContent();
    }

    public static String getCustodialHistory(Document document) {
        return document.getElementsByTagName("CustodialHistory").getLength() == 0 ?
            "historique indisponible" :
            document.getElementsByTagName("CustodialHistory").item(0).getTextContent();
    }

    public static String getServiceVersant(JSONObject jsonObject) throws JSONException {
        if (jsonObject.toString().contains("submissionAgency")) {
            return jsonObject.get("submissionAgency").toString();
        }
        return jsonObject.get("originatingAgency").toString();
    }

    public static int getBinaryFileNumber(Document document) {
        return document.getElementsByTagName("BinaryDataObject").getLength();
    }

    public static String resourceAsString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
