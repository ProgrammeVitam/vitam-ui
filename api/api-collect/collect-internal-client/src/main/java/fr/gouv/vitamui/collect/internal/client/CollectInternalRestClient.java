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
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

import static fr.gouv.vitamui.collect.common.rest.RestApi.OBJECT_GROUPS;
import static fr.gouv.vitamui.commons.api.CommonConstants.LAST_TRANSACTION_PATH;
import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ID;

public class CollectInternalRestClient
    extends BasePaginatingAndSortingRestClient<CollectProjectDto, InternalHttpContext> {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(CollectInternalRestClient.class);
    private static final String TRANSACTIONS = "/transactions";

    public CollectInternalRestClient(RestTemplate restTemplate, String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    protected Class<CollectProjectDto> getDtoClass() {
        return CollectProjectDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<CollectProjectDto>> getDtoListClass() {
        return new ParameterizedTypeReference<>() {
        };
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<CollectProjectDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<>() {
        };
    }

    @Override
    public String getPathUrl() {

        return RestApi.COLLECT_PROJECT_PATH;
    }

    private String getTransactionUrl() {
        if (baseUrl != null) {
            return baseUrl + RestApi.COLLECT_TRANSACTION_PATH;
        } else {
            return RestApi.COLLECT_TRANSACTION_PATH;
        }
    }

    @Override
    public CollectProjectDto create(final InternalHttpContext context, final CollectProjectDto dto) {
        return create(getUrl(), context, dto);
    }

    @Override
    public PaginatedValuesDto<CollectProjectDto> getAllPaginated(final InternalHttpContext context, final Integer page,
        final Integer size, final Optional<String> criteria, final Optional<String> orderBy,
        final Optional<DirectionDto> direction) {
        final URIBuilder builder = getUriBuilder(getUrl());
        return getAllPaginated(builder, context, page, size, criteria, orderBy, direction, Optional.empty());
    }


    public ResponseEntity<ResultsDto> findObjectById(String id, final InternalHttpContext context) {
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + OBJECT_GROUPS + PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, ResultsDto.class);
    }

    public void deleteProject(final InternalHttpContext context, String id) {
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        restTemplate.exchange(uriBuilder.build(id), HttpMethod.DELETE, request, Void.class);
    }

    public PaginatedValuesDto<CollectTransactionDto> getTransactionsByProjectPaginated(
        final InternalHttpContext context,
        final Integer page,
        final Integer size, final Optional<String> criteria, final Optional<String> orderBy,
        final Optional<DirectionDto> direction, final String projectId) {
        final URIBuilder builder = getUriBuilder(getUrl() + "/" + projectId + TRANSACTIONS);
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("search page={}, size={}, criteria={}, orderBy={}, direction={}", page, size, criteria,
            orderBy, direction);
        builder.addParameter("page", page.toString());
        builder.addParameter("size", size.toString());
        criteria.ifPresent(o -> builder.addParameter("criteria", o));
        orderBy.ifPresent(o -> builder.addParameter("orderBy", o));
        direction.ifPresent(o -> builder.addParameter("direction", o.toString()));

        final HttpEntity<CollectTransactionDto> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<PaginatedValuesDto<CollectTransactionDto>> response =
            restTemplate.exchange(buildUriBuilder(builder), HttpMethod.GET, request, getTransactionDtoPaginatedClass());
        checkResponse(response);
        return response.getBody();
    }

    public CollectTransactionDto getTransactionById(InternalHttpContext context, String transactionId) {
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getTransactionUrl() + PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        ResponseEntity<CollectTransactionDto> response =
            restTemplate.exchange(uriBuilder.build(transactionId), HttpMethod.GET, request,
                CollectTransactionDto.class);
        return response.getBody();
    }

    public CollectTransactionDto createTransaction(InternalHttpContext context, CollectTransactionDto dto, String id) {
        ApiUtils.checkValidity(dto);
        final HttpEntity<?> request = new HttpEntity<>(dto, buildHeaders(context));
        final ResponseEntity<CollectTransactionDto> response =
            restTemplate.exchange(getUrl() + "/" + id + TRANSACTIONS, HttpMethod.POST,
                request, CollectTransactionDto.class);
        checkResponse(response);
        return response.getBody();
    }

    public CollectTransactionDto getLastTransactionForProjectId(InternalHttpContext context, String id) {
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + PATH_ID + LAST_TRANSACTION_PATH);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        ResponseEntity<CollectTransactionDto> response =
            restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, CollectTransactionDto.class);
        return response.getBody();
    }

    protected ParameterizedTypeReference<PaginatedValuesDto<CollectTransactionDto>> getTransactionDtoPaginatedClass() {
        return new ParameterizedTypeReference<>() {
        };
    }
}
