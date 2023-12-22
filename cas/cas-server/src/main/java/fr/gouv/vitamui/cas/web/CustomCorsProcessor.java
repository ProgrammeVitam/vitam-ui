package fr.gouv.vitamui.cas.web;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class CustomCorsProcessor extends DefaultCorsProcessor {

    private static final String IDP_LOCATION_XPATH_EXPRESSION = "//*/SingleSignOnService[@Binding='urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST']/@Location";

    private static final String IDP_ENTITY_XPATH_EXPRESSION = "//*/@entityID";

    // CUSTO:
    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    static final private Set<String> ALLOWED_ORIGINS_WITHOUT_CREDENTIALS = Set.of("http://localhost");

    protected boolean handleInternal(ServerHttpRequest serverRequest, ServerHttpResponse response,
                                     CorsConfiguration config, boolean preFlightRequest) throws IOException {

        String requestOrigin = serverRequest.getHeaders().getOrigin();
        String allowOrigin = checkOrigin(config, requestOrigin);
        HttpHeaders responseHeaders = response.getHeaders();

        // CUSTO:
        if (ALLOWED_ORIGINS_WITHOUT_CREDENTIALS.contains(requestOrigin)) {
            allowOrigin = requestOrigin;
        }

        if (serverRequest instanceof ServletServerHttpRequest) {
            val request = ((ServletServerHttpRequest) serverRequest).getServletRequest();

            val uri = request.getRequestURI();
            val clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
            if (StringUtils.endsWith(uri, "/login") && StringUtils.isNotBlank(clientName)) {
                LOGGER.debug("Delegated authn callback for clientName: {}", clientName);
                val identityProvider = identityProviderHelper.findByTechnicalName(providersService.getProviders(), clientName);
                if (identityProvider.isPresent()) {
                    String providerUrl = null;
                    val provider = identityProvider.get();
                    // SAML?
                    val samlMetadata = provider.getIdpMetadata();
                    if (StringUtils.isNotBlank(samlMetadata)) {
                        providerUrl = getSamlProviderUrl(provider);
                        // OIDC?
                    } else {
                        val discoveryUrl = provider.getDiscoveryUrl();
                        if (StringUtils.isNotBlank(discoveryUrl)) {
                            providerUrl = discoveryUrl;
                        }
                    }
                    LOGGER.debug("providerUrl: {}", providerUrl);
                    if (StringUtils.isNotBlank(providerUrl)) {
                        val followingSlash = providerUrl.indexOf("/", 9);
                        if (followingSlash < 0) {
                            allowOrigin = providerUrl;
                        } else {
                            allowOrigin = providerUrl.substring(0, followingSlash);
                        }
                    }
                    LOGGER.debug("allowOrigin: {}", allowOrigin);
                }
            }
        }
        //

        if (allowOrigin == null) {
            LOGGER.debug("Reject: '" + requestOrigin + "' origin is not allowed");
            rejectRequest(response);
            return false;
        }

        HttpMethod requestMethod = getMethodToUse(serverRequest, preFlightRequest);
        List<HttpMethod> allowMethods = checkMethods(config, requestMethod);
        if (allowMethods == null) {
            LOGGER.debug("Reject: HTTP '" + requestMethod + "' is not allowed");
            rejectRequest(response);
            return false;
        }

        List<String> requestHeaders = getHeadersToUse(serverRequest, preFlightRequest);
        List<String> allowHeaders = checkHeaders(config, requestHeaders);
        if (preFlightRequest && allowHeaders == null) {
            LOGGER.debug("Reject: headers '" + requestHeaders + "' are not allowed");
            rejectRequest(response);
            return false;
        }

        responseHeaders.setAccessControlAllowOrigin(allowOrigin);

        if (preFlightRequest) {
            responseHeaders.setAccessControlAllowMethods(allowMethods);
        }

        if (preFlightRequest && !allowHeaders.isEmpty()) {
            responseHeaders.setAccessControlAllowHeaders(allowHeaders);
        }

        if (!CollectionUtils.isEmpty(config.getExposedHeaders())) {
            responseHeaders.setAccessControlExposeHeaders(config.getExposedHeaders());
        }

        // CUSTO:
        if (Boolean.TRUE.equals(config.getAllowCredentials()) && !ALLOWED_ORIGINS_WITHOUT_CREDENTIALS.contains(requestOrigin)) {
            responseHeaders.setAccessControlAllowCredentials(true);
        }

        if (preFlightRequest && config.getMaxAge() != null) {
            responseHeaders.setAccessControlMaxAge(config.getMaxAge());
        }

        response.flush();
        return true;
    }

    private HttpMethod getMethodToUse(ServerHttpRequest request, boolean isPreFlight) {
        return (isPreFlight ? request.getHeaders().getAccessControlRequestMethod() : request.getMethod());
    }

    private List<String> getHeadersToUse(ServerHttpRequest request, boolean isPreFlight) {
        HttpHeaders headers = request.getHeaders();
        return (isPreFlight ? headers.getAccessControlRequestHeaders() : new ArrayList<>(headers.keySet()));
    }

    private String getSamlProviderUrl(IdentityProviderDto provider) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try (StringReader samlMetadataCharacterStream = new StringReader(provider.getIdpMetadata());) {

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(samlMetadataCharacterStream));
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            var location = xpath.evaluate(IDP_LOCATION_XPATH_EXPRESSION, document);
            if (StringUtils.isNotBlank(location)) {
                return location;
            }

            var entityId = xpath.evaluate(IDP_ENTITY_XPATH_EXPRESSION, document);
            if (StringUtils.isNotBlank(entityId)) {
                return entityId;
            }

        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
            LOGGER.error("Error during IDP's URL extraction from IDP metadata of provider with Identifier: {}", provider.getIdentifier(), e);
        }
        return null;
    }
}
