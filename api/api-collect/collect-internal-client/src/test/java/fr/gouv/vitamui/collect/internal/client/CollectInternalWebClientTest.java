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

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.test.extension.ServerIdentityExtension;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_PATH;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_ACCESS_CONTRACT_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_APPLICATION_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_CUSTOMER_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_IDENTITY_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_REQUEST_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_TENANT_ID_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_LEVEL_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_TOKEN_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CollectInternalWebClientTest extends ServerIdentityExtension {

    private static final String UNIT_ID = "UNIT_ID";
    private CollectInternalWebClient collectInternalWebClient;

    @Mock
    private WebClient webClient;
    private final String BASE_URL = "http://localhost:7090";

    @BeforeEach
    public void setUp() {
        webClient = WebClient.builder()
            .exchangeFunction(clientRequest ->
                Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("content-type", "application/json")
                    .body("{ \"hello\" : \"I am an object\"}")
                    .build())
            ).build();
        collectInternalWebClient = new CollectInternalWebClient(webClient, BASE_URL);
    }

    @Test
    public void checkGetBaseUrlWithSuccess() {
        assertNotNull(collectInternalWebClient);
        assertThat(collectInternalWebClient.getPathUrl()).isEqualTo(COLLECT_PATH);
    }

    @Test
    public void shouldDownloadObjectFromUnitWithSuccess() {
        // GIVEN
        Pair<InternalHttpContext, MultiValueMap<String, String>> params = generateHeadersAndContext();
        // WHEN
        Mono<ResponseEntity<Resource>> response =
            collectInternalWebClient.downloadObjectFromUnit(UNIT_ID, "BinaryMaster", 1, params.getKey());
        // THEN
        assertNotNull(response);
        assertThat(response).isInstanceOf(Mono.class);
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
