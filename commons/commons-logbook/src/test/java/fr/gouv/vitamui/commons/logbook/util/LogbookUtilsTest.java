package fr.gouv.vitamui.commons.logbook.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.Response;

import fr.gouv.vitam.common.GlobalDataRest;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;

public class LogbookUtilsTest {

    @Test
    public void testRetrieveJsonData() throws InvalidParseOperationException {
        EventDiffDto evData = new EventDiffDto("Identifiant du contrat d'accès pour l'arbre", "AC-00001", "AC-00002");
        String evDetDataString = LogbookUtils.getEvData(Arrays.asList(evData)).toString();
        JsonNode evDetData = JsonHandler.getFromString(evDetDataString);
        assertThat(evDetData).isNotNull();

    }

    @Test
    public void testGetLogbookOperationStatus_whenParentOperationCompleted() throws JsonProcessingException {
        // Given
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvId("1");
        operation.setOutcome(StatusCode.OK.toString());
        operation.setEvType("type");
        final RequestResponse<LogbookOperation> response = new RequestResponseOK<LogbookOperation>().addResult(operation)
                .addHeader(GlobalDataRest.X_REQUEST_ID, "requestId")
                .setHttpCode(Response.Status.OK.getStatusCode());
        // When
        StatusCode statusCode = LogbookUtils.getLogbookOperationStatus(response);
        //Then
        assertThat(statusCode).isEqualTo(StatusCode.OK);
    }

    @Test
    public void testGetLogbookOperationStatus_whenChildOperationCompleted() throws JsonProcessingException {
        // Given
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvId("0");
        operation.setOutcome(StatusCode.STARTED.toString());
        operation.setEvType("type");
        final LogbookOperation subOperation1 = new LogbookOperation();
        subOperation1.setEvId("1");
        subOperation1.setOutcome(StatusCode.STARTED.toString());
        final LogbookOperation subOperation2 = new LogbookOperation();
        subOperation2.setEvId("2");
        subOperation2.setOutcome(StatusCode.OK.toString());
        subOperation2.setEvType("type");
        operation.setEvents(new ArrayList<>());
        operation.getEvents()
                .addAll(List.of(subOperation1, subOperation2));

        final RequestResponse<LogbookOperation> response = new RequestResponseOK<LogbookOperation>().addResult(operation)
                .addHeader(GlobalDataRest.X_REQUEST_ID, "requestId")
                .setHttpCode(Response.Status.OK.getStatusCode());
        // When
        StatusCode statusCode = LogbookUtils.getLogbookOperationStatus(response);
        //Then
        assertThat(statusCode).isEqualTo(StatusCode.OK);
    }

    @Test
    public void testGetLogbookOperationStatus_whenChildOperationNotParentType() throws JsonProcessingException {
        // Given
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvId("0");
        operation.setOutcome(StatusCode.STARTED.toString());
        operation.setEvType("type");
        final LogbookOperation subOperation1 = new LogbookOperation();
        subOperation1.setEvId("1");
        subOperation1.setOutcome(StatusCode.OK.toString());
        operation.setEvType("not parent type");
        operation.setEvents(new ArrayList<>());
        operation.getEvents().add(subOperation1);

        final RequestResponse<LogbookOperation> response = new RequestResponseOK<LogbookOperation>().addResult(operation)
                .addHeader(GlobalDataRest.X_REQUEST_ID, "requestId")
                .setHttpCode(Response.Status.OK.getStatusCode());
        // When
        StatusCode statusCode = LogbookUtils.getLogbookOperationStatus(response);
        // Then
        assertThat(statusCode).isEqualTo(StatusCode.STARTED);
    }
}
