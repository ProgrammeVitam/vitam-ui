package fr.gouv.vitamui.iam.internal.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.internal.server.application.service.ApplicationInternalService;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.security.IamAuthentificationService;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.token.domain.Token;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

/**
 * Class.
 *
 */
@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = { UserRepository.class, CustomSequenceRepository.class,
        TokenRepository.class }, repositoryBaseClass = VitamUIRepositoryImpl.class)
public final class UserInternalServiceIntegTest extends AbstractLogbookIntegrationTest {

    private static final String TOKEN_VALUE = "TOK1234567890";

    private static final String USER_ID = "userId";

    private static final String CUSTOMER_ID = "customerId";

    private static final String LEVEL = "DEV";

    private static final String GROUP_ID = "groupId";

    private UserInternalService internalUserService;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private IamLogbookService iamLogbookService;

    @Autowired
    private EventRepository eventRepository;

    private CustomerRepository customerRepository;

    private IamAuthentificationService iamAuthentificationService;

    private GroupInternalService groupInternalService;

    private ProfileInternalService internalProfileService;

    @MockBean
    private TenantRepository tenantRepository;

    private InternalHttpContext internalHttpContext;

    private ProfileRepository profilRepository;

    private SubrogationRepository subrogationRepository;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private ProfileRepository profileRepository;

    @Autowired
    private UserConverter userConverter;

    @MockBean
    private OwnerRepository ownerRepository;

    @MockBean
    private SpMetadataGenerator spMetadataGenerator;

    private AddressService addressService;

    @Mock
    private VitamUILogger logger;

    @MockBean
    private ApplicationInternalService applicationInternalService;

    @Before
    public void setUp() throws NoSuchFieldException, SecurityException, Exception {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        groupInternalService = mock(GroupInternalService.class);
        internalProfileService = mock(ProfileInternalService.class);
        internalHttpContext = mock(InternalHttpContext.class);
        customerRepository = mock(CustomerRepository.class);
        profilRepository = mock(ProfileRepository.class);
        subrogationRepository = mock(SubrogationRepository.class);
        addressService = mock(AddressService.class);

        internalUserService = new UserInternalService(sequenceRepository, userRepository, groupInternalService, internalProfileService,
                mock(UserEmailInternalService.class), tenantRepository, internalSecurityService, customerRepository, profilRepository, groupRepository,
                iamLogbookService, userConverter, null, null, addressService, applicationInternalService);

        iamAuthentificationService = new IamAuthentificationService(internalUserService, tokenRepository, subrogationRepository);
        iamAuthentificationService.setTokenAdditionalTtl(15);

        tokenRepository.deleteAll();
        userRepository.deleteAll();
        eventRepository.deleteAll();

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        final Tenant tenant = new Tenant();
        tenant.setIdentifier(10);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.ofNullable(tenant));

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setName(SequencesConstants.USER_IDENTIFIER);
        sequenceRepository.save(customSequence);

        internalUserService.getNextSequenceId(SequencesConstants.USER_IDENTIFIER);
    }

    @Test(expected = BadCredentialsException.class)
    public void testGetUserProfileByTokenNoTokenInDatabase() {
        when(internalHttpContext.getUserToken()).thenReturn(TOKEN_VALUE);
        iamAuthentificationService.getUserFromHttpContext(internalHttpContext);
    }

    @Test
    public void testGetUserProfileByToken() {
        final Token token = new Token();
        token.setId(TOKEN_VALUE);
        token.setUpdatedDate(Calendar.getInstance().getTime());
        token.setRefId(USER_ID);
        tokenRepository.save(token);

        final User user = IamServerUtilsTest.buildUser(USER_ID, "test@vitamui.com", GROUP_ID, CUSTOMER_ID, LEVEL);

        userRepository.save(user);

        when(groupInternalService.getOne(ArgumentMatchers.anyString(), any(), ArgumentMatchers.any())).thenReturn(new GroupDto());
        when(groupInternalService.getMany(any(String.class))).thenReturn(Arrays.asList(new GroupDto()));
        Mockito.when(internalSecurityService.userIsRootLevel()).thenReturn(true);

        final Customer customer = IamServerUtilsTest.buildCustomer();
        customer.setId(CUSTOMER_ID);
        final Tenant tenant = new Tenant();
        tenant.setId("id");
        tenant.setIdentifier(10);
        tenant.setEnabled(true);
        tenant.setProof(true);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(tenantRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Arrays.asList(tenant));

        when(internalSecurityService.getLevel()).thenReturn(LEVEL);
        when(groupInternalService.getMany(GROUP_ID)).thenReturn(Arrays.asList(buildGroupDto()));
        when(groupInternalService.getOneByPassSecurity(GROUP_ID, Optional.empty())).thenReturn(buildGroupDto());
        when(internalHttpContext.getUserToken()).thenReturn(TOKEN_VALUE);
        final UserDto userProfile = iamAuthentificationService.getUserFromHttpContext(internalHttpContext);

        assertEquals(USER_ID, userProfile.getId());
    }

    @Test
    public void testCreateUser() {
        final UserDto user = createUser();
        assertThat(user.getIdentifier()).isNotBlank();
        assertThat(user.getAddress()).isNotNull();

        final Optional<Event> ev = eventRepository.findOne(Query.query(Criteria.where("obId").is(user.getIdentifier())));
        assertThat(ev).isPresent();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAnotherUserAdmin() {
        final UserDto userAdminCom = IamServerUtilsTest.buildUserDto(null, "admin@vitamui.com", GROUP_ID, CUSTOMER_ID);
        final UserDto userAdminFr = IamServerUtilsTest.buildUserDto(null, "admin@vitamui.fr", GROUP_ID, CUSTOMER_ID);
        userAdminCom.setIdentifier(null);
        userAdminFr.setIdentifier(null);

        final Customer customer = new Customer();
        final String customerId = "customerId";
        customer.setId(customerId);
        customer.setEnabled(true);
        customer.setPasswordRevocationDelay(20);
        final GroupDto group = new GroupDto();
        group.setEnabled(true);
        group.setCustomerId(customerId);
        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(groupInternalService.getOne(any(), any(), any())).thenReturn(group);
        Mockito.when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(groupInternalService.getOneByPassSecurity(any(), any())).thenReturn(buildGroupDto());
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);

        final UserDto userAdminComDto = internalUserService.create(userAdminCom);
        assertThat(userAdminComDto.getIdentifier()).isNotBlank();
        internalUserService.create(userAdminFr);
    }

    @Test
    public void testCreateAGenericUser() {
        final UserDto userAdminFr = IamServerUtilsTest.buildUserDto(null, "support@vitamui.com", GROUP_ID, CUSTOMER_ID);
        userAdminFr.setIdentifier(null);
        userAdminFr.setMobile(null);
        userAdminFr.setType(UserTypeEnum.GENERIC);
        userAdminFr.setOtp(false);

        final Customer customer = new Customer();
        final String customerId = "customerId";
        customer.setId(customerId);
        customer.setEnabled(true);
        customer.setOtp(OtpEnum.MANDATORY);
        customer.setPasswordRevocationDelay(20);
        final GroupDto group = new GroupDto();
        group.setEnabled(true);
        group.setCustomerId(customerId);
        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(groupInternalService.getOne(any(), any(), any())).thenReturn(group);
        Mockito.when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(groupInternalService.getOneByPassSecurity(any(), any())).thenReturn(buildGroupDto());
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);

        final UserDto userAdminFrDto = internalUserService.create(userAdminFr);
        assertThat(userAdminFrDto.getIdentifier()).isNotBlank();
    }

    private UserDto createUser() {
        return createUser(null);
    }

    private UserDto createUser(final UserStatusEnum status) {
        UserDto user = IamServerUtilsTest.buildUserDto(null, "user-dev@vitamui.com", GROUP_ID, CUSTOMER_ID);
        if (status != null) {
            user.setStatus(status);
        }
        user.setIdentifier(null);
        user.setLevel("SUPPORT");
        final Customer customer = new Customer();
        final String customerId = "customerId";
        customer.setId(customerId);
        customer.setEnabled(true);
        customer.setPasswordRevocationDelay(20);
        final GroupDto group = new GroupDto();
        group.setEnabled(true);
        group.setCustomerId(customerId);
        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(groupInternalService.getOne(any(), any(), any())).thenReturn(group);
        Mockito.when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(groupInternalService.getOneByPassSecurity(any(), any())).thenReturn(buildGroupDto());
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);

        user = internalUserService.create(user);
        return user;
    }

    @Test
    public void testLogbookCreate() {
        final UserDto user = createUser();

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_CREATE_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{"
                + "\"Nom\":\"-\","
                + "\"Prénom\":\"-\","
                + "\"Email\":\"-\","
                + "\"Langue\":\"FRENCH\","
                + "\"Numéro mobile\":\"-\","
                + "\"Numéro fixe\":\"-\","
                + "\"Type\":\"NOMINATIVE\","
                + "\"Statut\":\"ENABLED\","
                + "\"Subrogeable\":\"false\","
                + "\"Code interne\":\"\","
                + "\"OTP\":\"true\","
                + "\"Date de désactivation\":\"\","
                + "\"Date de suppression\":\"\","
                + "\"Code du site\":\"\","
                + "\"Nom de la rue\":\"-\","
                + "\"Code postal\":\"-\","
                + "\"Ville\":\"-\","
                + "\"Pays\":\"-\""
                + "}");
       }

    @Test
    public void testPatch() {
        final UserDto user = createUser();
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(user.getCustomerId());

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("customerId", user.getCustomerId());
        partialDto.put("id", user.getId());

        partialDto.put("email", "new-email@vitamui.com");
        internalUserService.patch(partialDto);
        partialDto.remove("email");

        partialDto.put("firstname", "julien");
        internalUserService.patch(partialDto);
        partialDto.remove("firstname");

        partialDto.put("lastname", "cornille");
        internalUserService.patch(partialDto);
        partialDto.remove("lastname");

        partialDto.put("language", "EN");
        internalUserService.patch(partialDto);
        partialDto.remove("language");

        partialDto.put("type", UserTypeEnum.NOMINATIVE.toString());
        internalUserService.patch(partialDto);
        partialDto.remove("type");

        partialDto.put("mobile", "+33667452514");
        internalUserService.patch(partialDto);
        partialDto.remove("mobile");

        partialDto.put("phone", "+33167452514");
        internalUserService.patch(partialDto);
        partialDto.remove("phone");

        partialDto.put("groupId", "test");
        internalUserService.patch(partialDto);
        partialDto.remove("groupId");
        partialDto.remove("level");

        partialDto.put("status", UserStatusEnum.ANONYM.toString());
        internalUserService.patch(partialDto);
        partialDto.remove("status");

        partialDto.put("subrogeable", true);
        internalUserService.patch(partialDto);
        partialDto.remove("subrogeable");

        partialDto.put("otp", true);
        internalUserService.patch(partialDto);
        partialDto.remove("otp");

        partialDto.put("siteCode", "001");
        internalUserService.patch(partialDto);
        partialDto.remove("siteCode");

        final Collection<Event> events = eventRepository
                .findAll(Query.query(Criteria.where("obId").is(user.getIdentifier()).and("evType").is(EventType.EXT_VITAMUI_UPDATE_USER)));
        assertThat(events).hasSize(12);

    }

    @Test
    public void testCheckExist() {
        userRepository.save(IamServerUtilsTest.buildUser("userDev", "user-dev@vitamui.com", GROUP_ID, CUSTOMER_ID, LEVEL));
        userRepository.save(IamServerUtilsTest.buildUser("userAdmin", "user-admin@vitamui.com", GROUP_ID, CUSTOMER_ID, ApiIamInternalConstants.ADMIN_LEVEL));
        userRepository.save(IamServerUtilsTest.buildUser("userSubDev", "user-sub-dev@vitamui.com", GROUP_ID, "otherCustomerId", LEVEL + ".SUB"));

        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto(USER_ID, "test@vitamui.com");
        userDto.setLevel(LEVEL);
        userDto.setCustomerId(CUSTOMER_ID);

        final User user = new User();
        VitamUIUtils.copyProperties(userDto, user);
        userRepository.save(user);

        Mockito.when(internalSecurityService.getUser()).thenReturn(userDto);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("email", "test@vitamui.com", CriterionOperator.EQUALS);
        criteria.addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS);
        criteria.addCriterion("level", LEVEL, CriterionOperator.EQUALS);
        boolean exist = internalUserService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-dev@vitamui.com", CriterionOperator.EQUALS);
        exist = internalUserService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-admin@vitamui.com", CriterionOperator.EQUALS);
        criteria.addCriterion("level", ApiIamInternalConstants.ADMIN_LEVEL, CriterionOperator.EQUALS);
        exist = internalUserService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-unknown@vitamui.com", CriterionOperator.EQUALS);
        exist = internalUserService.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-sub-dev@vitamui.com", CriterionOperator.EQUALS);
        exist = internalUserService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    @Test
    public void testCheckExistAdminUser() {
        final AuthUserDto mainUserDto = IamDtoBuilder.buildAuthUserDto(USER_ID, "test@vitamui.com");
        mainUserDto.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);

        final User userDev = IamServerUtilsTest.buildUser("userDev", "user-dev@vitamui.com", GROUP_ID, CUSTOMER_ID, LEVEL);
        userRepository.save(userDev);
        final User userAdmin = IamServerUtilsTest.buildUser("userAdmin", "user-admin@vitamui.com", "otherGroup", CUSTOMER_ID,
                ApiIamInternalConstants.ADMIN_LEVEL);
        userRepository.save(userAdmin);

        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(internalSecurityService.getUser()).thenReturn(mainUserDto);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("email", "user-dev@vitamui.com", CriterionOperator.EQUALS);
        criteria.addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS);
        criteria.addCriterion("level", LEVEL, CriterionOperator.EQUALS);
        boolean exist = internalUserService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("email", "mailunknown@vitamui.com", CriterionOperator.EQUALS);
        exist = internalUserService.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-admin@vitamui.com", CriterionOperator.EQUALS);
        exist = internalUserService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    private GroupDto buildGroupDto() {
        final GroupDto dto = new GroupDto();
        dto.setName("Test Group");
        dto.setLevel(LEVEL);
        return dto;
    }

    @Test
    public void testLogbookUpdate() {
        final UserDto user = createUser(UserStatusEnum.DISABLED);
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        Mockito.when(internalSecurityService.getUser()).thenReturn(authUserDto);

        user.setStatus(UserStatusEnum.ENABLED);
        internalUserService.update(user);

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_PASSWORD_REVOCATION);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{}");
    }

    @Test
    public void testLogbookUpdateWithSuperUser() {
        final UserDto user = createUser(UserStatusEnum.DISABLED);
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setSuperUserIdentifier("145");
        Mockito.when(internalSecurityService.getUser()).thenReturn(authUserDto);

        user.setStatus(UserStatusEnum.ENABLED);
        internalUserService.update(user);

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_PASSWORD_REVOCATION);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{\"Super utilisateur\":\"145\"}");
    }

    @Test
    public void testLogbookPatch() {
        final UserDto user = createUser(UserStatusEnum.DISABLED);
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        Mockito.when(internalSecurityService.getUser()).thenReturn(authUserDto);

        final Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("customerId", user.getCustomerId());
        map.put("status", UserStatusEnum.ENABLED.toString());
        internalUserService.patch(map);

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_PASSWORD_REVOCATION);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{}");
    }

    @Test
    public void testLogbookPatchAddress() {
        final UserDto user = createUser(UserStatusEnum.ENABLED);
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        Mockito.when(internalSecurityService.getUser()).thenReturn(authUserDto);

        final Address newAddress = new Address();
        newAddress.setCity("newCity");
        newAddress.setCountry("newCountry");
        newAddress.setStreet("newStreet");
        newAddress.setZipCode("newZipCode");

        final Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("customerId", user.getCustomerId());
        map.put("address", TestUtils.getMapFromObject(newAddress));

        Mockito.doCallRealMethod().when(addressService).processPatch(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyBoolean());

        internalUserService.patch(map);

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_UPDATE_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo(
            "{\"diff\":{"
                + "\"-Code postal\":\"-\",\"+Code postal\":\"-\","
                + "\"-Pays\":\"-\",\"+Pays\":\"-\","
                + "\"-Ville\":\"-\",\"+Ville\":\"-\","
                + "\"-Nom de la rue\":\"-\",\"+Nom de la rue\":\"-\""
                + "}}");
    }

    @Test
    public void testLogbookPatchWithSuperUser() {
        final UserDto user = createUser(UserStatusEnum.DISABLED);
        Mockito.when(internalSecurityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setSuperUserIdentifier("610");
        Mockito.when(internalSecurityService.getUser()).thenReturn(authUserDto);

        final Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("customerId", user.getCustomerId());
        map.put("status", UserStatusEnum.ENABLED.toString());
        internalUserService.patch(map);

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_PASSWORD_REVOCATION);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{\"Super utilisateur\":\"610\"}");
    }

    @Test
    public void testGetLevels() {

        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto(USER_ID, "test@vitamui.com");
        userDto.setLevel(LEVEL);
        userDto.setCustomerId(CUSTOMER_ID);

        final User user = new User();
        VitamUIUtils.copyProperties(userDto, user);
        userRepository.save(user);
        final AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setId(user.getId());
        Mockito.when(internalSecurityService.getUser()).thenReturn(authUserDto);
        final Collection<String> levels = internalUserService.getLevels(Optional.empty());
        assertThat(levels).hasSize(1);
        assertThat(levels.iterator().next()).isEqualTo(LEVEL);
    }

    @Test
    public void testGroupFields() {

        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto(USER_ID, "julien@vitamui.com");
        userDto.setLevel(LEVEL);
        userDto.setCustomerId(CUSTOMER_ID);
        userDto.setIdentifier("1");

        final AuthUserDto user2Dto = IamDtoBuilder.buildAuthUserDto(USER_ID + "2", "pierre@vitamui.com");
        user2Dto.setLevel(LEVEL + ".2");
        user2Dto.setCustomerId(CUSTOMER_ID);
        userDto.setIdentifier("2");

        final User user1 = new User();
        VitamUIUtils.copyProperties(userDto, user1);
        userRepository.save(user1);

        final User user2 = new User();
        VitamUIUtils.copyProperties(user2Dto, user2);
        userRepository.save(user2);

        Mockito.when(internalSecurityService.getUser()).thenReturn(userDto);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(LEVEL);

        internalUserService.getAll(Optional.empty());
        final Document document = internalUserService.groupFields(Optional.empty(), "email", "level");
        final List<String> levelList = document.get("level", List.class);
        assertThat(levelList).hasSize(2);
        assertThat(levelList).contains(user2Dto.getLevel(), userDto.getLevel());
        final List<String> emailList = document.get("email", List.class);
        assertThat(emailList).hasSize(2);
        assertThat(emailList).contains(user2Dto.getEmail(), userDto.getEmail());
    }

}
