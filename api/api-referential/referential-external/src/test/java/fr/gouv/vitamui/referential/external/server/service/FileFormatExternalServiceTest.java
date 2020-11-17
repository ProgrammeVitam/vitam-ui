package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.FileFormatDto;
import fr.gouv.vitamui.referential.internal.client.FileFormatInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.FileFormatInternalWebClient;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileFormatExternalServiceTest extends ExternalServiceTest {

    @Mock
    private FileFormatInternalRestClient fileFormatInternalRestClient;
    @Mock
    private FileFormatInternalWebClient fileFormatInternalWebClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private FileFormatExternalService fileFormatExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        fileFormatExternalService = new FileFormatExternalService(externalSecurityService, fileFormatInternalRestClient, fileFormatInternalWebClient);
    }

    @Test
    public void getAll_should_return_FileFormatDtoList_when_fileFormatInternalRestClient_return_FileFormatDtoList() {
        List<FileFormatDto> list = new ArrayList<>();
        FileFormatDto fileFormatDto = new FileFormatDto();
        fileFormatDto.setId("1");
        list.add(fileFormatDto);

        when(fileFormatInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);

        assertThatCode(() -> {
            fileFormatExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_FileFormatDto_when_fileFormatInternalRestClient_return_FileFormatDto() {
        FileFormatDto fileFormatDto = new FileFormatDto();
        fileFormatDto.setId("1");

        when(fileFormatInternalRestClient.create(any(InternalHttpContext.class), any(FileFormatDto.class)))
            .thenReturn(fileFormatDto);

        assertThatCode(() -> {
            fileFormatExternalService.create(new FileFormatDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_boolean_when_fileFormatInternalRestClient_return_boolean() {
        FileFormatDto fileFormatDto = new FileFormatDto();
        fileFormatDto.setId("1");

        when(fileFormatInternalRestClient.check(any(InternalHttpContext.class), any(FileFormatDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            fileFormatExternalService.check(new FileFormatDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_fileFormatInternalRestClient_return_ok() {
        String id = "1";

        assertThatCode(() -> {
            fileFormatExternalService.delete(id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void export_should_return_ok_when_fileFormatInternalRestClient_return_ok() {
        when(fileFormatInternalRestClient.export(any(InternalHttpContext.class)))
            .thenReturn(new ResponseEntity<Resource>(HttpStatus.ACCEPTED));

        assertThatCode(() -> {
            fileFormatExternalService.export();
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void import_should_return_ok() throws IOException {
	    File file = new File("src/test/resources/data/import_fileFormats_valid.xml");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "text/csv", IOUtils.toByteArray(input));
	    
	    String stringReponse = "{\"httpCode\":\"201\"}";	 
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonResponse = mapper.readTree(stringReponse);
	    
        when(fileFormatInternalWebClient.importFileFormats(any(InternalHttpContext.class), any(String.class), any(MultipartFile.class)))
        	.thenReturn(jsonResponse);
	 
        assertThatCode(() -> {
        	fileFormatExternalService.importFileFormats(file.getName(), multipartFile);
        }).doesNotThrowAnyException();
    }
}
