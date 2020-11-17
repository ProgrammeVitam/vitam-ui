package fr.gouv.vitamui.referential.service;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.OntologyDto;
import fr.gouv.vitamui.referential.external.client.OntologyExternalRestClient;
import fr.gouv.vitamui.referential.external.client.OntologyExternalWebClient;
import fr.gouv.vitamui.ui.commons.service.CommonService;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class OntologyServiceTest {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OntologyService.class);
    @Mock
    private OntologyExternalRestClient client;
    @Mock
    private OntologyExternalWebClient webClient;
    @Mock
    private CommonService commonService;
    private OntologyService service;

    @Before
    public void setUp() {
        service = new OntologyService(commonService, client, webClient);
    }

    @Test
    public void testGetAll() {
        final List<OntologyDto> ontologyDtos = new ArrayList<OntologyDto>();
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("id");
        ontologyDtos.add(ontologyDto);

        Mockito.when(client.getAll(isNull(), any(Optional.class))).thenReturn(ontologyDtos);

        final Collection<OntologyDto> ontologyList = service.getAll(null, Optional.empty());
        Assert.assertNotNull(ontologyList);
        assertThat(ontologyList).containsExactly(ontologyDto);
    }

    @Test
    public void testCheck() {
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("id");

        Mockito.when(client.check(isNull(), any(OntologyDto.class))).thenReturn(true);

        final boolean check = service.check(null, ontologyDto);
        assertThat(check).isEqualTo(true);
    }

    @Test
    public void testExport() {

        ResponseEntity<Resource> responseEntity = new ResponseEntity<Resource>(HttpStatus.OK);

        Mockito.when(client.export(isNull())).thenReturn(responseEntity);

        final ResponseEntity<Resource> response = service.export(null);
        Assert.assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    public void import_should_return_ok() throws IOException {
	    File file = new File("src/test/resources/data/import_ontologies_valid.json");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "text/csv", IOUtils.toByteArray(input));
	    
	    String stringReponse = "{\"httpCode\":\"201\"}";	 
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonResponse = mapper.readTree(stringReponse);
	    
	    Mockito.when(webClient.importOntologies(any(ExternalHttpContext.class), any(MultipartFile.class)))
        	.thenReturn(jsonResponse);
	 
        assertThatCode(() -> {
        	service.importOntologies(null, multipartFile);
        }).doesNotThrowAnyException();
    }
}
