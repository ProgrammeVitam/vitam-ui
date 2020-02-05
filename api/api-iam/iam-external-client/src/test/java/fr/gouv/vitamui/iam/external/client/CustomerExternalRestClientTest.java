package fr.gouv.vitamui.iam.external.client;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
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
public class CustomerExternalRestClientTest extends AbstractServerIdentityBuilder {

    private CustomerExternalRestClient customerExternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp(){
        customerExternalRestClient = new CustomerExternalRestClient(restTemplate,"http://localhost:8083");
    }

    @Test
    public void getMyCustomer_returnsCustomer(){
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        String url = "http://localhost:8083/iam/v1/customers/me";
        Mockito.when(restTemplate.exchange(Mockito.eq(url), Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(CustomerDto.class))).thenReturn(new ResponseEntity<CustomerDto>(new CustomerDto(),
            HttpStatus.OK));
        customerExternalRestClient.getMyCustomer(context);
    }

    @Test(expected = InternalServerException.class)
    public void getMyCustomer_WhenResponseStatus_isNotOK(){
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        String url = "http://localhost:8083/iam/v1/customers/me";
        Mockito.when(restTemplate.exchange(Mockito.eq(url), Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(CustomerDto.class))).thenReturn(new ResponseEntity<CustomerDto>(new CustomerDto(),
            HttpStatus.ACCEPTED));
        customerExternalRestClient.getMyCustomer(context);
    }

    @Test
    public void getCustomerLogo_returnsResource() throws URISyntaxException {
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        String url = "http://localhost:8083/iam/v1/customers/123/logo";
        URIBuilder builder = new URIBuilder(url);
        Mockito.when(restTemplate.exchange(Mockito.eq(builder.build()), Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(Resource.class))).thenReturn(new ResponseEntity<Resource>(new ByteArrayResource(new byte[]{}),
            HttpStatus.OK));
        customerExternalRestClient.getCustomerLogo(context, "123");
    }
}
