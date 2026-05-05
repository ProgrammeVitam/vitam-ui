package fr.gouv.vitamui.commons.api.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonPropertiesTest {

    @Test
    void testProperties() {
        CommonProperties properties = new CommonProperties();

        String adminPath = properties.getAdminPath();
        assertTrue(adminPath == "/admin/v0");

        int timeout = properties.getConnectTimeout();
        assertTrue(timeout == 2000);
    }
}
