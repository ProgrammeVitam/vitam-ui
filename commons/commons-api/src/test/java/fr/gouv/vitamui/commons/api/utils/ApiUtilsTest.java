package fr.gouv.vitamui.commons.api.utils;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApiUtilsTest {

    @Test
    public void testGetContentFromResourceFile() throws IOException {
        final String expectedData = "Hello World from test.txt!!!";
        final String data = ApiUtils.getContentFromResourceFile(ApiUtilsTest.class, "test.txt");

        Assertions.assertEquals(expectedData, data.trim());
    }

    @Test
    public void testGetContentFromResourceFileNotFound() {
        assertThrows(
            InternalServerException.class,
            () -> ApiUtils.getContentFromResourceFile(ApiUtilsTest.class, "test2.txt")
        );
    }
}
