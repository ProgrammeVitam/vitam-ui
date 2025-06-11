package fr.gouv.vitamui.commons.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class JsonUtilsTest {

    @Test
    public void readTreeTest() throws IOException {
        JsonNode json = JsonUtils.readTree("{}");
        Assertions.assertNotNull(json);
    }
}
