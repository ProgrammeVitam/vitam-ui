package fr.gouv.vitamui.referential.external.server.service;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.internal.client.UnitInternalRestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class UnitExternalServiceTest extends ExternalServiceTest {
    @Mock
    private UnitInternalRestClient unitInternalRestClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private UnitExternalService unitExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        unitExternalService = new UnitExternalService(unitInternalRestClient, externalSecurityService);
    }
    
    @Test
    public void findUnitById_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        String unitId = "id";
        
        when(unitInternalRestClient.findUnitById(any(InternalHttpContext.class), any(String.class)))
            .thenReturn(new VitamUISearchResponseDto());

        assertThatCode(() -> {
        	unitExternalService.findUnitById(unitId);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findUnitByDsl_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        when(unitInternalRestClient.findUnitByDsl(any(InternalHttpContext.class), any(Optional.class), any(JsonNode.class)))
            .thenReturn(dslQuery);

        assertThatCode(() -> {
            unitExternalService.findUnitByDsl(Optional.empty(), dslQuery);
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void findObjectMetadataById_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        String unitId = "id";
        JsonNode dslQuery = JsonHandler.createObjectNode();

        when(unitInternalRestClient.findObjectMetadataById(any(InternalHttpContext.class), any(String.class), any(JsonNode.class)))
            .thenReturn(dslQuery);

        assertThatCode(() -> {
            unitExternalService.findObjectMetadataById(unitId, dslQuery);
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void getFilingPlan_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        when(unitInternalRestClient.getFilingPlan(any(InternalHttpContext.class)))
        	.thenReturn(new VitamUISearchResponseDto());

        assertThatCode(() -> {
        	unitExternalService.getFilingPlan();
        }).doesNotThrowAnyException();
    }
}

