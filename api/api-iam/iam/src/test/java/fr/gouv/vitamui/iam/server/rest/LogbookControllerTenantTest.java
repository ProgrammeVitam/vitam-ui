package fr.gouv.vitamui.iam.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The optional {@code vitamTenantIdentifier} of the logbook operations search must be the header tenant or a tenant
 * on which the current user holds ROLE_LOGBOOKS.
 */
class LogbookControllerTenantTest {

    private static final int HEADER_TENANT = 10;
    private static final int PROOF_TENANT = 1;
    private static final int LOGBOOKS_TENANT = 11;
    private static final int OTHER_ROLE_TENANT = 12;
    private static final int OTHER_TENANT = 99;

    private final LogbookService logbookService = mock(LogbookService.class);
    private final SecurityService securityService = mock(SecurityService.class);
    private final TenantService tenantService = mock(TenantService.class);

    private LogbookController controller;

    private final JsonNode select = JsonNodeFactory.instance.objectNode();

    @BeforeEach
    void setUp() throws Exception {
        controller = new LogbookController(
            logbookService,
            securityService,
            tenantService,
            mock(SignedDownloadTokenService.class)
        );

        final GroupDto group = new GroupDto();
        group.setProfiles(
            List.of(
                profile(LOGBOOKS_TENANT, ServicesData.ROLE_LOGBOOKS),
                profile(OTHER_ROLE_TENANT, ServicesData.ROLE_GET_USERS)
            )
        );
        final AuthUserDto user = new AuthUserDto();
        user.setProfileGroup(group);

        when(securityService.getUser()).thenReturn(user);
        when(securityService.getProofTenantIdentifier()).thenReturn(PROOF_TENANT);
        when(securityService.buildVitamContext(any())).thenReturn(new VitamContext(HEADER_TENANT));
        when(tenantService.findByIdentifier(any())).thenReturn(new TenantDto());
        // Reaching the Vitam call is enough for these tests.
        when(logbookService.selectOperations(any(), any())).thenThrow(new IllegalStateException("search done"));
    }

    private static ProfileDto profile(final int tenant, final String role) {
        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(tenant);
        profile.setEnabled(true);
        profile.setRoles(List.of(new Role(role)));
        return profile;
    }

    @Test
    void a_tenant_not_available_to_the_user_is_refused_before_the_tenant_lookup() throws Exception {
        assertRefused(OTHER_TENANT);
        verify(tenantService, never()).findByIdentifier(any());
    }

    @Test
    void a_tenant_without_the_logbooks_role_is_refused() throws Exception {
        assertRefused(OTHER_ROLE_TENANT);
    }

    @Test
    void the_proof_tenant_without_the_logbooks_role_is_refused() throws Exception {
        assertRefused(PROOF_TENANT);
    }

    @Test
    void a_tenant_with_the_logbooks_role_is_accepted() throws Exception {
        assertSearchUses(LOGBOOKS_TENANT);
    }

    @Test
    void the_header_tenant_is_accepted() throws Exception {
        assertSearchUses(HEADER_TENANT);
    }

    private void assertRefused(final int tenant) throws Exception {
        assertThatThrownBy(() -> controller.findOperations(HEADER_TENANT, select, tenant)).isInstanceOf(
            ForbiddenException.class
        );
        verify(logbookService, never()).selectOperations(any(), any());
    }

    private void assertSearchUses(final int tenant) throws Exception {
        assertThatThrownBy(() -> controller.findOperations(HEADER_TENANT, select, tenant)).hasMessage("search done");
        verify(securityService).buildVitamContext(tenant);
    }
}
