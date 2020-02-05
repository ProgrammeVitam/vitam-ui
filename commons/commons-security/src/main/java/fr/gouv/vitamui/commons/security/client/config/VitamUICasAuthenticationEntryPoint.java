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
package fr.gouv.vitamui.commons.security.client.config;

import static fr.gouv.vitamui.commons.api.CommonConstants.AJAX_HEADER_NAME;
import static fr.gouv.vitamui.commons.api.CommonConstants.AJAX_HEADER_VALUE;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import fr.gouv.vitamui.commons.api.CommonConstants;

public class VitamUICasAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean {

    // ~ Instance fields
    // ================================================================================================
    private ServiceProperties serviceProperties;

    private String loginUrl;

    /**
     * Determines whether the Service URL should include the session id for the specific
     * user. As of CAS 3.0.5, the session id will automatically be stripped. However,
     * older versions of CAS (i.e. CAS 2), do not automatically strip the session
     * identifier (this is a bug on the part of the older server implementations), so an
     * option to disable the session encoding is provided for backwards compatibility.
     *
     * By default, encoding is enabled.
     */
    private boolean encodeServiceUrlWithSessionId = true;

    @Value("${server.port}")
    @NotNull
    private int port;

    // ~ Methods
    // ========================================================================================================

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(loginUrl, "loginUrl must be specified");
        Assert.notNull(serviceProperties, "serviceProperties must be specified");
        Assert.notNull(serviceProperties.getService(), "serviceProperties.getService() cannot be null.");
    }

    @Override
    public final void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authenticationException)
            throws IOException, ServletException {

        final String urlEncodedService = createServiceUrl(request, response);
        String redirectUrl = createRedirectUrl(urlEncodedService);
        // customization:
        // pass the cas_idp parameter to the CAS server as the idp parameter
        final String casIdpParameter = request.getParameter(CommonConstants.CAS_IDP_PARAMETER);
        if (StringUtils.isNotBlank(casIdpParameter)) {
            redirectUrl += "&" + CommonConstants.IDP_PARAMETER + "=" + URLEncoder.encode(casIdpParameter, "UTF-8");
        }
        // pass the cas_username parameter to the CAS server as the username parameter
        final String casUsernameParameter = request.getParameter(CommonConstants.CAS_USERNAME_PARAMETER);
        if (StringUtils.isNotBlank(casUsernameParameter)) {
            redirectUrl += "&" + CommonConstants.USERNAME_PARAMETER + "=" + URLEncoder.encode(casUsernameParameter, "UTF-8");
        }

        preCommence(request, response);

        if (StringUtils.isNotEmpty(request.getHeader(AJAX_HEADER_NAME)) && request.getHeader(AJAX_HEADER_NAME).equalsIgnoreCase(AJAX_HEADER_VALUE)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, redirectUrl);
        }
        else {
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Constructs a new Service Url. The default implementation relies on the CAS client
     * to do the bulk of the work.
     * @param request the HttpServletRequest
     * @param response the HttpServlet Response
     * @return the constructed service url. CANNOT be NULL.
     */
    protected String createServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {
        return CommonUtils.constructServiceUrl(null, response, serviceProperties.getService(), null, serviceProperties.getArtifactParameter(),
                encodeServiceUrlWithSessionId);
    }

    /**
     * Constructs the Url for Redirection to the CAS server. Default implementation relies
     * on the CAS client to do the bulk of the work.
     *
     * @param serviceUrl the service url that should be included.
     * @return the redirect url. CANNOT be NULL.
     */
    protected String createRedirectUrl(final String serviceUrl) {
        return CommonUtils.constructRedirectUrl(loginUrl, serviceProperties.getServiceParameter(), serviceUrl, serviceProperties.isSendRenew(), false);
    }

    /**
     * Template method for you to do your own pre-processing before the redirect occurs.
     *
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     */
    protected void preCommence(final HttpServletRequest request, final HttpServletResponse response) {

    }

    /**
     * The enterprise-wide CAS login URL. Usually something like
     * <code>https://www.mycompany.com/cas/login</code>.
     *
     * @return the enterprise-wide CAS login URL
     */
    public final String getLoginUrl() {
        return loginUrl;
    }

    public final ServiceProperties getServiceProperties() {
        return serviceProperties;
    }

    public final void setLoginUrl(final String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public final void setServiceProperties(final ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    /**
     * Sets whether to encode the service url with the session id or not.
     *
     * @param encodeServiceUrlWithSessionId whether to encode the service url with the
     * session id or not.
     */
    public final void setEncodeServiceUrlWithSessionId(final boolean encodeServiceUrlWithSessionId) {
        this.encodeServiceUrlWithSessionId = encodeServiceUrlWithSessionId;
    }

    /**
     * Sets whether to encode the service url with the session id or not.
     * @return whether to encode the service url with the session id or not.
     *
     */
    protected boolean getEncodeServiceUrlWithSessionId() {
        return encodeServiceUrlWithSessionId;
    }

}
