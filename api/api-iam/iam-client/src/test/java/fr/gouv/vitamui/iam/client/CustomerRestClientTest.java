package fr.gouv.vitamui.iam.client;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;

@RunWith(MockitoJUnitRunner.class)
public class CustomerRestClientTest {

    private CustomerRestClient customerRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        customerRestClient = new CustomerRestClient(restTemplate, "http://localhost:8083");
    }

    @Test
    public void getMyCustomer_returnsCustomer() {
        HttpContext context = new HttpContext(9, "", false, "", "", null, null, null);
        String url = "http://localhost:8083/iam/v1/customers/me";
        Mockito.when(
            restTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(CustomerDto.class)
            )
        ).thenReturn(new ResponseEntity<CustomerDto>(new CustomerDto(), HttpStatus.OK));
        customerRestClient.getMyCustomer(context);
    }

    @Test(expected = InternalServerException.class)
    public void getMyCustomer_WhenResponseStatus_isNotOK() {
        HttpContext context = new HttpContext(9, "", false, "", "", null, null, null);
        String url = "http://localhost:8083/iam/v1/customers/me";
        Mockito.when(
            restTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(CustomerDto.class)
            )
        ).thenReturn(new ResponseEntity<CustomerDto>(new CustomerDto(), HttpStatus.ACCEPTED));
        customerRestClient.getMyCustomer(context);
    }

    @Test
    public void getCustomerLogo_returnsResource() throws URISyntaxException {
        HttpContext context = new HttpContext(9, "", false, "", "", null, null, null);
        String url = "http://localhost:8083/iam/v1/customers/123/logo";
        URIBuilder builder = new URIBuilder(url);
        Mockito.when(
            restTemplate.exchange(
                Mockito.eq(builder.build()),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(Resource.class)
            )
        ).thenReturn(new ResponseEntity<Resource>(new ByteArrayResource(new byte[] {}), HttpStatus.OK));
        customerRestClient.getCustomerLogo(context, "123");
    }
}
