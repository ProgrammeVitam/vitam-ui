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

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static fr.gouv.vitamui.commons.api.CommonConstants.X_TENANT_ID_HEADER;

/**
 * The context of an external REST call (to any API except security).
 *
 *
 */
@Getter
@EqualsAndHashCode
public class HttpContext implements Serializable {

    private static final long serialVersionUID = 56932902134844917L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpContext.class);

    private final Integer tenantIdentifier;
    private final String userToken;
    private final String applicationId;
    private final String identity;
    private final String requestId;
    private final String accessContract;
    private final String requestUri;

    // @formatter:off
    private static final List<String> CALLS_WITHOUT_TENANT_ID = Arrays.asList(
        "/actuator/health", "/actuator/prometheus", "/error/", "/favicon.ico",
        "/swagger-resources", "/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs", "/webjars"
    );

    // @formatter:on

    public HttpContext(
        final Integer tenantIdentifier,
        final String userToken,
        final String applicationId,
        final String identity,
        final String requestId,
        final String accessContract,
        final String requestUri
    ) {
        this.identity = identity;
        this.tenantIdentifier = tenantIdentifier;
        this.userToken = userToken;
        this.requestId = requestId;
        this.applicationId = applicationId;
        this.accessContract = accessContract;
        this.requestUri = requestUri;
        initMDC();
    }

    /**
     * Init variable for logg
     */
    private void initMDC() {
        MDC.put(CommonConstants.X_REQUEST_ID_HEADER, requestId);
        MDC.put(CommonConstants.X_APPLICATION_ID_HEADER, applicationId);
    }

    public HttpContext(
        final Integer tenantIdentifier,
        final String userToken,
        final String applicationId,
        final String identity,
        final String requestId,
        final String accessContract
    ) {
        this(tenantIdentifier, userToken, applicationId, identity, requestId, accessContract, null);
    }

    public HttpContext(
        final Integer tenantIdentifier,
        final String userToken,
        final String applicationId,
        final String identity,
        final String requestId
    ) {
        this(tenantIdentifier, userToken, applicationId, identity, requestId, null, null);
    }

    public HttpContext(
        final Integer tenantIdentifier,
        final String userToken,
        final String applicationId,
        final String identity
    ) {
        this(tenantIdentifier, userToken, applicationId, identity, null, null, null);
    }

    public static Integer getTenantIdentifier(final String tenant, final String url) {
        try {
            if (StringUtils.isNotBlank(tenant)) {
                return Integer.valueOf(tenant);
            }
        } catch (final NumberFormatException e) {
            throw new BadRequestException(
                String.format(
                    "%s header : Integer type was expected, instead value was %s. ",
                    X_TENANT_ID_HEADER,
                    tenant
                )
            );
        }
        if (urlNeedsTenantIdHeader(url)) {
            throw new InvalidAuthenticationException(CommonConstants.X_TENANT_ID_HEADER + " header is mandatory.");
        }
        return -1;
    }

    /**
     * Checks if the URL needs a tenantIdentifier, using our whitelist.
     * @param url
     * @return a boolean
     */
    public static boolean urlNeedsTenantIdHeader(final String url) {
        return CALLS_WITHOUT_TENANT_ID.stream()
            .noneMatch(
                whitelist -> StringUtils.isNotEmpty(url) && (whitelist.equalsIgnoreCase(url) || url.contains(whitelist))
            );
    }

    @Override
    public String toString() {
        return (
            "HttpContext(tenantIdentifier=" +
            tenantIdentifier +
            ", userToken(truncated)=" +
            StringUtils.substring(userToken, 0, StringUtils.length(userToken) / 2) +
            "****, applicationId= " +
            applicationId +
            ", identity=" +
            identity +
            ", requestId=" +
            requestId +
            ", accessContract=" +
            accessContract +
            ")"
        );
    }

    /**
     * Build an ExternalContext from request header in the UI layer.
     * Note. Usually called by the ExternalRequestHeadersAuthenticationFilter.
     */
    public static HttpContext buildFromUiRequest(final HttpServletRequest request, final AuthUserDto principal)
        throws PreconditionFailedException {
        return buildFromUiRequest(request, principal.getAuthToken(), null, null);
    }

    /**
     * Build an ExternalContext from request header in the UI layer.
     * Note. Usually called by the ExternalRequestHeadersAuthenticationFilter.
     */
    public static HttpContext buildFromUiRequest(
        final HttpServletRequest request,
        final AuthUserDto principal,
        final Integer tenantIdentifier,
        final String accessContract
    ) throws PreconditionFailedException {
        return buildFromUiRequest(request, principal.getAuthToken(), tenantIdentifier, accessContract);
    }

    /**
     * Build an ExternalContext from request header in the UI layer.
     * Note. Usually called by the ExternalRequestHeadersAuthenticationFilter.
     * @param request
     * @param userToken
     * @param tenantIdentifier
     * @param accessContract
     * @return
     */
    private static HttpContext buildFromUiRequest(
        final HttpServletRequest request,
        final String userToken,
        final Integer tenantIdentifier,
        final String accessContract
    ) throws PreconditionFailedException {
        LOGGER.debug(
            "Request Headers : {}",
            VitamUIUtils.secureFormatHeadersLogging(new ServletServerHttpRequest(request).getHeaders())
        );
        String applicationId = request.getHeader(CommonConstants.X_APPLICATION_ID_HEADER);
        final String identity = request.getHeader(CommonConstants.X_IDENTITY_HEADER);
        String requestId = request.getHeader(CommonConstants.X_REQUEST_ID_HEADER);
        SanityChecker.checkSecureParameter(applicationId, identity, requestId);
        if (StringUtils.isBlank(requestId)) {
            requestId = VitamUIUtils.generateRequestId();
        }

        // Only applicationid is kept through API's
        applicationId = applicationId + requestId;

        String accessContractToUse = accessContract;
        if (StringUtils.isBlank(accessContractToUse)) {
            accessContractToUse = request.getHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER);
            SanityChecker.checkSecureParameter(accessContractToUse);
        }
        Integer tenantIdentifierToUse = tenantIdentifier;
        if (tenantIdentifierToUse == null) {
            tenantIdentifierToUse = getTenantIdentifier(
                request.getHeader(CommonConstants.X_TENANT_ID_HEADER),
                request.getRequestURI()
            );
        }
        return new HttpContext(
            tenantIdentifierToUse,
            userToken,
            applicationId,
            identity,
            requestId,
            accessContractToUse
        );
    }

    /**
     * Build an ExternalContext from request header in the External layer.
     * Note. Usually called by the ExternalRequestHeadersAuthenticationFilter in the PreAuthentication Phase.
     */
    public static HttpContext buildFromExternalRequest(final HttpServletRequest request, String userToken) {
        LOGGER.debug(
            "Request Headers : {}",
            VitamUIUtils.secureFormatHeadersLogging(new ServletServerHttpRequest(request).getHeaders())
        );
        String requestUri = request.getRequestURI();
        final Integer tenantIdentifier = getTenantIdentifier(
            request.getHeader(CommonConstants.X_TENANT_ID_HEADER),
            requestUri
        );
        final String applicationId = request.getHeader(CommonConstants.X_APPLICATION_ID_HEADER);
        final String identity = request.getHeader(CommonConstants.X_IDENTITY_HEADER);
        final String requestId = UUID.randomUUID().toString();
        final String accessContract = request.getHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER);
        return new HttpContext(
            tenantIdentifier,
            userToken,
            applicationId,
            identity,
            requestId,
            accessContract,
            requestUri
        );
    }

    /**
     * Build an ExternalContext from a previous external httpContext in the External layer.
     * Note. Usually called by the ExternalApiAuthenticationProvider in the Authentification Phase.
     */
    public static HttpContext buildFromExternalHttpContext(final HttpContext httpContext, final String applicationId) {
        return new HttpContext(
            httpContext.getTenantIdentifier(),
            httpContext.getUserToken(),
            applicationId,
            httpContext.getIdentity(),
            httpContext.getRequestId(),
            httpContext.getAccessContract()
        );
    }
}
