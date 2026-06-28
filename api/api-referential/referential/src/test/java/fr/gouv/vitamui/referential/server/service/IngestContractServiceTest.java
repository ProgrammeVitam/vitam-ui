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
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.openapiclient.ApplicationsApi;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.dto.SignaturePolicyDto;
import fr.gouv.vitamui.referential.common.service.IngestContractCommonService;
import fr.gouv.vitamui.referential.server.service.ingestcontract.IngestContractConverter;
import fr.gouv.vitamui.referential.server.service.ingestcontract.IngestContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IngestContractServiceTest {

    @Mock
    private IngestContractCommonService ingestContractCommonService;

    @Mock
    private IngestContractConverter converter;

    @Mock
    private LogbookService logbookService;

    @Mock
    private ApplicationsApi applicationsApi;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private IngestContractService ingestContractService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new IngestContractConverter();
        ingestContractService = new IngestContractService(
            ingestContractCommonService,
            objectMapper,
            converter,
            logbookService,
            applicationsApi,
            securityService
        );
    }

    @Test
    void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ingestContractCommonService.findIngestContractById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(200)
        );

        assertThatCode(() -> ingestContractService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ingestContractCommonService.findIngestContractById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(400)
        );

        assertThatCode(() -> ingestContractService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    void getOne_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ingestContractCommonService.findIngestContractById(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> ingestContractService.getOne(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(
            ingestContractCommonService.findIngestContracts(any(VitamContext.class), any(ObjectNode.class))
        ).thenReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(200));

        assertThatCode(() -> ingestContractService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(
            ingestContractCommonService.findIngestContracts(any(VitamContext.class), any(ObjectNode.class))
        ).thenReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(400));

        assertThatCode(() -> ingestContractService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    void getAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(ingestContractCommonService.findIngestContracts(any(VitamContext.class), any(ObjectNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> ingestContractService.getAll(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(ingestContractCommonService.findIngestContracts(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(200)
        );

        assertThatCode(() -> ingestContractService.findAll(vitamContext, query)).doesNotThrowAnyException();
    }

    @Test
    void findAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(ingestContractCommonService.findIngestContracts(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<IngestContractModel>().setHttpCode(400)
        );

        assertThatCode(() -> ingestContractService.findAll(vitamContext, query)).doesNotThrowAnyException();
    }

    @Test
    void findAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(ingestContractCommonService.findIngestContracts(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> ingestContractService.findAll(vitamContext, query)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenReturn(1);

        assertThatCode(() -> ingestContractService.check(vitamContext, ingestContractDto)).doesNotThrowAnyException();
    }

    @Test
    void check_should_throw_BadRequestException_when_vitamclient_throws_BadRequestException() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenThrow(new BadRequestException("Exception thrown by vitam"));

        assertThatCode(() -> ingestContractService.check(vitamContext, ingestContractDto)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    void check_should_throw_UnavailableServiceException_when_vitamclient_throws_UnavailableServiceException() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenThrow(new UnavailableServiceException("Exception thrown by vitam"));

        assertThatCode(() -> ingestContractService.check(vitamContext, ingestContractDto)).isInstanceOf(
            UnavailableServiceException.class
        );
    }

    @Test
    void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenThrow(new ConflictException("Exception thrown by vitam"));

        assertThatCode(() -> ingestContractService.check(vitamContext, ingestContractDto)).doesNotThrowAnyException();
    }

    @Test
    void create_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenReturn(1);

        when(ingestContractCommonService.createIngestContracts(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> ingestContractService.create(vitamContext, ingestContractDto)).doesNotThrowAnyException();
    }

    @Test
    void create_should_return_ok_when_vitamclient_400()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenReturn(1);

        when(ingestContractCommonService.createIngestContracts(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(() -> ingestContractService.create(vitamContext, ingestContractDto)).doesNotThrowAnyException();
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenReturn(1);

        when(ingestContractCommonService.createIngestContracts(any(VitamContext.class), any(List.class))).thenThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> ingestContractService.create(vitamContext, ingestContractDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenReturn(1);

        when(ingestContractCommonService.createIngestContracts(any(VitamContext.class), any(List.class))).thenThrow(
            new IOException("Exception thrown by vitam")
        );

        assertThatCode(() -> ingestContractService.create(vitamContext, ingestContractDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        vitamContext.setApplicationSessionId("1");
        IngestContractDto ingestContractDto = new IngestContractDto();

        when(
            ingestContractCommonService.checkAbilityToCreateIngestContractInVitam(any(List.class), any(String.class))
        ).thenReturn(1);

        when(ingestContractCommonService.createIngestContracts(any(VitamContext.class), any(List.class))).thenThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );

        assertThatCode(() -> ingestContractService.create(vitamContext, ingestContractDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(200)
        );

        assertThatCode(
            () -> ingestContractService.findHistoryByIdentifier(vitamContext, id)
        ).doesNotThrowAnyException();
    }

    @Test
    void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(400)
        );

        assertThatCode(
            () -> ingestContractService.findHistoryByIdentifier(vitamContext, id)
        ).doesNotThrowAnyException();
    }

    @Test
    void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> ingestContractService.findHistoryByIdentifier(vitamContext, id)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    void import_should_return_ok() throws IOException, InvalidParseOperationException, AccessExternalClientException {
        //Given
        VitamContext vitamContext = new VitamContext(0);
        String fileName = "import_ingest_contracts_valid.csv";
        MultipartFile multipartFile = new MockMultipartFile(
            fileName,
            fileName,
            "text/csv",
            getClass().getResourceAsStream("/data/" + fileName)
        );

        when(securityService.getHttpContext()).thenReturn(new HttpContext(0, "", false, "", "", null, null, null));

        when(applicationsApi.isApplicationExternalIdentifierEnabled(eq("INGEST_CONTRACT"))).thenReturn(false);

        when(ingestContractCommonService.createIngestContracts(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        //When Then
        assertThatCode(
            () -> ingestContractService.importIngestContracts(vitamContext, multipartFile)
        ).doesNotThrowAnyException();
    }

    @Test
    void import_should_throws_BadRequestException_when_sending_to_vitam()
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

        when(securityService.getHttpContext()).thenReturn(new HttpContext(0, "", false, "", "", null, null, null));

        when(applicationsApi.isApplicationExternalIdentifierEnabled(eq("INGEST_CONTRACT"))).thenReturn(false);

        when(ingestContractCommonService.createIngestContracts(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        BadRequestException badRequestException = null;

        // When
        try {
            ingestContractService.importIngestContracts(vitamContext, multipartFile);
        } catch (BadRequestException e) {
            badRequestException = e;
        }

        //Then
        assertThat(badRequestException).isNotNull();
        assertThat(badRequestException.getMessage()).isEqualTo("The CSV file has been rejected by vitam");
    }

    @Test
    void export_should_return_ok() throws VitamClientException {
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
        ingestContract.setSignaturePolicy(new SignaturePolicyDto());
        ingestContract.setActivationDate("2023-12-31");
        ingestContract.setDeactivationDate("2023-12-31");
        List<IngestContractDto> ingestContracts = List.of(ingestContract);

        List<IngestContractModel> ingestContractModels = converter.convertDtosToVitams(ingestContracts);

        RequestResponse<IngestContractModel> requestResponse = new RequestResponseOK<>(
            JsonNodeFactory.instance.objectNode(),
            ingestContractModels,
            1
        );

        when(
            ingestContractCommonService.findIngestContracts(any(VitamContext.class), any(ObjectNode.class))
        ).thenReturn(requestResponse);

        //When
        Resource exportFile = ingestContractService.exportIngestContracts(vitamContext);

        //Then
        String result = asString(exportFile);
        String whened =
            """
            ﻿"Identifier";"Name";"Description";"Status";"ArchiveProfiles";"CheckParentLink";"CheckParentId";"LinkParentId";"FormatUnidentifiedAuthorized";"EveryFormatType";"FormatType";"ManagementContractId";"ComputedInheritedRulesAtIngest";"MasterMandatory";"EveryDataObjectVersion";"DataObjectVersion";"SignedDocument";"SigningRole";"ActivationDate";"DesactivationDate"
            "IC-000001";"Name";"Description";"ACTIVE";"PR-000001";"AUTHORIZED";"CheckParentId";"LinkParentId";"true";"true";"FormatType";"ManagementContractId";"true";"true";"true";"PhysicalMaster";"ALLOWED";"";"31/12/2023";"31/12/2023"
            """;
        assertThat(result).isEqualTo(whened);
    }

    private String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
