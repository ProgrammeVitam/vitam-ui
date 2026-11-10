package fr.gouv.vitamui.iam.server.externalparamprofile.service;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.utils.DtoFactory;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.externalparamprofile.dao.ExternalParamProfileRepository;
import fr.gouv.vitamui.iam.server.group.domain.Group;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ExternalParamProfileServiceTest {

    private ExternalParamProfileService externalParamProfileService;

    private final ProfileService profileService = mock(ProfileService.class);

    private final ExternalParametersService externalParametersService = mock(ExternalParametersService.class);

    private final ExternalParamProfileRepository externalParamProfileRepository = mock(
        ExternalParamProfileRepository.class
    );

    private final SecurityService securityService = mock(SecurityService.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final ExternalParametersRepository externalParametersRepository = mock(ExternalParametersRepository.class);

    private final IamLogbookService iamLogbookService = mock(IamLogbookService.class);

    private final LogbookService logbookService = mock(LogbookService.class);

    private final ProfileConverter profileConverter = new ProfileConverter();

    @BeforeEach
    public void setup() throws Exception {
        externalParamProfileService = new ExternalParamProfileService(
            externalParametersService,
            profileService,
            securityService,
            iamLogbookService,
            externalParamProfileRepository,
            logbookService,
            profileConverter
        );

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setSequence(1);
        when(sequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(customSequence));
        doNothing().when(iamLogbookService).createExternalParametersEvent(any());
    }

    @Test
    void testCreateProfileUser() {
        // Givens
        final ProfileDto profileDto = DtoFactory.buildProfileDto(
            "User",
            "User",
            false,
            "",
            10,
            "USERS_APP",
            Arrays.asList(ServicesData.ROLE_GET_USERS, ServicesData.ROLE_GET_GROUPS),
            IamServerUtilsTest.CUSTOMER_ID
        );
        profileDto.setExternalParamId("external_param_id");

        final ExternalParamProfileDto externalParamProfileDto = new ExternalParamProfileDto();
        externalParamProfileDto.setIdExternalParam("id");
        externalParamProfileDto.setName("name");
        externalParamProfileDto.setDescription("description");
        externalParamProfileDto.setAccessContract("access_contract");

        final ExternalParamProfileDto other = new ExternalParamProfileDto();
        VitamUIUtils.copyProperties(externalParamProfileDto, other);
        other.setId(UUID.randomUUID().toString());

        final Profile otherProfile = new Profile();
        VitamUIUtils.copyProperties(profileDto, otherProfile);
        other.setId(UUID.randomUUID().toString());

        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel("");

        final Group group = IamServerUtilsTest.buildGroup();
        group.setLevel("");

        // Whens
        when(securityService.userIsRootLevel()).thenCallRealMethod();
        when(securityService.getUser()).thenReturn(user);
        when(securityService.getLevel()).thenReturn("");
        when(securityService.isLevelAllowed(ArgumentMatchers.any())).thenReturn(true);
        when(securityService.getCustomerId()).thenReturn(IamServerUtilsTest.CUSTOMER_ID);

        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setIdentifier("external_param_id");
        ExternalParametersDto externalParametersDto = new ExternalParametersDto();
        externalParametersDto.setName("name");
        externalParametersDto.setIdentifier("external_param_id");
        externalParametersDto.setIdentifier("identifier");
        when(externalParametersRepository.save(externalParameters)).thenReturn(externalParameters);
        when(externalParametersRepository.save(externalParameters)).thenReturn(externalParameters);
        when(externalParametersService.getExternalParametersRepository()).thenReturn(externalParametersRepository);
        when(externalParametersRepository.generateSuperId()).thenReturn("id");
        when(externalParametersService.create(any())).thenReturn(externalParametersDto);
        when(profileService.create(any())).thenReturn(profileDto);

        // Then
        final ExternalParamProfileDto expectedValue = new ExternalParamProfileDto();
        expectedValue.setEnabled(false);
        expectedValue.setExternalParamIdentifier(externalParametersDto.getIdentifier());
        expectedValue.setName(externalParametersDto.getName());
        expectedValue.setDescription(other.getDescription());
        expectedValue.setAccessContract("access_contract");

        final ExternalParamProfileDto CreatedExternalParamProfileDto = externalParamProfileService.create(
            externalParamProfileDto
        );

        assertNotNull(CreatedExternalParamProfileDto, "Of course external parameter profile should not be null");
        // compare all fields except operation dateTime
        assertThat(CreatedExternalParamProfileDto).isEqualToComparingOnlyGivenFields(
            expectedValue,
            "name",
            "externalParamIdentifier",
            "description",
            "accessContract",
            "enabled",
            "idProfile",
            "profileIdentifier",
            "idExternalParam"
        );
    }

    @Test
    void getAllPaginated_keeps_the_tenant_restriction_whatever_the_criteria_operator() {
        when(securityService.getTenantIdentifier()).thenReturn(10);
        when(securityService.userIsRootLevel()).thenReturn(true);
        final QueryDto criteria = QueryDto.orQuery();
        criteria.addCriterion("name", "any", CriterionOperator.EQUALS);

        externalParamProfileService.getAllPaginated(0, 20, criteria.toJson(), null, null);

        final ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(externalParamProfileRepository).getAllPaginated(any(), any(), jsonCaptor.capture(), any(), any());
        final QueryDto sent = QueryDto.fromJson(jsonCaptor.getValue());
        assertThat(sent.getQueryOperator()).isEqualTo(QueryOperator.AND);
        assertThat(sent.find("tenantIdentifier")).hasValueSatisfying(c -> assertThat(c.getValue()).isEqualTo(10));
        assertThat(sent.getSubQueries())
            .singleElement()
            .satisfies(q -> assertThat(q.getQueryOperator()).isEqualTo(QueryOperator.OR));
    }

    @Test
    void getAllPaginated_applies_the_level_restriction_to_non_root_users() {
        when(securityService.getTenantIdentifier()).thenReturn(10);
        when(securityService.userIsRootLevel()).thenReturn(false);
        when(securityService.getLevel()).thenReturn("DEV");
        final AuthUserDto user = new AuthUserDto();
        final GroupDto group = new GroupDto();
        group.setProfileIds(List.of("ownProfileId"));
        user.setProfileGroup(group);
        when(securityService.getUser()).thenReturn(user);

        externalParamProfileService.getAllPaginated(0, 20, null, null, null);

        final ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(externalParamProfileRepository).getAllPaginated(any(), any(), jsonCaptor.capture(), any(), any());
        final QueryDto levelQuery = QueryDto.fromJson(jsonCaptor.getValue()).getSubQueries().get(0);
        assertThat(levelQuery.getQueryOperator()).isEqualTo(QueryOperator.OR);
        assertThat(levelQuery.find("level")).hasValueSatisfying(c -> {
            assertThat(c.getValue()).isEqualTo("DEV.");
            assertThat(c.getOperator()).isEqualTo(CriterionOperator.STARTWITH);
        });
        assertThat(levelQuery.find("_id")).hasValueSatisfying(
            c -> assertThat(c.getValue()).isEqualTo(List.of("ownProfileId"))
        );
    }

    @Test
    void getAllPaginated_refuses_criteria_on_other_keys() {
        when(securityService.getTenantIdentifier()).thenReturn(10);
        when(securityService.userIsRootLevel()).thenReturn(true);
        final QueryDto criteria = QueryDto.criteria("roles.name", "ROLE_X", CriterionOperator.EQUALS);

        assertThatThrownBy(
            () -> externalParamProfileService.getAllPaginated(0, 20, criteria.toJson(), null, null)
        ).isInstanceOf(ForbiddenException.class);
        verify(externalParamProfileRepository, never()).getAllPaginated(any(), any(), any(), any(), any());
    }

    @Test
    void getOne_requires_read_access_to_the_profile() {
        when(profileService.getOne("profileId")).thenThrow(new NotFoundException("not found"));

        assertThatThrownBy(() -> externalParamProfileService.getOne("profileId")).isInstanceOf(NotFoundException.class);
        verify(externalParamProfileRepository, never()).findByIdProfile(any());
    }

    @Test
    void findHistoryById_requires_read_access_to_the_profile() throws Exception {
        when(profileService.getOne("profileId")).thenThrow(new NotFoundException("not found"));

        assertThatThrownBy(() -> externalParamProfileService.findHistoryById("profileId")).isInstanceOf(
            NotFoundException.class
        );
        verify(logbookService, never()).findEventsByIdentifierAndCollectionNames(any(), any(), any(), any());
    }

    @Test
    void patch_requires_the_right_to_modify_the_profile() {
        when(profileService.checkCanBeModified(any(), any())).thenThrow(new IllegalArgumentException("readonly"));
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("idProfile", "profileId");
        partialDto.put("accessContract", "AC-2");

        assertThatThrownBy(() -> externalParamProfileService.patch(partialDto)).isInstanceOf(
            IllegalArgumentException.class
        );
        verify(externalParametersService, never()).update(any(), any());
    }

    @Test
    void patch_of_the_profile_fields_uses_the_profile_patch() throws Exception {
        final ProfileDto profile = new ProfileDto();
        profile.setId("profileId");
        profile.setCustomerId("customerId");
        profile.setTenantIdentifier(10);
        when(profileService.checkCanBeModified(any(), any())).thenReturn(profile);
        final ExternalParamProfileDto stored = new ExternalParamProfileDto();
        stored.setIdExternalParam("externalParamId");
        when(externalParamProfileRepository.findByIdProfile("profileId")).thenReturn(stored);
        when(externalParametersService.getOneByPassSecurity(any(), any())).thenReturn(new ExternalParametersDto());
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("idProfile", "profileId");
        partialDto.put("name", "new name");

        externalParamProfileService.patch(partialDto);

        verify(profileService).patch(
            Map.of("id", "profileId", "customerId", "customerId", "tenantIdentifier", 10, "name", "new name")
        );
        verify(profileService, never()).update(any());
    }
}
