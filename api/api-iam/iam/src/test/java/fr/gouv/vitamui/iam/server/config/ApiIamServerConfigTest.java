package fr.gouv.vitamui.iam.server.config;

import fr.gouv.vitamui.commons.logbook.service.EventService;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class ApiIamServerConfigTest extends AbstractMongoTests {

    @Autowired
    private EventService logbookService;

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private PasswordConfiguration passwordConfiguration;

    @Autowired
    @Qualifier("discussionMongoDatabaseFactory")
    private MongoDatabaseFactory discussionMongoDatabaseFactory;

    @Autowired
    @Qualifier("discussionReactiveMongoDatabaseFactory")
    private ReactiveMongoDatabaseFactory discussionReactiveMongoDatabaseFactory;

    @Test
    void testContext() {
        assertThat(logbookService).isNotNull();
    }

    @Test
    void testPasswordConfiguration() {
        assertThat(passwordConfiguration).isNotNull();
        assertThat(passwordConfiguration.getMaxOldPassword()).isEqualTo(Integer.valueOf(12));
    }

    @Test
    void testDiscussionMongoConfiguration() {
        assertThat(discussionMongoDatabaseFactory.getMongoDatabase().getName()).isEqualTo("discussions");
        assertThat(discussionReactiveMongoDatabaseFactory.getMongoDatabase().block().getName()).isEqualTo(
            "discussions"
        );
    }
}
