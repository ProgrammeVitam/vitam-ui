package fr.gouv.vitamui.commons.vitam.api.util;

import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
public class VitamRestUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VitamRestUtilsTest.class);

    @Test
    void testCheckResponseOk() throws IOException {
        final JsonNode resposeContent = stringToJsonNode("{\"message\": \"an error occured\"}");
        final RequestResponseOK<JsonNode> vitamResponse = new RequestResponseOK<>(resposeContent);
        vitamResponse.setHttpCode(200);
        try {
            VitamRestUtils.checkResponse(vitamResponse);
            VitamRestUtils.checkResponse(vitamResponse, new Integer[] {});
            VitamRestUtils.checkResponse(vitamResponse, 200);
            VitamRestUtils.checkResponse(vitamResponse, 200, 201);
            vitamResponse.setHttpCode(202);
            VitamRestUtils.checkResponse(vitamResponse, 200, 201, 202);
        } catch (final InternalServerException e) {
            LOGGER.error("testCheckResponseOk failed", e);
            Assertions.fail("Response should be accepted");
        }
    }

    @Test
    void testCheckResponseNotAccepted() {
        assertThrows(InternalServerException.class, () -> {
            final JsonNode resposeContent = stringToJsonNode("{\"message\": \"an error occured\"}");
            final RequestResponseOK<JsonNode> vitamResponse = new RequestResponseOK<>(resposeContent);
            vitamResponse.setHttpCode(500);
            VitamRestUtils.checkResponse(vitamResponse);
        });
    }

    @Test
    void testCheckJavaxResponseAccepted() throws IOException {
        final Response vitamResponse = Response.ok().build();

        try {
            VitamRestUtils.checkResponse(vitamResponse);
            VitamRestUtils.checkResponse(vitamResponse, 200, 201);
        } catch (final InternalServerException e) {
            LOGGER.error("testCheckResponseOk failed", e);
            Assertions.fail("Response should be accepted");
        }
    }

    @Test
    void testCheckResponseNotFound() throws IOException {
        final String responseMessage = "An error occured.";
        final Response vitamResponse = Response.status(Status.NOT_FOUND).build();

        final Response mockResponse = spy(vitamResponse);
        doReturn("{\"message\": \"" + responseMessage + "\"}").when(mockResponse).readEntity(String.class);

        try {
            VitamRestUtils.checkResponse(mockResponse);
            VitamRestUtils.checkResponse(mockResponse, 200, 201);
        } catch (final NotFoundException e) {
            LOGGER.debug("checkReponse 404", e);
            assertThat(e.getMessage()).contains(responseMessage);
            return;
        }
        Assertions.fail("checkResponse should throw a NotFoundException");
    }

    protected JsonNode stringToJsonNode(final String str) throws IOException {
        return new ObjectMapper().readTree(str);
    }
}
