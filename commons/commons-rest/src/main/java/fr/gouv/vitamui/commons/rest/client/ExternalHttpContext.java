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
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServletServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * The context of an external REST call (to any API except security).
 *
 *
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExternalHttpContext extends AbstractHttpContext {

    private static final long serialVersionUID = 56932902134844917L;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ExternalHttpContext.class);

    public ExternalHttpContext(final Integer tenantIdentifier, final String userToken, final String applicationId, final String identity) {
        this(tenantIdentifier, userToken, applicationId, identity, null, null);
    }

    public ExternalHttpContext(final Integer tenantIdentifier, final String userToken, final String applicationId, final String identity,
            final String requestId) {
        this(tenantIdentifier, userToken, applicationId, identity, requestId, null);
    }

    public ExternalHttpContext(final Integer tenantIdentifier, final String userToken, final String applicationId, final String identity,
            final String requestId, final String accessContract) {
        super(tenantIdentifier, userToken, applicationId, identity, requestId, accessContract);
    }

    /**
     * Build an ExternalContext from request header in the UI layer.
     * Note. Usually called by the ExternalRequestHeadersAuthenticationFilter.
     */
    public static ExternalHttpContext buildFromUiRequest(final HttpServletRequest request, final AuthUserDto principal) {
        return buildFromUiRequest(request, principal.getAuthToken(), null, null);
    }

    /**
     * Build an ExternalContext from request header in the UI layer.
     * Note. Usually called by the ExternalRequestHeadersAuthenticationFilter.
     */
    public static ExternalHttpContext buildFromUiRequest(final HttpServletRequest request, final AuthUserDto principal, final Integer tenantIdentifier,
            final String accessContract) {
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
    private static ExternalHttpContext buildFromUiRequest(final HttpServletRequest request, final String userToken, final Integer tenantIdentifier,
            final String accessContract) {
        LOGGER.debug("Request Headers : {}", VitamUIUtils.secureFormatHeadersLogging(new ServletServerHttpRequest(request).getHeaders()));
        String applicationId = request.getHeader(CommonConstants.X_APPLICATION_ID_HEADER);
        final String identity = request.getHeader(CommonConstants.X_IDENTITY_HEADER);
        String requestId = request.getHeader(CommonConstants.X_REQUEST_ID_HEADER);
        if (StringUtils.isBlank(requestId)) {
            requestId = VitamUIUtils.generateRequestId();
        }

        // Only applicationid is kept through API's
        applicationId = applicationId + requestId;

        String accessContractToUse = accessContract;
        if (StringUtils.isBlank(accessContractToUse)) {
            accessContractToUse = request.getHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER);
        }
        Integer tenantIdentifierToUse = tenantIdentifier;
        if (tenantIdentifierToUse == null) {
            tenantIdentifierToUse = getTenantIdentifier(request.getHeader(CommonConstants.X_TENANT_ID_HEADER), request.getRequestURI());

        }
        return new ExternalHttpContext(tenantIdentifierToUse, userToken, applicationId, identity, requestId, accessContractToUse);
    }

    /**
     * Build an ExternalContext from request header in the External layer.
     * Note. Usually called by the ExternalRequestHeadersAuthenticationFilter in the PreAuthentification Phase.
     */
    public static ExternalHttpContext buildFromExternalRequest(final HttpServletRequest request) {
        LOGGER.debug("Request Headers : {}", VitamUIUtils.secureFormatHeadersLogging(new ServletServerHttpRequest(request).getHeaders()));
        final Integer tenantIdentifier = getTenantIdentifier(request.getHeader(CommonConstants.X_TENANT_ID_HEADER), request.getRequestURI());

        final String applicationId = request.getHeader(CommonConstants.X_APPLICATION_ID_HEADER);
        final String userToken = request.getHeader(CommonConstants.X_USER_TOKEN_HEADER);
        final String identity = request.getHeader(CommonConstants.X_IDENTITY_HEADER);
        final String requestId = UUID.randomUUID().toString();
        final String accessContract = request.getHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER);
        return new ExternalHttpContext(tenantIdentifier, userToken, applicationId, identity, requestId, accessContract);
    }

    /**
     * Build an ExternalContext from a previous external httpContext in the External layer.
     * Note. Usually called by the ExternalApiAuthenticationProvider in the Authentification Phase.
     */
    public static ExternalHttpContext buildFromExternalHttpContext(final ExternalHttpContext httpContext, final String applicationId) {
        return new ExternalHttpContext(httpContext.getTenantIdentifier(), httpContext.getUserToken(), applicationId, httpContext.getIdentity(),
                httpContext.getRequestId(), httpContext.getAccessContract());
    }

}
