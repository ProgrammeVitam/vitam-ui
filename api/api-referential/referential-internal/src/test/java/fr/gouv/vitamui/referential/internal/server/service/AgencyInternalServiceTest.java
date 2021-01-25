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
import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Condition;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.common.service.VitamAgencyService;
import fr.gouv.vitamui.referential.internal.server.agency.AgencyConverter;
import fr.gouv.vitamui.referential.internal.server.agency.AgencyInternalService;

public class AgencyInternalServiceTest {

    private AgencyService agencyService;
    private ObjectMapper objectMapper;
    private AgencyConverter converter;
    private LogbookService logbookService;
    private VitamAgencyService vitamAgencyService;
    private AgencyInternalService agencyInternalService;

    @Before
    public void setUp() {
        agencyService = mock(AgencyService.class);
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new AgencyConverter();
        logbookService = mock(LogbookService.class);
        vitamAgencyService = mock(VitamAgencyService.class);
        agencyInternalService = new AgencyInternalService(agencyService, objectMapper, converter, logbookService, vitamAgencyService);
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException, JsonProcessingException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(agencyService.findAgencyById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            agencyInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(agencyService.findAgencyById(vitamContext, identifier))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(400));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            agencyInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(agencyService.findAgencyById(vitamContext, identifier))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            agencyInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(agencyService.findAgencies(vitamContext, new Select().getFinalSelect()))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            agencyInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(agencyService.findAgencies(vitamContext, new Select().getFinalSelect()))
            .andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(400));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            agencyInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(agencyService.findAgencies(vitamContext, new Select().getFinalSelect()))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
            agencyInternalService.getAll(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok()  {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        expect(vitamAgencyService.checkAbilityToCreateAgencyInVitam(isA(ArrayList.class), isA(String.class)))
            .andReturn(1);
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.check(vitamContext, agencyDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException()  {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        expect(vitamAgencyService.checkAbilityToCreateAgencyInVitam(isA(ArrayList.class), isA(String.class)))
            .andThrow(new ConflictException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.check(vitamContext, agencyDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        expect(vitamAgencyService.create(isA(VitamContext.class), isA(AgencyModelDto.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.create(vitamContext, agencyDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_400() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        expect(vitamAgencyService.create(isA(VitamContext.class), isA(AgencyModelDto.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.create(vitamContext, agencyDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        expect(vitamAgencyService.create(isA(VitamContext.class), isA(AgencyModelDto.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.create(vitamContext, agencyDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        expect(vitamAgencyService.create(isA(VitamContext.class), isA(AgencyModelDto.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.create(vitamContext, agencyDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        expect(vitamAgencyService.create(isA(VitamContext.class), isA(AgencyModelDto.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.create(vitamContext, agencyDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        expect(vitamAgencyService.create(isA(VitamContext.class), isA(AgencyModelDto.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.create(vitamContext, agencyDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_ok() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
    		.andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
        	vitamAgencyService.deleteAgency(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_400() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        expect(agencyService.findAgencies(isA(VitamContext.class), isA(ObjectNode.class)))
    		.andReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(400));
        EasyMock.replay(agencyService);

        assertThatCode(() -> {
        	vitamAgencyService.deleteAgency(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        expect(vitamAgencyService.deleteAgency(isA(VitamContext.class), isA(String.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        expect(vitamAgencyService.deleteAgency(isA(VitamContext.class), isA(String.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        expect(vitamAgencyService.deleteAgency(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        expect(vitamAgencyService.deleteAgency(isA(VitamContext.class), isA(String.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void export_should_return_ok_when_vitamclient_ok() throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        expect(vitamAgencyService.export(isA(VitamContext.class)))
            .andReturn(Response.status(200).build());
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.export(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void export_should_return_ok_when_vitamclient_400() throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        expect(vitamAgencyService.export(isA(VitamContext.class)))
            .andReturn(Response.status(400).build());
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.export(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void export_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        expect(vitamAgencyService.export(isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception throxn by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.export(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void export_should_throw_InternalServerException_when_vitamclient_throws_InvalidCreateOperationException() throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        expect(vitamAgencyService.export(isA(VitamContext.class)))
            .andThrow(new InvalidCreateOperationException("Exception throxn by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.export(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void export_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        expect(vitamAgencyService.export(isA(VitamContext.class)))
            .andThrow(new InvalidParseOperationException("Exception throxn by vitam"));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
            agencyInternalService.export(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        expect(logbookService.selectOperations(isA(JsonNode.class),isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(200));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            agencyInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        expect(logbookService.selectOperations(isA(JsonNode.class),isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(400));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            agencyInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        expect(logbookService.selectOperations(isA(JsonNode.class),isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            agencyInternalService.findHistoryByIdentifier(vitamContext, id);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void import_should_return_ok() throws
    InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
    	VitamContext vitamContext = new VitamContext(1);
	    File file = new File("src/test/resources/data/import_agencies_valid.csv");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "text/csv", IOUtils.toByteArray(input));

	    String stringReponse = "{\"httpCode\":\"201\"}";
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonResponse = mapper.readTree(stringReponse);

        expect(vitamAgencyService.importAgencies(isA(VitamContext.class), isA(String.class), isA(MultipartFile.class)))
    	    .andReturn((RequestResponse) new RequestResponseOK<JsonNode>(jsonResponse));
        EasyMock.replay(vitamAgencyService);

        assertThatCode(() -> {
        	agencyInternalService.importAgencies(vitamContext, file.getName(), multipartFile);
        }).doesNotThrowAnyException();
    }
}
