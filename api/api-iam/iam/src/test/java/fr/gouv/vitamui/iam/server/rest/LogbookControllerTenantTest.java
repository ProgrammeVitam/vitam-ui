package fr.gouv.vitamui.iam.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The optional {@code vitamTenantIdentifier} of the logbook operations search must be one of the tenants
 * available to the current user.
 */
class LogbookControllerTenantTest {

    private static final int HEADER_TENANT = 10;
    private static final int PROOF_TENANT = 1;
    private static final int USER_TENANT = 11;
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

        final TenantDto userTenant = new TenantDto();
        userTenant.setIdentifier(USER_TENANT);
        final TenantInformationDto tenantsOfApp = new TenantInformationDto();
        tenantsOfApp.setTenants(Set.of(userTenant));
        final AuthUserDto user = new AuthUserDto();
        user.setTenantsByApp(List.of(tenantsOfApp));

        when(securityService.getUser()).thenReturn(user);
        when(securityService.getProofTenantIdentifier()).thenReturn(PROOF_TENANT);
        when(securityService.buildVitamContext(any())).thenReturn(new VitamContext(HEADER_TENANT));
        when(tenantService.findByIdentifier(any())).thenReturn(new TenantDto());
        // Reaching the Vitam call is enough for these tests.
        when(logbookService.selectOperations(any(), any())).thenThrow(new IllegalStateException("search done"));
    }

    @Test
    void a_tenant_not_available_to_the_user_is_refused() throws Exception {
        assertThatThrownBy(() -> controller.findOperations(HEADER_TENANT, select, OTHER_TENANT)).isInstanceOf(
            ForbiddenException.class
        );
        verify(logbookService, never()).selectOperations(any(), any());
    }

    @Test
    void one_of_the_users_tenants_is_accepted() throws Exception {
        assertSearchUses(USER_TENANT);
    }

    @Test
    void the_proof_tenant_is_accepted() throws Exception {
        assertSearchUses(PROOF_TENANT);
    }

    @Test
    void the_header_tenant_is_accepted() throws Exception {
        assertSearchUses(HEADER_TENANT);
    }

    private void assertSearchUses(final int tenant) throws Exception {
        assertThatThrownBy(() -> controller.findOperations(HEADER_TENANT, select, tenant)).hasMessage("search done");
        verify(securityService).buildVitamContext(tenant);
    }
}
