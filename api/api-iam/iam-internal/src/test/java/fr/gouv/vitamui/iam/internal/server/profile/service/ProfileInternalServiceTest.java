package fr.gouv.vitamui.iam.internal.server.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.gouv.vitamui.commons.test.utils.FieldUtils;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.utils.DtoFactory;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

public class ProfileInternalServiceTest {

    private ProfileInternalService service;

    private final ProfileRepository profileRepository = mock(ProfileRepository.class);

    private final GroupInternalService profileGroupCrudService = mock(GroupInternalService.class);

    private final UserRepository userRepository = mock(UserRepository.class);

    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileInternalServiceTest.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final GroupRepository groupRepository = mock(GroupRepository.class);

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private final TenantRepository tenantRepository = mock(TenantRepository.class);

    private final IamLogbookService iamLogbookService = mock(IamLogbookService.class);

    private final ProfileConverter profileConverter = new ProfileConverter();

    @Before
    public void setup() throws Exception {

        service = new ProfileInternalService(sequenceRepository, profileRepository, customerRepository, groupRepository, tenantRepository, userRepository,
                internalSecurityService, iamLogbookService, profileConverter, null);

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        FieldUtils.setFinalStatic(CustomerInitConfig.class.getDeclaredField("allRoles"), ServicesData.getAllRoles());

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setSequence(1);
        when(sequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(customSequence));
    }

    @Test
    public void testCreateProfileUser() throws Exception {
        final ProfileDto profileDto = DtoFactory.buildProfileDto("User", "User", false, "", 10, "USERS_APP",
                Arrays.asList(ServicesData.ROLE_GET_USERS, ServicesData.ROLE_GET_GROUPS), IamServerUtilsTest.CUSTOMER_ID);

        final Profile other = new Profile();
        VitamUIUtils.copyProperties(profileDto, other);
        other.setId(UUID.randomUUID().toString());

        when(profileRepository.save(ArgumentMatchers.any())).thenReturn(other);
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel("");

        final Group group = IamServerUtilsTest.buildGroup();
        group.setLevel("");

        when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        when(internalSecurityService.getUser()).thenReturn(user);
        when(internalSecurityService.getLevel()).thenReturn("");
        when(internalSecurityService.isLevelAllowed(ArgumentMatchers.any())).thenReturn(true);
        when(internalSecurityService.getCustomerId()).thenReturn(IamServerUtilsTest.CUSTOMER_ID);
        when(customerRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(IamServerUtilsTest.buildCustomer()));
        when(tenantRepository.findByIdentifier(ArgumentMatchers.any())).thenReturn(IamServerUtilsTest.buildTenant("_id", "Tenant", 10));

        final ProfileDto profileCreated = service.create(profileDto);

        final ProfileDto profile = new ProfileDto();
        profile.setId(profileCreated.getId());
        profile.setEnabled(true);
        profile.setIdentifier(profileCreated.getIdentifier());
        profile.setCustomerId(profileDto.getCustomerId());
        profile.setTenantIdentifier(profileDto.getTenantIdentifier());
        profile.setApplicationName(profileDto.getApplicationName());
        profile.setName(profileDto.getName());
        profile.setDescription(profileDto.getDescription());
        profile.setRoles(profileDto.getRoles());

        assertNotNull("Profile id should be defined", profileCreated.getId());
        assertThat(profile).isEqualToComparingFieldByField(profileCreated);
    }

    @Test
    public void testProcessPatchSuccess() {
        final Profile entity = new Profile();
        final Profile other = IamServerUtilsTest.buildProfile();

        final Map<String, Object> partialDto = TestUtils.getMapFromObject(other);
        partialDto.put("roles", other.getRoles().stream().map(role -> TestUtils.getMapFromObject(role)).collect(Collectors.toList()));
        service.processPatch(entity, partialDto);

        entity.setId(other.getId());
        entity.setIdentifier(other.getIdentifier());
        entity.setCustomerId(other.getCustomerId());
        entity.setTenantIdentifier(other.getTenantIdentifier());
        entity.setApplicationName(other.getApplicationName());

        assertThat(entity).isEqualToComparingFieldByField(other);
    }

    @Test
    public void testAddMoreRestrictions() {
        final String userLevel = "TEST";

        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(userLevel);

        final Group group = IamServerUtilsTest.buildGroup();
        group.setLevel(userLevel);

        when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        when(internalSecurityService.getUser()).thenReturn(user);
        when(internalSecurityService.getLevel()).thenReturn(userLevel);
        when(groupRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(group));

        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("level").is(userLevel));

        service.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(1);

        criteriaList = new ArrayList<>();
        service.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(0);
    }

    @Test
    public void testAddMoreRestrictionsAdminUser() {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);

        when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        when(internalSecurityService.getUser()).thenReturn(user);
        when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);

        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("level").is(ApiIamInternalConstants.ADMIN_LEVEL));

        service.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(1);

        criteriaList = new ArrayList<>();
        service.addDataAccessRestrictions(criteriaList);
        assertThat(criteriaList.size()).isEqualTo(0);
    }

    @Test
    public void levelValidPatternValidator() {
        String level = "TEST.TOTO.TATA";
        boolean levelValid = Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level);
        assertThat(levelValid).isTrue();

        level = "TITI.12.TATA";
        levelValid = Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level);
        assertThat(levelValid).isTrue();

        level = "TITI.12.taTA";
        levelValid = Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level);
        assertThat(levelValid).isFalse();

        level = "TaTI..12.taTA";
        levelValid = Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level);
        assertThat(levelValid).isFalse();

        level = "TaTI12.taTA..";
        levelValid = Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level);
        assertThat(levelValid).isFalse();

        level = ".";
        levelValid = Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level);
        assertThat(levelValid).isFalse();

        level = "..";
        levelValid = Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level);
        assertThat(levelValid).isFalse();

        level = "";
        levelValid = Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level);
        assertThat(levelValid).isTrue();
    }

    @Test
    public void getLevels_whenProfilesExist_returnsLevels() {
        final Optional<String> criteria = Optional.of(
                "{\"criteria\":[{\"key\":\"applicationName\",\"value\":\"ARCHIVE_APP\",\"operator\":\"EQUALS\"},{\"key\":\"tenantIdentifier\",\"value\":107,\"operator\":\"EQUALS\"}]}");
        final List<Document> mappedResults = new ArrayList<>();
        final Document document = new Document("level", Arrays.asList("DEV", "TEST"));
        mappedResults.add(document);
        final Document rawResults = new Document();
        final AggregationResults<Document> value = new AggregationResults<>(mappedResults, rawResults);
        when(profileRepository.aggregate(any(TypedAggregation.class), ArgumentMatchers.eq(Document.class))).thenReturn(value);
        final List<String> levels = service.getLevels(criteria);
        assertThat(levels.size()).isEqualTo(2);
        assertThat(levels.get(0)).isEqualTo("DEV");
        assertThat(levels.get(1)).isEqualTo("TEST");
    }

    @Test
    public void getLevels_whenNoProfile_returnsEmptyList() {
        final Optional<String> criteria = Optional.of(
                "{\"criteria\":[{\"key\":\"applicationName\",\"value\":\"ARCHIVE_APP\",\"operator\":\"EQUALS\"},{\"key\":\"tenantIdentifier\",\"value\":107,\"operator\":\"EQUALS\"}]}");
        final List<Document> mappedResults = new ArrayList<>();
        final Document rawResults = new Document();
        final AggregationResults<Document> value = new AggregationResults<>(mappedResults, rawResults);
        when(profileRepository.aggregate(any(TypedAggregation.class), ArgumentMatchers.eq(Document.class))).thenReturn(value);
        final List<String> levels = service.getLevels(criteria);
        assertThat(levels.size()).isEqualTo(0);
    }
}
