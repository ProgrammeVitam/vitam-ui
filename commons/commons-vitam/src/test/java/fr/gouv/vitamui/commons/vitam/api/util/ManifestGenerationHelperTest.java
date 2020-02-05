package fr.gouv.vitamui.commons.vitam.api.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.test.utils.FileUtils;
import fr.gouv.vitamui.commons.vitam.seda.ArchiveTransferType;

public class ManifestGenerationHelperTest {

    private static final Path ROOT_PATH = Paths.get("/tmp/vitamui/ManifestGenerationHelperTest");

    @Before
    public void init() {
        FileUtils.recreate(ROOT_PATH);
    }

    @Test
    public void testCreateRngFile() throws IOException {
        final Path rngFilePath = Paths.get(ROOT_PATH.toString(), "rng-test-file.rng");

        final String resourceName = "profile.rng";
        final URL url = ManifestGenerationHelperTest.class.getClassLoader().getResource(resourceName);
        if (url == null) {
            throw new ApplicationServerException(String.format("No resource file has been found at the following location: %s", resourceName));
        }

        Assert.assertFalse(Files.exists(rngFilePath));
        ManifestGenerationHelper.createRngFile(new FileInputStream(url.getFile()), rngFilePath);
        Assert.assertTrue(Files.exists(rngFilePath));
        Assert.assertEquals(new String(Files.readAllBytes(rngFilePath)), new String(Files.readAllBytes(Paths.get(url.getFile()))));
    }

    @Test
    public void testConvertRNGFileToXSDFile() throws IOException {
        final Path rngFilePath = Paths.get(ROOT_PATH.toString(), "rng-test-file.rng");
        final Path xsdFilePath = Paths.get(ROOT_PATH.toString(), "xsd-test-file.xsd");

        final String rngResourceName = "profile.rng";
        final String xsdResourceName = "profile.xsd";
        FileUtils.copy(rngResourceName, rngFilePath.getParent().toString(), Optional.of("rng-test-file.rng"));
        final URL url = ManifestGenerationHelperTest.class.getClassLoader().getResource(xsdResourceName);
        if (url == null) {
            throw new ApplicationServerException(String.format("No resource file has been found at the following location: %s", xsdResourceName));
        }

        ManifestGenerationHelper.convertRNGFileToXSDFile(rngFilePath, xsdFilePath);
        Assert.assertTrue(Files.exists(rngFilePath));
        Assert.assertTrue(Files.exists(xsdFilePath));
        Assert.assertEquals(new String(Files.readAllBytes(xsdFilePath)), new String(Files.readAllBytes(Paths.get(url.getFile()))));
    }

    @Test
    public void testConvertXSDFileToXMLFile() throws IOException {

        final Path xsdFilePath = Paths.get(ROOT_PATH.toString(), "xsd-test-file.xsd");

        final String xsdResourceName = "profile.xsd";
        FileUtils.copy(xsdResourceName, xsdFilePath.getParent().toString(), Optional.of("xsd-test-file.xsd"));

        final InputStream stream = ManifestGenerationHelper.convertXSDFileToXMLFile(xsdFilePath);
        Assert.assertNotNull(stream);
        Assert.assertTrue(Files.exists(xsdFilePath));
        Assert.assertFalse(StringUtils.isBlank(convert(stream, StandardCharsets.UTF_8)));
    }

    @Test
    public void testConvertRngToManifest() throws IOException {

        final String resourceName = "profile.rng";
        final URL url = ManifestGenerationHelperTest.class.getClassLoader().getResource(resourceName);
        if (url == null) {
            throw new ApplicationServerException(String.format("No resource file has been found at the following location: %s", resourceName));
        }

        Assert.assertEquals(0, Files.list(ROOT_PATH).count());
        final ArchiveTransferType result = ManifestGenerationHelper.convertRngToManifest(new FileInputStream(url.getFile()), ROOT_PATH.toString());
        Assert.assertNotNull(result);
        Assert.assertEquals("MessageIdentifier1", result.getMessageIdentifier().getValue());
        Assert.assertEquals("ArchivalAgreement1", result.getArchivalAgreement().getValue());
        Assert.assertNotNull(result.getDataObjectPackage());
        Assert.assertNotNull(result.getDataObjectPackage().getDataObjectGroupOrBinaryDataObjectOrPhysicalDataObject());
        Assert.assertNotNull(result.getDataObjectPackage().getDescriptiveMetadata());
        Assert.assertEquals(1, result.getDataObjectPackage().getDescriptiveMetadata().getArchiveUnit().size());
        Assert.assertEquals("IdArcVitamUI", result.getArchivalAgency().getIdentifier().getValue());
        Assert.assertEquals("IdProdVitamUI", result.getTransferringAgency().getIdentifier().getValue());
        Assert.assertEquals(0, Files.list(ROOT_PATH).count());
    }

    @Test
    public void testConvertRngToManifest2() throws IOException {

        final String resourceName = "test.rng";
        final URL url = ManifestGenerationHelperTest.class.getClassLoader().getResource(resourceName);
        if (url == null) {
            throw new ApplicationServerException(String.format("No resource file has been found at the following location: %s", resourceName));
        }

        Assert.assertEquals(0, Files.list(ROOT_PATH).count());
        final ArchiveTransferType result = ManifestGenerationHelper.convertRngToManifest(new FileInputStream(url.getFile()), ROOT_PATH.toString());
        Files.write(Paths.get(ROOT_PATH.toString(), "test.xml"), ManifestGenerationHelper.marshal(result));
    }

    @Test
    public void testMarshall() throws IOException {

        final ArchiveTransferType source = new ArchiveTransferType();
        source.setId("1");
        final byte[] result = ManifestGenerationHelper.marshal(source);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length > 0);
    }

    protected String convert(final InputStream inputStream, final Charset charset) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
