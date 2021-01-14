/**
 * OWASP Enterprise Security API (ESAPI)
 *
 * This file is part of the Open Web Application Security Project (OWASP) Enterprise Security API (ESAPI) project. For
 * details, please see <a href="http://www.owasp.org/index.php/ESAPI">http://www.owasp.org/index.php/ESAPI</a>.
 *
 * Copyright (c) 2007 - The OWASP Foundation
 *
 * The ESAPI is published by OWASP under the BSD license. You should read and accept the LICENSE before you use, modify,
 * and/or redistribute this software.
 *
 * @author Jeff Williams <a href="http://www.aspectsecurity.com">Aspect Security</a>
 * @created 2007
 */
package org.owasp.esapi.reference.validation;

import fr.gouv.vitamui.commons.utils.ResourcesUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.Logger;
import org.owasp.esapi.StringUtilities;
import org.owasp.esapi.errors.ConfigurationException;
import org.owasp.esapi.errors.ValidationException;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * A validator performs syntax and possibly semantic validation of a single piece of data from an untrusted source.
 *
 * @author Jeff Williams (jeff.williams .at. aspectsecurity.com) <a href="http://www.aspectsecurity.com">Aspect
 *         Security</a>
 * @since June 1, 2007
 * @see org.owasp.esapi.Validator
 */
public class HTMLValidationRule extends StringValidationRule {

    /** OWASP AntiSamy markup verification policy */
    private static Policy antiSamyPolicy = null;
    private static final Logger LOGGER = ESAPI.getLogger("HTMLValidationRule");
    private static final String ANTISAMYPOLICY_FILENAME = "antisamy-esapi.xml";

    static {
        InputStream resourceStream = null;
        try {
            resourceStream = ESAPI.securityConfiguration().getResourceStream(ANTISAMYPOLICY_FILENAME);
        } catch (final IOException e) {
            LOGGER.info(null, "Loading " + ANTISAMYPOLICY_FILENAME + " from classpaths", e);

            final ClassLoader[] loaders = new ClassLoader[] {
                Thread.currentThread().getContextClassLoader(),
                ClassLoader.getSystemClassLoader(),
                HTMLValidationRule.class.getClassLoader()
            };
            for (final ClassLoader loader : loaders) {
                resourceStream = loader.getResourceAsStream(ANTISAMYPOLICY_FILENAME);
                if (resourceStream != null) {
                    LOGGER.info(null, "Successfully loaded antisamy policy from classpath");
                    break;
                }
            }
            try {
                resourceStream = ResourcesUtils.getResourceAsStream("esapi/" + ANTISAMYPOLICY_FILENAME);
                if (resourceStream != null) {
                    LOGGER.info(null, "Successfully loaded antisamy policy from VitamUI classpath");
                }
            } catch (final FileNotFoundException e1) {
                LOGGER.info(null, "Cannot loaded antisamy policy from VitamUI classpath", e1);
            }
        }
        if (resourceStream != null) {
            try {
                antiSamyPolicy = Policy.getInstance(resourceStream);
            } catch (final PolicyException e) {
                throw new ConfigurationException("Couldn't parse antisamy policy", e);
            }
        } else {
            throw new ConfigurationException("Couldn't find " + ANTISAMYPOLICY_FILENAME);
        }
    }

    /**
     * @param typeName
     */
    public HTMLValidationRule(String typeName) {
        super(typeName);
    }

    /**
     * @param typeName
     * @param encoder
     */
    public HTMLValidationRule(String typeName, Encoder encoder) {
        super(typeName, encoder);
    }

    /**
     * @param typeName
     * @param encoder
     * @param whitelistPattern
     */
    public HTMLValidationRule(String typeName, Encoder encoder, String whitelistPattern) {
        super(typeName, encoder, whitelistPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValid(String context, String input) throws ValidationException {
        return invokeAntiSamy(context, input);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sanitize(String context, String input) {
        String safe = "";
        try {
            safe = invokeAntiSamy(context, input);
        } catch (final ValidationException e) {
            // just return safe
        }
        return safe;
    }

    private String invokeAntiSamy(String context, String input) throws ValidationException {
        // CHECKME should this allow empty Strings? " " us IsBlank instead?
        if (StringUtilities.isEmpty(input)) {
            if (allowNull) {
                return null;
            }
            throw new ValidationException(context + " is required",
                "AntiSamy validation error: context=" + context + ", input=" + input, context);
        }

        final String canonical = super.getValid(context, input);

        try {
            final AntiSamy as = new AntiSamy();
            final CleanResults test = as.scan(canonical, antiSamyPolicy);

            final List<String> errors = test.getErrorMessages();
            if (!errors.isEmpty()) {
                LOGGER.info(Logger.SECURITY_FAILURE, "Cleaned up invalid HTML input: " + errors);
            }

            return test.getCleanHTML().trim();

        } catch (final ScanException e) {
            throw new ValidationException(context + ": Invalid HTML input",
                "Invalid HTML input: context=" + context + " error=" + e.getMessage(), e, context);
        } catch (final PolicyException e) {
            throw new ValidationException(context + ": Invalid HTML input",
                "Invalid HTML input does not follow rules in antisamy-esapi.xml: context=" + context + " error=" +
                    e.getMessage(),
                e, context);
        }
    }
}
