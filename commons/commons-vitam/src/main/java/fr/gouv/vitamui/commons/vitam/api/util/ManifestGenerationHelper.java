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
package fr.gouv.vitamui.commons.vitam.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.xs.XSModel;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.Assert;

import com.thaiopensource.relaxng.translate.Driver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.sip.model.ArchiveTransfer;
import fr.gouv.vitamui.commons.sip.util.SIPConstant;
import fr.gouv.vitamui.commons.vitam.seda.ArchiveTransferType;
import fr.gouv.vitamui.commons.vitam.seda.ArchiveUnitType;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;
import jlibs.xml.xsd.XSParser;

/**
 * Builder allowing to generate a manifest for a SIP.
 *
 *
 */
public class ManifestGenerationHelper {

    private static final Jaxb2Marshaller marshaller = initMarshallerFactory();

    /**
     * Method allowing to init the marshaller factory.
     * @return The initialized factory.
     */
    protected static Jaxb2Marshaller initMarshallerFactory() {

        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(ArchiveTransfer.class, ArchiveTransferType.class, ArchiveUnitType.class);
        final Map<String, Object> properties = new HashMap<>();
        properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        properties.put(Marshaller.JAXB_SCHEMA_LOCATION, SIPConstant.SEDA_NAMESPACE + " " + SIPConstant.SEDA_NAMESPACE_XSD);
        marshaller.setMarshallerProperties(properties);
        return marshaller;
    }

    /**
     * Method allowing to convert an RNG file to a manifest.
     * @param rngFileStream Stream of RNG file content.
     * @param workspacePath Path of the workspace where temp files can be created during the process of conversion.
     * @return The stream of the XML content file.
     */
    public static ArchiveTransferType convertRngToManifest(final InputStream rngFileStream, final String workspacePath) {

        initWorkspace(workspacePath);

        final Path rngFilePath = Paths.get(workspacePath, String.format("profile-%s.rng", UUID.randomUUID()));
        final Path xsdFilePath = Paths.get(workspacePath, String.format("profile-%s.xsd", UUID.randomUUID()));

        try {

            //Create RNG file
            createRngFile(rngFileStream, rngFilePath);

            // Convert from RNG to XSD
            convertRNGFileToXSDFile(rngFilePath, xsdFilePath);

            // Convert from XSD to XML
            final InputStream result = convertXSDFileToXMLFile(xsdFilePath);

            // convert to ArchiveTransferType instance
            return unmarshalManifest(result);
        }
        finally {
            deleteTempFile(rngFilePath);
            deleteTempFile(xsdFilePath);
        }
    }

    /**
     * Method allowing to init the workspace in order to process the conversion.
     * @param workspacePath Path of the workspace.
     */
    protected static void initWorkspace(final String workspacePath) {

        Assert.isTrue(StringUtils.isNotBlank(workspacePath), "No workspace path has been set");

        final Path workspace = Paths.get(workspacePath);
        if (!Files.exists(workspace)) {
            try {
                Files.createDirectories(workspace);
            }
            catch (final IOException exception) {
                final String message = String.format("Unable to create the following workspace: %s => %s", workspace.toString(), exception.getMessage());
                throw new InternalServerException(message, exception);
            }
        }
    }

    /**
     * Method allowing to remove a temp file.
     * @param file File to delete.
     */
    protected static void deleteTempFile(final Path file) {

        Assert.notNull(file, "A nil file has been set");
        try {
            Files.deleteIfExists(file);
        }
        catch (final IOException exception) {
            final String message = String.format("Unable to remove the following temp file: %s => %s", file.toString(), exception.getMessage());
            throw new InternalServerException(message, exception);
        }
    }

    /**
     * Method allowing to create a RNG file.
     * @param rngFileStream Stream of RNG file content.
     * @param rngFilePath Path where the RNG content will be stored.
     */
    protected static void createRngFile(final InputStream rngFileStream, final Path rngFilePath) {

        Assert.notNull(rngFilePath, "A nil RNG path has been set.");
        Assert.notNull(rngFileStream, "A nil RNG stream has been set.");

        try {
            Files.copy(rngFileStream, rngFilePath);
        }
        catch (final IOException exception) {
            final String message = String.format("Unable to store the RNG content into a file : %s", exception.getMessage());
            throw new InternalServerException(message, exception);
        }
    }

    /**
     * Method allowing to convert an RNG file to an XSD file.
     *
     * @param rngFilePath Stream of the RNG file.
     * @param xsdFilePath
     * @return
     */
    protected static void convertRNGFileToXSDFile(final Path rngFilePath, final Path xsdFilePath) {

        Assert.notNull(rngFilePath, "A nil RNG path has been set.");
        Assert.notNull(xsdFilePath, "A nil XSD path has been set.");

        final Driver driver = new Driver();
        final int ret = driver.run(new String[] { rngFilePath.toString(), xsdFilePath.toString() });
        if (ret != 0) {
            throw new UnexpectedDataException(String.format("Error while processing the conversion from RNG to xsd (return code: %d)", ret));
        }
    }

    /**
     * Method allowing to convert an XSD file to an XML content.
     * @param xsdFilePath Path of the XSD file.
     * @return The generated XML content.
     */
    protected static InputStream convertXSDFileToXMLFile(final Path xsdFilePath) {

        Assert.notNull(xsdFilePath, "A nil XSD path has been set.");

        final XSModel xsModel = new XSParser().parse(xsdFilePath.toString());
        final XSInstance xsInstance = new XSInstance();

        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumElementsGenerated = 1;
        xsInstance.generateDefaultAttributes = true;
        xsInstance.generateOptionalAttributes = false;
        xsInstance.maximumRecursionDepth = 0;
        xsInstance.generateAllChoices = true;
        xsInstance.showContentModel = true;
        xsInstance.generateOptionalElements = true;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final XMLDocument sampleXml = new XMLDocument(new StreamResult(outputStream), false, 4, null);
            final QName rootElement = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveTransfer");
            xsInstance.generate(xsModel, rootElement, sampleXml);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
        catch (TransformerConfigurationException | IOException exception) {
            throw new UnexpectedDataException("Error while processing the conversion from XSD to XML : " + exception.getMessage(), exception);
        }
    }

    /**
     * Method allowing to convert the XML streamed content to <code>ArchiveTransferType</code>.
     * @param xmlStream Stream to convert.
     * @return The converted object.
     */
    public static ArchiveTransferType unmarshalManifest(final InputStream xmlStream) {
        try {
            final JAXBContext jaxbContext2 = JAXBContext.newInstance(ArchiveTransferType.class);
            final Unmarshaller jaxbUnmarshaller2 = jaxbContext2.createUnmarshaller();
            final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
            final XMLEventReader xmlEventReader = xmlFactory.createXMLEventReader(xmlStream);
            final JAXBElement<ArchiveTransferType> archiveTransferElement = jaxbUnmarshaller2.unmarshal(xmlEventReader, ArchiveTransferType.class);
            return archiveTransferElement.getValue();
        }
        catch (JAXBException | XMLStreamException exception) {
            final String message = String.format("An error occurred during the conversion of the manifest: %s", exception.getMessage());
            throw new InternalServerException(message, exception);
        }
    }

    /**
     * Method allowing to convert an <code>ArchiveTransferType</code> to a byte array.
     * @param archiveTransfer The manifest to convert.
     * @return The converted item.
     */
    public static byte[] marshal(final ArchiveTransferType archiveTransfer) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final Result result = new StreamResult(outputStream);
            marshaller.marshal(archiveTransfer, result);
            return outputStream.toByteArray();
        }
        catch (final IOException exception) {
            throw new UnexpectedDataException("error while marshling to xml file : " + exception.getMessage(), exception);
        }

    }

    /**
     * Method allowing to convert an <code>ArchiveTransfer</code> to a byte array.
     * @param archiveTransfer The manifest to convert.
     * @return The converted item.
     */
    public static byte[] marshal(final ArchiveTransfer archiveTransfer) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final Result result = new StreamResult(outputStream);
            marshaller.marshal(archiveTransfer, result);
            return outputStream.toByteArray();
        }
        catch (final IOException e) {
            throw new UnexpectedDataException("error while marshling to xml file : ", e.getMessage());
        }

    }

    /**
     * Method allowing to clone an <code>ArchiveUnitType</code>.
     * @param source Source to clone.
     * @return The cloned item.
     */
    public static ArchiveUnitType clone(final ArchiveUnitType source) {
        final byte[] content = marshal(source);
        try (InputStream stream = new ByteArrayInputStream(content)) {
            return unmarshalUnit(stream);
        }
        catch (final IOException exception) {
            throw new UnexpectedDataException("Error while cloning the unit : " + exception.getMessage(), exception);
        }
    }

    /**
     * Method allowing to convert an <code>ArchiveUnitType</code> to a byte array.
     * @param archiveUnitType The unit to convert.
     * @return The converted item.
     */
    protected static byte[] marshal(final ArchiveUnitType archiveUnitType) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final Result result = new StreamResult(outputStream);
            final QName qName = new QName(String.format("%s.%s", ArchiveUnitType.class.getPackage().getName(), ArchiveUnitType.class.getName()),
                    ArchiveUnitType.class.getName());
            final JAXBElement<ArchiveUnitType> root = new JAXBElement<>(qName, ArchiveUnitType.class, archiveUnitType);
            marshaller.marshal(root, result);
            return outputStream.toByteArray();
        }
        catch (final IOException exception) {
            throw new UnexpectedDataException("error while marshling to xml file : " + exception.getMessage(), exception);
        }

    }

    /**
     * Method allowing to convert the XML streamed content to <code>ArchiveTransferType</code>.
     * @param xmlStream Stream to convert.
     * @return The converted object.
     */
    protected static ArchiveUnitType unmarshalUnit(final InputStream xmlStream) {
        try {
            final JAXBContext jaxbContext2 = JAXBContext.newInstance(ArchiveUnitType.class);
            final Unmarshaller jaxbUnmarshaller2 = jaxbContext2.createUnmarshaller();
            final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
            final XMLEventReader xmlEventReader = xmlFactory.createXMLEventReader(xmlStream);
            final JAXBElement<ArchiveUnitType> archiveTransferElement = jaxbUnmarshaller2.unmarshal(xmlEventReader, ArchiveUnitType.class);
            return archiveTransferElement.getValue();
        }
        catch (JAXBException | XMLStreamException exception) {
            final String message = String.format("An error occurred during the conversion of the manifest: %s", exception.getMessage());
            throw new InternalServerException(message, exception);
        }
    }
}
