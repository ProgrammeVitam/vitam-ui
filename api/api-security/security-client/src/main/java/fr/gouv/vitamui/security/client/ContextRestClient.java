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
package fr.gouv.vitamui.security.client;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseCrudRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.common.rest.RestApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static fr.gouv.vitamui.security.common.rest.RestApi.ADD_TENANT_TO_CONTEXT_PATH;

/**
 * A REST client to check existence, read, create, update and delete application contexts.
 *
 *
 */
public class ContextRestClient extends BaseCrudRestClient<ContextDto, InternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ContextRestClient.class);

    public ContextRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    public ContextDto findByCertificate(final InternalHttpContext context, final String certificate) {
        LOGGER.debug("certificate(truncated): {}****", StringUtils.substring(certificate, 0, 100));
        final HttpEntity<String> request = new HttpEntity<>(certificate, buildHeaders(context));
        final ResponseEntity<ContextDto> response = restTemplate.exchange(getUrl() + RestApi.FINDBYCERTIFICATE_PATH, HttpMethod.POST, request, getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    /**
     * Add Tenant to context tenants.
     * @param context
     * @param id
     * @param tenantIdentifier
     * @return
     */
    public ContextDto addTenant(final InternalHttpContext context, final String id, final String tenantIdentifier) {
        LOGGER.debug("Add Tenant {}, to {}", tenantIdentifier, id);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(ADD_TENANT_TO_CONTEXT_PATH);
        final MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<Void> request = new HttpEntity<>(headers);
        final ResponseEntity<ContextDto> response = restTemplate.exchange(uriBuilder.build(id, tenantIdentifier), HttpMethod.PATCH,
                request, getDtoClass());
        checkResponse(response);
        return response.getBody();
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_CONTEXTS_URL;
    }

    @Override
    protected Class<ContextDto> getDtoClass() {
        return ContextDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<ContextDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<ContextDto>>() {
        };
    }
}
