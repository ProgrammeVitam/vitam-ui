package fr.gouv.vitamui.commons.test.rest;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.rest.client.BaseCrudRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;

public class TestHealthRestClient extends BaseCrudRestClient<IdDto, ExternalHttpContext> {

    public TestHealthRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    public String getPathUrl() {
        return "";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<String> getStatus(final ExternalHttpContext context) {
        final HttpEntity<?> request = new HttpEntity(buildHeaders(context));
        final ResponseEntity<String> response = restTemplate.exchange(getUrl() + "/actuator/health", HttpMethod.GET,
                request, String.class);
        return response;
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return IdDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<IdDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<IdDto>>() {
        };
    }

}
