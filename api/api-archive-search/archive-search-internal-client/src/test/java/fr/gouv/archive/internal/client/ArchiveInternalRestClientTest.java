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

package fr.gouv.archive.internal.client;

import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArchiveInternalRestClientTest {

    String baseUrl = "https://tests" + RestApi.ARCHIVE_SEARCH_PATH;
    InternalHttpContext defaultContext = new InternalHttpContext(9, "", "", "", "", "", "", "");
    private ArchiveInternalRestClient archiveInternalRestClient;
    public final String ARCHIVE_UNITS_RESULTS_CSV = "data/vitam_archive_units_response.csv";

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        archiveInternalRestClient = new ArchiveInternalRestClient(restTemplate, baseUrl);
    }

    @Test
    public void sampleArchiveTest() {
        Assertions.assertNotNull(archiveInternalRestClient);
        assertEquals(RestApi.ARCHIVE_SEARCH_PATH, archiveInternalRestClient.getPathUrl());
    }

    @Test
    public void when_searchArchiveUnitsByCriteria_rest_template_ok_should_return_ok() {
        SearchCriteriaDto query = new SearchCriteriaDto();

        final ArchiveUnitsDto responseEntity = new ArchiveUnitsDto();

        when(
            restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(ArchiveUnitsDto.class))
        ).thenReturn(new ResponseEntity<>(responseEntity, HttpStatus.OK));
        ArchiveUnitsDto response = archiveInternalRestClient.searchArchiveUnitsByCriteria(defaultContext, query);
        assertEquals(response, responseEntity);
    }

    @Test
    public void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto() {
        final VitamUISearchResponseDto responseEntity = new VitamUISearchResponseDto();

        when(
            restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(VitamUISearchResponseDto.class)
            )
        ).thenReturn(new ResponseEntity<>(responseEntity, HttpStatus.OK));
        VitamUISearchResponseDto response = archiveInternalRestClient.getFilingHoldingScheme(defaultContext);
        assertEquals(response, responseEntity);
    }

    @Test
    public void whenGetexportCsvArchiveUnitsByCriteria_Srvc_ok_ThenShouldReturnOK() throws IOException {
        SearchCriteriaDto query = new SearchCriteriaDto();

        Resource resource = new ByteArrayResource(
            ArchiveInternalRestClientTest.class.getClassLoader()
                .getResourceAsStream(ARCHIVE_UNITS_RESULTS_CSV)
                .readAllBytes()
        );

        when(
            restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Resource.class))
        ).thenReturn(new ResponseEntity<>(resource, HttpStatus.OK));

        Resource response = archiveInternalRestClient.exportCsvArchiveUnitsByCriteria(query, defaultContext);

        assertEquals(response, resource);
    }

    @Test
    public void findUnitsByPersistentIdentifier_ok() throws URISyntaxException {
        // Given
        String arkId = "ark:/225867/001a9d7db5eghxac";
        PersistentIdentifierResponseDto result = new PersistentIdentifierResponseDto();
        URI uri = new URI(
            archiveInternalRestClient.getBaseUrl() +
            archiveInternalRestClient.getPathUrl() +
            RestApi.UNITS_PERSISTENT_IDENTIFIER +
            "?id=ark:/225867/001a9d7db5eghxac"
        );
        when(
            restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))
        ).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
        // When
        PersistentIdentifierResponseDto persistentIdentifierResponse =
            archiveInternalRestClient.findUnitsByPersistentIdentifier(arkId, defaultContext);
        // Then
        assertEquals(persistentIdentifierResponse, result);
        verify(restTemplate).exchange(
            eq(uri),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(PersistentIdentifierResponseDto.class)
        );
    }

    @Test
    public void findObjectsByPersistentIdentifier_ok() throws URISyntaxException {
        // Given
        final String arkId = "ark:/225867/001a9d7db5eghxac_binary_master";
        final PersistentIdentifierResponseDto result = new PersistentIdentifierResponseDto();
        final URI uri = new URI(
            archiveInternalRestClient.getBaseUrl() +
            archiveInternalRestClient.getPathUrl() +
            RestApi.OBJECTS_PERSISTENT_IDENTIFIER +
            "?id=ark:/225867/001a9d7db5eghxac_binary_master"
        );
        when(
            restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))
        ).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
        // When
        final PersistentIdentifierResponseDto persistentIdentifierResponse =
            archiveInternalRestClient.findObjectsByPersistentIdentifier(arkId, defaultContext);
        // Then
        assertEquals(persistentIdentifierResponse, result);
        verify(restTemplate).exchange(
            eq(uri),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(PersistentIdentifierResponseDto.class)
        );
    }
}
