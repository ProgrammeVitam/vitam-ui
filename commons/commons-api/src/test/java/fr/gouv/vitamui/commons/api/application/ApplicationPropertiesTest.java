package fr.gouv.vitamui.commons.api.application;

import fr.gouv.vitamui.commons.api.ApplicationTest;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApplicationPropertiesTest {

    @Autowired
    private ServerIdentityConfiguration serverIdentityConfiguration;

    @Test
    public void testApplicationProperties() {
        assertNotNull("ServerIdentityConfiguration should be created.", serverIdentityConfiguration);
        assertEquals(
            "ServerIdentityConfiguration name isn't correctly set.",
            "identityName",
            serverIdentityConfiguration.getIdentityName()
        );
        assertEquals(
            "ServerIdentityConfiguration role isn't correctly set.",
            "identityRole",
            serverIdentityConfiguration.getIdentityRole()
        );
        assertEquals(
            "ServerIdentityConfiguration serverId isn't correctly set.",
            1,
            serverIdentityConfiguration.getIdentityServerId()
        );
        assertEquals(
            "ServerIdentityConfiguration siteId isn't correctly set.",
            0,
            serverIdentityConfiguration.getIdentitySiteId()
        );
    }
}
