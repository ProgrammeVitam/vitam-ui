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

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;

public class ContextExternalRestClient extends BasePaginatingAndSortingRestClient<ContextDto, ExternalHttpContext> {

    public ContextExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override protected ParameterizedTypeReference<PaginatedValuesDto<ContextDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<ContextDto>>() { };
    }

    @Override
    public String getPathUrl() {
        return RestApi.CONTEXTS_URL;
    }

    @Override protected Class<ContextDto> getDtoClass() {
        return ContextDto.class;
    }

    protected ParameterizedTypeReference<List<ContextDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<ContextDto>>() {
        };
    }

    public boolean check(ExternalHttpContext context, ContextDto contextDto) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + CommonConstants.PATH_CHECK);
        final HttpEntity<ContextDto> request = new HttpEntity<>(contextDto, buildHeaders(context));
        final ResponseEntity<Boolean> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST,
                request, Boolean.class);
        return response.getStatusCode() == HttpStatus.OK;
    }

}
