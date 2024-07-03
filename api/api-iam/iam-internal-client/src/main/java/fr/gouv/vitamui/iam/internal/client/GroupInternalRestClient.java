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
package fr.gouv.vitamui.iam.internal.client;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * A REST client to check existence, read, create, update and delete the profile groups.
 *
 *
 */
public class GroupInternalRestClient extends BasePaginatingAndSortingRestClient<GroupDto, InternalHttpContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupInternalRestClient.class);

    public GroupInternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    private ParameterizedTypeReference<List<String>> getStringListClass() {
        return new ParameterizedTypeReference<List<String>>() {};
    }

    @Override
    public List<GroupDto> getAll(final InternalHttpContext context, final Optional<String> embedded) {
        LOGGER.debug("Get ALL embedded = {}", embedded);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        if (embedded.isPresent()) {
            uriBuilder.queryParam("embedded", embedded.get());
        }
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<List<GroupDto>> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.GET,
            request,
            getDtoListClass()
        );
        checkResponse(response);
        return response.getBody();
    }

    public List<String> getLevels(final InternalHttpContext context, final Optional<String> criteria) {
        LOGGER.debug("GetLevels criteria={}", criteria);

        final URIBuilder builder = getUriBuilderFromPath(CommonConstants.PATH_LEVELS);
        criteria.ifPresent(o -> builder.addParameter("criteria", o));
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<List<String>> response = restTemplate.exchange(
            buildUriBuilder(builder),
            HttpMethod.GET,
            request,
            getStringListClass()
        );
        checkResponse(response);
        return response.getBody();
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_GROUPS_URL;
    }

    @Override
    protected Class<GroupDto> getDtoClass() {
        return GroupDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<GroupDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<GroupDto>>() {};
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<GroupDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<GroupDto>>() {};
    }

    public ResponseEntity<Resource> exportProfileGroups(
        final InternalHttpContext context,
        final Optional<String> optionalCriteria
    ) {
        LOGGER.debug("Export profile groups");
        final URIBuilder uriBuilder = getUriBuilderFromPath(CommonConstants.PATH_EXPORT);
        optionalCriteria.ifPresent(criteria -> uriBuilder.addParameter("criteria", criteria));
        final URI uri = buildUriBuilder(uriBuilder);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<Resource> response = restTemplate.exchange(uri, HttpMethod.GET, request, Resource.class);
        checkResponse(response);
        return response;
    }
}
