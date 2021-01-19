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
package fr.gouv.vitamui.commons.rest.client;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.ParseOperationException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.converter.VitamUIErrorConverter;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * A Web client to check existence, read, create, update or delete  an object.
 * We can upload multipart data also.
 * with identifier.
 *
 *
 */
@ToString
public abstract class BaseWebClient<C extends AbstractHttpContext> extends BaseClient<C> {

    protected WebClient webClient;

    protected static final DataBufferFactory BUFFER_FACTORY = new DefaultDataBufferFactory();

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(BaseWebClient.class);

    public BaseWebClient(final WebClient webClient, final String baseUrl) {
        super(baseUrl);
        this.webClient = webClient;
    }

    /**
     * Send MultipartData.
     *
     * @param url
     * @param httpMethod
     * @param context
     * @param dto
     * @param multipartFile
     * @param clazz
     * @return
     */
    protected <T> T multipartData(final String url, final HttpMethod httpMethod, final AbstractHttpContext context, final Map<String, Object> dto,
            final Optional<Entry<String, MultipartFile>> multipartFile, final Class<T> clazz) {
        return multipartData(url, httpMethod, context, dto, multipartFile, null, clazz);
    }

    protected <T> T multiparts(final String url, final HttpMethod httpMethod, final AbstractHttpContext context, final Map<String, Object> dto,
                               final Optional<Entry<String, MultipartFile>> header, final Optional<Entry<String, MultipartFile>> footer, final Optional<Entry<String, MultipartFile>> portal, final Class<T> clazz) {
        return multiparts(url, httpMethod, context, dto, header, footer, portal,null, clazz);
    }

    /**
     * Send MultipartData.
     *
     * @param url
     * @param httpMethod
     * @param context
     * @param dto
     * @param multipartFile
     * @param headers
     * @param clazz
     * @return
     */
    protected <T> T multipartData(final String url, final HttpMethod httpMethod, final AbstractHttpContext context, final Map<String, Object> dto,
            final Optional<Entry<String, MultipartFile>> multipartFile, final MultiValueMap<String, String> headers, final Class<T> clazz) {
        final MultipartBodyBuilder builder = new MultipartBodyBuilder();

        checkHttpMethod(httpMethod);

        addMapEntriesToBuilder(dto, builder);

        if (multipartFile.isPresent()) {
            final String paramName = multipartFile.get().getKey();
            final MultipartFile valueMultipartFile = multipartFile.get().getValue();
            SafeFileChecker.checkSafeFilePath(valueMultipartFile.getOriginalFilename());
            final String contentDisposition = buildContentDisposition(paramName, valueMultipartFile.getName());
            addPartFile(builder, paramName, valueMultipartFile, contentDisposition);
        }

        final MultiValueMap<String, HttpEntity<?>> multiValueMap = builder.build();

        if (HttpMethod.POST == httpMethod) {
            return webClient.post().uri(url).headers(addHeaders(headers)).headers(addHeaders(buildHeaders(context))).contentType(MediaType.MULTIPART_FORM_DATA)
                    .syncBody(multiValueMap).retrieve().onStatus(status -> !status.is2xxSuccessful(), BaseWebClient::createResponseException).bodyToMono(clazz)
                    .block();
        }
        else {
            return webClient.patch().uri(url).headers(addHeaders(headers)).headers(addHeaders(buildHeaders(context))).contentType(MediaType.MULTIPART_FORM_DATA)
                    .syncBody(multiValueMap).retrieve().onStatus(status -> !status.is2xxSuccessful(), BaseWebClient::createResponseException).bodyToMono(clazz)
                    .block();
        }

    }

    private void computeFile(final MultipartBodyBuilder  builder, final Optional<Entry<String, MultipartFile>> multipartFile) {
        if (multipartFile.isPresent()) {
            final String paramName = multipartFile.get().getKey();
            final MultipartFile valueMultipartFile = multipartFile.get().getValue();
            SafeFileChecker.checkSafeFilePath(valueMultipartFile.getOriginalFilename());
            final String contentDisposition = buildContentDisposition(paramName, valueMultipartFile.getName());
            addPartFile(builder, paramName, valueMultipartFile, contentDisposition);
        }
    }

    protected <T> T multiparts(final String url, final HttpMethod httpMethod, final AbstractHttpContext context, final Map<String, Object> dto,
                               final Optional<Entry<String, MultipartFile>> header, final Optional<Entry<String, MultipartFile>> footer, final Optional<Entry<String, MultipartFile>> portal, final MultiValueMap<String, String> headers, final Class<T> clazz) {
        final MultipartBodyBuilder builder = new MultipartBodyBuilder();

        checkHttpMethod(httpMethod);

        addMapEntriesToBuilder(dto, builder);

        if (header.isPresent()) {
            this.computeFile(builder, header);
        }
        if (footer.isPresent()) {
            this.computeFile(builder, footer);
        }
        if (portal.isPresent()) {
            this.computeFile(builder, portal);
        }

        final MultiValueMap<String, HttpEntity<?>> multiValueMap = builder.build();

        if (HttpMethod.POST == httpMethod) {
            return webClient.post().uri(url).headers(addHeaders(headers)).headers(addHeaders(buildHeaders(context))).contentType(MediaType.MULTIPART_FORM_DATA)
                    .syncBody(multiValueMap).retrieve().onStatus(status -> !status.is2xxSuccessful(), BaseWebClient::createResponseException).bodyToMono(clazz)
                    .block();
        }
        else {
            return webClient.patch().uri(url).headers(addHeaders(headers)).headers(addHeaders(buildHeaders(context))).contentType(MediaType.MULTIPART_FORM_DATA)
                    .syncBody(multiValueMap).retrieve().onStatus(status -> !status.is2xxSuccessful(), BaseWebClient::createResponseException).bodyToMono(clazz)
                    .block();
        }

    }

    /**
     * Send MultipartData using a path.
     *
     * @param url
     * @param httpMethod
     * @param context
     * @param filePath
     * @param clazz
     * @return
     */
    protected <T> T multipartDataFromFile(final String url, final HttpMethod httpMethod, final AbstractHttpContext context,
            final Optional<Entry<String, Path>> filePath, final Class<T> clazz) {
        return multipartDataFromFile(url, httpMethod, context, null, filePath, null, clazz);
    }

    /**
     * Send MultipartData using a path.
     *
     * @param url
     * @param context
     * @param dto
     * @param multipartFile
     * @param clazz
     * @return
     */
    protected <T> T multipartDataFromFile(final String url, final HttpMethod httpMethod, final AbstractHttpContext context, final Map<String, Object> dto,
            final Optional<Entry<String, Path>> filePath, final Class<T> clazz) {
        return multipartDataFromFile(url, httpMethod, context, dto, filePath, null, clazz);
    }

    /**
     * Send MultipartData using a path.
     *
     * @param url
     * @param httpMethod
     * @param context
     * @param headers
     * @param filePath
     * @param clazz
     * @return
     */
    protected <T> T multipartDataFromFile(final String url, final HttpMethod httpMethod, final AbstractHttpContext context,
            final Optional<Entry<String, Path>> filePath, final MultiValueMap<String, String> headers, final Class<T> clazz) {
        return multipartDataFromFile(url, httpMethod, context, null, filePath, headers, clazz);
    }

    /**
     * Send MultipartData using a path.
     *
     * @param url
     * @param context
     * @param dto
     * @param multipartFile
     * @param clazz
     * @return
     */
    protected <T> T multipartDataFromFile(final String url, final HttpMethod httpMethod, final AbstractHttpContext context, final Map<String, Object> dto,
            final Optional<Entry<String, Path>> filePath, final MultiValueMap<String, String> headers, final Class<T> clazz) {
        final MultipartBodyBuilder builder = new MultipartBodyBuilder();

        checkHttpMethod(httpMethod);

        addMapEntriesToBuilder(dto, builder);
        if (filePath.isPresent()) {
            final String paramName = filePath.get().getKey();
            final Path path = filePath.get().getValue();
            SafeFileChecker.checkSafeFilePath(path.toString());
            final String contentDisposition = buildContentDisposition(paramName, path.getFileName().toString());
            builder.asyncPart(paramName, DataBufferUtils.readAsynchronousFileChannel(() -> AsynchronousFileChannel.open(path, StandardOpenOption.READ),
                    BUFFER_FACTORY, CommonConstants.INPUT_STREAM_BUFFER_SIZE), DataBuffer.class).header(RestUtils.CONTENT_DISPOSITION, contentDisposition);
        }

        final MultiValueMap<String, HttpEntity<?>> multiValueMap = builder.build();
        LOGGER.debug("multiValueMap .................... {}", multiValueMap);

        if (HttpMethod.POST == httpMethod) {
            return webClient.post().uri(url).headers(addHeaders(headers)).headers(addHeaders(buildHeaders(context))).contentType(MediaType.MULTIPART_FORM_DATA)
                    .syncBody(multiValueMap).retrieve().onStatus(status -> !status.is2xxSuccessful(), BaseWebClient::createResponseException).bodyToMono(clazz)
                    .block();
        }
        else {
            return webClient.patch().uri(url).headers(addHeaders(headers)).headers(addHeaders(buildHeaders(context))).contentType(MediaType.MULTIPART_FORM_DATA)
                    .syncBody(multiValueMap).retrieve().onStatus(status -> !status.is2xxSuccessful(), BaseWebClient::createResponseException).bodyToMono(clazz)
                    .block();
        }

    }

    /**
     * Send MultipartData using a path.
     * @param url
     * @param httpMethod
     * @param context
     * @param filePath
     * @param headers
     * @return
     */
    protected ClientResponse multipartDataFromFile(final String url, final HttpMethod httpMethod, final AbstractHttpContext context,
            final Optional<Entry<String, Path>> filePath, final MultiValueMap<String, String> headers) {
        final MultipartBodyBuilder builder = new MultipartBodyBuilder();

        checkHttpMethod(httpMethod);

        if (filePath.isPresent()) {
            final String paramName = filePath.get().getKey();
            final Path path = filePath.get().getValue();
            final String contentDisposition = buildContentDisposition(paramName, path.getFileName().toString());
            builder.asyncPart(paramName, DataBufferUtils.readAsynchronousFileChannel(() -> AsynchronousFileChannel.open(path, StandardOpenOption.READ),
                    BUFFER_FACTORY, CommonConstants.INPUT_STREAM_BUFFER_SIZE), DataBuffer.class).header(RestUtils.CONTENT_DISPOSITION, contentDisposition);
        }

        final MultiValueMap<String, HttpEntity<?>> multiValueMap = builder.build();
        LOGGER.debug("multiValueMap .................... {}", multiValueMap);

        if (HttpMethod.POST == httpMethod) {
            return webClient.post().uri(url).headers(addHeaders(headers)).headers(addHeaders(buildHeaders(context))).contentType(MediaType.MULTIPART_FORM_DATA)
                    .syncBody(multiValueMap).exchange().block();
        }
        else {
            return webClient.patch().uri(url).headers(addHeaders(headers)).headers(addHeaders(buildHeaders(context))).contentType(MediaType.MULTIPART_FORM_DATA)
                    .syncBody(multiValueMap).exchange().block();
        }

    }

    /**
     * Handle exceptions in response.
     * @param response
     * @return
     */
    public static Mono<? extends Throwable> createResponseException(final ClientResponse response) {
        LOGGER.error("ERROR .................... {}", response.statusCode());

        return response.bodyToMono(String.class).flatMap(serviceException -> {
            LOGGER.error("ERROR .................... {}", serviceException);

            VitamUIError error;
            // on HEAD requests, we don't have a body
            if (StringUtils.isBlank(serviceException)) {
                error = new VitamUIError();
                error.setStatus(response.statusCode().value());
                error.setMessage("Unknown problem");
                error.setError("apierror.unknown");
            }
            else {
                // Added FAIL_ON_UNKNOWN_PROPERTIES:false to prevent error "UnrecognizedPropertyException: Unrecognized field"
                // TODO check where the property "path" comes from
                try {
                    error = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(serviceException, VitamUIError.class);
                }
                catch (final IOException e) {
                    LOGGER.error("Error when retrieving exception {}", e);
                    error = new VitamUIError();
                    error.setStatus(response.statusCode().value());
                    error.setMessage(e.getMessage());
                    error.setError("apierror.unknown");
                }
            }

            final VitamUIErrorConverter converter = new VitamUIErrorConverter();
            return Mono.error(converter.convert(error));
        });

    }

    /**
     * Build Content disposition for multipart.
     * @param paramName
     * @param fileName
     * @return
     */
    protected String buildContentDisposition(final String paramName, final String fileName) {
        return String.format("%s; %s=%s; %s=%s", RestUtils.FORM_DATA, RestUtils.NAME, paramName, RestUtils.FILENAME, fileName);
    }

    /**
     * Add headers to request.
     * @param headers
     * @return
     */
    protected Consumer<HttpHeaders> addHeaders(final MultiValueMap<String, String> headers) {
        return headersConsumer -> {
            if (headers != null && !headers.isEmpty()) {
                headersConsumer.addAll(headers);
            }
        };
    }

    /**
     * Add Map entries to Builder.
     *
     * @param dto
     * @param builder
     */
    protected void addMapEntriesToBuilder(final Map<String, Object> dto, final MultipartBodyBuilder builder) {
        if (dto != null && !dto.isEmpty()) {
            dto.entrySet().forEach(entry -> builder.part(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Add File to builder.
     *
     * @param builder
     * @param paramName
     * @param valueMultipartFile
     * @param contentDisposition
     */
    protected void addPartFile(final MultipartBodyBuilder builder, final String paramName, final MultipartFile valueMultipartFile,
            final String contentDisposition) {
        builder.asyncPart(paramName,
                DataBufferUtils.readInputStream(() -> valueMultipartFile.getInputStream(), BUFFER_FACTORY, CommonConstants.INPUT_STREAM_BUFFER_SIZE),
                DataBuffer.class).header(RestUtils.CONTENT_DISPOSITION, contentDisposition);
    }

    /**
     * Check HTTP method.
     *
     * @param httpMethod
     */
    protected void checkHttpMethod(final HttpMethod httpMethod) {
        if (httpMethod == null || (HttpMethod.POST != httpMethod && HttpMethod.PATCH != httpMethod)) {
            throw new ParseOperationException(String.format("%s not supported. Only %s and %s are allowed.", httpMethod, HttpMethod.POST, HttpMethod.PATCH));
        }
    }

}
