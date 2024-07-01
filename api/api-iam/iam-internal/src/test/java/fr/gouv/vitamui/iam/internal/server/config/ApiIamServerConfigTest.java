package fr.gouv.vitamui.iam.internal.server.config;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.v2.AccessExternalClientV2;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.logbook.service.EventService;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
public class ApiIamServerConfigTest {

    @MockBean
    private AdminExternalClient adminExternalClient;

    @MockBean(name = "accessExternalClient")
    private AccessExternalClient accessExternalClient;

    @MockBean(name = "ingestExternalClient")
    private IngestExternalClient ingestExternalClient;

    @MockBean(name = "accessExternalClientV2")
    private AccessExternalClientV2 accessExternalClientV2;

    @Autowired
    private EventService logbookService;

    @MockBean
    private MongoTransactionManager mongoTransactionManager;

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private PasswordConfiguration passwordConfiguration;

    @Test
    public void testContext() {
        assertThat(logbookService).isNotNull();
    }

    @Test
    public void testPasswordConfiguration() {
        assertThat(passwordConfiguration).isNotNull();
        assertThat(passwordConfiguration.getMaxOldPassword()).isEqualTo(Integer.valueOf(12));
    }
}
