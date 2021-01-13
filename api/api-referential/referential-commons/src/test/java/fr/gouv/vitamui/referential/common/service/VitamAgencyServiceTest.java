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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
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
import java.io.InputStream;
import java.util.List;

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
    public void deleteAgency_should_return_ok_when_vitamclient_ok() throws VitamClientException,
        AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));
        expect(adminExternalClient.createAgencies(isA(VitamContext.class), isA(InputStream.class), isA(String.class)))
            .andReturn((RequestResponse) new RequestResponseOK<>().setHttpCode(200));
        EasyMock.replay(agencyService);
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            vitamAgencyService.deleteAgency(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void deleteAgency_should_return_ok_when_vitamclient_400() throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));
        expect(adminExternalClient.createAgencies(isA(VitamContext.class), isA(InputStream.class), isA(String.class)))
            .andReturn((RequestResponse) new RequestResponseOK<>().setHttpCode(400));
        EasyMock.replay(agencyService);
        EasyMock.replay(adminExternalClient);

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

