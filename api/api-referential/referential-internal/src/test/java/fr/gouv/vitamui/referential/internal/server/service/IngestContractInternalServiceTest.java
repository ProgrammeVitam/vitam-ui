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
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.service.IngestContractService;
import fr.gouv.vitamui.referential.internal.server.ingestcontract.IngestContractConverter;
import fr.gouv.vitamui.referential.internal.server.ingestcontract.IngestContractInternalService;
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
public class IngestContractInternalServiceTest {

    private IngestContractService ingestContractService;
    private ObjectMapper objectMapper;
    private IngestContractConverter converter;
    private LogbookService logbookService;
    private IngestContractInternalService ingestContractInternalService;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new IngestContractConverter();
        logbookService = mock(LogbookService.class);
        ingestContractService = mock(IngestContractService.class);
        ingestContractInternalService = new IngestContractInternalService(ingestContractService, objectMapper, converter, logbookService);


        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS IngestContractInternalServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ingestContractService.findIngestContractById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(200));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ingestContractService.findIngestContractById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(400));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ingestContractService.findIngestContractById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(200));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(400));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(ObjectNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getAll(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(200));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.findAll(vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(400));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.findAll(vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(JsonNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.findAll(vitamContext, query);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class)))
            .andReturn(1);
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.check(vitamContext, ingestContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_throw_BadRequestException_when_vitamclient_throws_BadRequestException() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class)))
            .andThrow(new BadRequestException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.check(vitamContext, ingestContractDto);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void check_should_throw_UnavailableServiceException_when_vitamclient_throws_UnavailableServiceException() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class)))
            .andThrow(new UnavailableServiceException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.check(vitamContext, ingestContractDto);
        }).isInstanceOf(UnavailableServiceException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class)))
            .andThrow(new ConflictException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.check(vitamContext, ingestContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    /*@Test
    public void patch_should_return_ok_when_vitamclient_ok() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(1);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");
        String id = "identifier";

        expect(ingestContractService.patchAccessContract(isA(VitamContext.class), isA(String.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.patch(vitamContext, partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_400() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(1);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");
        String id = "identifier";

        expect(ingestContractService.patchAccessContract(isA(VitamContext.class), isA(String.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.patch(vitamContext, partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(1);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");
        String id = "identifier";

        expect(ingestContractService.patchAccessContract(isA(VitamContext.class), isA(String.class), isA(ObjectNode.class)))
            .andThrow(new InternalServerException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(1);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");
        String id = "identifier";

        expect(ingestContractService.patchAccessContract(isA(VitamContext.class), isA(String.class), isA(ObjectNode.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }*/

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(200));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ingestContractInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(400));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ingestContractInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ingestContractInternalService.findHistoryByIdentifier(vitamContext, id);
        }).isInstanceOf(VitamClientException.class);
    }

}
