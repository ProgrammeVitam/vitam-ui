package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.external.client.ExternalParametersExternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.external.server.service.service.ExternalParametersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ExternalParametersServiceTest {

    @MockBean(name = "externalParametersExternalRestClient")
    private ExternalParametersExternalRestClient externalParametersExternalRestClient;

    @MockBean(name = "securityService")
    private ExternalSecurityService securityService;

    @InjectMocks
    private ExternalParametersService externalParametersService;

    public static final String PARAM_BULK_OPERATIONS_THRESHOLD_NAME = "PARAM_BULK_OPERATIONS_THRESHOLD";

    @BeforeEach
    public void setUp() {
        doReturn(new ExternalHttpContext(0, "", "", "")).when(securityService).getHttpContext();
        externalParametersService = new ExternalParametersService(
            externalParametersExternalRestClient,
            securityService
        );
    }

    @Test
    void getProfileThresholdValue() {
        Map<String, String> parameters = Map.of(PARAM_BULK_OPERATIONS_THRESHOLD_NAME, "1000");
        when(externalParametersExternalRestClient.getMyExternalParameters(any(ExternalHttpContext.class))).thenReturn(
            parameters
        );
        assertAll(
            "Grouped Assertions of a valid threshold",
            () -> assertTrue(externalParametersService.retrieveProfilThreshold().isPresent()),
            () -> assertEquals(1000L, externalParametersService.retrieveProfilThreshold().get())
        );
    }

    @Test
    void getProfileThresholdEmptyValue() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PARAM_BULK_OPERATIONS_THRESHOLD_NAME, null);
        when(externalParametersExternalRestClient.getMyExternalParameters(any(ExternalHttpContext.class))).thenReturn(
            parameters
        );
        assertTrue(!externalParametersService.retrieveProfilThreshold().isPresent());
    }

    @Test
    void getEmptyProfileThreshold() {
        Map<String, String> parameters = Map.of();
        when(externalParametersExternalRestClient.getMyExternalParameters(any(ExternalHttpContext.class))).thenReturn(
            parameters
        );

        assertTrue(!externalParametersService.retrieveProfilThreshold().isPresent());
    }
}
