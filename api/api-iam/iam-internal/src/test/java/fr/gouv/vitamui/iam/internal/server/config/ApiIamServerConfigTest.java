package fr.gouv.vitamui.iam.internal.server.config;

import fr.gouv.vitamui.commons.logbook.service.EventService;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class ApiIamServerConfigTest extends AbstractMongoTests {

    @Autowired
    private EventService logbookService;

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
