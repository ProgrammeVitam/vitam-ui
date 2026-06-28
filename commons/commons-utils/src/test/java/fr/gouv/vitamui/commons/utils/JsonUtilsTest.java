package fr.gouv.vitamui.commons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;

public class JsonUtilsTest {

    @Test
    void readTreeTest() throws IOException {
        JsonNode json = JsonUtils.readTree("{}");
        Assertions.assertNotNull(json);
    }
}
