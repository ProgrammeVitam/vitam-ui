package fr.gouv.vitamui.commons.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ResourcesUtilsTest {

    @Test
    void testBuildPath() {
        try (InputStream stream = ResourcesUtils.getResourceAsStream("json-test.json")) {
            assertTrue(stream != null);
        } catch (final IOException e) {
            fail("Should not raized an exception");
        }
    }
}
