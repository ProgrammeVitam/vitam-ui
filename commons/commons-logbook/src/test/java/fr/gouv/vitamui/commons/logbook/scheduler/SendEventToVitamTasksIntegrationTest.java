package fr.gouv.vitamui.commons.logbook.scheduler;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.common.EventStatus;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SendEventToVitamTasksIntegrationTest extends AbstractMongoTests {

    private AutoCloseable mocks;

    private final Long retryErrorEventInMinutes = 60L;
    private SendEventToVitamTasks sendEventToVitamTasks;

    @Autowired
    private EventRepository eventRepository;

    @MockitoBean
    private AdminExternalClient adminExternalClient;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        sendEventToVitamTasks = new SendEventToVitamTasks(eventRepository, adminExternalClient);
        sendEventToVitamTasks = Mockito.spy(sendEventToVitamTasks);
        sendEventToVitamTasks.setRetryErrorEventInMinutes(retryErrorEventInMinutes);
        eventRepository.deleteAll();
    }

    @Test
    void run_then_ok() {
        sendEventToVitamTasks.run();
    }

    @Test
    void getEventsElligibleToBeSentToVitam_when_oneEventsIsCreated_then_return_oneEvent() {
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
    void getEventsElligibleToBeSentToVitam_when_oneEventsIsOnError_and_isElligibleToBeResend_then_return_oneEvents() {
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
    void getEventsElligibleToBeSentToVitam_when_oneEventsIsOnError_and_isNotElligibleToBeResend_then_return_emptyList() {
        List<Event> mockResults = new ArrayList<>();
        mockResults.add(new Event());

        Event ev = new Event();
        ev.setStatus(EventStatus.ERROR);
        ev.setSynchronizedVitamDate(OffsetDateTime.now().minusMinutes(retryErrorEventInMinutes - 30L));

        eventRepository.save(ev);

        List<Event> events = sendEventToVitamTasks.getEventsElligibleToBeSentToVitam();
        assertThat(events).isEmpty();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
