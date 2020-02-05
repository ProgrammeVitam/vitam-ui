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

/**
 * <em>Internal-use-only</em> logger used by VitamUI. <strong>DO NOT</strong>
 * access this class outside of VitamUI. Inspired from org.slf4j.Logger.
 *
 */
public interface VitamUILogger {

    /**
     * Return the name of this <code>Logger</code> instance.
     *
     * @return name of this logger instance
     */
    public String getName();

    /**
     * Is the logger instance enabled for the TRACE level?
     *
     * @return True if this Logger is enabled for the TRACE level, false
     *         otherwise.
     */
    public boolean isTraceEnabled();

    /**
     * Log a message at the TRACE level.
     *
     * @param msg
     *            the message string to be logged
     */
    public void trace(String msg);

    /**
     * Log a message at the TRACE level according to the specified format and
     * argument.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the TRACE level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg
     *            the argument
     */
    public void trace(String format, Object arg);

    /**
     * Log a message at the TRACE level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the TRACE level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg1
     *            the first argument
     * @param arg2
     *            the second argument
 *
     */
    public void trace(String format, Object arg1, Object arg2);

    /**
     * Log a message at the TRACE level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger is
     * disabled for the TRACE level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before
     * invoking the method, even if this logger is disabled for TRACE. The
     * variants taking {@link #trace(String, Object) one} and
     * {@link #trace(String, Object, Object) two} arguments exist solely in
     * order to avoid this hidden cost.
     * </p>
     *
     * @param format
     *            the format string
     * @param arguments
     *            a list of 3 or more arguments
     */
    public void trace(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the TRACE level with an accompanying
     * message.
     *
     * @param msg
     *            the message accompanying the exception
     * @param t
     *            the exception (throwable) to log
     */
    public void trace(String msg, Throwable t);

    /**
     * Is the logger instance enabled for the DEBUG level?
     *
     * @return True if this Logger is enabled for the DEBUG level, false
     *         otherwise.
     */
    public boolean isDebugEnabled();

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg
     *            the message string to be logged
     */
    public void debug(String msg);

    /**
     * Log a message at the DEBUG level according to the specified format and
     * argument.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the DEBUG level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg
     *            the argument
     */
    public void debug(String format, Object arg);

    /**
     * Log a message at the DEBUG level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the DEBUG level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg1
     *            the first argument
     * @param arg2
     *            the second argument
     */
    public void debug(String format, Object arg1, Object arg2);

    /**
     * Log a message at the DEBUG level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger is
     * disabled for the DEBUG level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before
     * invoking the method, even if this logger is disabled for DEBUG. The
     * variants taking {@link #debug(String, Object) one} and
     * {@link #debug(String, Object, Object) two} arguments exist solely in
     * order to avoid this hidden cost.
     * </p>
     *
     * @param format
     *            the format string
     * @param arguments
     *            a list of 3 or more arguments
     */
    public void debug(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the DEBUG level with an accompanying
     * message.
     *
     * @param msg
     *            the message accompanying the exception
     * @param t
     *            the exception (throwable) to log
     */
    public void debug(String msg, Throwable t);

    /**
     * Is the logger instance enabled for the INFO level?
     *
     * @return True if this Logger is enabled for the INFO level, false
     *         otherwise.
     */
    public boolean isInfoEnabled();

    /**
     * Log a message at the INFO level.
     *
     * @param msg
     *            the message string to be logged
     */
    public void info(String msg);

    /**
     * Log a message at the INFO level according to the specified format and
     * argument.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the INFO level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg
     *            the argument
     */
    public void info(String format, Object arg);

    /**
     * Log a message at the INFO level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the INFO level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg1
     *            the first argument
     * @param arg2
     *            the second argument
     */
    public void info(String format, Object arg1, Object arg2);

    /**
     * Log a message at the INFO level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger is
     * disabled for the INFO level. However, this variant incurs the hidden (and
     * relatively small) cost of creating an <code>Object[]</code> before
     * invoking the method, even if this logger is disabled for INFO. The
     * variants taking {@link #info(String, Object) one} and
     * {@link #info(String, Object, Object) two} arguments exist solely in order
     * to avoid this hidden cost.
     * </p>
     *
     * @param format
     *            the format string
     * @param arguments
     *            a list of 3 or more arguments
     */
    public void info(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the INFO level with an accompanying
     * message.
     *
     * @param msg
     *            the message accompanying the exception
     * @param t
     *            the exception (throwable) to log
     */
    public void info(String msg, Throwable t);

    /**
     * Is the logger instance enabled for the WARN level?
     *
     * @return True if this Logger is enabled for the WARN level, false
     *         otherwise.
     */
    public boolean isWarnEnabled();

    /**
     * Log a message at the WARN level.
     *
     * @param msg
     *            the message string to be logged
     */
    public void warn(String msg);

    /**
     * Log a message at the WARN level according to the specified format and
     * argument.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the WARN level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg
     *            the argument
     */
    public void warn(String format, Object arg);

    /**
     * Log a message at the WARN level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger is
     * disabled for the WARN level. However, this variant incurs the hidden (and
     * relatively small) cost of creating an <code>Object[]</code> before
     * invoking the method, even if this logger is disabled for WARN. The
     * variants taking {@link #warn(String, Object) one} and
     * {@link #warn(String, Object, Object) two} arguments exist solely in order
     * to avoid this hidden cost.
     * </p>
     *
     * @param format
     *            the format string
     * @param arguments
     *            a list of 3 or more arguments
     */
    public void warn(String format, Object... arguments);

    /**
     * Log a message at the WARN level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the WARN level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg1
     *            the first argument
     * @param arg2
     *            the second argument
     */
    public void warn(String format, Object arg1, Object arg2);

    /**
     * Log an exception (throwable) at the WARN level with an accompanying
     * message.
     *
     * @param msg
     *            the message accompanying the exception
     * @param t
     *            the exception (throwable) to log
     */
    public void warn(String msg, Throwable t);

    /**
     * Is the logger instance enabled for the ERROR level?
     *
     * @return True if this Logger is enabled for the ERROR level, false
     *         otherwise.
     */
    public boolean isErrorEnabled();

    /**
     * Log a message at the ERROR level.
     *
     * @param msg
     *            the message string to be logged
     */
    public void error(String msg);

    /**
     * Log a message at the ERROR level according to the specified format and
     * argument.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the ERROR level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg
     *            the argument
     */
    public void error(String format, Object arg);

    /**
     * Log a message at the ERROR level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the ERROR level.
     * </p>
     *
     * @param format
     *            the format string
     * @param arg1
     *            the first argument
     * @param arg2
     *            the second argument
     */
    public void error(String format, Object arg1, Object arg2);

    /**
     * Log a message at the ERROR level according to the specified format and
     * arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger is
     * disabled for the ERROR level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before
     * invoking the method, even if this logger is disabled for ERROR. The
     * variants taking {@link #error(String, Object) one} and
     * {@link #error(String, Object, Object) two} arguments exist solely in
     * order to avoid this hidden cost.
     * </p>
     *
     * @param format
     *            the format string
     * @param arguments
     *            a list of 3 or more arguments
     */
    public void error(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the ERROR level with an accompanying
     * message.
     *
     * @param msg
     *            the message accompanying the exception
     * @param t
     *            the exception (throwable) to log
     */
    public void error(String msg, Throwable t);

}
