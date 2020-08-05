package fr.gouv.vitamui.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PdfFileGeneratorTest {

    public static final String GENERATED_PDF_NAME = "generated-pdf.pdf";

    private static final String TEST_DIRECTORY = "src/test/resources/";

    private static final String TMP_DIRECTORY = "/tmp/dlab/test/PdfFileGeneratorTest/";

    @BeforeClass
    public static void setUp() throws IOException {
        if (!new File(TMP_DIRECTORY).exists()) {
            Files.createDirectories(Paths.get(TMP_DIRECTORY));
        }
    }

    @Test
    public void testCreatePdfWithDynamicInfo() throws Exception {
        // The template contains "${data},${dynamic.field}"
        try (final InputStream templateInput = new FileInputStream(new File(TEST_DIRECTORY + "template-dynamic-info.odt"));
             final FileOutputStream pdfOutput = new FileOutputStream(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))) {
            final Map<String, Object> dataMap = new HashMap<>();
            final String[] dynamicFields = {"dynamic.field"};
            dataMap.put("data", "value1");
            dataMap.put("dynamic.field", "value2");

            PdfFileGenerator.createPdfWithDynamicInfo(templateInput, pdfOutput, dataMap, dynamicFields);
        }

        try (final PDDocument document = PDDocument.load(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))) {
            final String content = new PDFTextStripper().getText(document);
            final String[] results = content.split(",");
            Assert.assertEquals("value1", results[0].trim());
            Assert.assertEquals("value2", results[1].trim());
        }
    }

    @Test
    public void testCreatePdfWithMetadata() throws Exception {
        // The template contains "${data},${dynamic.field},${imageField}"
        try (final InputStream templateInput = new FileInputStream(new File(TEST_DIRECTORY + "template-metadata.odt"));
             final FileOutputStream pdfOutput = new FileOutputStream(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))) {
            final Map<String, Object> dataMap = new HashMap<>();
            final String[] dynamicFields = {"dynamic.field"};
            final String[] imageFields = {"imageField"};
            dataMap.put("data", "value1");
            dataMap.put("dynamic.field", "value2");
            dataMap.put("imageField", "value3");

            PdfFileGenerator.createPdfWithMetadata(templateInput, pdfOutput, dataMap, dynamicFields, imageFields);
        }

        try (final PDDocument document = PDDocument.load(new File(TMP_DIRECTORY + GENERATED_PDF_NAME))) {
            final String content = new PDFTextStripper().getText(document);
            final String[] results = content.split(",");
            Assert.assertEquals("value1", results[0].trim());
            Assert.assertEquals("value2", results[1].trim());
            Assert.assertEquals("value3", results[2].trim());
        }
    }

}
