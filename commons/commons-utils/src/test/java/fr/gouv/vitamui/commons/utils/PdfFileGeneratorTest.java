package fr.gouv.vitamui.commons.utils;

import freemarker.core.InvalidReferenceException;
import freemarker.core.ParseException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfFileGeneratorTest {

    @Test
    void shouldRenderUtf8ValuesListsAndEscapeHtml() throws Exception {
        String template =
            """
            <html><body><h1>${title}</h1>
            [#list values as value]<p>${value}</p>[/#list]
            <p>${optional!"Absent"}</p></body></html>
            """;
        byte[] pdf = render(
            template,
            Map.of("title", "Relevé de valeur probante", "values", List.of("été & archives", "<b>texte</b>"))
        );

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertEquals(1, document.getNumberOfPages());
            assertTrue(text.contains("Relevé de valeur probante"));
            assertTrue(text.contains("été & archives"));
            assertTrue(text.contains("<b>texte</b>"));
            assertTrue(text.contains("Absent"));
            assertEquals("openhtmltopdf.com", document.getDocumentInformation().getProducer());
        }
    }

    @Test
    void shouldPaginateWithoutLosingContent() throws Exception {
        String template =
            """
            <html><body>[#list 1..100 as index]
            <p>Vérification ${index}</p>[/#list]</body></html>
            """;
        try (PDDocument document = Loader.loadPDF(render(template, Map.of()))) {
            assertTrue(document.getNumberOfPages() > 1);
            String text = new PDFTextStripper().getText(document);
            for (int index = 1; index <= 100; index++) {
                assertTrue(text.contains("Vérification " + index + "\n"));
            }
        }
    }

    @Test
    void shouldRejectMissingRequiredValue() {
        assertThrows(InvalidReferenceException.class, () -> render("<html><body>${required}</body></html>", Map.of()));
    }

    @Test
    void shouldRejectInvalidTemplate() {
        assertThrows(ParseException.class, () -> render("<html><body>[#list values as value]</body></html>", Map.of()));
    }

    @Test
    void shouldNotResolveExternalResources(@TempDir Path directory) throws Exception {
        Path image = directory.resolve("image.png");
        Files.write(
            image,
            Base64.getDecoder()
                .decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=")
        );
        String template =
            """
            <html><head><link rel="stylesheet" href="https://example.invalid/style.css" /></head>
            <body><p>Rapport autonome</p><img src="%s" /></body></html>
            """.formatted(image.toUri());
        try (PDDocument document = Loader.loadPDF(render(template, Map.of()))) {
            assertTrue(new PDFTextStripper().getText(document).contains("Rapport autonome"));
            assertFalse(document.getPage(0).getResources().getXObjectNames().iterator().hasNext());
        }
    }

    @Test
    void shouldLeaveCallerStreamsOpen() throws Exception {
        boolean[] closed = { false, false };
        var input = new ByteArrayInputStream("<html><body>Rapport</body></html>".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() {
                closed[0] = true;
            }
        };
        var output = new ByteArrayOutputStream() {
            @Override
            public void close() {
                closed[1] = true;
            }
        };

        PdfFileGenerator.createPdf(input, output, Map.of());

        assertFalse(closed[0]);
        assertFalse(closed[1]);
        assertTrue(output.size() > 0);
    }

    private byte[] render(String template, Map<String, Object> data) throws Exception {
        try (
            var input = new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8));
            var output = new ByteArrayOutputStream()
        ) {
            PdfFileGenerator.createPdf(input, output, data);
            return output.toByteArray();
        }
    }
}
