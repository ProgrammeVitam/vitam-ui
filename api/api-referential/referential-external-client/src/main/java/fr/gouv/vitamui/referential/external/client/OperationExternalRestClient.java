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
package fr.gouv.vitamui.referential.external.client;

import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitam.common.model.ProbativeValueRequest;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.referential.common.dto.ReportType;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class OperationExternalRestClient extends BasePaginatingAndSortingRestClient<LogbookOperationDto, ExternalHttpContext> {

    public OperationExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<LogbookOperationDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<LogbookOperationDto>>() {
        };
    }

    @Override
    public String getPathUrl() {
        return RestApi.OPERATIONS_URL;
    }

    @Override
    protected Class<LogbookOperationDto> getDtoClass() {
        return LogbookOperationDto.class;
    }

    protected ParameterizedTypeReference<List<LogbookOperationDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<LogbookOperationDto>>() {
        };
    }

    public boolean runAudit(ExternalHttpContext context, AuditOptions auditOptions) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        final HttpEntity<AuditOptions> request = new HttpEntity<>(auditOptions, buildHeaders(context));
        final ResponseEntity<Boolean> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST,
            request, Boolean.class);
        return response.getStatusCode() == HttpStatus.OK;
    }

    public JsonNode checkTraceabilityOperation(ExternalHttpContext context, String id) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + "/check" + CommonConstants.PATH_ID);
        final HttpEntity<AuditOptions> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<JsonNode> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, JsonNode.class);
        checkResponse(response);
        return response.getBody();
    }

    public ResponseEntity<Resource> export(ExternalHttpContext context, String id, ReportType type) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + CommonConstants.PATH_ID + "/download/{type}");
        final HttpEntity<AuditOptions> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id, type), HttpMethod.GET, request, Resource.class);
    }

    public boolean runProbativeValue(ExternalHttpContext context, ProbativeValueRequest probativeValueOptions) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + "/probativeValue");
        final HttpEntity<ProbativeValueRequest> request = new HttpEntity<>(probativeValueOptions, buildHeaders(context));
        final ResponseEntity<Boolean> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST,
                request, Boolean.class);
        return response.getStatusCode() == HttpStatus.OK;
    }

    public ResponseEntity<Resource> exportProbativeValue(ExternalHttpContext context, String operationId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + "/probativeValue" + CommonConstants.PATH_ID);
        final HttpEntity<ProbativeValueRequest> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(operationId), HttpMethod.GET, request, Resource.class);
    }
}
