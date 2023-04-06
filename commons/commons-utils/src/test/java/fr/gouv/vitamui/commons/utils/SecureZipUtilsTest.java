package fr.gouv.vitamui.commons.utils;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecureZipUtilsTest {

    private static final String UNZIP_FOLDER = "/tmp/vitamui-zipTesting/";
    private static final String RESSOURCES_BASE_PATH = "src/test/resources/";
    private static final String GOOD_ZIP_FILE = RESSOURCES_BASE_PATH + "good-zip.zip";
    private static final String EVIL_ZIP_FILE = RESSOURCES_BASE_PATH + "zip-slip.zip";
    private static final String EVIL_FILE_PATH = "/tmp/evil.txt";

    @BeforeEach
    public void setup() throws IOException {
        var unzipFolderFile = new File(UNZIP_FOLDER);
        if (unzipFolderFile.exists() && UNZIP_FOLDER.startsWith("/tmp/")/* safe delete if the path is other than tmp*/) {
            FileUtils.deleteDirectory(unzipFolderFile);
        }
    }

    @AfterEach
    public void teardown() throws IOException {
        setup();
        File evilFile = new File(EVIL_FILE_PATH);
        if (evilFile.exists()) {
            evilFile.delete();
        }
    }

    @Test
    public void testUnzipFolderKo() {

        assertThrows(SecurityException.class, () -> SecureZipUtils.unzipFolder(EVIL_ZIP_FILE, UNZIP_FOLDER));

        File evilFile = new File(EVIL_FILE_PATH);
        Assertions.assertFalse(evilFile.exists());
    }

    @Test
    public void testUnzipFolderOk() throws IOException, SecurityException {
        testUnzipFolder(GOOD_ZIP_FILE, UNZIP_FOLDER);
    }

    void testUnzipFolder(String zipFile, String unzipFolder) throws IOException, SecurityException {

        SecureZipUtils.unzipFolder(zipFile, unzipFolder);

        File file = new File(unzipFolder + "directory/subdirectory/file.txt");
        Assertions.assertTrue(file.exists());

        Assertions.assertEquals("content\n", FileUtils.readFileToString(file, "UTF-8"));

        file = new File(unzipFolder + "directory/file.txt");
        Assertions.assertTrue(file.exists());
        file = new File(unzipFolder + "/file.txt");
        Assertions.assertTrue(file.exists());
    }

    @Test
    public void testZipFolderOk() throws Exception {
        final String zipFolder = UNZIP_FOLDER + "testZip/";
        SecureZipUtils.unzipFolder(GOOD_ZIP_FILE, zipFolder);

        final String sipFilePath = UNZIP_FOLDER +"/zipOutput.zip";
        try(var zipFile = new FileOutputStream(sipFilePath)) {
            SecureZipUtils.zipFolder(zipFolder, zipFile);
        }
        FileUtils.deleteDirectory(Paths.get(zipFolder).toFile());

        testUnzipFolder(sipFilePath, zipFolder);
    }

    @Test
    public void testZipFolderKoFileSymlink() throws Exception {
        final String zipFolder = UNZIP_FOLDER + "testZip/";
        SecureZipUtils.unzipFolder(GOOD_ZIP_FILE, zipFolder);

        Path linkedFile = Paths.get(UNZIP_FOLDER + "/file.txt");
        FileUtils.writeStringToFile(linkedFile.toFile(), "Hello File", Charset.defaultCharset());
        Path link = Paths.get(zipFolder + "/link");
        Files.createSymbolicLink(link, linkedFile);

        try(var zipFile = new FileOutputStream(UNZIP_FOLDER +"/zipOutput.zip")) {
            Assertions.assertThrows(SecurityException.class, () -> SecureZipUtils.zipFolder(zipFolder, zipFile));
        }
        finally {
            Files.delete(link);
        }
    }

    @Test
    public void testZipFolderKoFolderSymlink() throws Exception {
        final String zipFolder = UNZIP_FOLDER + "testZip/";
        SecureZipUtils.unzipFolder(GOOD_ZIP_FILE, zipFolder);

        Path linkedFile = Paths.get(UNZIP_FOLDER + "/testZip");
        Path link = Paths.get(zipFolder + "/link");
        Files.createSymbolicLink(link, linkedFile);

        try(var zipFile = new FileOutputStream(UNZIP_FOLDER +"/zipOutput.zip")) {
            Assertions.assertThrows(SecurityException.class, () -> SecureZipUtils.zipFolder(zipFolder, zipFile));
        }
        finally {
            Files.delete(link);
        }
    }

    @Test
    public void testZipFilesOk() throws Exception {
        final String zipFolder = UNZIP_FOLDER + "testZip/";
        SecureZipUtils.unzipFolder(GOOD_ZIP_FILE, zipFolder);

        List<Path> filesToZip = Arrays.asList(Paths.get(zipFolder + "file.txt"));

        final String zipFilePath = UNZIP_FOLDER +"/zipOutput.zip";
        try(var zipFile = new FileOutputStream(zipFilePath)) {
            SecureZipUtils.zipFiles(filesToZip, zipFile);
        }
        FileUtils.deleteDirectory(Paths.get(zipFolder).toFile());

        SecureZipUtils.unzipFolder(zipFilePath, UNZIP_FOLDER);
        File file = new File(UNZIP_FOLDER + "file.txt");
        Assertions.assertTrue(file.exists());

        Assertions.assertEquals("content\n", FileUtils.readFileToString(file, "UTF-8"));
    }

    @Test
    public void testZipStreamsOk() throws IOException {
        final String zipFolder = UNZIP_FOLDER + "testZip/";
        SecureZipUtils.unzipFolder(GOOD_ZIP_FILE, zipFolder);

        final Map<String, InputStream> filesToZip = Map.of(
                "directory/subdirectory/file.txt", new FileInputStream(zipFolder + "directory/subdirectory/file.txt"),
                "directory/file.txt", new FileInputStream(zipFolder + "directory/file.txt"),
                "file.txt", new FileInputStream(zipFolder + "file.txt"));

        final String zipFilePath = UNZIP_FOLDER +"/zipOutput.zip";
        try(var zipFile = new FileOutputStream(zipFilePath)) {
            SecureZipUtils.zipStreams(filesToZip, zipFile);
        }
        FileUtils.deleteDirectory(Paths.get(zipFolder).toFile());

        testUnzipFolder(zipFilePath, zipFolder);
    }

    @Test
    @ParameterizedTest
    @ValueSource(strings = {"../file.pdf",
        "/../etc/hosts/file.pdf",
        "file.pdf\n../../mmm",
        "file.pdf\r../../mmm",
        "/file\0.exe.pdf",
        "../../file.txt"})
    void testZipStreamsKoSecurity(final String filename) throws IOException {
        final Map<String, InputStream> filesToZip = Map.of(filename, InputStream.nullInputStream());

        var unzipFolderFile = new File(UNZIP_FOLDER);
        if (!unzipFolderFile.exists()) {
            Files.createDirectories(Paths.get(UNZIP_FOLDER));
        }

        final String zipFilePath = UNZIP_FOLDER +"zipOutput.zip";
        try(var zipFile = new FileOutputStream(zipFilePath)) {
            Assertions.assertThrows(SecurityException.class, () -> SecureZipUtils.zipStreams(filesToZip, zipFile));
        }
    }


}
