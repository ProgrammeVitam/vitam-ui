/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
package fr.gouv.vitamui.pastis.common.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public final class PropertiesUtils {

    private static final String FILE_NOT_FOUND_IN_RESOURCES = "File not found in Resources: ";

    private PropertiesUtils() {
        // Empty
    }

    /**
     * Get the File representation from the local path to the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated File
     * @throws FileNotFoundException if the resource file not found
     */
    public static File getResourceFile(String resourcesFile) throws FileNotFoundException {
        if (resourcesFile == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        URL url;
        try {
            url = PropertiesUtils.class.getClassLoader().getResource(resourcesFile);
        } catch (final SecurityException e) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        if (url == null) {
            try {
                url = Thread.currentThread().getContextClassLoader().getResource(resourcesFile);
            } catch (Exception e) {
                throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
            }
        }
        File file;
        try {
            file = new File(url.toURI());
        } catch (final URISyntaxException e) {
            file = new File(url.getFile().replace("%20", " "));
        }
        if (file.exists()) {
            return file;
        }
        throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
    }

    /**
     * Get the InputStream representation from the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated File
     * @throws FileNotFoundException if the resource file not found
     */
    public static InputStream getResourceAsStream(String resourcesFile) throws FileNotFoundException {
        if (resourcesFile == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES);
        }
        InputStream stream;
        try {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcesFile);
        } catch (final SecurityException e) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        if (stream == null) {
            try {
                stream = PropertiesUtils.class.getClassLoader().getResourceAsStream(resourcesFile);
            } catch (final SecurityException e) {
                throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
            }
        }
        if (stream == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        return stream;
    }
}
