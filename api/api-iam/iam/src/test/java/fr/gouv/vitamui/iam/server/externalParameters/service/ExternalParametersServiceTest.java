package fr.gouv.vitamui.iam.server.externalParameters.service;

import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.common.enums.Application;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class ExternalParametersServiceTest extends AbstractLogbookIntegrationTest {

    public static final String ANY_EXTERNAL_PARAM_ID = "ANY_EXTERNAL_PARAM_ID";

    private ExternalParametersService service;

    @MockitoBean
    private ExternalParametersRepository externalParametersRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private ExternalParametersConverter externalParametersConverter;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private IamLogbookService iamLogbookService;

    private static final String ID = "ID";
    private static final String TEST_IDENTIFIER = "identifier";
    private static final String TEST_NAME = "name";
    private static final String TEST_KEY = "key";
    private static final String TEST_VALUE = "value";

    @BeforeEach
    public void setup() {
        service = new ExternalParametersService(
            sequenceGeneratorService,
            externalParametersRepository,
            externalParametersConverter,
            securityService,
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
        when(securityService.getUser()).thenReturn(user);
        when(securityService.getTenantIdentifier()).thenReturn(1);

        ExternalParametersDto res = this.service.getMyExternalParameters();
        Assertions.assertNotNull(res, "ExternalParameters should be returned.");
        Assertions.assertEquals(ID, res.getId());
    }
}
