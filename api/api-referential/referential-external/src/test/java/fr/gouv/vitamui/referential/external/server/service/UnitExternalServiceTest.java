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

