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
package fr.gouv.vitamui.iam.security.provider;

import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.authentication.ExternalAuthentication;
import fr.gouv.vitamui.iam.security.service.ExternalAuthentificationService;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * General authentication provider for the External API.
 *
 *
 */

public class ExternalApiAuthenticationProvider implements AuthenticationProvider {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ExternalApiAuthenticationProvider.class);

    private final ExternalAuthentificationService extAuthService;

    /**
     * ExternalApiAuthenticationProvider
     * @param externalAuthentificationService
     */
    @Autowired
    public ExternalApiAuthenticationProvider(final ExternalAuthentificationService externalAuthentificationService) {
        extAuthService = externalAuthentificationService;
    }

    /**
     * This method is called by the Spring Security Filter
     *
     * {@inheritDoc}
     */
    @Override
    public Authentication authenticate(final Authentication authentication) {
        if (supports(authentication.getClass())) {

            final PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authentication;
            final ExternalHttpContext httpContext = (ExternalHttpContext) token.getPrincipal();
            final X509Certificate certificate = (X509Certificate) token.getCredentials();
            LOGGER.debug("Principal: {}", httpContext);
            LOGGER.debug("Credential not null?: {}", certificate != null);

            if (httpContext != null && certificate != null) {
                try {
                    final ContextDto context = extAuthService.getContextFromHttpContext(httpContext, certificate);
                    final AuthUserDto userDto = extAuthService.getUserFromHttpContext(httpContext);
                    final Integer tenantIdentifier = httpContext.getTenantIdentifier();
                    final List<String> intersectionRoles = extAuthService.getRoles(context, userDto, tenantIdentifier);

                    final String applicationId = extAuthService.buildApplicationId(userDto, httpContext, context) ;
                    final ExternalHttpContext newHttpContext = ExternalHttpContext.buildFromExternalHttpContext(httpContext, applicationId);

                    return new ExternalAuthentication(userDto, newHttpContext, certificate, intersectionRoles);
                }
                catch (final InvalidAuthenticationException vitamuiException) {
                    throw new BadCredentialsException(vitamuiException.getMessage());
                }
            }
        }

        throw new BadCredentialsException("Unable to authenticate REST call");
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

}
