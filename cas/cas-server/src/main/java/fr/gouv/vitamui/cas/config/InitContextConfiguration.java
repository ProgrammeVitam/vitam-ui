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
import jakarta.servlet.ServletContext;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Custom context initializer to pre-fill logo and favicon.
 */
@Slf4j
@RequiredArgsConstructor
public class InitContextConfiguration implements ServletContextInitializer {

    private final String vitamuiLogoLargePath;

    private final String vitamuiFaviconPath;

    @Override
    public void onStartup(final ServletContext servletContext) {
        putBase64Image(servletContext, vitamuiLogoLargePath, Constants.VITAMUI_LOGO_LARGE, true, "large logo");
        putBase64Image(servletContext, vitamuiFaviconPath, Constants.VITAM_UI_FAVICON, false, "favicon");
    }

    // A missing image is a warning, never a boot failure (a missing favicon used to abort the start-up).
    private static void putBase64Image(
        final ServletContext servletContext,
        final String path,
        final String attribute,
        final boolean asDataUri,
        final String label
    ) {
        if (path == null) {
            return;
        }
        try {
            String base64 = DatatypeConverter.printBase64Binary(Files.readAllBytes(Paths.get(path)));
            if (asDataUri) {
                base64 = (path.endsWith(".svg") ? "data:image/svg+xml;base64," : "data:image/png;base64,") + base64;
            }
            servletContext.setAttribute(attribute, base64);
        } catch (final IOException e) {
            LOGGER.warn("Can't find vitam ui {}", label, e);
        }
    }
}
