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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.rest.converter.VitamUIErrorConverter;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * A Web client to check existence, read, create, update or delete  an object.
 * We can upload multipart data also.
 * with identifier.
 */
@ToString
public abstract class BaseWebClientVitamui<C extends HttpContext> extends BaseClientVitamui<C> {

    protected WebClient webClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWebClientVitamui.class);

    public BaseWebClientVitamui(final WebClient webClient, final String baseUrl) {
        super(baseUrl);
        this.webClient = webClient;
    }

    /**
     * Handle exceptions in response.
     *
     * @param response
     * @return
     */
    public static Mono<? extends Throwable> createResponseException(final ClientResponse response) {
        LOGGER.error("ERROR .................... {}", response.statusCode());

        return response
            .bodyToMono(String.class)
            .flatMap(serviceException -> {
                LOGGER.error("ERROR .................... {}", serviceException);

                VitamUIError error;
                // on HEAD requests, we don't have a body
                if (StringUtils.isBlank(serviceException)) {
                    error = new VitamUIError();
                    error.setStatus(response.statusCode().value());
                    error.setMessage("Unknown problem");
                    error.setError("apierror.unknown");
                } else {
                    // Added FAIL_ON_UNKNOWN_PROPERTIES:false to prevent error "UnrecognizedPropertyException: Unrecognized field"
                    // TODO check where the property "path" comes from
                    try {
                        error = new ObjectMapper()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .readValue(serviceException, VitamUIError.class);
                        error.setStatus(response.statusCode().value());
                    } catch (final IOException e) {
                        LOGGER.error("Error when retrieving exception {}", e);
                        error = new VitamUIError();
                        error.setStatus(response.statusCode().value());
                        error.setMessage(e.getMessage());
                        error.setError("apierror.unknown");
                    }
                }

                final VitamUIErrorConverter converter = new VitamUIErrorConverter();
                return Mono.error(converter.convert(error));
            });
    }
}
