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
package fr.gouv.vitamui.iam.external.client;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseCrudRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * A REST client to check existence, read, create, update and delete the subrogations.
 *
 *
 */
public class SubrogationExternalRestClient extends BaseCrudRestClient<SubrogationDto, ExternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SubrogationExternalRestClient.class);

    public SubrogationExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    public SubrogationDto accept(final ExternalHttpContext context, final String id) {
        LOGGER.debug("accept subrogation id={}", id);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path("/surrogate/accept");
        uriBuilder.path(CommonConstants.PATH_ID);
        final HttpEntity request = new HttpEntity(buildHeaders(context));
        final ResponseEntity<SubrogationDto> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.PATCH, request, getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    public void decline(final ExternalHttpContext context, final String id) {
        LOGGER.debug("decline subrogation id={}", id);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path("/surrogate/decline");
        uriBuilder.path(CommonConstants.PATH_ID);
        final HttpEntity request = new HttpEntity(buildHeaders(context));
        restTemplate.exchange(uriBuilder.build(id), HttpMethod.DELETE, request, Void.class);
    }

    public SubrogationDto getMySubrogationAsSurrogate(final ExternalHttpContext context) {
        final HttpEntity request = new HttpEntity(buildHeaders(context));
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path("/me/surrogate");
        final ResponseEntity<SubrogationDto> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, request, getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    public SubrogationDto getMySubrogationAsSuperuser(final ExternalHttpContext context) {
        final HttpEntity request = new HttpEntity(buildHeaders(context));
        final URIBuilder uriBuilder = getUriBuilderFromPath("/me/superuser");
        final ResponseEntity<SubrogationDto> response = restTemplate.exchange(buildUriBuilder(uriBuilder), HttpMethod.GET, request, getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    public PaginatedValuesDto<UserDto> getGenericUsers(final ExternalHttpContext context, final Integer page, final Integer size,
            final Optional<String> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction) {
        LOGGER.debug("search page={}, size={}, criteria={}, orderBy={}, direction={}", page, size, criteria, orderBy, direction);

        final URIBuilder builder = getUriBuilderFromPath("/users/generic");
        builder.addParameter("page", page.toString());
        builder.addParameter("size", size.toString());
        criteria.ifPresent(o -> builder.addParameter("criteria", o));
        orderBy.ifPresent(o -> builder.addParameter("orderBy", o));
        direction.ifPresent(o -> builder.addParameter("direction", o.toString()));

        final HttpEntity<UserDto> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<PaginatedValuesDto<UserDto>> response = restTemplate.exchange(buildUriBuilder(builder), HttpMethod.GET, request,
                getUserDtoPaginatedClass());
        checkResponse(response);
        return response.getBody();
    }

    public GroupDto getGroupById(final ExternalHttpContext context, final String id) {
        LOGGER.debug("Get {}", id);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final URIBuilder builder = getUriBuilderFromPath("/groups/" + id + "/");

        final ResponseEntity<GroupDto> response = restTemplate.exchange(buildUriBuilder(builder), HttpMethod.GET, request, GroupDto.class);
        checkResponse(response);
        return response.getBody();
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_SUBROGATIONS_URL;
    }

    @Override
    protected Class<SubrogationDto> getDtoClass() {
        return SubrogationDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<SubrogationDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<SubrogationDto>>() {
        };
    }

    protected ParameterizedTypeReference<PaginatedValuesDto<UserDto>> getUserDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<UserDto>>() {
        };
    }
}
