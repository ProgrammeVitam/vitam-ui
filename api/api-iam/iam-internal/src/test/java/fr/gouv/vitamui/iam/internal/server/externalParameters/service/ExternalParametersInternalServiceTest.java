package fr.gouv.vitamui.iam.internal.server.externalParameters.service;

import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.enums.Application;
import fr.gouv.vitamui.iam.internal.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@EnableMongoRepositories(
	basePackageClasses =  {ExternalParametersRepository.class, CustomSequenceRepository.class, GroupRepository.class,
        OwnerRepository.class, ProfileRepository.class, UserRepository.class, TenantRepository.class },
	repositoryBaseClass = VitamUIRepositoryImpl.class)
public class ExternalParametersInternalServiceTest extends AbstractLogbookIntegrationTest {

	private ExternalParametersInternalService service;

    @MockBean
    private ExternalParametersRepository externalParametersRepository;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @Autowired
    private ExternalParametersConverter externalParametersConverter;

    @Autowired
    private InternalSecurityService internalSecurityService;

    @MockBean
    private SpMetadataGenerator spMetadataGenerator;


    private static final String ID = "ID";

    @Before
    public void setup() {
        service = new ExternalParametersInternalService(
        		sequenceRepository, externalParametersRepository, externalParametersConverter, internalSecurityService);
    }

    @Test
    public void testGetOne() {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.getProfileGroup().getProfiles().get(0).setApplicationName(Application.EXTERNAL_PARAMS.toString());
        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setId(ID);

    	when(externalParametersRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(externalParameters));
        when(internalSecurityService.getUser()).thenReturn(user);

        ExternalParametersDto res = this.service.getMyExternalParameters();
        Assert.assertNotNull("ExternalParameters should be returned.", res);
        Assert.assertTrue(res.getId().equals(ID));
    }
}
