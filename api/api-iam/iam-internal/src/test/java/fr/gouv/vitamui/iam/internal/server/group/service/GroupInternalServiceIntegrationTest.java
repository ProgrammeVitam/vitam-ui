package fr.gouv.vitamui.iam.internal.server.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = { GroupRepository.class, CustomSequenceRepository.class }, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class GroupInternalServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private GroupInternalService service;

    @MockBean
    private ProfileRepository profileRepository;

    @Autowired
    private GroupRepository repository;

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private final ProfileInternalService internalProfileService = mock(ProfileInternalService.class);

    @MockBean
    private UserRepository userRepository;

    private static final String ID = "ID";

    private static final String CUSTOMER_ID = "CUSTOMER_ID";

    private static final String LEVEL = "LEVEL";

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @MockBean
    private OwnerRepository ownerRepository;

    @Autowired
    private GroupConverter groupConverter;

    @Mock
    private InternalHttpContext internalHttpContext;

    @MockBean
    private SpMetadataGenerator spMetadataGenerator;

    @MockBean
    private TenantRepository tenantRepository;

    @Before
    public void setup() {
        service = new GroupInternalService(sequenceRepository, repository, customerRepository, internalProfileService, userRepository, internalSecurityService,
                tenantRepository, iamLogbookService, groupConverter, null);

        repository.deleteAll();
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.ofNullable(new Tenant()));

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setName(SequencesConstants.GROUP_IDENTIFIER);
        sequenceRepository.save(customSequence);

        service.getNextSequenceId(SequencesConstants.GROUP_IDENTIFIER);
    }

    @Test
    public void testCheckExistByCustomerIdAndName() {
        final AuthUserDto mainUserDto = IamDtoBuilder.buildAuthUserDto("userId", "test@vitamui.com");
        mainUserDto.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);
        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(internalSecurityService.getUser()).thenReturn(mainUserDto);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);

        repository.save(IamServerUtilsTest.buildGroup(ID, "identifier", "nameknow", CUSTOMER_ID));

        QueryDto criteria = QueryDto.criteria().addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS).addCriterion("name", "nameknow",
                CriterionOperator.EQUALS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = QueryDto.criteria().addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS).addCriterion("name", "nameunknow",
                CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = QueryDto.criteria().addCriterion("name", "nameunknow", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();
    }

    @Test
    public void testGetSubLevels() {
        repository.save(IamServerUtilsTest.buildGroup(ID, "id1", "nametest", CUSTOMER_ID, "EDF.RH"));
        repository.save(IamServerUtilsTest.buildGroup("id25", "id2", "nametest25", CUSTOMER_ID, "EDF.RH.PARIS"));
        repository.save(IamServerUtilsTest.buildGroup("id30", "id3", "nametest30", CUSTOMER_ID, "EDF.INFRA"));
        repository.save(IamServerUtilsTest.buildGroup("id35", "id4", "nametest35", CUSTOMER_ID, "EDF.MARKET"));
        repository.save(IamServerUtilsTest.buildGroup("id36", "id5", "nametest36", CUSTOMER_ID, "EDF"));
        repository.save(IamServerUtilsTest.buildGroup("idothercustomerid", "id2", "nametest35", "other_customer_id", "EDF.VITAMUI"));

        List<String> levels = service.getSubLevels("EDF", CUSTOMER_ID);
        assertThat(levels).containsOnly("EDF.RH.PARIS", "EDF.RH", "EDF.MARKET", "EDF.INFRA");

        levels = service.getSubLevels("EDF", "other_customer_id");
        assertThat(levels).containsOnly("EDF.VITAMUI");
    }

    @Test
    public void testCheckExist() {
        repository.save(IamServerUtilsTest.buildGroup(ID, "id1", "name", CUSTOMER_ID, LEVEL));
        repository.save(IamServerUtilsTest.buildGroup("idLevel", "id2", "nameLevel", CUSTOMER_ID, LEVEL));
        repository.save(IamServerUtilsTest.buildGroup("idAdmin", "id3", "nameAdmin", CUSTOMER_ID, ApiIamInternalConstants.ADMIN_LEVEL));
        repository.save(IamServerUtilsTest.buildGroup("idSubLvl", "id4", "nameSubLvl", CUSTOMER_ID, LEVEL + ".SUB"));

        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto("userId", "test@vitamui.com");
        userDto.setLevel(LEVEL);
        userDto.setCustomerId(CUSTOMER_ID);
        userDto.setGroupId(ID);

        Mockito.when(internalSecurityService.getUser()).thenReturn(userDto);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("id", ID, CriterionOperator.EQUALS);
        criteria.addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS);
        criteria.addCriterion("level", LEVEL, CriterionOperator.EQUALS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idLevel", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idAdmin", CriterionOperator.EQUALS);
        criteria.addCriterion("level", ApiIamInternalConstants.ADMIN_LEVEL, CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idUnknown", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idSubLvl", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    @Test
    public void testCheckExistAdminUser() {
        final AuthUserDto mainUserDto = IamDtoBuilder.buildAuthUserDto("userId", "test@vitamui.com");
        mainUserDto.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);
        mainUserDto.setCustomerId(CUSTOMER_ID);

        repository.save(IamServerUtilsTest.buildGroup(ID, "id1", "name", CUSTOMER_ID, LEVEL));
        repository.save(IamServerUtilsTest.buildGroup("idAdmin", "id2", "nameAdmin", CUSTOMER_ID, ApiIamInternalConstants.ADMIN_LEVEL));

        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(internalSecurityService.getUser()).thenReturn(mainUserDto);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("id", ID, CriterionOperator.EQUALS);
        criteria.addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS);
        criteria.addCriterion("level", LEVEL, CriterionOperator.EQUALS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idunknown", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idAdmin", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    @Test
    public void testCreateGroup() {
        final GroupDto group = createGroup();
        assertThat(group.getIdentifier()).isNotBlank();

        final Criteria criteria = Criteria.where("obId").is(group.getIdentifier()).and("obIdReq").is(MongoDbCollections.GROUPS).and("evType")
                .is(EventType.EXT_VITAMUI_CREATE_GROUP);
        final Optional<Event> ev = eventRepository.findOne(Query.query(criteria));
        assertThat(ev).isPresent();
    }

    @Test
    public void testPatch() {
        final GroupDto group = createGroup();

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("customerId", group.getCustomerId());
        partialDto.put("id", group.getId());

        partialDto.put("name", "nameTest");
        service.patch(partialDto);
        partialDto.remove("name");

        partialDto.put("description", "descriptionTest");
        service.patch(partialDto);
        partialDto.remove("description");

        partialDto.put("profileIds", group.getProfileIds());
        service.patch(partialDto);
        partialDto.remove("profileIds");

        partialDto.put("enabled", false);
        service.patch(partialDto);
        partialDto.remove("enabled");

        final Criteria criteria = Criteria.where("obId").is(group.getIdentifier()).and("obIdReq").is(MongoDbCollections.GROUPS).and("evType")
                .is(EventType.EXT_VITAMUI_UPDATE_GROUP);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(4);
    }

    private GroupDto createGroup() {
        final String customerId = "customerId";
        final Integer tenantIdentifier = 10;
        final Tenant tenant = new Tenant();
        tenant.setIdentifier(tenantIdentifier);
        tenant.setCustomerId(customerId);
        tenant.setEnabled(true);
        final ProfileDto profile = IamServerUtilsTest.buildProfileDto();
        profile.setCustomerId(customerId);
        profile.setTenantIdentifier(tenantIdentifier);

        final GroupDto groupDto = IamServerUtilsTest.buildGroupDto(null, "nameTest", customerId, Arrays.asList(profile.getId()));
        groupDto.setIdentifier(null);
        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(customerId);

        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(customerId);
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
        Mockito.when(internalProfileService.getMany(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Arrays.asList(profile));
        Mockito.when(tenantRepository.findByIdentifier(ArgumentMatchers.any())).thenReturn(tenant);

        return service.create(groupDto);
    }
}
