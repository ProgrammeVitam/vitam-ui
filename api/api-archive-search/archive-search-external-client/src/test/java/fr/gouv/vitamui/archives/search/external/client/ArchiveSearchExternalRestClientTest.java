/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.archives.search.external.client;

import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.extension.ServerIdentityExtension;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArchiveSearchExternalRestClientTest extends ServerIdentityExtension {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchExternalRestClientTest.class);

    public final String ARCHIVE_UNITS_RESULTS_CSV = "data/vitam_archive_units_response.csv";


    private ArchiveSearchExternalRestClient archiveSearchExternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        archiveSearchExternalRestClient =
            new ArchiveSearchExternalRestClient(restTemplate, RestApi.ARCHIVE_SEARCH_PATH);
    }

    @Test
    public void sampleArchiveTest() {
        Assertions.assertNotNull(archiveSearchExternalRestClient);
        Assertions.assertEquals(archiveSearchExternalRestClient.getPathUrl(), RestApi.ARCHIVE_SEARCH_PATH);
    }


    @Test
    public void when_searchArchiveUnitsByCriteria_rest_template_ok_should_return_ok() {
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        SearchCriteriaDto query = new SearchCriteriaDto();
        MultiValueMap<String, String> headers = archiveSearchExternalRestClient.buildSearchHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        final ArchiveUnitsDto responseEntity = new ArchiveUnitsDto();

        when(restTemplate
            .exchange(anyString(), eq(HttpMethod.POST), eq(request), eq(ArchiveUnitsDto.class)))
            .thenReturn(new ResponseEntity<>(responseEntity, HttpStatus.OK));

        ArchiveUnitsDto response =
            archiveSearchExternalRestClient.searchArchiveUnitsByCriteria(context, query);

        Assertions.assertEquals(response, responseEntity);
    }

    @Test
    public void whenGetFilingHoldingSChemeRestTemplateOKThenShouldReturnOK() {
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        MultiValueMap<String, String> headers = archiveSearchExternalRestClient.buildSearchHeaders(context);
        final HttpEntity<Void> request = new HttpEntity<>(headers);
        final VitamUISearchResponseDto responseEntity = new VitamUISearchResponseDto();

        when(restTemplate
            .exchange(anyString(), eq(HttpMethod.GET), eq(request), eq(VitamUISearchResponseDto.class)))
            .thenReturn(new ResponseEntity<>(responseEntity, HttpStatus.OK));

        VitamUISearchResponseDto response =
            archiveSearchExternalRestClient.getFilingHoldingScheme(context);

        Assertions.assertEquals(response, responseEntity);
    }


    @Test
    public void whenGetexportCsvArchiveUnitsByCriteria_Srvc_ok_ThenShouldReturnOK() throws IOException {
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        MultiValueMap<String, String> headers = archiveSearchExternalRestClient.buildSearchHeaders(context);
        SearchCriteriaDto query = new SearchCriteriaDto();
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);

        Resource resource = new ByteArrayResource(ArchiveSearchExternalRestClientTest.class.getClassLoader()
            .getResourceAsStream(ARCHIVE_UNITS_RESULTS_CSV).readAllBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
            eq(request), eq(Resource.class))).thenReturn(new ResponseEntity<>(resource, HttpStatus.OK));

        ResponseEntity<Resource> response =
            archiveSearchExternalRestClient.exportCsvArchiveUnitsByCriteria(query, context);

        Assertions.assertEquals(response.getBody(), resource);
    }

    @Test
    public void transferRequest_should_return_OK() {
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        MultiValueMap<String, String> headers = archiveSearchExternalRestClient.buildSearchHeaders(context);
        TransferRequestDto transferRequestDto = new TransferRequestDto();
        final HttpEntity<TransferRequestDto> request = new HttpEntity<>(transferRequestDto, headers);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), eq(request), any(Class.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        // When
        ResponseEntity<String> response = archiveSearchExternalRestClient.transferRequest(transferRequestDto, context);

        org.assertj.core.api.Assertions.assertThat(response).isNotNull();
        org.assertj.core.api.Assertions.assertThat(response.getBody()).isEqualTo("OK");
    }
}
