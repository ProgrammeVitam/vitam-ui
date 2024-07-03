package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.customer.service.CustomerInternalService;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserEmailInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UserInternalController}.
 *
 * Emmanuel Deviller
 */
public final class UserControllerTest implements InternalCrudControllerTest {

    private static final String IDENTIFIER = "userIdentifier";

    private static final String UNKNOWN_CUSTOMER_ID = "unknownCustomerId";

    private UserInternalController userController;

    @InjectMocks
    private UserInternalService internalUserService;

    @Mock
    private GroupInternalService internalGroupService;

    @Mock
    protected CustomerInternalService internalCustomerService;

    @Mock
    protected InternalSecurityService internalSecurityService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private UserEmailInternalService internalUserEmailService;

    @Mock
    private UserConverter userConverter;

    @Mock
    private ConnectionHistoryService connectionHistoryService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        userController = new UserInternalController(internalUserService, connectionHistoryService);
        Mockito.when(userConverter.convertDtoToEntity(ArgumentMatchers.any())).thenCallRealMethod();
        Mockito.when(userConverter.convertEntityToDto(ArgumentMatchers.any())).thenCallRealMethod();

        when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
    }

    protected void prepareServices() {
        final UserDto userDto = buildUserDto();
        final User user = buildUser();

        when(customerRepository.findById(userDto.getCustomerId())).thenReturn(Optional.of(buildCustomer()));
        when(internalGroupService.getOne(userDto.getGroupId(), Optional.empty(), Optional.empty())).thenReturn(
            buildGroupDto()
        );
        when(internalGroupService.getOneByPassSecurity(userDto.getGroupId(), Optional.empty())).thenReturn(
            buildGroupDto()
        );
        when(internalSecurityService.isLevelAllowed(anyString())).thenCallRealMethod();
        when(internalSecurityService.getLevel()).thenReturn("");
        when(internalSecurityService.getCustomerId()).thenReturn(buildCustomerDto().getId());
        when(internalCustomerService.getOne(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
            buildCustomerDto()
        );

        when(userRepository.findByIdAndCustomerId(userDto.getId(), userDto.getCustomerId())).thenReturn(
            Optional.of(buildUser())
        );
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsById(userDto.getId())).thenReturn(true);
        when(userRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    @Override
    public void testCreationOK() throws Exception {
        final UserDto userDto = buildUserDto();
        userDto.setId(null);
        userDto.setIdentifier(null);
        prepareServices();
        userController.create(userDto);
    }

    @Override
    public void testCreationFailsAsIdIsProvided() throws Exception {
        final UserDto userDto = buildUserDto();
        prepareServices();

        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to update user " + userDto.getId() + ": mail already exists", e.getMessage());
        }
    }

    @Test
    @Override
    public void testCreationFailsAsCustomerDoesNotExist() throws Exception {
        final UserDto userDto = buildUserDto();
        userDto.setId(null);
        userDto.setIdentifier(null);
        userDto.setCustomerId(UNKNOWN_CUSTOMER_ID);

        prepareServices();
        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create user user@supermail.fr (unknownCustomerId): customer does not exist",
                e.getMessage()
            );
        }
    }

    @Test
    public void testUserCreationFailsAsCustomerIsNull()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setId(null);
        userDto.setIdentifier(null);
        userDto.setCustomerId(null);

        prepareServices();

        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to create user user@supermail.fr (null): customer does not exist", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsUserIdIsNotNull()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setIdentifier(null);

        prepareServices();

        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsIdenfierIsNotNull()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setId(null);
        userDto.setIdentifier(IDENTIFIER);

        prepareServices();

        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create user user@supermail.fr (customerId): identifier must be null",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreationFailsAsEmailAlreadyExistsForSameCustomer()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setId(null);
        userDto.setIdentifier(null);

        prepareServices();
        when(
            userRepository.findByEmailIgnoreCaseAndCustomerId(
                IamServerUtilsTest.USER_MAIL,
                IamServerUtilsTest.CUSTOMER_ID
            )
        ).thenReturn(buildUser());

        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to create user user@supermail.fr (customerId): mail already exists", e.getMessage());
        }
    }

    @Test
    public void testCreationDoesNotFailAsEmailAlreadyExistsForOtherCustomer()
        throws InvalidParseOperationException, PreconditionFailedException {
        String SOME_CUSTOMER_ID = "SOME_CUSTOMER_ID";
        final UserDto userDto = buildUserDto();
        userDto.setId(null);
        userDto.setIdentifier(null);

        User someOtherUser = new User();
        prepareServices();
        when(
            userRepository.findByEmailIgnoreCaseAndCustomerId(any(), Mockito.eq(IamServerUtilsTest.CUSTOMER_ID))
        ).thenReturn(null);
        when(userRepository.findByEmailIgnoreCaseAndCustomerId(any(), Mockito.eq(SOME_CUSTOMER_ID))).thenReturn(
            someOtherUser
        );

        userController.create(userDto);
    }

    @Test
    public void testCreationFailsAsGroupDoesNotExist()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setId(null);
        userDto.setIdentifier(null);
        userDto.setGroupId("UKNOWN_GROUP_ID");

        prepareServices();

        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to create user user@supermail.fr (customerId): group does not exist", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsLevelIsNotValid()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setId(null);

        prepareServices();

        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create user user@supermail.fr (customerId): identifier must be null",
                e.getMessage()
            );
        }
    }

    @Override
    @Test
    public void testUpdateOK() throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();

        prepareServices();
        userController.update(userDto.getId(), userDto);

        final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue()).isEqualToIgnoringGivenFields(
            userConverter.convertDtoToEntity(userDto),
            "passwordExpirationDate"
        );
    }

    @Override
    @Test
    public void testUpdateFailsAsCustomerDoesNotExist() throws Exception {
        final UserDto userDto = buildUserDto();
        userDto.setCustomerId(UNKNOWN_CUSTOMER_ID);

        prepareServices();
        when(internalCustomerService.getOne(userDto.getCustomerId(), Optional.empty())).thenReturn(null);

        try {
            userController.update(userDto.getId(), userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to update user " + userDto.getId() + ": customerId " + UNKNOWN_CUSTOMER_ID + " is not allowed",
                e.getMessage()
            );
        }
    }

    @Test
    public void testUpdateFailsAsTheGroupDoesNotExist()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();

        prepareServices();
        when(internalGroupService.getOne(userDto.getGroupId(), Optional.empty(), Optional.empty())).thenReturn(null);

        try {
            userController.update(userDto.getId(), userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to update user " + userDto.getId() + ": group does not exist", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsTheEmailAlreadyExists()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setEmail("test" + userDto.getEmail());

        prepareServices();
        when(userRepository.findByEmailIgnoreCaseAndCustomerId(userDto.getEmail(), userDto.getCustomerId())).thenReturn(
            buildUser()
        );

        try {
            userController.update(userDto.getId(), userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to update user " + userDto.getId() + ": mail already exists", e.getMessage());
        }
    }

    @Test
    public void testUpdateSuccessAsTheEmailAlreadyExistsOnlyForOtherCustomer()
        throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setEmail("test" + userDto.getEmail());

        User someOtherUser = new User();
        String SOME_CUSTOMER_ID = "SOME_CUSTOMER_ID";

        prepareServices();

        when(
            userRepository.findByEmailIgnoreCaseAndCustomerId(any(), Mockito.eq(IamServerUtilsTest.CUSTOMER_ID))
        ).thenReturn(null);
        when(userRepository.findByEmailIgnoreCaseAndCustomerId(any(), Mockito.eq(SOME_CUSTOMER_ID))).thenReturn(
            someOtherUser
        );
        userController.update(userDto.getId(), userDto);
    }

    @Override
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() throws Exception {
        final UserDto userDto = buildUserDto();

        prepareServices();

        try {
            userController.update("BAD ID", userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to update user " + userDto.getId() + ": mail already exists", e.getMessage());
        }
    }

    @Test
    public void testPatchAnalyticsOk() {
        UserDto userDto = buildUserDto();
        UserInternalService userInternalService = Mockito.mock(UserInternalService.class);
        when(userInternalService.patchAnalytics(any())).thenReturn(userDto);
        userController = new UserInternalController(userInternalService, connectionHistoryService);
        Map<String, Object> partialDto = Map.of(APPLICATION_ID, "SUBROGATIONS_APP");

        UserDto result = userController.patchAnalytics(partialDto);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(userInternalService).patchAnalytics(captor.capture());
        assertThat(captor.getValue()).isEqualTo(partialDto);
        assertThat(result).isEqualTo(userDto);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotDelete() throws InvalidParseOperationException, PreconditionFailedException {
        userController.delete("dummy");
    }

    protected CustomerDto buildCustomerDto() {
        final CustomerDto dto = IamServerUtilsTest.buildCustomerDto();
        return dto;
    }

    protected GroupDto buildGroupDto() {
        final GroupDto dto = IamServerUtilsTest.buildGroupDto();
        return dto;
    }

    protected ProfileDto buildProfileDto() {
        final ProfileDto dto = IamServerUtilsTest.buildProfileDto();
        return dto;
    }

    protected User buildUser() {
        final User user = IamServerUtilsTest.buildUser();
        return user;
    }

    protected UserDto buildUserDto() {
        final UserDto userDto = IamServerUtilsTest.buildUserDto();
        return userDto;
    }

    private Customer buildCustomer() {
        return IamServerUtilsTest.buildCustomer();
    }
}
