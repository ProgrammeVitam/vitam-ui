package fr.gouv.vitamui.commons.api.logger.debug;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.config.ServerIdentityConfigurationForProfiles;
import fr.gouv.vitamui.commons.api.logger.AbstractVitamUITest;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(ServerIdentityConfigurationForProfiles.class)
public class VitamUIDebugLoggerTest extends AbstractVitamUITest {

    @Test
    public void testIsDebugEnabled() {
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIDebugLoggerTest.class);
        assertTrue("Logger DEBUG should be enabled.", logger.isDebugEnabled() == true);
    }

    @Test
    public void testDebug() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIDebugLoggerTest.class);
        final String message = "message";
        logger.debug(message);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.length() > message.length());
    }

    @Test
    public void testDebugWithThrowable() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIDebugLoggerTest.class);
        final String message = "message";
        logger.debug(message, new IOException());
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.length() > message.length());
    }

    @Test
    public void testDebugWithOneArgument() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIDebugLoggerTest.class);
        final String message = "message";
        final String format = message + " {}";
        final Integer object1 = 10;
        logger.debug(format, object1);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message + " " + object1.toString()) > 0);
    }

    @Test
    public void testDebugWithTwoArguments() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIDebugLoggerTest.class);
        final String message = "message";
        final String format = message + " {} {}";
        final Integer object1 = 1;
        final Integer object2 = 2;
        logger.debug(format, object1, object2);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.",
                buf.lastIndexOf(message + " " + object1.toString() + " " + object2.toString()) > 0);
    }

    @Test
    public void testDebugWithNArguments() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIDebugLoggerTest.class);
        final String message = "message";
        final String format = message + " {} {} {}";
        final Integer object1 = 1;
        final Integer object2 = 2;
        final Integer object3 = 3;
        logger.debug(format, object1, object2, object3);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(
                message + " " + object1.toString() + " " + object2.toString() + " " + object3.toString()) > 0);
    }

}
