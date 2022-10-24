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
package fr.gouv.vitamui.collect.external.client;

import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseCrudWebClient;
import fr.gouv.vitamui.commons.rest.client.BaseWebClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.io.InputStream;
import java.time.Duration;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.DOWNLOAD_ARCHIVE_UNIT;
import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.OBJECT_GROUPS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.PROJECTS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.UPDATE_UNITS_METADATA_PATH;

public class CollectExternalWebClient extends BaseWebClient<ExternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CollectExternalWebClient.class);

    public CollectExternalWebClient(final WebClient webClient, final String baseUrl) {
        super(webClient, baseUrl);
    }

    @Override
    public String getPathUrl() {
        return COLLECT_PATH;
    }

    /**
     * Download object from unit
     *
     * @param id unit id
     * @param usage usage
     * @param version version
     * @param context internl context
     * @return a mono<Response<Resourse>
     **/
    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(String id, final String usage, Integer version,
        final ExternalHttpContext context) {
        LOGGER.debug("Start downloading Object from unit id : {} usage : {} version : {}", id, usage, version);
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(
                getUrl() + PROJECTS + OBJECT_GROUPS + DOWNLOAD_ARCHIVE_UNIT + "/" + id + "?usage=" + usage +
                    "&version=" + version);

        Flux<DataBuffer> dataBuffer = webClient
            .get()
            .uri(uriBuilder.toUriString())
            .headers(addHeaders(buildHeaders(context)))
            .retrieve()
            .bodyToFlux(DataBuffer.class);

        return Mono.just(ResponseEntity
            .ok().cacheControl(CacheControl.noCache())
            .body(convertDataBufferFileToInputStreamResponse(dataBuffer)));

    }

    /**
     * function to update archive Units Metadata From File
     *
     * @param transactionId the transaction id
     * @param inputStream thr inputstream file
     * @param context the internal vitamui context
     * @return
     */
    public Void updateArchiveUnitsMetadataFromFile(final String transactionId, InputStream inputStream,
        final ExternalHttpContext context) {

        LOGGER.debug("start updating archive units from file for transactionId {}", transactionId);

        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(
                getUrl() + RestApi.COLLECT_ARCHIVE_UNITS + "/" + transactionId + UPDATE_UNITS_METADATA_PATH);
        /**
         * @TODO
         * Update timeout and make it as parameter
         */
        return webClient.post().uri(uriBuilder.toUriString()).
            httpRequest(httpRequest -> {
                HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
                reactorRequest.responseTimeout(Duration.ofMinutes(4));
            })
            .body(BodyInserters.fromValue(inputStream))
            .headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context))).retrieve()
            .onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException)
            .bodyToMono(Void.class).block();
    }
}
