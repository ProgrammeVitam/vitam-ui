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
package fr.gouv.vitamui.referential.server.service;

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
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyCommonService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.common.service.VitamAgencyCommonService;
import fr.gouv.vitamui.referential.server.service.agency.AgencyService;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgencyServiceTest {

    @Mock
    private AgencyCommonService agencyCommonService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private VitamAgencyCommonService vitamAgencyCommonService;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private AgencyService agencyService;

    @BeforeEach
    public void setUp() {
        Mockito.when(securityService.getVitamContext()).thenReturn(new VitamContext(10));
        Mockito.when(securityService.getHttpContext()).thenReturn(
            new HttpContext(10, "userToken", false, "applicationId", "id", null, null, null)
        );
        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        agencyService = new AgencyService(
            agencyCommonService,
            objectMapper,
            logbookService,
            vitamAgencyCommonService,
            securityService
        );
    }

    @Test
    void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(agencyCommonService.findAgencyById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        assertThatCode(() -> agencyService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    void getOne_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(agencyCommonService.findAgencyById(vitamContext, identifier)).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );

        assertThatCode(() -> agencyService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    void getOne_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(agencyCommonService.findAgencyById(vitamContext, identifier)).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.getOne(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(agencyCommonService.findAgencies(vitamContext, new Select().getFinalSelect())).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        assertThatCode(() -> agencyService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    void getAll_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(agencyCommonService.findAgencies(vitamContext, new Select().getFinalSelect())).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );

        assertThatCode(() -> agencyService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    void getAll_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(agencyCommonService.findAgencies(vitamContext, new Select().getFinalSelect())).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.getAll(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(
            vitamAgencyCommonService.checkAbilityToCreateAgencyInVitam(any(ArrayList.class), any(String.class))
        ).thenReturn(1);

        assertThatCode(() -> agencyService.check(vitamContext, agencyDto)).doesNotThrowAnyException();
    }

    @Test
    void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(
            vitamAgencyCommonService.checkAbilityToCreateAgencyInVitam(any(ArrayList.class), any(String.class))
        ).thenThrow(new ConflictException("Exception thrown by vitam"));

        assertThatCode(() -> agencyService.check(vitamContext, agencyDto)).doesNotThrowAnyException();
    }

    @Test
    void create_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyCommonService.create(any(VitamContext.class), any(AgenciesModel.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> agencyService.create(vitamContext, agencyDto)).doesNotThrowAnyException();
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_400()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyCommonService.create(any(VitamContext.class), any(AgenciesModel.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(() -> agencyService.create(vitamContext, agencyDto)).doesNotThrowAnyException();
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyCommonService.create(any(VitamContext.class), any(AgenciesModel.class))).thenThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.create(vitamContext, agencyDto)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyCommonService.create(any(VitamContext.class), any(AgenciesModel.class))).thenThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.create(vitamContext, agencyDto)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyCommonService.create(any(VitamContext.class), any(AgenciesModel.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.create(vitamContext, agencyDto)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("1");

        when(vitamAgencyCommonService.create(any(VitamContext.class), any(AgenciesModel.class))).thenThrow(
            new IOException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.create(vitamContext, agencyDto)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void delete_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        assertThatCode(
            () -> vitamAgencyCommonService.deleteAgency(vitamContext, identifier)
        ).doesNotThrowAnyException();
    }

    @Test
    void delete_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("ASId_1");
        String identifier = "identifier";

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );

        assertThatCode(
            () -> vitamAgencyCommonService.deleteAgency(vitamContext, identifier)
        ).doesNotThrowAnyException();
    }

    @Test
    void delete_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        String identifier = "identifier";

        when(vitamAgencyCommonService.deleteAgency(any(VitamContext.class), any(String.class))).thenThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.delete(identifier)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void delete_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        String identifier = "identifier";

        when(vitamAgencyCommonService.deleteAgency(any(VitamContext.class), any(String.class))).thenThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.delete(identifier)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void delete_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        String identifier = "identifier";

        when(vitamAgencyCommonService.deleteAgency(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.delete(identifier)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void delete_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        String identifier = "identifier";

        when(vitamAgencyCommonService.deleteAgency(any(VitamContext.class), any(String.class))).thenThrow(
            new IOException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.delete(identifier)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void export_should_return_ok_when_vitamclient_ok()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyCommonService.export(any(VitamContext.class))).thenReturn(Response.status(200).build());

        assertThatCode(() -> agencyService.export(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    void export_should_return_ok_when_vitamclient_400()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyCommonService.export(any(VitamContext.class))).thenReturn(Response.status(400).build());

        assertThatCode(() -> agencyService.export(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    void export_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyCommonService.export(any(VitamContext.class))).thenThrow(
            new VitamClientException("Exception throxn by vitam")
        );

        assertThatCode(() -> agencyService.export(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void export_should_throw_InternalServerException_when_vitamclient_throws_InvalidCreateOperationException()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyCommonService.export(any(VitamContext.class))).thenThrow(
            new InvalidCreateOperationException("Exception throxn by vitam")
        );

        assertThatCode(() -> agencyService.export(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void export_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);

        when(vitamAgencyCommonService.export(any(VitamContext.class))).thenThrow(
            new InvalidParseOperationException("Exception throxn by vitam")
        );

        assertThatCode(() -> agencyService.export(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(200)
        );

        assertThatCode(() -> agencyService.findHistoryByIdentifier(vitamContext, id)).doesNotThrowAnyException();
    }

    @Test
    void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(400)
        );

        assertThatCode(() -> agencyService.findHistoryByIdentifier(vitamContext, id)).doesNotThrowAnyException();
    }

    @Test
    void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> agencyService.findHistoryByIdentifier(vitamContext, id)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    void import_should_return_ok()
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
            vitamAgencyCommonService.importAgencies(
                any(VitamContext.class),
                any(String.class),
                any(MultipartFile.class)
            )
        ).thenReturn((RequestResponse) new RequestResponseOK<JsonNode>(jsonResponse));

        assertThatCode(() -> agencyService.importAgencies(file.getName(), multipartFile)).doesNotThrowAnyException();
    }
}
