package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.OntologyDto;
import fr.gouv.vitamui.referential.internal.client.OntologyInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.OntologyInternalWebClient;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OntologyExternalServiceTest extends ExternalServiceTest {

    @Mock
    private OntologyInternalRestClient ontologyInternalRestClient;
    @Mock
    private OntologyInternalWebClient ontologyInternalWebClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private OntologyExternalService ontologyExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        ontologyExternalService = new OntologyExternalService(externalSecurityService, ontologyInternalRestClient, ontologyInternalWebClient);
    }

    @Test
    public void getAll_should_return_OntologyDtoList_when_ontologyInternalRestClient_return_OntologyDtoList() {
        List<OntologyDto> list = new ArrayList<>();
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");
        list.add(ontologyDto);

        when(ontologyInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);

        assertThatCode(() -> {
            ontologyExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_OntologyDto_when_ontologyInternalRestClient_return_OntologyDto() {
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        when(ontologyInternalRestClient.create(any(InternalHttpContext.class), any(OntologyDto.class)))
            .thenReturn(ontologyDto);

        assertThatCode(() -> {
            ontologyExternalService.create(new OntologyDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_OntologyDto_when_ontologyInternalRestClient_return_OntologyDto() {
        doNothing().when(ontologyInternalRestClient).delete(any(InternalHttpContext.class), any(String.class));
        String id = "1";

        assertThatCode(() -> {
            ontologyExternalService.delete(id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_boolean_when_ontologyInternalRestClient_return_boolean() {
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        when(ontologyInternalRestClient.check(any(InternalHttpContext.class), any(OntologyDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            ontologyExternalService.check(ontologyDto);
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void import_should_return_ok() throws IOException {
	    File file = new File("src/test/resources/data/import_ontologies_valid.json");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "text/csv", IOUtils.toByteArray(input));
	    
	    String stringReponse = "{\"httpCode\":\"201\"}";	 
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonResponse = mapper.readTree(stringReponse);
	    
        when(ontologyInternalWebClient.importOntologies(any(InternalHttpContext.class), any(String.class), any(MultipartFile.class)))
        	.thenReturn(jsonResponse);
	 
        assertThatCode(() -> {
        	ontologyExternalService.importOntologies(file.getName(), multipartFile);
        }).doesNotThrowAnyException();
    }
}
