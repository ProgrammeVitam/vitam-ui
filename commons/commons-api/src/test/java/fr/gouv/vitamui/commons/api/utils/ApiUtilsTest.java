package fr.gouv.vitamui.commons.api.utils;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;

public class ApiUtilsTest {

    @Test
    public void testGetContentFromResourceFile() throws IOException {
        final String expectedData = "Hello World from test.txt!!!";
        final String data = ApiUtils.getContentFromResourceFile(ApiUtilsTest.class, "test.txt");

        Assert.assertEquals(expectedData, data.trim());
    }

    @Test(expected = InternalServerException.class)
    public void testGetContentFromResourceFileNotFound() throws IOException {
        ApiUtils.getContentFromResourceFile(ApiUtilsTest.class, "test2.txt");
    }
}
