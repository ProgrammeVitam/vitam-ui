package fr.gouv.vitamui.commons.logbook.service;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EventServiceIntegrationTest extends AbstractMongoTests {

    @MockitoBean
    private AdminExternalClient adminExternalClient;

    @Autowired
    private EventRepository repository;

    @Autowired
    private EventService service;

    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void createLogbook() {
        String evIdReq = UUID.randomUUID().toString();
        HttpContext context = new HttpContext(10, "", true, "x-application-id", "identity", evIdReq, "", null);
        service.logCreate(context, "AC-000002", 10, "obId", "TEST", EventType.EXT_VITAMUI_CREATE_USER, "data");
        Optional<Event> logbook = repository.findOne(Query.query(Criteria.where("evIdReq").is(evIdReq)));
        assertThat(logbook).isPresent();
        assertThat(logbook.get().getOutDetail()).isEqualTo(EventType.EXT_VITAMUI_CREATE_USER + "." + StatusCode.OK);
    }
}
