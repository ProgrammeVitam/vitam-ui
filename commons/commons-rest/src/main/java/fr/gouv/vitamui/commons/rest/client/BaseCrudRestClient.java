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

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A REST client to check existence, read, created, update and delete an object with identifier.
 *
 *
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public abstract class BaseCrudRestClient<D extends IdDto, C extends AbstractHttpContext> extends BaseRestClient<C> {

    private static final String EMBEDDED_QUERY_PARAM = "embedded";

    private static final String CRITERIA_QUERY_PARAM = "criteria";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(BaseCrudRestClient.class);

    public BaseCrudRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    public List<D> getAll(final C context) {
        LOGGER.debug("Get ALL");
        return getAll(context, Optional.empty(), Optional.empty());
    }

    public List<D> getAll(final C context, final Optional<String> criteria) {
        SanityChecker.sanitizeCriteria(criteria);
        return getAll(context, criteria, Optional.empty());
    }

    public List<D> getAll(final C context, final Optional<String> criteria, final Optional<String> embedded) {
        LOGGER.debug("Get ALL");

        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));

        final URIBuilder builder = getUriBuilderFromUrl();
        criteria.ifPresent(c -> builder.addParameter(CRITERIA_QUERY_PARAM, c));
        embedded.ifPresent(e -> builder.addParameter(EMBEDDED_QUERY_PARAM, e));

        final ResponseEntity<List<D>> response = restTemplate.exchange(buildUriBuilder(builder), HttpMethod.GET,
                request, getDtoListClass());
        checkResponse(response);
        return response.getBody();
    }

    public boolean checkExist(final C context, final String criteria) {
        LOGGER.debug("Check exists criteria={}", criteria);
        SanityChecker.check(criteria);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final URIBuilder builder = getUriBuilderFromPath(CommonConstants.PATH_CHECK);
        builder.addParameter(CRITERIA_QUERY_PARAM, criteria);

        final ResponseEntity<Void> response = restTemplate.exchange(buildUriBuilder(builder), HttpMethod.HEAD, request,
                Void.class);
        checkResponse(response, 200, 204);
        return response.getStatusCodeValue() == 200;
    }

    public D getOne(final C context, final String id) {
        LOGGER.debug("Get {}", id);
        return getOne(context, id, Optional.empty());
    }

    public D getOne(final C context, final String id, final Optional<String> criteria) {
        LOGGER.debug("Get {}, criteria={}", id, criteria);

        SanityChecker.check(id);
        SanityChecker.sanitizeCriteria(criteria);

        return getOne(context, id, criteria, Optional.empty());
    }

    public D getOne(final C context, final String id, final Optional<String> criteria,
            final Optional<String> embedded) {
        LOGGER.debug("Get {}, criteria={} embedded={}", id, criteria, embedded);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final URIBuilder builder = getUriBuilderFromPath("/" + id + "/");
        criteria.ifPresent(c -> builder.addParameter(CRITERIA_QUERY_PARAM, c));
        embedded.ifPresent(e -> builder.addParameter(EMBEDDED_QUERY_PARAM, e));

        final ResponseEntity<D> response = restTemplate.exchange(buildUriBuilder(builder), HttpMethod.GET, request,
                getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    public D create(final C context, final D dto) {
        LOGGER.debug("Create {}", dto);
        final HttpEntity<D> request = new HttpEntity<>(dto, buildHeaders(context));
        final ResponseEntity<D> response = restTemplate.exchange(getUrl(), HttpMethod.POST, request, getDtoClass());
        checkResponse(response, 200, 201);
        return response.getBody();
    }

    public D update(final C context, final D dto) {
        LOGGER.debug("Update {}", dto);
        ApiUtils.checkValidity(dto);
        final String dtoId = dto.getId();
        final HttpEntity<D> request = new HttpEntity<>(dto, buildHeaders(context));
        final ResponseEntity<D> response = restTemplate.exchange(getUrl() + CommonConstants.PATH_ID, HttpMethod.PUT,
                request, getDtoClass(), dtoId);
        checkResponse(response);
        return response.getBody();
    }

    public D patch(final C context, final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {}", partialDto);
        ApiUtils.checkValidity(partialDto);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.PATH_ID);
        final String id = (String) partialDto.get("id");
        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(partialDto, buildHeaders(context));

        final ResponseEntity<D> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.PATCH, request,
                getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    public D patchWithDto(final C context, final D partialDto) {
        LOGGER.debug("Patch {}", partialDto);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.PATH_ID);
        final String id = partialDto.getId();
        final MultiValueMap<String, String> headers = buildHeaders(context);

        final HttpEntity<D> request = new HttpEntity<>(partialDto, headers);
        final ResponseEntity<D> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.PATCH, request,
                getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    public void delete(final C context, final String id) {
        LOGGER.debug("Delete {}", id);
        SanityChecker.check(id);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        restTemplate.exchange(getUrl() + CommonConstants.PATH_ID, HttpMethod.DELETE, request, getDtoClass(), id);
    }

    /**
     * Find operation by id and collection name.
     *
     * @param context context
     * @param id identifier
     * @return JsonNode
     */
    public JsonNode findHistoryById(final C context, final String id) {
        LOGGER.debug("Get logbook of id :{}", id);
        SanityChecker.check(id);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.PATH_LOGBOOK);
        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<JsonNode> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request,
                JsonNode.class);
        checkResponse(response);
        return response.getBody();
    }

    protected abstract Class<D> getDtoClass();

    protected abstract ParameterizedTypeReference<List<D>> getDtoListClass();

    /**
     * Method allowing to generate an URI builder.
     * @return The linked builder.
     */
    @Override
    protected URIBuilder getUriBuilderFromUrl() {
        return getUriBuilder(getUrl());
    }

    /**
     * Method allowing to generate an URI builder.
     * @param url Url to reach.
     * @return The linked builder.
     */
    @Override
    protected URIBuilder getUriBuilder(final String url) {
        try {
            return new URIBuilder(url);
        }
        catch (final URISyntaxException exception) {
            throw new ApplicationServerException(exception.getMessage(), exception);
        }
    }

    /**
     * Method for get UriBuilder from Url
     * @return URIBuilder
     */
    @Override
    protected URIBuilder getUriBuilderFromPath(final String path) {
        try {
            return new URIBuilder(getUrl() + path);
        }
        catch (final URISyntaxException e) {
            throw new ApplicationServerException(e.getMessage());
        }
    }

    @Override
    protected URI buildUriBuilder(final URIBuilder builder) {
        try {
            return builder.build();
        }
        catch (final URISyntaxException exception) {
            throw new ApplicationServerException(exception.getMessage(), exception);
        }
    }

}
