package fr.gouv.vitamui.commons.logbook.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.data.mongodb.core.query.Query;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.LogbookExternalClientException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.logbook.common.parameters.LogbookOperationParameters;
import fr.gouv.vitam.logbook.common.parameters.LogbookParametersFactory;
import fr.gouv.vitamui.commons.logbook.common.EventStatus;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.common.EventTypeProc;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;

public class SendEventToVitamTasksTest {

    private SendEventToVitamTasks sendEventToVitamTasks;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AdminExternalClient adminExternalClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        sendEventToVitamTasks = new SendEventToVitamTasks(eventRepository, adminExternalClient);
        sendEventToVitamTasks = Mockito.spy(sendEventToVitamTasks);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        sendEventToVitamTasks.setRetryErrorEventInMinutes(60L);
    }

    @Test
    public void run_then_ok() {
        sendEventToVitamTasks.run();
    }

    @Test
    public void getEventsElligibleToBeSentToVitam_when_oneEventsIsElligible_then_return_oneEvents() {
        List<Event> mockResults = new ArrayList<>();
        mockResults.add(new Event());

        Mockito.when(eventRepository.findAll(ArgumentMatchers.any(Query.class))).thenReturn(mockResults);
        List<Event> events = sendEventToVitamTasks.getEventsElligibleToBeSentToVitam();
        assertThat(events).isNotEmpty();
        assertThat(events).hasSize(1);
    }

    @Test()
    public void sendToVitam_when_vitamIsUnreacheable_then_eventSatusIsError() throws InvalidParseOperationException,
            IllegalArgumentException, IOException, LogbookExternalClientException {
        Comparator<Event> byPersistedDate = (final Event e1, final Event e2) -> e1.getCreationDate()
                .compareTo(e2.getCreationDate());
        TreeSet<Event> events = new TreeSet<>(byPersistedDate);
        Event ev1 = new Event();
        ev1.setCreationDate(System.currentTimeMillis());
        events.add(ev1);
        Mockito.doReturn(LogbookParametersFactory.newLogbookOperationParameters()).when(sendEventToVitamTasks)
                .convertEventToMaster(ArgumentMatchers.any());
        Mockito.doReturn(LogbookParametersFactory.newLogbookOperationParameters()).when(sendEventToVitamTasks)
                .convertEventToLogbookOperationParams(ArgumentMatchers.any());
        Mockito.when(adminExternalClient.createExternalOperation(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new LogbookExternalClientException("Vitam client exception"));
        sendEventToVitamTasks.sendToVitam(events);
        Mockito.verify(sendEventToVitamTasks, VerificationModeFactory.times(1)).updateEventStatus(
                ArgumentMatchers.any(), ArgumentMatchers.eq(EventStatus.ERROR), ArgumentMatchers.any());

    }

    @Test
    public void sendToVitam_when_vitamResponseIs201_then_eventSatusIsSuccess() throws InvalidParseOperationException,
            IllegalArgumentException, IOException, LogbookExternalClientException {
        Comparator<Event> byPersistedDate = (final Event e1, final Event e2) -> e1.getCreationDate()
                .compareTo(e2.getCreationDate());
        TreeSet<Event> events = new TreeSet<>(byPersistedDate);
        Event ev1 = new Event();
        ev1.setCreationDate(System.currentTimeMillis());
        events.add(ev1);
        Mockito.doReturn(LogbookParametersFactory.newLogbookOperationParameters()).when(sendEventToVitamTasks)
                .convertEventToMaster(ArgumentMatchers.any());
        Mockito.doReturn(LogbookParametersFactory.newLogbookOperationParameters()).when(sendEventToVitamTasks)
                .convertEventToLogbookOperationParams(ArgumentMatchers.any());

        RequestResponse reqResponse = new RequestResponseOK<>();
        reqResponse.setHttpCode(Status.CREATED.getStatusCode());
        Mockito.when(adminExternalClient.createExternalOperation(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(reqResponse);
        sendEventToVitamTasks.sendToVitam(events);
        Mockito.verify(sendEventToVitamTasks, VerificationModeFactory.times(1)).updateEventStatus(
                ArgumentMatchers.any(), ArgumentMatchers.eq(EventStatus.SUCCESS), ArgumentMatchers.any());
    }

    @Test
    public void convertEventToMaster_when_eventIsCorrectFormed_then_ok() throws IllegalArgumentException, IOException {
        Event event = new Event();
        event.setId("id");
        event.setEvType(EventType.EXT_VITAMUI_CREATE_USER.toString());
        event.setEvIdProc("idproc");
        event.setEvIdReq("evIdreq");
        event.setEvParentId("evParentId");
        event.setEvTypeProc(EventTypeProc.EXTERNAL_LOGBOOK);
        event.setOutDetail("outdetail");
        event.setOutcome(StatusCode.OK);
        event.setOutMessg("outMessg");
        event.setEvDetData("");
        event.setEvDateTime(OffsetDateTime.now().toString());
        LogbookOperationParameters op = sendEventToVitamTasks.convertEventToMaster(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertEventToMaster_when_eventIsMalFormed_then_throws_IllegalArgumentException()
            throws IllegalArgumentException, IOException {
        Event event = new Event();
        event.setEvType(EventType.EXT_VITAMUI_CREATE_USER.toString());
        event.setEvIdProc("idproc");
        event.setEvIdReq("evIdreq");
        event.setEvParentId("evParentId");
        event.setEvTypeProc(EventTypeProc.EXTERNAL_LOGBOOK);
        event.setOutDetail("outdetail");
        event.setOutcome(StatusCode.OK);
        event.setOutMessg("outMessg");
        event.setEvDetData("");
        event.setEvDateTime(OffsetDateTime.now().toString());
        LogbookOperationParameters op = sendEventToVitamTasks.convertEventToMaster(event);
    }

    @Test
    public void convertEventToLogbookOperationParams_when_eventIsCorrectFormed_then_ok()
            throws IllegalArgumentException, IOException {
        Event event = new Event();
        event.setId("id");
        event.setEvType(EventType.EXT_VITAMUI_CREATE_USER.toString());
        event.setEvIdProc("idproc");
        event.setEvIdReq("evIdreq");
        event.setEvParentId("evParentId");
        event.setEvTypeProc(EventTypeProc.EXTERNAL_LOGBOOK);
        event.setOutDetail("outdetail");
        event.setOutcome(StatusCode.OK);
        event.setOutMessg("outMessg");
        event.setEvDetData("");
        event.setEvDateTime(OffsetDateTime.now().toString());
        LogbookOperationParameters op = sendEventToVitamTasks.convertEventToLogbookOperationParams(event);
    }

    @Test
    public void addDateInformation_when_evDetDataIsEmpty_then_ok() throws IOException {
        sendEventToVitamTasks.addDateInformation(StringUtils.EMPTY, OffsetDateTime.now().toString());
    }

    @Test
    public void addDateInformation_when_evDetDataIsEmptyJson_then_ok() throws IOException {
        sendEventToVitamTasks.addDateInformation("{}", OffsetDateTime.now().toString());
    }

    @Test(expected = IOException.class)
    public void addDateInformation_when_evDetDataFormatIsNotValid_then_IOException() throws IOException {
        sendEventToVitamTasks.addDateInformation("{,,}", OffsetDateTime.now().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDateInformation_when_evDetDataIsNull_then_ok() throws IOException {
        sendEventToVitamTasks.addDateInformation(null, OffsetDateTime.now().toString());
    }

}
