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

package fr.gouv.archive.internal.client;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

public class ArchiveInternalRestClient
    extends BasePaginatingAndSortingRestClient<ArchiveUnitsDto, InternalHttpContext> {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveInternalRestClient.class);

    protected Class<JsonNode> getJsonNodeClass() {
        return JsonNode.class;
    }

    public ArchiveInternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    protected Class<ArchiveUnitsDto> getDtoClass() {
        return ArchiveUnitsDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<ArchiveUnitsDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<ArchiveUnitsDto>>() {
        };
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<ArchiveUnitsDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<ArchiveUnitsDto>>() {
        };
    }

    @Override
    public String getPathUrl() {
        return RestApi.ARCHIVE_SEARCH_PATH;
    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(InternalHttpContext context, SearchCriteriaDto query) {
        LOGGER.info("Calling searchArchiveUnitsByCriteria with query {} ", query);
        MultiValueMap<String, String> headers = buildSearchHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        final ResponseEntity<ArchiveUnitsDto> response =
            restTemplate.exchange(getUrl() + RestApi.SEARCH_PATH, HttpMethod.POST,
                request, ArchiveUnitsDto.class);
        checkResponse(response);
        return response.getBody();
    }


    public VitamUISearchResponseDto getFilingHoldingScheme(InternalHttpContext context) {
        LOGGER.debug("Calling get filing holding scheme");
        MultiValueMap<String, String> headers = buildSearchHeaders(context);

        final HttpEntity<Void> request = new HttpEntity<>(headers);
        final ResponseEntity<VitamUISearchResponseDto> response = restTemplate
            .exchange(getUrl() + RestApi.FILING_HOLDING_SCHEME_PATH, HttpMethod.GET, request,
                VitamUISearchResponseDto.class);
        checkResponse(response);
        return response.getBody();
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

    public ResponseEntity<Resource> downloadObjectFromUnit(String id, final InternalHttpContext context) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.DOWNLOAD_ARCHIVE_UNIT + CommonConstants.PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, Resource.class);
    }

    public ResponseEntity<ResultsDto> findUnitById(String id , final InternalHttpContext context) {
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, ResultsDto.class);
    }


}
