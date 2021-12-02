package fr.gouv.vitamui.commons.api.logger;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;

import org.slf4j.Logger;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

/**
 * VitamUILogger Without ServerIdentity Test.
 *
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
@DirtiesContext
public class VitamUILoggerImplWithoutConfigurationTest {

    /**
    *
    */
    protected static IMocksControl iMocksControl = EasyMock.createControl();

    @Test(expected = IllegalAccessError.class)
    public void testMessagePrependWithoutServerIdentity() {
        PowerMock.mockStatic(ServerIdentityConfiguration.class);

        final Logger logger = LoggerFactory.getLogger(VitamUILoggerImplWithoutConfigurationTest.class);
        final VitamUILogger vitamuiLogger = new VitamUILoggerImpl(logger);
        EasyMock.expect(ServerIdentityConfiguration.getInstance()).andReturn(null);

        PowerMock.replayAll();

        vitamuiLogger.debug("Message.");
        PowerMock.verifyAll();
    }

}
