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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.bind.DatatypeConverter;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

/**
 * Custom initial flow action to retrieve pre-filled inputs.
 */
public class InitContextConfiguration implements ServletContextInitializer {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(InitContextConfiguration.class);

    @Value("${theme.vitam-logo:#{null}}")
    private String vitamLogoPath;

    @Value("${theme.vitamui-logo-large:#{null}}")
    private String vitamuiLargeLogoPath;

    @Value("${theme.vitamui-favicon:#{null}}")
    private String vitamuiFaviconPath;


    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {

        if (vitamLogoPath != null) {
            try {
                final Path logoFile = Paths.get(vitamLogoPath);
                final String logo = DatatypeConverter.printBase64Binary(Files.readAllBytes(logoFile));
                servletContext.setAttribute(Constants.VITAM_LOGO, logo);
            } catch (final IOException e) {
                LOGGER.warn("Can't find vitam logo");
                e.printStackTrace();
            }
        }
        if (vitamuiLargeLogoPath != null) {
            try {
                final Path logoFile = Paths.get(vitamuiLargeLogoPath);
                final String logo = DatatypeConverter.printBase64Binary(Files.readAllBytes(logoFile));
                servletContext.setAttribute(Constants.VITAM_UI_LARGE_LOGO, logo);
            } catch (final IOException e) {
                LOGGER.warn("Can't find vitam ui large logo");
                e.printStackTrace();
            }
        }

        if (vitamuiFaviconPath != null) {
            try {
                final Path faviconFile = Paths.get(vitamuiFaviconPath);
                final String favicon = DatatypeConverter.printBase64Binary(Files.readAllBytes(faviconFile));
                servletContext.setAttribute(Constants.VITAM_UI_FAVICON, favicon);
            } catch (final IOException e) {
                LOGGER.warn("Can't find vitam ui favicon");
                e.printStackTrace();
            }

        }
    }

}


