package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class VitamAgencyServiceTest {

    private AdminExternalClient adminExternalClient;
    private AccessExternalClient accessExternalClient;
    private AgencyService agencyService;
    private ObjectMapper objectMapper;
    private VitamAgencyService vitamAgencyService;


    @Before
    public void setUp() {
        adminExternalClient = mock(AdminExternalClient.class);
        accessExternalClient = mock(AccessExternalClient.class);
        agencyService = mock(AgencyService.class);
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        vitamAgencyService = new VitamAgencyService(adminExternalClient, agencyService, objectMapper, accessExternalClient);

        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS VitamAgencyServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void export_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");

        expect(accessExternalClient.selectOperations(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().addResult(logbookOperation).setHttpCode(200));
        EasyMock.replay(accessExternalClient);

        expect(adminExternalClient.downloadAgenciesCsvAsStream(isA(VitamContext.class), isA(String.class)))
            .andReturn(Response.status(200).build());
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamAgencyService.export(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void export_should_throw_VitamClientException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");

        expect(accessExternalClient.selectOperations(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(400));
        EasyMock.replay(accessExternalClient);

        expect(adminExternalClient.downloadAgenciesCsvAsStream(isA(VitamContext.class), isA(String.class)))
            .andReturn(Response.status(400).build());
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamAgencyService.export(vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void export_should_throw_VitamClientException_when_vitamclient_throw_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");

        expect(accessExternalClient.selectOperations(isA(VitamContext.class), isA(JsonNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(accessExternalClient);

        expect(adminExternalClient.downloadAgenciesCsvAsStream(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamAgencyService.export(vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void patchAgency_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgencyModelDto patchAgency = new AgencyModelDto();

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.patchAgency(vitamContext, id,  patchAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchAgency_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgencyModelDto patchAgency = new AgencyModelDto();

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(400));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.patchAgency(vitamContext, id,  patchAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchAgency_should_throw_VitamClientException_when_vitamclient_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgencyModelDto patchAgency = new AgencyModelDto();

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.patchAgency(vitamContext, id,  patchAgency);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void deleteAgency_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.deleteAgency(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void deleteAgency_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(400));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.deleteAgency(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void deleteAgency_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andThrow(new VitamClientException("Exception throw by vitam"));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.deleteAgency(vitamContext, id);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        AgencyModelDto newAgency = new AgencyModelDto();

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.create(vitamContext, newAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        AgencyModelDto newAgency = new AgencyModelDto();

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(400));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.create(vitamContext, newAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_VitamClientException_when_vitamclient_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        AgencyModelDto newAgency = new AgencyModelDto();

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            vitamAgencyService.create(vitamContext, newAgency);
        }).isInstanceOf(VitamClientException.class);
    }	
}

