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
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A REST client to check existence, read, create, update and delete the users.
 *
 *
 */
public class UserInternalRestClient extends BasePaginatingAndSortingRestClient<UserDto, InternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserInternalRestClient.class);

    public UserInternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    public AuthUserDto getMe(final InternalHttpContext context) {
        LOGGER.debug("GetMe");

        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + CommonConstants.PATH_ME);

        final ResponseEntity<AuthUserDto> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET,
                request, AuthUserDto.class);
        checkResponse(response);
        return response.getBody();
    }

    @Override public UserDto patch(final InternalHttpContext context, final Map<String, Object> partialDto) {
        LOGGER.debug("Patch partialDto={}");
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.PATH_ID);
        final String id = (String) partialDto.get("id");
        final MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(partialDto, headers);
        final ResponseEntity<UserDto> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.PATCH,
                request, getDtoClass());
        checkResponse(response);
        return response.getBody();
    }


    public List<String> getLevels(final InternalHttpContext context, final Optional<String> criteria) {
        LOGGER.debug("GetLevels criteria={}", criteria);

        final URIBuilder builder = getUriBuilderFromPath(CommonConstants.PATH_LEVELS);
        criteria.ifPresent(o -> builder.addParameter("criteria", o));
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<List<String>> response = restTemplate.exchange(buildUriBuilder(builder),
                HttpMethod.GET, request, getStringListClass());
        checkResponse(response);
        return response.getBody();
    }

    public UserDto patchAnalytics(final InternalHttpContext context, final Map<String, Object> partialDto) {
        LOGGER.debug("Patch analytics partialDto={}");
        final URIBuilder uriBuilder = getUriBuilderFromPath(CommonConstants.PATH_ANALYTICS);
        final URI uri = buildUriBuilder(uriBuilder);
        final MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(partialDto, headers);
        final ResponseEntity<UserDto> response = restTemplate.exchange(uri, HttpMethod.POST,
                request, getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_USERS_URL;
    }

    @Override
    protected Class<UserDto> getDtoClass() {
        return UserDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<UserDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<UserDto>>() {
        };
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<UserDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<UserDto>>() {
        };
    }

    private ParameterizedTypeReference<List<String>> getStringListClass() {
        return new ParameterizedTypeReference<List<String>>() {
        };
    }
}
