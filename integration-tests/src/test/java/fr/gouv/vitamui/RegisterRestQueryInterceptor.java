package fr.gouv.vitamui;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;
import net.serenitybdd.core.rest.RestMethod;
import net.serenitybdd.core.rest.RestQuery;
import net.thucydides.core.steps.StepEventBus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

public class RegisterRestQueryInterceptor implements ClientHttpRequestInterceptor {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(RegisterRestQueryInterceptor.class);

    private static final Map<String, RestMethod> TO_REST_METHOD = new HashMap<>();

    static {
        TO_REST_METHOD.put("GET", RestMethod.GET);
        TO_REST_METHOD.put("PUT", RestMethod.PUT);
        TO_REST_METHOD.put("POST", RestMethod.POST);
        TO_REST_METHOD.put("DELETE", RestMethod.DELETE);
        TO_REST_METHOD.put("PATCH", RestMethod.PATCH);
        TO_REST_METHOD.put("OPTIONS", RestMethod.OPTIONS);
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        final ClientHttpResponse response = execution.execute(request, body);
        logErrorResponse(request, response);
        registerForSerenity(request, body, response);

        return response;
    }

    private void registerForSerenity(final HttpRequest request, final byte[] body, final ClientHttpResponse response)
            throws IOException {
        final String jsonResponse = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());

        final RestQuery query = new RestQuery(TO_REST_METHOD.get(request.getMethodValue()), request.getURI().toString())
                .withRequestHeaders(request.getHeaders().toString()).withContent(new String(body, "UTF-8"))
                .withResponseHeaders(response.getHeaders().toString()).withResponse(jsonResponse);

        StepEventBus.getEventBus().getBaseStepListener().recordRestQuery(query);
    }

    private void logRequest(final HttpRequest request, final byte[] body) throws IOException {
        LOGGER.debug("Request URI : {}, Method : {}, Headers : {}, Request body : {}", request.getURI(),
                request.getMethod(), request.getHeaders(), new String(body, "UTF-8"));

    }

    private void logErrorResponse(final HttpRequest request, final ClientHttpResponse response) throws IOException {
        final StringBuilder builder = new StringBuilder("Received \"").append(response.getRawStatusCode()).append(" ")
                .append(response.getStatusText()).append("\" response for ").append(request.getMethod())
                .append(" request to ").append(request.getURI());
        final HttpHeaders responseHeaders = response.getHeaders();
        final long contentLength = responseHeaders.getContentLength();
        if (contentLength != 0) {
            if (hasTextBody(responseHeaders)) {
                final String bodyText = StreamUtils.copyToString(response.getBody(), determineCharset(responseHeaders));
                builder.append(": [\n").append(bodyText).append("\n]");
            }
            if (contentLength == -1) {
                builder.append(" with content of unknown length");
            } else {
                builder.append(" with content of length ").append(contentLength);
            }
            final MediaType contentType = responseHeaders.getContentType();
            if (contentType != null) {
                builder.append(" and content type ").append(contentType);
            } else {
                builder.append(" and unknown content type");
            }
        }
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
        final String res = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
        if (response.getStatusCode().isError()) {
            try {
                VitamUIError error = null;
                // on HEAD requests, we don't have a body
                if (request.getMethod() != HttpMethod.HEAD) {
                    error = mapper.readValue(res, VitamUIError.class);
                    LOGGER.debug("Response Status code : {}, Status text : {}, Headers : {}, Response body: {} !!! {}",
                            response.getStatusCode(), response.getStatusText(), response.getHeaders(),
                            builder.toString(), error);
                } else {
                    LOGGER.debug("Response Status code : {}, Status text : {}, Headers : {}", response.getStatusCode(),
                            response.getStatusText(), response.getHeaders());
                }
            }
            catch (final Exception ex) {
                LOGGER.error("Response Status code : {}, Status text : {}, Headers : {}, Response body: {} !!! {} !!! {}",
                        response.getStatusCode(), response.getStatusText(), response.getHeaders(), builder.toString(),
                        StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()), ex);
            }
        } else {
            LOGGER.debug("Response Status code : {}, Status text : {}, Headers : {}, Response body: {} !!! {}",
                    response.getStatusCode(), response.getStatusText(), response.getHeaders(), builder.toString(), res);
        }
    }

    protected boolean hasTextBody(final HttpHeaders headers) {
        final MediaType contentType = headers.getContentType();
        if (contentType != null) {
            if ("text".equals(contentType.getType())) {
                return true;
            }
            final String subtype = contentType.getSubtype();
            if (subtype != null) {
                return "xml".equals(subtype) || "json".equals(subtype) || subtype.endsWith("+xml")
                        || subtype.endsWith("+json");
            }
        }
        return false;
    }

    protected Charset determineCharset(final HttpHeaders headers) {
        final MediaType contentType = headers.getContentType();
        if (contentType != null) {
            try {
                final Charset charSet = contentType.getCharset();
                if (charSet != null) {
                    return charSet;
                }
            }
            catch (final UnsupportedCharsetException e) {
                // ignore
            }
        }
        return StandardCharsets.UTF_8;
    }

}
