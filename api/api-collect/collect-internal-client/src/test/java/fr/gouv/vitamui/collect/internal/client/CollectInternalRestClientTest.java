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

import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_PROJECT_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_TRANSACTION_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.OBJECT_GROUPS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.TRANSACTIONS;
import static fr.gouv.vitamui.commons.api.CommonConstants.LAST_TRANSACTION_PATH;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CollectInternalRestClientTest {

    private CollectInternalRestClient collectInternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    final PodamFactory factory = new PodamFactoryImpl();
    private final String BASE_URL = "http://localhost:7090";
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String OBJECT_ID = "OBJECT_ID";
    private static final String TRANSACTION_ID = "TRANSACTION_ID";

    @BeforeEach
    public void setUp() {
        collectInternalRestClient = new CollectInternalRestClient(restTemplate, BASE_URL);
    }

    @Test
    public void checkGetBaseUrlWithSuccess() {
        assertNotNull(collectInternalRestClient);
        assertThat(collectInternalRestClient.getPathUrl()).isEqualTo(COLLECT_PROJECT_PATH);
    }

    @Test
    public void shouldCreateProjectWithSuccess() {
        // GIVEN
        CollectProjectDto collectProjectDto = factory.manufacturePojo(CollectProjectDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();

        when(
            restTemplate.exchange(
                BASE_URL + COLLECT_PROJECT_PATH,
                HttpMethod.POST,
                new HttpEntity<>(collectProjectDto, params.getValue()),
                CollectProjectDto.class
            )
        ).thenReturn(new ResponseEntity<>(collectProjectDto, HttpStatus.OK));

        // WHEN
        CollectProjectDto response = collectInternalRestClient.create(params.getKey(), collectProjectDto);
        // THEN
        assertThat(response).isInstanceOf(CollectProjectDto.class);
        assertNotNull(response);
        assertEquals(collectProjectDto.getName(), response.getName());
        assertEquals(collectProjectDto.getUnitUp(), response.getUnitUp());
        assertEquals(collectProjectDto.getArchivalAgreement(), response.getArchivalAgreement());
        assertEquals(collectProjectDto.getLegalStatus(), response.getLegalStatus());
    }

    @Test
    public void shouldGetPaginatedProjectsWithSuccess() {
        // GIVEN
        PaginatedValuesDto<CollectProjectDto> paginatedprojects = factory.manufacturePojo(
            PaginatedValuesDto.class,
            CollectProjectDto.class
        );
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();

        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_PROJECT_PATH + "?page=1&size=1"),
                HttpMethod.GET,
                new HttpEntity<>(params.getValue()),
                collectInternalRestClient.getDtoPaginatedClass()
            )
        ).thenReturn(new ResponseEntity<>(paginatedprojects, HttpStatus.OK));

        // WHEN
        PaginatedValuesDto<CollectProjectDto> response = collectInternalRestClient.getAllPaginated(
            params.getKey(),
            1,
            1,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(PaginatedValuesDto.class);
        assertEquals(5, response.getValues().size());
        assertTrue(response.getValues().stream().findFirst().isPresent());
        assertEquals(
            response.getValues().stream().findFirst().get().getId(),
            response.getValues().stream().findFirst().get().getId()
        );
    }

    @Test
    public void shouldDeleteProjectWithSuccess() {
        // GIVEN
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_PROJECT_PATH + "/" + PROJECT_ID),
                HttpMethod.DELETE,
                new HttpEntity<>(params.getValue()),
                Void.class
            )
        ).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        // THEN
        assertDoesNotThrow(() -> collectInternalRestClient.deleteProject(params.getKey(), PROJECT_ID));
    }

    @Test
    public void shouldGetObjectByIdWithSuccess() {
        // GIVEN
        ResultsDto resultsDto = factory.manufacturePojo(ResultsDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_PROJECT_PATH + OBJECT_GROUPS + "/" + OBJECT_ID),
                HttpMethod.GET,
                new HttpEntity<>(params.getValue()),
                ResultsDto.class
            )
        ).thenReturn(new ResponseEntity<>(resultsDto, HttpStatus.OK));

        // WHEN
        ResponseEntity<ResultsDto> response = collectInternalRestClient.findObjectById(OBJECT_ID, params.getKey());
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(ResponseEntity.class);
        assertThat(response.getBody()).isInstanceOf(ResultsDto.class);
        assertEquals(resultsDto.getId(), response.getBody().getId());
        assertEquals(resultsDto.getDescription(), response.getBody().getDescription());
        assertEquals(resultsDto.getNbobjects(), response.getBody().getNbobjects());
    }

    @Test
    public void shouldGetPaginatedTransactionsWithSuccess() {
        // GIVEN
        PaginatedValuesDto<CollectTransactionDto> paginatedTransactions = factory.manufacturePojo(
            PaginatedValuesDto.class,
            CollectTransactionDto.class
        );
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();

        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_PROJECT_PATH + "/" + PROJECT_ID + TRANSACTIONS + "?page=1&size=1"),
                HttpMethod.GET,
                new HttpEntity<>(params.getValue()),
                collectInternalRestClient.getTransactionDtoPaginatedClass()
            )
        ).thenReturn(new ResponseEntity<>(paginatedTransactions, HttpStatus.OK));

        // WHEN
        PaginatedValuesDto<CollectTransactionDto> response =
            collectInternalRestClient.getTransactionsByProjectPaginated(
                params.getKey(),
                1,
                1,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                PROJECT_ID
            );
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(PaginatedValuesDto.class);
        assertEquals(5, response.getValues().size());
        assertTrue(response.getValues().stream().findFirst().isPresent());
        assertEquals(
            response.getValues().stream().findFirst().get().getId(),
            response.getValues().stream().findFirst().get().getId()
        );
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
        CollectTransactionDto response = collectInternalRestClient.getTransactionById(params.getKey(), TRANSACTION_ID);
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(CollectTransactionDto.class);
        assertEquals(collectTransactionDto.getId(), response.getId());
        assertEquals(collectTransactionDto.getProjectId(), response.getProjectId());
        assertEquals(collectTransactionDto.getName(), response.getName());
    }

    @Test
    public void shouldGetLastTransactionByProjectIdWithSuccess() {
        // GIVEN
        CollectTransactionDto collectTransactionDto = factory.manufacturePojo(CollectTransactionDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                URI.create(BASE_URL + COLLECT_PROJECT_PATH + "/" + PROJECT_ID + LAST_TRANSACTION_PATH),
                HttpMethod.GET,
                new HttpEntity<>(params.getValue()),
                CollectTransactionDto.class
            )
        ).thenReturn(new ResponseEntity<>(collectTransactionDto, HttpStatus.OK));

        // WHEN
        CollectTransactionDto response = collectInternalRestClient.getLastTransactionForProjectId(
            params.getKey(),
            PROJECT_ID
        );
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(CollectTransactionDto.class);
        assertEquals(collectTransactionDto.getId(), response.getId());
        assertEquals(collectTransactionDto.getProjectId(), response.getProjectId());
        assertEquals(collectTransactionDto.getName(), response.getName());
    }

    @Test
    public void shouldCreateTransactionWithSuccess() {
        // GIVEN
        CollectTransactionDto collectTransactionDto = factory.manufacturePojo(CollectTransactionDto.class);
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                BASE_URL + COLLECT_PROJECT_PATH + "/" + PROJECT_ID + TRANSACTIONS,
                HttpMethod.POST,
                new HttpEntity<>(collectTransactionDto, params.getValue()),
                CollectTransactionDto.class
            )
        ).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        // THEN
        assertDoesNotThrow(
            () -> collectInternalRestClient.createTransaction(params.getKey(), collectTransactionDto, PROJECT_ID)
        );
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
}
