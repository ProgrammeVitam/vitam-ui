package fr.gouv.vitamui.iam.internal.server.tenant.service;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.VitamConfigurationDto;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.configuration.ConfigurationInternalService;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.service.CustomerInternalService;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
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

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Class for test InternalTenantService with a real repository
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class TenantInternalServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private static final String USER_TOKEN = "userToken";

    private static final String USER_LEVEL = "userLevel";

    private static final String IDENTITY = "identity";

    private static final String REQUEST_ID = "requestId";

    private static final String ACCESS_CONTRACT = "accessContract";

    private static final String NEW_NAME = "newName";

    private static final String NEW_OWNER_ID = "newOwnerId";

    private TenantInternalService service;

    private ProfileInternalService internalProfileService;

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
    private CustomerInternalService internalCustomerService;

    @Mock
    private OwnerInternalService internalOwnerService;

    @Mock
    private CustomerRepository customerRepository;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private ProfileRepository profileRepository;

    @Mock
    private UserInternalService internalUserService;

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
    private ExternalParametersInternalService externalParametersInternalService;

    @MockBean
    private ConfigurationInternalService configurationInternalService;

    @BeforeEach
    public void setup() {
        GroupInternalService internalGroupService = new GroupInternalService(
            new SequenceGeneratorService(sequenceRepository),
            groupRepository,
            customerRepository,
            internalProfileService,
            userRepository,
            internalSecurityService,
            repository,
            iamLogbookService,
            groupConverter,
            null,
            null
        );

        internalProfileService = new ProfileInternalService(
            new SequenceGeneratorService(sequenceRepository),
            profileRepository,
            customerRepository,
            groupRepository,
            repository,
            userRepository,
            internalSecurityService,
            iamLogbookService,
            profileConverter,
            null,
            customerInitConfig
        );

        repository.deleteAll();
        service = new TenantInternalService(
            new SequenceGeneratorService(sequenceRepository),
            repository,
            customerRepository,
            ownerRepository,
            groupRepository,
            profileRepository,
            userRepository,
            internalGroupService,
            internalUserService,
            internalOwnerService,
            internalProfileService,
            internalSecurityService,
            iamLogbookService,
            tenantConverter,
            initVitamTenantService,
            logbookService,
            customerInitConfig,
            externalParametersRepository,
            externalParametersInternalService,
            configurationInternalService
        );

        Mockito.reset(internalCustomerService);

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

        internalGroupService.getNextSequenceId(SequencesConstants.GROUP_IDENTIFIER);
        service.getNextSequenceId(SequencesConstants.TENANT_IDENTIFIER);
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
        final InternalHttpContext internalHttpContext = new InternalHttpContext(
            IamServerUtilsTest.TENANT_IDENTIFIER,
            USER_TOKEN,
            IamServerUtilsTest.CUSTOMER_ID,
            USER_LEVEL,
            APPLICATION_ID,
            IDENTITY,
            REQUEST_ID,
            ACCESS_CONTRACT
        );
        when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
        when(internalSecurityService.getLevel()).thenReturn("");
        when(internalSecurityService.getUser()).thenReturn(new AuthUserDto());
        when(internalUserService.getDefaultAdminUser(IamServerUtilsTest.CUSTOMER_ID)).thenReturn(
            IamServerUtilsTest.buildUserDto()
        );
        Mockito.when(internalSecurityService.getProofTenantIdentifier()).thenReturn(10001);
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
        Mockito.when(configurationInternalService.getVitamPublicConfigurations()).thenReturn(vitamConfigurationDto);

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
        when(internalOwnerService.getOne(IamServerUtilsTest.OWNER_ID, Optional.empty())).thenReturn(oldOwner);
        final OwnerDto newOwner = IamDtoBuilder.buildOwnerDto(
            NEW_OWNER_ID,
            IamServerUtilsTest.OWNER_NAME,
            IamServerUtilsTest.CUSTOMER_ID
        );
        newOwner.setIdentifier("identifier_" + newOwner.getId());
        when(internalOwnerService.getOne(NEW_OWNER_ID, Optional.empty())).thenReturn(newOwner);

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
