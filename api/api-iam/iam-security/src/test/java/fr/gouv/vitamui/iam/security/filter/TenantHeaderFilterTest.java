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
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.utils.DtoFactory;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TenantHeaderFilterTest {

    private static final String PROFILE_ID = "PROFILE_ID";

    @Mock
    private InternalSecurityService securityService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TenantHeaderFilter tenantHeaderFilter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        tenantHeaderFilter = new TenantHeaderFilter(securityService, -1);
    }

    @Test
    void testNoTenantHeader() throws ServletException, IOException {
        when(request.getHeader(CommonConstants.X_TENANT_ID_HEADER)).thenReturn(null);

        tenantHeaderFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(securityService);
    }

    @Test
    void testInvalidTenantHeader() throws ServletException, IOException {
        when(request.getHeader(CommonConstants.X_TENANT_ID_HEADER)).thenReturn("invalid");

        tenantHeaderFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID format");
        verifyNoInteractions(filterChain);
    }

    @Test
    void testMatchingTenantId() throws ServletException, IOException {
        when(request.getHeader(CommonConstants.X_TENANT_ID_HEADER)).thenReturn("123");
        AuthUserDto user = mock(AuthUserDto.class);
        when(user.getProofTenantIdentifier()).thenReturn(123);
        when(securityService.getUser()).thenReturn(user);

        tenantHeaderFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testCrossTenantRoleAllowsAccess() throws ServletException, IOException {
        Integer tenantIdentifier = 123;
        when(request.getHeader(CommonConstants.X_TENANT_ID_HEADER)).thenReturn("124");
        GroupDto groupDto = new GroupDto();
        groupDto.setProfiles(
            List.of(
                buildProfileDto(
                    PROFILE_ID,
                    "PROFILE_NAME",
                    "CUSTOMER_ID",
                    tenantIdentifier,
                    "APP_NAME",
                    List.of(new Role("ROLE_1"), new Role("ROLE_2"), new Role("_OTHER_TENANTS"))
                )
            )
        );
        final AuthUserDto user = new AuthUserDto();
        user.setProfileGroup(groupDto);
        user.setProofTenantIdentifier(tenantIdentifier);
        when(securityService.getUser()).thenReturn(user);
        when(securityService.getTenant(anyInt())).thenCallRealMethod();
        tenantHeaderFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testUnauthorizedTenantAccess() throws ServletException, IOException {
        Integer tenantIdentifier = 123;
        when(request.getHeader(CommonConstants.X_TENANT_ID_HEADER)).thenReturn("124");
        GroupDto groupDto = new GroupDto();
        groupDto.setProfiles(
            List.of(
                buildProfileDto(
                    PROFILE_ID,
                    "PROFILE_NAME",
                    "CUSTOMER_ID",
                    tenantIdentifier,
                    "APP_NAME",
                    List.of(new Role("ROLE_1"), new Role("ROLE_2"))
                )
            )
        );
        final AuthUserDto user = new AuthUserDto();
        user.setProfileGroup(groupDto);
        user.setProofTenantIdentifier(tenantIdentifier);
        when(securityService.getUser()).thenReturn(user);
        when(securityService.getTenant(anyInt())).thenCallRealMethod();
        tenantHeaderFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized access to tenant");
        verifyNoMoreInteractions(filterChain);
    }

    public static ProfileDto buildProfileDto(
        final String id,
        final String name,
        final String customerId,
        final Integer tenantId,
        final String applicationName,
        final List<Role> roles
    ) {
        final ProfileDto profileDto = DtoFactory.buildProfileDto(
            name,
            "description",
            false,
            "level",
            tenantId,
            applicationName,
            new ArrayList<String>(),
            customerId
        );
        profileDto.setId(id);
        profileDto.setRoles(roles);
        return profileDto;
    }
}
