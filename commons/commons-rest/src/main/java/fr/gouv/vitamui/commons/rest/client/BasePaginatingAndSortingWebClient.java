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

import fr.gouv.vitamui.commons.api.domain.*;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.utils.ParameterizedTypeReferenceFactory;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

/**
 * A REST client to check existence, read, created, update and delete an object with identifier, with paginated results.
 * There is only a BaseCrudWebClient in vitam-ui commons-rest, and no paginated base for web client.
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public abstract class BasePaginatingAndSortingWebClient<C extends AbstractHttpContext, D extends IdDto>
    extends BaseCrudWebClient<C, D> {

    private static final String EMBEDDED_QUERY_PARAM = "embedded";

    private static final String CRITERIA_QUERY_PARAM = "criteria";

    @Autowired
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        BasePaginatingAndSortingWebClient.class
    );

    public BasePaginatingAndSortingWebClient(@Autowired final WebClient webClient, final String baseUrl) {
        super(webClient, baseUrl);
    }

    public PaginatedValuesDto<D> getAllPaginated(
        final C context,
        final Integer page,
        final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction
    ) {
        return getAllPaginated(context, page, size, criteria, orderBy, direction, Optional.empty());
    }

    public PaginatedValuesDto<D> getAllPaginated(
        final C context,
        final Integer page,
        final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        final Optional<String> embedded
    ) {
        LOGGER.debug(
            "search page={}, size={}, criteria={}, orderBy={}, direction={}, embedded={}",
            page,
            size,
            criteria,
            orderBy,
            direction,
            embedded
        );

        final URIBuilder builder = getUriBuilderFromUrl();
        builder.addParameter("page", page.toString());
        builder.addParameter("size", size.toString());
        criteria.ifPresent(o -> builder.addParameter(CRITERIA_QUERY_PARAM, o));
        orderBy.ifPresent(o -> builder.addParameter("orderBy", o));
        direction.ifPresent(o -> builder.addParameter("direction", o.toString()));
        embedded.ifPresent(o -> builder.addParameter(EMBEDDED_QUERY_PARAM, o));

        return webClient
            .get()
            .uri(buildUriBuilder(builder))
            .headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context)))
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException)
            .bodyToMono(getDtoPaginatedClass())
            .block();
    }

    public ResultsDto<D> getAllRequest(final C context, final RequestParamDto requestParam) {
        LOGGER.debug("search {}", requestParam);

        final URIBuilder builder = getUriBuilderFromUrl();
        builder.addParameter("page", requestParam.getPage().toString());
        builder.addParameter("size", requestParam.getSize().toString());
        if (requestParam.getCriteria() != null) {
            builder.addParameter(CRITERIA_QUERY_PARAM, requestParam.getCriteria());
        }
        if (requestParam.getOrderBy() != null) {
            builder.addParameter("orderBy", requestParam.getOrderBy());
        }
        if (requestParam.getDirection() != null) {
            builder.addParameter("direction", requestParam.getDirection().toString());
        }
        if (requestParam.getEmbedded() != null) {
            builder.addParameter(EMBEDDED_QUERY_PARAM, requestParam.getEmbedded());
        }

        if (requestParam.getExcludeFields() != null) {
            for (var excludeField : requestParam.getExcludeFields()) {
                builder.addParameter("excludeFields", excludeField);
            }
        }

        if (requestParam.getGroups() != null && requestParam.getGroups().getFields() != null) {
            for (var field : requestParam.getGroups().getFields()) {
                builder.addParameter("fields", field);
            }
            builder.addParameter("operator", requestParam.getGroups().getOperator().name());

            if (requestParam.getGroups().getFieldOperator() != null) {
                builder.addParameter("fieldOperator", requestParam.getGroups().getFieldOperator());
            }
        }

        return webClient
            .get()
            .uri(buildUriBuilder(builder))
            .headers(headersConsumer -> headersConsumer.addAll(buildHeaders(context)))
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(), BaseCrudWebClient::createResponseException)
            .bodyToMono(getResultsDtoClass())
            .block();
    }

    protected abstract ParameterizedTypeReference<PaginatedValuesDto<D>> getDtoPaginatedClass();

    protected ParameterizedTypeReference<ResultsDto<D>> getResultsDtoClass() {
        return ParameterizedTypeReferenceFactory.createFromInstance(ResultsDto.class, this);
    }
}
