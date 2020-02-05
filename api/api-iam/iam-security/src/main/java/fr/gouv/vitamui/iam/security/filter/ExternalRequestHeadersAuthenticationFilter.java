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
package fr.gouv.vitamui.iam.security.filter;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;

import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;

/**
 * The authentication filter based on the request headers.
 *
 *
 */
public class ExternalRequestHeadersAuthenticationFilter extends X509AuthenticationFilter {

    /**
     * RequestHeadersAuthenticationFilter.
     * @param authenticationManager
     */
    public ExternalRequestHeadersAuthenticationFilter(final AuthenticationManager authenticationManager) {
        super();
        setAuthenticationManager(authenticationManager);
    }

    /**
     * Return as principal the External Http Context that is built from the request
     */
    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("request: " + request.getRequestURI() + " - " + request.getQueryString());
        }
        return ExternalHttpContext.buildFromExternalRequest(request);
    }

    /**
     * Return as credentials the the certficate extracted from the request
     */
    @Override
    protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
        return extractRequestClientCertificate(request);
    }

    private X509Certificate extractRequestClientCertificate(final HttpServletRequest request) {
        X509Certificate cert = null;
        final X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0) {
            cert = certs[0];
            if (logger.isDebugEnabled()) {
                logger.debug("X.509 client authentication certificate:" + cert.getSubjectX500Principal());
            }

        }
        else if (logger.isDebugEnabled()) {
            logger.debug("No client certificate found in request.");
        }

        return cert;
    }
}
