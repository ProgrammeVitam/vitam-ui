package fr.gouv.vitamui.commons.mongo.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.mongo.domain.Person;
import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VitamUICrudServiceTest {

    private final VitamUIRepository<Person, String> repository = mock(VitamUIRepository.class);

    private final Logger logger = (Logger) LoggerFactory.getLogger(VitamUICrudService.class);

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private Level previousLevel;

    @BeforeEach
    void setUp() {
        previousLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        logger.setLevel(previousLevel);
    }

    @Test
    void patch_should_not_log_patched_values() {
        final Person person = mock(Person.class);
        when(person.getId()).thenReturn("person-id");
        when(repository.findById("person-id")).thenReturn(Optional.of(person));
        when(repository.existsById("person-id")).thenReturn(true);
        when(repository.save(any())).thenReturn(person);

        new PersonCrudService(repository).patch(Map.of("id", "person-id", "keystorePassword", "patched-value"));

        assertThat(appender.list).isNotEmpty();
        assertThat(appender.list).noneMatch(event -> event.getFormattedMessage().contains("patched-value"));
    }

    private static class PersonDto extends IdDto {}

    private static class PersonCrudService extends VitamUICrudService<PersonDto, Person> {

        private final VitamUIRepository<Person, String> repository;

        PersonCrudService(final VitamUIRepository<Person, String> repository) {
            super(null);
            this.repository = repository;
        }

        @Override
        protected VitamUIRepository<Person, String> getRepository() {
            return repository;
        }

        @Override
        protected Class<Person> getEntityClass() {
            return Person.class;
        }

        @Override
        public PersonDto internalConvertFromEntityToDto(final Person entity) {
            return new PersonDto();
        }
    }
}
