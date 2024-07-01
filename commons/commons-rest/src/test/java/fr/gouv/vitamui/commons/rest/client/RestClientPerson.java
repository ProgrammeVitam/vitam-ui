package fr.gouv.vitamui.commons.rest.client;

import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.dto.PersonDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class RestClientPerson extends BasePaginatingAndSortingRestClient<PersonDto, ExternalHttpContext> {

    public RestClientPerson(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<PersonDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<PersonDto>>() {};
    }

    @Override
    protected Class<PersonDto> getDtoClass() {
        return PersonDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<PersonDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<PersonDto>>() {};
    }

    @Override
    public String getPathUrl() {
        return "/persons";
    }
}
