package fr.gouv.vitamui.iam.server.common.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link BaseStatusController}.
 */
public final class BaseStatusControllerTest {

    private final MockStatusController controller = new MockStatusController();

    @Test
    public void testStatus() {
        assertEquals("OK", controller.status());
    }

    @Test
    public void testAutoTest() {
        assertEquals("Test : OK", controller.autotest());
    }
}
