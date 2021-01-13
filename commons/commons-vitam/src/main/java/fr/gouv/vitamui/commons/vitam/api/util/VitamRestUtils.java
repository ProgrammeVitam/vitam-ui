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
package fr.gouv.vitamui.commons.vitam.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import fr.gouv.vitam.common.external.client.DefaultClient;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.stream.StreamUtils;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidOperationException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.utils.JsonUtils;

public class VitamRestUtils {

    public static final String PARSING_ERROR_MSG = "Error while parsing Vitam response";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamRestUtils.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private VitamRestUtils() {
        //  nothing
    }

    /**
     * Write the http response based on the given vitam reponse.
     *
     * The content, Content-Type header and Content-Disposition
     * header will be copied from the vitam response to the http response.
     * @param vitamObjectStreamResponse
     * @param response
     * @throws IOException
     */
    public static void writeFileResponse(final Response vitamObjectStreamResponse, final HttpServletResponse response) throws IOException {

        // Sets media type
        final Optional<MediaType> contentTypeOpt = getContentType(vitamObjectStreamResponse);
        if (contentTypeOpt.isPresent()) {
            response.setContentType(contentTypeOpt.get().toString());
        }
        else {
            LOGGER.debug("No content type in Vitam reponse");
        }

        // Copies content-disposition header from Vitam response
        final Optional<String> contentDispositionOpt = getContentDisposition(vitamObjectStreamResponse);
        if (contentDispositionOpt.isPresent()) {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDispositionOpt.get());
        }

        // Copies the content of the Vitam response to the http response
        if (vitamObjectStreamResponse == null) {
            throw new UnexpectedDataException("An error occured while retrieving the output stream from Vitam. ");
        }

        final InputStream inputStream = vitamObjectStreamResponse.readEntity(InputStream.class);
        final OutputStream os;
        try {
            os = response.getOutputStream();
        }
        catch (final IOException exception) {
            throw new InternalServerException("An error occured while retrieving the output stream from Vitam: " + exception.getMessage(), exception);
        }
        try {
            StreamUtils.copy(inputStream, os);
            DefaultClient.staticConsumeAnyEntityAndClose(vitamObjectStreamResponse);
        }
        catch (final IOException exception) {
            throw new InternalServerException("An error occured while copying Vitam input stream to the output stream : " + exception.getMessage(), exception);
        }
    }

    /**
     * Method allowing to retrieve content type from Vitam response.
     * @param vitamObjectStreamResponse Vitam response to analyze.
     * @return The content type if the information is set.
     */
    public static Optional<MediaType> getContentType(final Response vitamObjectStreamResponse) {

        if (vitamObjectStreamResponse != null && vitamObjectStreamResponse.getMediaType() != null) {
            final String vitamMediaType = vitamObjectStreamResponse.getMediaType().toString();
            if (StringUtils.isNotBlank(vitamMediaType)) {
                String updatedMediaType = vitamMediaType;
                // Fix invalid media type with no value in vitam response
                if (vitamMediaType.endsWith("=")) {
                    updatedMediaType = vitamMediaType.substring(0, vitamMediaType.length() - 1);
                }
                try {
                    return Optional.ofNullable(MediaType.parseMediaType(updatedMediaType));
                }
                catch (final InvalidMediaTypeException e) {
                    LOGGER.warn("Invalid media type in Vitam response : " + e.getMessage(), e);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Method allowing to retrieve content disposition from Vitam response.
     * @param vitamObjectStreamResponse Vitam response to analyze.
     * @return The content disposition if the information is set.
     */
    public static Optional<String> getContentDisposition(final Response vitamObjectStreamResponse) {

        if (vitamObjectStreamResponse != null) {
            final Object contentDisposition = vitamObjectStreamResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            if (contentDisposition instanceof String && StringUtils.isNotBlank((String) contentDisposition)) {
                return Optional.of((String) contentDisposition);
            }
        }
        return Optional.empty();
    }

    private static boolean isStatusAccepted(final Integer status, final Integer... acceptedStatus) {
        final Integer[] defaultAcceptedStatuses = { 200 };
        final List<Integer> statuses;
        if (acceptedStatus == null || acceptedStatus.length == 0) {
            statuses = Arrays.asList(defaultAcceptedStatuses);
        }
        else {
            statuses = Arrays.asList(acceptedStatus);
        }
        return statuses.contains(status);
    }

    /**
     * Checks if the given response has an accepted status and throws an error if it hasn't.
     *
     * If no acceptedStatus parameter is given, the default accepted status is 200.
     *
     * @throws
     * @param response Response of which the status is checked
     * @param acceptedStatus Statuses accepted
     */
    public static void checkResponse(final Response response, final Integer... acceptedStatus) {
        Assert.notNull(response, "The server response cannot be null");
        final int responseStatus = response.getStatus();
        if (!isStatusAccepted(responseStatus, acceptedStatus)) {
            final String body = response.readEntity(String.class);
            String vitamMessage = "";
            LOGGER.error("Vitam error: status: {}, response body:\n{}", responseStatus, body);
            if (StringUtils.isNotBlank(body)) {
                try {
                    final JsonNode jsonBody = mapper.readTree(body);
                    vitamMessage = jsonBody.get("message").asText();
                }
                catch (final IOException e) {
                    LOGGER.debug("Vitam response is not in a valid json format");
                }
            }
            final String errorMessage = String.format("status: %d, message: %s", responseStatus, vitamMessage);

            throw getException(responseStatus, errorMessage);
        }
    }

    /**
     * Checks if the given response has an accepted status and throws an error if it hasn't.
     *
     * If no acceptedStatus parameter is given, the default accepted status is 200.
     *
     * @throws
     * @param response Response of which the status is checked
     * @param acceptedStatus Statuses accepted
     */
    public static void checkResponse(final RequestResponse<?> response, final Integer... acceptedStatus) {
        Assert.notNull(response, "The server response cannot be null");
        final int responseStatus = response.getStatus();
        if (!isStatusAccepted(responseStatus, acceptedStatus)) {
            final JsonNode jsonResponse = response.toJsonNode();
            LOGGER.debug("Vitam error: body:\n{}", jsonResponse);
            final String message = String.format("status: %d, message: %s", responseStatus, jsonResponse.get("message"));
            throw getException(responseStatus, message);
        }
    }

    private static VitamUIException getException(final int responseStatus, final String message) {
        if (responseStatus == HttpStatus.NOT_FOUND.value()) {
            return new NotFoundException("Vitam not found error: " + message);
        }
        else if (responseStatus == HttpStatus.BAD_REQUEST.value()) {
            return new BadRequestException("Vitam Bad request error: " + message);
        }
        else if (responseStatus == HttpStatus.UNAUTHORIZED.value()) {
            return new ForbiddenException("Vitam unauthorized error: " + message);
        } else if (responseStatus == HttpStatus.FORBIDDEN.value()) {
            return new ForbiddenException("Vitam forbidden error: " + message);
        } else if (responseStatus == HttpStatus.PRECONDITION_FAILED.value()) {
            return new InvalidOperationException("Vitam Precondion failed: " + message);
        }

        return new InternalServerException("Vitam error:" + message);
    }

    public static <T> T responseMapping(final JsonNode json, final Class<T> clazz) {
        try {
            return JsonUtils.treeToValue(json, clazz, false);
        } catch (final JsonProcessingException e) {
            throw new InternalServerException(VitamRestUtils.PARSING_ERROR_MSG, e);
        }
    }

}
