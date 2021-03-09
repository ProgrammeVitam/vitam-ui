package fr.gouv.vitamui.iam.internal.server.user.service;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.CommonConstants;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.AnalyticsDto;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.internal.server.application.service.ApplicationInternalService;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.token.domain.Token;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.ApplicationAnalytics;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Tests {@link UserInternalService}.
 *
 *
 */
public final class UserInternalServiceTest {

    private static final String TOKEN_VALUE = "TOK-1-F8lEhVif0FWjgDF32ov73TtKhE6mflRu";

    private static final String USER_ID = "userId";

    private static final String LEVEL = "DSI.DEV";

    private static final String GROUP_ID = "groupId";

    private UserInternalService internalUserService;

    private UserRepository userRepository;

    private TokenRepository tokenRepository;

    private GroupInternalService internalGroupService;

    private ProfileInternalService internalProfileService;

    private InternalSecurityService internalSecurityService;

    private UserEmailInternalService userEmailInternalService;

    private AddressService addressService;

    private CustomerRepository customerRepository;

    private GroupRepository groupRepository;

    private CustomSequenceRepository sequenceRepository;

    private ProfileRepository profileRepository;

    private TenantRepository tenantRepository;

    private AddressConverter addressConverter;

    private final UserConverter userConverter = new UserConverter(groupRepository, addressConverter);

    private ApplicationInternalService applicationInternalService;

    @Before
    public void setUp() {

        sequenceRepository = mock(CustomSequenceRepository.class);

        userRepository = mock(UserRepository.class);
        internalProfileService = mock(ProfileInternalService.class);
        internalSecurityService = mock(InternalSecurityService.class);
        customerRepository = mock(CustomerRepository.class);
        groupRepository = mock(GroupRepository.class);
        userEmailInternalService = mock(UserEmailInternalService.class);
        profileRepository = mock(ProfileRepository.class);
        tenantRepository = mock(TenantRepository.class);
        internalGroupService = mock(GroupInternalService.class);
        addressService = mock(AddressService.class);
        applicationInternalService = mock(ApplicationInternalService.class);

        internalUserService = new UserInternalService(sequenceRepository, userRepository, internalGroupService, internalProfileService,
                userEmailInternalService, tenantRepository, internalSecurityService, customerRepository, profileRepository, groupRepository,
                mock(IamLogbookService.class), userConverter, null, null, addressService, applicationInternalService);

        tokenRepository = mock(TokenRepository.class);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserByTokenNoTokenInDatabase() {
        Mockito.when(internalSecurityService.userIsRootLevel()).thenReturn(true);
        internalUserService.findUserById(TOKEN_VALUE);
    }

    @Test
    public void testGetUserByToken() {

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.userIsRootLevel()).thenReturn(true);
        when(internalGroupService.getMany(GROUP_ID)).thenReturn(Arrays.asList(buildGroupDto()));
        when(internalGroupService.getOne(GROUP_ID, Optional.empty(), Optional.empty())).thenReturn(buildGroupDto());

        final Token token = new Token();
        token.setRefId(USER_ID);
        when(tokenRepository.findById(TOKEN_VALUE)).thenReturn(Optional.of(token));
        final User user = new User();
        user.setId(USER_ID);
        user.setCustomerId("customerId");
        user.setGroupId(GROUP_ID);

        when(userRepository.findOne(any(Query.class))).thenReturn(Optional.of(user));
        when(internalGroupService.getMany(any(String.class))).thenReturn(Arrays.asList(buildGroupDto()));
        Mockito.when(internalSecurityService.userIsRootLevel()).thenReturn(true);

        final Customer customer = new Customer();
        customer.setId(user.getCustomerId());
        when(customerRepository.findById(user.getCustomerId())).thenReturn(Optional.of(customer));

        final UserDto userProfile = internalUserService.findUserById(TOKEN_VALUE);
        assertEquals(USER_ID, userProfile.getId());
    }

    @Test
    public void testUpdateUser() {
        final String emailToTest = "test@vitamui.com";
        final User user = IamServerUtilsTest.buildUser(USER_ID, emailToTest, "profileGroupId");

        final UserDto userToUpdate = new UserDto();
        VitamUIUtils.copyProperties(user, userToUpdate);
        userToUpdate.setAddress(VitamUIUtils.copyProperties(user.getAddress(), new AddressDto()));
        userToUpdate.setAnalytics(VitamUIUtils.copyProperties(user.getAnalytics(), new AnalyticsDto()));
        final GroupDto groupDto = buildGroupDto();
        groupDto.setId(user.getGroupId());
        groupDto.setCustomerId(user.getCustomerId());
        groupDto.setLevel(user.getLevel());

        when(userRepository.save(any())).thenReturn(user);
        when(userRepository.findByEmail(any())).thenReturn(user);
        when(userRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(user));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalGroupService.getOne(any(), any(), any())).thenReturn(groupDto);

        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(user.getCustomerId());
        customer.setOtp(OtpEnum.OPTIONAL);
        final CustomerDto customerDto = new CustomerDto();
        VitamUIUtils.copyProperties(customer, customerDto);

        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(internalSecurityService.getCustomerId()).thenReturn(customerDto.getId());

        final UserDto userUpdated = internalUserService.update(userToUpdate);
        assertNotNull("User shouldn't be null", userUpdated);
        assertThat(userToUpdate).isEqualToComparingFieldByFieldRecursively(userUpdated);
    }

    @Test
    public void testGetUsersGenericByCustomer() {
        final String customerId = "customerId";
        when(userRepository.getPaginatedValues(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any())).thenReturn(buildPageable(buildUser("id")));

        final Customer customer = new Customer();
        customer.setId("ID");
        customer.setCode("code");
        customer.setName("name");
        customer.setSubrogeable(true);
        when(customerRepository.findById(ArgumentMatchers.anyString())).thenReturn(Optional.of(customer));

        final QueryDto criteriaWrapper = new QueryDto();
        criteriaWrapper.addCriterion(new Criterion(customerId, customer.getId(), CriterionOperator.EQUALS));
        criteriaWrapper.addCriterion(new Criterion("type", "GENERIC", CriterionOperator.EQUALS));

        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        final PaginatedValuesDto<UserDto> subrogateUsers = internalUserService.getAllPaginated(0, 20, criteriaWrapper.toOptionalJson(), Optional.empty(),
                Optional.empty());
        assertThat(subrogateUsers.getValues()).isNotEmpty();
        assertThat(subrogateUsers.getValues().size()).isEqualTo(1);
    }

    @Test
    public void testGetUsersGenericByCustomerNotSubreogable() {
        when(userRepository.getPaginatedValues(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any())).thenReturn(buildPageable(buildUser("id")));
        final String customerId = "customerId";
        final Customer customer = new Customer();
        customer.setId("ID");
        customer.setCode("code");
        customer.setName("name");
        Mockito.when(customerRepository.findById(ArgumentMatchers.anyString())).thenReturn(Optional.of(customer));

        final QueryDto criteriaWrapper = new QueryDto();
        criteriaWrapper.addCriterion(new Criterion(customerId, customer.getId(), CriterionOperator.EQUALS));
        criteriaWrapper.addCriterion(new Criterion("type", "GENERIC", CriterionOperator.EQUALS));

        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        internalUserService.getAllPaginated(0, 20, criteriaWrapper.toOptionalJson(), Optional.empty(), Optional.empty());
    }

    private PaginatedValuesDto<User> buildPageable(final User user) {
        return new PaginatedValuesDto<>(Arrays.asList(user), 0, 1, false);
    }

    private User buildUser(final String id) {
        final User user = new User();
        user.setId(id);
        user.setGroupId(GROUP_ID);
        return user;
    }

    private GroupDto buildGroupDto() {
        final GroupDto dto = new GroupDto();
        dto.setName("Test Group");
        dto.setLevel(LEVEL);
        return dto;
    }

    @Test
    public void testAddMoreRestrictions() {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(LEVEL);

        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(internalSecurityService.getUser()).thenReturn(user);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(LEVEL);

        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("level").is(LEVEL));

        internalUserService.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(1);

        criteriaList = new ArrayList<>();
        internalUserService.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(0);
    }

    @Test
    public void testAddMoreRestrictionsAdminUser() {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);

        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(internalSecurityService.getUser()).thenReturn(user);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);

        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("level").is(ApiIamInternalConstants.ADMIN_LEVEL));

        internalUserService.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(1);

        criteriaList = new ArrayList<>();
        internalUserService.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(0);
    }

    @Test
    public void testDisableUser() {
        final String emailToTest = "test@vitamui.com";
        final User user = IamServerUtilsTest.buildUser(USER_ID, emailToTest, "profileGroupId");
        final User userUpdated = IamServerUtilsTest.buildUser(USER_ID, emailToTest, "profileGroupId");
        userUpdated.setStatus(UserStatusEnum.DISABLED);

        final UserDto userToUpdate = new UserDto();
        VitamUIUtils.copyProperties(user, userToUpdate);
        final GroupDto groupDto = buildGroupDto();
        groupDto.setId(user.getGroupId());
        groupDto.setCustomerId(user.getCustomerId());
        groupDto.setLevel(user.getLevel());

        when(userRepository.save(any())).thenReturn(userUpdated);
        when(userRepository.findByEmail(any())).thenReturn(user);
        when(userRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(user));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.existsById(any())).thenReturn(true);

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalGroupService.getOne(any(), any(), any())).thenReturn(groupDto);

        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(user.getCustomerId());
        customer.setOtp(OtpEnum.OPTIONAL);
        final CustomerDto customerDto = new CustomerDto();
        VitamUIUtils.copyProperties(customer, customerDto);

        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(internalSecurityService.getCustomerId()).thenReturn(customerDto.getId());

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", USER_ID);
        partialDto.put("customerId", user.getCustomerId());
        partialDto.put("status", UserStatusEnum.DISABLED.toString());
        final UserDto userUpdatedDto = internalUserService.patch(partialDto);
        assertNotNull("User shouldn't be null", userUpdatedDto);
        assertEquals("User status isn't correct", UserStatusEnum.DISABLED, userUpdatedDto.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void beforePatch_whenUserWasNotBlocked_thenIllegalArgumentException() {
        final User user = IamServerUtilsTest.buildUser(USER_ID, "test@vitamui.com", "profileGroupId");

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", USER_ID);
        partialDto.put("customerId", user.getCustomerId());
        partialDto.put("status", UserStatusEnum.BLOCKED.toString());
        internalUserService.beforePatch(partialDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void beforePatch_whenUserWasBlocked_thenIllegalArgumentException() {
        final String emailToTest = "test@vitamui.com";
        final User user = IamServerUtilsTest.buildUser(USER_ID, "test@vitamui.com", "profileGroupId");
        user.setStatus(UserStatusEnum.BLOCKED);
        final User userUpdated = IamServerUtilsTest.buildUser(USER_ID, emailToTest, "profileGroupId");
        userUpdated.setStatus(UserStatusEnum.BLOCKED);
        final UserDto userToUpdate = new UserDto();
        VitamUIUtils.copyProperties(user, userToUpdate);
        final GroupDto groupDto = buildGroupDto();
        groupDto.setId(user.getGroupId());
        groupDto.setCustomerId(user.getCustomerId());
        groupDto.setLevel(user.getLevel());

        when(userRepository.save(any())).thenReturn(userUpdated);
        when(userRepository.findByEmail(any())).thenReturn(user);
        when(userRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(user));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.existsById(any())).thenReturn(true);

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalSecurityService.getUser()).thenReturn(new AuthUserDto());
        when(internalGroupService.getOne(any(), any(), any())).thenReturn(groupDto);
        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(user.getCustomerId());
        customer.setOtp(OtpEnum.OPTIONAL);
        final CustomerDto customerDto = new CustomerDto();
        VitamUIUtils.copyProperties(customer, customerDto);

        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(internalSecurityService.getCustomerId()).thenReturn(customerDto.getId());

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", USER_ID);
        partialDto.put("customerId", user.getCustomerId());
        partialDto.put("status", UserStatusEnum.BLOCKED.toString());
        internalUserService.beforePatch(partialDto);
    }

    /**
     * Disable then Enable an user.
     * An email should be sent to the user in order to change his password.
     */
    @Test
    public void testDisableThenEnableUser() {
        final String emailToTest = "test@vitamui.com";
        final User user = IamServerUtilsTest.buildUser(USER_ID, emailToTest, "profileGroupId");
        user.setStatus(UserStatusEnum.DISABLED);
        final User userUpdated = IamServerUtilsTest.buildUser(USER_ID, emailToTest, "profileGroupId");
        userUpdated.setStatus(UserStatusEnum.ENABLED);

        final UserDto userToUpdate = new UserDto();
        VitamUIUtils.copyProperties(user, userToUpdate);
        final GroupDto groupDto = buildGroupDto();
        groupDto.setId(user.getGroupId());
        groupDto.setCustomerId(user.getCustomerId());
        groupDto.setLevel(user.getLevel());

        when(userRepository.save(any())).thenReturn(userUpdated);
        when(userRepository.findByEmail(any())).thenReturn(user);
        when(userRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(user));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.existsById(any())).thenReturn(true);

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalSecurityService.getUser()).thenReturn(new AuthUserDto());
        when(internalGroupService.getOne(any(), any(), any())).thenReturn(groupDto);

        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(user.getCustomerId());
        customer.setOtp(OtpEnum.OPTIONAL);
        final CustomerDto customerDto = new CustomerDto();
        VitamUIUtils.copyProperties(customer, customerDto);

        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(internalSecurityService.getCustomerId()).thenReturn(customerDto.getId());

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", USER_ID);
        partialDto.put("customerId", user.getCustomerId());
        partialDto.put("status", UserStatusEnum.ENABLED.toString());
        final UserDto userUpdatedDto = internalUserService.patch(partialDto);
        assertNotNull("User shouldn't be null", userUpdatedDto);
        assertEquals("User status isn't correct", UserStatusEnum.ENABLED, userUpdatedDto.getStatus());
    }

    @Test
    public void getLevels_whenProfilesExist_returnsLevels() {
        final Optional<String> criteria = Optional.empty();
        final List<Document> mappedResults = new ArrayList<>();
        final Document document = new Document("level", Arrays.asList("DEV", "TEST"));
        mappedResults.add(document);
        final Document rawResults = new Document();
        final AggregationResults<Document> value = new AggregationResults<>(mappedResults, rawResults);
        when(userRepository.aggregate(any(TypedAggregation.class), ArgumentMatchers.eq(Document.class))).thenReturn(value);
        final List<String> levels = internalUserService.getLevels(criteria);
        assertThat(levels.size()).isEqualTo(2);
        assertThat(levels.get(0)).isEqualTo("DEV");
        assertThat(levels.get(1)).isEqualTo("TEST");
    }

    @Test
    public void getLevels_whenNoProfile_returnsEmptyList() {
        final Optional<String> criteria = Optional.empty();
        final List<Document> mappedResults = new ArrayList<>();
        final Document rawResults = new Document();
        final AggregationResults<Document> value = new AggregationResults<>(mappedResults, rawResults);
        when(userRepository.aggregate(any(TypedAggregation.class), ArgumentMatchers.eq(Document.class))).thenReturn(value);
        final List<String> levels = internalUserService.getLevels(criteria);
        assertThat(levels.size()).isEqualTo(0);
    }

    @Test
    public void testUpdateUserWithInvalidEmailFormat() {
        final String email = "test@vitamui.com";
        final User user = IamServerUtilsTest.buildUser(USER_ID, email, "profileGroupId");

        final UserDto userToUpdate = new UserDto();
        VitamUIUtils.copyProperties(user, userToUpdate);
        userToUpdate.setAddress(VitamUIUtils.copyProperties(user.getAddress(), new AddressDto()));
        final GroupDto groupDto = buildGroupDto();
        groupDto.setId(user.getGroupId());
        groupDto.setCustomerId(user.getCustomerId());
        groupDto.setLevel(user.getLevel());

        when(userRepository.save(any())).thenReturn(user);
        when(userRepository.findByEmail(any())).thenReturn(user);
        when(userRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(user));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalGroupService.getOne(any(), any(), any())).thenReturn(groupDto);

        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(user.getCustomerId());
        customer.setOtp(OtpEnum.OPTIONAL);
        final CustomerDto customerDto = new CustomerDto();
        VitamUIUtils.copyProperties(customer, customerDto);

        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(internalSecurityService.getCustomerId()).thenReturn(customerDto.getId());

        userToUpdate.setEmail("-test@vitamui.com");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail("test-@vitamui.com");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail("test--toto@vitamui.com");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail(".test@vitamui.com");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail("test.@vitamui.com");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail("test@t");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail("test@vitamui.f");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail("test@vitamui");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail("test..toto@vitamui.com");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setEmail(null);
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }
    }

    @Test
    public void testUpdateUserWithValidEmailFormat() {
        final String email = "test@vitamui.com";
        final User user = IamServerUtilsTest.buildUser(USER_ID, email, "profileGroupId");

        final UserDto userToUpdate = new UserDto();
        VitamUIUtils.copyProperties(user, userToUpdate);
        userToUpdate.setAddress(VitamUIUtils.copyProperties(user.getAddress(), new AddressDto()));
        userToUpdate.setAnalytics(VitamUIUtils.copyProperties(user.getAnalytics(), new AnalyticsDto()));
        final GroupDto groupDto = buildGroupDto();
        groupDto.setId(user.getGroupId());
        groupDto.setCustomerId(user.getCustomerId());
        groupDto.setLevel(user.getLevel());

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(userRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(user));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalGroupService.getOne(any(), any(), any())).thenReturn(groupDto);

        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(user.getCustomerId());
        customer.setOtp(OtpEnum.OPTIONAL);
        final CustomerDto customerDto = new CustomerDto();
        VitamUIUtils.copyProperties(customer, customerDto);

        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(internalSecurityService.getCustomerId()).thenReturn(customerDto.getId());

        userToUpdate.setEmail("_test-test.test.test-test_@vitamui.com");
        User updatedUser = new User();
        VitamUIUtils.copyProperties(user, updatedUser);
        updatedUser.setEmail(userToUpdate.getEmail());
        when(userRepository.save(any())).thenReturn(updatedUser);
        UserDto userUpdated = internalUserService.update(userToUpdate);
        assertNotNull("User shouldn't be null", userUpdated);
        assertThat(userToUpdate).isEqualToComparingFieldByFieldRecursively(userUpdated);

        userToUpdate.setEmail("test@vitamui.fr");
        updatedUser = new User();
        VitamUIUtils.copyProperties(user, updatedUser);
        updatedUser.setEmail(userToUpdate.getEmail());
        when(userRepository.save(any())).thenReturn(updatedUser);
        userUpdated = internalUserService.update(userToUpdate);
        assertNotNull("User shouldn't be null", userUpdated);
        assertThat(userToUpdate).isEqualToComparingFieldByFieldRecursively(userUpdated);
    }

    @Test
    public void testUpdateUserWithInvalidPhoneNumberFormat() {
        final String email = "test@vitamui.com";
        final User user = IamServerUtilsTest.buildUser(USER_ID, email, "profileGroupId");

        final UserDto userToUpdate = new UserDto();
        VitamUIUtils.copyProperties(user, userToUpdate);
        userToUpdate.setAddress(VitamUIUtils.copyProperties(user.getAddress(), new AddressDto()));
        final GroupDto groupDto = buildGroupDto();
        groupDto.setId(user.getGroupId());
        groupDto.setCustomerId(user.getCustomerId());
        groupDto.setLevel(user.getLevel());

        when(userRepository.save(any())).thenReturn(user);
        when(userRepository.findByEmail(any())).thenReturn(user);
        when(userRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(user));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalGroupService.getOne(any(), any(), any())).thenReturn(groupDto);

        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(user.getCustomerId());
        customer.setOtp(OtpEnum.OPTIONAL);
        final CustomerDto customerDto = new CustomerDto();
        VitamUIUtils.copyProperties(customer, customerDto);

        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(internalSecurityService.getCustomerId()).thenReturn(customerDto.getId());

        userToUpdate.setPhone("0171270691");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setPhone(user.getPhone());
        userToUpdate.setMobile("0671270691");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setPhone("+331712706999999999999");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setPhone(user.getPhone());
        userToUpdate.setMobile("+336712706999999999999");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setPhone("+33171");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setPhone(user.getPhone());
        userToUpdate.setMobile("+33671");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setPhone("+33171$_éù%999999+");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }

        userToUpdate.setPhone(user.getPhone());
        userToUpdate.setMobile("+33671$_éù%999999+");
        try {
            internalUserService.update(userToUpdate);
        }
        catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("format is not allowed"));
        }
    }

    @Test
    public void testUpdateUserWithValidPhoneNumberFormat() {
        final String email = "test@vitamui.com";
        final User user = IamServerUtilsTest.buildUser(USER_ID, email, "profileGroupId");

        final UserDto userToUpdate = new UserDto();
        VitamUIUtils.copyProperties(user, userToUpdate);
        userToUpdate.setAddress(VitamUIUtils.copyProperties(user.getAddress(), new AddressDto()));
        userToUpdate.setAnalytics(VitamUIUtils.copyProperties(user.getAnalytics(), new AnalyticsDto()));
        final GroupDto groupDto = buildGroupDto();
        groupDto.setId(user.getGroupId());
        groupDto.setCustomerId(user.getCustomerId());
        groupDto.setLevel(user.getLevel());

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(userRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(user));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalGroupService.getOne(any(), any(), any())).thenReturn(groupDto);

        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(user.getCustomerId());
        customer.setOtp(OtpEnum.OPTIONAL);
        final CustomerDto customerDto = new CustomerDto();
        VitamUIUtils.copyProperties(customer, customerDto);

        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(internalSecurityService.getCustomerId()).thenReturn(customerDto.getId());

        userToUpdate.setPhone("+33171270699");
        final User updatedUser = new User();
        VitamUIUtils.copyProperties(user, updatedUser);
        updatedUser.setPhone(userToUpdate.getPhone());
        when(userRepository.save(any())).thenReturn(updatedUser);
        UserDto userUpdated = internalUserService.update(userToUpdate);
        assertNotNull("User shouldn't be null", userUpdated);
        assertThat(userToUpdate).isEqualToComparingFieldByFieldRecursively(userUpdated);

        userToUpdate.setPhone("+333171270699");
        VitamUIUtils.copyProperties(user, updatedUser);
        updatedUser.setPhone(userToUpdate.getPhone());
        when(userRepository.save(any())).thenReturn(updatedUser);
        userUpdated = internalUserService.update(userToUpdate);
        assertNotNull("User shouldn't be null", userUpdated);
        assertThat(userToUpdate).isEqualToComparingFieldByFieldRecursively(userUpdated);

        userToUpdate.setMobile("+33671270699");
        VitamUIUtils.copyProperties(user, updatedUser);
        updatedUser.setPhone(userToUpdate.getPhone());
        updatedUser.setMobile(userToUpdate.getMobile());
        when(userRepository.save(any())).thenReturn(updatedUser);
        userUpdated = internalUserService.update(userToUpdate);
        assertNotNull("User shouldn't be null", userUpdated);
        assertThat(userToUpdate).isEqualToComparingFieldByFieldRecursively(userUpdated);

        userToUpdate.setMobile("+333671270699");
        VitamUIUtils.copyProperties(user, updatedUser);
        updatedUser.setPhone(userToUpdate.getPhone());
        updatedUser.setMobile(userToUpdate.getMobile());
        when(userRepository.save(any())).thenReturn(updatedUser);
        userUpdated = internalUserService.update(userToUpdate);
        assertNotNull("User shouldn't be null", userUpdated);
        assertThat(userToUpdate).isEqualToComparingFieldByFieldRecursively(userUpdated);
    }

    @Test
    public void patchNotAllowedAnalyticsFieldShouldThrowAnException() {
        Throwable thrown = catchThrowable(() -> internalUserService.patchAnalytics(Map.of("notAllowedField", "test")));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unable to patch user analytics key : notAllowedField is not allowed");
        verifyNoInteractions(applicationInternalService);
        verifyNoInteractions(userRepository);
    }

    @Test
    public void patchAnalyticsWithEmptyPayloadShouldThrowAnException() {
        Throwable thrown = catchThrowable(() -> internalUserService.patchAnalytics(Map.of()));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unable to patch user analytics : payload is empty");
        verifyNoInteractions(applicationInternalService);
        verifyNoInteractions(userRepository);
    }

    @Test
    public void patchApplicationAnalyticsNonExistentUserShouldThrowAnException() {
        internalUserService = spy(internalUserService);
        final AuthUserDto authUserDto = IamServerUtilsTest.buildAuthUserDto();
        when(internalUserService.getMe()).thenReturn(authUserDto);
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> internalUserService.patchAnalytics(Map.of(APPLICATION_ID, "PROFILES_APP")));

        assertThat(thrown).isInstanceOf(NotFoundException.class).hasMessageContaining("No user found with id : userId");
        verify(userRepository).findById(authUserDto.getId());
        verify(applicationInternalService, never()).getAll(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void patchApplicationAnalyticsWithoutPermissionShouldThrowAnException() {
        internalUserService = spy(internalUserService);
        final AuthUserDto authUserDto = IamServerUtilsTest.buildAuthUserDto();
        when(internalUserService.getMe()).thenReturn(authUserDto);
        final User user = IamServerUtilsTest.buildUser(authUserDto.getId(), "", "");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(applicationInternalService.getAll(Optional.empty(), Optional.empty())).thenReturn(List.of());

        Throwable thrown = catchThrowable(() -> internalUserService.patchAnalytics(Map.of(APPLICATION_ID, "applicationWithoutPermission")));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("User has no permission to access to the application : applicationWithoutPermission");
        verify(userRepository).findById(user.getId());
        verify(applicationInternalService).getAll(Optional.empty(), Optional.empty());
        verifyNoMoreInteractions(applicationInternalService);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void patchApplicationAnalyticsOk() {
        String applicationId = "PROFILES_APP";
        ApplicationDto application = new ApplicationDto();
        application.setIdentifier(applicationId);

        internalUserService = spy(internalUserService);
        final AuthUserDto authUserDto = IamServerUtilsTest.buildAuthUserDto();
        when(internalUserService.getMe()).thenReturn(authUserDto);
        final User user = IamServerUtilsTest.buildUser(authUserDto.getId(), "", "");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(applicationInternalService.getAll(Optional.empty(), Optional.empty())).thenReturn(List.of(application));
        assertThat(user.getAnalytics().getApplications()).isNullOrEmpty();

        internalUserService.patchAnalytics(Map.of(APPLICATION_ID, applicationId));

        verify(userRepository).findById(user.getId());
        verify(applicationInternalService).getAll(Optional.empty(), Optional.empty());
        verifyNoMoreInteractions(applicationInternalService);

        final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue()).isEqualToIgnoringGivenFields(user);
        List<ApplicationAnalytics> applications = captor.getValue().getAnalytics().getApplications();
        assertThat(applications).hasSize(1);
        assertThat(applications.get(0).getApplicationId()).isEqualTo(applicationId);
        assertThat(applications.get(0).getAccessCounter()).isEqualTo(1);
    }
}
