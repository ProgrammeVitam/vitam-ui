package fr.gouv.vitamui.commons.utils;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtilsTest {

    @Test
    public void readTreeTest() throws IOException {
        JsonNode json = JsonUtils.readTree("{}");
        Assert.assertNotNull(json);

    }
}
