package fr.gouv.vitamui.iam.server.user.service;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.server.application.service.ApplicationService;
import fr.gouv.vitamui.iam.server.common.ApiIamConstants;
import fr.gouv.vitamui.iam.server.common.domain.Address;
import fr.gouv.vitamui.iam.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.server.common.service.AddressService;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.security.IamAuthentificationService;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.server.token.domain.Token;
import fr.gouv.vitamui.iam.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.apache.commons.lang.time.DateUtils;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public final class UserServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private static final String TOKEN_VALUE = "TOK1234567890";
    private static final String CAS_SECRET_TOKEN = "some_cas_secret_token";

    private static final String USER_ID = "userId";
    private static final String CAS_USER_ID = "casuser";

    private static final String CUSTOMER_ID = "customerId";
    private static final String CAS_CUSTOMER_ID = "system_customer";

    private static final String LEVEL = "DEV";

    private static final String GROUP_ID = "groupId";
    private static final String CAS_GROUP_ID = "casGroupId";

    private static int ttlInMinutes = 30;

    private UserService userService;

    private CustomerRepository customerRepository;

    private IamAuthentificationService iamAuthentificationService;

    private GroupService groupService;

    private HttpContext httpContext;

    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private IamLogbookService iamLogbookService;

    @Autowired
    private EventRepository eventRepository;

    @MockBean
    private SequenceGeneratorService sequenceGeneratorService;

    @MockBean
    private TenantRepository tenantRepository;

    @MockBean
    private GroupRepository groupRepository;

    @Autowired
    private UserConverter userConverter;

    @MockBean
    private ApplicationService applicationService;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, SecurityException, Exception {
        groupService = mock(GroupService.class);
        ProfileService profileService = mock(ProfileService.class);
        httpContext = mock(HttpContext.class);
        customerRepository = mock(CustomerRepository.class);
        ProfileRepository profilRepository = mock(ProfileRepository.class);
        SubrogationRepository subrogationRepository = mock(SubrogationRepository.class);
        addressService = mock(AddressService.class);
        UserExportService userExportService = mock(UserExportService.class);
        UserInfoService userInfoInternalService = mock(UserInfoService.class);
        ConnectionHistoryService connectionHistoryService = mock(ConnectionHistoryService.class);
        userService = new UserService(
            sequenceGeneratorService,
            userRepository,
            groupService,
            profileService,
            mock(UserEmailService.class),
            tenantRepository,
            securityService,
            customerRepository,
            iamLogbookService,
            userConverter,
            null,
            null,
            addressService,
            applicationService,
            null,
            userExportService,
            userInfoInternalService,
            connectionHistoryService
        );
        iamAuthentificationService = new IamAuthentificationService(
            userService,
            tokenRepository,
            subrogationRepository
        );
        iamAuthentificationService.setTokenMaxTtl(30);
        iamAuthentificationService.setTokenAdditionalTtl(15);
        iamAuthentificationService.setCasSecretToken(CAS_SECRET_TOKEN);

        tokenRepository.deleteAll();
        userRepository.deleteAll();
        eventRepository.deleteAll();

        final Tenant tenant = new Tenant();
        tenant.setIdentifier(10);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(tenant));
        when(securityService.hasRole(eq(ServicesData.ROLE_GET_USERS_ALL_CUSTOMERS))).thenReturn(true);
        when(securityService.hasRole(eq(ServicesData.ROLE_GENERIC_USERS))).thenReturn(true);
        when(securityService.userIsRootLevel()).thenReturn(true);

        // retrieve sequences
        userService.getNextSequenceId(SequencesConstants.USER_IDENTIFIER);
    }

    @Test
    public void testGetUserProfileByTokenNoTokenInDatabase() {
        when(httpContext.getUserToken()).thenReturn(TOKEN_VALUE);
        Assertions.assertThrows(
            BadCredentialsException.class,
            () -> iamAuthentificationService.getUserFromHttpContext(httpContext)
        );
    }

    @Test
    public void testGetUserProfileByToken() {
        final Token token = new Token();
        token.setId(TOKEN_VALUE);

        Date currentDate = Calendar.getInstance().getTime();
        final Date nowPlusXMinutes = DateUtils.addMinutes(currentDate, ttlInMinutes);
        token.setCreatedDate(currentDate);
        token.setUpdatedDate(nowPlusXMinutes);
        token.setRefId(USER_ID);
        tokenRepository.save(token);

        final User user = IamServerUtilsTest.buildUser(USER_ID, "test@vitamui.com", GROUP_ID, CUSTOMER_ID, LEVEL);

        userRepository.save(user);

        when(groupService.getOne(ArgumentMatchers.anyString(), any(), ArgumentMatchers.any())).thenReturn(
            new GroupDto()
        );
        when(groupService.getMany(any(String.class))).thenReturn(Arrays.asList(new GroupDto()));
        Mockito.when(securityService.userIsRootLevel()).thenReturn(true);

        final Customer customer = IamServerUtilsTest.buildCustomer();
        customer.setId(CUSTOMER_ID);
        final Tenant tenant = new Tenant();
        tenant.setId("id");
        tenant.setIdentifier(10);
        tenant.setEnabled(true);
        tenant.setProof(true);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(tenantRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Arrays.asList(tenant));

        when(securityService.getLevel()).thenReturn(LEVEL);
        when(groupService.getMany(GROUP_ID)).thenReturn(Arrays.asList(buildGroupDto()));
        when(groupService.getOneByPassSecurity(GROUP_ID, Optional.empty())).thenReturn(buildGroupDto());
        when(httpContext.getUserToken()).thenReturn(TOKEN_VALUE);
        final UserDto userProfile = iamAuthentificationService.getUserFromHttpContext(httpContext);

        assertEquals(USER_ID, userProfile.getId());
    }

    @Test
    public void testGetUserProfileByTokenForCasSecretToken() {
        final User user = IamServerUtilsTest.buildUser(
            CAS_USER_ID,
            "cas@vitamui.com",
            CAS_GROUP_ID,
            CAS_CUSTOMER_ID,
            ""
        );
        userRepository.save(user);

        when(groupService.getOne(ArgumentMatchers.anyString(), any(), ArgumentMatchers.any())).thenReturn(
            new GroupDto()
        );
        when(groupService.getMany(any(String.class))).thenReturn(Arrays.asList(new GroupDto()));
        Mockito.when(securityService.userIsRootLevel()).thenReturn(true);

        final Customer customer = IamServerUtilsTest.buildCustomer();
        customer.setId(CAS_CUSTOMER_ID);
        final Tenant tenant = new Tenant();
        tenant.setId("id");
        tenant.setIdentifier(1);
        tenant.setEnabled(true);
        tenant.setProof(true);
        when(customerRepository.findById(CAS_CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(tenantRepository.findByCustomerId(CAS_CUSTOMER_ID)).thenReturn(List.of(tenant));

        when(securityService.getLevel()).thenReturn("");
        when(groupService.getMany(CAS_GROUP_ID)).thenReturn(List.of(buildGroupDto()));
        when(groupService.getOneByPassSecurity(CAS_GROUP_ID, Optional.empty())).thenReturn(buildGroupDto());
        when(httpContext.getUserToken()).thenReturn(CAS_SECRET_TOKEN);
        final UserDto userProfile = iamAuthentificationService.getUserFromHttpContext(httpContext);

        assertThat(userProfile.getId()).isEqualTo(CAS_USER_ID);
    }

    @Test
    public void testGetUserProfileByExpiredTokenShouldThrowUnauthorizedException() {
        final Token token = new Token();
        token.setId(TOKEN_VALUE);

        Date currentDate = Calendar.getInstance().getTime();
        final Date nowMinusXMinutes = DateUtils.addMinutes(currentDate, (-1) * ttlInMinutes);
        token.setCreatedDate(currentDate);
        token.setUpdatedDate(nowMinusXMinutes);
        token.setRefId(USER_ID);
        tokenRepository.save(token);

        final User user = IamServerUtilsTest.buildUser(USER_ID, "test@vitamui.com", GROUP_ID, CUSTOMER_ID, LEVEL);

        userRepository.save(user);

        when(groupService.getOne(ArgumentMatchers.anyString(), any(), ArgumentMatchers.any())).thenReturn(
            new GroupDto()
        );
        when(groupService.getMany(any(String.class))).thenReturn(Arrays.asList(new GroupDto()));
        Mockito.when(securityService.userIsRootLevel()).thenReturn(true);

        final Customer customer = IamServerUtilsTest.buildCustomer();
        customer.setId(CUSTOMER_ID);
        final Tenant tenant = new Tenant();
        tenant.setId("id");
        tenant.setIdentifier(10);
        tenant.setEnabled(true);
        tenant.setProof(true);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(tenantRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Arrays.asList(tenant));

        when(securityService.getLevel()).thenReturn(LEVEL);
        when(groupService.getMany(GROUP_ID)).thenReturn(Arrays.asList(buildGroupDto()));
        when(groupService.getOneByPassSecurity(GROUP_ID, Optional.empty())).thenReturn(buildGroupDto());
        when(httpContext.getUserToken()).thenReturn(TOKEN_VALUE);

        Assertions.assertThrows(
            BadCredentialsException.class,
            () -> iamAuthentificationService.getUserFromHttpContext(httpContext)
        );
    }

    @Test
    public void testCreateUser() {
        final UserDto user = createUser();
        assertThat(user.getIdentifier()).isNotBlank();
        assertThat(user.getAddress()).isNotNull();

        final Optional<Event> ev = eventRepository.findOne(
            Query.query(Criteria.where("obId").is(user.getIdentifier()))
        );
        assertThat(ev).isPresent();
    }

    @Test
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
        Mockito.when(groupService.getOne(any(), any(), any())).thenReturn(group);
        Mockito.when(securityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(groupService.getOneByPassSecurity(any(), any())).thenReturn(buildGroupDto());
        Mockito.when(securityService.getHttpContext()).thenReturn(httpContext);

        final UserDto userAdminComDto = userService.create(userAdminCom);
        assertThat(userAdminComDto.getIdentifier()).isNotBlank();
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.create(userAdminFr));
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
        Mockito.when(groupService.getOne(any(), any(), any())).thenReturn(group);
        Mockito.when(securityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(groupService.getOneByPassSecurity(any(), any())).thenReturn(buildGroupDto());
        Mockito.when(securityService.getHttpContext()).thenReturn(httpContext);

        final UserDto userAdminFrDto = userService.create(userAdminFr);
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
        user.setSiteCode("001");
        user.setCenterCodes(List.of("002"));
        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(groupService.getOne(any(), any(), any())).thenReturn(group);
        Mockito.when(securityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(groupService.getOneByPassSecurity(any(), any())).thenReturn(buildGroupDto());
        Mockito.when(securityService.getHttpContext()).thenReturn(httpContext);

        user = userService.create(user);
        return user;
    }

    @Test
    public void testLogbookCreate() {
        final UserDto user = createUser();

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo(
            "{" +
            "\"Nom\":\"-\"," +
            "\"Prénom\":\"-\"," +
            "\"Email\":\"-\"," +
            "\"Numéro mobile\":\"-\"," +
            "\"Numéro fixe\":\"-\"," +
            "\"Type\":\"NOMINATIVE\"," +
            "\"Statut\":\"ENABLED\"," +
            "\"Subrogeable\":\"false\"," +
            "\"Code interne\":\"\"," +
            "\"Mise à jour automatique\":\"false\"," +
            "\"OTP\":\"true\"," +
            "\"Date de désactivation\":\"\"," +
            "\"Date de suppression\":\"\"," +
            "\"Code du site\":\"001\"," +
            "\"Code des centres\":\"[002]\"," +
            "\"Nom de la rue\":\"-\"," +
            "\"Code postal\":\"-\"," +
            "\"Ville\":\"-\"," +
            "\"Pays\":\"-\"" +
            "}"
        );
    }

    @Test
    public void testPatch() {
        final UserDto user = createUser();
        Mockito.when(securityService.getCustomerId()).thenReturn(user.getCustomerId());

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("customerId", user.getCustomerId());
        partialDto.put("id", user.getId());

        partialDto.put("email", "new-email@vitamui.com");
        userService.patch(partialDto);
        partialDto.remove("email");

        partialDto.put("firstname", "julien");
        userService.patch(partialDto);
        partialDto.remove("firstname");

        partialDto.put("lastname", "cornille");
        userService.patch(partialDto);
        partialDto.remove("lastname");

        partialDto.put("language", "EN");
        userService.patch(partialDto);
        partialDto.remove("language");

        partialDto.put("type", UserTypeEnum.NOMINATIVE.toString());
        userService.patch(partialDto);
        partialDto.remove("type");

        partialDto.put("mobile", "+33667452514");
        userService.patch(partialDto);
        partialDto.remove("mobile");

        partialDto.put("phone", "+33167452514");
        userService.patch(partialDto);
        partialDto.remove("phone");

        partialDto.put("groupId", "test");
        userService.patch(partialDto);
        partialDto.remove("groupId");
        partialDto.remove("level");

        partialDto.put("status", UserStatusEnum.ANONYM.toString());
        userService.patch(partialDto);
        partialDto.remove("status");

        partialDto.put("subrogeable", true);
        userService.patch(partialDto);
        partialDto.remove("subrogeable");

        partialDto.put("otp", true);
        userService.patch(partialDto);
        partialDto.remove("otp");

        partialDto.put("siteCode", "001");
        userService.patch(partialDto);
        partialDto.remove("siteCode");

        partialDto.put("centerCodes", List.of("002"));
        userService.patch(partialDto);
        partialDto.remove("centerCodes");
        partialDto.put("autoProvisioningEnabled", true);
        userService.patch(partialDto);
        partialDto.remove("autoProvisioningEnabled");

        final Collection<Event> events = eventRepository.findAll(
            Query.query(
                Criteria.where("obId").is(user.getIdentifier()).and("evType").is(EventType.EXT_VITAMUI_UPDATE_USER)
            )
        );
        assertThat(events).hasSize(14);
    }

    @Test
    public void testCheckExist() {
        userRepository.save(
            IamServerUtilsTest.buildUser("userDev", "user-dev@vitamui.com", GROUP_ID, CUSTOMER_ID, LEVEL)
        );
        userRepository.save(
            IamServerUtilsTest.buildUser(
                "userAdmin",
                "user-admin@vitamui.com",
                GROUP_ID,
                CUSTOMER_ID,
                ApiIamConstants.ADMIN_LEVEL
            )
        );
        userRepository.save(
            IamServerUtilsTest.buildUser(
                "userSubDev",
                "user-sub-dev@vitamui.com",
                GROUP_ID,
                "otherCustomerId",
                LEVEL + ".SUB"
            )
        );

        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto(USER_ID, "test@vitamui.com", CUSTOMER_ID);
        userDto.setLevel(LEVEL);
        userDto.setCustomerId(CUSTOMER_ID);

        final User user = new User();
        VitamUIUtils.copyProperties(userDto, user);
        userRepository.save(user);

        Mockito.when(securityService.getUser()).thenReturn(userDto);
        Mockito.when(securityService.getLevel()).thenReturn(LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("email", "test@vitamui.com", CriterionOperator.EQUALS);
        criteria.addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS);
        criteria.addCriterion("level", LEVEL, CriterionOperator.EQUALS);
        boolean exist = userService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-dev@vitamui.com", CriterionOperator.EQUALS);
        exist = userService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-admin@vitamui.com", CriterionOperator.EQUALS);
        criteria.addCriterion("level", ApiIamConstants.ADMIN_LEVEL, CriterionOperator.EQUALS);
        exist = userService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-unknown@vitamui.com", CriterionOperator.EQUALS);
        exist = userService.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-sub-dev@vitamui.com", CriterionOperator.EQUALS);
        exist = userService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    @Test
    public void testCheckExistAdminUser() {
        final AuthUserDto mainUserDto = IamDtoBuilder.buildAuthUserDto(USER_ID, "test@vitamui.com", CUSTOMER_ID);
        mainUserDto.setLevel(ApiIamConstants.ADMIN_LEVEL);

        final User userDev = IamServerUtilsTest.buildUser(
            "userDev",
            "user-dev@vitamui.com",
            GROUP_ID,
            CUSTOMER_ID,
            LEVEL
        );
        userRepository.save(userDev);
        final User userAdmin = IamServerUtilsTest.buildUser(
            "userAdmin",
            "user-admin@vitamui.com",
            "otherGroup",
            CUSTOMER_ID,
            ApiIamConstants.ADMIN_LEVEL
        );
        userRepository.save(userAdmin);

        Mockito.when(securityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(securityService.getUser()).thenReturn(mainUserDto);
        Mockito.when(securityService.getLevel()).thenReturn(ApiIamConstants.ADMIN_LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("email", "user-dev@vitamui.com", CriterionOperator.EQUALS);
        criteria.addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS);
        criteria.addCriterion("level", LEVEL, CriterionOperator.EQUALS);
        boolean exist = userService.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("email", "mailunknown@vitamui.com", CriterionOperator.EQUALS);
        exist = userService.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("email", "user-admin@vitamui.com", CriterionOperator.EQUALS);
        exist = userService.checkExist(criteria.toJson());
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
        Mockito.when(securityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        Mockito.when(securityService.getUser()).thenReturn(authUserDto);

        user.setStatus(UserStatusEnum.ENABLED);
        userService.update(user);

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_PASSWORD_REVOCATION);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{}");
    }

    @Test
    public void testLogbookUpdateWithSuperUser() {
        final UserDto user = createUser(UserStatusEnum.DISABLED);
        Mockito.when(securityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setSuperUserIdentifier("145");
        Mockito.when(securityService.getUser()).thenReturn(authUserDto);

        user.setStatus(UserStatusEnum.ENABLED);
        userService.update(user);

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_PASSWORD_REVOCATION);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{\"Super utilisateur\":\"145\"}");
    }

    @Test
    public void testLogbookPatch() {
        final UserDto user = createUser(UserStatusEnum.DISABLED);
        Mockito.when(securityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        Mockito.when(securityService.getUser()).thenReturn(authUserDto);

        final Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("customerId", user.getCustomerId());
        map.put("status", UserStatusEnum.ENABLED.toString());
        userService.patch(map);

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_PASSWORD_REVOCATION);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{}");
    }

    @Test
    public void testLogbookPatchAddress() {
        final UserDto user = createUser(UserStatusEnum.ENABLED);
        Mockito.when(securityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        Mockito.when(securityService.getUser()).thenReturn(authUserDto);

        final Address newAddress = new Address();
        newAddress.setCity("newCity");
        newAddress.setCountry("newCountry");
        newAddress.setStreet("newStreet");
        newAddress.setZipCode("newZipCode");

        final Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("customerId", user.getCustomerId());
        map.put("address", TestUtils.getMapFromObject(newAddress));

        Mockito.doCallRealMethod()
            .when(addressService)
            .processPatch(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.anyBoolean()
            );

        userService.patch(map);

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_UPDATE_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo(
            "{\"diff\":{" +
            "\"-Code postal\":\"-\",\"+Code postal\":\"-\"," +
            "\"-Pays\":\"-\",\"+Pays\":\"-\"," +
            "\"-Ville\":\"-\",\"+Ville\":\"-\"," +
            "\"-Nom de la rue\":\"-\",\"+Nom de la rue\":\"-\"" +
            "}}"
        );
    }

    @Test
    public void testLogbookPatchWithSuperUser() {
        final UserDto user = createUser(UserStatusEnum.DISABLED);
        Mockito.when(securityService.getCustomerId()).thenReturn(user.getCustomerId());
        final AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setSuperUserIdentifier("610");
        Mockito.when(securityService.getUser()).thenReturn(authUserDto);

        final Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("customerId", user.getCustomerId());
        map.put("status", UserStatusEnum.ENABLED.toString());
        userService.patch(map);

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_PASSWORD_REVOCATION);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{\"Super utilisateur\":\"610\"}");
    }

    @Test
    public void testGetLevels() {
        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto(USER_ID, "test@vitamui.com", CUSTOMER_ID);
        userDto.setLevel(LEVEL);
        userDto.setCustomerId(CUSTOMER_ID);

        final User user = new User();
        VitamUIUtils.copyProperties(userDto, user);
        userRepository.save(user);
        final AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setId(user.getId());
        Mockito.when(securityService.getUser()).thenReturn(authUserDto);
        final Collection<String> levels = userService.getLevels(Optional.empty());
        assertThat(levels).hasSize(1);
        assertThat(levels.iterator().next()).isEqualTo(LEVEL);
    }

    @Test
    public void testGroupFields() {
        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto(USER_ID, "julien@vitamui.com", CUSTOMER_ID);
        userDto.setLevel(LEVEL);
        userDto.setCustomerId(CUSTOMER_ID);
        userDto.setIdentifier("1");

        final AuthUserDto user2Dto = IamDtoBuilder.buildAuthUserDto(USER_ID + "2", "pier08re@vitamui.com", CUSTOMER_ID);
        user2Dto.setLevel(LEVEL + ".2");
        user2Dto.setCustomerId(CUSTOMER_ID);
        userDto.setIdentifier("2");

        final User user1 = new User();
        VitamUIUtils.copyProperties(userDto, user1);
        userRepository.save(user1);

        final User user2 = new User();
        VitamUIUtils.copyProperties(user2Dto, user2);
        userRepository.save(user2);

        Mockito.when(securityService.getUser()).thenReturn(userDto);
        Mockito.when(securityService.getLevel()).thenReturn(LEVEL);

        userService.getAll(Optional.empty());
        final Document document = userService.groupFields(Optional.empty(), "email", "level");
        final List<String> levelList = document.get("level", List.class);
        assertThat(levelList).hasSize(2);
        assertThat(levelList).contains(user2Dto.getLevel(), userDto.getLevel());
        final List<String> emailList = document.get("email", List.class);
        assertThat(emailList).hasSize(2);
        assertThat(emailList).contains(user2Dto.getEmail(), userDto.getEmail());
    }
}
