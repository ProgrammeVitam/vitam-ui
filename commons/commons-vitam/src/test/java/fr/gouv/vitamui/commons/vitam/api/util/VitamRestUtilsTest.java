package fr.gouv.vitamui.commons.vitam.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(SpringRunner.class)
public class VitamRestUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VitamRestUtilsTest.class);

    @Test
    public void testCheckResponseOk() throws IOException {
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
            Assert.fail("Response should be accepted");
        }
    }

    @Test(expected = InternalServerException.class)
    public void testCheckResponseNotAccepted() throws IOException {
        final JsonNode resposeContent = stringToJsonNode("{\"message\": \"an error occured\"}");
        final RequestResponseOK<JsonNode> vitamResponse = new RequestResponseOK<>(resposeContent);
        vitamResponse.setHttpCode(500);
        VitamRestUtils.checkResponse(vitamResponse);
    }

    @Test
    public void testCheckJavaxResponseAccepted() throws IOException {
        final Response vitamResponse = Response.ok().build();

        try {
            VitamRestUtils.checkResponse(vitamResponse);
            VitamRestUtils.checkResponse(vitamResponse, 200, 201);
        } catch (final InternalServerException e) {
            LOGGER.error("testCheckResponseOk failed", e);
            Assert.fail("Response should be accepted");
        }
    }

    @Test
    public void testCheckResponseNotFound() throws IOException {
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
        Assert.fail("checkResponse should throw a NotFoundException");
    }

    protected JsonNode stringToJsonNode(final String str) throws IOException {
        return new ObjectMapper().readTree(str);
    }
}
