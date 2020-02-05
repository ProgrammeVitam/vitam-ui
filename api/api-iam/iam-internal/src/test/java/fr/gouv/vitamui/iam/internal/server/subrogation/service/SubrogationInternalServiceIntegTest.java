package fr.gouv.vitamui.iam.internal.server.subrogation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;

/**
 * Class for test InternalProfileService with a real repository
 *
 */

@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = {
        SubrogationRepository.class }, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class SubrogationInternalServiceIntegTest extends AbstractLogbookIntegrationTest {

    private SubrogationInternalService service;

    @Autowired
    private SubrogationRepository repository;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private UserRepository userRepository;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SubrogationInternalServiceIntegTest.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private final InternalHttpContext internalHttpContext = mock(InternalHttpContext.class);

    @Autowired
    private IamLogbookService iamLogbookService;

    @MockBean
    private OwnerRepository ownerRepository;

    @MockBean
    private SpMetadataGenerator spMetadataGenerator;

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

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        service = new SubrogationInternalService(sequenceRepository, repository, userRepository, userInternalService,
                groupInternalService, groupRepository, profilRepository, internalSecurityService, customerRepository,
                subrogationConverter, iamLogbookService);

        Tenant tenant = new Tenant();
        tenant.setIdentifier(10);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class)))
                .thenReturn(Optional.ofNullable(tenant));
        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testDeclineSubrogation() {
        final String currentUserEmail = "surrogate@vitamui.com";
        final Subrogation subro = buildSubro();
        subro.setStatus(SubrogationStatusEnum.CREATED);
        subro.setSurrogate(currentUserEmail);

        repository.save(subro);
        final AuthUserDto currentUser = new AuthUserDto();
        currentUser.setEmail("surrogate@vitamui.com");
        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString())).thenReturn(new User());
        Mockito.when(internalSecurityService.getUser()).thenReturn(currentUser);
        service.decline(subro.getId());

        final Criteria subroCriteriaCreation = Criteria.where("obId").is("" + subro.getId()).and("obIdReq")
                .is(MongoDbCollections.SUBROGATIONS).and("evType").is(EventType.EXT_VITAMUI_DECLINE_SURROGATE);
        final Optional<Event> evSubroDeclined = eventRepository.findOne(Query.query(subroCriteriaCreation));
        assertThat(evSubroDeclined).isPresent();
    }

    @Test
    public void testStopSubrogation() {
        final String currentUserEmail = "surrogate@vitamui.com";
        final Subrogation subro = buildSubro();
        subro.setStatus(SubrogationStatusEnum.ACCEPTED);
        subro.setSurrogate(currentUserEmail);

        repository.save(subro);
        final AuthUserDto currentUser = new AuthUserDto();
        currentUser.setEmail("surrogate@vitamui.com");
        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString())).thenReturn(new User());
        Mockito.when(internalSecurityService.getUser()).thenReturn(currentUser);
        service.decline(subro.getId());

        final Criteria subroCriteriaCreation = Criteria.where("obId").is("" + subro.getId()).and("obIdReq")
                .is(MongoDbCollections.SUBROGATIONS).and("evType").is(EventType.EXT_VITAMUI_STOP_SURROGATE);
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
