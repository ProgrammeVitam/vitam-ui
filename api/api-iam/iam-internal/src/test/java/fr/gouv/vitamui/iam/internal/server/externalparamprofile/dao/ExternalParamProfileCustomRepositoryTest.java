package fr.gouv.vitamui.iam.internal.server.externalparamprofile.dao;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.internal.server.common.domain.Parameter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UserRepository}
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class ExternalParamProfileCustomRepositoryTest extends AbstractMongoTests {

    private ExternalParamProfileRepository externalParamProfileRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private ExternalParametersRepository externalParametersRepository;

    @AfterEach
    public void cleanUp() {
        profileRepository.deleteAll();
        externalParametersRepository.deleteAll();
    }

    @BeforeEach
    public void init() {
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
