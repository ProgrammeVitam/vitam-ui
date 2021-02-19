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
package fr.gouv.vitamui.referential.internal.server.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.OntologyModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dto.OntologyDto;
import fr.gouv.vitamui.referential.common.service.OntologyService;
import fr.gouv.vitamui.referential.internal.server.ontology.OntologyConverter;
import fr.gouv.vitamui.referential.internal.server.ontology.OntologyInternalService;

public class OntologyInternalServiceTest {

    private OntologyService ontologyService;
    private ObjectMapper objectMapper;
    private OntologyConverter converter;
    private LogbookService logbookService;
    private OntologyInternalService ontologyInternalService;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new OntologyConverter();
        ontologyService = mock(OntologyService.class);
        logbookService = mock(LogbookService.class);
        ontologyInternalService = new OntologyInternalService(ontologyService, objectMapper, converter, logbookService);

    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ontologyService.findOntologyById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ontologyService.findOntologyById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(400));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ontologyService.findOntologyById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(400));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.getAll(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();

        expect(ontologyService.checkAbilityToCreateOntologyInVitam(isA(List.class), isA(VitamContext.class)))
            .andReturn(true);
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.check(vitamContext, ontologyDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_throw_BadRequestException_when_vitamclient_throws_BadRequestException() {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();

        expect(ontologyService.checkAbilityToCreateOntologyInVitam(isA(List.class), isA(VitamContext.class)))
            .andThrow(new BadRequestException("Exception thrown by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.check(vitamContext, ontologyDto);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void check_should_throw_UnavailableServiceException_when_vitamclient_throws_UnavailableServiceException() {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();

        expect(ontologyService.checkAbilityToCreateOntologyInVitam(isA(List.class), isA(VitamContext.class)))
            .andThrow(new UnavailableServiceException("Exception thrown by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.check(vitamContext, ontologyDto);
        }).isInstanceOf(UnavailableServiceException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();

        expect(ontologyService.checkAbilityToCreateOntologyInVitam(isA(List.class), isA(VitamContext.class)))
            .andThrow(new ConflictException("Exception thrown by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.check(vitamContext, ontologyDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.create(vitamContext, ontologyDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.create(vitamContext, ontologyDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.create(vitamContext, ontologyDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.create(vitamContext, ontologyDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.create(vitamContext, ontologyDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_400() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new AccessExternalClientException("Exception throw by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new IOException("Exception throw by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new InvalidParseOperationException("Exception throw by vitam"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void updateOntology_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";
        OntologyDto patchOntology = new OntologyDto();

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void updateOntology_should_return_ok_when_vitamclient_400() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";
        OntologyDto patchOntology = new OntologyDto();

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void updateOntology_should_throw_AccessExternalClientException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";
        OntologyDto patchOntology = new OntologyDto();

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new AccessExternalClientException("Exception"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void updateOntology_should_throw_IOException_when_vitamclient_throws_IOException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";
        OntologyDto patchOntology = new OntologyDto();

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new IOException("Exception"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void updateOntology_should_throw_InvalidParseOperationException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";
        OntologyDto patchOntology = new OntologyDto();

        expect(ontologyService.findOntologies(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<OntologyModel>().setHttpCode(200));

        expect(ontologyService.importOntologies(isA(VitamContext.class), isA(List.class)))
            .andThrow(new InvalidParseOperationException("Exception"));
        EasyMock.replay(ontologyService);

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(200));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ontologyInternalService.findHistoryByIdentifier(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(400));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ontologyInternalService.findHistoryByIdentifier(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throws_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException(("Exception throws by vitam")));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ontologyInternalService.findHistoryByIdentifier(vitamContext, identifier);
        }).isInstanceOf(VitamClientException.class);
    }
}
