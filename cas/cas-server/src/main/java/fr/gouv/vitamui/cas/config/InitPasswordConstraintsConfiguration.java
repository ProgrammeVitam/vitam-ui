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
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.cas.config;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Objects;

/**
 * Custom context initializer for password complexity configuration.
 */
public class InitPasswordConstraintsConfiguration implements ServletContextInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitPasswordConstraintsConfiguration.class);

    @Autowired
    private PasswordConfiguration passwordConfiguration;

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        LOGGER.debug("PASSWORD_CONSTRAINTS = {}", passwordConfiguration.toString());
        if (
            Objects.isNull(passwordConfiguration) ||
            (Objects.isNull(passwordConfiguration.getConstraints()) ||
                (Objects.isNull(passwordConfiguration.getConstraints().getDefaults()) &&
                    Objects.isNull(passwordConfiguration.getConstraints().getCustoms()))) ||
            Objects.isNull(passwordConfiguration.getProfile())
        ) {
            LOGGER.debug("Configuration error, check your password server constraints configurations !");
            throw new ServletException(
                "Error starting CAS Server due to absence of password configurations. consider configuring at least default profile for password configurations in application.yml file !"
            );
        }

        validateAnssiPasswordConstraints(passwordConfiguration);

        if (passwordConfiguration != null && passwordConfiguration.getConstraints() != null) {
            switch (passwordConfiguration.getProfile().toLowerCase()) {
                case "anssi":
                    putAnssiConfigurations(servletContext, passwordConfiguration);
                    break;
                case "custom":
                    putCustomConfigurations(servletContext, passwordConfiguration);
                    break;
                default:
                    throw new ServletException(
                        "Error starting CAS Server due to absence of password configurations. consider configuring a valid profile, anssi or custom profile in your configuration !"
                    );
            }
            servletContext.setAttribute(Constants.MAX_OLD_PASSWORD, passwordConfiguration.getMaxOldPassword());
            servletContext.setAttribute(Constants.CHECK_OCCURRENCE, passwordConfiguration.isCheckOccurrence());
            servletContext.setAttribute(
                Constants.OCCURRENCE_CHAR_NUMBERS,
                passwordConfiguration.getOccurrencesCharsNumber()
            );
        }
    }

    private void validateAnssiPasswordConstraints(PasswordConfiguration passwordConfiguration) throws ServletException {
        if (passwordConfiguration.getProfile().equalsIgnoreCase("anssi")) {
            if (
                passwordConfiguration.getMaxOldPassword() < 12 ||
                passwordConfiguration.getLength() < 12 ||
                (!passwordConfiguration.isCheckOccurrence() && passwordConfiguration.getOccurrencesCharsNumber() < 3)
            ) {
                throw new ServletException(
                    "Some ANSSI security standards for password complexity are violated with the given configurations !"
                );
            }
        }
    }

    private void putCustomConfigurations(ServletContext servletContext, PasswordConfiguration passwordConfiguration) {
        if (passwordConfiguration.getConstraints().getCustoms() != null) {
            LOGGER.debug(
                "PASSWORD_CUSTOM_CONSTRAINTS = {}",
                passwordConfiguration.getConstraints().getCustoms().toString()
            );
            servletContext.setAttribute(Constants.PASSWORD_CUSTOM_CONSTRAINTS, passwordConfiguration.getConstraints());
        } else {
            servletContext.setAttribute(Constants.PASSWORD_CUSTOM_CONSTRAINTS, null);
        }
    }

    private void putAnssiConfigurations(ServletContext servletContext, PasswordConfiguration passwordConfiguration) {
        if (passwordConfiguration.getConstraints().getDefaults() != null) {
            LOGGER.debug(
                "PASSWORD_ANSSI_CONSTRAINTS = {}",
                passwordConfiguration.getConstraints().getDefaults().toString()
            );
            servletContext.setAttribute(Constants.PASSWORD_DEFAULT_CONSTRAINTS, passwordConfiguration.getConstraints());
        } else {
            servletContext.setAttribute(Constants.PASSWORD_DEFAULT_CONSTRAINTS, null);
        }
    }
}
