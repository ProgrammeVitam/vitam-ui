package fr.gouv.vitamui.commons.logbook.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitamui.commons.logbook.TestMongoConfig;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.common.EventTypeProc;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = EventRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class EventRepositoryTest {

    @MockBean
    private AdminExternalClient adminExternalClient;

    @Autowired
    private EventRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void createEvent() {
        Event log = new Event();
        log.setEvTypeProc(EventTypeProc.EXTERNAL_LOGBOOK);
        log.setTenantIdentifier(10);
        log.setEvDateTime(OffsetDateTime.now().toString());
        log.setEvDetData("test");
        log.setEvIdProc("test");
        log.setEvIdReq("test");
        log.setObId("test");
        log.setOutcome(StatusCode.KO);
        log.setOutMessg("coucou");
        log.setEvType(EventType.EXT_VITAMUI_CREATE_USER.toString());
        log.setOutMessg("" + EventType.EXT_VITAMUI_CREATE_USER + StatusCode.OK);
        log.setEvIdAppSession("evIdAppSession");
        log.setCreationDate(System.currentTimeMillis());
        log = repository.save(log);
        assertThat(log.getId()).isNotBlank();
    }
}
