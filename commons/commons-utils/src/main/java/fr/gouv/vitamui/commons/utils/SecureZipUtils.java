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

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Tool allowing to unzip securely compress files into a ZIP archive.
 */
public class SecureZipUtils {

    private SecureZipUtils() {}

    /**
     * Securely unzip a compressed file
     * @param zipFilePath zip file path
     * @param unzipFolderPath unzip folder, if not exist ll be created
     * @throws SecurityException if attack is being detected
     */
    public static void unzipFolder(final String zipFilePath, final String unzipFolderPath)
        throws IOException, SecurityException {
        Path zipFile = Paths.get(zipFilePath);

        try (InputStream zipFileStream = new FileInputStream(zipFile.toFile())) {
            unzipFolder(zipFileStream, unzipFolderPath);
        }
    }

    /**
     * Securely unzip a compressed file
     * @param zipFileStream zip input stream
     * @param unzipFolderPath unzip folder, if not exist ll be created
     * @throws SecurityException if attack is being detected
     */
    public static void unzipFolder(InputStream zipFileStream, String unzipFolderPath) throws IOException {
        Path unzipFolder = Paths.get(unzipFolderPath);
        if (unzipFolder.toFile().exists() && !unzipFolder.toFile().isDirectory()) {
            throw new IOException("The specified destination is not a directory.");
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFileStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Path newPath = zipSlipProtect(zipEntry, unzipFolder);

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null && Files.notExists(newPath.getParent())) {
                        Files.createDirectories(newPath.getParent());
                    }
                    Files.copy(zipInputStream, newPath);
                }
            }
            zipInputStream.closeEntry();
        }
    }

    /**
     * check if there is a symbolic link external to the zip folder(possible ZiSlip Attack)
     * @param zipEntry the zip entries to check
     * @param targetDir the target dir where the zip ll be extracted
     * @return the normalized path
     * @throws SecurityException throw security exception where a threat is detected
     */
    public static Path zipSlipProtect(final ZipEntry zipEntry, final Path targetDir) throws SecurityException {
        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new SecurityException("possible Zip slip attack " + zipEntry.getName());
        }

        return normalizePath;
    }

    /**
     * Method allowing to generate a zip folder containing the documents from the map.
     * @param filePaths List containing the files paths to add to the folder
     * @param zipOutputStream the stream to write to. The owner of the outputstream is responsible for closing the stream.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void zipFiles(final List<Path> filePaths, final OutputStream zipOutputStream) throws IOException {
        try (final ZipOutputStream zipFolder = new ZipOutputStream(zipOutputStream)) {
            for (final Path filePath : filePaths) {
                canSecurelyZipFile(filePath.toFile());

                ZipEntry zipEntry = new ZipEntry(filePath.getFileName().toString());
                zipFolder.putNextEntry(zipEntry);
                Files.copy(filePath, zipFolder);
                zipFolder.closeEntry();
            }
        }
    }

    /**
     * Method allowing to generate a zip folder containing the documents from the map.
     * @param streamMap Map containing the filename and stream to add to the folder
     * @param zipOutputStream the stream to write to. The owner of the outputstream is responsible for closing the stream.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void zipStreams(final Map<String, InputStream> streamMap, final OutputStream zipOutputStream)
        throws IOException {
        try (final ZipOutputStream zipFolderStream = new ZipOutputStream(zipOutputStream)) {
            for (final Map.Entry<String, InputStream> entry : streamMap.entrySet()) {
                final var filename = entry.getKey();
                final var inputStream = entry.getValue();

                isFilenameSecure(filename);

                ZipEntry zipEntry = new ZipEntry(filename);
                zipFolderStream.putNextEntry(zipEntry);
                inputStream.transferTo(zipFolderStream);
                zipFolderStream.closeEntry();
            }
        }
    }

    /**
     * Method allowing to generate a zip folder containing the files in a folder
     * @param sourceFolderPath List containing the files paths to add to the folder
     * @param zipOutputStream the stream to write to. The owner of the outputstream is responsible for closing the stream.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void zipFolder(final String sourceFolderPath, final OutputStream zipOutputStream) throws IOException {
        try (ZipOutputStream realZipOutputStream = new ZipOutputStream(zipOutputStream)) {
            Path folderToZipPath = Paths.get(sourceFolderPath);
            final List<Path> filePaths;
            try (final var fileStream = Files.walk(folderToZipPath)) {
                filePaths = fileStream.collect(Collectors.toList());
            }
            for (Path filePath : filePaths) {
                canSecurelyZipFile(filePath.toFile());
                if (Files.isDirectory(filePath)) {
                    String name = folderToZipPath.relativize(filePath).toString();
                    if (!name.isEmpty()) {
                        realZipOutputStream.putNextEntry(new ZipEntry(name + (name.endsWith("/") ? "" : "/")));
                        realZipOutputStream.closeEntry();
                    }
                    continue;
                }

                ZipEntry zipEntry = new ZipEntry(folderToZipPath.relativize(filePath).toString());
                realZipOutputStream.putNextEntry(zipEntry);
                Files.copy(filePath, realZipOutputStream);
                realZipOutputStream.closeEntry();
            }
        }
    }

    /**
     * if we can securely zip file without risk of passing a zip slip to another service
     * @param file file to check
     */
    private static void canSecurelyZipFile(File file) {
        if (FileUtils.isSymlink(file)) {
            throw new SecurityException("Can't zip file with symlink in it");
        }
    }

    /**
     * Method allowing to check if a filename is valid for a zip file
     * @param filename filename to check
     */
    private static void isFilenameSecure(String filename) {
        SecurePathUtils.checkDirectoryTraversalVulnerability(filename);

        if (filename.startsWith("/") || filename.startsWith("\\")) {
            throw new SecurityException("Can't zip file with absolute path in it");
        }
    }
}
