package fr.gouv.vitamui.commons.api.logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.annotation.PostConstruct;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import fr.gouv.vitamui.commons.api.ApplicationTest;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;

/**
 * Abstract Test Class for VITAMUI.
 *
 *
 */
public abstract class AbstractVitamUITest {

    /**
     *
     */
    protected static IMocksControl iMocksControl = EasyMock.createControl();

    protected static ConfigurableApplicationContext context;

    protected static final Exception e = new Exception();

    protected static final PrintStream out = System.out; // NOSONAR since Logger test

    protected static final StringBuilder buf = new StringBuilder();

    @PostConstruct
    public static void setUpBeforeClass() {
        System.setProperty("spring.profiles.active", "ServerIdentityConfiguration-test");
        final SpringApplication springApplication = new SpringApplicationBuilder().sources(ApplicationTest.class)
                .build();
        context = springApplication.run();
        final VitamUILogger logger = VitamUILoggerFactory.getInstance(VitamUILoggerTest.class);
        logger.debug("Start Logback test", new Exception("test", new Exception("original")));
        try {
            System.setOut(new PrintStream(new OutputStream() {

                @Override
                public void write(final int b) {
                    buf.append((char) b);
                }
            }, true, "UTF-8"));
        }
        catch (final UnsupportedEncodingException e) {
            throw new InternalServerException(e.getMessage());
        }
        e.setStackTrace(new StackTraceElement[] { new StackTraceElement("vitamui1", "vitamui2", "vitamui3", 4) });
    }

    /**
     * @throws java.lang.Exception
     *             Exception.
     */
    @Before
    public void setUp() throws Exception {
        System.clearProperty("spring.profiles.active");
        iMocksControl.reset();
        buf.setLength(0);
    }

    @After
    public void tearDown() {
        SpringApplication.exit(context);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        System.setErr(out);
    }

}
