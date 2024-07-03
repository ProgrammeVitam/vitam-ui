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
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.config.ConverterConfig;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.logbook.config.LogbookConfiguration;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileNotFoundException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import({ ConverterConfig.class, LogbookConfiguration.class, VitamClientTestConfig.class })
public class CasServiceIntegrationTest extends AbstractMongoTests {

    private static final String CREDENTIALS_DETAILS_FILE = "credentialsRepository/userCredentials.json";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    protected InternalSecurityService internalSecurityService;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected IamLogbookService iamLogbookService;

    @InjectMocks
    private CasInternalService casService;

    @Mock
    private UserInternalService internalUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubrogationRepository subrogationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private InternalHttpContext internalHttpContext;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TokenRepository tokenRepository;

    private final PasswordValidator passwordValidator = new PasswordValidator();

    private JsonNode jsonNode;

    @BeforeEach
    public void setup() throws FileNotFoundException, InvalidParseOperationException {
        jsonNode = JsonHandler.getFromFile(PropertiesUtils.findFile(CREDENTIALS_DETAILS_FILE));
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
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(tenant));

        tokenRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    @Disabled
    public void testLogoutSubrogation() {
        final String superUser = "superUser@vitamui.com";
        final String superUserCustomerId = "superUserCustomerId";
        final String surrogate = jsonNode.findValue("EMAIL").textValue();
        String surrogateCustomerId = jsonNode.findValue("CUSTOMER_ID").textValue();
        final Subrogation subro = new Subrogation();
        subro.setSuperUser(superUser);
        subro.setSuperUserCustomerId(superUserCustomerId);
        subro.setSurrogate(surrogate);
        subro.setSurrogateCustomerId(surrogateCustomerId);
        Mockito.when(
            subrogationRepository.findBySuperUserAndSuperUserCustomerIdAndSurrogateAndSurrogateCustomerId(
                superUser,
                superUserCustomerId,
                surrogate,
                surrogateCustomerId
            )
        ).thenReturn(Optional.of(subro));
        Mockito.when(userRepository.findByEmailIgnoreCaseAndCustomerId(surrogate, surrogateCustomerId)).thenReturn(
            new User()
        );
        Mockito.when(userRepository.findByEmailIgnoreCaseAndCustomerId(superUser, superUserCustomerId)).thenReturn(
            new User()
        );
        casService.deleteSubrogationBySuperUserAndSurrogate(
            superUser,
            superUserCustomerId,
            surrogate,
            surrogateCustomerId
        );

        final Criteria criteria = Criteria.where("obId")
            .is(subro.getId())
            .and("obIdReq")
            .is(MongoDbCollections.SUBROGATIONS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_LOGOUT_SURROGATE);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    @Test
    @Disabled
    public void testGetUserByEmailWithGenericUsers() {
        final UserDto user = new UserDto();
        user.setType(UserTypeEnum.GENERIC);
        user.setId("ID");
        user.setEmail(jsonNode.findValue("EMAIL").textValue());
        user.setCustomerId(jsonNode.findValue("CUSTOMER_ID").textValue());
        final Subrogation subro = getUsersByEmail(user);

        final Criteria criteria = Criteria.where("obId")
            .is(subro.getId())
            .and("obIdReq")
            .is(MongoDbCollections.SUBROGATIONS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_START_SURROGATE_GENERIC);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    @Test
    @Disabled
    public void testGetUserByEmailWithNominativecUsers() {
        final UserDto user = new UserDto();
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setId("ID");
        user.setEmail(jsonNode.findValue("EMAIL").textValue());
        user.setCustomerId(jsonNode.findValue("CUSTOMER_ID").textValue());
        final Subrogation subro = getUsersByEmail(user);

        final Criteria criteria = Criteria.where("obId")
            .is(subro.getId())
            .and("obIdReq")
            .is(MongoDbCollections.SUBROGATIONS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_START_SURROGATE_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    private Subrogation getUsersByEmail(final UserDto user) {
        final String email = user.getEmail();
        final String customerId = user.getCustomerId();
        final AuthUserDto authUser = new AuthUserDto();
        authUser.setId("ID");
        final Subrogation subro = new Subrogation();
        subro.setSuperUser("superuser@vitamui.com");
        subro.setSuperUserCustomerId("customer_system");
        subro.setSurrogate(email);
        subro.setSurrogateCustomerId(customerId);
        Mockito.when(internalUserService.findUserByEmailAndCustomerId(email, customerId)).thenReturn(user);
        Mockito.when(internalUserService.findUsersByEmail(email)).thenReturn(List.of(user));
        Mockito.when(internalUserService.loadGroupAndProfiles(ArgumentMatchers.any())).thenReturn(authUser);
        Mockito.when(subrogationRepository.findOneBySurrogateAndSurrogateCustomerId(email, customerId)).thenReturn(
            subro
        );
        Mockito.when(userRepository.findAllByEmailIgnoreCase(email)).thenReturn(List.of(new User()));
        Mockito.when(userRepository.findByEmailIgnoreCaseAndCustomerId(email, customerId)).thenReturn(new User());
        Mockito.when(
            userRepository.findByEmailIgnoreCaseAndCustomerId("superuser@vitamui.com", "customer_system")
        ).thenReturn(new User());
        casService.getUsersByEmail(
            email,
            CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER
        );
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
        Mockito.when(
            userRepository.findByEmailIgnoreCaseAndCustomerId(
                jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("CUSTOMER_ID").textValue()
            )
        ).thenReturn(user);
        final Customer customer = new Customer();
        customer.setId(jsonNode.findValue("CUSTOMER_ID").textValue());
        customer.setPasswordRevocationDelay(
            Integer.parseInt(jsonNode.findValue("PASSWORD_EXPIRATION_DELAY").textValue())
        );
        Mockito.when(customerRepository.findById(jsonNode.findValue("CUSTOMER_ID").textValue())).thenReturn(
            Optional.of(customer)
        );

        return user;
    }

    @Test
    @Disabled
    public void testPasswordCreation() {
        final User user = prepareUserPwd(null);

        casService.updatePassword(
            jsonNode.findValue("EMAIL").textValue(),
            jsonNode.findValue("PASSWORD").textValue(),
            jsonNode.findValue("CUSTOMER_ID").textValue()
        );

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_PASSWORD_INIT);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
    }

    @Test
    @Disabled
    public void testPasswordUpdate() {
        final User user = prepareUserPwd("oldPassword");
        final String oldPassword = user.getPassword();
        final OffsetDateTime passwordExpirationDate = OffsetDateTime.now()
            .plusMonths(Integer.parseInt(jsonNode.findValue("PASSWORD_EXPIRATION_DELAY").textValue()));
        casService.updatePassword(
            jsonNode.findValue("EMAIL").textValue(),
            jsonNode.findValue("PASSWORD").textValue(),
            jsonNode.findValue("CUSTOMER_ID").textValue()
        );

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_PASSWORD_CHANGE);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        Assertions.assertNotEquals(oldPassword, user.getPassword(), "Password should not be the same");
        Assertions.assertTrue(
            user.getPasswordExpirationDate().isAfter(passwordExpirationDate),
            "Password Expiration date is not correct"
        );
    }

    @Test
    @Disabled
    public void testPasswordUpdateAlreadyUsedPassword() {
        final User user = prepareUserPwd("oldPassword");
        user.getOldPasswords().add(passwordEncoder.encode(jsonNode.findValue("PASSWORD").textValue()));

        Assertions.assertThrows(
            ConflictException.class,
            () ->
                casService.updatePassword(
                    jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue(),
                    jsonNode.findValue("CUSTOMER_ID").textValue()
                )
        );
    }

    @Test
    @Disabled
    public void testUpdateNbFailedAttempsNoSurrogate() {
        final User user = new User();
        user.setIdentifier(jsonNode.findValue("USER_IDENTIFIER").textValue());
        user.setStatus(UserStatusEnum.BLOCKED);

        casService.updateNbFailedAttempsPlusLastConnectionAndStatus(user, 4, UserStatusEnum.ENABLED);

        final Criteria criteria = Criteria.where("obId")
            .is(user.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.USERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_BLOCK_USER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(1);
        final Event event = events.iterator().next();
        assertThat(event.getEvDetData()).isEqualTo(
            "{\"diff\":{\"-Statut\":\"ENABLED\"," + "\"+Statut\":\"BLOCKED\"},\"Durée du blocage\":\"PT20M\"}"
        );
    }
}
