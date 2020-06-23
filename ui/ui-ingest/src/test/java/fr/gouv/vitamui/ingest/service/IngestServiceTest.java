package fr.gouv.vitamui.ingest.service;

import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.ingest.external.client.IngestExternalRestClient;
import fr.gouv.vitamui.ingest.external.client.IngestExternalWebClient;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link IngestService}.
 *
 *
 */
@RunWith(SpringRunner.class)
public class IngestServiceTest {

    private IngestService ingestService;

    @Mock
    private IngestExternalRestClient ingestExternalRestClient;

    @Mock
    private IngestExternalWebClient ingestExternalWebClient;

    @Mock
    private CommonService commonService;

    @Before
    public void init() {
        ingestService = new IngestService(commonService, ingestExternalRestClient, ingestExternalWebClient);
    }

    /**
     * Test method {@link fr.gouv.vitamui.ingest.service.IngestService#ingest(ExternalHttpContext)}.
     */
    @Test
    public void testIngest() {

        ResponseEntity<String> responseEntity = new ResponseEntity<>(
            "ingest response body",
            null,
            HttpStatus.OK
        );

        when(ingestExternalRestClient.ingest(ArgumentMatchers.any()))
                .thenReturn(responseEntity);
        String result = ingestService.ingest(ArgumentMatchers.any());
        assertNotNull(result);
    }
}
