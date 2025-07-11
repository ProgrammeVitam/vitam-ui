package fr.gouv.vitamui.commons.utils;

import fr.opensagres.xdocreport.document.images.FileImageProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Disabled //Tests sur des fonctionnalités non utilisées de vitamui + erreur librairie interne "fr.opensagres.xdocreport"
// => tests + fonctionnalités à supprimer?
public class PdfFileGeneratorTest {

    public static final String GENERATED_PDF_NAME = "generated-pdf.pdf";

    private static final String TEST_DIRECTORY = "src/test/resources/";

    private static final String TMP_DIRECTORY = "/tmp/dlab/test/PdfFileGeneratorTest/";

    @BeforeAll
    public static void setUp() throws IOException {
        if (!new File(TMP_DIRECTORY).exists()) {
            Files.createDirectories(Path.of(TMP_DIRECTORY));
        }
    }

    @Test
    public void testCreatePdfWithDynamicInfo() throws Exception {
        // The template contains "${data},${dynamic.field}"
        try (
            final InputStream templateInput = new FileInputStream(
                new File(TEST_DIRECTORY + "template-dynamic-info.odt")
            );
            final FileOutputStream pdfOutput = new FileOutputStream(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))
        ) {
            final Map<String, Object> dataMap = new HashMap<>();
            final String[] dynamicFields = { "dynamic.field" };
            dataMap.put("data", "value1");
            dataMap.put("dynamic.field", "value2");

            PdfFileGenerator.createPdfWithDynamicInfo(templateInput, pdfOutput, dataMap, dynamicFields);
        }

        try (final PDDocument document = PDDocument.load(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))) {
            final String content = new PDFTextStripper().getText(document);
            final String[] results = content.split(",");
            Assertions.assertEquals("value1", results[0].trim());
            Assertions.assertEquals("value2", results[1].trim());
        }
    }

    @Test
    public void testCreatePdfWithMetadata() throws Exception {
        // The template contains "${data},${dynamic.field},${imageField}"
        try (
            final InputStream templateInput = new FileInputStream(new File(TEST_DIRECTORY + "template-metadata.odt"));
            final FileOutputStream pdfOutput = new FileOutputStream(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))
        ) {
            final Map<String, Object> dataMap = new HashMap<>();
            final String[] dynamicFields = { "dynamic.field" };
            final String[] imageFields = { "imageField" };
            dataMap.put("data", "value1");
            dataMap.put("dynamic.field", "value2");
            dataMap.put("imageField", "value3");

            PdfFileGenerator.createPdfWithMetadata(templateInput, pdfOutput, dataMap, dynamicFields, imageFields);
        }

        try (final PDDocument document = PDDocument.load(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))) {
            final String content = new PDFTextStripper().getText(document);
            final String[] results = content.split(",");
            Assertions.assertEquals("value1", results[0].trim());
            Assertions.assertEquals("value2", results[1].trim());
            Assertions.assertEquals("value3", results[2].trim());
        }
    }

    @Test
    public void testCreatePdfWithMetadataAndHtml() throws Exception {
        // The template contains "${data},${dynamic.field},${imageField},${htmlField}"
        try (
            final InputStream templateInput = new FileInputStream(
                new File(TEST_DIRECTORY + "template-metadata-html.odt")
            );
            final FileOutputStream pdfOutput = new FileOutputStream(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))
        ) {
            final Map<String, Object> dataMap = new HashMap<>();
            final String[] dynamicFields = { "dynamic.field" };
            final String[] imageFields = { "imageField" };
            final String[] htmlFields = { "htmlField" };
            dataMap.put("data", "data");
            dataMap.put("dynamic.field", "dynamic field");
            dataMap.put("imageField", new FileImageProvider(Path.of(TEST_DIRECTORY, "image.png").toFile()));

            dataMap.put("htmlField", "<span><i>html</i> field</span>");

            PdfFileGenerator.createPdfWithMetadataAndHtml(
                templateInput,
                pdfOutput,
                dataMap,
                dynamicFields,
                imageFields,
                htmlFields
            );
        }

        try (final PDDocument document = PDDocument.load(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))) {
            final String content = new PDFTextStripper().getText(document);
            final String[] results = content.split(",");
            Assertions.assertEquals("data", results[0].trim());
            Assertions.assertEquals("dynamic field", results[1].trim());
            Assertions.assertEquals("", results[2].trim());
            Assertions.assertEquals("html field", results[3].trim());
        }
    }
}
