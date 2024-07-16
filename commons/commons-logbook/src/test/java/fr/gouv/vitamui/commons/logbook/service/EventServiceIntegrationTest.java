package fr.gouv.vitamui.commons.logbook.service;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class EventServiceIntegrationTest extends AbstractMongoTests {

    @MockBean
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
    @Disabled
    public void createLogbook() {
        String evIdReq = UUID.randomUUID().toString();
        InternalHttpContext context = new InternalHttpContext(
            10,
            "",
            "",
            "",
            "x-application-id",
            "identity",
            evIdReq,
            ""
        );
        service.logCreate(context, "AC-000002", 10, "obId", "TEST", EventType.EXT_VITAMUI_CREATE_USER, "data");
        Optional<Event> logbook = repository.findOne(Query.query(Criteria.where("evIdReq").is(evIdReq)));
        assertThat(logbook).isPresent();
        assertThat(logbook.get().getOutDetail()).isEqualTo(EventType.EXT_VITAMUI_CREATE_USER + "." + StatusCode.OK);
    }
}
