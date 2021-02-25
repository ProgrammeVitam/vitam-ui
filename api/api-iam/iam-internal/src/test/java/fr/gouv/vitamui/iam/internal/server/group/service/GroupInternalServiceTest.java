package fr.gouv.vitamui.iam.internal.server.group.service;

import fr.gouv.vitamui.commons.api.domain.*;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.utils.IamUtils;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupInternalServiceTest {

    private GroupInternalService internalGroupService;

    private final GroupRepository groupRepository = mock(GroupRepository.class);

    private final ProfileInternalService profileInternalService = mock(ProfileInternalService.class);

    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    private final UserRepository userRepository = mock(UserRepository.class);

    private final TenantRepository tenantRepository = mock(TenantRepository.class);

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private static final String LEVEL = "DSI.DEV";

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final IamLogbookService iamLogbookService = mock(IamLogbookService.class);

    private final ProfileRepository profileRepository = mock(ProfileRepository.class);

    private final GroupConverter groupConverter = new GroupConverter(profileRepository);

    @Before
    public void setup() {
        internalGroupService = new GroupInternalService(sequenceRepository, groupRepository, customerRepository,
                profileInternalService, userRepository, internalSecurityService, tenantRepository, iamLogbookService,
                groupConverter, null);
        final Tenant tenant = new Tenant();
        tenant.setCustomerId("customerId");
        when(tenantRepository.findByIdentifier(any())).thenReturn(tenant);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void testGetAll() {
        final Group group = IamServerUtilsTest.buildGroup();
        final List<Group> groups = Arrays.asList(group);
        when(groupRepository.findAll(any(Query.class))).thenReturn(groups);

        wireInternalSecurityServerCalls();

        final QueryDto criteria = QueryDto.criteria("name", "cont", CriterionOperator.CONTAINSIGNORECASE);

        final List<GroupDto> result = internalGroupService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assert.assertNotNull("Groups should be returned.", result);
        Assert.assertEquals("Groups size should be returned.", groups.size(), result.size());
    }

    @Test
    public void testGetPaginatedValues() {
        final Group group = IamServerUtilsTest.buildGroup();

        final PaginatedValuesDto<Group> data = new PaginatedValuesDto<>(Arrays.asList(group), 0, 5, false);
        when(groupRepository.getPaginatedValues(any(), any(), any(), any(), any())).thenReturn(data);

        wireInternalSecurityServerCalls();

        final PaginatedValuesDto<GroupDto> result = internalGroupService.getAllPaginated(Integer.valueOf(0),
                Integer.valueOf(5), Optional.empty(), Optional.empty(), Optional.of(DirectionDto.ASC));
        Assert.assertNotNull("Groups should be returned.", result);
        Assert.assertNotNull("Groups should be returned.", result.getValues());
        Assert.assertEquals("Groups size should be returned.", 1, result.getValues().size());
        Assert.assertEquals("Groups size should be returned.", 0, result.getPageNum());
        Assert.assertEquals("Groups size should be returned.", 5, result.getPageSize());
        Assert.assertEquals("Groups size should be returned.", false, result.isHasMore());
    }

    private void wireInternalSecurityServerCalls() {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);

        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(internalSecurityService.getUser()).thenReturn(user);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);
    }

    @Test
    public void testProcessPatch() {
        final Group entity = buildGroup();
        final Group other = IamServerUtilsTest.buildGroup();

        final Map<String, Object> partialDto = TestUtils.getMapFromObject(other);

        internalGroupService.processPatch(entity, partialDto);

        entity.setId(other.getId());
        entity.setCustomerId(other.getCustomerId());

        assertThat(entity).isEqualToComparingFieldByField(other);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBeforePatchStatusFailed() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "id");
        partialDto.put("customerId", "customerId");
        partialDto.put("enabled", false);
        partialDto.put("name", "name");
        partialDto.put("description", "description");

        final Group group = IamServerUtilsTest.buildGroup();

        when(groupRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(group));
        when(userRepository.countByGroupId(any())).thenReturn(1l);
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);

        final Group entity = internalGroupService.beforePatch(partialDto);
        assertThat(entity.getName()).isEqualTo("name");
        assertThat(entity.getDescription()).isEqualTo("description");
        assertThat(entity.getProfileIds()).containsExactly("id1", "id2");
    }

    @Test
    public void testProcessPatchStatus() {
        final Group group = buildGroup();

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("enabled", false);
        partialDto.put("name", "name");
        partialDto.put("description", "description");
        partialDto.put("profileIds", Arrays.asList("id1", "id2"));

        when(userRepository.countByGroupId(any())).thenReturn(0l);
        internalGroupService.processPatch(group, partialDto);

        assertThat(group.getName()).isEqualTo("name");
        assertThat(group.getDescription()).isEqualTo("description");
        assertThat(group.getProfileIds()).containsExactly("id1", "id2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBeforePatchFailed() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("name", "name");
        partialDto.put("description", "description");
        partialDto.put("profileIds", Arrays.asList("id1", "id2"));
        when(profileInternalService.getOne(any(), any(), any())).thenThrow(NotFoundException.class);
        internalGroupService.beforePatch(partialDto);
    }

    @Test
    public void testBeforePatchSuccess() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "id");
        partialDto.put("customerId", "customerId");
        partialDto.put("name", "name");
        partialDto.put("description", "description");
        partialDto.put("profileIds", Arrays.asList("id1", "id2"));

        when(profileInternalService.getMany(any(), any()))
        .thenReturn(Arrays.asList(buildProfileDto("id1", "app1"), buildProfileDto("id2", "app2")));
        when(groupRepository.findById(any())).thenReturn(Optional.of(buildGroup()));
        when(groupRepository.findByIdAndCustomerId(any(), any())).thenReturn(Optional.of(buildGroup()));
        when(internalSecurityService.isLevelAllowed(any())).thenCallRealMethod();
        when(internalSecurityService.getLevel()).thenReturn("DSI");
        when(internalSecurityService.getCustomerId()).thenReturn(buildCustomerDto().getId());

        internalGroupService.beforePatch(partialDto);
    }

    @Test
    public void testInternalConvertFromEntityToDto() {
        final Tenant tenant1 = new Tenant();
        tenant1.setName("Tenant1");

        final ProfileDto profile1 = IamServerUtilsTest.buildProfileDto();
        profile1.setName("Profile1");
        profile1.setTenantIdentifier(1);
        profile1.setTenantName(tenant1.getName());

        final Tenant tenant2 = new Tenant();
        tenant2.setName("Tenant2");

        final ProfileDto profile2 = IamServerUtilsTest.buildProfileDto();
        profile2.setName("Profile2");
        profile2.setTenantIdentifier(2);
        profile2.setTenantName(tenant2.getName());

        final Group group = IamServerUtilsTest.buildGroup();

        when(profileInternalService.getMany(any(), any())).thenReturn(Arrays.asList(profile1, profile2));
        when(groupRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(group));
        Mockito.when(internalSecurityService.userIsRootLevel()).thenReturn(true);
        when(tenantRepository.findByIdentifier(1)).thenReturn(tenant1);
        when(tenantRepository.findByIdentifier(2)).thenReturn(tenant2);

        final GroupDto groupDto = internalGroupService.getOne(group.getId(), Optional.empty(),
                IamUtils.buildOptionalEmbedded(EmbeddedOptions.ALL));

        assertThat(groupDto).isNotNull();
        assertThat(groupDto.getId()).isEqualTo(group.getId());
        assertThat(groupDto.getProfiles()).size().isEqualTo(2);
    }

    @Test
    public void testAddMoreRestrictions() {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(LEVEL);

        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(internalSecurityService.getUser()).thenReturn(user);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(LEVEL);

        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("level").is(LEVEL));

        internalGroupService.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(1);

        criteriaList = new ArrayList<>();
        internalGroupService.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(0);
    }

    @Test
    public void testAddMoreRestrictionsAdminUser() {
        wireInternalSecurityServerCalls();

        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("level").is(ApiIamInternalConstants.ADMIN_LEVEL));

        internalGroupService.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(1);

        criteriaList = new ArrayList<>();
        internalGroupService.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(0);
    }

    private CustomerDto buildCustomerDto() {
        return IamServerUtilsTest.buildCustomerDto();
    }

    private Group buildGroup() {
        final Group group = IamServerUtilsTest.buildGroup();
        group.setLevel(LEVEL);
        return group;
    }

    private ProfileDto buildProfileDto(final String id, final String app) {
        final ProfileDto dto = IamServerUtilsTest.buildProfileDto();
        dto.setId(id);
        dto.setApplicationName(app);
        dto.setTenantName("myTenant");
        dto.setLevel(LEVEL);
        return dto;
    }

    @Test
    public void getLevels_whenProfilesExist_returnsLevels(){
        Optional<String> criteria = Optional.empty();
        List<Document> mappedResults = new ArrayList<>();
        Document document = new Document("level", Arrays.asList("DEV", "TEST"));
        mappedResults.add(document);
        Document rawResults = new Document();
        AggregationResults<Document> value = new AggregationResults<>(mappedResults,rawResults);
        when(groupRepository.aggregate(any(TypedAggregation.class), ArgumentMatchers.eq(Document.class))).thenReturn(value);
        List<String> levels = internalGroupService.getLevels(criteria);
        assertThat(levels.size()).isEqualTo(2);
        assertThat(levels.get(0)).isEqualTo("DEV");
        assertThat(levels.get(1)).isEqualTo("TEST");
    }

    @Test
    public void getLevels_whenNoProfile_returnsEmptyList(){
        Optional<String> criteria = Optional.empty();
        List<Document> mappedResults = new ArrayList<>();
        Document rawResults = new Document();
        AggregationResults<Document> value = new AggregationResults<>(mappedResults,rawResults);
        when(groupRepository.aggregate(any(TypedAggregation.class), ArgumentMatchers.eq(Document.class))).thenReturn(value);
        List<String> levels = internalGroupService.getLevels(criteria);
        assertThat(levels.size()).isEqualTo(0);
    }
}
