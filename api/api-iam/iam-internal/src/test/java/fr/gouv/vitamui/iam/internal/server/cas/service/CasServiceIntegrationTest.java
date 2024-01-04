package fr.gouv.vitamui.iam.internal.server.cas.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
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
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
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
import fr.gouv.vitamui.iam.internal.server.user.dao.ConnectionHistoryRepository;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
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

import java.io.FileNotFoundException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = {CustomSequenceRepository.class, TokenRepository.class}, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class CasServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private static final String CREDENTIALS_DETAILS_FILE = "credentialsRepository/userCredentials.json";
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

    @MockBean
    private ConnectionHistoryService connectionHistoryService;

    @MockBean
    private ConnectionHistoryRepository connectionHistoryRepository;

    @Mock
    private InternalHttpContext internalHttpContext;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private PasswordValidator passwordValidator = new PasswordValidator();

    private JsonNode jsonNode;

    @Before
    public void setup() throws FileNotFoundException, InvalidParseOperationException {
        jsonNode =
            JsonHandler.getFromFile(PropertiesUtils.findFile(CREDENTIALS_DETAILS_FILE));
        MockitoAnnotations.initMocks(this);
        casService.setTokenRepository(tokenRepository);
        casService.setIamLogbookService(iamLogbookService);
        casService.setMongoTemplate(mongoTemplate);
        casService.setTokenTtl(15);
        casService.setSubrogationTokenTtl(15);
        casService.setTimeIntervalForLoginAttempts(20);
        casService.setPasswordEncoder(passwordEncoder);
        casService.setPasswordValidator(passwordValidator);
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
        final String surrogate = jsonNode.findValue("EMAIL").textValue();
        final Subrogation subro = new Subrogation();
        subro.setSuperUser(superUser);
        subro.setSuperUserCustomerId("superUserCustomerId");
        subro.setSurrogate(surrogate);
        subro.setSurrogateCustomerId("surrogateCustomerId");
        Mockito.when(subrogationRepository.findBySuperUserAndSurrogate(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(Optional.of(subro));
        Mockito.when(userRepository.findByEmailIgnoreCase(ArgumentMatchers.anyString())).thenReturn(new User());
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
        user.setEmail(jsonNode.findValue("EMAIL").textValue());
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
        user.setEmail(jsonNode.findValue("EMAIL").textValue());
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
        Mockito.when(userRepository.findByEmailIgnoreCase(ArgumentMatchers.anyString())).thenReturn(new User());
        casService.getUserByEmail(email, Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER));
        return subro;
    }

    private User prepareUserPwd(final String pwd) {
        final User user = new User();
        user.setEmail(jsonNode.findValue("EMAIL").textValue());
        user.setLastname("zz");
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setStatus(UserStatusEnum.ENABLED);
        user.setCustomerId(jsonNode.findValue("CUSTOMER_ID").textValue());
        user.setIdentifier(jsonNode.findValue("USER_IDENTIFIER").textValue());
        if (pwd != null) {
            user.setPassword(passwordEncoder.encode(pwd));
        }
        Mockito.when(userRepository.findByEmailIgnoreCase(jsonNode.findValue("EMAIL").textValue())).thenReturn(user);
        final Customer customer = new Customer();
        customer.setId(jsonNode.findValue("CUSTOMER_ID").textValue());
        customer.setPasswordRevocationDelay(Integer.parseInt(jsonNode.findValue("PASSWORD_EXPIRATION_DELAY").textValue()));
        Mockito.when(customerRepository.findById(jsonNode.findValue("CUSTOMER_ID").textValue())).thenReturn(Optional.of(customer));

        return user;
    }

    @Test
    public void testPasswordCreation() {

        final User user = prepareUserPwd(null);

        casService.updatePassword(jsonNode.findValue("EMAIL").textValue(), jsonNode.findValue("PASSWORD").textValue());

        final Criteria criteria = Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType")
            .is(EventType.EXT_VITAMUI_PASSWORD_INIT);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    @Test
    public void testPasswordUpdate() {

        final User user = prepareUserPwd("oldPassword");
        final String oldPassword = user.getPassword();
        final OffsetDateTime passwordExpirationDate = OffsetDateTime.now().plusMonths(Integer.parseInt(jsonNode.findValue("PASSWORD_EXPIRATION_DELAY").textValue())
        );
        casService.updatePassword(jsonNode.findValue("EMAIL").textValue(), jsonNode.findValue("PASSWORD").textValue());

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
        user.getOldPasswords().add(passwordEncoder.encode(jsonNode.findValue("PASSWORD").textValue()));

        casService.updatePassword(jsonNode.findValue("EMAIL").textValue(), jsonNode.findValue("PASSWORD").textValue());
    }

    @Test
    public void testUpdateNbFailedAttempsNoSurrogate() {

        final User user = new User();
        user.setIdentifier(jsonNode.findValue("USER_IDENTIFIER").textValue());
        user.setStatus(UserStatusEnum.BLOCKED);

        casService.updateNbFailedAttempsPlusLastConnectionAndStatus(user, 4, UserStatusEnum.ENABLED);

        final Criteria criteria =
            Criteria.where("obId").is(user.getIdentifier()).and("obIdReq").is(MongoDbCollections.USERS).and("evType").is(EventType.EXT_VITAMUI_BLOCK_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo("{\"diff\":{\"-Statut\":\"ENABLED\"," + "\"+Statut\":\"BLOCKED\"},\"Durée du blocage\":\"PT20M\"}");
    }

}
