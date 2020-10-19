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
package fr.gouv.vitamui.referential.internal.client;

import java.util.Collections;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;

/**
 * A REST client to search operations.
 *
 * @author Paul PEYREFITTE
 * @since 0.1.0
 */
public class UnitInternalRestClient extends BaseRestClient<InternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UnitInternalRestClient.class);

    private ObjectMapper objectMapper;

    public UnitInternalRestClient(RestTemplate restTemplate, String baseUrl) {
        super(restTemplate, baseUrl);
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String getPathUrl() {
        return RestApi.UNITS_PATH;
    }

    protected Class<JsonNode> getJsonNodeClass() {
        return JsonNode.class;
    }

    public VitamUISearchResponseDto findUnitById(InternalHttpContext context, String unitId) {
        MultiValueMap<String, String> headers = buildSearchHeaders(context);

        final HttpEntity<Void> request = new HttpEntity<>(headers);
        VitamUISearchResponseDto result;
        final ResponseEntity<JsonNode> response = restTemplate.exchange(getUrl() + "/" + unitId, HttpMethod.GET,
                request, getJsonNodeClass());
        checkResponse(response);
        try {
            result = objectMapper.treeToValue(response.getBody(), VitamUISearchResponseDto.class);
        }
        catch (JsonProcessingException e) {
            throw new InternalServerException("Error while parsing Vitam response", e);
        }
        return result;
    }

    public JsonNode findUnitByDsl(InternalHttpContext context, Optional<String> id, JsonNode dsl) {
        MultiValueMap<String, String> headers = buildSearchHeaders(context);

        final HttpEntity<JsonNode> request = new HttpEntity<>(dsl, headers);
        final ResponseEntity<JsonNode> response = restTemplate.exchange(
        	getUrl() + RestApi.DSL_PATH + (id.isPresent() ? "/" + id.get() : ""), 
        	HttpMethod.POST, request, getJsonNodeClass());
        checkResponse(response);
        return response.getBody();
    }
    
    public JsonNode findObjectMetadataById(InternalHttpContext context, String id, JsonNode dsl) {
        MultiValueMap<String, String> headers = buildSearchHeaders(context);

        final HttpEntity<JsonNode> request = new HttpEntity<>(dsl, headers);
        final ResponseEntity<JsonNode> response = restTemplate.exchange(
        	getUrl() + "/" + id + RestApi.OBJECTS_PATH, 
        	HttpMethod.POST, request, getJsonNodeClass());
        checkResponse(response);
        return response.getBody();
    }
    
    public VitamUISearchResponseDto getFilingPlan(InternalHttpContext context) {
        LOGGER.debug("Calling get filing plan");
        MultiValueMap<String, String> headers = buildSearchHeaders(context);

        final HttpEntity<Void> request = new HttpEntity<>(headers);
        final ResponseEntity<VitamUISearchResponseDto> response = restTemplate
                .exchange(getUrl() + RestApi.FILING_PLAN_PATH, HttpMethod.GET, request, VitamUISearchResponseDto.class);
        checkResponse(response);
        return response.getBody();
    }

    // TODO: Mutualize me in an abstract class ?
    private MultiValueMap<String, String> buildSearchHeaders(final InternalHttpContext context) {
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

}
