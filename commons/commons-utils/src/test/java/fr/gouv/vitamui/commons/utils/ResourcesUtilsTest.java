package fr.gouv.vitamui.commons.utils;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ResourcesUtilsTest {

    @Test
    public void testBuildPath() {
        try (InputStream stream = ResourcesUtils.getResourceAsStream("json-test.json")) {
            assertTrue(stream != null);
        }
        catch (final IOException e) {
            fail("Should not raized an exception");
        }
    }

}
