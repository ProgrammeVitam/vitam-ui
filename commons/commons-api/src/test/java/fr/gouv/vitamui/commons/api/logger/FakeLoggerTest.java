package fr.gouv.vitamui.commons.api.logger;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;

public class FakeLoggerTest {

    protected static final PrintStream saveErr = System.err; // NOSONAR since Logger test

    protected static ByteArrayOutputStream err; // NOSONAR since Logger test

    protected static final PrintStream saveOut = System.out; // NOSONAR since Logger test

    protected static ByteArrayOutputStream out;

    protected static final StringBuilder buf = new StringBuilder();

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    /**
     * @throws java.lang.Exception
     *             Exception.
     */
    @Before
    public void setUp() throws Exception {
        buf.setLength(0);
        outputCapture.reset();
        err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));

        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        System.setErr(saveErr);
        System.setOut(saveOut);
    }

    @Test
    public void testIgnoreLogThrowable() {
        assertTrue("Log messages should be empty.", err.toString().length() == 0);
        FakeLogger.ignoreLog("Message", new IOException());
        assertThat("Log message should be written.", err.toString(), not(containsString("Message")));
    }

    @Test
    public void testIgnoreLogMessageAndThrowable() {
        assertTrue("Log messages should be empty.", err.toString().length() == 0);
        FakeLogger.ignoreLog(new IOException());
        assertThat("Log message should be written.", err.toString(), not(containsString("IOException")));
    }

    @Test
    public void testLogThrowable() {
        assertTrue("Log messages should be empty.", err.toString().length() == 0);
        FakeLogger.log("Message", new IOException());
        assertThat("Log message should be written.", err.toString(), containsString("Message"));
    }

    @Test
    public void testLogMessageAndThrowable() {
        assertTrue("Log messages should be empty.", err.toString().length() == 0);
        FakeLogger.log(new IOException());
        assertThat("Log message should be written.", err.toString(), containsString("IOException"));
    }

}
