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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.exceptions.CsvException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class IngestContractInternalServiceTest {

    private IngestContractService ingestContractService;
    private ObjectMapper objectMapper;
    private IngestContractConverter converter;
    private LogbookService logbookService;
    private ApplicationInternalRestClient applicationInternalRestClient;
    private InternalSecurityService internalSecurityService;
    private IngestContractInternalService ingestContractInternalService;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new IngestContractConverter();
        logbookService = mock(LogbookService.class);
        ingestContractService = mock(IngestContractService.class);
        applicationInternalRestClient = mock(ApplicationInternalRestClient.class);
        internalSecurityService = mock(InternalSecurityService.class);
        ingestContractInternalService = new IngestContractInternalService(
            ingestContractService,
            objectMapper,
            converter,
            logbookService,
            applicationInternalRestClient,
            internalSecurityService
        );

        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(
            ServerIdentityConfiguration.class
        );
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend())
            .andReturn("LOG TESTS IngestContractInternalServiceTest - ")
            .anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ingestContractService.findIngestContractById(isA(VitamContext.class), isA(String.class))).andReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(200)
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ingestContractService.findIngestContractById(isA(VitamContext.class), isA(String.class))).andReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(400)
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(ingestContractService.findIngestContractById(isA(VitamContext.class), isA(String.class))).andThrow(
            new VitamClientException("Exception thrown by vitam")
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(ObjectNode.class))).andReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(200)
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(ObjectNode.class))).andReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(400)
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(ObjectNode.class))).andThrow(
            new VitamClientException("Exception thrown by vitam")
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.getAll(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(JsonNode.class))).andReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(200)
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.findAll(vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(JsonNode.class))).andReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(400)
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.findAll(vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(JsonNode.class))).andThrow(
            new VitamClientException("Exception thrown by vitam")
        );
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

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andReturn(1);
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

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andThrow(new BadRequestException("Exception thrown by vitam"));
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

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andThrow(new UnavailableServiceException("Exception thrown by vitam"));
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

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andThrow(new ConflictException("Exception thrown by vitam"));
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.check(vitamContext, ingestContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andReturn(1);

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class))).andReturn(
            new RequestResponseOK().setHttpCode(200)
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andReturn(1);

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class))).andReturn(
            new RequestResponseOK().setHttpCode(400)
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andReturn(1);

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class))).andThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andReturn(1);

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class))).andThrow(
            new IOException("Exception thrown by vitam")
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        expect(
            ingestContractService.checkAbilityToCreateIngestContractInVitam(isA(List.class), isA(String.class))
        ).andReturn(1);

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class))).andThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );
        EasyMock.replay(ingestContractService);

        assertThatCode(() -> {
            ingestContractInternalService.create(vitamContext, ingestContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class))).andReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(200)
        );
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ingestContractInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class))).andReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(400)
        );
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ingestContractInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class))).andThrow(
            new VitamClientException("Exception thrown by vitam")
        );
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            ingestContractInternalService.findHistoryByIdentifier(vitamContext, id);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void import_should_return_ok()
        throws IOException, InvalidParseOperationException, AccessExternalClientException {
        //Given
        VitamContext vitamContext = new VitamContext(0);
        String fileName = "import_ingest_contracts_valid.csv";
        MultipartFile multipartFile = new MockMultipartFile(
            fileName,
            fileName,
            "text/csv",
            getClass().getResourceAsStream("/data/" + fileName)
        );

        expect(internalSecurityService.getHttpContext()).andReturn(
            new InternalHttpContext(0, "", "", "", "", "", "", "")
        );
        EasyMock.replay(internalSecurityService);

        expect(
            applicationInternalRestClient.isApplicationExternalIdentifierEnabled(
                isA(InternalHttpContext.class),
                eq("INGEST_CONTRACT")
            )
        ).andReturn(new ResponseEntity<>(false, HttpStatus.OK));
        EasyMock.replay(applicationInternalRestClient);

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class))).andReturn(
            new RequestResponseOK().setHttpCode(200)
        );
        EasyMock.replay(ingestContractService);

        //When Then
        assertThatCode(() -> {
            ingestContractInternalService.importIngestContracts(vitamContext, multipartFile);
        }).doesNotThrowAnyException();
    }

    @Test
    public void import_should_throws_BadRequestException_when_sending_to_vitam()
        throws IOException, InvalidParseOperationException, AccessExternalClientException {
        //Given
        VitamContext vitamContext = new VitamContext(0);
        String fileName = "import_ingest_contracts_invalid_wrong_ids.csv";
        MultipartFile multipartFile = new MockMultipartFile(
            fileName,
            fileName,
            "text/csv",
            getClass().getResourceAsStream("/data/" + fileName)
        );

        expect(internalSecurityService.getHttpContext()).andReturn(
            new InternalHttpContext(0, "", "", "", "", "", "", "")
        );
        EasyMock.replay(internalSecurityService);

        expect(
            applicationInternalRestClient.isApplicationExternalIdentifierEnabled(
                isA(InternalHttpContext.class),
                eq("INGEST_CONTRACT")
            )
        ).andReturn(new ResponseEntity<>(false, HttpStatus.OK));
        EasyMock.replay(applicationInternalRestClient);

        expect(ingestContractService.createIngestContracts(isA(VitamContext.class), isA(List.class))).andReturn(
            new RequestResponseOK().setHttpCode(400)
        );
        EasyMock.replay(ingestContractService);

        BadRequestException badRequestException = null;

        // When
        try {
            ingestContractInternalService.importIngestContracts(vitamContext, multipartFile);
        } catch (BadRequestException e) {
            badRequestException = e;
        }

        //Then
        assertThat(badRequestException).isNotNull();
        assertThat(badRequestException.getMessage()).isEqualTo("The CSV file has been rejected by vitam");
    }

    @Test
    public void export_should_return_ok() throws VitamClientException, IOException, CsvException {
        //Given
        VitamContext vitamContext = new VitamContext(0);

        IngestContractDto ingestContract = new IngestContractDto();
        ingestContract.setIdentifier("IC-000001");
        ingestContract.setName("Name");
        ingestContract.setDescription("Description");
        ingestContract.setStatus(ActivationStatus.ACTIVE);
        ingestContract.setArchiveProfiles(Set.of("PR-000001"));
        ingestContract.setCheckParentLink("AUTHORIZED");
        ingestContract.setCheckParentId(Set.of("CheckParentId"));
        ingestContract.setLinkParentId("LinkParentId");
        ingestContract.setFormatUnidentifiedAuthorized(true);
        ingestContract.setEveryFormatType(true);
        ingestContract.setFormatType(Set.of("FormatType"));
        ingestContract.setManagementContractId("ManagementContractId");
        ingestContract.setComputeInheritedRulesAtIngest(true);
        ingestContract.setMasterMandatory(true);
        ingestContract.setEveryDataObjectVersion(true);
        ingestContract.setDataObjectVersion(Set.of("PhysicalMaster"));
        ingestContract.setActivationDate("2023-12-31");
        ingestContract.setDeactivationDate("2023-12-31");
        List<IngestContractDto> ingestContracts = List.of(ingestContract);

        List<IngestContractModel> ingestContractModels = converter.convertDtosToVitams(ingestContracts);

        RequestResponse<IngestContractModel> requestResponse = new RequestResponseOK<>(
            JsonNodeFactory.instance.objectNode(),
            ingestContractModels,
            1
        );

        expect(ingestContractService.findIngestContracts(isA(VitamContext.class), isA(ObjectNode.class))).andReturn(
            requestResponse
        );
        EasyMock.replay(ingestContractService);

        //When
        Resource exportFile = ingestContractInternalService.exportIngestContracts(vitamContext);

        //Then
        String result = asString(exportFile);
        String expected =
            "\uFEFF\"Identifier\";\"Name\";\"Description\";\"Status\";\"ArchiveProfiles\";\"CheckParentLink\";\"CheckParentId\";\"LinkParentId\";\"FormatUnidentifiedAuthorized\";\"EveryFormatType\";\"FormatType\";\"ManagementContractId\";\"ComputedInheritedRulesAtIngest\";\"MasterMandatory\";\"EveryDataObjectVersion\";\"DataObjectVersion\";\"ActivationDate\";\"DesactivationDate\"\n" +
            "\"IC-000001\";\"Name\";\"Description\";\"ACTIVE\";\"PR-000001\";\"AUTHORIZED\";\"CheckParentId\";\"LinkParentId\";\"true\";\"true\";\"FormatType\";\"ManagementContractId\";\"true\";\"true\";\"true\";\"PhysicalMaster\";\"31/12/2023\";\"31/12/2023\"\n";
        assertThat(result).isEqualTo(expected);
    }

    private String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
