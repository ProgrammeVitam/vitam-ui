package fr.gouv.vitamui.iam.internal.server.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.logbook.service.EventService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {"spring.config.name=iam-internal-application"})
@ActiveProfiles("test")
public class ApiIamServerConfigTest {
    @MockBean
    private AdminExternalClient adminExternalClient;

    @MockBean(name = "accessExternalClient")
    private AccessExternalClient accessExternalClient;

    @MockBean(name = "ingestExternalClient")
    private IngestExternalClient ingestExternalClient;

    @Autowired
    private EventService logbookService;

    @MockBean
    private MongoTransactionManager mongoTransactionManager;

    @Test
    public void testContext() {
        assertThat(logbookService).isNotNull();
    }
}
