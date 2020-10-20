package fr.gouv.vitamui.referential.internal.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.SecurityProfileModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.referential.common.service.VitamSecurityProfileService;
import fr.gouv.vitamui.referential.internal.server.securityprofile.SecurityProfileConverter;
import fr.gouv.vitamui.referential.internal.server.securityprofile.SecurityProfileInternalService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class SecurityProfileInternalServiceTest {

    private VitamSecurityProfileService vitamSecurityProfileService;
    private ObjectMapper objectMapper;
    private SecurityProfileConverter converter;
    private LogbookService logbookService;
    private SecurityProfileInternalService securityProfileInternalService;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new SecurityProfileConverter();
        vitamSecurityProfileService = mock(VitamSecurityProfileService.class);
        logbookService = mock(LogbookService.class);
        securityProfileInternalService = new SecurityProfileInternalService(vitamSecurityProfileService, objectMapper, converter, logbookService);


        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS SecurityProfileInternalServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamSecurityProfileService.findSecurityProfileById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(200));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamSecurityProfileService.findSecurityProfileById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(400));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamSecurityProfileService.findSecurityProfileById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(vitamSecurityProfileService.findSecurityProfiles(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(200));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(vitamSecurityProfileService.findSecurityProfiles(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(400));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(vitamSecurityProfileService.findSecurityProfiles(isA(VitamContext.class), isA(ObjectNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.getAll(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(vitamSecurityProfileService.findSecurityProfiles(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(200));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.findAll( vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(vitamSecurityProfileService.findSecurityProfiles(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(400));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.findAll( vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(vitamSecurityProfileService.findSecurityProfiles(isA(VitamContext.class), isA(JsonNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.findAll( vitamContext, query);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok(){
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        expect(vitamSecurityProfileService.checkAbilityToCreateSecurityProfileInVitam(isA(List.class), isA(VitamContext.class)))
            .andReturn(true);
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.check(vitamContext, securityProfileDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        expect(vitamSecurityProfileService.checkAbilityToCreateSecurityProfileInVitam(isA(List.class), isA(VitamContext.class)))
            .andThrow(new ConflictException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.check(vitamContext, securityProfileDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok() throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        expect(vitamSecurityProfileService.createSecurityProfile(isA(VitamContext.class), isA(SecurityProfileModel.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext , securityProfileDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400() throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        expect(vitamSecurityProfileService.createSecurityProfile(isA(VitamContext.class), isA(SecurityProfileModel.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext , securityProfileDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        expect(vitamSecurityProfileService.createSecurityProfile(isA(VitamContext.class), isA(SecurityProfileModel.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext , securityProfileDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        expect(vitamSecurityProfileService.createSecurityProfile(isA(VitamContext.class), isA(SecurityProfileModel.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext , securityProfileDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        expect(vitamSecurityProfileService.createSecurityProfile(isA(VitamContext.class), isA(SecurityProfileModel.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext , securityProfileDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        expect(vitamSecurityProfileService.createSecurityProfile(isA(VitamContext.class), isA(SecurityProfileModel.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext , securityProfileDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");

        expect(vitamSecurityProfileService.patchSecurityProfile(isA(VitamContext.class), isA(String.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.patch(vitamContext ,partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");

        expect(vitamSecurityProfileService.patchSecurityProfile(isA(VitamContext.class), isA(String.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.patch(vitamContext ,partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");

        expect(vitamSecurityProfileService.patchSecurityProfile(isA(VitamContext.class), isA(String.class), isA(JsonNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.patch(vitamContext ,partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(vitamSecurityProfileService.deleteSecurityProfile(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_400() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(vitamSecurityProfileService.deleteSecurityProfile(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(vitamSecurityProfileService.deleteSecurityProfile(isA(VitamContext.class), isA(String.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(vitamSecurityProfileService.deleteSecurityProfile(isA(VitamContext.class), isA(String.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(vitamSecurityProfileService.deleteSecurityProfile(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(vitamSecurityProfileService.deleteSecurityProfile(isA(VitamContext.class), isA(String.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(vitamSecurityProfileService);

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(200));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            securityProfileInternalService.findHistoryByIdentifier(vitamContext , id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(400));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            securityProfileInternalService.findHistoryByIdentifier(vitamContext , id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            securityProfileInternalService.findHistoryByIdentifier(vitamContext , id);
        }).isInstanceOf(VitamClientException.class);
    }

}
