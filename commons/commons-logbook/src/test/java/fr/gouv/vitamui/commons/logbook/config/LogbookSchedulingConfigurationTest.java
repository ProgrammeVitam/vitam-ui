package fr.gouv.vitamui.commons.logbook.config;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClientMock;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
class LogbookSchedulingConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(UserConfiguration.class, LogbookSchedulingConfiguration.class);

    @Test
    void shouldBeDisabled_when_propertiesIsFalse() {
        contextRunner
            .withPropertyValues(
                "instance.primary=true",
                "logbook.scheduling.enabled=true",
                "logbook.scheduling.sendEventToVitamTasks.enabled=false",
                "logbook.scheduling.deleteSynchronizedEventsTasks.enabled=false",
                "logbook.scheduling.deleteSynchronizedEventsTasks.cronExpression=0 0,30 0-6 ? * *"
            )
            .run(context -> {
                assertThat(context.containsBean("sendEventToVitamTasks")).isFalse();
                assertThat(context.containsBean("deleteSyncrhonizedEventsTasks")).isFalse();
            });
    }

    @Test
    void shouldBeDisabled_whenIsNotPrimary() {
        contextRunner
            .withPropertyValues(
                "instance.primary=false",
                "logbook.scheduling.enabled=false",
                "logbook.scheduling.deleteSynchronizedEventsTasks.cronExpression=0 0,30 0-6 ? * *"
            )
            .run(context -> {
                assertThat(context.containsBean("sendEventToVitamTasks")).isFalse();
                assertThat(context.containsBean("deleteSyncrhonizedEventsTasks")).isFalse();
            });
    }

    @Test
    void shouldBeEnabled_whenIsPrimaryAndPropertiesIsTrue() {
        contextRunner
            .withPropertyValues(
                "instance.primary=true",
                "logbook.scheduling.enabled=true",
                "logbook.scheduling.sendEventToVitamTasks.enabled=true",
                "logbook.scheduling.deleteSynchronizedEventsTasks.enabled=true",
                "logbook.scheduling.sendEventToVitamTasks.delay=900000",
                "logbook.scheduling.deleteSynchronizedEventsTasks.cronExpression=0 0,30 0-6 ? * *"
            )
            .run(context -> {
                assertThat(context.containsBean("sendEventToVitamTasks")).isTrue();
                assertThat(context.containsBean("deleteSynchronizedEventsTasks")).isTrue();
            });
    }

    @Test
    void shouldBeEnabled_whenIsPrimaryAndPropertiesIsMissing() {
        contextRunner
            .withPropertyValues(
                "instance.primary=true",
                "logbook.scheduling.enabled=true",
                "logbook.scheduling.sendEventToVitamTasks.delay=900000",
                "logbook.scheduling.deleteSynchronizedEventsTasks.cronExpression=0 0,30 0-6 ? * *"
            )
            .run(context -> {
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
