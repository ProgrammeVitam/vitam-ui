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
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.FileFormatModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dto.FileFormatDto;
import fr.gouv.vitamui.referential.common.service.VitamFileFormatService;
import fr.gouv.vitamui.referential.internal.server.fileformat.FileFormatConverter;
import fr.gouv.vitamui.referential.internal.server.fileformat.FileFormatInternalService;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class FileFormatInternalServiceTest {

    private ObjectMapper objectMapper;
    private FileFormatConverter converter;
    private LogbookService logbookService;
    private VitamFileFormatService vitamFileFormatService;
    private FileFormatInternalService fileFormatInternalService;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new FileFormatConverter();
        logbookService = mock(LogbookService.class);
        vitamFileFormatService = mock(VitamFileFormatService.class);
        fileFormatInternalService = new FileFormatInternalService(objectMapper, converter, logbookService, vitamFileFormatService);


        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS FileFormatInternalServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<FileFormatModel>().setHttpCode(200));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<FileFormatModel>().setHttpCode(400));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(vitamFileFormatService.findFileFormats(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<FileFormatModel>().setHttpCode(200));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(vitamFileFormatService.findFileFormats(isA(VitamContext.class), isA(ObjectNode.class)))
            .andReturn(new RequestResponseOK<FileFormatModel>().setHttpCode(400));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(vitamFileFormatService.findFileFormats(isA(VitamContext.class), isA(ObjectNode.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.getAll(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.checkAbilityToCreateFileFormatInVitam(isA(List.class), isA(VitamContext.class)))
            .andReturn(true);
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.check(vitamContext, accessContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException()  {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.checkAbilityToCreateFileFormatInVitam(isA(List.class), isA(VitamContext.class)))
            .andThrow(new ConflictException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.check(vitamContext, accessContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.create(isA(VitamContext.class), isA(FileFormatModel.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.create(vitamContext, accessContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.create(isA(VitamContext.class), isA(FileFormatModel.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.create(vitamContext, accessContractDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_JAXBException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.create(isA(VitamContext.class), isA(FileFormatModel.class)))
            .andThrow(new JAXBException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.create(vitamContext, accessContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.create(isA(VitamContext.class), isA(FileFormatModel.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.create(vitamContext, accessContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.create(isA(VitamContext.class), isA(FileFormatModel.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.create(vitamContext, accessContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.create(isA(VitamContext.class), isA(FileFormatModel.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.create(vitamContext, accessContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        FileFormatDto accessContractDto = new FileFormatDto();

        expect(vitamFileFormatService.create(isA(VitamContext.class), isA(FileFormatModel.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.create(vitamContext, accessContractDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_ok() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setAccessContract("accessContract");
        vitamContext.setApplicationSessionId("applicationSessionId");
        String puid = "EXTERNAL_LOGBOOK";
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("puid", "EXTERNAL_LOGBOOK");

        RequestResponseOK<FileFormatModel> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.setHits(1,1,1,1);
        response.addResult(new FileFormatModel());

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(response).anyTimes();

        expect(vitamFileFormatService.patchFileFormat(isA(VitamContext.class), isA(String.class), isA(FileFormatModel.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200)).anyTimes();
        EasyMock.replay(vitamFileFormatService);


        assertThatCode(() -> {
            fileFormatInternalService.patch(vitamContext, partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_400() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setAccessContract("accessContract");
        vitamContext.setApplicationSessionId("applicationSessionId");
        String puid = "EXTERNAL_LOGBOOK";
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("puid", "EXTERNAL_LOGBOOK");

        RequestResponseOK<FileFormatModel> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.setHits(1,1,1,1);
        response.addResult(new FileFormatModel());

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(response).anyTimes();

        expect(vitamFileFormatService.patchFileFormat(isA(VitamContext.class), isA(String.class), isA(FileFormatModel.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.patch(vitamContext, partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_JAXBException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setAccessContract("accessContract");
        vitamContext.setApplicationSessionId("applicationSessionId");
        String puid = "EXTERNAL_LOGBOOK";
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("puid", "EXTERNAL_LOGBOOK");

        RequestResponseOK<FileFormatModel> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.setHits(1,1,1,1);
        response.addResult(new FileFormatModel());

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(response).anyTimes();

        expect(vitamFileFormatService.patchFileFormat(isA(VitamContext.class), isA(String.class), isA(FileFormatModel.class)))
            .andThrow(new JAXBException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setAccessContract("accessContract");
        vitamContext.setApplicationSessionId("applicationSessionId");
        String puid = "EXTERNAL_LOGBOOK";
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("puid", "EXTERNAL_LOGBOOK");

        RequestResponseOK<FileFormatModel> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.setHits(1,1,1,1);
        response.addResult(new FileFormatModel());

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(response).anyTimes();

        expect(vitamFileFormatService.patchFileFormat(isA(VitamContext.class), isA(String.class), isA(FileFormatModel.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setAccessContract("accessContract");
        vitamContext.setApplicationSessionId("applicationSessionId");
        String puid = "EXTERNAL_LOGBOOK";
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("puid", "EXTERNAL_LOGBOOK");

        RequestResponseOK<FileFormatModel> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.setHits(1,1,1,1);
        response.addResult(new FileFormatModel());

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(response).anyTimes();

        expect(vitamFileFormatService.patchFileFormat(isA(VitamContext.class), isA(String.class), isA(FileFormatModel.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setAccessContract("accessContract");
        vitamContext.setApplicationSessionId("applicationSessionId");
        String puid = "EXTERNAL_LOGBOOK";
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("puid", "EXTERNAL_LOGBOOK");

        RequestResponseOK<FileFormatModel> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.setHits(1,1,1,1);
        response.addResult(new FileFormatModel());

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(response).anyTimes();

        expect(vitamFileFormatService.patchFileFormat(isA(VitamContext.class), isA(String.class), isA(FileFormatModel.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        vitamContext.setAccessContract("accessContract");
        vitamContext.setApplicationSessionId("applicationSessionId");
        String puid = "EXTERNAL_LOGBOOK";
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("puid", "EXTERNAL_LOGBOOK");

        RequestResponseOK<FileFormatModel> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.setHits(1,1,1,1);
        response.addResult(new FileFormatModel());

        expect(vitamFileFormatService.findFileFormatById(isA(VitamContext.class), isA(String.class)))
            .andReturn(response).anyTimes();

        expect(vitamFileFormatService.patchFileFormat(isA(VitamContext.class), isA(String.class), isA(FileFormatModel.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_ok() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "EXTERNAL_0";

        expect(vitamFileFormatService.deleteFileFormat(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK().setHttpCode(200));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.delete(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_400() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "EXTERNAL_0";

        expect(vitamFileFormatService.deleteFileFormat(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK().setHttpCode(400));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.delete(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_JAXBException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "EXTERNAL_0";

        expect(vitamFileFormatService.deleteFileFormat(isA(VitamContext.class), isA(String.class)))
            .andThrow(new JAXBException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "EXTERNAL_0";

        expect(vitamFileFormatService.deleteFileFormat(isA(VitamContext.class), isA(String.class)))
            .andThrow(new AccessExternalClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "EXTERNAL_0";

        expect(vitamFileFormatService.deleteFileFormat(isA(VitamContext.class), isA(String.class)))
            .andThrow(new InvalidParseOperationException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "EXTERNAL_0";

        expect(vitamFileFormatService.deleteFileFormat(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_IOException() throws JAXBException, AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "EXTERNAL_0";

        expect(vitamFileFormatService.deleteFileFormat(isA(VitamContext.class), isA(String.class)))
            .andThrow(new IOException("Exception thrown by vitam"));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
            fileFormatInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(200));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            fileFormatInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(400));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            fileFormatInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        expect(logbookService.findEventsByIdentifierAndCollectionNames(isA(String.class), isA(String.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(logbookService);

        assertThatCode(() -> {
            fileFormatInternalService.findHistoryByIdentifier(vitamContext, id);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void import_should_return_ok() throws
    InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
    	VitamContext vitamContext = new VitamContext(1);
	    File file = new File("src/test/resources/data/import_fileFormats_valid.xml");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "text/csv", IOUtils.toByteArray(input));

	    String stringReponse = "{\"httpCode\":\"201\"}";
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonResponse = mapper.readTree(stringReponse);

        expect(vitamFileFormatService.importFileFormats(isA(VitamContext.class), isA(String.class), isA(MultipartFile.class)))
    	    .andReturn((RequestResponse) new RequestResponseOK<JsonNode>(jsonResponse));
        EasyMock.replay(vitamFileFormatService);

        assertThatCode(() -> {
        	fileFormatInternalService.importFileFormats(vitamContext, file.getName(), multipartFile);
        }).doesNotThrowAnyException();
    }
}
