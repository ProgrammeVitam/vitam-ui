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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;


/**
 * Property Utility class
 * <p>
 * NOTE for developers: Do not add LOGGER there
 */

public final class ResourcesUtils {

    private static final String FILE_NOT_FOUND_IN_RESOURCES = "File not found in Resources: ";

    private ResourcesUtils() {

    }

    /**
     * Get the InputStream representation from the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated File
     * @throws FileNotFoundException if the resource file not found
     */
    public static final InputStream getResourceAsStream(final String resourcesFile) throws FileNotFoundException {
        if (resourcesFile == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        InputStream stream = null;
        try {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcesFile);
        }
        catch (final SecurityException e) {
            // since another exception is thrown
            // Nothing to do
        }
        if (stream == null) {
            try {
                stream = ResourcesUtils.class.getClassLoader().getResourceAsStream(resourcesFile);
            }
            catch (final SecurityException e) {
                // since another exception is thrown
                // Nothing to do
            }
        }
        if (stream == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        return stream;
    }

    /**
     * Get the File representation from the local path to the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated File
     * @throws FileNotFoundException if the resource file not found
     */
    public static final File getResourceFile(final String resourcesFile) throws FileNotFoundException {
        if (resourcesFile == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        URL url;
        try {
            url = ResourcesUtils.class.getClassLoader().getResource(resourcesFile);
        }
        catch (final SecurityException e) {
            // since another exception is thrown
            // Nothing to do
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(resourcesFile);
        }
        if (url == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        File file;
        try {
            file = new File(url.toURI());
        }
        catch (final URISyntaxException e) {
            // Nothing to do
            file = new File(url.getFile().replaceAll("%20", " "));
        }
        if (file.exists()) {
            return file;
        }
        throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
    }

    /**
     * Get the Path representation from the local path to the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated Path
     * @throws FileNotFoundException if resource file not found
     */
    public static final Path getResourcePath(final String resourcesFile) throws FileNotFoundException {
        return getResourceFile(resourcesFile).toPath();
    }


}
