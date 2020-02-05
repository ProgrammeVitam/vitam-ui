package fr.gouv.vitamui.commons.api.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.ApplicationTest;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(classes = { ServerIdentityAutoConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ActiveProfiles("test")
public class ApplicationPropertiesTest {

    @Test
    public void testApplicationProperties() {
        final ServerIdentityConfiguration serverIdentity = ServerIdentityConfiguration.getInstance();
        assertNotNull("ServerIdentityConfiguration should be created.", serverIdentity);
        assertEquals("ServerIdentityConfiguration name isn't correctly set.", "identityName",
                serverIdentity.getIdentityName());
        assertEquals("ServerIdentityConfiguration role isn't correctly set.", "identityRole",
                serverIdentity.getIdentityRole());
        assertEquals("ServerIdentityConfiguration serverId isn't correctly set.", 1,
                serverIdentity.getIdentityServerId());
        assertEquals("ServerIdentityConfiguration siteId isn't correctly set.", 0, serverIdentity.getIdentitySiteId());
    }

}
