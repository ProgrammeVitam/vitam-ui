package fr.gouv.vitamui.iam.server.group.service;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.server.common.ApiIamConstants;
import fr.gouv.vitamui.iam.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class GroupServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private static final String ID = "ID";

    private static final String CUSTOMER_ID = "CUSTOMER_ID";

    private static final String LEVEL = "LEVEL";

    private GroupService service;

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private final ProfileService profileService = mock(ProfileService.class);

    @Autowired
    private GroupRepository repository;

    @Autowired
    private GroupConverter groupConverter;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Mock
    private HttpContext httpContext;

    @MockitoBean
    private TenantRepository tenantRepository;

    @BeforeEach
    public void setup() {
        service = new GroupService(
            new SequenceGeneratorService(sequenceRepository),
            repository,
            customerRepository,
            profileService,
            userRepository,
            securityService,
            tenantRepository,
            iamLogbookService,
            groupConverter,
            null,
            null
        );

        repository.deleteAll();

        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(new Tenant()));

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setName(SequencesConstants.GROUP_IDENTIFIER);
        sequenceRepository.save(customSequence);

        service.getNextSequenceId(SequencesConstants.GROUP_IDENTIFIER);

        when(securityService.userIsRootLevel()).thenReturn(true);
        when(securityService.getCustomerId()).thenReturn(CUSTOMER_ID);
        when(securityService.getTenantIdentifier()).thenReturn(10);
        when(securityService.hasRole(eq(ServicesData.ROLE_GET_ALL_TENANTS))).thenReturn(true);
    }

    @Test
    public void testCheckExistByCustomerIdAndName() {
        final AuthUserDto mainUserDto = IamDtoBuilder.buildAuthUserDto("userId", "test@vitamui.com", CUSTOMER_ID);
        mainUserDto.setLevel(ApiIamConstants.ADMIN_LEVEL);
        Mockito.when(securityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(securityService.getUser()).thenReturn(mainUserDto);
        Mockito.when(securityService.getLevel()).thenReturn(ApiIamConstants.ADMIN_LEVEL);

        repository.save(IamServerUtilsTest.buildGroup(ID, "identifier", "nameknow", CUSTOMER_ID));

        QueryDto criteria = QueryDto.criteria()
            .addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS)
            .addCriterion("name", "nameknow", CriterionOperator.EQUALS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = QueryDto.criteria()
            .addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS)
            .addCriterion("name", "nameunknow", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = QueryDto.criteria().addCriterion("name", "nameunknow", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();
    }

    @Test
    public void testGetSubLevels() {
        repository.save(IamServerUtilsTest.buildGroup(ID, "id1", "nametest", CUSTOMER_ID, "EDF.RH"));
        repository.save(IamServerUtilsTest.buildGroup("id25", "id2", "nametest25", CUSTOMER_ID, "EDF.RH.PARIS"));
        repository.save(IamServerUtilsTest.buildGroup("id30", "id3", "nametest30", CUSTOMER_ID, "EDF.INFRA"));
        repository.save(IamServerUtilsTest.buildGroup("id35", "id4", "nametest35", CUSTOMER_ID, "EDF.MARKET"));
        repository.save(IamServerUtilsTest.buildGroup("id36", "id5", "nametest36", CUSTOMER_ID, "EDF"));
        repository.save(
            IamServerUtilsTest.buildGroup("idothercustomerid", "id2", "nametest35", "other_customer_id", "EDF.VITAMUI")
        );

        List<String> levels = service.getSubLevels("EDF", CUSTOMER_ID);
        assertThat(levels).containsOnly("EDF.RH.PARIS", "EDF.RH", "EDF.MARKET", "EDF.INFRA");

        levels = service.getSubLevels("EDF", "other_customer_id");
        assertThat(levels).containsOnly("EDF.VITAMUI");
    }

    @Test
    public void testCheckExist() {
        repository.save(IamServerUtilsTest.buildGroup(ID, "id1", "name", CUSTOMER_ID, LEVEL));
        repository.save(IamServerUtilsTest.buildGroup("idLevel", "id2", "nameLevel", CUSTOMER_ID, LEVEL));
        repository.save(
            IamServerUtilsTest.buildGroup("idAdmin", "id3", "nameAdmin", CUSTOMER_ID, ApiIamConstants.ADMIN_LEVEL)
        );
        repository.save(IamServerUtilsTest.buildGroup("idSubLvl", "id4", "nameSubLvl", CUSTOMER_ID, LEVEL + ".SUB"));

        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto("userId", "test@vitamui.com", CUSTOMER_ID);
        userDto.setLevel(LEVEL);
        userDto.setCustomerId(CUSTOMER_ID);
        userDto.setGroupId(ID);

        Mockito.when(securityService.getUser()).thenReturn(userDto);
        Mockito.when(securityService.getLevel()).thenReturn(LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("id", ID, CriterionOperator.EQUALS);
        criteria.addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS);
        criteria.addCriterion("level", LEVEL, CriterionOperator.EQUALS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idLevel", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idAdmin", CriterionOperator.EQUALS);
        criteria.addCriterion("level", ApiIamConstants.ADMIN_LEVEL, CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idUnknown", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idSubLvl", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    @Test
    public void testCheckExistAdminUser() {
        final AuthUserDto mainUserDto = IamDtoBuilder.buildAuthUserDto("userId", "test@vitamui.com", CUSTOMER_ID);
        mainUserDto.setLevel(ApiIamConstants.ADMIN_LEVEL);
        mainUserDto.setCustomerId(CUSTOMER_ID);

        repository.save(IamServerUtilsTest.buildGroup(ID, "id1", "name", CUSTOMER_ID, LEVEL));
        repository.save(
            IamServerUtilsTest.buildGroup("idAdmin", "id2", "nameAdmin", CUSTOMER_ID, ApiIamConstants.ADMIN_LEVEL)
        );

        Mockito.when(securityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(securityService.getUser()).thenReturn(mainUserDto);
        Mockito.when(securityService.getLevel()).thenReturn(ApiIamConstants.ADMIN_LEVEL);

        QueryDto criteria = new QueryDto();
        criteria.addCriterion("id", ID, CriterionOperator.EQUALS);
        criteria.addCriterion("customerId", CUSTOMER_ID, CriterionOperator.EQUALS);
        criteria.addCriterion("level", LEVEL, CriterionOperator.EQUALS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idunknown", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();

        criteria = new QueryDto();
        criteria.addCriterion("id", "idAdmin", CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();
    }

    @Test
    public void testCreateGroup() {
        final GroupDto group = createGroup();
        assertThat(group.getIdentifier()).isNotBlank();

        final Criteria criteria = Criteria.where("obId")
            .is(group.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.GROUPS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_GROUP);
        final Optional<Event> ev = eventRepository.findOne(Query.query(criteria));
        assertThat(ev).isPresent();
    }

    @Test
    public void testPatch() {
        final GroupDto group = createGroup();

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("customerId", group.getCustomerId());
        partialDto.put("id", group.getId());

        partialDto.put("name", "nameTest");
        service.patch(partialDto);
        partialDto.remove("name");

        partialDto.put("description", "descriptionTest");
        service.patch(partialDto);
        partialDto.remove("description");

        partialDto.put("profileIds", group.getProfileIds());
        service.patch(partialDto);
        partialDto.remove("profileIds");

        partialDto.put("enabled", false);
        service.patch(partialDto);
        partialDto.remove("enabled");

        final Criteria criteria = Criteria.where("obId")
            .is(group.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.GROUPS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_UPDATE_GROUP);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(4);
    }

    private GroupDto createGroup() {
        final String customerId = "customerId";
        final Integer tenantIdentifier = 10;
        final Tenant tenant = new Tenant();
        tenant.setIdentifier(tenantIdentifier);
        tenant.setCustomerId(customerId);
        tenant.setEnabled(true);
        final ProfileDto profile = IamServerUtilsTest.buildProfileDto();
        profile.setCustomerId(customerId);
        profile.setTenantIdentifier(tenantIdentifier);

        final GroupDto groupDto = IamServerUtilsTest.buildGroupDto(
            null,
            "nameTest",
            customerId,
            Arrays.asList(profile.getId())
        );
        groupDto.setIdentifier(null);
        final Customer customer = new Customer();
        customer.setEnabled(true);
        customer.setId(customerId);

        Mockito.when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        Mockito.when(securityService.isLevelAllowed(any())).thenReturn(true);
        Mockito.when(securityService.getCustomerId()).thenReturn(customerId);
        Mockito.when(securityService.getHttpContext()).thenReturn(httpContext);
        Mockito.when(profileService.getMany(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
            List.of(profile)
        );
        Mockito.when(tenantRepository.findByIdentifier(ArgumentMatchers.any())).thenReturn(tenant);

        return service.create(groupDto);
    }
}
