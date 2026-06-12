package fr.gouv.vitamui.referential.server.service;

import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.iam.openapiclient.ExternalParametersApi;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.server.service.service.ExternalParametersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExternalParametersServiceTest {

    @Mock
    private ExternalParametersApi externalParametersApi;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ExternalParametersService externalParametersService;

    public static final String PARAM_BULK_OPERATIONS_THRESHOLD_NAME = "PARAM_BULK_OPERATIONS_THRESHOLD";

    @BeforeEach
    public void setUp() {
        doReturn(new HttpContext(0, "", false, "", "", null, null, null)).when(securityService).getHttpContext();
        externalParametersService = new ExternalParametersService(externalParametersApi, securityService);
    }

    @Test
    void getProfileThresholdValue() {
        Map<String, String> parameters = Map.of(PARAM_BULK_OPERATIONS_THRESHOLD_NAME, "1000");
        when(externalParametersApi.getMyExternalParameters()).thenReturn(parameters);
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
        when(externalParametersApi.getMyExternalParameters()).thenReturn(parameters);
        assertTrue(externalParametersService.retrieveProfilThreshold().isEmpty());
    }

    @Test
    void getEmptyProfileThreshold() {
        Map<String, String> parameters = Map.of();
        when(externalParametersApi.getMyExternalParameters()).thenReturn(parameters);

        assertTrue(externalParametersService.retrieveProfilThreshold().isEmpty());
    }
}
