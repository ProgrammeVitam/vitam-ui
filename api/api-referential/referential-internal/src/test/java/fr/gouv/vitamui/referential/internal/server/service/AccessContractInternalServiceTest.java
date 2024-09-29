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
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.error.VitamError;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.domain.AccessContractDto;
import fr.gouv.vitamui.commons.api.dtos.ErrorImportFile;
import fr.gouv.vitamui.commons.api.enums.ErrorImportFileMessage;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.service.VitamUIAccessContractService;
import fr.gouv.vitamui.referential.internal.server.accesscontract.AccessContractCSVUtils;
import fr.gouv.vitamui.referential.internal.server.accesscontract.AccessContractInternalService;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class AccessContractInternalServiceTest {

    @Mock
    private AccessContractService accessContractService;

    @Mock
    private VitamUIAccessContractService vitamUIAccessContractService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private ApplicationInternalRestClient applicationInternalRestClient;

    @Mock
    private InternalSecurityService internalSecurityService;

    @InjectMocks
    private AccessContractInternalService accessContractInternalService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        accessContractInternalService = new AccessContractInternalService(
            accessContractService,
            vitamUIAccessContractService,
            objectMapper,
            logbookService,
            applicationInternalRestClient,
            internalSecurityService
        );
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "id_0";

        when(accessContractService.findAccessContractById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<AccessContractModel>().setHttpCode(200)
        );

        assertThatCode(() -> accessContractInternalService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "id_0";

        when(accessContractService.findAccessContractById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<AccessContractModel>().setHttpCode(400)
        );

        assertThatCode(() -> accessContractInternalService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throw_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "id_0";

        when(accessContractService.findAccessContractById(vitamContext, identifier)).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> accessContractInternalService.getOne(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(accessContractService.findAccessContracts(vitamContext, new Select().getFinalSelect())).thenReturn(
            new RequestResponseOK<AccessContractModel>().setHttpCode(200)
        );

        assertThatCode(() -> accessContractInternalService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(accessContractService.findAccessContracts(vitamContext, new Select().getFinalSelect())).thenReturn(
            new RequestResponseOK<AccessContractModel>().setHttpCode(400)
        );

        assertThatCode(() -> accessContractInternalService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(accessContractService.findAccessContracts(vitamContext, new Select().getFinalSelect())).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> accessContractInternalService.getAll(vitamContext)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(accessContractService.findAccessContracts(vitamContext, query)).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> accessContractInternalService.findAll(vitamContext, query)).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(accessContractService.findAccessContracts(vitamContext, query)).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(() -> accessContractInternalService.findAll(vitamContext, query)).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(accessContractService.findAccessContracts(vitamContext, query)).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> accessContractInternalService.findAll(vitamContext, query)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setApplicationSessionId("ASId_0");
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(0);

        when(
            accessContractService.checkAbilityToCreateAccessContractInVitam(any(List.class), any(String.class))
        ).thenReturn(0);

        assertThatCode(
            () -> accessContractInternalService.check(vitamContext, accessContractDto)
        ).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setApplicationSessionId("ASId_0");
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(0);

        when(
            accessContractService.checkAbilityToCreateAccessContractInVitam(any(List.class), any(String.class))
        ).thenThrow(new ConflictException("Exception thrown by Vitam"));

        assertThatCode(
            () -> accessContractInternalService.check(vitamContext, accessContractDto)
        ).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setApplicationSessionId("ASId_0");
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(0);

        when(accessContractService.createAccessContracts(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(
            () -> accessContractInternalService.create(vitamContext, accessContractDto)
        ).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_BadRequestException_when_vitamclient_400()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setApplicationSessionId("ASId_0");
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(0);

        when(accessContractService.createAccessContracts(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(() -> accessContractInternalService.create(vitamContext, accessContractDto)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setApplicationSessionId("ASId_0");
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(0);

        when(accessContractService.createAccessContracts(any(VitamContext.class), any(List.class))).thenThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );

        assertThatCode(() -> accessContractInternalService.create(vitamContext, accessContractDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setApplicationSessionId("ASId_0");
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(0);

        when(accessContractService.createAccessContracts(any(VitamContext.class), any(List.class))).thenThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> accessContractInternalService.create(vitamContext, accessContractDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setApplicationSessionId("ASId_0");
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(0);

        when(accessContractService.createAccessContracts(any(VitamContext.class), any(List.class))).thenThrow(
            new IOException("Exception thrown by vitam")
        );

        assertThatCode(() -> accessContractInternalService.create(vitamContext, accessContractDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_ok()
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(vitamUIAccessContractService.patchAccessContract(any(), any(), any())).thenReturn(
            new RequestResponseOK<>()
        );

        when(accessContractService.findAccessContractById(any(), any())).thenReturn(new RequestResponseOK<>());

        Map<String, Object> partialDto = new HashMap<>(Map.of("identifier", "value"));
        assertThatCode(() -> accessContractInternalService.patch(vitamContext, partialDto)).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_400()
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(vitamUIAccessContractService.patchAccessContract(any(), any(), any())).thenReturn(
            new VitamError<>("BAD_REQUEST")
        );

        when(accessContractService.findAccessContractById(any(), any())).thenReturn(new RequestResponseOK<>());

        Map<String, Object> partialDto = new HashMap<>(Map.of("identifier", "value"));
        assertThatCode(() -> accessContractInternalService.patch(vitamContext, partialDto)).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");

        when(
            vitamUIAccessContractService.patchAccessContract(
                any(VitamContext.class),
                any(String.class),
                any(ObjectNode.class)
            )
        ).thenThrow(new InvalidParseOperationException("Exception thrown by vitam"));

        assertThatCode(() -> accessContractInternalService.patch(vitamContext, partialDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");

        when(
            vitamUIAccessContractService.patchAccessContract(
                any(VitamContext.class),
                any(String.class),
                any(ObjectNode.class)
            )
        ).thenThrow(new AccessExternalClientException("Exception thrown by vitam"));

        assertThatCode(() -> accessContractInternalService.patch(vitamContext, partialDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(200)
        );

        assertThatCode(
            () -> accessContractInternalService.findHistoryByIdentifier(vitamContext, id)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_BadRequestException_when_vitamclient_400()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenThrow(
            new BadRequestException("Exception thrown by Vitam")
        );

        assertThatCode(() -> accessContractInternalService.findHistoryByIdentifier(vitamContext, id)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> accessContractInternalService.findHistoryByIdentifier(vitamContext, id)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    public void import_should_return_ok()
        throws IOException, InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        File file = new File("src/test/resources/data/import_access_contracts_valid.csv");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
            file.getName(),
            file.getName(),
            "text/csv",
            IOUtils.toByteArray(input)
        );

        when(internalSecurityService.getHttpContext()).thenReturn(
            new InternalHttpContext(0, "", "", "", "", "", "", "")
        );

        when(
            applicationInternalRestClient.isApplicationExternalIdentifierEnabled(
                any(InternalHttpContext.class),
                eq("ACCESS_CONTRACT")
            )
        ).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        when(accessContractService.createAccessContracts(any(VitamContext.class), any(InputStream.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(
            () -> accessContractInternalService.importAccessContracts(vitamContext, multipartFile)
        ).doesNotThrowAnyException();
    }

    @Test
    public void import_should_throws_BadRequestException_when_sending_to_vitam()
        throws IOException, InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        File file = new File("src/test/resources/data/import_access_contracts_invalid_rejected_by_vitam.csv");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
            file.getName(),
            file.getName(),
            "text/csv",
            IOUtils.toByteArray(input)
        );

        when(internalSecurityService.getHttpContext()).thenReturn(
            new InternalHttpContext(0, "", "", "", "", "", "", "")
        );

        when(
            applicationInternalRestClient.isApplicationExternalIdentifierEnabled(
                any(InternalHttpContext.class),
                eq("ACCESS_CONTRACT")
            )
        ).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        when(accessContractService.createAccessContracts(any(VitamContext.class), any(InputStream.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        BadRequestException badRequestException = null;

        try {
            accessContractInternalService.importAccessContracts(vitamContext, multipartFile);
        } catch (BadRequestException e) {
            badRequestException = e;
        }

        assertThat(badRequestException).isNotNull();
        assertThat(badRequestException.getMessage()).isEqualTo("The CSV file has been rejected by vitam");
        assertThat(badRequestException.getArgs()).isEqualTo(
            List.of(
                AccessContractCSVUtils.errorToJson(
                    ErrorImportFile.builder()
                        .error(ErrorImportFileMessage.REJECT_BY_VITAM_CHECK_LOGBOOK_OPERATION_APP)
                        .build()
                )
            )
        );
    }
}
