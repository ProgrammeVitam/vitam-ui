package fr.gouv.vitamui.commons.rest;

import fr.gouv.vitamui.commons.rest.util.AbstractServerIdentityBuilder;
import org.junit.AfterClass;

import javax.annotation.PostConstruct;
import javax.management.RuntimeErrorException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Test Rest Exception Handler.
 *
 *
 */
public abstract class AbstractRestTest extends AbstractServerIdentityBuilder {

    protected static final PrintStream out = System.out; // NOSONAR since Logger test

    protected static final StringBuilder buf = new StringBuilder();

    /**
     * Catch Errors Logs and Set DateTIme.
     */
    @PostConstruct
    public static void setUpBeforeClass() {
        try {
            System.setOut(new PrintStream(new OutputStream() {

                @Override
                public void write(final int b) {
                    buf.append((char) b);
                }
            }, true, "UTF-8"));
        }
        catch (final UnsupportedEncodingException e) {
            throw new RuntimeErrorException(new Error(e));
        }
    }

    /**
     * Clean Up.
     */
    @AfterClass
    public static void tearDownAfterClass() {
        System.setErr(out);
    }

}
