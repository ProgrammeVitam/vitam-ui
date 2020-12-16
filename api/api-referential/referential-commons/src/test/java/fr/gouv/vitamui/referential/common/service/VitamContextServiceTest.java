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
package fr.gouv.vitamui.referential.common.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ContextModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class VitamContextServiceTest {

    private AdminExternalClient adminExternalClient;
    private VitamContextService vitamContextService;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        adminExternalClient = mock(AdminExternalClient.class);
        vitamContextService = new VitamContextService(adminExternalClient, objectMapper);
        objectMapper = new ObjectMapper();

        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS VitamContextServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void patchContext_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        final String id = "0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateContext(isA(VitamContext.class), isA(String.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.patchContext(vitamContext, id, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchContext_should_return_ok_when_vitamclient_400() throws AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        final String id = "0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateContext(isA(VitamContext.class), isA(String.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.patchContext(vitamContext, id, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchContext_should_throw_AccessExternalClientException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        final String id = "0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateContext(isA(VitamContext.class), isA(String.class), isA(JsonNode.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.patchContext(vitamContext, id, jsonNode);
        }).isInstanceOf(AccessExternalClientException.class);
    }

    @Test
    public void patchContext_should_throw_InvalidParseOperationException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        final String id = "0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateContext(isA(VitamContext.class), isA(String.class), isA(JsonNode.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.patchContext(vitamContext, id, jsonNode);
        }).isInstanceOf(InvalidParseOperationException.class);
    }

    @Test
    public void findContexts_should_return_ok_when_vitamclient_return_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.findContexts(vitamContext, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findContexts_should_throw_BadRequestException_when_vitamclient_return_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.findContexts(vitamContext, jsonNode);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findContexts_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.findContexts(vitamContext, jsonNode);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findContextById_should_return_ok_when_vitamclient_return_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String contextId = "CId_0";

        expect(adminExternalClient.findContextById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.findContextById(vitamContext, contextId);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findContextById_should_throw_InternalServerException_when_vitamclient_return_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String contextId = "CId_0";

        expect(adminExternalClient.findContextById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.findContextById(vitamContext, contextId);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findContextById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String contextId = "CId_0";

        expect(adminExternalClient.findContextById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamContextService.findContextById(vitamContext, contextId);
        }).isInstanceOf(VitamClientException.class);
    }
}
