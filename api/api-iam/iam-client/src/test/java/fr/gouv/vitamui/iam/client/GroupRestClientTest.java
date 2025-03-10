package fr.gouv.vitamui.iam.client;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class GroupRestClientTest {

    private GroupRestClient groupRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        groupRestClient = new GroupRestClient(restTemplate, "http://localhost:8083");
    }

    @Test
    public void getAll_returnsGroups() {
        HttpContext context = new HttpContext(9, "", "", "");
        String url = "http://localhost:8083/iam/v1/groups";
        Mockito.when(
            restTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(groupRestClient.getDtoListClass())
            )
        ).thenReturn(new ResponseEntity<List<GroupDto>>(Arrays.asList(new GroupDto()), HttpStatus.OK));
        groupRestClient.getAll(context, Optional.empty());
    }
}
