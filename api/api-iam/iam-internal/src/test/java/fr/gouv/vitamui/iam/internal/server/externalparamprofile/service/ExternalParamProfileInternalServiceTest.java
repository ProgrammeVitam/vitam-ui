package fr.gouv.vitamui.iam.internal.server.externalparamprofile.service;

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
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.externalparamprofile.dao.ExternalParamProfileRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ExternalParamProfileInternalServiceTest {

    private ExternalParamProfileInternalService externalParamProfileInternalService;

    private final ProfileInternalService profileInternalService = mock(ProfileInternalService.class);

    private final ExternalParametersInternalService externalParametersInternalService = mock(
        ExternalParametersInternalService.class
    );

    private final ExternalParamProfileRepository externalParamProfileRepository = mock(
        ExternalParamProfileRepository.class
    );

    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final ExternalParametersRepository externalParametersRepository = mock(ExternalParametersRepository.class);

    private final IamLogbookService iamLogbookService = mock(IamLogbookService.class);

    private final LogbookService logbookService = mock(LogbookService.class);

    private final ProfileConverter profileConverter = new ProfileConverter();

    @Before
    public void setup() throws Exception {
        externalParamProfileInternalService = new ExternalParamProfileInternalService(
            externalParametersInternalService,
            profileInternalService,
            internalSecurityService,
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
    public void testCreateProfileUser() {
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
        when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        when(internalSecurityService.getUser()).thenReturn(user);
        when(internalSecurityService.getLevel()).thenReturn("");
        when(internalSecurityService.isLevelAllowed(ArgumentMatchers.any())).thenReturn(true);
        when(internalSecurityService.getCustomerId()).thenReturn(IamServerUtilsTest.CUSTOMER_ID);

        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setIdentifier("external_param_id");
        ExternalParametersDto externalParametersDto = new ExternalParametersDto();
        externalParametersDto.setName("name");
        externalParametersDto.setIdentifier("external_param_id");
        externalParametersDto.setIdentifier("identifier");
        when(externalParametersRepository.save(externalParameters)).thenReturn(externalParameters);
        when(externalParametersRepository.save(externalParameters)).thenReturn(externalParameters);
        when(externalParametersInternalService.getExternalParametersRepository()).thenReturn(
            externalParametersRepository
        );
        when(externalParametersRepository.generateSuperId()).thenReturn("id");
        when(externalParametersInternalService.create(any())).thenReturn(externalParametersDto);
        when(profileInternalService.create(any())).thenReturn(profileDto);

        // Then
        final ExternalParamProfileDto expectedValue = new ExternalParamProfileDto();
        expectedValue.setEnabled(false);
        expectedValue.setExternalParamIdentifier(externalParametersDto.getIdentifier());
        expectedValue.setName(externalParametersDto.getName());
        expectedValue.setDescription(other.getDescription());
        expectedValue.setAccessContract("access_contract");

        final ExternalParamProfileDto CreatedExternalParamProfileDto = externalParamProfileInternalService.create(
            externalParamProfileDto
        );

        assertNotNull("Of course external parameter profile should not be null", CreatedExternalParamProfileDto);
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
