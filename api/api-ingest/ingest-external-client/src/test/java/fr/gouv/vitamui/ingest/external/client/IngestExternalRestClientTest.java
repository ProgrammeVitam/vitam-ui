package fr.gouv.vitamui.ingest.external.client;

import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.ingest.common.rest.RestApi;
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

@RunWith(MockitoJUnitRunner.Silent.class)
public class IngestExternalRestClientTest extends AbstractServerIdentityBuilder {

    private IngestExternalRestClient ingestExternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        ingestExternalRestClient = new IngestExternalRestClient(restTemplate, "http://localhost:8088");
    }

    @Test
    public void basicIngestTest() {
        ExternalHttpContext context = new ExternalHttpContext(3, "someToken", "INGEST_APP", "11");
        String url = "http://localhost:8088" + RestApi.V1_INGEST;
        ResponseEntity<?> responseEntity = new ResponseEntity<>(
            "ingest response body",
            null,
            HttpStatus.OK
        );

        Mockito.when(restTemplate
            .exchange(Mockito.eq(url), Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(ResponseEntity.class)))
            .thenReturn((ResponseEntity<ResponseEntity>) responseEntity);
        ingestExternalRestClient.ingest(context);
    }

}
