package fr.gouv.vitamui.commons.rest.error;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import fr.gouv.vitamui.commons.rest.ErrorsConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ApiErrorGeneratorTest {

    @Test
    void testKeyException() {
        final String buildKey = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertEquals(
            buildKey,
            ErrorsConstants.API_ERRORS + ErrorsConstants.MESSAGE_DOT + ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR,
            "Key for Exception is incorrect."
        );
    }

    @Test
    void testUnknownKeyException() {
        Assertions.assertNull(ApiErrorGenerator.buildKey(IOException.class), "Key for Exception is incorrect.");
    }
}
