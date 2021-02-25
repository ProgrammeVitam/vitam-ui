package fr.gouv.vitamui.iam.internal.server.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.FieldUtils;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import junit.framework.Assert;

/**
 * Class for test InternalProfileService with a real repository
 *
 */

@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = { ProfileRepository.class }, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class ProfileInternalServiceIntegTest extends AbstractLogbookIntegrationTest {

    private ProfileInternalService service;

    @Autowired
    private ProfileRepository repository;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private UserRepository userRepository;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileInternalServiceIntegTest.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    @MockBean
    private TenantRepository tenantRepository;

    @Autowired
    private EventRepository eventRepository;

    private final InternalHttpContext internalHttpContext = mock(InternalHttpContext.class);

    private static final Integer TENANT_IDENTIFIER = 10;

    @Autowired
    private IamLogbookService iamLogbookService;

    @Autowired
    private ProfileConverter profileConverter;

    @MockBean
    private OwnerRepository ownerRepository;

    @MockBean
    private SpMetadataGenerator spMetadataGenerator;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        service = new ProfileInternalService(sequenceRepository, repository, customerRepository, groupRepository, tenantRepository, userRepository,
                internalSecurityService, iamLogbookService, profileConverter, null);
        repository.deleteAll();

        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.ofNullable(new Tenant()));

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setId(UUID.randomUUID().toString());
        customSequence.setSequence(1);
        Mockito.when(sequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(customSequence));

        FieldUtils.setFinalStatic(CustomerInitConfig.class.getDeclaredField("allRoles"), ServicesData.getAllRoles());
    }

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testCheckExist() {
        final String userLevel = "TEST";

        repository.save(IamServerUtilsTest.buildProfile("idTest", "1", "nametest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME, userLevel));
        repository.save(IamServerUtilsTest.buildProfile("idAdmin", "2", "nameadmin", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME,
                ApiIamInternalConstants.ADMIN_LEVEL));
        repository.save(IamServerUtilsTest.buildProfile("idSubTest", "3", "namesubtest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME,
                userLevel + ".SUB"));

        final Group group = IamServerUtilsTest.buildGroup();
        group.setLevel(userLevel);
        group.getProfileIds().add("idTest");

        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(userLevel);
        user.setGroupId(group.getId());

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(userLevel);
        Mockito.when(groupRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(group));

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("name", "nametest", CriterionOperator.EQUALS);
        criteria.addCriterion("tenantIdentifier", 10, CriterionOperator.EQUALS);
        criteria.addCriterion("level", userLevel, CriterionOperator.EQUALS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("name", "nametest", CriterionOperator.EQUALS);
        criteria.addCriterion("level", userLevel + ".SUB", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("name", "nameadmin", CriterionOperator.EQUALS);
        criteria.addCriterion("level", ApiIamInternalConstants.ADMIN_LEVEL, CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("name", "nameunknow", CriterionOperator.EQUALS);
        criteria.addCriterion("tenantIdentifier", 10, CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("name", "namesubtest", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        group.getProfileIds().clear();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idTest", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    @Test
    public void testCheckExistAdminUser() {
        repository.save(IamServerUtilsTest.buildProfile("profileIdTest", "1", "nametest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME, "TEST"));
        repository.save(IamServerUtilsTest.buildProfile("profileIdAdmin", "2", "nameadmin", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME,
                ApiIamInternalConstants.ADMIN_LEVEL));

        Mockito.when(internalSecurityService.getUser()).thenReturn(buildUserProfileDto("id", "customerIdTest", "groupTest"));
        Mockito.when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("name", "nametest", CriterionOperator.EQUALS);
        criteria.addCriterion("tenantIdentifier", 10, CriterionOperator.EQUALS);
        criteria.addCriterion("level", "TEST", CriterionOperator.EQUALS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("name", "nameunknow", CriterionOperator.EQUALS);
        criteria.addCriterion("tenantIdentifier", 10, CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("name", "nameadmin", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    @Test
    public void checkNameExistByTenantIdentifierAndLevel() {
        repository.save(IamServerUtilsTest.buildProfile("id", "identifier1", "nametest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME, "EDF.RH"));
        final Criteria criteria = Criteria.where("customerId").is("customerId").and("name").is("nametest").and("level").is("EDF.RH").and("tenantIdentifier")
                .is(10);

        assertThat(repository.exists(criteria)).isTrue();

    }

    @Test
    public void testGetSubLevels() {
        repository.save(IamServerUtilsTest.buildProfile("id", "identifier1", "nametest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME, "EDF.RH"));

        repository.save(IamServerUtilsTest.buildProfile("id25", "identifier2", "nametest25", "customerId", 11, CommonConstants.USERS_APPLICATIONS_NAME,
                "EDF.RH.PARIS"));

        repository.save(IamServerUtilsTest.buildProfile("id30", "identifier3", "nametest30", "customerId2", 10, CommonConstants.PROFILES_APPLICATIONS_NAME,
                "EDF.INFRA"));

        repository.save(IamServerUtilsTest.buildProfile("id31", "identifier4", "nametest31", "customerId2", 10, CommonConstants.PROFILES_APPLICATIONS_NAME,
                "EDF.INFRA.DSI"));

        repository.save(
                IamServerUtilsTest.buildProfile("id32", "identifier5", "nametest32", "customerId2", 10, CommonConstants.PROFILES_APPLICATIONS_NAME, "EDF"));

        List<String> levels = service.getSubLevels("EDF", "customerId");
        assertThat(levels).containsOnly("EDF.RH.PARIS", "EDF.RH");

        levels = service.getSubLevels("EDF", "customerId2");
        assertThat(levels).containsOnly("EDF.INFRA", "EDF.INFRA.DSI");

    }

    @Test
    public void testCreateProfile() {
        final ProfileDto profile = createProfile(Arrays.asList(new Role(ServicesData.ROLE_CREATE_USERS)));
        assertThat(profile.getIdentifier()).isNotBlank();

        final Criteria criteria = Criteria.where("obId").is(profile.getIdentifier()).and("obIdReq").is(MongoDbCollections.PROFILES).and("evType")
                .is(EventType.EXT_VITAMUI_CREATE_PROFILE);
        final Optional<Event> ev = eventRepository.findOne(Query.query(criteria));
        assertThat(ev).isPresent();
    }

    private ProfileDto createProfile(final List<Role> roles) {
        ProfileDto profile = IamServerUtilsTest.buildProfileDto();
        profile.setRoles(roles);
        profile.setId(null);
        final String customerId = "customerId";
        final Customer customer = new Customer();
        customer.setId(customerId);
        customer.setEnabled(true);
        customer.setPasswordRevocationDelay(20);
        profile.setCustomerId(customerId);
        final Tenant tenant = new Tenant();
        final Integer tenantIdentifier = TENANT_IDENTIFIER;
        profile.setTenantIdentifier(tenantIdentifier);
        tenant.setIdentifier(tenantIdentifier);
        tenant.setCustomerId(customerId);
        tenant.setEnabled(true);
        final AuthUserDto authUser = new AuthUserDto();
        final GroupDto group = new GroupDto();
        authUser.setProfileGroup(group);
        final ProfileDto authUserProfile = new ProfileDto();
        authUserProfile.setTenantIdentifier(tenantIdentifier);
        authUserProfile.setEnabled(true);
        authUserProfile.setRoles(roles);
        group.setProfiles(Arrays.asList(authUserProfile));

        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(tenantRepository.findByIdentifier(any())).thenReturn(tenant);
        Mockito.when(internalSecurityService.getTenantIdentifier()).thenReturn(tenantIdentifier);
        Mockito.when(internalSecurityService.getUser()).thenReturn(authUser);
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(customerId);
        Mockito.when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
        profile = service.create(profile);
        return profile;
    }

    @Test
    public void testPatch() {
        final ProfileDto profile = createProfile(Arrays.asList(new Role(ServicesData.ROLE_CREATE_USERS), new Role(ServicesData.ROLE_GET_USERS)));

        Mockito.when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("customerId", profile.getCustomerId());
        partialDto.put("id", profile.getId());
        partialDto.put("tenantIdentifier", TENANT_IDENTIFIER);

        partialDto.put("name", "nameTest");
        service.patch(partialDto);
        partialDto.remove("name");

        partialDto.put("description", "descriptionTest");
        service.patch(partialDto);
        partialDto.remove("description");

        partialDto.put("enabled", true);
        service.patch(partialDto);
        partialDto.remove("enabled");

        partialDto.put("roles", Arrays.asList(ImmutableMap.of("name", ServicesData.ROLE_GET_USERS)));
        service.patch(partialDto);
        partialDto.remove("roles");

        partialDto.put("level", "RH");
        service.patch(partialDto);
        partialDto.remove("level");

        final Criteria criteria = Criteria.where("obId").is(profile.getIdentifier()).and("obIdReq").is(MongoDbCollections.PROFILES).and("evType")
                .is(EventType.EXT_VITAMUI_UPDATE_PROFILE);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(5);
    }

    private AuthUserDto buildUserProfileDto(final String id, final String customerId, final String groupId) {
        final AuthUserDto u = new AuthUserDto();
        u.setGroupId(groupId);
        u.setId(id);
        u.setCustomerId(customerId);
        u.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);
        return u;
    }

    @Test
    public void testCreateProfilesWithDuplicateIdentifier() {
        final String identifier = "duplicateIdentifier";
        try {
            repository.save(IamServerUtilsTest.buildProfile("idSubTest1", identifier, "namesubtest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME,
                    "TEST"));
            repository.save(IamServerUtilsTest.buildProfile("idSubTest2", identifier, "namesubtest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME,
                    "SUPPORT"));
        }
        catch (final DuplicateKeyException e) {
            assertThat(e.getMessage()).contains(identifier);
            return;
        }
        Assert.fail("Excepted DuplicateKeyException to be thrown");
    }

    @Test
    public void testGetLevels() {
        final String profileLevel = "TEST";
        final String profileSupportLevel = "SUPPORT";
        repository.save(
                IamServerUtilsTest.buildProfile("idSubTest1", "3", "namesubtest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME, profileLevel));
        repository.save(IamServerUtilsTest.buildProfile("idSubTest2", "4", "namesubtest", "customerId", 10, CommonConstants.USERS_APPLICATIONS_NAME,
                profileSupportLevel));

        final Collection<String> levels = service.getLevels(Optional.empty());
        assertThat(levels).containsExactlyInAnyOrder(profileLevel, profileSupportLevel);
    }

}
