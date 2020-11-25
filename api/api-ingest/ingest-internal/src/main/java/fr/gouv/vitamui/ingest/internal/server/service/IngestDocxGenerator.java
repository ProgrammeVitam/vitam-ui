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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class IngestDocxGenerator {

    public static Document convertStringToXMLDocument(String xmlString) {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void generateTableOne() {
    }

    public static void generateTableTwo() {
    }

    public static void generateTableThree() {
    }

    public static void generateTableFour() {

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
        return document.getElementsByTagName("CustodialHistory").getLength() == 0 ? "historique indisponible" : document.getElementsByTagName("CustodialHistory").item(0).getTextContent();
    }
    public static String getServiceVersant(JSONObject jsonObject) throws JSONException {
        if(jsonObject.toString().contains("submissionAgency")) {
            return  jsonObject.get("submissionAgency").toString();
        }
        return  jsonObject.get("originatingAgency").toString();
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
