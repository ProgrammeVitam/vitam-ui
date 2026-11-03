package fr.gouv.vitamui.iam.server.tenant.service;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.NoRightsException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@code TenantService.patch} / {@code update} check the customer of the stored tenant: a caller holding
 * {@code ROLE_UPDATE_TENANTS} on its own customer cannot modify a tenant of another customer without
 * {@code ROLE_UPDATE_TENANTS_ALL_CUSTOMERS}.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
class TenantServiceCustomerScopeTest extends AbstractLogbookIntegrationTest {

    private static final String CUSTOMER_A = "customerA";
    private static final String CUSTOMER_B = "customerB";
    private static final int TENANT_A_IDENTIFIER = 101;
    private static final int TENANT_B_IDENTIFIER = 202;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @Autowired
    private TenantConverter tenantConverter;

    private TenantService service;

    private Tenant otherTenant;

    @BeforeEach
    void setUp() {
        customerRepository.save(buildCustomer(CUSTOMER_A, "100001", "CODE-A"));
        customerRepository.save(buildCustomer(CUSTOMER_B, "200002", "CODE-B"));

        tenantRepository.save(IamServerUtilsTest.buildTenant("tenantA", "Tenant A", TENANT_A_IDENTIFIER, CUSTOMER_A));
        otherTenant = tenantRepository.save(
            IamServerUtilsTest.buildTenant("tenantB", "Tenant B", TENANT_B_IDENTIFIER, CUSTOMER_B)
        );

        service = new TenantService(
            new SequenceGeneratorService(sequenceRepository),
            tenantRepository,
            customerRepository,
            null,
            null,
            null,
            null,
            null,
            securityService,
            iamLogbookService,
            tenantConverter,
            null,
            null,
            null,
            null,
            null,
            null
        );

        // Caller: authenticated on customer A, holds ROLE_UPDATE_TENANTS but NOT the all-customers one.
        when(securityService.getCustomerId()).thenReturn(CUSTOMER_A);
        when(securityService.hasRole(eq(ServicesData.ROLE_UPDATE_TENANTS_ALL_CUSTOMERS))).thenReturn(false);
        when(securityService.getTenantIdentifier()).thenReturn(TENANT_A_IDENTIFIER);
        when(securityService.getProofTenantIdentifier()).thenReturn(TENANT_A_IDENTIFIER);
        when(securityService.getHttpContext()).thenReturn(
            new HttpContext(
                TENANT_A_IDENTIFIER,
                "token",
                false,
                "appId",
                "identity",
                "requestId",
                "accessContract",
                null
            )
        );
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        tenantRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void patch_of_another_customers_tenant_is_denied_and_leaves_it_unchanged() {
        final Map<String, Object> patch = new HashMap<>();
        patch.put("id", otherTenant.getId());
        patch.put("name", "Renamed by customer A");
        patch.put("enabled", false);
        patch.put("accessContractHoldingIdentifier", "AC-OTHER");

        assertThatThrownBy(() -> service.patch(patch)).isInstanceOf(NoRightsException.class);

        final Tenant stored = tenantRepository.findById(otherTenant.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo("Tenant B");
        assertThat(stored.getEnabled()).isTrue();
        assertThat(stored.getCustomerId()).isEqualTo(CUSTOMER_B);
    }

    private Customer buildCustomer(final String id, final String identifier, final String code) {
        final Customer customer = IamServerUtilsTest.buildCustomer();
        customer.setId(id);
        customer.setIdentifier(identifier);
        customer.setCode(code);
        return customer;
    }
}
