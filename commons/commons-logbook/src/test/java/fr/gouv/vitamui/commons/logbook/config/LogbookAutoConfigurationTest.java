package fr.gouv.vitamui.commons.logbook.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClientMock;
import fr.gouv.vitamui.commons.logbook.TestMongoConfig;
import fr.gouv.vitamui.commons.logbook.service.EventService;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
public class LogbookAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class));

    @BeforeClass
    public static void setup() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void serviceNameCanBeConfigured() {
        contextRunner.withUserConfiguration(TestMongoConfig.class).withUserConfiguration(UserConfiguration.class)
                .run((context) -> {
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
