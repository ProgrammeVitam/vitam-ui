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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.authentication.InternalAuthentication;

/**
 *
 * Internal Security Service
 *
 *
 */
public class InternalSecurityService {

    public boolean isLevelAllowed(final String childLevel) {
        if (childLevel == null) {
            throw new IllegalArgumentException("childLevel is not defined");
        }
        final String parentLevel = getLevel();
        return (parentLevel.isEmpty() || childLevel.startsWith(parentLevel + "."));
    }

    public boolean userIsRootLevel() {
        return StringUtils.equals(getLevel(), StringUtils.EMPTY);
    }

    private InternalAuthentication getAuthentication() {
        final InternalAuthentication authentication = (InternalAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnAuthorizedException("Unable to get the security context. You probably are not authenticated.");
        }
        return authentication;
    }

    public String getLevel() {
        final String level = getAuthentication().getCredentials().getUserLevel();
        if (level == null) {
            throw new ApplicationServerException("Level not found in security context");
        }
        return level;
    }

    public Integer getProofTenantIdentifier() {
        final Integer tenantIdentifier = getAuthentication().getPrincipal().getProofTenantIdentifier();
        if (tenantIdentifier == null) {
            throw new ApplicationServerException("Proof tenant identifier not found in security context");
        }
        return tenantIdentifier;
    }

    public String getCustomerId() {
        final String customerId = getAuthentication().getCredentials().getCustomerId();
        if (customerId == null) {
            throw new ApplicationServerException("Customer not found in security context");
        }
        return customerId;
    }

    public Integer getTenantIdentifier() {
        final Integer tenantIdentifier = getAuthentication().getCredentials().getTenantIdentifier();
        if (tenantIdentifier == null) {
            throw new ApplicationServerException("Tenant not found in security context");
        }
        return tenantIdentifier;
    }

    public String getApplicationId() {
        final String applicationId = getAuthentication().getCredentials().getApplicationId();
        if (applicationId == null) {
            throw new ApplicationServerException("Application ID not found in security context");
        }
        return applicationId;
    }

    public String getRequestId() {
        final String requestId = getAuthentication().getCredentials().getRequestId();
        if (requestId == null) {
            throw new ApplicationServerException("Request ID not found in security context");
        }
        return requestId;
    }

    public String getUserToken() {
        final String userToken = getAuthentication().getCredentials().getUserToken();
        if (userToken == null) {
            throw new ApplicationServerException("Token not found in security context");
        }
        return userToken;
    }

    public AuthUserDto getUser() {
        final AuthUserDto user = getAuthentication().getPrincipal();
        if (user == null) {
            throw new ApplicationServerException("User not found in security context");
        }
        return user;
    }

    public InternalHttpContext getHttpContext() {
        final InternalHttpContext httpContext = getAuthentication().getCredentials();
        if (httpContext == null) {
            throw new ApplicationServerException("HttpContext not found in security context");
        }
        return httpContext;
    }

    public static List<Role> getRoles(final AuthUserDto user) {
        if (user.getProfileGroup() == null) {
            return Collections.emptyList();
        }

        final List<ProfileDto> profiles = user.getProfileGroup().getProfiles();
        return profiles == null || profiles.size() == 0
                ? Collections.emptyList()
                : profiles.stream().filter(ProfileDto::isEnabled).flatMap(p -> p.getRoles().stream()).collect(Collectors.toList());
    }

    public static List<Role> getRoles(final AuthUserDto user, final Integer tenantIdentifier) {
        if (user.getProfileGroup() == null) {
            return Collections.emptyList();
        }

        final List<ProfileDto> profiles = user.getProfileGroup().getProfiles();
        return profiles == null || profiles.size() == 0
                ? Collections.emptyList()
                : profiles.stream().filter(ProfileDto::isEnabled).filter(p -> tenantIdentifier.equals(p.getTenantIdentifier()))
                        .flatMap(p -> p.getRoles().stream()).collect(Collectors.toList());
    }

    public TenantDto getTenant(final Integer tenantIdentifier) {
        final Optional<TenantDto> tenant = getUser().getTenantsByApp().stream().flatMap(t -> t.getTenants().stream())
                .filter(t -> tenantIdentifier.equals(t.getIdentifier())).findAny();
        return tenant.orElseThrow(() -> new ApplicationServerException("Tenant not found"));
    }

    public TenantDto getCurrentTenantDto() {
        return getTenant(getTenantIdentifier());
    }

    /**
     * Build Vitam Context using tenantIdentifier.
     * @param tenantId
     * @return
     */
    public VitamContext buildVitamContext(final Integer tenantId) {
        return new VitamContext(tenantId).setApplicationSessionId(getApplicationId());
    }

    /**
     * Build Vitam Context using tenantIdentifier & accessContractIdentifier.
     * @param tenantId
     * @param accessContractId
     * @return
     */
    public VitamContext buildVitamContext(final Integer tenantId, final String accessContractId) {
        return new VitamContext(tenantId).setAccessContract(accessContractId).setApplicationSessionId(getApplicationId());
    }
}
