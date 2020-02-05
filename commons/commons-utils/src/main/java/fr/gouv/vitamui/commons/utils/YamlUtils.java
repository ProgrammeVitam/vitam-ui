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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


public final class YamlUtils {

    private static final String ARGUMENTS_MUST_BE_NON_NULL = "Arguments must be non null";

    private YamlUtils() {
    }

    /**
     * Read the Yaml file and return the object read
     *
     * @param yamlFile the yaml file to read
     * @param clasz    the class representing the target object
     * @return the object read
     * @throws IOException if read yaml input stream to class template exception occurred
     */
    public static final <C> C readYaml(File yamlFile, Class<C> clasz) throws IOException {
        if (yamlFile == null || clasz == null) {
            throw new FileNotFoundException(ARGUMENTS_MUST_BE_NON_NULL);
        }
        try (final FileReader yamlFileReader = new FileReader(yamlFile)) {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return clasz.cast(mapper.readValue(yamlFileReader, clasz));
        }
        catch (final RuntimeException e) {
            throw new IOException(e);
        }
    }

    /**
     * Read the Yaml file and return the object read
     *
     * @param yamlFile      the yaml file
     * @param typeReference the type reference representing the target interface object
     * @return the object read
     * @throws IOException if read yaml input stream to class template exception occurred
     */
    public static final <C> C readYaml(File yamlFile, TypeReference<C> typeReference) throws IOException {
        if (yamlFile == null || typeReference == null) {
            throw new FileNotFoundException(ARGUMENTS_MUST_BE_NON_NULL);
        }
        try (final FileReader yamlFileReader = new FileReader(yamlFile)) {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(yamlFileReader, typeReference);
        }
        catch (final RuntimeException e) {
            throw new IOException(e);
        }
    }

    /**
     * Read the Yaml InputStream and return the object read
     *
     * @param yamlInputStream the yaml input stream to read
     * @param clasz           the class representing the target object
     * @return the object read
     * @throws IOException if read yaml input stream to class template exception occurred
     */
    public static final <C> C readYaml(InputStream yamlInputStream, Class<C> clasz) throws IOException {
        if (yamlInputStream == null || clasz == null) {
            throw new FileNotFoundException(ARGUMENTS_MUST_BE_NON_NULL);
        }
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return clasz.cast(mapper.readValue(yamlInputStream, clasz));
        }
        catch (final RuntimeException e) {
            throw new IOException(e);
        }
    }

    /**
     * Read the Yaml file and return the object read
     *
     * @param yamlPath yaml file path
     * @param clasz    the class representing the target object
     * @return the object read
     * @throws IOException if file not found exception
     */
    public static final <C> C readYaml(Path yamlPath, Class<C> clasz) throws IOException {
        if (yamlPath == null || clasz == null) {
            throw new FileNotFoundException(ARGUMENTS_MUST_BE_NON_NULL);
        }
        final File file = yamlPath.toFile();
        return readYaml(file, clasz);
    }

    /**
     * Write the Yaml file
     *
     * @param destination the destination file
     * @param config      the configuration object to write using Yaml format
     * @throws IOException if write object config exception occurred
     */
    public static final void writeYaml(File destination, Object config) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(destination)) {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.writeValue(outputStream, config);
        }
    }
}
