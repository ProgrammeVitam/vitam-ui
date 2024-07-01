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

package fr.gouv.vitamui.collect.external.client;

import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.ARCHIVE_UNIT_INFO;
import static fr.gouv.vitamui.archives.search.common.rest.RestApi.EXPORT_CSV_SEARCH_PATH;
import static fr.gouv.vitamui.archives.search.common.rest.RestApi.UNIT_WITH_INHERITED_RULES;
import static fr.gouv.vitamui.collect.common.rest.RestApi.ABORT_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.ARCHIVE_UNITS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.REOPEN_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.SEND_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.VALIDATE_PATH;
import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ID;

public class CollectTransactionExternalRestClient
    extends BasePaginatingAndSortingRestClient<CollectTransactionDto, ExternalHttpContext> {

    public CollectTransactionExternalRestClient(RestTemplate restTemplate, String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    protected Class<CollectTransactionDto> getDtoClass() {
        return CollectTransactionDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<CollectTransactionDto>> getDtoListClass() {
        return new ParameterizedTypeReference<>() {};
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<CollectTransactionDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<>() {};
    }

    @Override
    public String getPathUrl() {
        return RestApi.COLLECT_TRANSACTION_PATH;
    }

    public ArchiveUnitsDto searchArchiveUnitsByProjectAndSearchQuery(
        ExternalHttpContext context,
        String transactionId,
        SearchCriteriaDto searchQuery
    ) {
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(searchQuery, headers);
        final ResponseEntity<ArchiveUnitsDto> response = restTemplate.exchange(
            getUrl() + "/" + transactionId + ARCHIVE_UNITS,
            HttpMethod.POST,
            request,
            ArchiveUnitsDto.class
        );
        checkResponse(response);
        return response.getBody();
    }

    public ResponseEntity<ResultsDto> findUnitById(String id, ExternalHttpContext context) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID
        );
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, ResultsDto.class);
    }

    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(
        String transactionId,
        SearchCriteriaDto query,
        ExternalHttpContext context
    ) {
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        return restTemplate.exchange(
            getUrl() + "/" + transactionId + ARCHIVE_UNITS + EXPORT_CSV_SEARCH_PATH,
            HttpMethod.POST,
            request,
            Resource.class
        );
    }

    public void sendTransaction(ExternalHttpContext context, String transactionId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + CommonConstants.PATH_ID + SEND_PATH
        );
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        restTemplate.exchange(uriBuilder.build(transactionId), HttpMethod.PUT, request, Void.class);
    }

    public void reopenTransaction(ExternalHttpContext context, String transactionId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + CommonConstants.PATH_ID + REOPEN_PATH
        );
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        restTemplate.exchange(uriBuilder.build(transactionId), HttpMethod.PUT, request, Void.class);
    }

    public void abortTransaction(ExternalHttpContext context, String transactionId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + CommonConstants.PATH_ID + ABORT_PATH
        );
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        restTemplate.exchange(uriBuilder.build(transactionId), HttpMethod.PUT, request, Void.class);
    }

    public void validateTransaction(ExternalHttpContext context, String transactionId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + CommonConstants.PATH_ID + VALIDATE_PATH
        );
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        restTemplate.exchange(uriBuilder.build(transactionId), HttpMethod.PUT, request, Void.class);
    }

    public CollectTransactionDto getTransactionById(ExternalHttpContext context, String transactionId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + CommonConstants.PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        ResponseEntity<CollectTransactionDto> response = restTemplate.exchange(
            uriBuilder.build(transactionId),
            HttpMethod.GET,
            request,
            CollectTransactionDto.class
        );
        return response.getBody();
    }

    public CollectTransactionDto updateTransaction(
        ExternalHttpContext context,
        CollectTransactionDto collectTransactionDto
    ) {
        final HttpEntity<?> request = new HttpEntity<>(collectTransactionDto, buildHeaders(context));
        final ResponseEntity<CollectTransactionDto> response = restTemplate.exchange(
            getUrl(),
            HttpMethod.PUT,
            request,
            CollectTransactionDto.class
        );
        checkResponse(response);
        return response.getBody();
    }

    public ResponseEntity<ResultsDto> findObjectGroupById(String id, ExternalHttpContext context) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + CommonConstants.OBJECTS_PATH + PATH_ID
        );
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, ResultsDto.class);
    }

    public List<VitamUiOntologyDto> getExternalOntologiesList(ExternalHttpContext context) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + CommonConstants.EXTERNAL_ONTOLOGIES_LIST
        );
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.GET, request, ArrayList.class).getBody();
    }

    public ResponseEntity<ResultsDto> selectUnitWithInheritedRules(
        ExternalHttpContext context,
        String transactionId,
        SearchCriteriaDto query
    ) {
        MultiValueMap<String, String> headers = buildHeaders(context);

        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        final ResponseEntity<ResultsDto> response = restTemplate.exchange(
            getUrl() + "/" + transactionId + UNIT_WITH_INHERITED_RULES,
            HttpMethod.POST,
            request,
            ResultsDto.class
        );
        checkResponse(response);
        return response;
    }
}
