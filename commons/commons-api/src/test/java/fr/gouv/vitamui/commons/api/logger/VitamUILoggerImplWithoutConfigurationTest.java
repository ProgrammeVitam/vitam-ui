package fr.gouv.vitamui.commons.api.logger;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.powermock.api.easymock.PowerMock;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

/**
 * VitamUILogger Without ServerIdentity Test.
 *
 *
 */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ ServerIdentityConfiguration.class })
//@DirtiesContext
public class VitamUILoggerImplWithoutConfigurationTest {

    /**
    *
    */
    protected static IMocksControl iMocksControl = EasyMock.createControl();

    //    @Test(expected = InternalServerException.class)
    public void testMessagePrependWithoutServerIdentity() {
        PowerMock.mockStatic(ServerIdentityConfiguration.class);

        final Logger logger = (Logger) LoggerFactory.getLogger(VitamUILoggerImplWithoutConfigurationTest.class);
        final VitamUILogger vitamuiLogger = new VitamUILoggerImpl(logger);
        EasyMock.expect(ServerIdentityConfiguration.getInstance()).andReturn(null);

        PowerMock.replayAll();

        vitamuiLogger.debug("Message.");
        PowerMock.verifyAll();
    }

}
