package fr.gouv.vitamui.commons.api.logger.warn;

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
public class VitamUIWarnLoggerTest extends AbstractVitamUITest {

    @Test
    public void testIsWarnEnabled() {
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIWarnLoggerTest.class);
        assertTrue("Logger WARN should be enabled.", logger.isWarnEnabled() == true);
    }

    @Test
    public void testWarn() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIWarnLoggerTest.class);
        final String message = "message";
        logger.warn(message);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.length() > message.length());
    }

    @Test
    public void testWarnWithThrowable() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIWarnLoggerTest.class);
        final String message = "message";
        logger.warn(message, new IOException());
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.length() > message.length());
    }

    @Test
    public void testWarnWithOneArgument() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIWarnLoggerTest.class);
        final String message = "message";
        final String format = message + " {}";
        final Integer object1 = 10;
        logger.warn(format, object1);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message + " " + object1.toString()) > 0);
    }

    @Test
    public void testWarnWithTwoArguments() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIWarnLoggerTest.class);
        final String message = "message";
        final String format = message + " {} {}";
        final Integer object1 = 1;
        final Integer object2 = 2;
        logger.warn(format, object1, object2);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.",
                buf.lastIndexOf(message + " " + object1.toString() + " " + object2.toString()) > 0);
    }

    @Test
    public void testWarnWithNArguments() {
        buf.setLength(0);

        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIWarnLoggerTest.class);
        final String message = "message";
        final String format = message + " {} {} {}";
        final Integer object1 = 1;
        final Integer object2 = 2;
        final Integer object3 = 3;
        logger.warn(format, object1, object2, object3);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(
                message + " " + object1.toString() + " " + object2.toString() + " " + object3.toString()) > 0);
    }

}
