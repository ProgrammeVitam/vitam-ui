/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
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

package fr.gouv.vitamui.referential.external.client;


import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.ProcessDetailDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUIProcessDetailResponseDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class LogbookManagementOperationExternalRestClient extends BasePaginatingAndSortingRestClient<ProcessDetailDto, ExternalHttpContext> {

    public LogbookManagementOperationExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<ProcessDetailDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<ProcessDetailDto>>() { };
    }
    @Override
    public String getPathUrl() {
        return RestApi.LOGBOOK_MANAGEMENT_OPERATION_PATH;
    }

    @Override
    protected Class<ProcessDetailDto> getDtoClass() {
        return ProcessDetailDto.class;
    }

    protected ParameterizedTypeReference<List<ProcessDetailDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<ProcessDetailDto>>() {
        };
    }

    public ResponseEntity<VitamUIProcessDetailResponseDto> searchOperationsDetails(ExternalHttpContext context, ProcessQuery processQuery) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.OPERATIONS_PATH );
        final HttpEntity<AuditOptions> request = new HttpEntity(processQuery, buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.POST, request, VitamUIProcessDetailResponseDto.class);

    }

    public ResponseEntity<VitamUIProcessDetailResponseDto> cancelOperationProcessExecution(ExternalHttpContext context, String id) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.CANCEL_OPERATION_PATH+ CommonConstants.PATH_ID);
        final HttpEntity<AuditOptions> request = new HttpEntity(buildHeaders(context));
        ResponseEntity<VitamUIProcessDetailResponseDto> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.POST, request, VitamUIProcessDetailResponseDto.class);
        return response;
    }

    public ResponseEntity<VitamUIProcessDetailResponseDto> updateOperationActionProcess(ExternalHttpContext context, String id, String actionId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.UPDATE_OPERATION_PATH+ CommonConstants.PATH_ID);
        final HttpEntity<AuditOptions> request = new HttpEntity(actionId, buildHeaders(context));
        ResponseEntity<VitamUIProcessDetailResponseDto> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.POST, request, VitamUIProcessDetailResponseDto.class);
        return response;
    }

}
