package fr.gouv.vitamui.iam.server.externalparamprofile.service;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
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
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
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
}
