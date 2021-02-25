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

import java.util.List;

import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;

/**
 * A REST client to check existence, read, create, update and delete customers.
 *
 *
 */
public class CustomerInternalRestClient extends BasePaginatingAndSortingRestClient<CustomerDto, InternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerInternalRestClient.class);

    public CustomerInternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    public CustomerDto getMyCustomer(final InternalHttpContext context) {
        LOGGER.debug("GetMyCustomer");
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + CommonConstants.PATH_ME);

        final ResponseEntity<CustomerDto> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, request, CustomerDto.class);
        checkResponse(response);
        return response.getBody();
    }

    public CustomerDto getByCode(final InternalHttpContext context, final String code) {
        LOGGER.debug("GetByCode {}", code);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.queryParam("code", code);
        final ResponseEntity<CustomerDto> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, request, getDtoClass(), code);
        checkResponse(response);
        return response.getBody();
    }

    @Override
    @Deprecated
    public CustomerDto create(final InternalHttpContext context, final CustomerDto dto) {
        return super.create(context, dto);
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_CUSTOMERS_URL;
    }

    @Override
    protected Class<CustomerDto> getDtoClass() {
        return CustomerDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<CustomerDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<CustomerDto>>() {
        };
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<CustomerDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<CustomerDto>>() {
        };
    }

    public ResponseEntity<Resource> getLogo(final InternalHttpContext context, final String id, final AttachmentType type) {
        LOGGER.debug("Get logo for customer with id {}, type: {}", id, type);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final URIBuilder builder = getUriBuilderFromPath("/" + id + "/logo?type=" + type);
        final ResponseEntity<Resource> response = restTemplate.exchange(buildUriBuilder(builder), HttpMethod.GET, request, Resource.class);
        checkResponse(response, 200, 204);
        return response;

    }

    public boolean getGdprSettingStatus(final InternalHttpContext context) {
        LOGGER.debug("get Gdpr Setting Status");
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + CommonConstants.GDPR_STATUS);

        final ResponseEntity<Boolean> response =
            restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, request, Boolean.class);
        checkResponse(response);
        return response.getBody();
    }

}
