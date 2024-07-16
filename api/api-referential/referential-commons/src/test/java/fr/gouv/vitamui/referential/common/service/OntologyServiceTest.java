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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OntologyServiceTest {

    @Mock
    private AdminExternalClient adminExternalClient;

    @InjectMocks
    private OntologyService ontologyService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void findOntologyById_should_return_ontologies_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "0";

        when(adminExternalClient.findOntologyById(any(VitamContext.class), eq(identifier))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        assertThatCode(() -> {
            ontologyService.findOntologyById(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findOntologyById_should_return_vitamClientException_when_vitamClient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String identifier = "1";

        when(adminExternalClient.findOntologyById(any(VitamContext.class), eq(identifier))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(400)
        );

        assertThatCode(() -> {
            ontologyService.findOntologyById(vitamContext, identifier);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findOntologyById_should_return_vitamClientException_when_vitamClient_throws_vitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String identifier = "1";

        when(adminExternalClient.findOntologyById(any(VitamContext.class), eq(identifier))).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> {
            ontologyService.findOntologyById(vitamContext, identifier);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findOntologies_should_return_ontologies_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findOntologies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        assertThatCode(() -> {
            ontologyService.findOntologies(vitamContext, jsonNode);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findOntologies_should_return_vitamClientException_when_vitamClient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findOntologies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(400)
        );

        assertThatCode(() -> {
            ontologyService.findOntologies(vitamContext, jsonNode);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findOntologies_should_return_vitamClientException_when_vitamClient_throws_vitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(2);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findOntologies(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> {
            ontologyService.findOntologies(vitamContext, jsonNode);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void importOntologies_should_return_ok_when_vitamclient_ok()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        List<OntologyModel> ontologies = new ArrayList<>();
        OntologyModel ontologyModel = new OntologyModel();
        ontologyModel.setIdentifier("identifier");
        ontologyModel.setId("1");
        ontologyModel.setTenant(0);
        ontologies.add(ontologyModel);

        when(
            adminExternalClient.importOntologies(
                any(Boolean.class),
                any(VitamContext.class),
                any(ByteArrayInputStream.class)
            )
        ).thenReturn(new RequestResponseOK<>().setHttpCode(200));

        assertThatCode(() -> {
            ontologyService.importOntologies(vitamContext, ontologies);
        }).doesNotThrowAnyException();
    }

    @Test
    public void importOntologies_should_return_ok_when_vitamclient_400()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        List<OntologyModel> ontologies = new ArrayList<>();

        when(
            adminExternalClient.importOntologies(
                any(Boolean.class),
                any(VitamContext.class),
                any(ByteArrayInputStream.class)
            )
        ).thenReturn(new RequestResponseOK<>().setHttpCode(400));

        assertThatCode(() -> {
            ontologyService.importOntologies(vitamContext, ontologies);
        }).doesNotThrowAnyException();
    }

    @Test
    public void importOntologies_should_throw_InvalidParseOperationException_when_vitamclient_throws_InvalidParseOperationException()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        List<OntologyModel> ontologies = new ArrayList<>();

        when(
            adminExternalClient.importOntologies(
                any(Boolean.class),
                any(VitamContext.class),
                any(ByteArrayInputStream.class)
            )
        ).thenThrow(new InvalidParseOperationException("Exception thrown by Vitam"));

        assertThatCode(() -> {
            ontologyService.importOntologies(vitamContext, ontologies);
        }).isInstanceOf(InvalidParseOperationException.class);
    }

    @Test
    public void importOntologies_should_throw_AccessExternalClientException_when_vitamclient_throws_AccessExternalClientException()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        List<OntologyModel> ontologies = new ArrayList<>();

        when(
            adminExternalClient.importOntologies(
                any(Boolean.class),
                any(VitamContext.class),
                any(ByteArrayInputStream.class)
            )
        ).thenThrow(new AccessExternalClientException("Exception thrown by Vitam"));

        assertThatCode(() -> {
            ontologyService.importOntologies(vitamContext, ontologies);
        }).isInstanceOf(AccessExternalClientException.class);
    }
}
