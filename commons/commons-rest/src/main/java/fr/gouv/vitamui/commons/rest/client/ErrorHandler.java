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
package fr.gouv.vitamui.commons.rest.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.converter.VitamUIErrorConverter;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;

/**
 *
 * Error Handler for HTTP Response.
 *
 *
 */
public class ErrorHandler extends DefaultResponseErrorHandler {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ErrorHandler.class);

    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {
        final String body = IOUtils.toString(response.getBody(), StandardCharsets.UTF_8);
        LOGGER.debug("handle error, body response : " + body);
        final VitamUIError error;
        // on HEAD requests, we don't have a body
        if (StringUtils.isBlank(body)) {
            error = new VitamUIError();
            error.setStatus(response.getRawStatusCode());
            error.setMessage("Unknown problem");
            error.setError("apierror.unknown");
        } else {
            // Added FAIL_ON_UNKNOWN_PROPERTIES:false to prevent error "UnrecognizedPropertyException: Unrecognized field"
            // TODO check where the property "path" comes from
            error = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(body, VitamUIError.class);
        }
        final VitamUIErrorConverter converter = new VitamUIErrorConverter();
        throw converter.convert(error);
    }

}
