/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
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
package fr.gouv.vitamui.collect.internal.client;

import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.test.extension.ServerIdentityExtension;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.ARCHIVE_UNIT_INFO;
import static fr.gouv.vitamui.archives.search.common.rest.RestApi.EXPORT_CSV_SEARCH_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.ABORT_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.ARCHIVE_UNITS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_TRANSACTION_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.REOPEN_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.SEND_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.VALIDATE_PATH;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_ACCESS_CONTRACT_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_APPLICATION_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_CUSTOMER_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_IDENTITY_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_REQUEST_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_TENANT_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_LEVEL_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_TOKEN_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CollectTransactionInternalRestClientTest extends ServerIdentityExtension {

    private CollectTransactionInternalRestClient collectTransactionInternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    final PodamFactory factory = new PodamFactoryImpl();

    private final String BASE_URL = "http://localhost:7090";
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String TRANSACTION_ID = "TRANSACTION_ID";
    private static final String UNIT_ID = "UNI_ID";

    private static final String OBJECT_ID = "OBJECT_ID";

    @BeforeEach
    public void setUp() {
        collectTransactionInternalRestClient = new CollectTransactionInternalRestClient(restTemplate, BASE_URL);
    }

    @Test
    public void checkGetBaseUrlWithSuccess() {
        assertNotNull(collectTransactionInternalRestClient);
        assertThat(collectTransactionInternalRestClient.getPathUrl()).isEqualTo(COLLECT_TRANSACTION_PATH);
    }

    @Test
    public void shouldSendTransactionWithSuccess() {
        // GIVEN
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_TRANSACTION_PATH + "/" + PROJECT_ID + SEND_PATH),
                HttpMethod.PUT,
                new HttpEntity<>(params.getValue()),
                Void.class
            )
        ).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        // THEN
        assertDoesNotThrow(() -> collectTransactionInternalRestClient.sendTransaction(params.getKey(), PROJECT_ID));
    }

    @Test
    public void shouldValidateTransactionWithSuccess() {
        // GIVEN
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_TRANSACTION_PATH + "/" + PROJECT_ID + VALIDATE_PATH),
                HttpMethod.PUT,
                new HttpEntity<>(params.getValue()),
                Void.class
            )
        ).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        // THEN
        assertDoesNotThrow(() -> collectTransactionInternalRestClient.validateTransaction(params.getKey(), PROJECT_ID));
    }

    @Test
    public void shouldReopenTransactionWithSuccess() {
        // GIVEN
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_TRANSACTION_PATH + "/" + PROJECT_ID + REOPEN_PATH),
                HttpMethod.PUT,
                new HttpEntity<>(params.getValue()),
                Void.class
            )
        ).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        // THEN
        assertDoesNotThrow(() -> collectTransactionInternalRestClient.reopenTransaction(params.getKey(), PROJECT_ID));
    }

    @Test
    public void shouldAbortTransactionWithSuccess() {
        // GIVEN
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_TRANSACTION_PATH + "/" + PROJECT_ID + ABORT_PATH),
                HttpMethod.PUT,
                new HttpEntity<>(params.getValue()),
                Void.class
            )
        ).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        // THEN
        assertDoesNotThrow(() -> collectTransactionInternalRestClient.abortTransaction(params.getKey(), PROJECT_ID));
    }

    @Test
    public void shouldGetTransactionByIdWithSuccess() {
        // GIVEN
        CollectTransactionDto collectTransactionDto = factory.manufacturePojo(CollectTransactionDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_TRANSACTION_PATH + "/" + TRANSACTION_ID),
                HttpMethod.GET,
                new HttpEntity<>(params.getValue()),
                CollectTransactionDto.class
            )
        ).thenReturn(new ResponseEntity<>(collectTransactionDto, HttpStatus.OK));
        // WHEN
        CollectTransactionDto response = collectTransactionInternalRestClient.getTransactionById(
            params.getKey(),
            TRANSACTION_ID
        );
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(CollectTransactionDto.class);
        assertEquals(collectTransactionDto.getId(), response.getId());
        assertEquals(collectTransactionDto.getProjectId(), response.getProjectId());
        assertEquals(collectTransactionDto.getName(), response.getName());
    }

    @Test
    public void shouldUpdateTransactionWithSuccess() {
        // GIVEN
        CollectTransactionDto collectTransactionDto = factory.manufacturePojo(CollectTransactionDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                BASE_URL + COLLECT_TRANSACTION_PATH,
                HttpMethod.PUT,
                new HttpEntity<>(collectTransactionDto, params.getValue()),
                CollectTransactionDto.class
            )
        ).thenReturn(new ResponseEntity<>(collectTransactionDto, HttpStatus.OK));
        // WHEN
        CollectTransactionDto response = collectTransactionInternalRestClient.updateTransaction(
            params.getKey(),
            collectTransactionDto
        );
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(CollectTransactionDto.class);
        assertEquals(collectTransactionDto.getId(), response.getId());
        assertEquals(collectTransactionDto.getProjectId(), response.getProjectId());
        assertEquals(collectTransactionDto.getName(), response.getName());
    }

    @Test
    public void shouldGetUnitByIdWithSuccess() {
        // GIVEN
        ResultsDto resultsDto = factory.manufacturePojo(ResultsDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_TRANSACTION_PATH + ARCHIVE_UNIT_INFO + "/" + UNIT_ID),
                HttpMethod.GET,
                new HttpEntity<>(params.getValue()),
                ResultsDto.class
            )
        ).thenReturn(new ResponseEntity<>(resultsDto, HttpStatus.OK));
        // WHEN
        ResponseEntity<ResultsDto> response = collectTransactionInternalRestClient.findUnitById(
            UNIT_ID,
            params.getKey()
        );
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(ResponseEntity.class);
        assertThat(response.getBody()).isInstanceOf(ResultsDto.class);
        assertEquals(resultsDto.getId(), response.getBody().getId());
        assertEquals(resultsDto.getDescription(), response.getBody().getDescription());
        assertEquals(resultsDto.getNbobjects(), response.getBody().getNbobjects());
    }

    @Test
    public void shouldSearchCollectProjectArchiveUnitsWithSuccess() {
        // GIVEN
        ArchiveUnitsDto archiveUnitsDto = factory.manufacturePojo(ArchiveUnitsDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        final SearchCriteriaDto searchQuery = new SearchCriteriaDto();
        when(
            restTemplate.exchange(
                BASE_URL + COLLECT_TRANSACTION_PATH + "/" + PROJECT_ID + ARCHIVE_UNITS,
                HttpMethod.POST,
                new HttpEntity<>(searchQuery, params.getValue()),
                ArchiveUnitsDto.class
            )
        ).thenReturn(new ResponseEntity<>(archiveUnitsDto, HttpStatus.OK));
        // WHEN
        ArchiveUnitsDto response = collectTransactionInternalRestClient.searchCollectProjectArchiveUnits(
            params.getKey(),
            PROJECT_ID,
            searchQuery
        );
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(ArchiveUnitsDto.class);
        assertNotNull(response.getArchives());
        assertNotNull(response.getArchives().getResults());
        assertEquals(5, response.getArchives().getResults().size());
        assertEquals(
            archiveUnitsDto.getArchives().getResults().get(0).getId(),
            response.getArchives().getResults().get(0).getId()
        );
        assertEquals(
            archiveUnitsDto.getArchives().getResults().get(0).getDescription(),
            response.getArchives().getResults().get(0).getDescription()
        );
        assertEquals(
            archiveUnitsDto.getArchives().getResults().get(0).getOriginatingAgencyName(),
            response.getArchives().getResults().get(0).getOriginatingAgencyName()
        );
        assertEquals(
            archiveUnitsDto.getArchives().getResults().get(4).getId(),
            response.getArchives().getResults().get(4).getId()
        );
        assertEquals(
            archiveUnitsDto.getArchives().getResults().get(4).getDescription(),
            response.getArchives().getResults().get(4).getDescription()
        );
        assertEquals(
            archiveUnitsDto.getArchives().getResults().get(4).getOriginatingAgencyName(),
            response.getArchives().getResults().get(4).getOriginatingAgencyName()
        );
    }

    @Test
    public void shouldExportCSVWithSuccess() throws IOException {
        // GIVEN
        final String csvContent = "I am a csv file :D ";
        Resource resource = new ByteArrayResource(csvContent.getBytes());
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        final SearchCriteriaDto searchQuery = new SearchCriteriaDto();
        when(
            restTemplate.exchange(
                BASE_URL + COLLECT_TRANSACTION_PATH + "/" + PROJECT_ID + ARCHIVE_UNITS + EXPORT_CSV_SEARCH_PATH,
                HttpMethod.POST,
                new HttpEntity<>(searchQuery, params.getValue()),
                Resource.class
            )
        ).thenReturn(new ResponseEntity<>(resource, HttpStatus.OK));
        // WHEN
        Resource response = collectTransactionInternalRestClient.exportCsvArchiveUnitsByCriteria(
            PROJECT_ID,
            searchQuery,
            params.getKey()
        );
        // THEN
        assertNotNull(response);
        assertEquals(csvContent, IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8));
    }

    private static Pair<InternalHttpContext, MultiValueMap<String, String>> generateHeadersAndContext() {
        InternalHttpContext context = new InternalHttpContext(9, "", "", "", "", "", "", "");
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(X_TENANT_ID_HEADER, Collections.singletonList("9"));
        headers.put(X_USER_TOKEN_HEADER, Collections.singletonList(""));
        headers.put(X_APPLICATION_ID_HEADER, Collections.singletonList(""));
        headers.put(X_IDENTITY_HEADER, Collections.singletonList(""));
        headers.put(X_REQUEST_ID_HEADER, Collections.singletonList(""));
        headers.put(X_ACCESS_CONTRACT_ID_HEADER, Collections.singletonList(""));
        headers.put(X_USER_LEVEL_HEADER, Collections.singletonList(""));
        headers.put(X_CUSTOMER_ID_HEADER, Collections.singletonList(""));
        return Pair.of(context, headers);
    }

    @Test
    public void shouldGetObjecTGroupByIdRunWithSuccess() {
        // GIVEN
        ResultsDto resultsDto = factory.manufacturePojo(ResultsDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_TRANSACTION_PATH + CommonConstants.OBJECTS_PATH + "/" + OBJECT_ID),
                HttpMethod.GET,
                new HttpEntity<>(params.getValue()),
                ResultsDto.class
            )
        ).thenReturn(new ResponseEntity<>(resultsDto, HttpStatus.OK));

        // WHEN
        ResponseEntity<ResultsDto> response = collectTransactionInternalRestClient.findObjectGroupById(
            OBJECT_ID,
            params.getKey()
        );
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(ResponseEntity.class);
        assertThat(response.getBody()).isInstanceOf(ResultsDto.class);
        assertEquals(resultsDto.getId(), Objects.requireNonNull(response.getBody()).getId());
        assertEquals(resultsDto.getDescription(), response.getBody().getDescription());
        assertEquals(resultsDto.getNbobjects(), response.getBody().getNbobjects());
    }
}
