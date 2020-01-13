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
package fr.gouv.vitamui.commons.rest.client;

import java.util.List;
import java.util.Map;

import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

/**
 *
 * Abstract class with crud operations.
 *
 *
 * @param <C>
 * @param <D>
 */
public abstract class BaseCrudWebClient<C extends AbstractHttpContext, D extends IdDto> extends BaseWebClient<C> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(BaseCrudWebClient.class);

    private static final String CRITERIA_QUERY_PARAM = "criteria";

    public BaseCrudWebClient(final WebClient webClient, final String baseUrl) {
        super(webClient, baseUrl);
    }

    /**
     * Create
     * @param context
     * @param dto
     * @return
     */
    public D create(final C context, final D dto) {
        return webClient.post().uri(getPathUrl()).headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context))).syncBody(dto).retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException).bodyToMono(getDtoClass()).block();
    }

    /**
     * Retrieve one
     * @param context
     * @param id
     * @return
     */
    public D getOne(final C context, final String id) throws NotFoundException {
        return webClient.get().uri(getPathUrl() + "/" + id).headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context))).retrieve()
            .onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException).bodyToMono(getDtoClass()).block();
    }

    /**
     * Retrieve one
     * @param context
     * @param id
     * @param criteria
     * @return
     */
    public D getOne(final C context, final String id, Optional<String> criteria) throws NotFoundException {
        return webClient.get().uri(getPathUrl() + "/" + id).headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context)))
                .attribute(CRITERIA_QUERY_PARAM, criteria).retrieve().onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException)
                .bodyToMono(getDtoClass()).block();
    }

    /**
     * Retrieve all
     * @param context
     * @return
     */
    public List<D> getAll(final C context) {
        return webClient.get().uri(getPathUrl()).headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context))).retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException).bodyToMono(getDtoListClass()).block();
    }

    /**
     * Retrieve all By Criteria
     * @param context
     * @param criteria
     * @return
     */
    public List<D> getAll(final C context, final Optional<String> criteria) {
        final URIBuilder builder = getUriBuilderFromUrl();
        criteria.ifPresent(c -> builder.addParameter(CRITERIA_QUERY_PARAM, c));

        return webClient.get().uri(buildUriBuilder(builder)).headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context))).retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException).bodyToMono(getDtoListClass()).block();
    }

    /**
     *
     * @param context
     * @param id
     * @param partialDto
     * @return
     */
    public D patch(final C context, final String id, final Map<String, Object> partialDto) {
        return webClient.patch().uri(getPathUrl() + "/" + id).headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context))).syncBody(partialDto)
                .retrieve().onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException).bodyToMono(getDtoClass()).block();
    }

    /**
     * Delete
     * @param context
     * @param id
     * @return
     */
    public void delete(final C context, final String id) {
        webClient.delete().uri(getPathUrl() + "/" + id).headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context))).retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException).bodyToMono(Void.class).block();
    }

    public boolean checkExist(final C context, final String criteria) {
        final URIBuilder builder = getUriBuilderFromPath(CommonConstants.PATH_CHECK);
        builder.addParameter(CRITERIA_QUERY_PARAM, criteria);

        final HttpStatus httpStatus = webClient.head().uri(buildUriBuilder(builder)).headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context)))
                .exchange().block().statusCode();
        return HttpStatus.OK.equals(httpStatus);
    }

    protected abstract ParameterizedTypeReference<List<D>> getDtoListClass();

    protected abstract Class<D> getDtoClass();

    protected abstract VitamUILogger getLogger();

}
