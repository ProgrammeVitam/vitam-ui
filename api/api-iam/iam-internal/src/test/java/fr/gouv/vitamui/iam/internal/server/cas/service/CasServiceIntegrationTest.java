package fr.gouv.vitamui.iam.internal.server.cas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;

@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = { CustomSequenceRepository.class, TokenRepository.class }, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class CasServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private static final int PASSWORD_EXPIRATION_DELAY = 42;

    private static final String EMAIL = "surrogate@vitamui.com";

    private static final String PASSWORD = "passwd";

    private static final String CUSTOMER_ID = "customerId";

    private static final String USER_IDENTIFIER = "123456";

    @InjectMocks
    private CasInternalService casService;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private OwnerRepository ownerRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @MockBean
    private UserInternalService internalUserService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private SubrogationInternalService internalSubrogationService;

    @MockBean
    private SubrogationRepository subrogationRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private TenantInternalService internalTenantService;

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private SpMetadataGenerator spMetadataGenerator;

    @MockBean
    private TenantRepository tenantRepository;

    @Mock
    private InternalHttpContext internalHttpContext;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        casService.setTokenRepository(tokenRepository);
        casService.setIamLogbookService(iamLogbookService);
        casService.setMongoTemplate(mongoTemplate);
        casService.setTokenTtl(15);
        casService.setSubrogationTokenTtl(15);
        casService.setTimeIntervalForLoginAttempts(20);
        casService.setPasswordEncoder(passwordEncoder);
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
        final Tenant tenant = new Tenant();
        tenant.setIdentifier(10);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.ofNullable(tenant));
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);

        tokenRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    public void testLogoutSubrogation() {
        final String superUser = "superUser@vitamui.com";
        final String surrogate = EMAIL;
        final Subrogation subro = new Subrogation();
        subro.setSuperUser(superUser);
        subro.setSuperUserCustomerId("superUserCustomerId");
        subro.setSurrogate(surrogate);
        subro.setSurrogateCustomerId("surrogateCustomerId");
        Mockito.when(subrogationRepository.findBySuperUserAndSurrogate(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(subro));
        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString())).thenReturn(new User());
        casService.deleteSubrogationBySuperUserAndSurrogate(superUser, surrogate);

        final Criteria criteria = Criteria.where("obId").is(subro.getId()).and("obIdReq").is(MongoDbCollections.SUBROGATIONS).and("evType")
                .is(EventType.EXT_VITAMUI_LOGOUT_SURROGATE);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    @Test
    public void testGetUserByEmailWithGenericUsers() {
        final UserDto user = new UserDto();
        user.setType(UserTypeEnum.GENERIC);
        user.setId("ID");
        user.setEmail(EMAIL);
        final Subrogation subro = getUsersByEmail(user);

        final Criteria criteria = Criteria.where("obId").is(subro.getId()).and("obIdReq").is(MongoDbCollections.SUBROGATIONS).and("evType")
                .is(EventType.EXT_VITAMUI_START_SURROGATE_GENERIC);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    @Test
    public void testGetUserByEmailWithNominativecUsers() {
        final UserDto user = new UserDto();
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setId("ID");
        user.setEmail(EMAIL);
        final Subrogation subro = getUsersByEmail(user);

        final Criteria criteria = Criteria.where("obId").is(subro.getId()).and("obIdReq").is(MongoDbCollections.SUBROGATIONS).and("evType")
                .is(EventType.EXT_VITAMUI_START_SURROGATE_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    private Subrogation getUsersByEmail(final UserDto user) {
        final String email = user.getEmail();
        final AuthUserDto authUser = new AuthUserDto();
        authUser.setId("ID");
        final Subrogation subro = new Subrogation();
        subro.setSuperUser("superuser@vitamui.com");
        subro.setSurrogate(email);
        Mockito.when(internalUserService.findUserByEmail(ArgumentMatchers.anyString())).thenReturn(user);
        Mockito.when(internalUserService.loadGroupAndProfiles(ArgumentMatchers.any())).thenReturn(authUser);
        Mockito.when(subrogationRepository.findOneBySurrogate(ArgumentMatchers.anyString())).thenReturn(subro);
        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString())).thenReturn(new User());
        casService.getUserByEmail(email, Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER));
        return subro;
    }

    private User prepareUserPwd(final String pwd) {
        final User user = new User();
        user.setEmail(EMAIL);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setStatus(UserStatusEnum.ENABLED);
        user.setCustomerId(CUSTOMER_ID);
        user.setIdentifier(USER_IDENTIFIER);
        if (pwd != null) {
            user.setPassword(passwordEncoder.encode(pwd));
        }
        Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(user);
        final Customer customer = new Customer();
        customer.setId(CUSTOMER_ID);
        customer.setPasswordRevocationDelay(PASSWORD_EXPIRATION_DELAY);
        Mockito.when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        return user;
    }

    @Test
    public void testPasswordCreation() {

        final User user = prepareUserPwd(null);

        casService.updatePassword(EMAIL, PASSWORD);

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_PASSWORD_INIT);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    @Test
    public void testPasswordUpdate() {

        final User user = prepareUserPwd("oldPassword");
        final String oldPassword = user.getPassword();
        final OffsetDateTime passwordExpirationDate = OffsetDateTime.now().plusMonths(PASSWORD_EXPIRATION_DELAY);
        casService.updatePassword(EMAIL, PASSWORD);

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_PASSWORD_CHANGE);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        assertNotEquals("Password should not be the same", oldPassword, user.getPassword());
        assertTrue("Password Expiration date is not correct", user.getPasswordExpirationDate().isAfter(passwordExpirationDate));
    }

    @Test(expected = ConflictException.class)
    public void testPasswordUpdateAlreadyUsedPassword() {

        final User user = prepareUserPwd("oldPassword");
        user.getOldPasswords().add(passwordEncoder.encode(PASSWORD));

        casService.updatePassword(EMAIL, PASSWORD);
    }

    @Test
    public void testUpdateNbFailedAttempsNoSurrogate() {

        final User user = new User();
        user.setIdentifier(USER_IDENTIFIER);
        user.setStatus(UserStatusEnum.BLOCKED);

        casService.updateNbFailedAttempsPlusLastConnectionAndStatus(user, 4, UserStatusEnum.ENABLED);

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
                .is(EventType.EXT_VITAMUI_BLOCK_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{\"diff\":{\"-Statut\":\"ENABLED\"," + "\"+Statut\":\"BLOCKED\"},\"Durée du blocage\":\"PT20M\"}");
    }
}
