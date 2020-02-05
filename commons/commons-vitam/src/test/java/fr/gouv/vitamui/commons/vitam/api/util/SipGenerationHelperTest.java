package fr.gouv.vitamui.commons.vitam.api.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.gouv.vitamui.commons.sip.util.SIPConstant;
import fr.gouv.vitamui.commons.test.utils.FileUtils;

public class SipGenerationHelperTest {

    private static final Path ROOT_PATH = Paths.get("/tmp/vitamui/SipGenerationHelperTest");

    @Before
    public void init() {
        FileUtils.recreate(ROOT_PATH);
    }

    @Test
    public void testSipGenerationWithPath() throws IOException {

        final Path generatedSipPath = Files.createTempFile(Paths.get(ROOT_PATH.toString()), "sip", "");
        final Path unzipSipPath = Paths.get(ROOT_PATH.toString(), "unzip");

        final Path filesPath = Paths.get(ROOT_PATH.toString(), "files");
        Files.createDirectories(Paths.get(filesPath.toString(), SIPConstant.CONTENT_FOLDER_NAME));
        Files.write(Paths.get(filesPath.toString(), "manifest.xml"), new String("Manifest").getBytes());
        Files.write(Paths.get(filesPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc.txt"), new String("Doc1").getBytes());
        Files.write(Paths.get(filesPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc2.txt"), new String("Doc2").getBytes());

        final FileInputStream manifestStream = new FileInputStream(Paths.get(filesPath.toString(), "manifest.xml").toFile());
        final Path file1 = Paths.get(filesPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc.txt");
        final Path file2 = Paths.get(filesPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc2.txt");

        Assert.assertTrue(Files.size(generatedSipPath) == 0);
        SipGenerationHelper.generate(manifestStream, Arrays.asList(file1, file2), new FileOutputStream(generatedSipPath.toFile()));
        Assert.assertTrue(Files.exists(generatedSipPath));
        Assert.assertTrue(Files.size(generatedSipPath) > 0);

        // Decompress
        final ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(generatedSipPath.toFile()));
        ZipEntry entry = null;
        while ((entry = zipInputStream.getNextEntry()) != null) {

            final String fileName = entry.getName();
            final Path newFile = Paths.get(unzipSipPath.toString(), fileName);
            if (!Files.exists(newFile.getParent())) {
                Files.createDirectories(newFile.getParent());
            }
            if (entry.isDirectory()) {
                continue;
            }
            Files.copy(zipInputStream, newFile);
        }
        zipInputStream.closeEntry();
        zipInputStream.close();

        // Checks

        Assert.assertTrue(Files.exists(Paths.get(unzipSipPath.toString(), "manifest.xml")));
        Assert.assertTrue(Files.exists(Paths.get(unzipSipPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc.txt")));
        Assert.assertTrue(Files.exists(Paths.get(unzipSipPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc2.txt")));
        Assert.assertEquals(new String(Files.readAllBytes(Paths.get(unzipSipPath.toString(), "manifest.xml"))),
                new String(Files.readAllBytes(Paths.get(filesPath.toString(), "manifest.xml"))));
        Assert.assertEquals(new String(Files.readAllBytes(Paths.get(unzipSipPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc.txt"))),
                new String(Files.readAllBytes(Paths.get(filesPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc.txt"))));
        Assert.assertEquals(new String(Files.readAllBytes(Paths.get(unzipSipPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc2.txt"))),
                new String(Files.readAllBytes(Paths.get(filesPath.toString(), SIPConstant.CONTENT_FOLDER_NAME, "doc2.txt"))));

    }

    protected void copyFolder(final Path src, final Path dest) throws IOException {
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    protected void copy(final Path source, final Path dest) {
        try {
            Files.copy(source, dest);
        }
        catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
