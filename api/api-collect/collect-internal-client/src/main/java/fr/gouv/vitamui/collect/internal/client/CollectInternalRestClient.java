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

import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static fr.gouv.vitamui.collect.common.rest.RestApi.ARCHIVES_SEARCH_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.SEARCH;

public class CollectInternalRestClient
    extends BasePaginatingAndSortingRestClient<CollectProjectDto, InternalHttpContext> {

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

    public ArchiveUnitsDto getAllArchiveUnitsForCollect(InternalHttpContext context, String projectId,
        SearchCriteriaDto searchQuery) {
        MultiValueMap<String, String> headers = buildSearchHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(searchQuery, headers);
        final ResponseEntity<ArchiveUnitsDto> response =
            restTemplate.exchange(getUrl() + ARCHIVES_SEARCH_PATH + SEARCH + "/" + projectId, HttpMethod.POST,
                request, ArchiveUnitsDto.class);
        checkResponse(response);
        return response.getBody();
    }
}
