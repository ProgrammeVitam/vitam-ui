package fr.gouv.vitamui.iam.server.common.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link BaseStatusController}.
 */
final class BaseStatusControllerTest {

    private final MockStatusController controller = new MockStatusController();

    @Test
    void testStatus() {
        assertEquals("OK", controller.status());
    }

    @Test
    void testAutoTest() {
        assertEquals("Test : OK", controller.autotest());
    }
}
