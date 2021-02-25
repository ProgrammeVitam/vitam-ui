package fr.gouv.vitamui.iam.internal.server.idp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = { IdentityProviderRepository.class,
        CustomSequenceRepository.class }, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class IdentityProviderInternalIntegrationTest extends AbstractLogbookIntegrationTest {

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private GroupRepository groupRepository;

    @Autowired
    private IdentityProviderRepository repository;

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    @MockBean
    private TenantRepository tenantRepository;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @MockBean
    private OwnerRepository ownerRepository;

    @Mock
    private InternalHttpContext internalHttpContext;

    @MockBean
    private SpMetadataGenerator spMetadataGenerator;

    @Autowired
    private IamLogbookService iamLogbookService;

    private IdentityProviderConverter identityProviderConverter = new IdentityProviderConverter(spMetadataGenerator);

    private IdentityProviderInternalService service;

    @MockBean
    private UserRepository userRepository;

    @Before
    public void setup() {

        identityProviderConverter = new IdentityProviderConverter(spMetadataGenerator);
        service = new IdentityProviderInternalService(sequenceRepository, repository, spMetadataGenerator, customerRepository, iamLogbookService,
                identityProviderConverter);
        repository.deleteAll();
        final Tenant tenant = new Tenant();
        tenant.setIdentifier(10);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.ofNullable(tenant));

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setName(SequencesConstants.IDP_IDENTIFIER);
        sequenceRepository.save(customSequence);

        service.getNextSequenceId(SequencesConstants.IDP_IDENTIFIER);
    }

    @Test
    public void testCreateIdp() {
        final IdentityProviderDto idp = createIdp();
        assertThat(idp.getIdentifier()).isNotBlank();

        final Criteria criteria = Criteria.where("obId").is(idp.getIdentifier()).and("obIdReq").is(MongoDbCollections.PROVIDERS).and("evType")
                .is(EventType.EXT_VITAMUI_CREATE_IDP);
        final Optional<Event> ev = eventRepository.findOne(Query.query(criteria));
        assertThat(ev).isPresent();
    }

    @Test
    public void testPatch() {
        final IdentityProviderDto dto = createIdp();

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("customerId", dto.getCustomerId());
        partialDto.put("id", dto.getId());

        partialDto.put("name", "nameTest");
        service.patch(partialDto);
        partialDto.remove("name");

        partialDto.put("enabled", false);
        service.patch(partialDto);
        partialDto.remove("enabled");

        partialDto.put("internal", false);
        service.patch(partialDto);
        partialDto.remove("internal");

        partialDto.put("patterns", java.util.Arrays.asList(".*@vitamui.com"));
        service.patch(partialDto);
        partialDto.remove("patterns");

        partialDto.put("maximumAuthenticationLifetime", 1);
        service.patch(partialDto);
        partialDto.remove("maximumAuthenticationLifetime");

        final Criteria criteria = Criteria.where("obId").is(dto.getIdentifier()).and("obIdReq").is(MongoDbCollections.PROVIDERS).and("evType")
                .is(EventType.EXT_VITAMUI_UPDATE_IDP);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(5);
    }

    private IdentityProviderDto createIdp() {
        final String customerId = "customerId";
        final IdentityProviderDto dto = IamServerUtilsTest.buildIdentityProviderDto();
        dto.setCustomerId(customerId);
        dto.setId(null);

        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(customerId);

        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(customerId);
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);

        return service.create(dto);
    }
}
