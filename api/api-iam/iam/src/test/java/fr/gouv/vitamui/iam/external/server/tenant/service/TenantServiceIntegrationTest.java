package fr.gouv.vitamui.iam.external.server.tenant.service;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.VitamConfigurationDto;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.external.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.external.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.external.server.configuration.ConfigurationService;
import fr.gouv.vitamui.iam.external.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.external.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.external.server.customer.service.CustomerService;
import fr.gouv.vitamui.iam.external.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.external.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.external.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.external.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.external.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.external.server.group.service.GroupService;
import fr.gouv.vitamui.iam.external.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.external.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.external.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.external.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.external.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.external.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.external.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.external.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.external.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.external.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.external.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.external.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.external.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.external.server.user.service.UserService;
import fr.gouv.vitamui.iam.external.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Class for test InternalTenantService with a real repository
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class TenantServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private static final String USER_TOKEN = "userToken";

    private static final String USER_LEVEL = "userLevel";

    private static final String IDENTITY = "identity";

    private static final String REQUEST_ID = "requestId";

    private static final String ACCESS_CONTRACT = "accessContract";

    private static final String NEW_NAME = "newName";

    private static final String NEW_OWNER_ID = "newOwnerId";

    private TenantService service;

    private ProfileService profileService;

    @Autowired
    private TenantRepository repository;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @Autowired
    private TenantConverter tenantConverter;

    @Autowired
    private GroupConverter groupConverter;

    @Autowired
    private ProfileConverter profileConverter;

    @Autowired
    private IamLogbookService iamLogbookService;

    @Mock
    private CustomerService customerService;

    @Mock
    private OwnerService ownerService;

    @Mock
    private CustomerRepository customerRepository;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private ProfileRepository profileRepository;

    @Mock
    private UserService userService;

    @MockBean
    private OwnerRepository ownerRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private InitVitamTenantService initVitamTenantService;

    @MockBean
    private LogbookService logbookService;

    @MockBean
    private CustomerInitConfig customerInitConfig;

    @MockBean
    private ExternalParametersRepository externalParametersRepository;

    @MockBean
    private ExternalParametersService externalParametersService;

    @MockBean
    private ConfigurationService configurationService;

    @BeforeEach
    public void setup() {
        GroupService groupService = new GroupService(
            new SequenceGeneratorService(sequenceRepository),
            groupRepository,
            customerRepository,
            profileService,
            userRepository,
            externalSecurityService,
            repository,
            iamLogbookService,
            groupConverter,
            null,
            null
        );

        profileService = new ProfileService(
            new SequenceGeneratorService(sequenceRepository),
            profileRepository,
            customerRepository,
            groupRepository,
            repository,
            userRepository,
            externalSecurityService,
            iamLogbookService,
            profileConverter,
            null,
            customerInitConfig
        );

        repository.deleteAll();
        service = new TenantService(
            new SequenceGeneratorService(sequenceRepository),
            repository,
            customerRepository,
            ownerRepository,
            profileRepository,
            groupService,
            userService,
            ownerService,
            externalSecurityService,
            iamLogbookService,
            tenantConverter,
            initVitamTenantService,
            logbookService,
            customerInitConfig,
            externalParametersRepository,
            externalParametersService,
            configurationService
        );

        Mockito.reset(customerService);

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setName(SequencesConstants.TENANT_IDENTIFIER);
        customSequence.setSequence(1);
        sequenceRepository.save(customSequence);

        final CustomSequence customSequence2 = new CustomSequence();
        customSequence2.setName(SequencesConstants.PROFILE_IDENTIFIER);
        customSequence2.setSequence(1);
        sequenceRepository.save(customSequence2);

        final CustomSequence customSequence3 = new CustomSequence();
        customSequence3.setName(SequencesConstants.GROUP_IDENTIFIER);
        customSequence3.setSequence(1);
        sequenceRepository.save(customSequence3);

        groupService.getNextSequenceId(SequencesConstants.GROUP_IDENTIFIER);
        service.getNextSequenceId(SequencesConstants.TENANT_IDENTIFIER);

        when(externalSecurityService.getCustomerId()).thenReturn(IamServerUtilsTest.CUSTOMER_ID);
        when(externalSecurityService.getTenantIdentifier()).thenReturn(IamServerUtilsTest.TENANT_IDENTIFIER);
        when(externalSecurityService.hasRole(eq(ServicesData.ROLE_GET_ALL_TENANTS))).thenReturn(true);
    }

    @AfterEach
    public void cleanUp() {
        eventRepository.deleteAll();
    }

    @Test
    public void testFindByNames() {
        repository.save(IamServerUtilsTest.buildTenant("id1", "test", 1));
        repository.save(IamServerUtilsTest.buildTenant("id1", "test contains string", 1));

        final QueryDto criteria = QueryDto.criteria("name", "cont", CriterionOperator.CONTAINSIGNORECASE);
        final List<TenantDto> result = service.getAll(criteria);
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    public void testCheckExist() {
        final CustomerDto customer = IamServerUtilsTest.buildCustomerDto();

        repository.save(IamServerUtilsTest.buildTenant("id1", "test", 1));
        repository.save(IamServerUtilsTest.buildTenant("id2", "test contains string", 2));

        QueryDto criteria = QueryDto.criteria("identifier", 1, CriterionOperator.EQUALS);
        Assertions.assertTrue(service.checkExist(criteria.toJson()));

        criteria = QueryDto.criteria("identifier", 2, CriterionOperator.EQUALS);
        Assertions.assertTrue(service.checkExist(criteria.toJson()));

        criteria = QueryDto.criteria("customerId", customer.getId(), CriterionOperator.EQUALS);
        Assertions.assertTrue(service.checkExist(criteria.toJson()));
    }

    @Test
    public void testCreatePatch() {
        final Owner owner = IamServerUtilsTest.buildOwner();
        owner.setIdentifier("identifier_" + owner.getId());

        when(groupRepository.findOne(any(Query.class))).thenReturn(Optional.of(IamServerUtilsTest.buildGroup()));
        when(customerRepository.findById(IamServerUtilsTest.CUSTOMER_ID)).thenReturn(
            Optional.of(IamServerUtilsTest.buildCustomer())
        );
        when(ownerRepository.findById(IamServerUtilsTest.OWNER_ID)).thenReturn(Optional.of(owner));
        final Profile profile = IamServerUtilsTest.buildProfile();
        profile.setIdentifier("1");
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        final ExternalHttpContext externalHttpContext = new ExternalHttpContext(
            IamServerUtilsTest.TENANT_IDENTIFIER,
            USER_TOKEN,
            IamServerUtilsTest.CUSTOMER_ID,
            IDENTITY,
            REQUEST_ID,
            ACCESS_CONTRACT
        );
        when(externalSecurityService.getHttpContext()).thenReturn(externalHttpContext);
        when(externalSecurityService.getLevel()).thenReturn("");
        when(externalSecurityService.getUser()).thenReturn(new AuthUserDto());
        when(userService.getDefaultAdminUser(IamServerUtilsTest.CUSTOMER_ID)).thenReturn(
            IamServerUtilsTest.buildUserDto()
        );
        Mockito.when(externalSecurityService.getProofTenantIdentifier()).thenReturn(10001);
        when(externalParametersRepository.findByIdentifier(Mockito.anyString())).thenReturn(
            Optional.of(buildExternalParameter())
        );
        Integer someTenantId = 10001;

        final Tenant tenantProof = new Tenant();
        tenantProof.setCustomerId(IamServerUtilsTest.CUSTOMER_ID);
        tenantProof.setIdentifier(someTenantId);
        tenantProof.setEnabled(true);
        tenantProof.setProof(true);
        tenantProof.setName("proof tenant");
        repository.save(tenantProof);

        VitamConfigurationDto vitamConfigurationDto = new VitamConfigurationDto();
        vitamConfigurationDto.setTenants(List.of(someTenantId + 1));
        Mockito.when(configurationService.getVitamPublicConfigurations()).thenReturn(vitamConfigurationDto);

        TenantDto tenant = IamServerUtilsTest.buildTenantDto();
        tenant.setId(null);
        tenant.setIdentifier(someTenantId + 1);

        tenant = service.create(tenant);

        final Criteria tenantCriteriaCreation = Criteria.where("obId")
            .is("" + tenant.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.TENANTS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_TENANT);
        final Optional<Event> evTenantCreation = eventRepository.findOne(Query.query(tenantCriteriaCreation));
        assertThat(evTenantCreation).isPresent();
        final Criteria profileCriteria = Criteria.where("obIdReq")
            .is(MongoDbCollections.PROFILES)
            .and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_PROFILE);
        final List<Event> evProfileCreation = eventRepository.findAll(Query.query(profileCriteria));
        assertThat(evProfileCreation).isNotNull().isNotEmpty().hasSize(2);
        final Criteria groupUpdateCriteria = Criteria.where("obId")
            .is("" + IamServerUtilsTest.GROUP_IDENTIFIER)
            .and("obIdReq")
            .is(MongoDbCollections.GROUPS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_UPDATE_GROUP);
        final List<Event> evGroupUpdate = eventRepository.findAll(Query.query(groupUpdateCriteria));
        assertThat(evGroupUpdate).isNotNull().isNotEmpty().hasSize(1);

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", tenant.getId());
        partialDto.put("enabled", false);
        partialDto.put("name", NEW_NAME);
        partialDto.put("ownerId", NEW_OWNER_ID);

        when(ownerRepository.findById(NEW_OWNER_ID)).thenReturn(Optional.of(owner));
        final OwnerDto oldOwner = IamServerUtilsTest.buildOwnerDto();
        oldOwner.setIdentifier("identifier_" + oldOwner.getId());
        when(ownerService.getOne(IamServerUtilsTest.OWNER_ID, Optional.empty())).thenReturn(oldOwner);
        final OwnerDto newOwner = IamDtoBuilder.buildOwnerDto(
            NEW_OWNER_ID,
            IamServerUtilsTest.OWNER_NAME,
            IamServerUtilsTest.CUSTOMER_ID
        );
        newOwner.setIdentifier("identifier_" + newOwner.getId());
        when(ownerService.getOne(NEW_OWNER_ID, Optional.empty())).thenReturn(newOwner);

        tenant = service.patch(partialDto);

        final Criteria tenantCriteriaUpdate = Criteria.where("obId")
            .is("" + tenant.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.TENANTS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_UPDATE_TENANT);
        final Optional<Event> evTenantUpdate = eventRepository.findOne(Query.query(tenantCriteriaUpdate));
        assertThat(evTenantUpdate).isPresent();
        assertThat(evTenantUpdate.get().getEvDetData()).isEqualTo(
            "{\"diff\":{\"-Nom\":\"tenantName\"," +
            "\"+Nom\":\"" +
            NEW_NAME +
            "\"," +
            "\"-Identifiant du propriétaire\":\"identifier_ownerId\"," +
            "\"+Identifiant du propriétaire\":\"identifier_" +
            NEW_OWNER_ID +
            "\"," +
            "\"-Activé\":\"true\"," +
            "\"+Activé\":\"false\"" +
            "}}"
        );
    }

    public ExternalParameters buildExternalParameter() {
        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setIdentifier("identifierdefault_ac_customerId");
        externalParameters.setName("identifierdefault_ac_customerId");
        return externalParameters;
    }
}
