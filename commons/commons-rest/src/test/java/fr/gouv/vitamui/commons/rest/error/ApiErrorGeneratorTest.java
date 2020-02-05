package fr.gouv.vitamui.commons.rest.error;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.Import;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import fr.gouv.vitamui.commons.rest.ErrorsConstants;

@RunWith(PowerMockRunner.class)
@Import(ApiErrorGenerator.class)
public class ApiErrorGeneratorTest {

    @Test
    public void testKeyException() {
        final String buildKey = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assert.assertEquals("Key for Exception is incorrect.", buildKey, ErrorsConstants.API_ERRORS
                + ErrorsConstants.MESSAGE_DOT + ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testUnknownKeyException() {
        Assert.assertNull("Key for Exception is incorrect.", ApiErrorGenerator.buildKey(IOException.class));
    }

}
