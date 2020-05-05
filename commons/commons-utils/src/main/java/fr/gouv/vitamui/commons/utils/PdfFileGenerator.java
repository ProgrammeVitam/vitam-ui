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
package fr.gouv.vitamui.commons.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

public class PdfFileGenerator {

    /**
     * Method allowing to fill an odt template and generate a pdf file from it.
     *
     * @param odtTemplateInputStream the odt template to fill and convert to pdf /!\ the owner is responsible for closing the stream.
     * @param pdfOutputStream        the generated pdf file /!\ the owner is responsible for closing the stream.
     * @param dataMap                the object containing the data to add to the template.
     * @throws Exception
     */
    public static void createPdf(final InputStream odtTemplateInputStream, final OutputStream pdfOutputStream, final Map<String, Object> dataMap)
            throws Exception {
        final IXDocReport xdocGenerator = createXDocReport(odtTemplateInputStream);
        createPdfDocument(xdocGenerator, dataMap, pdfOutputStream);
    }

    /**
     * Method allowing to fill an odt template with dynamic and static data and generate a pdf file from it.
     *
     * @param odtTemplateInputStream the odt template to fill and convert to pdf /!\ the owner is reponsible for closing the stream.
     * @param pdfOutputStream        the generated pdf file /!\ the owner is reponsible for closing the stream.
     * @param dataMap                the object containing the data to add to the template.
     * @param dynamicFields          the dynamic fields to add to the report to enable the creation of the pdf.
     * @throws Exception
     */
    public static void createPdfWithDynamicInfo(final InputStream odtTemplateInputStream, final OutputStream pdfOutputStream, final Map<String, Object> dataMap,
            final String... dynamicFields) throws Exception {
        final IXDocReport xdocGenerator = generateXDocReport(odtTemplateInputStream, dynamicFields, new String[]{});
        createPdfDocument(xdocGenerator, dataMap, pdfOutputStream);
    }

    /**
     * Method allowing to fill an odt template with images, dynamic and static data and generate a pdf file from it.
     *
     * @param odtTemplateInputStream the odt template to fill and convert to pdf /!\ the owner is responsible for closing the stream.
     * @param pdfOutputStream        the generated pdf file /!\ the owner is responsible for closing the stream.
     * @param dataMap                the object containing the data to add to the template.
     * @param dynamicFields          the dynamic fields to add to the report to enable the creation of the pdf.
     * @param imageFields            the image fields to add to the report to enable the creation of the pdf.
     * @throws Exception
     */
    public static void createPdfWithMetadata(final InputStream odtTemplateInputStream, final OutputStream pdfOutputStream, final Map<String, Object> dataMap,
            final String[] dynamicFields, final String[] imageFields) throws Exception {
        final IXDocReport xdocGenerator = generateXDocReport(odtTemplateInputStream, dynamicFields, imageFields);
        createPdfDocument(xdocGenerator, dataMap, pdfOutputStream);
    }

    /**
     * Method allowing to get the report to generate a pdf file.
     *
     * @param odtTemplateInputStream the odt template to fill and convert to pdf /!\ the owner is responsible for closing the stream.
     * @return the XDocReport.
     * @throws Exception
     */
    protected static IXDocReport createXDocReport(final InputStream odtTemplateInputStream) throws Exception {
        return XDocReportRegistry.getRegistry().loadReport(odtTemplateInputStream, TemplateEngineKind.Freemarker);
    }

    /**
     * Method allowing to to generate the dynamic and/or static pdf file.
     * @param xdocGenerator the XDocGenerator used to generate the pdf file.
     * @param dataMap the object containing the data to add to the template.
     * @param pdfOutputStream the generated pdf file /!\ the owner is reponsible for closing the stream.
     * @throws Exception
     */
    protected static void createPdfDocument(final IXDocReport xdocGenerator, final Map<String, Object> dataMap, final OutputStream pdfOutputStream)
            throws Exception {
        final IContext context = xdocGenerator.createContext();
        dataMap.entrySet().forEach(entry -> context.put(entry.getKey(), entry.getValue()));
        final Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);
        xdocGenerator.convert(context, options, pdfOutputStream);
    }

    private static IXDocReport generateXDocReport(final InputStream odtTemplateInputStream, final String[] dynamicFields, final String[] imageFields)
            throws Exception {
        final IXDocReport xdocGenerator = createXDocReport(odtTemplateInputStream);
        final FieldsMetadata fieldsMetadata = new FieldsMetadata();
        for (final String dynamicField : dynamicFields) {
            fieldsMetadata.addFieldAsList(dynamicField);
        }
        for (final String imageField : imageFields) {
            fieldsMetadata.addFieldAsImage(imageField);
        }
        xdocGenerator.setFieldsMetadata(fieldsMetadata);
        return xdocGenerator;
    }

}
