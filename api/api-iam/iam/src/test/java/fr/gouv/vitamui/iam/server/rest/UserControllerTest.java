package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.test.rest.CrudControllerTest;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.customer.service.CustomerService;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.server.user.service.UserEmailService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UserController}.
 *
 * Emmanuel Deviller
 */
public final class UserControllerTest implements CrudControllerTest {

    private AutoCloseable mocks;

    private static final String IDENTIFIER = "userIdentifier";

    private static final String UNKNOWN_CUSTOMER_ID = "unknownCustomerId";

    private UserController userController;

    @InjectMocks
    private UserService userService;

    @Mock
    private GroupService groupService;

    @Mock
    protected CustomerService customerService;

    @Mock
    protected SecurityService securityService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private UserEmailService userEmailService;

    @Mock
    private UserConverter userConverter;

    @Mock
    private ConnectionHistoryService connectionHistoryService;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        userController = new UserController(userService, connectionHistoryService, securityService);
        Mockito.when(userConverter.convertDtoToEntity(ArgumentMatchers.any())).thenCallRealMethod();
        Mockito.when(userConverter.convertEntityToDto(ArgumentMatchers.any())).thenCallRealMethod();

        when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
    }

    protected void prepareServices() {
        final UserDto userDto = buildUserDto();
        final User user = buildUser();

        when(customerRepository.findById(userDto.getCustomerId())).thenReturn(Optional.of(buildCustomer()));
        when(groupService.getOne(userDto.getGroupId(), Optional.empty(), Optional.empty())).thenReturn(buildGroupDto());
        when(groupService.getOneByPassSecurity(userDto.getGroupId(), Optional.empty())).thenReturn(buildGroupDto());
        when(securityService.isLevelAllowed(anyString())).thenCallRealMethod();
        when(securityService.getLevel()).thenReturn("");
        when(securityService.getCustomerId()).thenReturn(buildCustomerDto().getId());
        when(customerService.getOne(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(buildCustomerDto());

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
    void testCreationFailsAsCustomerDoesNotExist() throws Exception {
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
                "Unable to create user user@supermail.fr (unknownCustomerId): customerId unknownCustomerId is not allowed",
                e.getMessage()
            );
        }
    }

    @Test
    void testUserCreationFailsAsCustomerIsNull() throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();
        userDto.setId(null);
        userDto.setIdentifier(null);
        userDto.setCustomerId(null);

        prepareServices();

        try {
            userController.create(userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create user user@supermail.fr (null): customerId null is not allowed",
                e.getMessage()
            );
        }
    }

    @Test
    void testCreationFailsAsUserIdIsNotNull() throws InvalidParseOperationException, PreconditionFailedException {
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
    void testCreationFailsAsIdenfierIsNotNull() throws InvalidParseOperationException, PreconditionFailedException {
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
    void testCreationFailsAsEmailAlreadyExistsForSameCustomer()
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
    void testCreationDoesNotFailAsEmailAlreadyExistsForOtherCustomer()
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
    void testCreationFailsAsGroupDoesNotExist() throws InvalidParseOperationException, PreconditionFailedException {
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
    void testCreationFailsAsLevelIsNotValid() throws InvalidParseOperationException, PreconditionFailedException {
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

    @Test
    void testUpdateFailsAsCustomerDoesNotExist() throws Exception {
        final UserDto userDto = buildUserDto();
        userDto.setCustomerId(UNKNOWN_CUSTOMER_ID);

        prepareServices();
        when(customerService.getOne(userDto.getCustomerId(), Optional.empty())).thenReturn(null);

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
    void testUpdateFailsAsTheGroupDoesNotExist() throws InvalidParseOperationException, PreconditionFailedException {
        final UserDto userDto = buildUserDto();

        prepareServices();
        when(groupService.getOne(userDto.getGroupId(), Optional.empty(), Optional.empty())).thenReturn(null);

        try {
            userController.update(userDto.getId(), userDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to update user " + userDto.getId() + ": group does not exist", e.getMessage());
        }
    }

    @Test
    void testUpdateFailsAsTheEmailAlreadyExists() throws InvalidParseOperationException, PreconditionFailedException {
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
    void testUpdateSuccessAsTheEmailAlreadyExistsOnlyForOtherCustomer()
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
    void testPatchAnalyticsOk() throws InvalidParseOperationException {
        UserDto userDto = buildUserDto();
        UserService userService1 = Mockito.mock(UserService.class);
        when(userService1.patchAnalytics(any())).thenReturn(userDto);
        userController = new UserController(userService1, connectionHistoryService, securityService);
        Map<String, Object> partialDto = Map.of(APPLICATION_ID, "SUBROGATIONS_APP");

        UserDto result = userController.patchAnalytics(partialDto);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(userService1).patchAnalytics(captor.capture());
        assertThat(captor.getValue()).isEqualTo(partialDto);
        assertThat(result).isEqualTo(userDto);
    }

    @Test
    void testCannotDelete() {
        assertThrows(UnsupportedOperationException.class, () -> userController.delete("dummy"));
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

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
