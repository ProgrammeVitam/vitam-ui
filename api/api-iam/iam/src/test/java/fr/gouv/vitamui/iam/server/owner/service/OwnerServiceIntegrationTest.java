package fr.gouv.vitamui.iam.server.owner.service;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.HistoryEventDto;
import fr.gouv.vitamui.iam.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.server.common.service.AddressService;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Tests the {@link OwnerService}.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class OwnerServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private AutoCloseable mocks;

    private OwnerService ownerService;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private OwnerConverter ownerConverter;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private HttpContext httpContext;

    @MockitoBean
    private SequenceGeneratorService sequenceGeneratorService;

    @MockitoBean
    private TenantRepository tenantRepository;

    @MockitoBean
    private LogbookService logbookService;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        ownerService = new OwnerService(
            sequenceGeneratorService,
            ownerRepository,
            customerRepository,
            new AddressService(),
            iamLogbookService,
            securityService,
            ownerConverter,
            logbookService,
            tenantRepository
        );

        final Tenant tenant = new Tenant();
        tenant.setIdentifier(10);

        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(tenant));
        Mockito.when(tenantRepository.findByIdentifier(any())).thenReturn(tenant);
        eventRepository.deleteAll();
    }

    @Test
    public void testCreateOwner() {
        final OwnerDto owner = createOwner();
        assertThat(owner.getCode()).isNotBlank();

        final Criteria criteria = Criteria.where("obId")
            .is(owner.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.OWNERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_OWNER);
        final Optional<Event> ev = eventRepository.findOne(Query.query(criteria));
        assertThat(ev).isPresent();
    }

    private OwnerDto createOwner() {
        OwnerDto owner = buildOwnerDto();
        owner.setId(null);
        final String customerId = "customerId";
        final Customer customer = new Customer();
        customer.setId(customerId);
        customer.setEnabled(true);
        customer.setPasswordRevocationDelay(20);
        owner.setCustomerId(customerId);

        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(securityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(securityService.getHttpContext()).thenReturn(httpContext);
        Mockito.when(securityService.getApplicationId()).thenReturn("appId");

        owner = ownerService.create(owner);
        return owner;
    }

    @Test
    public void testPatch() {
        final OwnerDto owner = createOwner();

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("customerId", owner.getCustomerId());
        partialDto.put("id", owner.getId());

        partialDto.put("name", "nameTest");
        ownerService.patch(partialDto);
        partialDto.remove("name");

        partialDto.put("companyName", "companyNameTest");
        ownerService.patch(partialDto);
        partialDto.remove("companyName");

        partialDto.put("address", ImmutableMap.of("street", "streetTest"));
        ownerService.patch(partialDto);

        partialDto.put("address", ImmutableMap.of("city", "cityTest"));
        ownerService.patch(partialDto);
        partialDto.remove("address");

        partialDto.put("address", ImmutableMap.of("zipCode", "zipCodeTest"));
        ownerService.patch(partialDto);

        partialDto.put("address", ImmutableMap.of("zipCode", "zipCodeTest"));
        ownerService.patch(partialDto);
        partialDto.remove("address");

        partialDto.put("address", ImmutableMap.of("country", "countryTest"));
        ownerService.patch(partialDto);
        partialDto.remove("address");

        partialDto.put("code", "codeTest");
        ownerService.patch(partialDto);
        partialDto.remove("code");

        final Criteria criteria = Criteria.where("obId")
            .is(owner.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.OWNERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_UPDATE_OWNER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(8);
    }

    @Test
    public void testFindOwnerHistory() throws VitamClientException {
        final OwnerDto ownerCreated = createOwner();
        final Owner owner = new Owner();
        VitamUIUtils.copyProperties(ownerCreated, owner);
        assertThat(ownerCreated.getCode()).isNotBlank();

        final TenantDto tenant = new TenantDto();
        tenant.setOwnerId(owner.getId());
        tenant.setProof(true);
        tenant.setIdentifier(125);
        tenant.setAccessContractLogbookIdentifier("AC-000002");

        Mockito.when(securityService.getTenantIdentifier()).thenReturn(tenant.getIdentifier());
        Mockito.when(securityService.getTenant(eq(tenant.getIdentifier()))).thenReturn(tenant);
        Mockito.when(
            logbookService.findEventsByIdentifierAndCollectionNames(anyString(), anyString(), any(), anyList())
        ).thenReturn(List.of());

        final List<HistoryEventDto> historyResult = ownerService.findHistoryById(ownerCreated.getId());

        assertThat(historyResult).isNotNull();
        Mockito.verify(securityService).getTenantIdentifier();
        Mockito.verify(securityService).getTenant(tenant.getIdentifier());
    }

    private OwnerDto buildOwnerDto() {
        return IamServerUtilsTest.buildOwnerDto();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
