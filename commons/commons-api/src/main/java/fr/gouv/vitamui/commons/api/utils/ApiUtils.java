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
package fr.gouv.vitamui.commons.api.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.ParseOperationException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;
import fr.gouv.vitamui.commons.utils.VitamUIStringUtils;
import fr.gouv.vitamui.commons.utils.JsonUtils;

public final class ApiUtils {

    private static final String ROLE_PREFIX = "ROLE_";

    private ApiUtils() {
    }

    public static String ensureHasRolePrefix(final String authority) {
        if (!authority.startsWith(ROLE_PREFIX)) {
            return ROLE_PREFIX + authority;
        } else {
            return authority;
        }
    }

    /**
     * Check external argument
     *
     * @param strings
     * @throws ParseOperationException
     */
    public static void checkSanityString(final String... strings) {
        for (final String field : strings) {
            if (VitamUIStringUtils.UNPRINTABLE_PATTERN.matcher(field).find()) {
                throw new ParseOperationException("Invalid input bytes");
            }
            for (final String rule : VitamUIStringUtils.getRules()) {
                if (field != null && rule != null && field.contains(rule)) {
                    throw new ParseOperationException("Invalid tag sanity check");
                }
            }
        }
    }

    /**
     * Get Content from a Resource's file.
     * @param fileName
     * @return
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    public static String getContentFromResourceFile(final Class clazz, final String fileName) throws IOException {
        final ClassLoader classLoader = clazz.getClassLoader();
        final InputStream stream = classLoader.getResourceAsStream(fileName);
        if (stream == null) {
            throw new InternalServerException("File not found : " + fileName);
        }

        return IOUtils.toString(stream, StandardCharsets.UTF_8);
    }

    public static ByteArrayResource getByteArrayResourceFromFilePath(final String fileName) {
        try {
            final File file = ResourceUtils.getFile(fileName);
            return new ByteArrayResource(FileUtils.readFileToByteArray(file));
        }
        catch (final IOException e) {
            throw new InternalServerException("File exception : " + fileName + e.getMessage());
        }
    }

    public static void checkValidity(final Object dto) {
        if (dto == null) {
            throw new ValidationException("validation failed  object is null");
        }
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        final Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ValidationException("validation failed " + violations.toString());
        }
    }

    public static <T> T fromJson(final String json, final TypeReference<T> type) {
        try {
            return JsonUtils.fromJson(json, type);
        }
        catch (IOException e) {
            throw new InvalidFormatException(e.getMessage(), e);
        }
    }

    public static String toJson(final Object object) {
        try {
            return JsonUtils.toJson(object);
        }
        catch (JsonProcessingException e) {
            throw new InvalidFormatException(e.getMessage(), e);

        }
    }

    public static <T> T treeToValue(final JsonNode json, final Class<T> clazz) {
        try {
            return JsonUtils.treeToValue(json, clazz);
        }
        catch (JsonProcessingException e) {
            throw new InvalidFormatException(e.getMessage(), e);

        }
    }
}
