package fr.gouv.vitamui.iam.internal.server.owner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;

import fr.gouv.vitam.common.GlobalDataRest;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

/**
 * Tests the {@link OwnerInternalService}.
 *
 *
 */
@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = { OwnerRepository.class, CustomSequenceRepository.class,
        TokenRepository.class }, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class OwnerInternalServiceIntegTest extends AbstractLogbookIntegrationTest {

    private OwnerInternalService ownerService;

    @Autowired
    private OwnerRepository ownerRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @Mock
    private InternalHttpContext internalHttpContext;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private ProfileRepository profileRepository;

    @Autowired
    private OwnerConverter ownerConverter;

    @MockBean
    private SpMetadataGenerator spMetadataGenerator;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TenantRepository tenantRepository;

    @MockBean
    private LogbookService logbookService;

    public OwnerInternalServiceIntegTest() {
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ownerService = new OwnerInternalService(sequenceRepository, ownerRepository, customerRepository, new AddressService(), iamLogbookService,
                internalSecurityService, ownerConverter, logbookService, tenantRepository);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        final Tenant tenant = new Tenant();
        tenant.setIdentifier(10);

        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.ofNullable(tenant));
        Mockito.when(tenantRepository.findByIdentifier(any())).thenReturn(tenant);
        eventRepository.deleteAll();

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setName(SequencesConstants.OWNER_IDENTIFIER);
        sequenceRepository.save(customSequence);

        ownerService.getNextSequenceId(SequencesConstants.OWNER_IDENTIFIER);
    }

    @Test
    public void testCreateOwner() {
        final OwnerDto owner = createOwner();
        assertThat(owner.getCode()).isNotBlank();

        final Criteria criteria = Criteria.where("obId").is(owner.getIdentifier()).and("obIdReq").is(MongoDbCollections.OWNERS).and("evType")
                .is(EventType.EXT_VITAMUI_CREATE_OWNER);;
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
        Mockito.when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
        Mockito.when(internalSecurityService.getApplicationId()).thenReturn("appId");

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

        final Criteria criteria = Criteria.where("obId").is(owner.getIdentifier()).and("obIdReq").is(MongoDbCollections.OWNERS).and("evType")
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

        Mockito.when(internalSecurityService.getTenantIdentifier()).thenReturn(tenant.getIdentifier());
        Mockito.when(internalSecurityService.getTenant(eq(tenant.getIdentifier()))).thenReturn(tenant);
        final RequestResponse<LogbookOperation> operationsResponse = new RequestResponseOK<JsonNode>().addHeader(GlobalDataRest.X_REQUEST_ID, "requestId")
                .addHeader(GlobalDataRest.X_APPLICATION_ID, "appId").setHttpCode(Response.Status.OK.getStatusCode());
        Mockito.when(logbookService.findEventsByIdentifierAndCollectionNames(anyString(), anyString(), any())).thenReturn(operationsResponse);

        final JsonNode historyResult = ownerService.findHistoryById(ownerCreated.getId());

        assertThat(historyResult).isNotEmpty();
        Mockito.verify(internalSecurityService).getTenantIdentifier();
        Mockito.verify(internalSecurityService).getTenant(tenant.getIdentifier());

    }

    private OwnerDto buildOwnerDto() {
        return IamServerUtilsTest.buildOwnerDto();
    }
}
