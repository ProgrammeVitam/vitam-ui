/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
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
