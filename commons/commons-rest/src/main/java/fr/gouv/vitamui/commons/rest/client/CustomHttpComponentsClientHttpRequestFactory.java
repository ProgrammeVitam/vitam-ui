package fr.gouv.vitamui.commons.rest.client;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.http.client.HttpClient;
import java.net.URI;

/**
 * Custom HttpComponentsClientHttpRequestFactory to override createContext
 */
public class CustomHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomHttpComponentsClientHttpRequestFactory.class);

    /**
     * default construct
     */
    public CustomHttpComponentsClientHttpRequestFactory() {
        super();
    }

    /**
     * construct with httpClient
     * @param httpClient
     */
    public CustomHttpComponentsClientHttpRequestFactory(HttpClient httpClient) {
        super(httpClient);
    }

    /**
     * Create the httpContext and init with a userToken
     * @param httpMethod
     * @param uri
     * @return
     */
    @Override
    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri)  {
        LOGGER.debug("Context creation");
        HttpContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.USER_TOKEN, "fake_user_token_value");
        return context;
    }

}
