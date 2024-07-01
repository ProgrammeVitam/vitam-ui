package fr.gouv.vitamui.iam.internal.server.externalparamprofile.dao;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.common.domain.Parameter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UserRepository}
 */

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(
    basePackageClasses = { ProfileRepository.class, ExternalParametersRepository.class },
    repositoryBaseClass = VitamUIRepositoryImpl.class
)
public class ExternalParamProfileCustomRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    MongoOperations mongoOperations;

    private ExternalParamProfileRepository externalParamProfileRepository;

    @Autowired
    private ExternalParametersRepository externalParametersRepository;

    @After
    public void cleanUp() {
        profileRepository.deleteAll();
        externalParametersRepository.deleteAll();
    }

    @Before
    public void init() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        externalParamProfileRepository = new ExternalParamProfileRepository(mongoOperations);
        Profile profile = IamServerUtilsTest.buildProfile(
            "id",
            "identifier",
            "name",
            "customerId",
            10,
            CommonConstants.USERS_APPLICATIONS_NAME
        );
        profile.setExternalParamId("externalparamidentifier");
        profileRepository.save(profile);
        final ExternalParameters parameters = new ExternalParameters();
        parameters.setId("externalparamidentifier");
        parameters.setIdentifier("EXTERNAL_PARAMETERS_IDENTIFIANT");
        parameters.setParameters(List.of(new Parameter("PARAM_ACCESS_CONTRACT", "ContratTNR")));
        externalParametersRepository.save(parameters);
    }

    @Test
    public void testFindExternalParamProfile() {
        final ExternalParamProfileDto externalParamProfileDto = externalParamProfileRepository.findByIdProfile("id");
        assertThat(externalParamProfileDto.getId()).isEqualTo("id");
        assertThat(externalParamProfileDto.getIdExternalParam()).isEqualTo("externalparamidentifier");
        assertThat(externalParamProfileDto.getExternalParamIdentifier()).isEqualTo("EXTERNAL_PARAMETERS_IDENTIFIANT");
    }

    @Test
    public void testGetAllPaginated() {
        PaginatedValuesDto<ExternalParamProfileDto> allPaginated = externalParamProfileRepository.getAllPaginated(
            0,
            20,
            "{}",
            null,
            null
        );
        assertThat(allPaginated.getValues().size()).isEqualTo(1);
    }
}
