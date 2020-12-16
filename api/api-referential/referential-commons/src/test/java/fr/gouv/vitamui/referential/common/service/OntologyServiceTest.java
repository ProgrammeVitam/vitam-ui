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
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.OntologyModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class OntologyServiceTest {

    private AdminExternalClient adminExternalClient;
    private OntologyService ontologyService;

    @Before
    public void setUp() {
        adminExternalClient = mock(AdminExternalClient.class);
        ontologyService = new OntologyService(adminExternalClient);

        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS OntologyServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void findOntologyById_should_return_ontologies_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "0";

        expect(adminExternalClient.findOntologyById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.findOntologyById(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findOntologyById_should_return_vitamClientException_when_vitamClient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String identifier = "1";

        expect(adminExternalClient.findOntologyById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.findOntologyById(vitamContext, identifier);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findOntologyById_should_return_vitamClientException_when_vitamClient_throws_vitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String identifier = "1";

        expect(adminExternalClient.findOntologyById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.findOntologyById(vitamContext, identifier);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findOntologies_should_return_ontologies_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findOntologies(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.findOntologies(vitamContext, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findOntologies_should_return_vitamClientException_when_vitamClient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findOntologies(isA(VitamContext.class), isA(JsonNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.findOntologies(vitamContext, jsonNode);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findOntologies_should_return_vitamClientException_when_vitamClient_throws_vitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(2);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        expect(adminExternalClient.findOntologies(isA(VitamContext.class), isA(JsonNode.class)))
            .andThrow(new VitamClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.findOntologies(vitamContext, jsonNode);
        }).isInstanceOf(VitamClientException.class);
    }


    @Ignore
    @Test
    public void importOntologies_should_return_ok_when_vitamclient_ok() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        List<OntologyModel> ontologies = new ArrayList<OntologyModel>();
        OntologyModel ontologyModel= new OntologyModel();
        ontologyModel.setIdentifier("identifier");
        ontologyModel.setId("1");
        ontologyModel.setTenant(0);
        ontologies.add(ontologyModel);

        expect(adminExternalClient.importOntologies(isA(Boolean.class), isA(VitamContext.class), isA(ByteArrayInputStream.class)))
            .andReturn(new RequestResponseOK<>().setHttpCode(200));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.importOntologies(vitamContext, ontologies);
        }).doesNotThrowAnyException();

    }

    @Ignore
    @Test
    public void importOntologies_should_return_ok_when_vitamclient_400() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        List<OntologyModel> ontologies = new ArrayList<OntologyModel>();

        expect(adminExternalClient.importOntologies(isA(Boolean.class), isA(VitamContext.class), isA(ByteArrayInputStream.class)))
            .andReturn(new RequestResponseOK<>().setHttpCode(400));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.importOntologies(vitamContext, ontologies);
        }).doesNotThrowAnyException();
    }

    @Ignore
    @Test
    public void importOntologies_should_throw_InvalidParseOperationException_when_vitamclient_throws_InvalidParseOperationException() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        List<OntologyModel> ontologies = new ArrayList<OntologyModel>();

        expect(adminExternalClient.importOntologies(isA(Boolean.class), isA(VitamContext.class), isA(ByteArrayInputStream.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.importOntologies(vitamContext, ontologies);
        }).isInstanceOf(InvalidParseOperationException.class);
    }

    @Ignore
    @Test
    public void importOntologies_should_throw_AccessExternalClientException_when_vitamclient_throws_AccessExternalClientException() throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        List<OntologyModel> ontologies = new ArrayList<OntologyModel>();

        expect(adminExternalClient.importOntologies(isA(Boolean.class), isA(VitamContext.class), isA(ByteArrayInputStream.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by Vitam"));
        EasyMock.replay(adminExternalClient);

        assertThatCode(() -> {
            ontologyService.importOntologies(vitamContext, ontologies);
        }).isInstanceOf(AccessExternalClientException.class);
    }

}
