package fr.gouv.vitamui.ingest.external.client;

import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
    }
}
