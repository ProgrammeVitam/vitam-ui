package fr.gouv.vitamui.commons.test.utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;

/**
 * Helper allowing to manage files during a test's context.
 *
 *
 */
public class FileUtils {

    public static Path recreate(final Path folder) {
        try {
            Files.createDirectories(folder);
            delete(folder);
            return Files.createDirectories(folder);
        }
        catch (final IOException exception) {
            throw new ApplicationServerException(exception.getMessage(), exception);
        }
    }

    public static void delete(final Path folder) {
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (final IOException exception) {
            throw new ApplicationServerException(exception.getMessage(), exception);
        }
    }

    public static void copy(final String resourceName, final String destinationPath, final Optional<String> destinationFilename) throws IOException {

        final Path destination = Paths.get(destinationPath);
        if (!Files.exists(destination)) {
            Files.createDirectories(destination);
        }
        final URL url = FileUtils.class.getClassLoader().getResource(resourceName);
        if (url == null) {
            throw new ApplicationServerException(String.format("No resource file has been found at the following location: %s", resourceName));
        }
        final Path sourceFilePath = Paths.get(url.getFile());
        Files.copy(sourceFilePath, Paths.get(destination.toString(), destinationFilename.orElse(sourceFilePath.getFileName().toString())));
    }

}
