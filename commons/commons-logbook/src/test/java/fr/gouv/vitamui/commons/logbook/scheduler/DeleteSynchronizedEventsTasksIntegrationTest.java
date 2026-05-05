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
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DeleteSynchronizedEventsTasksIntegrationTest extends AbstractMongoTests {

    private AutoCloseable mocks;

    private final Long ttlInDays = 30L;

    @Autowired
    private EventRepository eventRepository;

    private DeleteSynchronizedEventsTasks deleteSynchronizedEventsTasks;

    @MockitoBean
    private AdminExternalClient adminExternalClient;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        deleteSynchronizedEventsTasks = new DeleteSynchronizedEventsTasks(eventRepository);
        deleteSynchronizedEventsTasks = Mockito.spy(deleteSynchronizedEventsTasks);
        deleteSynchronizedEventsTasks.setTtlInDays(ttlInDays);
        eventRepository.deleteAll();
    }

    @Test
    void run_then_ok() {
        Event ev = new Event();
        ev.setStatus(EventStatus.SUCCESS);
        ev.setSynchronizedVitamDate(OffsetDateTime.now().minusDays(ttlInDays));
        eventRepository.save(ev);
        Mockito.when(deleteSynchronizedEventsTasks.getEventsElligibleToBeDeleted()).thenCallRealMethod();
        deleteSynchronizedEventsTasks.run();
    }

    @Test
    void getEventsElligibleToBeDeleted_when_noneIsElligible_then_returnEmptyList() {
        Event ev = new Event();
        ev.setSynchronizedVitamDate(OffsetDateTime.now());
        ev.setStatus(EventStatus.SUCCESS);
        eventRepository.save(ev);

        Collection<Event> events = deleteSynchronizedEventsTasks.getEventsElligibleToBeDeleted();
        assertThat(events).isEmpty();
    }

    @Test
    void getEventsElligibleToBeDeleted_when_oneIsElligible_then_returnOneEvent() {
        Event ev = new Event();
        ev.setSynchronizedVitamDate(OffsetDateTime.now().minusDays(ttlInDays));
        ev.setStatus(EventStatus.SUCCESS);
        eventRepository.save(ev);

        Event evNotElligible = new Event();
        evNotElligible.setSynchronizedVitamDate(OffsetDateTime.now().minusDays(ttlInDays - 1));
        evNotElligible.setStatus(EventStatus.SUCCESS);
        eventRepository.save(evNotElligible);

        Collection<Event> events = deleteSynchronizedEventsTasks.getEventsElligibleToBeDeleted();
        assertThat(events).isNotEmpty();
        assertThat(events).hasSize(1);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
