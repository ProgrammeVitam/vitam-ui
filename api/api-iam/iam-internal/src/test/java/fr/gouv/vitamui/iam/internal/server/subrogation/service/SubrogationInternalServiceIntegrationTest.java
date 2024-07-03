package fr.gouv.vitamui.iam.internal.server.subrogation.service;

import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Class for test InternalProfileService with a real repository
 */

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class SubrogationInternalServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private SubrogationInternalService service;

    @Autowired
    private SubrogationRepository repository;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SequenceGeneratorService sequenceGeneratorService;

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private final InternalHttpContext internalHttpContext = mock(InternalHttpContext.class);

    @Autowired
    private IamLogbookService iamLogbookService;

    @MockBean
    private UserInternalService userInternalService;

    @MockBean
    private GroupInternalService groupInternalService;

    @MockBean
    private ProfileRepository profilRepository;

    @Autowired
    private SubrogationConverter subrogationConverter;

    @MockBean
    private TenantRepository tenantRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        service = new SubrogationInternalService(
            sequenceGeneratorService,
            repository,
            userRepository,
            userInternalService,
            groupInternalService,
            groupRepository,
            profilRepository,
            internalSecurityService,
            customerRepository,
            subrogationConverter,
            iamLogbookService
        );

        Tenant tenant = new Tenant();
        tenant.setIdentifier(10);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(tenant));
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
    }

    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testDeclineSubrogation() {
        final String superUserEmail = "sub-roggator@vitamui.com";
        final String superUserCustomerId = "surrogate_system";

        final String currentUserEmail = "surrogate@vitamui.com";
        final String currentUserCustomerId = "surrogate_customer";
        final Subrogation subro = buildSubro();
        subro.setStatus(SubrogationStatusEnum.CREATED);
        subro.setSurrogate(currentUserEmail);
        subro.setSurrogateCustomerId(currentUserCustomerId);
        subro.setSuperUser(superUserEmail);
        subro.setSuperUserCustomerId(superUserCustomerId);

        repository.save(subro);
        final AuthUserDto currentUser = new AuthUserDto();
        currentUser.setEmail(currentUserEmail);
        currentUser.setCustomerId(currentUserCustomerId);
        Mockito.when(
            userRepository.findByEmailIgnoreCaseAndCustomerId(currentUserEmail, currentUserCustomerId)
        ).thenReturn(new User());
        Mockito.when(userRepository.findByEmailIgnoreCaseAndCustomerId(superUserEmail, superUserCustomerId)).thenReturn(
            new User()
        );
        Mockito.when(internalSecurityService.getUser()).thenReturn(currentUser);
        service.decline(subro.getId());

        final Criteria subroCriteriaCreation = Criteria.where("obId")
            .is(subro.getId())
            .and("obIdReq")
            .is(MongoDbCollections.SUBROGATIONS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_DECLINE_SURROGATE);
        final Optional<Event> evSubroDeclined = eventRepository.findOne(Query.query(subroCriteriaCreation));
        assertThat(evSubroDeclined).isPresent();
    }

    @Test
    public void testStopSubrogation() {
        final String superUserEmail = "sub-roggator@vitamui.com";
        final String superUserCustomerId = "surrogate_system";

        final String currentUserEmail = "surrogate@vitamui.com";
        final String currentUserCustomerId = "surrogate_customer";
        final Subrogation subro = buildSubro();
        subro.setStatus(SubrogationStatusEnum.ACCEPTED);
        subro.setSurrogate(currentUserEmail);
        subro.setSurrogateCustomerId(currentUserCustomerId);
        subro.setSuperUser(superUserEmail);
        subro.setSuperUserCustomerId(superUserCustomerId);

        repository.save(subro);
        final AuthUserDto currentUser = new AuthUserDto();
        currentUser.setEmail(currentUserEmail);
        currentUser.setCustomerId(currentUserCustomerId);
        Mockito.when(
            userRepository.findByEmailIgnoreCaseAndCustomerId(currentUserEmail, currentUserCustomerId)
        ).thenReturn(new User());
        Mockito.when(userRepository.findByEmailIgnoreCaseAndCustomerId(superUserEmail, superUserCustomerId)).thenReturn(
            new User()
        );
        Mockito.when(internalSecurityService.getUser()).thenReturn(currentUser);
        service.decline(subro.getId());

        final Criteria subroCriteriaCreation = Criteria.where("obId")
            .is(subro.getId())
            .and("obIdReq")
            .is(MongoDbCollections.SUBROGATIONS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_STOP_SURROGATE);
        final Optional<Event> evSubroDeclined = eventRepository.findOne(Query.query(subroCriteriaCreation));
        assertThat(evSubroDeclined).isPresent();
    }

    private Subrogation buildSubro() {
        final Subrogation subro = new Subrogation();
        subro.setDate(new Date());
        subro.setId("id");
        subro.setStatus(SubrogationStatusEnum.CREATED);
        subro.setSurrogate("surrogate@vitamui.com");
        subro.setSuperUser("superUser@vitamui.com");
        return subro;
    }
}
