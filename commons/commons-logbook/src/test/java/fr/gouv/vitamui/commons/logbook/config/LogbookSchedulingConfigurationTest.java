package fr.gouv.vitamui.commons.logbook.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClientMock;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
public class LogbookSchedulingConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LogbookSchedulingConfiguration.class).withUserConfiguration(UserConfiguration.class);

    @BeforeClass
    public static void setup() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void schedulingDisabled() {
        contextRunner
                .withPropertyValues("logbook.scheduling.enabled=true",
                        "logbook.scheduling.sendEventToVitamTasks.enabled=false",
                        "logbook.scheduling.deleteSynchronizedEventsTasks.enabled=false",
                        "logbook.scheduling.deleteSynchronizedEventsTasks.cronExpression=0 0,30 0-6 ? * *")
                .run((context) -> {
                    assertThat(context.containsBean("sendEventToVitamTasks")).isFalse();
                    assertThat(context.containsBean("deleteSyncrhonizedEventsTasks")).isFalse();
                });

        contextRunner
                .withPropertyValues("logbook.scheduling.enabled=false",
                        "logbook.scheduling.deleteSynchronizedEventsTasks.cronExpression=0 0,30 0-6 ? * *")
                .run((context) -> {
                    assertThat(context.containsBean("sendEventToVitamTasks")).isFalse();
                    assertThat(context.containsBean("deleteSyncrhonizedEventsTasks")).isFalse();
                });
    }

    @Test
    public void schedulingEnabled() {
        contextRunner
                .withPropertyValues("logbook.scheduling.enabled=true",
                        "logbook.scheduling.sendEventToVitamTasks.delay=900000",
                        "logbook.scheduling.deleteSynchronizedEventsTasks.cronExpression=0 0,30 0-6 ? * *")
                .run((context) -> {
                    assertThat(context.containsBean("sendEventToVitamTasks")).isTrue();
                    assertThat(context.containsBean("deleteSynchronizedEventsTasks")).isTrue();
                });
    }

    @Configuration
    static class UserConfiguration {

        @Bean
        EventRepository eventRepository() {
            return mock(EventRepository.class);
        }

        @Bean
        AdminExternalClient adminExternalClient() {
            return new AdminExternalClientMock();
        }

    }

}
