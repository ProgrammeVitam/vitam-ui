package fr.gouv.vitamui.iam.external.client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Arrays;
import java.util.Optional;

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

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.iam.common.rest.RestApi;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationExternalRestClientTest extends AbstractServerIdentityBuilder {

    private ApplicationExternalRestClient applicationExternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp(){
        applicationExternalRestClient = new ApplicationExternalRestClient(restTemplate,"http://localhost:8083");
    }

    @Test
    public void getAll_returnsApplications() throws Exception {
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        String url = "http://localhost:8083" + RestApi.V1_APPLICATIONS_URL;
        final URIBuilder builder = new URIBuilder(url);
        Mockito.when(restTemplate.exchange(Mockito.eq(builder.build()), Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(applicationExternalRestClient.getDtoListClass())))
            .thenReturn(new ResponseEntity<>(Arrays.asList(new ApplicationDto()), HttpStatus.OK));
        applicationExternalRestClient.getAll(context, Optional.empty());
    }

    @Test
    public void getDtoClass_returnsApplicationDto() {
        Class clazz = applicationExternalRestClient.getDtoClass();
        assertThat(clazz).isEqualTo(ApplicationDto.class);
    }
}
