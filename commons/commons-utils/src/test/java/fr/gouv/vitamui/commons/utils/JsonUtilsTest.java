package fr.gouv.vitamui.commons.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class JsonUtilsTest {

    @Test
    public void readTreeTest() throws IOException {
        JsonNode json = JsonUtils.readTree("{}");
        Assert.assertNotNull(json);
    }
}
