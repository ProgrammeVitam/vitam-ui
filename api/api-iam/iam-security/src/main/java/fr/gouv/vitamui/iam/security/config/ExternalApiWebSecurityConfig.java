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
package fr.gouv.vitamui.iam.security.config;

import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.security.filter.ExternalRequestHeadersAuthenticationFilter;
import fr.gouv.vitamui.iam.security.filter.TokenExtractor;
import fr.gouv.vitamui.iam.security.filter.X509CertificateExtractor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_TOKEN_HEADER;

/**
 * The security configuration.
 *
 *
 */
@Getter
@Setter
public class ExternalApiWebSecurityConfig extends AbstractApiWebSecurityConfig {

    private static final String GATEWAY_ENABLED = "gateway.enabled";

    private static final String CLIENT_CERTIFICATE_HEADER_NAME = "server.ssl.client-certificate-header-name";

    private final boolean isGatewayEnabled;

    public ExternalApiWebSecurityConfig(
        final AuthenticationProvider apiAuthenticationProvider,
        final RestExceptionHandler restExceptionHandler,
        final Environment env
    ) {
        super(apiAuthenticationProvider, restExceptionHandler, env);
        isGatewayEnabled = env.getProperty(GATEWAY_ENABLED, Boolean.class, false);
    }

    @Override
    protected AbstractPreAuthenticatedProcessingFilter getRequestHeadersAuthenticationFilter() throws Exception {
        return new ExternalRequestHeadersAuthenticationFilter(
            authenticationManager(),
            getX509CertificateExtractors(),
            getTokenExtractors()
        );
    }

    // This is a temporary patch to allow mTLS authentication behind reverse proxy or full mTLS during migration
    private List<X509CertificateExtractor> getX509CertificateExtractors() {
        final List<X509CertificateExtractor> x509CertificateExtractors = new ArrayList<>();
        x509CertificateExtractors.add(X509CertificateExtractor.requestAttributeX509CertificateExtractor());
        if (isGatewayEnabled) {
            final String certificateHeaderName = env.getProperty(CLIENT_CERTIFICATE_HEADER_NAME);
            x509CertificateExtractors.add(
                X509CertificateExtractor.requestHeaderX509CertificateExtractor(certificateHeaderName)
            );
        }
        return x509CertificateExtractors;
    }

    // This is a temporary patch to get authentication token when service is behind a gateway during migration
    private List<TokenExtractor> getTokenExtractors() {
        final List<TokenExtractor> tokenExtractors = new ArrayList<>();
        tokenExtractors.add(TokenExtractor.headerExtractor(X_USER_TOKEN_HEADER));
        if (isGatewayEnabled) {
            tokenExtractors.add(TokenExtractor.bearerExtractor());
        }
        return tokenExtractors;
    }
}
