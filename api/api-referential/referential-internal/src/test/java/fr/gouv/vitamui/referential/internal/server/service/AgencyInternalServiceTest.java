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
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class AgencyInternalServiceTest {

    @Mock
    private AgencyService agencyService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private VitamAgencyService vitamAgencyService;

    @InjectMocks
    private AgencyInternalService agencyInternalService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        AgencyConverter converter = new AgencyConverter();
        agencyInternalService = new AgencyInternalService(
            agencyService,
            objectMapper,
            converter,
            logbookService,
            vitamAgencyService
        );
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(agencyService.findAgencyById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        assertThatCode(() -> agencyInternalService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(agencyService.findAgencyById(vitamContext, identifier)).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );

        assertThatCode(() -> agencyInternalService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(agencyService.findAgencyById(vitamContext, identifier)).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.getOne(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(agencyService.findAgencies(vitamContext, new Select().getFinalSelect())).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        assertThatCode(() -> agencyInternalService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(agencyService.findAgencies(vitamContext, new Select().getFinalSelect())).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );

        assertThatCode(() -> agencyInternalService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(agencyService.findAgencies(vitamContext, new Select().getFinalSelect())).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.getAll(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyService.checkAbilityToCreateAgencyInVitam(any(ArrayList.class), any(String.class))).thenReturn(
            1
        );

        assertThatCode(() -> agencyInternalService.check(vitamContext, agencyDto)).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyService.checkAbilityToCreateAgencyInVitam(any(ArrayList.class), any(String.class))).thenThrow(
            new ConflictException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.check(vitamContext, agencyDto)).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyService.create(any(VitamContext.class), any(AgencyModelDto.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> agencyInternalService.create(vitamContext, agencyDto)).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_400()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyService.create(any(VitamContext.class), any(AgencyModelDto.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(() -> agencyInternalService.create(vitamContext, agencyDto)).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyService.create(any(VitamContext.class), any(AgencyModelDto.class))).thenThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.create(vitamContext, agencyDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyService.create(any(VitamContext.class), any(AgencyModelDto.class))).thenThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.create(vitamContext, agencyDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyService.create(any(VitamContext.class), any(AgencyModelDto.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.create(vitamContext, agencyDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyService.create(any(VitamContext.class), any(AgencyModelDto.class))).thenThrow(
            new IOException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.create(vitamContext, agencyDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        when(agencyService.findAgencies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        assertThatCode(() -> vitamAgencyService.deleteAgency(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        when(agencyService.findAgencies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );

        assertThatCode(() -> vitamAgencyService.deleteAgency(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        when(vitamAgencyService.deleteAgency(any(VitamContext.class), any(String.class))).thenThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        when(vitamAgencyService.deleteAgency(any(VitamContext.class), any(String.class))).thenThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        when(vitamAgencyService.deleteAgency(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        when(vitamAgencyService.deleteAgency(any(VitamContext.class), any(String.class))).thenThrow(
            new IOException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void export_should_return_ok_when_vitamclient_ok()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyService.export(any(VitamContext.class))).thenReturn(Response.status(200).build());

        assertThatCode(() -> agencyInternalService.export(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void export_should_return_ok_when_vitamclient_400()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyService.export(any(VitamContext.class))).thenReturn(Response.status(400).build());

        assertThatCode(() -> agencyInternalService.export(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void export_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyService.export(any(VitamContext.class))).thenThrow(
            new VitamClientException("Exception throxn by vitam")
        );

        assertThatCode(() -> agencyInternalService.export(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void export_should_throw_InternalServerException_when_vitamclient_throws_InvalidCreateOperationException()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyService.export(any(VitamContext.class))).thenThrow(
            new InvalidCreateOperationException("Exception throxn by vitam")
        );

        assertThatCode(() -> agencyInternalService.export(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void export_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyService.export(any(VitamContext.class))).thenThrow(
            new InvalidParseOperationException("Exception throxn by vitam")
        );

        assertThatCode(() -> agencyInternalService.export(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(200)
        );

        assertThatCode(
            () -> agencyInternalService.findHistoryByIdentifier(vitamContext, id)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(400)
        );

        assertThatCode(
            () -> agencyInternalService.findHistoryByIdentifier(vitamContext, id)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyInternalService.findHistoryByIdentifier(vitamContext, id)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    public void import_should_return_ok()
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        File file = new File("src/test/resources/data/import_agencies_valid.csv");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
            file.getName(),
            file.getName(),
            "text/csv",
            IOUtils.toByteArray(input)
        );

        String stringReponse = "{\"httpCode\":\"201\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(stringReponse);

        when(
            vitamAgencyService.importAgencies(any(VitamContext.class), any(String.class), any(MultipartFile.class))
        ).thenReturn((RequestResponse) new RequestResponseOK<JsonNode>(jsonResponse));

        assertThatCode(
            () -> agencyInternalService.importAgencies(vitamContext, file.getName(), multipartFile)
        ).doesNotThrowAnyException();
    }
}
