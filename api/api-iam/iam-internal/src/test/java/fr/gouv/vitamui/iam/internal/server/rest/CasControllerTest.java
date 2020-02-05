package fr.gouv.vitamui.iam.internal.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.internal.server.cas.service.CasInternalService;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Tests the {@link CasInternalController}.
 *
 *
 */

public final class CasControllerTest extends AbstractServerIdentityBuilder {

    private static final String ID = "myUser";

    private static final String EMAIL = "jerome.leleu@vitamui.com";

    private static final String SUPER_USER_EMAIL = "sub.rogateur@vitamui.com";

    private static final String PASSWORD = "password";

    private static final String CUSTOMER_ID = "customerId";

    private CasInternalController controller;

    private UserInternalService internalUserService;

    private CasInternalService casService;

    private TokenRepository tokenRepository;

    private MongoTemplate mongoTemplate;

    private UserRepository userRepository;

    private SubrogationInternalService internalSubrogationService;

    private SubrogationRepository subrogationRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private CustomerRepository customerRepository;

    private CustomSequenceRepository sequenceRepository;

    private User user;

    private UserInternalService userInternalService;

    private GroupInternalService groupInternalService;

    private GroupRepository groupRepository;

    private ProfileRepository profilRepository;

    private InternalSecurityService internalSecurityService;

    private SubrogationConverter subrogationConverter;

    private IamLogbookService iamLogbookService;

    @Before
    public void setup() {
        internalUserService = mock(UserInternalService.class);
        userRepository = mock(UserRepository.class);
        mongoTemplate = mock(MongoTemplate.class);
        tokenRepository = mock(TokenRepository.class);
        sequenceRepository = mock(CustomSequenceRepository.class);

        subrogationRepository = mock(SubrogationRepository.class);
        userInternalService = mock(UserInternalService.class);
        groupInternalService = mock(GroupInternalService.class);
        groupRepository = mock(GroupRepository.class);
        profilRepository = mock(ProfileRepository.class);
        internalSecurityService = mock(InternalSecurityService.class);
        customerRepository = mock(CustomerRepository.class);
        iamLogbookService = mock(IamLogbookService.class);

        subrogationConverter = new SubrogationConverter(userRepository);
        internalSubrogationService = new SubrogationInternalService(sequenceRepository, subrogationRepository, userRepository, userInternalService,
                groupInternalService, groupRepository, profilRepository, internalSecurityService, customerRepository, subrogationConverter, iamLogbookService);

        casService = spy(CasInternalService.class);
        casService.setInternalUserService(internalUserService);
        casService.setUserRepository(userRepository);
        casService.setMongoTemplate(mongoTemplate);
        casService.setTokenRepository(tokenRepository);
        casService.setInternalSubrogationService(internalSubrogationService);
        casService.setSubrogationRepository(subrogationRepository);
        casService.setCustomerRepository(customerRepository);
        casService.setTokenTtl(15);
        casService.setSubrogationTokenTtl(15);
        casService.setTimeIntervalForLoginAttempts(20);
        casService.setIamLogbookService(iamLogbookService);
        casService.setPasswordEncoder(passwordEncoder);

        controller = new CasInternalController(casService, passwordEncoder, internalUserService);
        controller.setMaximumFailuresForLoginAttempts(5);
        controller.setIamLogbookService(iamLogbookService);

        user = new User();
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setId(ID);
        user.setPassword(null);
        user.setEmail(EMAIL);
        user.setCustomerId(CUSTOMER_ID);
        user.setPasswordExpirationDate(OffsetDateTime.now());
        when(userRepository.findByEmail(EMAIL)).thenReturn(user);

        final Customer customer = new Customer();
        customer.setPasswordRevocationDelay(156);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindByEmail() {
        final AuthUserDto user = new AuthUserDto();
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setStatus(UserStatusEnum.ENABLED);
        user.setLevel("DEV");

        when(internalUserService.findUserByEmail(EMAIL)).thenReturn(user);
        final AuthUserDto userProfile = new AuthUserDto();
        userProfile.setProfileGroup(new GroupDto());
        when(internalUserService.loadGroupAndProfiles(user)).thenReturn(userProfile);
        when(tokenRepository.generateSuperId()).thenReturn("en");
        final AuthUserDto result = (AuthUserDto) controller.getUserByEmail(EMAIL, Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));

        userProfile.setAuthToken("TOKEN");
        assertEquals(userProfile, result);
        verify(tokenRepository, times(1)).save(any());
    }

    @Test
    public void testLoginOK() {
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setType(UserTypeEnum.NOMINATIVE);
        final LoginRequestDto request = new LoginRequestDto();
        request.setUsername(EMAIL);
        request.setPassword(PASSWORD);
        controller.login(request);
        verify(casService).updateNbFailedAttempsPlusLastConnectionAndStatus(user, 0, UserStatusEnum.ENABLED);
        verify(iamLogbookService).loginEvent(user, null, null, null);
    }

    @Test
    public void testLoginOKGoodLoginButTooManyAttempsNotInTheLast20Minutes() {
        user.setNbFailedAttempts(10000);
        user.setLastConnection(OffsetDateTime.now().plusMinutes(-30));
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setType(UserTypeEnum.NOMINATIVE);
        final LoginRequestDto request = new LoginRequestDto();
        request.setUsername(EMAIL);
        request.setPassword(PASSWORD);
        controller.login(request);
        verify(casService).updateNbFailedAttempsPlusLastConnectionAndStatus(user, 0, UserStatusEnum.ENABLED);
    }

    @Test
    public void testLoginOKEvenIfAlmostTooManyFailedAttempts() {
        user.setNbFailedAttempts(4);
        user.setLastConnection(OffsetDateTime.now().plusMinutes(-10));
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setType(UserTypeEnum.NOMINATIVE);
        final LoginRequestDto request = new LoginRequestDto();
        request.setUsername(EMAIL);
        request.setPassword(PASSWORD);
        controller.login(request);
        verify(casService).updateNbFailedAttempsPlusLastConnectionAndStatus(user, 0, UserStatusEnum.ENABLED);
    }

    @Test(expected = TooManyRequestsException.class)
    public void testLoginKoGoodLoginButTooManyAttempsInTheLast20Minutes() {
        user.setNbFailedAttempts(10000);
        user.setLastConnection(OffsetDateTime.now().plusMinutes(-10));
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setType(UserTypeEnum.NOMINATIVE);
        final LoginRequestDto request = new LoginRequestDto();
        request.setUsername(EMAIL);
        request.setPassword(PASSWORD);
        try {
            controller.login(request);
        }
        finally {
            verify(iamLogbookService).loginEvent(user, null, null, "Too many login attempts for username: " + EMAIL);
        }
    }

    @Test(expected = TooManyRequestsException.class)
    public void testLoginKoOneMoreFailedAttempt() {
        user.setNbFailedAttempts(4);
        user.setLastConnection(OffsetDateTime.now().plusMinutes(-10));
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setPassword(passwordEncoder.encode("badPassword"));
        user.setType(UserTypeEnum.NOMINATIVE);
        final LoginRequestDto request = new LoginRequestDto();
        request.setUsername(EMAIL);
        request.setPassword(PASSWORD);
        controller.login(request);
    }

    @Test
    public void testLoginKoNullPasswords() {
        user.setType(UserTypeEnum.NOMINATIVE);
        final LoginRequestDto request = new LoginRequestDto();
        request.setUsername(EMAIL);
        try {
            controller.login(request);
            fail("should fail");
        }
        catch (final UnAuthorizedException e) {
            verify(iamLogbookService).loginEvent(user, null, null, "Bad credentials for username: " + EMAIL);
            assertEquals("Bad credentials for username: " + EMAIL, e.getMessage());
        }
        verify(casService).updateNbFailedAttempsPlusLastConnectionAndStatus(user, 1, UserStatusEnum.ENABLED);
    }

    @Test
    public void testChangePasswordOk() {
        user.setType(UserTypeEnum.NOMINATIVE);
        controller.changePassword(EMAIL, PASSWORD);

        verify(casService).updatePassword(EMAIL, PASSWORD);
    }

    @Test
    public void testFindSubrogationsBySuperUserId() {
        final Subrogation subrogation = buildSubrogation();
        when(subrogationRepository.findBySuperUser(SUPER_USER_EMAIL)).thenReturn(Arrays.asList(subrogation));
        final UserDto user = new UserDto();
        user.setEmail(SUPER_USER_EMAIL);
        user.setStatus(UserStatusEnum.ENABLED);
        when(internalUserService.getOne(ID, Optional.empty())).thenReturn(user);

        final List<SubrogationDto> subrogations = controller.getSubrogationsBySuperUserId(ID);
        assertEquals(1, subrogations.size());
        final SubrogationDto dto = subrogations.get(0);
        assertEquals(subrogation.getId(), dto.getId());
        assertEquals(subrogation.getDate(), Date.from(dto.getDate().toInstant()));
        assertEquals(subrogation.getSuperUser(), dto.getSuperUser());
        assertEquals(subrogation.getSurrogate(), dto.getSurrogate());
        assertEquals(subrogation.getStatus(), dto.getStatus());
    }

    @Test
    public void testFindSubrogationsBySuperUserEmail() {
        final Subrogation subrogation = buildSubrogation();
        when(subrogationRepository.findBySuperUser(SUPER_USER_EMAIL)).thenReturn(Arrays.asList(subrogation));

        final List<SubrogationDto> subrogations = controller.getSubrogationsBySuperUserEmail(SUPER_USER_EMAIL);
        assertEquals(1, subrogations.size());
        final SubrogationDto dto = subrogations.get(0);
        assertEquals(subrogation.getId(), dto.getId());
        assertEquals(subrogation.getDate(), Date.from(dto.getDate().toInstant()));
        assertEquals(subrogation.getSuperUser(), dto.getSuperUser());
        assertEquals(subrogation.getSurrogate(), dto.getSurrogate());
        assertEquals(subrogation.getStatus(), dto.getStatus());
    }

    private Subrogation buildSubrogation() {
        final Subrogation subrogation = new Subrogation();
        subrogation.setId(ID);
        subrogation.setStatus(SubrogationStatusEnum.CREATED);
        subrogation.setDate(new Date());
        subrogation.setSurrogate(EMAIL);
        subrogation.setSuperUser(SUPER_USER_EMAIL);
        return subrogation;
    }
}
