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

import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fr.gouv.vitamui.collect.common.rest.RestApi.SEARCH_CRITERIA_HISTORY;
import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ME;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_ACCESS_CONTRACT_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_APPLICATION_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_CUSTOMER_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_IDENTITY_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_REQUEST_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_TENANT_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_LEVEL_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_TOKEN_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchCriteriaHistoryInternalRestClientTest {

    private SearchCriteriaHistoryInternalRestClient searchCriteriaHistoryInternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:7090";
    final PodamFactory factory = new PodamFactoryImpl();

    @BeforeEach
    public void setUp() {
        searchCriteriaHistoryInternalRestClient = new SearchCriteriaHistoryInternalRestClient(restTemplate, BASE_URL);
    }

    @Test
    public void checkGetBaseUrlWithSuccess() {
        assertNotNull(searchCriteriaHistoryInternalRestClient);
        assertThat(searchCriteriaHistoryInternalRestClient.getPathUrl()).isEqualTo(SEARCH_CRITERIA_HISTORY);
    }

    @Test
    public void shouldGetSearchCriteriaHistoryWithSuccess() {
        // GIVEN
        List<SearchCriteriaHistoryDto> results = factory.manufacturePojo(
            ArrayList.class,
            SearchCriteriaHistoryDto.class
        );
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        when(
            restTemplate.exchange(
                BASE_URL + SEARCH_CRITERIA_HISTORY + PATH_ME,
                HttpMethod.GET,
                new HttpEntity<>(params.getValue()),
                searchCriteriaHistoryInternalRestClient.getDtoListClass()
            )
        ).thenReturn(new ResponseEntity<>(results, HttpStatus.OK));
        // WHEN
        List<SearchCriteriaHistoryDto> response = searchCriteriaHistoryInternalRestClient.getSearchCriteriaHistory(
            params.getKey()
        );

        // THEN
        assertNotNull(response);
        assertEquals(5, response.size());
        assertEquals(results.get(0).getId(), response.get(0).getId());
        assertEquals(results.get(0).getName(), response.get(0).getName());
        assertEquals(
            results.get(0).getSearchCriteriaList().get(0).getCriteria(),
            response.get(0).getSearchCriteriaList().get(0).getCriteria()
        );
        assertEquals(results.get(4).getId(), response.get(4).getId());
        assertEquals(results.get(4).getName(), response.get(4).getName());
        assertEquals(
            results.get(4).getSearchCriteriaList().get(0).getCriteria(),
            response.get(4).getSearchCriteriaList().get(0).getCriteria()
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
