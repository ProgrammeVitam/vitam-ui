package fr.gouv.vitamui.iam.client;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.apache.http.client.utils.URIBuilder;
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
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationRestClientTest {

    private ApplicationRestClient applicationRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        applicationRestClient = new ApplicationRestClient(restTemplate, "http://localhost:8083");
    }

    @Test
    public void getAll_returnsApplications() throws Exception {
        HttpContext context = new HttpContext(9, "", false, "", "", null, null, null);
        String url = "http://localhost:8083" + RestApi.V1_APPLICATIONS_URL;
        final URIBuilder builder = new URIBuilder(url);
        Mockito.when(
            restTemplate.exchange(
                Mockito.eq(builder.build()),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(applicationRestClient.getDtoListClass())
            )
        ).thenReturn(new ResponseEntity<>(Arrays.asList(new ApplicationDto()), HttpStatus.OK));
        applicationRestClient.getAll(context, Optional.empty());
    }

    @Test
    public void getDtoClass_returnsApplicationDto() {
        Class clazz = applicationRestClient.getDtoClass();
        assertThat(clazz).isEqualTo(ApplicationDto.class);
    }
}
