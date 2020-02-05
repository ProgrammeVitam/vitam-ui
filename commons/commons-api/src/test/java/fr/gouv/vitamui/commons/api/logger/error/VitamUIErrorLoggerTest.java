package fr.gouv.vitamui.commons.api.logger.error;

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
public class VitamUIErrorLoggerTest extends AbstractVitamUITest {

    @Test
    public void testIsErrorEnabled() {
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIErrorLoggerTest.class);
        assertTrue("Logger ERROR should be enabled.", logger.isErrorEnabled() == true);
    }

    @Test
    public void testError() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIErrorLoggerTest.class);
        final String message = "message";
        logger.error(message);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.length() > message.length());
    }

    @Test
    public void testErrorWithThrowable() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIErrorLoggerTest.class);
        final String message = "message";
        logger.error(message, new IOException());
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.length() > message.length());
    }

    @Test
    public void testErrorWithOneArgument() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIErrorLoggerTest.class);
        final String message = "message";
        final String format = message + " {}";
        final Integer object1 = 10;
        logger.error(format, object1);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message + " " + object1.toString()) > 0);
    }

    @Test
    public void testErrorWithTwoArguments() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIErrorLoggerTest.class);
        final String message = "message";
        final String format = message + " {} {}";
        final Integer object1 = 1;
        final Integer object2 = 2;
        logger.error(format, object1, object2);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.",
                buf.lastIndexOf(message + " " + object1.toString() + " " + object2.toString()) > 0);
    }

    @Test
    public void testErrorWithNArguments() {
        buf.setLength(0);
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUIErrorLoggerTest.class);
        final String message = "message";
        final String format = message + " {} {} {}";
        final Integer object1 = 1;
        final Integer object2 = 2;
        final Integer object3 = 3;
        logger.error(format, object1, object2, object3);
        assertTrue("Log message should be written.", buf.length() > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(message) > 0);
        assertTrue("Log message should be written.", buf.lastIndexOf(
                message + " " + object1.toString() + " " + object2.toString() + " " + object3.toString()) > 0);
    }

}
