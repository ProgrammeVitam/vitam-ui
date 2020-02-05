package fr.gouv.vitamui.commons.api.property;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CommonPropertiesTest {

    @Test
    public void testProperties() {

        CommonProperties properties =  new CommonProperties() ;

        String adminPath = properties.getAdminPath() ;
        assertTrue(adminPath == "/admin/v0");

        int timeout = properties.getConnectTimeout() ;
        assertTrue(timeout == 2000);

    }

}
