/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.iam.security.filter;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class TenantHeaderFilter extends OncePerRequestFilter {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantHeaderFilter.class);

    public static final String ROLE_CROSS_TENANTS_SUFFIX = "_ALL_TENANTS";
    private final InternalSecurityService securityService;
    private final Integer casTenant;

    public TenantHeaderFilter(InternalSecurityService securityService, Integer casTenant) {
        this.securityService = securityService;
        this.casTenant = casTenant;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String tenantIdHeader = request.getHeader(CommonConstants.X_TENANT_ID_HEADER);
        //The control only when the header is set
        if (StringUtils.isNotBlank(tenantIdHeader)) {
            try {
                AuthUserDto user = securityService.getUser();
                Integer requestTenantId = Integer.parseInt(tenantIdHeader);
                Integer sessionTenantId = user.getProofTenantIdentifier();
                if (!Objects.equals(requestTenantId, sessionTenantId)) {
                    List<Role> userRoles = InternalSecurityService.getRoles(securityService.getUser());
                    boolean hasAnyCrossTenantRole = userRoles
                        .stream()
                        .anyMatch(r -> r.getName().endsWith(ROLE_CROSS_TENANTS_SUFFIX));
                    if (!hasAnyCrossTenantRole) {
                        if (!casTenant.equals(requestTenantId)) {
                            // Check if the user has access to this tenant
                            try {
                                securityService.getTenant(requestTenantId);
                            } catch (ApplicationServerException e) {
                                LOGGER.error("User not allowed on tenant {}", tenantIdHeader);
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized access to tenant");
                                return;
                            }
                        }
                    }
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid tenant ID format: {}", tenantIdHeader);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID format");
                return;
            } catch (UnAuthorizedException e1) {
                LOGGER.error("User not authenticated ");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized user");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
