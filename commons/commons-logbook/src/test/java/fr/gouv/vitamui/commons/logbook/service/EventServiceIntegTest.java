package fr.gouv.vitamui.commons.logbook.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.sun.jna.platform.win32.Guid.GUID;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitamui.commons.logbook.TestMongoConfig;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.config.LogbookAutoConfiguration;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { LogbookAutoConfiguration.class, TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = EventRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class EventServiceIntegTest {

    @MockBean
    private AdminExternalClient adminExternalClient;

    @Autowired
    private EventRepository repository;

    @Autowired
    private EventService service;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @BeforeClass
    public static void setup() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void createLogbook() {
        String evIdReq = GUID.newGuid().toString();
        InternalHttpContext context = new InternalHttpContext(10, "", "", "", "x-application-id", "identity", evIdReq,
                "");
        service.logCreate(context, "AC-000002", 10, "obId", "TEST", EventType.EXT_VITAMUI_CREATE_USER, "data");
        Optional<Event> logbook = repository.findOne(Query.query(Criteria.where("evIdReq").is(evIdReq)));
        assertThat(logbook).isPresent();
        assertThat(logbook.get().getOutDetail()).isEqualTo(EventType.EXT_VITAMUI_CREATE_USER + "." + StatusCode.OK);

    }

}
