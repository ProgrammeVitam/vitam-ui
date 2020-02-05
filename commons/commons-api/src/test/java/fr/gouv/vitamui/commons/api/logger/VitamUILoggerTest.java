package fr.gouv.vitamui.commons.api.logger;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.config.ServerIdentityConfigurationForProfiles;

/**
 * VitamUILogger Test.
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Import(ServerIdentityConfigurationForProfiles.class)
public class VitamUILoggerTest extends AbstractVitamUITest {

    @Test
    public void testGetName() {
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUILoggerTest.class);
        assertEquals("Logger name isn't correctly set.", VitamUILoggerTest.class.getName(), logger.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithoutNameAndLogger() {
        new VitamUILoggerImpl(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithoutName() {
        new VitamUILoggerImpl(null, LoggerFactory.getLogger("Test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithoutLogger() {
        new VitamUILoggerImpl("VitamUILogger", null);
    }

    @Test
    public void testToString() {
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUILoggerTest.class);
        assertEquals("Logger name isn't correctly set.",
                "VitamUILoggerImpl(fr.gouv.vitamui.commons.api.logger.VitamUILoggerTest)", logger.toString());
    }

}
