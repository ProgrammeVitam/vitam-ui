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

import java.io.Serializable;

import org.slf4j.Logger;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

public class VitamUILoggerImpl implements VitamUILogger, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -240123962335667505L;

    /**
     * Internal logger
     */
    private final transient Logger logger; // NOSONAR keep it non static

    private final String name;

    private static final char PACKAGE_SEPARATOR_CHAR = '.';

    VitamUILoggerImpl(final String name, final Logger parent) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("A logger must be provided.");
        }
        this.name = name;
        logger = parent;
    }

    VitamUILoggerImpl(final Logger logger) {
        this(logger.getName(), logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String msg) {
        logger.trace(getMessagePrepend(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object arg) {
        logger.trace(getMessagePrepend(format), arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object argA, final Object argB) {
        logger.trace(getMessagePrepend(format), argA, argB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object... arguments) {
        logger.trace(getMessagePrepend(format), arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String msg, final Throwable t) {
        logger.trace(getMessagePrepend(msg), t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String msg) {
        logger.debug(getMessagePrepend(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object arg) {
        logger.debug(getMessagePrepend(format), arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object argA, final Object argB) {
        logger.debug(getMessagePrepend(format), argA, argB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object... arguments) {
        logger.debug(getMessagePrepend(format), arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String msg, final Throwable t) {
        logger.debug(getMessagePrepend(msg), t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String msg) {
        logger.info(getMessagePrepend(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object arg) {
        logger.info(getMessagePrepend(format), arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object argA, final Object argB) {
        logger.info(getMessagePrepend(format), argA, argB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object... arguments) {
        logger.info(getMessagePrepend(format), arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String msg, final Throwable t) {
        logger.info(getMessagePrepend(msg), t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String msg) {
        logger.warn(getMessagePrepend(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object arg) {
        logger.warn(getMessagePrepend(format), arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object... arguments) {
        logger.warn(getMessagePrepend(format), arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object argA, final Object argB) {
        logger.warn(getMessagePrepend(format), argA, argB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String msg, final Throwable t) {
        logger.warn(getMessagePrepend(msg), t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String msg) {
        logger.error(getMessagePrepend(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object arg) {
        logger.error(getMessagePrepend(format), arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object argA, final Object argB) {
        logger.error(getMessagePrepend(format), argA, argB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object... arguments) {
        logger.error(getMessagePrepend(format), arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String msg, final Throwable t) {
        logger.error(getMessagePrepend(msg), t);
    }

    public static final String simpleClassName(final Object o) {
        return simpleClassName(o.getClass());
    }

    /**
     * @param clazz
     *            instance of a class
     * @return the simple Class Name
     */
    public static final String simpleClassName(final Class<?> clazz) {
        final String className = clazz.getName();
        final int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (lastDotIdx > -1) {
            return className.substring(lastDotIdx + 1);
        }
        return className;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return simpleClassName(this) + '(' + getName() + ')';
    }

    /**
     *
     * @return Message prepend using ServerIdentity
     */
    final String getMessagePrepend(final String otherMessage) {
        final ServerIdentityConfiguration serverIdentity = ServerIdentityConfiguration.getInstance();
        if (serverIdentity == null) {
            throw new InternalServerException("ServerIdentity is null.");
        }
        return serverIdentity.getLoggerMessagePrepend() + " " + otherMessage;
    }

}
