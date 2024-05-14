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
package fr.gouv.vitamui.archives.search.external.client;

import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * A REST client to check existence, read and create the search criteria history.
 *
 *
 */
public class SearchCriteriaHistoryExternalRestClient
    extends BasePaginatingAndSortingRestClient<SearchCriteriaHistoryDto, ExternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        SearchCriteriaHistoryExternalRestClient.class
    );

    public SearchCriteriaHistoryExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    /**
     * Retrieve the search criterias associated to the authenticated user.
     * @param context
     * @return the search criterias
     */
    public List<SearchCriteriaHistoryDto> getSearchCriteriaHistory(final ExternalHttpContext context) {
        LOGGER.debug("getSearchCriteriaHistory external client");
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());

        final ResponseEntity<List<SearchCriteriaHistoryDto>> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.GET,
            request,
            getDtoListClass()
        );
        checkResponse(response);
        return response.getBody();
    }

    @Override
    public String getPathUrl() {
        return RestApi.SEARCH_CRITERIA_HISTORY;
    }

    @Override
    protected Class<SearchCriteriaHistoryDto> getDtoClass() {
        return SearchCriteriaHistoryDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<SearchCriteriaHistoryDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<SearchCriteriaHistoryDto>>() {};
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<SearchCriteriaHistoryDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<SearchCriteriaHistoryDto>>() {};
    }
}
