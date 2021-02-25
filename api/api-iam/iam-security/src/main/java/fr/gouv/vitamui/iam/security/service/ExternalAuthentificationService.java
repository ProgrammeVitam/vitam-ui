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
package fr.gouv.vitamui.iam.security.service;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import lombok.Getter;

/**
 * External authentication service
 *
 *
 */
@Getter
public class ExternalAuthentificationService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ExternalAuthentificationService.class);

    private final UserInternalRestClient userInternalRestClient;

    private final ContextRestClient contextRestClient;

    @Autowired
    public ExternalAuthentificationService(final ContextRestClient contextRestClient, final UserInternalRestClient userInternalRestClient) {

        this.contextRestClient = contextRestClient;
        this.userInternalRestClient = userInternalRestClient;
    }

    /**
     * Method for build X-Application-Id params
     * @param user
     * @param httpContext
     * @param context
     * @return
     */
    public String buildApplicationId(final AuthUserDto user, final ExternalHttpContext httpContext, final ContextDto context) {
        return VitamUIUtils.generateApplicationId(httpContext.getApplicationId(), context.getName(), user.getIdentifier(), user.getSuperUserIdentifier(),
                user.getCustomerIdentifier(), httpContext.getRequestId());
    }

    /**
     * This method is called before authentification by the Authentication Provider
     */
    public AuthUserDto getUserFromHttpContext(final ExternalHttpContext httpContext) {
        final String userToken = httpContext.getUserToken();
        if (userToken == null) {
            throw new BadCredentialsException("User token not found: " + userToken);
        }

        final InternalHttpContext internalHttpContext = InternalHttpContext.buildFromExternalHttpContext(httpContext, null, null);
        final AuthUserDto userDto = userInternalRestClient.getMe(internalHttpContext);
        if (userDto == null) {
            throw new NotFoundException("User not found for token: " + userToken);
        }


        final Integer tenantIdentifier = httpContext.getTenantIdentifier();
        final List<Integer> userTenants = userDto.getProfileGroup().getProfiles().stream().filter(ProfileDto::isEnabled).map(ProfileDto::getTenantIdentifier)
                .collect(Collectors.toList());
        if (!userTenants.contains(tenantIdentifier)) {
            LOGGER.debug("Tenant id [{}] not in user tenants [{}]", tenantIdentifier, userTenants);
            throw new BadCredentialsException("This tenant: " + httpContext.getTenantIdentifier() + " is not allowed for this user: " + userDto.getId());
        }
        return userDto;
    }

    /**
     * Retrieve Security Context associated with the request and check context security.
     *
     * @param httpContext
     * @param certificate
     * @return
     */
    public ContextDto getContextFromHttpContext(final ExternalHttpContext httpContext, final X509Certificate certificate) {

        String certificateBase64;
        try {
            certificateBase64 = Base64.getEncoder().encodeToString(certificate.getEncoded());
        }
        catch (final CertificateEncodingException e) {
            throw new InvalidFormatException("Invalid certificate: " + e.getMessage());
        }

        final InternalHttpContext internalHttpContext = InternalHttpContext.buildFromExternalHttpContext(httpContext);

        try {
            final ContextDto context = contextRestClient.findByCertificate(internalHttpContext, certificateBase64);
            LOGGER.debug("authenticateInternal context={}", context);

            final List<Integer> contextTenants = context.getTenants();
            final Integer tenantIdentifier = httpContext.getTenantIdentifier();

            if (!context.isFullAccess() && tenantIdentifier != null && !contextTenants.contains(tenantIdentifier)) {
                LOGGER.warn("[InvalidAuthenticationException] This tenant: {} is not allowed for the application context: {}. credential={}", tenantIdentifier,
                        httpContext.getIdentity(), certificate);
                throw new BadCredentialsException(
                        "This tenant: " + tenantIdentifier + " is not allowed for the application context: " + httpContext.getIdentity());
            }
            return context;
        }
        catch (final NotFoundException e) {
            LOGGER.error("Certificate not found [IssuerDN={}, certificateBase64={}, credential={}]", certificate.getIssuerDN(), certificateBase64, certificate);
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

        return contextRoles.stream().filter(userRoles::contains).collect(Collectors.toList());
    }

    protected List<String> getUserRoles(final AuthUserDto userProfile, final int tenantIdentifier) {
        final List<String> userRoles = new ArrayList<>();
        userProfile.getProfileGroup().getProfiles().stream().filter(profile -> profile.getTenantIdentifier().intValue() == tenantIdentifier)
                .filter(ProfileDto::isEnabled).forEach(profile -> {
                    final List<String> rolesNames = profile.getRoles().stream().map(role -> ApiUtils.ensureHasRolePrefix(role.getName()))
                            .collect(Collectors.toList());
                    userRoles.addAll(rolesNames);
                });

        LOGGER.debug("user roles: {}", userRoles);
        return userRoles;
    }

    protected InternalHttpContext getInternalHttpContextForUserNotAuthenticated(final ExternalHttpContext externalHttpContext) {
        return InternalHttpContext.buildFromExternalHttpContext(externalHttpContext, null, null);
    }
}
