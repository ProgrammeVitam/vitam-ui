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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class VitamUIUtils {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static final String PRINT_ALGORITHM = "SHA-512";

    private VitamUIUtils() {
        // empty
    }

    /**
     * Random Generator
     */
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    /**
     * Verify parameter respect the pattern  {@link #EMAIL_PATTERN}
     *
     * @param email the parameter to be tested
     * @return true if email is valid else false
     *
     */
    public static boolean isValidEmail(final String email) {
        final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        final Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * @param length
     *            the length of array
     * @return a byte array with random values
     */
    public static byte[] getRandom(final int length) {
        if (length <= 0) {
            return new byte[0];
        }
        final byte[] result = new byte[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (RANDOM.nextInt(95) + 32);
        }
        return result;
    }

    public static <T, E> E copyProperties(final T source, final E target) {
        if (source != null && target != null) {
            BeanUtils.copyProperties(source, target);
        }
        return target;
    }

    public static boolean isValid(final Object obj) {
        if (obj == null) {
            return false;
        }
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        final Set<ConstraintViolation<Object>> violations = validator.validate(obj);
        return violations.isEmpty();
    }

    /**
     * Cast the object into the given type.
     * @param value
     * @param clazz
     * @throws ClassCastException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static <T> T castValue(final Object value, final Class<T> clazz) throws ClassCastException {
        return clazz.cast(value);
    }

    public static <T> boolean canBeCastByClass(final Object value, final Class<T> clazz) {
        T t;
        try {
            t = clazz.cast(value);
            return true;
        }
        catch (final ClassCastException e) {
            return false;
        }
    }

    public static <T> T convertObjectFromJson(final String json, final Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        final T object = new ObjectMapper().readValue(json, clazz);
        return object;
    }

    public static void saveContentInFile(final String filename, final String content) throws IOException {
        final File file = new File(filename);
        FileUtils.writeStringToFile(file, content, Charset.forName("UTF-8"));
    }

    public static void saveContentInFile(final String filename, final byte[] content) throws IOException {
        final File file = new File(filename);
        FileUtils.writeByteArrayToFile(file, content);
    }

    /**
     * Try to convert a string to a date
     */
    public static final Date convertLocalDateToDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Try to convert a string to a date
     */
    public static final Date convertStringToDate(final String content) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }

        try {
            final LocalDate date = LocalDate.parse(content);
            return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        catch (final Exception e) {
            // do nothing
        }

        try {
            final LocalDateTime date = LocalDateTime.parse(content);
            return Date.from(date.toInstant(ZoneOffset.UTC));
        }
        catch (final Exception e) {
            // do nothing
        }

        try {
            final OffsetDateTime date = OffsetDateTime.parse(content);
            return Date.from(date.toInstant());
        }
        catch (final Exception e) {
            throw new DateTimeParseException("Text '" + content + "' could not be parsed", content, -1);
        }
    }

    /**
     * Method allowing to generate a request ID.
     * @return The generated identifier.
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     *
     * Method allowing to generate an application ID.
     * @param requestId
     * @param applicationIdExt Give by the caller
     * @param applicationName Name of the client certificate
     * @param userIdentifier Identifier of the user connected
     * @param superUserIdentifier Exist if the user is in "Subrogation mode"
     * @param customerIdentifier
     * @param requestId Request identifier linked to the current request.
     * @return  The generated application ID.
     */
    public static String generateApplicationId(final String applicationIdExt, final String applicationName, final String userIdentifier,
            final String superUserIdentifier, final String customerIdentifier, final String requestId) {
        // Application-Id format: applicationIdExt:requestId:applicationName:userIdentifier:superUserIdentifier:customerIdentifier.
        final String msg = "Missing %s information for construct X-Application-Id header";
        ParamsUtils.checkParameter(String.format(msg, "applicationName"), applicationName);
        ParamsUtils.checkParameter(String.format(msg, "userIdentifier"), userIdentifier);
        ParamsUtils.checkParameter(String.format(msg, "customerIdentifier"), customerIdentifier);
        ParamsUtils.checkParameter(String.format(msg, "requestId"), requestId);

        return String.format("%s:%s:%s:%s:%s:%s", formatOptionalEntriesForApplicationId(StringUtils.remove(applicationIdExt, ":")), requestId, applicationName,
                userIdentifier, formatOptionalEntriesForApplicationId(superUserIdentifier), customerIdentifier);
    }

    private static String formatOptionalEntriesForApplicationId(final String entry) {
        return StringUtils.isBlank(entry) ? "-" : entry;

    }

    public static String getBase64(final MultipartFile file) throws IOException {
        try (final InputStream fileInputStream = file.getInputStream()) {
            final byte[] fileInByteArray = IOUtils.toByteArray(fileInputStream);
            final String fileBase64 = new String(Base64.getEncoder().encode(fileInByteArray), "UTF-8");
            return fileBase64;
        }
    }

    /**
     * Create an immutable List (use List.of in Java 9)
     */
    public static <T> List<T> listOf(final T... a) {
        return Collections.unmodifiableList(Arrays.asList(a));
    }

    public static <T extends List, S> List<S> unionOf(final T... l) {
        final List<S> unionList = new ArrayList<>();
        Arrays.asList(l).stream().forEach(e -> unionList.addAll(e));
        return Collections.unmodifiableList(unionList);
    }

    public static String getSha512Print(final byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        Security.addProvider(new BouncyCastleProvider());
        final MessageDigest digest = MessageDigest.getInstance(PRINT_ALGORITHM, "BC");
        digest.digest(data);
        byte[] mdbytes = null;
        mdbytes = digest.digest(data);

        return DatatypeConverter.printHexBinary(mdbytes);
    }

    public static String secureFormatHeadersLogging(HttpHeaders headers) {
        return headers.entrySet().stream()
            .map(entry -> {
                List<String> values = entry.getValue();
                if (values.size() == 1) {

                    return (entry.getKey().equalsIgnoreCase(HttpHeaders.AUTHORIZATION) ||
                        entry.getKey().equalsIgnoreCase( HttpHeaders.PROXY_AUTHORIZATION) ||
                        entry.getKey().equalsIgnoreCase(HttpHeaders.PROXY_AUTHENTICATE)) ?
                        (entry.getKey() + ":" + "\"" +
                            (values.get(0).split(" ")[0] + " **********" + "\"")) :
                        entry.getKey() + ":" + "\"" +
                            values.get(0) + "\"";

                } else {
                    return entry.getKey() + ":" +
                        (values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
                }
            })
            .collect(Collectors.joining(", ", "[", "]"));
    }
}
