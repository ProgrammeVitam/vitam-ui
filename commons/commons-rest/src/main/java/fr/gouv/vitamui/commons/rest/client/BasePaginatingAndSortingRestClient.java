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

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

/**
 * A REST client to check existence, read, created, update and delete an object with identifier.
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public abstract class BasePaginatingAndSortingRestClient<D extends IdDto, C extends AbstractHttpContext>
    extends BaseCrudRestClient<D, C> {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(BasePaginatingAndSortingRestClient.class);

    public BasePaginatingAndSortingRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    public PaginatedValuesDto<D> getAllPaginated(final C context, final Integer page, final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy, final Optional<DirectionDto> direction) {
        SanityChecker.sanitizeCriteria(criteria);
        return getAllPaginated(context, page, size, criteria, orderBy, direction, Optional.empty());
    }

    public PaginatedValuesDto<D> getAllPaginated(final C context, final Integer page, final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy, final Optional<DirectionDto> direction, final Optional<String> embedded) {
        LOGGER
            .debug("search page={}, size={}, criteria={}, orderBy={}, direction={}, embedded={}", page, size, criteria,
                orderBy, direction, embedded);

        final URIBuilder builder = getUriBuilderFromUrl();
        builder.addParameter("page", page.toString());
        builder.addParameter("size", size.toString());
        criteria.ifPresent(o -> builder.addParameter("criteria", o));
        orderBy.ifPresent(o -> builder.addParameter("orderBy", o));
        direction.ifPresent(o -> builder.addParameter("direction", o.toString()));
        embedded.ifPresent(o -> builder.addParameter("embedded", o));

        final HttpEntity<D> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<PaginatedValuesDto<D>> response =
            restTemplate.exchange(buildUriBuilder(builder), HttpMethod.GET, request, getDtoPaginatedClass());
        checkResponse(response);
        return response.getBody();
    }

    protected abstract ParameterizedTypeReference<PaginatedValuesDto<D>> getDtoPaginatedClass();

    protected MultiValueMap<String, String> buildSearchHeaders(final ExternalHttpContext context) {
        final MultiValueMap<String, String> headers = buildHeaders(context);
        String accessContract = null;
        if (context instanceof ExternalHttpContext) {
            final ExternalHttpContext externalCallContext = context;
            accessContract = externalCallContext.getAccessContract();
        }
        if (accessContract != null) {
            headers.put(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER, Collections.singletonList(accessContract));
        }
        return headers;
    }

    protected MultiValueMap<String, String> buildSearchHeaders(final InternalHttpContext context) {
        final MultiValueMap<String, String> headers = buildHeaders(context);
        String accessContract = null;
        if (context instanceof InternalHttpContext) {
            final InternalHttpContext externalCallContext = context;
            accessContract = externalCallContext.getAccessContract();
        }

        if (accessContract != null) {
            headers.put(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER, Collections.singletonList(accessContract));
        }
        return headers;
    }
}
