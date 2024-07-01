package fr.gouv.vitamui.referential.internal.server.service;

import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ParameterDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.internal.client.ExternalParametersInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class ExternalParametersServiceTest {

    @MockBean(name = "exteralParametersInternalRestClient")
    private ExternalParametersInternalRestClient exteralParametersInternalRestClient;

    @MockBean(name = "securityService")
    private InternalSecurityService securityService;

    @InjectMocks
    private ExternalParametersService externalParametersService;

    public static final String PARAM_BULK_OPERATIONS_THRESHOLD_NAME = "PARAM_BULK_OPERATIONS_THRESHOLD";

    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        externalParametersService = new ExternalParametersService(exteralParametersInternalRestClient, securityService);
    }

    @Test
    void getProfileThresholdValue() {
        ExternalParametersDto myExternalParameter = new ExternalParametersDto();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setKey(ExternalParametersService.PARAM_BULK_OPERATIONS_THRESHOLD_NAME);
        // get parameter that has a value
        parameterDto.setValue("1000");
        myExternalParameter.setParameters(List.of(parameterDto));
        Mockito.when(
            exteralParametersInternalRestClient.getMyExternalParameters(securityService.getHttpContext())
        ).thenReturn(myExternalParameter);
        assertAll(
            "Grouped Assertions of a valid threshold",
            () -> assertTrue(externalParametersService.retrieveProfilThreshold().isPresent()),
            () -> assertEquals(1000L, externalParametersService.retrieveProfilThreshold().get())
        );
    }

    @Test
    void getProfileThresholdEmptyValue() {
        ExternalParametersDto myExternalParameter = new ExternalParametersDto();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setKey(ExternalParametersService.PARAM_BULK_OPERATIONS_THRESHOLD_NAME);

        // get from empty parameter value
        parameterDto.setValue(null);
        myExternalParameter.setParameters(List.of(parameterDto));
        Mockito.when(
            exteralParametersInternalRestClient.getMyExternalParameters(securityService.getHttpContext())
        ).thenReturn(myExternalParameter);
        Assertions.assertTrue(!externalParametersService.retrieveProfilThreshold().isPresent());
    }

    @Test
    void getEmptyProfileThreshold() {
        ExternalParametersDto myExternalParameter = new ExternalParametersDto();
        ParameterDto parameterDto = new ParameterDto();

        // get from null parameter
        parameterDto.setKey(null);
        parameterDto.setValue(null);
        myExternalParameter.setParameters(List.of(parameterDto));
        Mockito.when(
            exteralParametersInternalRestClient.getMyExternalParameters(securityService.getHttpContext())
        ).thenReturn(myExternalParameter);

        Assertions.assertTrue(!externalParametersService.retrieveProfilThreshold().isPresent());
    }
}
