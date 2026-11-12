/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
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

        assertThat(appender.list)
            .isNotEmpty()
            .noneMatch(event -> event.getFormattedMessage().contains("patched-value"));
    }

    @Test
    void create_and_update_should_not_log_dto_values() {
        final Person person = mock(Person.class);
        when(person.getId()).thenReturn("person-id");
        when(repository.existsById("person-id")).thenReturn(true);
        when(repository.save(any())).thenReturn(person);
        final PersonCrudService service = new PersonCrudService(repository) {
            @Override
            protected Person internalConvertFromDtoToEntity(final PersonDto dto) {
                return person;
            }
        };

        service.create(new PersonDto("secret-value"));
        final PersonDto updatedDto = new PersonDto("secret-value");
        updatedDto.setId("person-id");
        service.update(updatedDto);

        assertThat(appender.list).isNotEmpty().noneMatch(event -> event.getFormattedMessage().contains("secret-value"));
    }

    private static class PersonDto extends IdDto {

        private final String password;

        PersonDto() {
            this(null);
        }

        PersonDto(final String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "PersonDto(password=" + password + ")";
        }
    }

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
