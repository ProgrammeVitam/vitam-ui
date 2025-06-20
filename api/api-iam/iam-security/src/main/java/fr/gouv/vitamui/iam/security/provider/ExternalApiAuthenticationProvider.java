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

import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.security.authentication.AuthenticationToken;
import fr.gouv.vitamui.iam.security.service.UserAuthenticationService;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.openapiclient.ContextsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * General authentication provider for the External API.
 *
 *
 */
public class ExternalApiAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiAuthenticationProvider.class);

    private final ContextsApi contextsApi;
    private final UserAuthenticationService userAuthenticationService;

    public ExternalApiAuthenticationProvider(
        final ContextsApi contextsApi,
        final UserAuthenticationService userAuthenticationService
    ) {
        this.contextsApi = contextsApi;
        this.userAuthenticationService = userAuthenticationService;
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
            final HttpContext httpContext = (HttpContext) token.getPrincipal();
            final X509Certificate certificate = (X509Certificate) token.getCredentials();
            LOGGER.debug("Principal: {}", httpContext);
            LOGGER.debug("Credential not null?: {}", certificate != null);

            if (httpContext != null && certificate != null) {
                try {
                    final ContextDto context = getContextFromHttpContext(httpContext, certificate);
                    final AuthUserDto userDto = userAuthenticationService.getUserFromHttpContext(token);
                    final Integer tenantIdentifier = httpContext.getTenantIdentifier();
                    final List<String> intersectionRoles = getRoles(context, userDto, tenantIdentifier);

                    final String applicationId = buildApplicationId(userDto, httpContext, context);
                    final HttpContext newHttpContext = HttpContext.buildFromExternalHttpContext(
                        httpContext,
                        applicationId
                    );

                    return new AuthenticationToken(userDto, newHttpContext, certificate, intersectionRoles);
                } catch (final InvalidAuthenticationException vitamuiException) {
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

    /**
     * Method for build X-Application-Id params
     * @param user
     * @param httpContext
     * @param context
     * @return
     */
    public String buildApplicationId(final AuthUserDto user, final HttpContext httpContext, final ContextDto context) {
        return VitamUIUtils.generateApplicationId(
            httpContext.getApplicationId(),
            context.getName(),
            user.getIdentifier(),
            user.getSuperUserIdentifier(),
            user.getCustomerIdentifier(),
            httpContext.getRequestId()
        );
    }

    /**
     * Retrieve Security Context associated with the request and check context security.
     *
     * @param httpContext
     * @param certificate
     * @return
     */
    public ContextDto getContextFromHttpContext(final HttpContext httpContext, final X509Certificate certificate) {
        String certificateBase64;
        try {
            certificateBase64 = Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (final CertificateEncodingException e) {
            throw new InvalidFormatException("Invalid certificate: " + e.getMessage());
        }

        try {
            final ContextDto context = contextsApi.findByCertificate(certificateBase64);
            LOGGER.debug("authenticateInternal context={}", context);

            final List<Integer> contextTenants = context.getTenants();
            final Integer tenantIdentifier = httpContext.getTenantIdentifier();

            if (!context.isFullAccess() && tenantIdentifier != null && !contextTenants.contains(tenantIdentifier)) {
                LOGGER.warn(
                    "[InvalidAuthenticationException] This tenant: {} is not allowed for the application context: {}. credential={}",
                    tenantIdentifier,
                    httpContext.getIdentity(),
                    certificate
                );
                throw new BadCredentialsException(
                    "This tenant: " +
                    tenantIdentifier +
                    " is not allowed for the application context: " +
                    httpContext.getIdentity()
                );
            }
            return context;
        } catch (final NotFoundException e) {
            LOGGER.error(
                "Certificate not found [IssuerDN={}, certificateBase64={}, credential={}]",
                certificate.getIssuerDN(),
                certificateBase64,
                certificate
            );
            throw e;
        }
    }

    /**
     * Get Roles looking at the security context and user profile.
     * @param context
     * @param userProfile
     * @return
     */
    public List<String> getRoles(final ContextDto context, final AuthUserDto userProfile, final int tenantIdentifier) {
        final List<String> contextRoles = context.extractRoleNames();
        LOGGER.debug("context roles: {}", contextRoles);

        final List<String> userRoles = getUserRoles(userProfile, tenantIdentifier);

        return contextRoles
            .stream()
            .filter(userRoles::contains)
            // Prevent using "ROLE_INTERNAL" from external
            .filter(role -> !ServicesData.ROLE_INTERNAL.equals(role))
            .toList();
    }

    protected List<String> getUserRoles(final AuthUserDto userProfile, final int tenantIdentifier) {
        final List<String> userRoles = new ArrayList<>();
        userProfile
            .getProfileGroup()
            .getProfiles()
            .stream()
            .filter(profile -> profile.getTenantIdentifier() == tenantIdentifier)
            .filter(ProfileDto::isEnabled)
            .forEach(profile -> {
                final List<String> rolesNames = profile
                    .getRoles()
                    .stream()
                    .map(role -> ApiUtils.ensureHasRolePrefix(role.getName()))
                    .toList();
                userRoles.addAll(rolesNames);
            });

        LOGGER.debug("user roles: {}", userRoles);
        return userRoles;
    }
}
