package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.FileFormatModel;
import fr.gouv.vitam.common.model.administration.SecurityProfileModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class VitamSecurityProfileServiceTest {

    private AdminExternalClient adminExternalClient;
    private ObjectMapper objectMapper;
    private VitamSecurityProfileService vitamSecurityProfileService;

    @Before
    public void setUp() {
        adminExternalClient = mock(AdminExternalClient.class);
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        vitamSecurityProfileService = new VitamSecurityProfileService(adminExternalClient, objectMapper);

        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS VitamSecurityProfileServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void patchSecurityProfile_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateSecurityProfile(vitamSecurityProfile, id, jsonNode))
            .andReturn(new RequestResponseOK<FileFormatModel>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.patchSecurityProfile(vitamSecurityProfile, id, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchSecurityProfile_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateSecurityProfile(vitamSecurityProfile, id, jsonNode))
            .andReturn(new RequestResponseOK<FileFormatModel>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.patchSecurityProfile(vitamSecurityProfile, id, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchSecurityProfile_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.updateSecurityProfile(vitamSecurityProfile, id, jsonNode))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.patchSecurityProfile(vitamSecurityProfile, id, jsonNode);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void deleteSecurityProfile_should_return_ok_when_vitamclient_ok() throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "1";

        expect(adminExternalClient.createSecurityProfiles(isA(VitamContext.class), isA(InputStream.class), isA(String.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));

        expect(adminExternalClient.findSecurityProfiles(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.deleteSecurityProfile(vitamSecurityProfile, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void deleteSecurityProfile_should_throw_JsonProcessingException_when_vitamclient_400() throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "1";

        expect(adminExternalClient.createSecurityProfiles(isA(VitamContext.class), isA(InputStream.class), isA(String.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));

        expect(adminExternalClient.findSecurityProfiles(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.deleteSecurityProfile(vitamSecurityProfile, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void deleteSecurityProfile_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";

        expect(adminExternalClient.createSecurityProfiles(isA(VitamContext.class), isA(InputStream.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));

        expect(adminExternalClient.findSecurityProfiles(vitamSecurityProfile, new Select().getFinalSelect()))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.deleteSecurityProfile(vitamSecurityProfile, id);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findSecurityProfiles_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findSecurityProfiles(vitamSecurityProfile, jsonNode))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.findSecurityProfiles(vitamSecurityProfile, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findSecurityProfiles_should_throw_BadRequestException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findSecurityProfiles(vitamSecurityProfile, jsonNode))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.findSecurityProfiles(vitamSecurityProfile, jsonNode);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findSecurityProfiles_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findSecurityProfiles(vitamSecurityProfile, jsonNode))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.findSecurityProfiles(vitamSecurityProfile, jsonNode);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findSecurityProfileById_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String securityProfileId = "SPId_0";

        expect(adminExternalClient.findSecurityProfileById(vitamSecurityProfile, securityProfileId))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.findSecurityProfileById(vitamSecurityProfile, securityProfileId);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findSecurityProfileById_should_return_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String securityProfileId = "SPId_0";

        expect(adminExternalClient.findSecurityProfileById(vitamSecurityProfile, securityProfileId))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.findSecurityProfileById(vitamSecurityProfile, securityProfileId);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findSecurityProfileById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String securityProfileId = "SPId_0";

        expect(adminExternalClient.findSecurityProfileById(vitamSecurityProfile, securityProfileId))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamSecurityProfileService.findSecurityProfileById(vitamSecurityProfile, securityProfileId);
        }).isInstanceOf(VitamClientException.class);
    }
}
