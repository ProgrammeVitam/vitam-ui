package fr.gouv.vitamui.iam.internal.server.common.rest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link BaseStatusController}.
 *
 *
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
