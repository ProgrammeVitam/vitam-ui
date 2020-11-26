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
package fr.gouv.vitamui.commons.rest.client.logbook;

import java.util.Collections;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.rest.client.AbstractHttpContext;
import fr.gouv.vitamui.commons.rest.client.BaseRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;

/**
 * A REST client to get logbooks.
 *
 *
 */
public class LogbookInternalRestClient<C extends AbstractHttpContext> extends BaseRestClient<C> {

    public LogbookInternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    public String getPathUrl() {
        return CommonConstants.API_VERSION_1;
    }

    /**
     * Find unit life cycle by id.
     *
     * @param context
     * @param unitId
     * @return
     */
    public JsonNode findUnitLifeCyclesByUnitId(final C context, final String id) {
        final MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<Void> request = new HttpEntity<>(headers);

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.LOGBOOK_UNIT_LYFECYCLES_PATH);
        final ResponseEntity<JsonNode> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET,
                request, JsonNode.class);
        checkResponse(response);
        return response.getBody();
    }

    /**
     * Find object life cycle by id.
     *
     * @param context
     * @param objectId
     * @return
     */
    public JsonNode findObjectLifeCyclesByUnitId(final C context, final String id) {
        final MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<Void> request = new HttpEntity<>(headers);

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.LOGBOOK_OBJECT_LYFECYCLES_PATH);

        final ResponseEntity<JsonNode> response = restTemplate.exchange(uriBuilder.build(id),
                HttpMethod.GET, request, JsonNode.class);
        checkResponse(response);
        return response.getBody();
    }

    /**
     * Find operation by id.
     *
     * @param context
     * @param operationId
     * @return
     */
    public JsonNode findOperationById(final C context, final String id) {
        final MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<Void> request = new HttpEntity<>(headers);

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.LOGBOOK_OPERATION_BY_ID_PATH);

        final ResponseEntity<JsonNode> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET,
                request, JsonNode.class);
        checkResponse(response);
        return response.getBody();
    }

    /**
     * Find Operations using a JsonNode query.
     * @param context
     * @param select
     * @return
     */
    public JsonNode findOperations(final C context, final JsonNode select) {
        final MultiValueMap<String, String> headers = buildRequestHeaders(context);
        final HttpEntity<JsonNode> request = new HttpEntity<>(select, headers);

        final ResponseEntity<JsonNode> response =
                restTemplate.exchange(getUrl() + CommonConstants.LOGBOOK_OPERATIONS_PATH + "/", HttpMethod.POST, request, JsonNode.class);
        checkResponse(response);
        return response.getBody();
    }

    /**
     * Download an operation manifest
     *
     * @param context
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadManifest(final C context, final String id) {
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.LOGBOOK_DOWNLOAD_MANIFEST_PATH);

        final ResponseEntity<Resource> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, Resource.class);
        checkResponse(response, 200);
        return response;
    }

    /**
     * Download an operation ATR
     *
     * @param context
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadAtr(final C context, final String id) {
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.LOGBOOK_DOWNLOAD_ATR_PATH);

        final ResponseEntity<Resource> response = restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, Resource.class);
        checkResponse(response, 200);
        return response;
    }

    /**
     * Download an operation report
     *
     * @param context
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadReport(final C context, final String id, final String downloadType) {
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
        uriBuilder.path(CommonConstants.LOGBOOK_DOWNLOAD_REPORT_PATH);

        final ResponseEntity<Resource> response = restTemplate.exchange(uriBuilder.build(id, downloadType), HttpMethod.GET, request, Resource.class);
        checkResponse(response, 200);
        return response;
    }

    private MultiValueMap<String, String> buildRequestHeaders(final AbstractHttpContext context) {
        final MultiValueMap<String, String> headers = buildHeaders(context);
        String accessContract = null;
        if (context instanceof ExternalHttpContext) {
            final ExternalHttpContext externalCallContext = (ExternalHttpContext) context;
            accessContract = externalCallContext.getAccessContract();
        }

        if (accessContract != null) {
            headers.put(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER, Collections.singletonList(accessContract));
        }
        return headers;
    }

}
