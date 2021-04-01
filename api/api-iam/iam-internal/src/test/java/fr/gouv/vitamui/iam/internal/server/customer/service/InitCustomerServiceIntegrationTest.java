package fr.gouv.vitamui.iam.internal.server.customer.service;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"spring.config.name=iam-internal-application"})
@Import({TestMongoConfig.class})
@ActiveProfiles(value = "test")
public class InitCustomerServiceIntegrationTest {

    private static final String PROFILE_NAME_1 = "profile1";
    private static final String PROFILE_NAME_2 = "profile2";
    private static final String PROFILE_NAME_3 = "profile3";
    private static final String PROFILE_NAME_4 = "profile4";
    private static final String GROUP_NAME_1 = "group1";
    private static final String LEVEL_1 = "1";
    private static final String LEVEL_2 = "2";
    private static final String DESCRIPTION_1 = "desc1";
    private static final String DESCRIPTION_2 = "desc2";
    private static final String DESCRIPTION_3 = "desc3";
    private static final String DESCRIPTION_4 = "desc4";
    private static final String APP_NAME_1 = "app1";

    private static final String APP_NAME_2 = "app2";

    private static final String ROLE_1 = "role_1";

    private static final String ROLE_2 = "role_2";

    private static final String ROLE_3 = "role_3";

    private static final String LAST_NAME = "LASTNAME";

    private static final String FIRST_NAME = "FirstName";
    private static final String EMAIl = "a@vitamui.com";
    private static final String CUSTOMER_CODE = "0123456";

    private static final Integer TENANT_IDENTIFIER = 10;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @Autowired
    private CustomerInternalService customerInternalService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private AdminExternalClient adminExternalClient;

    @MockBean(name = "accessExternalClient")
    private AccessExternalClient accessExternalClient;

    @MockBean(name = "ingestExternalClient")
    private IngestExternalClient ingestExternalClient;

    @MockBean
    private InternalSecurityService internalSecurityService;

    @Mock
    private InternalHttpContext internalHttpContext;

    @MockBean
    private TenantRepository tenantRepository;

    @MockBean
    private MongoTransactionManager mongoTransactionManager;

    @MockBean
    private InitVitamTenantService initVitamTenantService;

    private static VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(InitCustomerServiceIntegrationTest.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        customerInternalService.getUserInternalService().setMongoTransactionManager(null);
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
        Mockito.when(internalSecurityService.userIsRootLevel()).thenReturn(true);
        Mockito.when(internalSecurityService.isLevelAllowed(ArgumentMatchers.any())).thenReturn(true);
        initSeq();
        final Tenant tenant = new Tenant();
        tenant.setIdentifier(10);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class)))
            .thenReturn(Optional.ofNullable(tenant));
        Mockito.when(tenantRepository.save(ArgumentMatchers.any())).thenReturn(tenant);
        Mockito.when(initVitamTenantService.init(ArgumentMatchers.any(Tenant.class), ArgumentMatchers.any(
            ExternalParametersDto.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        customerRepository.deleteAll();
    }

    private void initSeq() {
        final CustomSequence customSequence = new CustomSequence();
        customSequence.setName(SequencesConstants.CUSTOMER_IDENTIFIER);
        customSequence.setSequence(1);
        sequenceRepository.save(customSequence);

        final CustomSequence customSequence2 = new CustomSequence();
        customSequence2.setSequence(1);
        customSequence2.setName(SequencesConstants.IDP_IDENTIFIER);
        sequenceRepository.save(customSequence2);

        final CustomSequence customSequence3 = new CustomSequence();
        customSequence3.setName(SequencesConstants.GROUP_IDENTIFIER);
        sequenceRepository.save(customSequence3);

        final CustomSequence customSequence4 = new CustomSequence();
        customSequence4.setName(SequencesConstants.PROFILE_IDENTIFIER);
        sequenceRepository.save(customSequence4);

        final CustomSequence customSequence5 = new CustomSequence();
        customSequence5.setName(SequencesConstants.OWNER_IDENTIFIER);
        sequenceRepository.save(customSequence5);

        final CustomSequence customSequence6 = new CustomSequence();
        customSequence6.setName(SequencesConstants.USER_IDENTIFIER);
        sequenceRepository.save(customSequence6);

        final CustomSequence customSequence7 = new CustomSequence();
        customSequence7.setName(SequencesConstants.TENANT_IDENTIFIER);
        sequenceRepository.save(customSequence7);

        customerInternalService.getNextSequenceId(SequencesConstants.CUSTOMER_IDENTIFIER);
        customerInternalService.getNextSequenceId(SequencesConstants.IDP_IDENTIFIER);
        customerInternalService.getNextSequenceId(SequencesConstants.GROUP_IDENTIFIER);
        customerInternalService.getNextSequenceId(SequencesConstants.PROFILE_IDENTIFIER);
        customerInternalService.getNextSequenceId(SequencesConstants.OWNER_IDENTIFIER);
        customerInternalService.getNextSequenceId(SequencesConstants.USER_IDENTIFIER);
        customerInternalService.getNextSequenceId(SequencesConstants.TENANT_IDENTIFIER);
    }

    @Test
    public void testCreateCustomer() {
        final CustomerCreationFormData customerDta = new CustomerCreationFormData(buildCustomerDto());
        customerDta.setTenantName("tenantName");
        customerInternalService.create(customerDta);

        Criteria criteria = Criteria.where("obId").is(customerDta.getCustomerDto().getIdentifier()).and("obIdReq")
            .is(MongoDbCollections.CUSTOMERS)
            .and("evType").is(EventType.EXT_VITAMUI_CREATE_CUSTOMER);
        List<Event> ev = eventRepository.findAll(Query.query(criteria));
        assertThat(ev).isNotEmpty();
        assertThat(ev).hasSize(1);

        criteria = Criteria.where("obIdReq").is(MongoDbCollections.OWNERS).and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_OWNER);
        ev = eventRepository.findAll(Query.query(criteria));
        assertThat(ev).isNotEmpty();
        assertThat(ev).hasSize(1);

        criteria = Criteria.where("obIdReq").is(MongoDbCollections.TENANTS).and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_TENANT);
        ev = eventRepository.findAll(Query.query(criteria));
        assertThat(ev).isNotEmpty();
        assertThat(ev).hasSize(1);

        criteria = Criteria.where("obIdReq").is(MongoDbCollections.PROFILES).and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_PROFILE);
        ev = eventRepository.findAll(Query.query(criteria));
        assertThat(ev).isNotEmpty();
        assertThat(ev).hasSize(10);

        criteria = Criteria.where("obIdReq").is(MongoDbCollections.GROUPS).and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_GROUP);
        ev = eventRepository.findAll(Query.query(criteria));
        assertThat(ev).isNotEmpty();
        assertThat(ev).hasSize(2);

        criteria =
            Criteria.where("obIdReq").is(MongoDbCollections.USERS).and("evType").is(EventType.EXT_VITAMUI_CREATE_USER);
        ev = eventRepository.findAll(Query.query(criteria));
        assertThat(ev).isNotEmpty();
        assertThat(ev).hasSize(2);

        final Optional<Customer> customer = customerRepository.findByCode(CUSTOMER_CODE);
        assertThat(customer).isPresent();

        final Profile profile1 = profileRepository
            .findByNameAndLevelAndTenantIdentifier(PROFILE_NAME_1 + " " + TENANT_IDENTIFIER, LEVEL_1,
                TENANT_IDENTIFIER);
        assertThat(profile1).isNotNull();
        assertThat(profile1.getDescription()).isEqualTo(DESCRIPTION_1);
        assertThat(profile1.getLevel()).isEqualTo(LEVEL_1);
        assertThat(profile1.getApplicationName()).isEqualTo(APP_NAME_1);
        assertThat(profile1.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
            .contains(ROLE_1, ROLE_2, ROLE_3);

        final Profile profile2 = profileRepository
            .findByNameAndLevelAndTenantIdentifier(PROFILE_NAME_2 + " " + TENANT_IDENTIFIER, LEVEL_2,
                TENANT_IDENTIFIER);
        assertThat(profile2).isNotNull();
        assertThat(profile2.getDescription()).isEqualTo(DESCRIPTION_2);
        assertThat(profile2.getLevel()).isEqualTo(LEVEL_2);
        assertThat(profile2.getApplicationName()).isEqualTo(APP_NAME_2);
        assertThat(profile2.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
            .contains(ROLE_2, ROLE_3);

        final Profile profile3 = profileRepository
            .findByNameAndLevelAndTenantIdentifier(PROFILE_NAME_3 + " " + TENANT_IDENTIFIER, LEVEL_1,
                TENANT_IDENTIFIER);
        assertThat(profile3).isNotNull();
        assertThat(profile3.getDescription()).isEqualTo(DESCRIPTION_4);
        assertThat(profile3.getLevel()).isEqualTo(LEVEL_1);
        assertThat(profile3.getApplicationName()).isEqualTo(APP_NAME_2);
        assertThat(profile3.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
            .contains(ROLE_1, ROLE_2, ROLE_3);

        final List<Group> groups = groupRepository.findByCustomerId(customer.get().getId());
        assertThat(groups).isNotEmpty();
        final Map<String, Group> groupByName =
            groups.stream().collect(Collectors.toMap(Group::getName, Function.identity()));
        final Group group = groupByName.get(GROUP_NAME_1);
        assertThat(group).isNotNull();
        assertThat(group.getDescription()).isEqualTo(DESCRIPTION_3);
        assertThat(group.getLevel()).isEqualTo(LEVEL_1);
        assertThat(group.getProfileIds().contains(profile1.getId()));

        final Group adminGroup = groupByName.get(ApiIamInternalConstants.ADMIN_CLIENT_ROOT + " " + CUSTOMER_CODE);
        assertThat(adminGroup).isNotNull();
        final List<Profile> adminProfiles = profileRepository.findAllByIdIn(adminGroup.getProfileIds());
        final Map<String, Profile> profileByName =
            adminProfiles.stream().collect(Collectors.toMap(Profile::getName, Function.identity()));
        final Profile customProfileAdmin = profileByName.get(PROFILE_NAME_4 + " " + TENANT_IDENTIFIER);
        assertThat(customProfileAdmin).isNotNull();
        assertThat(customProfileAdmin.getApplicationName()).isEqualTo(APP_NAME_1);
        assertThat(customProfileAdmin.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
            .contains(ROLE_3);

        final User user = userRepository.findByEmail(EMAIl);
        assertThat(user).isNotNull();
        assertThat(user.getLastname()).isEqualTo(LAST_NAME);
        assertThat(user.getFirstname()).isEqualTo(FIRST_NAME);
        assertThat(user.getGroupId()).isEqualTo(group.getId());
        assertThat(user.getLevel()).isEqualTo(LEVEL_1);


    }

    protected CustomerDto buildCustomerDto() {
        final CustomerDto dto = new CustomerDto();
        dto.setEnabled(true);
        dto.setName("CustomerName");
        dto.setCode(CUSTOMER_CODE);
        dto.setOtp(OtpEnum.OPTIONAL);
        dto.setLanguage(LanguageDto.FRENCH);
        dto.setPasswordRevocationDelay(365 * 10);
        dto.setDefaultEmailDomain("vitamui.com");
        final List<String> domainsEmails = new ArrayList<>();
        domainsEmails.add("vitamui.com");
        dto.setEmailDomains(domainsEmails);
        final OwnerDto owner = new OwnerDto();
        owner.setName("The Boss");
        owner.setAddress(new AddressDto());
        final List<OwnerDto> owners = new ArrayList<>();
        owners.add(owner);
        dto.setOwners(owners);
        dto.setHasCustomGraphicIdentity(false);
        dto.setGdprAlertDelay(72);
        dto.setGdprAlert(false);
        return dto;
    }
}
