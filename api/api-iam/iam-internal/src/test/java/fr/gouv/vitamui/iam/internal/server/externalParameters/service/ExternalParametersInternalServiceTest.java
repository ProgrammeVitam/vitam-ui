package fr.gouv.vitamui.iam.internal.server.externalParameters.service;

import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.common.enums.Application;
import fr.gouv.vitamui.iam.internal.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class ExternalParametersInternalServiceTest extends AbstractLogbookIntegrationTest {

    public static final String ANY_EXTERNAL_PARAM_ID = "ANY_EXTERNAL_PARAM_ID";
    private ExternalParametersInternalService service;

    @MockBean
    private ExternalParametersRepository externalParametersRepository;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @Autowired
    private ExternalParametersConverter externalParametersConverter;

    @Autowired
    private InternalSecurityService internalSecurityService;

    @Autowired
    private IamLogbookService iamLogbookService;

    private static final String ID = "ID";

    @BeforeEach
    public void setup() {
        service = new ExternalParametersInternalService(
            sequenceRepository,
            externalParametersRepository,
            externalParametersConverter,
            internalSecurityService,
            iamLogbookService
        );
    }

    @Test
    public void testGetOne() {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.getProfileGroup().getProfiles().get(0).setApplicationName(Application.EXTERNAL_PARAMS.toString());
        user.getProfileGroup().getProfiles().get(0).setExternalParamId(ANY_EXTERNAL_PARAM_ID);
        user.getProfileGroup().getProfiles().get(0).setTenantIdentifier(1);
        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setId(ID);

        when(externalParametersRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(
            Optional.of(externalParameters)
        );
        when(internalSecurityService.getUser()).thenReturn(user);
        when(internalSecurityService.getTenantIdentifier()).thenReturn(1);

        ExternalParametersDto res = this.service.getMyExternalParameters();
        Assertions.assertNotNull(res, "ExternalParameters should be returned.");
        Assertions.assertEquals(ID, res.getId());
    }
}
