/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.api.logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import ch.qos.logback.classic.Logger;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

/**
 * The <code>VitamUILoggerFactory</code> is a utility class producing Loggers for VITAMUI.
 * Please note that all methods in <code>VitamUILoggerFactory</code> are static.
 *
 */
public final class VitamUILoggerFactory {

    private static boolean initialized = false;

    @SuppressWarnings("unused")
    private static ServerIdentityConfiguration serverIdentity = null;

    private VitamUILoggerFactory() {
        initializeLogger();
    }

    /**
     * Creates a new logger instance with the name of the specified class.
     *
     * @param clazz
     *            specified class
     * @return the logger instance
     */
    public static VitamUILogger getInstance(final Class<?> clazz) {
        if (!VitamUILoggerFactory.initialized) {
            new VitamUILoggerFactory();
        }
        final Logger logger = (Logger) LoggerFactory.getLogger(clazz.getName()); // NOSONAR
        return new VitamUILoggerImpl(logger);
    }

    private static void initializeLogger() {
        // SFL4J writes it error messages to System.err. Capture them so that
        // the user does not see such a message on
        // the console during automatic detection.
        final StringBuilder buf = new StringBuilder();
        final PrintStream err = System.err; // NOSONAR
        try {
            System.setErr(new PrintStream(new OutputStream() {

                @Override
                public void write(final int b) {
                    buf.append((char) b);
                }
            }, true, "US-ASCII"));
        }
        catch (final UnsupportedEncodingException e) {
            throw new InternalServerException(e.getMessage());
        }

        try {
            if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
                throw new NoClassDefFoundError(buf.toString());
            } else {
                err.print(buf.toString());
                err.flush();
            }
        }
        finally {
            System.setErr(err);
        }
        VitamUILoggerFactory.initialized = true;
    }

}
