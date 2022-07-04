package fr.gouv.vitamui.commons.test.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractRestControllerMockMvcTest extends AbstractServerIdentityBuilder {

    @Autowired
    protected MockMvc mockMvc;

    /**
     * Method for construct the uri
     *
     *
     * @param endpoint
     * @return
     */
    protected UriComponentsBuilder getUriBuilder(final String endpoint) {
        final String url = getRessourcePrefix() + endpoint;
        final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.scheme("https");
        builder.path(url);
        return builder;
    }

    /**
     * Method for construct the uri
     * @return
     */
    protected UriComponentsBuilder getUriBuilder() {
        final String url = getRessourcePrefix();
        final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.scheme("https");
        builder.path(url);
        return builder;
    }

    /** Convert Object to json
     *
     * @param Object
     * @return
     */
    protected static String asJsonString(final Object Object) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            final String jsonContent = mapper.writeValueAsString(Object);
            return jsonContent;
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Method for addParams to a builder
     *
     * @param params
     * @param builder
     */
    protected void addParams(final Map<String, Object> params, final UriComponentsBuilder builder) {
        params.forEach((key, value) -> {
            if (value instanceof List) {
                final List<Object> values = (List) value;
                for (final Object val : values) {
                    builder.queryParam(key, val);
                }
            }
            else {
                builder.queryParam(key, value);
            }
        });
    }

    /**
     *
     * @return the url ressource prefix
     */
    protected abstract String getRessourcePrefix();

    /**
     * Method for perform mvc request
     * @param builder
     * @param method
     * @param resultMatcher
     * @param headers
     * @return
     */
    protected ResultActions perform(final UriComponentsBuilder builder, final String jsonBody, final HttpMethod method, final ResultMatcher resultMatcher,
            final HttpHeaders headers) {
        ResultActions result = null;
        MockHttpServletRequestBuilder request = null;
        switch (method) {
            case GET :
                request = MockMvcRequestBuilders.get(builder.build().toUri()).contentType(MediaType.APPLICATION_JSON);
                break;
            case HEAD :
                request = MockMvcRequestBuilders.head(builder.build().toUri());
                break;
            case PATCH :
                request = MockMvcRequestBuilders.patch(builder.build().toUri());
                break;
            case DELETE :
                request = MockMvcRequestBuilders.delete(builder.build().toUri());
                break;
            case PUT :
                request = MockMvcRequestBuilders.put(builder.build().toUri());
                break;
            case POST :
                request = MockMvcRequestBuilders.post(builder.build().toUri());
                break;
            default :
                break;
        }
        if (StringUtils.isNoneBlank(jsonBody)) {
            request.contentType(MediaType.APPLICATION_JSON).content(jsonBody);
        }
        request.headers(headers);
        request.with(authentication(buildUserAuthenticated()));
        request.with(SecurityMockMvcRequestPostProcessors.csrf());
        try {
            result = mockMvc.perform(request).andDo(print()).andExpect(resultMatcher);
            return result;
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;
    }

    protected ResultActions performHead(final String endpoint, final ResultMatcher resultMatcher) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return perform(builder, StringUtils.EMPTY, HttpMethod.HEAD, resultMatcher, getHeaders());
    }

    protected ResultActions performHead(final String endpoint, final Map<String, Object> params) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        addParams(params, builder);
        return perform(builder, StringUtils.EMPTY, HttpMethod.HEAD, status().isOk(), getHeaders());
    }

    protected ResultActions performHead(final String endpoint, final Map<String, Object> params, final ResultMatcher resultMatcher) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        addParams(params, builder);
        return perform(builder, StringUtils.EMPTY, HttpMethod.HEAD, resultMatcher, getHeaders());
    }

    protected ResultActions performHead(final String endpoint, final Map<String, Object> params, final ResultMatcher resultMatcher, final HttpHeaders headers) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        addParams(params, builder);
        return perform(builder, StringUtils.EMPTY, HttpMethod.HEAD, resultMatcher, headers);
    }

    protected ResultActions performHead(final UriComponentsBuilder builder, final HttpHeaders headers) {
        return perform(builder, StringUtils.EMPTY, HttpMethod.HEAD, status().isOk(), headers);
    }

    protected ResultActions performGet(final String endpoint) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return perform(builder, StringUtils.EMPTY, HttpMethod.GET, status().isOk(), getHeaders());
    }

    protected ResultActions performGet(final String endpoint, final HttpHeaders headers) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return perform(builder, StringUtils.EMPTY, HttpMethod.GET, status().isOk(), headers);
    }

    protected ResultActions performGet(final UriComponentsBuilder builder) {
        return perform(builder, StringUtils.EMPTY, HttpMethod.GET, status().isOk(), getHeaders());
    }

    protected ResultActions performGet(final UriComponentsBuilder builder, final HttpHeaders headers) {
        return perform(builder, StringUtils.EMPTY, HttpMethod.GET, status().isOk(), headers);
    }

    protected ResultActions performGet(final String endpoint, final Map<String, Object> params) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        addParams(params, builder);
        return perform(builder, StringUtils.EMPTY, HttpMethod.GET, status().isOk(), getHeaders());
    }

    protected ResultActions performGet(final String endpoint, final Map<String, Object> params, final ResultMatcher resultMatcher) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        addParams(params, builder);
        return perform(builder, StringUtils.EMPTY, HttpMethod.GET, resultMatcher, getHeaders());
    }

    protected ResultActions performGet(final String endpoint, final Map<String, Object> params, final HttpHeaders headers) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        addParams(params, builder);
        return perform(builder, StringUtils.EMPTY, HttpMethod.GET, status().isOk(), headers);
    }

    protected ResultActions performGet(final String endpoint, final Map<String, Object> params, final HttpHeaders headers, final ResultMatcher resultMatcher) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        addParams(params, builder);
        return perform(builder, StringUtils.EMPTY, HttpMethod.GET, resultMatcher, headers);
    }

    /**
     * Method for performMultipart
     * @param builder
     * @param method
     * @param parts
     * @param resultMatcher
     * @param headers
     * @return ResultActions
     */
    private ResultActions performMultiPart(final UriComponentsBuilder builder, final HttpMethod method, final Collection<MockMultipartFile> parts,
            final ResultMatcher resultMatcher, final HttpHeaders headers) {
        ResultActions result = null;
        final MockMultipartHttpServletRequestBuilder request;
        request = MockMvcRequestBuilders.multipart(builder.build().toUri());
        request.with(authentication(buildUserAuthenticated()));
        request.headers(headers);
        final RequestPostProcessor requestPostProcessor = getRequestPostProcessor(method);
        request.with(requestPostProcessor);
        request.with(SecurityMockMvcRequestPostProcessors.csrf());
        parts.forEach(p -> request.file(p));
        try {
            result = mockMvc.perform(request).andDo(print()).andExpect(resultMatcher);
            return result;
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;
    }

    /**
     *
     * @param method
     * @return RequestProcessor
     */
    private RequestPostProcessor getRequestPostProcessor(final HttpMethod method) {
        RequestPostProcessor requestPostProcessor = null;
        switch (method) {
            case POST :
                requestPostProcessor = request -> {
                    request.setMethod("POST");
                    return request;
                };
                break;
            case PUT :
                requestPostProcessor = request -> {
                    request.setMethod("PUT");
                    return request;
                };
            case PATCH :
                requestPostProcessor = request -> {
                    request.setMethod("PATCH");
                    return request;
                };
            default :
                break;
        }
        return requestPostProcessor;
    }

    protected ResultActions performPostMultipart(final String endpoint, final Collection<MockMultipartFile> parts) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return performMultiPart(builder, HttpMethod.POST, parts, status().isCreated(), getHeaders());
    }

    protected ResultActions performPostMultipart(final String endpoint, final Collection<MockMultipartFile> parts, final HttpHeaders headers) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return performMultiPart(builder, HttpMethod.POST, parts, status().isCreated(), headers);
    }

    protected ResultActions performPostMultipart(final UriComponentsBuilder builder, final Collection<MockMultipartFile> parts) {
        return performMultiPart(builder, HttpMethod.POST, parts, status().isCreated(), getHeaders());
    }

    protected ResultActions performPostMultipart(final UriComponentsBuilder builder, final Collection<MockMultipartFile> parts, final HttpHeaders headers) {
        return performMultiPart(builder, HttpMethod.POST, parts, status().isCreated(), headers);
    }

    protected ResultActions performPostMultipart(
        final UriComponentsBuilder builder, final Collection<MockMultipartFile> parts,
        final ResultMatcher status, final HttpHeaders headers) {
        return performMultiPart(builder, HttpMethod.POST, parts, status, headers);
    }

    protected ResultActions performPutMultipart(final UriComponentsBuilder builder, final HttpHeaders headers, final Collection<MockMultipartFile> parts) {
        return performMultiPart(builder, HttpMethod.PUT, parts, status().isOk(), headers);
    }

    protected ResultActions performPatchMultipart(final String endpoint, final Collection<MockMultipartFile> parts) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return performMultiPart(builder, HttpMethod.PATCH, parts, status().isOk(), getHeaders());
    }

    protected ResultActions performPatchMultipart(final UriComponentsBuilder builder, final Collection<MockMultipartFile> parts) {
        return performMultiPart(builder, HttpMethod.PATCH, parts, status().isOk(), getHeaders());
    }

    protected ResultActions performPatchMultipart(final UriComponentsBuilder builder, final Collection<MockMultipartFile> parts, final HttpHeaders headers) {
        return performMultiPart(builder, HttpMethod.PATCH, parts, status().isOk(), headers);
    }

    protected ResultActions performPost(final UriComponentsBuilder builder, final String jsonBody, final ResultMatcher resultMatcher) {
        return perform(builder, jsonBody, HttpMethod.POST, resultMatcher, getHeaders());
    }

    protected ResultActions performPatch(final String endpoint) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return perform(builder, StringUtils.EMPTY, HttpMethod.PATCH, status().isOk(), getHeaders());
    }

    protected ResultActions performPatch(final String endpoint, final String jsonBody) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return perform(builder, jsonBody, HttpMethod.PATCH, status().isOk(), getHeaders());
    }

    protected ResultActions performDelete(final String endpoint) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        return perform(builder, StringUtils.EMPTY, HttpMethod.DELETE, status().isOk(), getHeaders());
    }

    protected ResultActions performDelete(final String endpoint, final Map<String, Object> params) {
        final UriComponentsBuilder builder = getUriBuilder(endpoint);
        addParams(params, builder);
        return perform(builder, StringUtils.EMPTY, HttpMethod.DELETE, status().isOk(), getHeaders());
    }

    protected abstract Authentication buildUserAuthenticated();

    protected HttpHeaders getHeaders() {
        return new HttpHeaders();
    }
}
