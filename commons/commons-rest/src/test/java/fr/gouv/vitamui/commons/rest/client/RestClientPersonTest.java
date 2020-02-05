package fr.gouv.vitamui.commons.rest.client;

import fr.gouv.vitamui.commons.rest.dto.PersonDto;
import fr.gouv.vitamui.commons.rest.util.AbstractServerIdentityBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RestClientPersonTest extends AbstractServerIdentityBuilder {

    private RestClientPerson client;

    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    private final ExternalHttpContext header = new ExternalHttpContext(1, "", "", "");

    @Before
    public void setup() {
        client = new RestClientPerson(restTemplate, "http://localhost:8080");
    }

    @Test
    public void testPaginated() {
        final String criteria = "{\"criteria\" : [{\"key\":\"creationDate\", \"value\" :\"2018-08-24T17:15:33.790+20:00\", \"operator\":\"EQUALS\"}]}";
        Mockito.when(restTemplate.exchange(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        client.getAllPaginated(header, 0, 20, Optional.of(criteria), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Test
    public void testCheckExist() {
        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        Mockito.when(restTemplate.exchange(uri.capture(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        client.checkExist(header, "toto");
        assertThat(uri.getValue().toString()).isEqualTo(client.getUrl() + "/check?criteria=toto");

    }

    @Test
    public void testGetOne() {
        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        String id = UUID.randomUUID().toString();
        Mockito.when(restTemplate.exchange(uri.capture(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.eq(PersonDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        client.getOne(header, id, Optional.of("toto"), Optional.empty());
        assertThat(uri.getValue().toString()).isEqualTo(client.getUrl() + "/" + id + "/?criteria=toto");
    }
}
