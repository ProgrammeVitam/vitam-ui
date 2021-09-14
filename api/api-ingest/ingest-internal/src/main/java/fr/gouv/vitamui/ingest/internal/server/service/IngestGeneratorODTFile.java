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

package fr.gouv.vitamui.ingest.internal.server.service;

import fr.gouv.vitamui.commons.api.exception.IngestFileGenerationException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.ingest.common.dto.ArchiveUnitDto;
import fr.gouv.vitamui.ingest.common.enums.Extension;
import org.apache.commons.io.FileUtils;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Border;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ingest ODT file Generator
 *
 * All methods to
 * Generate ODT report
 */
public class IngestGeneratorODTFile {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestGeneratorODTFile.class);
    public static final String FIRST_TITLE = "Bordereau de versement d'archives";
    public static final String SECOND_TITLE = "Détail des unités archivistiques de type répertoire et dossiers:";


    public void generateDocumentHeader(TextDocument document, CustomerDto myCustomer,
        Resource customerLogo) throws IOException, URISyntaxException {

        String imgFile;

        Border border = new Border(Color.WHITE, 1.0, StyleTypeDefinitions.SupportedLinearMeasure.PT);

        Table headerTable = document.addTable(1, 2);
        headerTable.getRowByIndex(0).setHeight(35, false);
        headerTable.getColumnByIndex(1).setWidth(35);
        Font font = headerTable.getCellByPosition(0,0).getFont();
        font.setSize(11);
        headerTable.getCellByPosition(0,0).setFont(font);
        headerTable.getCellByPosition(0,0).setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
        headerTable.getCellByPosition(1,0).setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);

        if(myCustomer != null) {
            Cell cell = headerTable.getCellByPosition(0,0);
            cell.addParagraph(myCustomer.getName());
            cell.addParagraph(myCustomer.getCompanyName());
            cell.addParagraph("");
            cell.addParagraph(myCustomer.getAddress().getStreet());
            cell.addParagraph(myCustomer.getAddress().getCity());
            cell.addParagraph(myCustomer.getAddress().getZipCode());
            cell.addParagraph(myCustomer.getAddress().getCountry());
        }

        if (myCustomer.isHasCustomGraphicIdentity() && customerLogo != null) {

            byte[] customerLogoBytes = customerLogo.getInputStream().readAllBytes();
            String customerLogoBase64Image = Base64.getEncoder().encodeToString(customerLogoBytes);
            byte[] customerLogoDecodedBytes = Base64
                .getDecoder()
                .decode(customerLogoBase64Image);

            imgFile = "src/main/resources/logo_ministere." + getExtensionByCustomerLogo(customerLogoBase64Image).toLowerCase();
            FileUtils.writeByteArrayToFile(new File(imgFile), customerLogoDecodedBytes);
            headerTable.getCellByPosition(1,0).setImage(new URI(imgFile));

            FileUtils.forceDelete(new File(imgFile));
        }

        addSpace(document);

    }
    public void generateFirstTitle(TextDocument document) {
        addSpace(document);
        generateTile(document, 22, FIRST_TITLE, StyleTypeDefinitions.HorizontalAlignmentType.CENTER);

    }

    public void generateSecondtTitle(TextDocument document) {
        generateTile(document, 14, SECOND_TITLE, StyleTypeDefinitions.HorizontalAlignmentType.LEFT);
    }

    /*
     * Méthode pour créer le premier tableau du rapport Ingest
     * Le tableau contient 2 informations sur le :
     * Le service producteur et le service versant
     *
     * */
    public void generateServicesTable(TextDocument document, Document manifest, JSONObject jsonObject)
        throws JSONException {

        addSpace(document);
        try {
            Table table = document.addTable(2, 2);
            Cell cellOne = table.getColumnByIndex(0).getCellByIndex(0);
            Cell cellTwo = table.getColumnByIndex(0).getCellByIndex(1);
            cellOne.setStringValue("Service producteur :");
            cellTwo.setStringValue("Service versant :");
            table.getColumnByIndex(0).setWidth(50);

            Font fontCellOne = cellOne.getFont();
            fontCellOne.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
            cellOne.setFont(fontCellOne);

            Font fontCellTwo = cellTwo.getFont();
            fontCellTwo.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
            cellTwo.setFont(fontCellTwo);

            table.getColumnByIndex(1).getCellByIndex(0)
                .setStringValue(getManifestPrincipalData(manifest,"OriginatingAgencyIdentifier"));
            table.getColumnByIndex(1).getCellByIndex(1)
                .setStringValue(getServiceVersant(jsonObject));

            addSpace( document);

        } catch (JSONException e) {
            LOGGER.error("Unable to get the data from the JsonObject : {}", e.getMessage());
            throw new JSONException("Unable to get the data from the JsonObject " + e.getMessage());
        }

    }

    /*
     * Méthode pour créer le 2eme tableau du rapport Ingest
     * Le tableau contient les informations sur le :
     * Numéro du versement, Présentation du contenu, Dates extrêmes et l'historique de conservation
     *
     * */
    public void generateDepositDataTable(TextDocument document, Document manifest, List<ArchiveUnitDto> archiveUnitDtoList) {

        addSpace( document);
        Table table = document.addTable(4, 2);
        table.getColumnByIndex(0).setWidth(50);

        Cell tableCellOne = table.getColumnByIndex(0).getCellByIndex(0);
        tableCellOne.setStringValue("Numéro du versement :");
        Font fontCellOne = tableCellOne.getFont();
        fontCellOne.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        tableCellOne.setFont(fontCellOne);

        Cell tableCellTwo = table.getColumnByIndex(0).getCellByIndex(1);
        tableCellTwo.setStringValue("Présentation du contenu :");
        Font fontCellTwo = tableCellTwo.getFont();
        fontCellTwo.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        tableCellTwo.setFont(fontCellTwo);

        Cell tableCellThree = table.getColumnByIndex(0).getCellByIndex(2);
        tableCellThree.setStringValue("Dates extrêmes :");
        Font fontCellThree = tableCellThree.getFont();
        fontCellThree.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        tableCellThree.setFont(fontCellThree);

        Cell tableCellFour = table.getColumnByIndex(0).getCellByIndex(3);
        tableCellFour.setStringValue("Historique des conservations :");
        Font fontCellFour = tableCellFour.getFont();
        fontCellFour.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        tableCellFour.setFont(fontCellFour);


        table.getColumnByIndex(1).getCellByIndex(0).setStringValue(getManifestPrincipalData(manifest,"MessageIdentifier"));
        table.getColumnByIndex(1).getCellByIndex(1).setStringValue(getManifestPrincipalData(manifest,"Comment"));
        if(!archiveUnitDtoList.isEmpty() && archiveUnitDtoList != null) {
            table.getColumnByIndex(1).getCellByIndex(2).addParagraph(
                "Date de début : " + getStartedDate(getArchiveUnitStartDatesList(archiveUnitDtoList)));
            table.getColumnByIndex(1).getCellByIndex(2).addParagraph(
                "Date de fin : " + getEndDate(getArchiveUnitEndDatesList(archiveUnitDtoList)));
        }
          table.getColumnByIndex(1).getCellByIndex(3).setStringValue(getCustodialHistory(manifest));

        addSpace( document);

    }

    /*
     * Méthode pour créer le 3eme tableau du rapport Ingest
     * Le tableau contient les informations sur le :
     * Nombre de fichiers binaires et Identifiant de l’opération d’entrée
     *
     * */
    public void generateOperationDataTable(TextDocument document, Document manifest, String id) {

        addSpace( document);
        Table table = document.addTable(2, 2);
        table.getColumnByIndex(0).setWidth(50);

        Cell cellOne = table.getColumnByIndex(0).getCellByIndex(0);
        cellOne.setStringValue("Nombre de fichiers binaires:");
        Font fontCellOne = cellOne.getFont();
        fontCellOne.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        cellOne.setFont(fontCellOne);

        Cell cellTwo = table.getColumnByIndex(0).getCellByIndex(1);
        cellTwo.setStringValue("Identifiant de l’opération d’entrée :");
        Font fontCellTwo = cellTwo.getFont();
        fontCellTwo.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        cellTwo.setFont(fontCellTwo);

        table.getColumnByIndex(1).getCellByIndex(0)
            .setStringValue(getBinaryFileNumber(manifest) + " fichiers");
        table.getColumnByIndex(1).getCellByIndex(1).setStringValue(id);

        addSpace( document);
        addSpace( document);

    }

    /*
     * Méthode pour créer le 4eme tableau du rapport Ingest
     * Le tableau contient les informations sur le :
     * Date de signature, Le responsable du versement et Le responsable du service d'archives
     *
     * */
    public void generateResponsibleSignatureTable(TextDocument document) {

        addSpace( document);
        Border border = new Border(Color.WHITE, 1.0, StyleTypeDefinitions.SupportedLinearMeasure.PT);

        Table table = document.addTable(2, 2);
        table.getRowByIndex(0).setHeight(25, false);
        table.getRowByIndex(1).setHeight(25, false);

        Cell tableCellOne = table.getColumnByIndex(0).getCellByIndex(0);
        tableCellOne.setStringValue("Date de signature : ");
        tableCellOne.addParagraph("");

        Cell tableCellTwo = table.getColumnByIndex(0).getCellByIndex(1);
        tableCellTwo.setStringValue("Le responsable du versement :");
        tableCellTwo.addParagraph("");

        Cell tableCellThree = table.getColumnByIndex(1).getCellByIndex(0);
        tableCellThree.setStringValue("Date de signature :");
        tableCellThree.addParagraph("");

        Cell tableCellFour = table.getColumnByIndex(1).getCellByIndex(1);
        tableCellFour.setStringValue("Le responsable du service d'archives :");
        tableCellFour.addParagraph("");

        tableCellOne.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
        tableCellTwo.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
        tableCellThree.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
        tableCellFour.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);

    }

    /*
     * Méthode pour créer tableau dynamique du rapport Ingest
     * Le tableau contient les informations sur le :
     * Identifiant SAE VAS, Titre, date de début et date de fin
     *
     * */
    public void generateArchiveUnitDetailsTable(TextDocument document, List<ArchiveUnitDto> archiveUnitDtoList) {

        Table dynamicTable = document.addTable(1,4);

        dynamicTable.getCellByPosition(0,0).addParagraph("Identifiant SAE VAS").setHorizontalAlignment(
            StyleTypeDefinitions.HorizontalAlignmentType.CENTER);
        dynamicTable.getCellByPosition(1,0).addParagraph("Titre").setHorizontalAlignment(
            StyleTypeDefinitions.HorizontalAlignmentType.CENTER);
        dynamicTable.getCellByPosition(2,0).addParagraph("Date de début").setHorizontalAlignment(
            StyleTypeDefinitions.HorizontalAlignmentType.CENTER);
        dynamicTable.getCellByPosition(3,0).addParagraph("Date de fin").setHorizontalAlignment(
            StyleTypeDefinitions.HorizontalAlignmentType.CENTER);

        // the Width of each row is 170
        dynamicTable.getColumnByIndex(0).setWidth(46.5);
        dynamicTable.getColumnByIndex(1).setWidth(77.5);
        dynamicTable.getColumnByIndex(2).setWidth(23);
        dynamicTable.getColumnByIndex(3).setWidth(23);



        archiveUnitDtoList.stream().forEach(archiveUnitDto -> {
            Row row = dynamicTable.appendRow();

            Font fontCellOne = row.getCellByIndex(0).getFont();
            fontCellOne.setSize(9);
            row.getCellByIndex(0).setFont(fontCellOne);

            Font fontCellTwo = row.getCellByIndex(1).getFont();
            fontCellTwo.setSize(9.5);
            row.getCellByIndex(1).setFont(fontCellTwo);

            row.getCellByIndex(0).addParagraph(archiveUnitDto.getSystemId());
            row.getCellByIndex(1).addParagraph(archiveUnitDto.getTitle());
            row.getCellByIndex(2).addParagraph(transformDate(archiveUnitDto.getStartDate().split("T")[0]));
            row.getCellByIndex(3).addParagraph(transformDate(archiveUnitDto.getEndDate().split("T")[0]));

        });


        Font fontCellOne = dynamicTable.getRowByIndex(0).getCellByIndex(0).getFont();
        fontCellOne.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        dynamicTable.getRowByIndex(0).getCellByIndex(0).setFont(fontCellOne);

        Font fontCellTwo = dynamicTable.getRowByIndex(0).getCellByIndex(1).getFont();
        fontCellTwo.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        dynamicTable.getRowByIndex(0).getCellByIndex(1).setFont(fontCellTwo);

        Font fontCellThree = dynamicTable.getRowByIndex(0).getCellByIndex(2).getFont();
        fontCellThree.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        dynamicTable.getRowByIndex(0).getCellByIndex(2).setFont(fontCellThree);

        Font fontCellFour = dynamicTable.getRowByIndex(0).getCellByIndex(3).getFont();
        fontCellFour.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        dynamicTable.getRowByIndex(0).getCellByIndex(3).setFont(fontCellFour);

        dynamicTable.getCellByPosition(0,0).setCellBackgroundColor(Color.GRAY);
        dynamicTable.getCellByPosition(1,0).setCellBackgroundColor(Color.GRAY);
        dynamicTable.getCellByPosition(2,0).setCellBackgroundColor(Color.GRAY);
        dynamicTable.getCellByPosition(3,0).setCellBackgroundColor(Color.GRAY);


    }

    public List<ArchiveUnitDto> getValuesForDynamicTable(Document atr, Document manifest) {

        List<ArchiveUnitDto> archiveUnitDtoList = new ArrayList<>();
        Map<String, String> map = getSystemIdValues(atr);
        manifest.getDocumentElement().normalize();
        NodeList nList = manifest.getElementsByTagName("ArchiveUnit");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                ArchiveUnitDto archiveUnitDto = new ArchiveUnitDto();
                Element eElement = (Element) nNode;

                if (map.get(eElement.getAttribute("id")) != null) {

                    archiveUnitDto.setId(eElement.getAttribute("id"));
                    archiveUnitDto.setTitle(eElement.getElementsByTagName("Title").getLength() == 0 ?
                        "_ _ _ _" :
                        eElement.getElementsByTagName("Title").item(0).getTextContent());
                    archiveUnitDto.setEndDate(eElement.getElementsByTagName("EndDate").getLength() == 0 ?
                        "_ _ _ _" :
                        eElement.getElementsByTagName("EndDate").item(0).getTextContent());
                    archiveUnitDto.setStartDate(eElement.getElementsByTagName("StartDate").getLength() == 0 ?
                        "_ _ _ _" :
                        eElement.getElementsByTagName("StartDate").item(0).getTextContent());
                    archiveUnitDto.setSystemId(map.get(eElement.getAttribute("id")));

                    archiveUnitDtoList.add(archiveUnitDto);
                }
            }
        }
        return archiveUnitDtoList;
    }

    public Document convertStringToXMLDocument(String xmlString) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } catch (Exception e) {
            LOGGER.error("Error while converting string to XML Document {}", e.getMessage());
            throw new IngestFileGenerationException("Error while converting string to XML Document {}", e);
        }
    }

    public String resourceAsString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            LOGGER.error("Error while converting string to Resource Document {}", e.getMessage());
            throw new UncheckedIOException("Error while converting string to Resource Document",e);
        }
    }

    private Map<String, String> getSystemIdValues(Document document) {

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

    private String getServiceVersant(JSONObject jsonObject) throws JSONException {
        if (jsonObject.toString().contains("submissionAgency")) {
            return jsonObject.get("submissionAgency").toString();
        }
        return jsonObject.get("originatingAgency").toString();
    }

    private String getManifestPrincipalData(Document document, String tageName) {
        document.getDocumentElement().normalize();
        return document.getElementsByTagName(tageName).item(0).getTextContent();
    }

    private String getCustodialHistory(Document document) {
        return document.getElementsByTagName("CustodialHistory").getLength() == 0 ?
            "historique indisponible" :
            document.getElementsByTagName("CustodialHistory").item(0).getTextContent();
    }

    private int getBinaryFileNumber(Document document) {
        return document.getElementsByTagName("BinaryDataObject").getLength();
    }

    private List<String> getArchiveUnitStartDatesList(List<ArchiveUnitDto> archiveUnitDtoList) {
        return archiveUnitDtoList.stream().map(ArchiveUnitDto::getStartDate ).filter(startDate->
            startDate != "_ _ _ _"
        ).collect(Collectors.toList());
    }

    private List<String> getArchiveUnitEndDatesList(List<ArchiveUnitDto> archiveUnitDtoList) {
        return archiveUnitDtoList.stream().map(ArchiveUnitDto::getEndDate ).filter(endDate->
            endDate != "_ _ _ _"
        ).collect(Collectors.toList());
    }

    private String transformDate(String date) {
        if(date != "_ _ _ _") {
            LocalDate finalDate = LocalDate.parse(date);
            return finalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        else {
            return date;
        }
    }

    private void addSpace(TextDocument document) {

        document.addParagraph("");
    }

    private String getExtensionByCustomerLogo(String customerLogoBase64Image) {

       return Extension.findExtensionFromValue(Character
           .toString(customerLogoBase64Image.charAt(0)))
           .toString();
    }

    private void generateTile(TextDocument document, int size, String tilte, StyleTypeDefinitions.HorizontalAlignmentType horizontalAlignmentType) {

        Paragraph paragraph = document.addParagraph(tilte);
        paragraph.setHorizontalAlignment(horizontalAlignmentType);
        Font font = paragraph.getFont();
        font.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        font.setSize(size);
        paragraph.setFont(font);
        addSpace(document);
    }

    private String getStartedDate(List<String> listOfDate) {
        if(listOfDate.size() > 0) {
            return transformDate(listOfDate.get(0).split("T")[0]);
        }
        return "_ _ _ _";
    }

    private String getEndDate(List<String> listOfDate) {
        if(listOfDate.size() > 0) {
            return transformDate(listOfDate.get(listOfDate.size() - 1).split("T")[0]);
        }
        return "_ _ _ _";
    }
}
