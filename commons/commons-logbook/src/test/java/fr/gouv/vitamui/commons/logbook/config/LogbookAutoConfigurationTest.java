package fr.gouv.vitamui.commons.logbook.config;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClientMock;
import fr.gouv.vitamui.commons.logbook.service.EventService;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class LogbookAutoConfigurationTest extends AbstractMongoTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class));

    @Test
    @Disabled
    public void serviceNameCanBeConfigured() {
        contextRunner
            .withUserConfiguration(UserConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(EventService.class);
            });
    }

    @Configuration
    static class UserConfiguration {

        @Bean
        AdminExternalClient adminExternalClient() {
            return new AdminExternalClientMock();
        }
    }
}
