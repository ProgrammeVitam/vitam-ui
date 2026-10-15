/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * a digital archiving front-office system for the secure and efficient high
 * volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.utils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PdfFileGenerator {

    private static final Configuration FREEMARKER_CONFIGURATION = createFreemarkerConfiguration();

    private PdfFileGenerator() {}

    /**
     * Renders an HTML/FreeMarker template to PDF.
     *
     * @param htmlTemplateInputStream the template to render; the caller is responsible for closing the stream
     * @param pdfOutputStream the generated PDF; the caller is responsible for closing the stream
     * @param dataMap the data to add to the template
     * @throws Exception if template processing or PDF generation fails
     */
    public static void createPdf(
        final InputStream htmlTemplateInputStream,
        final OutputStream pdfOutputStream,
        final Map<String, Object> dataMap
    ) throws Exception {
        final Template template = new Template(
            "probativevaluereport.ftlh",
            new InputStreamReader(htmlTemplateInputStream, StandardCharsets.UTF_8),
            FREEMARKER_CONFIGURATION
        );

        final StringWriter htmlWriter = new StringWriter();
        template.process(dataMap, htmlWriter);

        new PdfRendererBuilder()
            .useFastMode()
            .useUriResolver((baseUri, uri) -> null)
            .withHtmlContent(htmlWriter.toString(), null)
            .toStream(pdfOutputStream)
            .run();
    }

    private static Configuration createFreemarkerConfiguration() {
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_35);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setOutputFormat(HTMLOutputFormat.INSTANCE);
        configuration.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        configuration.setAutoEscapingPolicy(Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        return configuration;
    }
}
