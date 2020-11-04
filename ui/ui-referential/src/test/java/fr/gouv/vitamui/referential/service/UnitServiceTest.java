package fr.gouv.vitamui.referential.service;

import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;

import fr.gouv.vitamui.referential.external.client.UnitExternalRestClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UnitServiceTest {
    @Mock
    private UnitExternalRestClient client;
    private UnitService service;

    @Before
    public void setUp() {
        service = new UnitService(client);
    }    

    @Test
    public void testSearchById() {
        String unitId = "id";

        when(client.findUnitById(isNull(), any(String.class)))
        	.thenReturn(new VitamUISearchResponseDto());
        
        final VitamUISearchResponseDto response = service.searchById(unitId, null);
        Assert.assertNotNull(response);
    }  

    @Test
    public void testFindByDsl() {
        JsonNode json = JsonHandler.createObjectNode();

        when(client.findUnitByDsl(isNull(), any(Optional.class), any(JsonNode.class)))
        	.thenReturn(json);
        
        final JsonNode response = service.findByDsl(Optional.empty(), json, null);
        Assert.assertNotNull(response);
    }
    
    @Test
    public void testFindObjectMetadataById() {
        String unitId = "id";
        JsonNode json = JsonHandler.createObjectNode();

        when(client.findObjectMetadataById(isNull(), any(String.class), any(JsonNode.class)))
        	.thenReturn(json);
        
        final JsonNode response = service.findObjectMetadataById(unitId, json, null);
        Assert.assertNotNull(response);
    }
    
    @Test
    public void testFindFilingPlan() {
        when(client.getFilingPlan(isNull()))
    		.thenReturn(new VitamUISearchResponseDto());
        
        final VitamUISearchResponseDto response = service.findFilingPlan(null);
        Assert.assertNotNull(response);
    }
}
