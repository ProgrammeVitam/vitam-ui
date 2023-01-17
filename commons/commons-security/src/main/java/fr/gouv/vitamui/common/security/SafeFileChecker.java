/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
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
 */
package fr.gouv.vitamui.common.security;

import com.google.common.base.Joiner;
import fr.gouv.vitamui.commons.api.exception.InvalidFileSanitizeException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Checker for Sanity of file manipulation to avoid Path Traversal vulnerability <br>
 *
 */
public class SafeFileChecker {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SafeFileChecker.class);

    private static final String FILENAME_PATTERN = "^[a-zA-Z0-9\\-_]+(\\.[a-zA-Z0-9]+)*$";
    private static final Pattern PATH_COMPONENT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_.@]+$");

    private SafeFileChecker() {
        // Empty constructor
    }

    /**
     * do an ESAPI path sanityCheck and prevent a path traversal attack
     *
     * @param path full path representing a FileSystem resource
     * @throws IOException thrown when any check fails with UnChecked or Runtime exception
     */
    public static void checkSafeFilePath(String path)  {

        try {
            checkNullParameter(path);
            doCanonicalPathCheck(path);

        } catch (Exception ex) {
            throw new InvalidFileSanitizeException(String
                .format("Security check error : Invalid name (%s)", path));
        }

    }

    /**
     * do an ESAPI path sanityCheck and prevent a path traversal attack
     *
     * @param rootPath first or initial part(s) of a path representing a FileSystem resource
     * @param subPaths sub (additional) parts after root part(s) to be joined to rootPath parameter
     *                 using File.separator FileSystem String
     * @throws IOException thrown when any check fails with UnChecked or Runtime exception
     */
    public static void checkSafeFilePath(String rootPath, String... subPaths) {
        try {
            checkNullParameter(rootPath);
            String finalPath = rootPath;
            if (subPaths != null && subPaths.length > 0) {
                finalPath = finalPath + File.separator + Joiner.on(File.separator).join(subPaths);
            }
            checkSafeFilePath(finalPath);
        } catch (Exception e) {
            throw e;
        }
    }

    private static void checkNullParameter(String path) {
        if (path == null || path.length() == 0) {
            throw new InvalidFileSanitizeException("Null or empty path submitted");
        }
    }

    /**
     * check recursive/out of submitted root path to avoid Path Traversal attack
     *
     * @param path
     * @throws IOException
     */
    private static void doCanonicalPathCheck(String path) throws IOException {
        String canonicalPath = new File(path).getCanonicalFile().getName();

        if (!path.equals(canonicalPath)) {
            LOGGER.error("Invalid path {} did not match canonical : {}", path, canonicalPath);
            throw new IOException(
                String.format("Invalid path (%s) did not match canonical : %s", path, canonicalPath));
        }
    }


    /**
     * Check directory path component against a whitelist of characters
     *
     * @param pathParent a parent path obtained from File.getParent()
     */
    private static void doDirCheck(String pathParent) {

        String[] dirComponent = pathParent.split(File.separator);

        for (int index = 0; index < dirComponent.length; index++) {
            String component = dirComponent[index];
            if (index != 0 && !PATH_COMPONENT_PATTERN.matcher(component).matches()) {
                LOGGER.error("Invalid path {} (has unauthorized characters in component[{}] : {}", pathParent, index,
                    component);
                throw new InvalidFileSanitizeException(String
                        .format("Invalid path (%s) (has unauthorized characters in component[%d] : %s", pathParent, index,
                                component));
            }
        }

    }

    /**
     * Check name path component against a whitelist of characters
     *
     * @param pathName a  path name obtained from File.getName()
     */
    private static void doFilenameCheck(String pathName) {
        if (pathName != null) {
            if(!pathName.matches(FILENAME_PATTERN)) {
                LOGGER.error("Invalid pathName {} ", pathName);
                throw new InvalidFileSanitizeException(String
                    .format("Invalid filename (%s) (has unauthorized characters in part %s", pathName));
            }
        }

    }
}
