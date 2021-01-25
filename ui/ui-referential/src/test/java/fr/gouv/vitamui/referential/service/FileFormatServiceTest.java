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
package fr.gouv.vitamui.referential.service;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.FileFormatDto;
import fr.gouv.vitamui.referential.external.client.FileFormatExternalRestClient;
import fr.gouv.vitamui.referential.external.client.FileFormatExternalWebClient;
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
public class FileFormatServiceTest {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(FileFormatService.class);
    @Mock
    private FileFormatExternalRestClient client;
    @Mock
    private FileFormatExternalWebClient webClient;

    @Mock
    private CommonService commonService;
    private FileFormatService service;

    @Before
    public void setUp() {
        service = new FileFormatService(commonService, client, webClient);
    }

    @Test
    public void testGetAll() {
        final List<FileFormatDto> fileFormatDtos = new ArrayList<FileFormatDto>();
        FileFormatDto fileFormatDto = new FileFormatDto();
        fileFormatDto.setId("id");
        fileFormatDtos.add(fileFormatDto);

        Mockito.when(client.getAll(isNull(), any(Optional.class))).thenReturn(fileFormatDtos);

        final Collection<FileFormatDto> fileFormatList = service.getAll(null, Optional.empty());
        Assert.assertNotNull(fileFormatList);
        assertThat(fileFormatList).containsExactly(fileFormatDto);
    }

    @Test
    public void testCheck() {
        FileFormatDto fileFormatDto = new FileFormatDto();
        fileFormatDto.setId("id");

        Mockito.when(client.check(isNull(), any(FileFormatDto.class))).thenReturn(true);

        final boolean check = service.check(null, fileFormatDto);
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
	    File file = new File("src/test/resources/data/import_fileFormats_valid.xml");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "text/csv", IOUtils.toByteArray(input));

	    String stringReponse = "{\"httpCode\":\"201\"}";
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonResponse = mapper.readTree(stringReponse);

	    Mockito.when(webClient.importFileFormats(any(ExternalHttpContext.class), any(MultipartFile.class)))
        	.thenReturn(jsonResponse);

        assertThatCode(() -> {
        	service.importFileFormats(null, multipartFile);
        }).doesNotThrowAnyException();
    }
}
