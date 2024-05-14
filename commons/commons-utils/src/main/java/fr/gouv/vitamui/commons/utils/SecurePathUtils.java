package fr.gouv.vitamui.commons.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class SecurePathUtils {

    private SecurePathUtils() {}

    /**
     * regex to validate filename without path
     * alphanumeric chars, _, -," "and "." but no more than "." in sequence
     */
    public static final String FILENAME_VALIDATION_REGEX = "^(?!.*(\\.)\\1+)([\\w\\- \\.])+$";

    /**
     * exception message
     */
    public static final String DIRECTORY_TRAVERSAL_ATTEMPT = "Directory traversal attempt ? ";

    /**
     * check if there is a directory traversal attempt in the file path
     * @param fullFilePath file path to check
     */
    public static void checkDirectoryTraversalVulnerability(String fullFilePath) {
        File file = new File(fullFilePath);

        String pathUsingCanonical;
        String pathUsingAbsolute;
        try {
            pathUsingCanonical = file.getCanonicalPath();
            pathUsingAbsolute = file.getAbsolutePath();
        } catch (IOException e) {
            throw new SecurityException(DIRECTORY_TRAVERSAL_ATTEMPT + fullFilePath, e);
        }

        if (!pathUsingCanonical.equals(pathUsingAbsolute)) {
            throw new SecurityException(DIRECTORY_TRAVERSAL_ATTEMPT + fullFilePath);
        }
    }

    /**
     * build a secure full file path based on a trusted basePath,
     * if the base path is un trusted please use {@link #buildFullSecuredFilePath(String, String, String)}
     * @param trustedBasePath trusted internal path, not provided by the user, having "/" at the end is optional
     * @param untrustedFilename untrusted file name, can be given by the user
     * @return full file path
     * @throws SecurityException when a threat is detected
     */
    public static String buildFilePath(String trustedBasePath, String untrustedFilename) throws SecurityException {
        if (!Pattern.matches(FILENAME_VALIDATION_REGEX, untrustedFilename)) {
            throw new SecurityException("not valid filename");
        }
        final var trustedFilename = Paths.get(untrustedFilename).getFileName().toString();
        //return FilenameUtils.concat(trustedBasePath, trustedFilename);
        return Paths.get(trustedBasePath, trustedFilename).toString();
    }

    /**
     * build full path from untrusted partial directory path and untrusted filename
     * @param trustedPartialBasePath trusted base path ex /tmp
     * @param untrustedPartialBasePath untrusted partial base path ex: client1/
     * @param untrustedFilename untrusted file name
     * @return full secure path
     * @throws SecurityException when a threat is detected
     */
    public static String buildFullSecuredFilePath(
        String trustedPartialBasePath,
        String untrustedPartialBasePath,
        String untrustedFilename
    ) throws SecurityException {
        String untrustedFullBasePath;
        try {
            untrustedFullBasePath = Paths.get(trustedPartialBasePath, untrustedPartialBasePath).toString();
        } catch (InvalidPathException e) {
            throw new SecurityException(DIRECTORY_TRAVERSAL_ATTEMPT + untrustedPartialBasePath, e);
        }
        final String partiallyTrustedFullPath = buildFilePath(untrustedFullBasePath, untrustedFilename);
        checkDirectoryTraversalVulnerability(partiallyTrustedFullPath);
        return partiallyTrustedFullPath;
    }
}
