package fr.gouv.vitamui.ingest.service;

import fr.gouv.vitamui.ingest.external.client.IngestExternalRestClient;
import fr.gouv.vitamui.ingest.external.client.IngestExternalWebClient;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Test
    public void testIngest() {

    }
}
