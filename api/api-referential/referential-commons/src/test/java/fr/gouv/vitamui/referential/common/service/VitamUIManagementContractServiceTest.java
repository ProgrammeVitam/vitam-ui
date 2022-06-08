package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.FileFormatModel;
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;
import static org.junit.Assert.*;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class VitamUIManagementContractServiceTest {

    private AdminExternalClient adminExternalClient;

    private VitamUIManagementContractService vitamUIManagementContractService;

    @Before
    public void setUp() {
        adminExternalClient = mock(AdminExternalClient.class);
        vitamUIManagementContractService = new VitamUIManagementContractService(adminExternalClient);

        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS VitamUIAccessContractServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void patchAccessContract_should_return_ok_when_vitamclient_ok() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateManagementContract(vitamSecurityProfile, id, jsonNode))
            .andReturn(new RequestResponseOK<ManagementContractModel>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(()-> {
            vitamUIManagementContractService.patchManagementContract(vitamSecurityProfile, id, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchAccessContract_should_return_ok_when_vitamclient_400() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateManagementContract(vitamSecurityProfile, id, jsonNode))
            .andReturn(new RequestResponseOK<FileFormatModel>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamUIManagementContractService.patchManagementContract(vitamSecurityProfile, id, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchAccessContract_should_throw_InvalidParseOperationException_when_vitamclient_throws_InvalidParseOperationException() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateManagementContract(vitamSecurityProfile, id, jsonNode))
            .andThrow(new InvalidParseOperationException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamUIManagementContractService.patchManagementContract(vitamSecurityProfile, id, jsonNode);
        }).isInstanceOf(InvalidParseOperationException.class);
    }

    @Test
    public void patchAccessContract_should_throw_AccessExternalClientException_when_vitamclient_throws_AccessExternalClientException() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateManagementContract(vitamSecurityProfile, id, jsonNode))
            .andThrow(new AccessExternalClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamUIManagementContractService.patchManagementContract(vitamSecurityProfile, id, jsonNode);
        }).isInstanceOf(AccessExternalClientException.class);
    }
}
