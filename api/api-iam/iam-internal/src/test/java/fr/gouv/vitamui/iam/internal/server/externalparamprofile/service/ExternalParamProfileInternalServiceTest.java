package fr.gouv.vitamui.iam.internal.server.externalparamprofile.service;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.FieldUtils;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.utils.DtoFactory;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.utils.ProfileSequenceGenerator;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.externalparamprofile.dao.ExternalParamProfileRepository;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ExternalParamProfileInternalServiceTest {

    private ExternalParamProfileInternalService externalParamProfileInternalService;
    private ProfileInternalService profileInternalService;
    private ExternalParametersInternalService externalParametersInternalService;
    private final ExternalParamProfileRepository externalParamProfileRepository = mock(ExternalParamProfileRepository.class);

    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final ProfileSequenceGenerator profileSequenceGenerator = mock(ProfileSequenceGenerator.class);

    private final ExternalParametersRepository externalParametersRepository = mock(ExternalParametersRepository.class);

    private final IamLogbookService iamLogbookService = mock(IamLogbookService.class);

    private final LogbookService logbookService = mock(LogbookService.class);

    private final ProfileConverter profileConverter = new ProfileConverter();
    private final ExternalParametersConverter ExternalParametersConverter = new ExternalParametersConverter();

    @Before
    public void setup() throws Exception {

        externalParametersInternalService = new ExternalParametersInternalService(sequenceRepository, externalParametersRepository, ExternalParametersConverter,
            internalSecurityService, iamLogbookService);

        externalParamProfileInternalService = new ExternalParamProfileInternalService(externalParametersInternalService, profileInternalService,
            internalSecurityService, profileSequenceGenerator,iamLogbookService,
            externalParamProfileRepository, logbookService, profileConverter);

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        FieldUtils.setFinalStatic(CustomerInitConfig.class.getDeclaredField("allRoles"), ServicesData.getAllRoles());

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setSequence(1);
        when(sequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(customSequence));
        when(profileSequenceGenerator.generateIdentifier(any())).thenReturn("11");
        doNothing().when(iamLogbookService).createExternalParametersEvent(any());
    }

    @Test
    public void testCreateExternalParamProfile() {
        final ExternalParamProfileDto externalParamProfileDto = new ExternalParamProfileDto();
        externalParamProfileDto.setIdExternalParam("id");
        externalParamProfileDto.setName("name");
        externalParamProfileDto.setDescription("description");
        externalParamProfileDto.setAccessContract("access_contract");

        final ExternalParamProfileDto other = new ExternalParamProfileDto();
        VitamUIUtils.copyProperties(externalParamProfileDto, other);
        other.setId(UUID.randomUUID().toString());

        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel("");

        final Group group = IamServerUtilsTest.buildGroup();
        group.setLevel("");

        ExternalParametersDto externalParametersDto = new ExternalParametersDto();
        externalParametersDto.setIdentifier("identifier");

        when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        when(internalSecurityService.getUser()).thenReturn(user);
        when(internalSecurityService.getLevel()).thenReturn("");

        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setIdentifier("identifier");

        when(externalParametersRepository.save(externalParameters)).thenReturn(externalParameters);

        //doNothing().when(iamLogbookService).createExternalParametersEvent(externalParametersDto);

        /*final ExternalParametersDto ep = new ExternalParametersDto();
        VitamUIUtils.copyProperties(externalParametersDto, ep);


        when(externalParametersRepository.generateSuperId()).thenReturn("id");

        when(externalParametersInternalService.create(externalParametersDto)).thenReturn(ep);

        final ExternalParamProfileDto externalParamProfileDtoCreated = externalParamProfileInternalService.create(other);

        assertNotNull("external Profile id should be defined", externalParamProfileDtoCreated.getId());*/
    }
}
