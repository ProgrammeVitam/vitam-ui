package fr.gouv.vitamui.commons.logbook.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.TestMongoConfig;
import fr.gouv.vitamui.commons.logbook.common.EventStatus;
import fr.gouv.vitamui.commons.logbook.config.LogbookAutoConfiguration;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { LogbookAutoConfiguration.class, TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = EventRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class SendEventToVitamTasksIntegTest {

    private SendEventToVitamTasks sendEventToVitamTasks;

    @Autowired
    private EventRepository eventRepository;

    @MockBean
    private AdminExternalClient adminExternalClient;

    private final Long retryErrorEventInMinutes = 60L;

    @BeforeClass
    public static void beforeClass() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);

    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        sendEventToVitamTasks = new SendEventToVitamTasks(eventRepository, adminExternalClient);
        sendEventToVitamTasks = Mockito.spy(sendEventToVitamTasks);
        sendEventToVitamTasks.setRetryErrorEventInMinutes(retryErrorEventInMinutes);
        eventRepository.deleteAll();
    }

    @Test
    public void run_then_ok() {
        sendEventToVitamTasks.run();
    }

    @Test
    public void getEventsElligibleToBeSentToVitam_when_oneEventsIsCreated_then_return_oneEvent() {
        List<Event> mockResults = new ArrayList<>();
        mockResults.add(new Event());

        Event ev1 = new Event();
        ev1.setStatus(EventStatus.CREATED);

        Event ev2 = new Event();
        ev2.setStatus(EventStatus.ERROR);

        eventRepository.saveAll(Arrays.asList(ev1, ev2));

        List<Event> events = sendEventToVitamTasks.getEventsElligibleToBeSentToVitam();
        assertThat(events).isNotEmpty();
        assertThat(events).hasSize(1);
    }

    @Test
    public void getEventsElligibleToBeSentToVitam_when_oneEventsIsOnError_and_isElligibleToBeResend_then_return_oneEvents() {
        List<Event> mockResults = new ArrayList<>();
        mockResults.add(new Event());

        Event ev = new Event();
        ev.setStatus(EventStatus.ERROR);
        ev.setSynchronizedVitamDate(OffsetDateTime.now().minusMinutes(retryErrorEventInMinutes));

        eventRepository.save(ev);

        List<Event> events = sendEventToVitamTasks.getEventsElligibleToBeSentToVitam();
        assertThat(events).isNotEmpty();
        assertThat(events).hasSize(1);
    }

    @Test
    public void getEventsElligibleToBeSentToVitam_when_oneEventsIsOnError_and_isNotElligibleToBeResend_then_return_emptyList() {
        List<Event> mockResults = new ArrayList<>();
        mockResults.add(new Event());

        Event ev = new Event();
        ev.setStatus(EventStatus.ERROR);
        ev.setSynchronizedVitamDate(OffsetDateTime.now().minusMinutes(retryErrorEventInMinutes - 30L));

        eventRepository.save(ev);

        List<Event> events = sendEventToVitamTasks.getEventsElligibleToBeSentToVitam();
        assertThat(events).isEmpty();
    }
}
