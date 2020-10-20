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
import fr.gouv.vitam.common.model.administration.ContextModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.service.VitamContextService;
import fr.gouv.vitamui.referential.internal.server.context.ContextConverter;
import fr.gouv.vitamui.referential.internal.server.context.ContextInternalService;
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
public class ContextInternalServiceTest {

    private VitamContextService vitamContextService;
    private ObjectMapper objectMapper;
    private ContextConverter converter;
    private LogbookService logbookService;
    private ContextInternalService contextInternalService;

    @Before
    public void setUp() {
        vitamContextService = mock(VitamContextService.class);
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new ContextConverter();
        logbookService = mock(LogbookService.class);
        contextInternalService = new ContextInternalService(vitamContextService, objectMapper, converter, logbookService);


        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS ContextInternalServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamContextService.findContextById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(200));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamContextService.findContextById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(400));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throw_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamContextService.findContextById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(vitamContextService.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(200));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamContextService.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(400));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throw_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(vitamContextService.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.getAll(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(vitamContextService.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(200));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.findAll(vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(vitamContextService.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<ContextModel>().setHttpCode(400));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.findAll(vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(vitamContextService.findContexts(isA(VitamContext.class), isA(JsonNode.class)))
            .andThrow(new VitamClientException("Exception throw by vitam"));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.findAll(vitamContext, query);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok()  {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        expect(vitamContextService.checkAbilityToCreateContextInVitam(isA(List.class),isA(VitamContext.class)))
            .andReturn(true);
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.check(vitamContext, contextDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_ok_when_vitamclient_400()  {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        expect(vitamContextService.checkAbilityToCreateContextInVitam(isA(List.class),isA(VitamContext.class)))
            .andThrow(new UnavailableServiceException("Exception throw by vitam"));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.check(vitamContext, contextDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException()  {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        expect(vitamContextService.checkAbilityToCreateContextInVitam(isA(List.class),isA(VitamContext.class)))
            .andThrow(new ConflictException("Exception throw by vitam"));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.check(vitamContext, contextDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        expect(vitamContextService.createContext(isA(VitamContext.class), isA(ContextDto.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.create(vitamContext, contextDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        expect(vitamContextService.createContext(isA(VitamContext.class), isA(ContextDto.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.create(vitamContext, contextDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        expect(vitamContextService.createContext(isA(VitamContext.class), isA(ContextDto.class)))
            .andThrow(new AccessExternalClientException(("Exception throw by vitam")));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.create(vitamContext, contextDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        expect(vitamContextService.createContext(isA(VitamContext.class), isA(ContextDto.class)))
            .andThrow(new IOException(("Exception throw by vitam")));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.create(vitamContext, contextDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        expect(vitamContextService.createContext(isA(VitamContext.class), isA(ContextDto.class)))
            .andThrow(new InvalidParseOperationException(("Exception throw by vitam")));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.create(vitamContext, contextDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_ok() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto partialDto = new ContextDto();
        partialDto.setIdentifier("identifier");

        expect(vitamContextService.patchContext(isA(VitamContext.class), isA(String.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.patch(vitamContext, partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_400() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto partialDto = new ContextDto();
        partialDto.setIdentifier("identifier");

        expect(vitamContextService.patchContext(isA(VitamContext.class), isA(String.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.patch(vitamContext, partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto partialDto = new ContextDto();
        partialDto.setIdentifier("identifier");

        expect(vitamContextService.patchContext(isA(VitamContext.class), isA(String.class), isA(ObjectNode.class)))
            .andThrow(new InvalidParseOperationException("Exception throw by vitam"));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto partialDto = new ContextDto();
        partialDto.setIdentifier("identifier");

        expect(vitamContextService.patchContext(isA(VitamContext.class), isA(String.class), isA(ObjectNode.class)))
            .andThrow(new AccessExternalClientException("Exception throw by vitam"));
        EasyMock.replay(vitamContextService);

        assertThatCode(() -> {
            contextInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id ="id_0";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(200));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            contextInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id ="id_0";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(400));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            contextInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id ="id_0";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception"));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            contextInternalService.findHistoryByIdentifier(vitamContext, id);
        }).isInstanceOf(VitamClientException.class);
    }

}
