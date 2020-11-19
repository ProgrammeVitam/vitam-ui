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
 *//*


package fr.gouv.vitamui.ingest.internal.server.service;

*/
/**
 *
 *//*


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.FileImageProvider;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;



public class DocxDocumentMergerAndConverter {
    */
/**
     * Takes file path as input and returns the stream opened on it
     * @param filePath
     * @return
     * @throws IOException
     *//*

    public InputStream loadDocumentAsStream(String filePath) throws IOException{
        //URL url =new File(filePath).toURL();
        URL url =new File(filePath).toURI().toURL();
        InputStream documentTemplateAsStream=null;
        documentTemplateAsStream= url.openStream();
        return documentTemplateAsStream;
    }
    */
/**
     * Loads the docx report
     * @param documentTemplateAsStream
     * @param freemarkerOrVelocityTemplateKind
     * @return
     * @throws IOException
     * @throws XDocReportException
     *//*

    public IXDocReport loadDocumentAsIDocxReport(InputStream documentTemplateAsStream, TemplateEngineKind freemarkerOrVelocityTemplateKind) throws IOException, XDocReportException{
        IXDocReport xdocReport = XDocReportRegistry.getRegistry().loadReport(documentTemplateAsStream, freemarkerOrVelocityTemplateKind);
        return xdocReport;
    }
    */
/**
     * Takes the IXDocReport instance, creates IContext instance out of it and puts variables in the context
     * @param report
     * @param variablesToBeReplaced
     * @return
     * @throws XDocReportException
     *//*

    public IContext replaceVariabalesInTemplateOtherThanImages(IXDocReport report, Map<String, Object> variablesToBeReplaced) throws XDocReportException{
        IContext context = report.createContext();
        for(Map.Entry<String, Object> variable: variablesToBeReplaced.entrySet()){
            context.put(variable.getKey(), variable.getValue());
        }
        return context;
    }
    */
/**
     * Takes Map of image variable name and fileptah of the image to be replaced. Creates IImageprovides and adds the variable in context
     * @param report
     * @param variablesToBeReplaced
     * @param context
     *//*

    public void replaceImagesVariabalesInTemplate(IXDocReport report, Map<String, String> variablesToBeReplaced, IContext context){

        FieldsMetadata metadata = new FieldsMetadata();
        for(Map.Entry<String, String> variable: variablesToBeReplaced.entrySet()){
            metadata.addFieldAsImage(variable.getKey());
            context.put(variable.getKey(), new FileImageProvider(new File(variable.getValue()),true));
        }
        report.setFieldsMetadata(metadata);

    }
    */
/**
     * Generates byte array as output from merged template
     * @param report
     * @param context
     * @return
     * @throws XDocReportException
     * @throws IOException
     *//*

    public byte[] generateMergedOutput(IXDocReport report,IContext context ) throws XDocReportException, IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        report.process(context, outputStream);
        return outputStream.toByteArray();
    }
    */
/**
     * Takes inputs and returns merged output as byte[]
     * @param templatePath
     * @param templateEngineKind
     * @param nonImageVariableMap
     * @param imageVariablesWithPathMap
     * @return
     * @throws IOException
     * @throws XDocReportException
     *//*

    public byte[] mergeAndGenerateOutput(String templatePath, TemplateEngineKind templateEngineKind, Map<String, Object> nonImageVariableMap,Map<String, String> imageVariablesWithPathMap ) throws IOException, XDocReportException{
        InputStream inputStream = loadDocumentAsStream(templatePath);
        IXDocReport xdocReport = loadDocumentAsIDocxReport(inputStream,templateEngineKind);
        IContext context = replaceVariabalesInTemplateOtherThanImages(xdocReport,nonImageVariableMap);
        replaceImagesVariabalesInTemplate(xdocReport, imageVariablesWithPathMap, context);
        byte[] mergedOutput = generateMergedOutput(xdocReport, context);
        return mergedOutput;
    }
}
*/
