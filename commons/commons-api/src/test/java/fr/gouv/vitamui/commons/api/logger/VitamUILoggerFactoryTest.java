package fr.gouv.vitamui.commons.api.logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.config.ServerIdentityConfigurationForProfiles;

/**
 * VitamUILoggerFactory Test.
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Import(ServerIdentityConfigurationForProfiles.class)
public class VitamUILoggerFactoryTest extends AbstractVitamUITest {

    @Test
    public void testGetInstanceWithClass() {
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUILoggerTest.class);
        assertNotNull("Logger should be created.", logger);
        assertEquals("Logger name isn't correctly set.", VitamUILoggerTest.class.getName(), logger.getName());
        assertTrue("Log messages should be empty.", buf.length() == 0);
        logger.info("Message");
        assertTrue("Log message should be written.", buf.length() > 0);
    }

}
