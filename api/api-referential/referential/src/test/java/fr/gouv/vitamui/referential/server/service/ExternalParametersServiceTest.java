package fr.gouv.vitamui.referential.server.service;

import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.iam.client.ExternalParametersRestClient;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.server.service.service.ExternalParametersService;
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

    @MockBean(name = "externalParametersRestClient")
    private ExternalParametersRestClient externalParametersRestClient;

    @MockBean(name = "securityService")
    private SecurityService securityService;

    @InjectMocks
    private ExternalParametersService externalParametersService;

    public static final String PARAM_BULK_OPERATIONS_THRESHOLD_NAME = "PARAM_BULK_OPERATIONS_THRESHOLD";

    @BeforeEach
    public void setUp() {
        doReturn(new HttpContext(0, "", "", "")).when(securityService).getHttpContext();
        externalParametersService = new ExternalParametersService(externalParametersRestClient, securityService);
    }

    @Test
    void getProfileThresholdValue() {
        Map<String, String> parameters = Map.of(PARAM_BULK_OPERATIONS_THRESHOLD_NAME, "1000");
        when(externalParametersRestClient.getMyExternalParameters(any(HttpContext.class))).thenReturn(parameters);
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
        when(externalParametersRestClient.getMyExternalParameters(any(HttpContext.class))).thenReturn(parameters);
        assertTrue(!externalParametersService.retrieveProfilThreshold().isPresent());
    }

    @Test
    void getEmptyProfileThreshold() {
        Map<String, String> parameters = Map.of();
        when(externalParametersRestClient.getMyExternalParameters(any(HttpContext.class))).thenReturn(parameters);

        assertTrue(!externalParametersService.retrieveProfilThreshold().isPresent());
    }
}
