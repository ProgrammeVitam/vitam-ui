/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.collect.internal.server.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ControllerExceptionHandler.class);
    private ObjectMapper objectMapper;

    @Autowired
    public ControllerExceptionHandler() {
        super();
    }

    @ExceptionHandler(value = VitamClientException.class)
    ResponseEntity<VitamUIError> handleVitamClientException(VitamClientException exception) {
        LOGGER.warn("A VitamClientException not handled have been captured");

        if (exception.getMessage() == null) {
            final String message = "A VitamClientException not handled by controllers does not have message";

            return ResponseEntity.internalServerError().body(convertToVitamUIError(createDefaultErrorMessage(message)));
        }

        if (exception.getMessage().equals("")) {
            final String message = "A VitamClientException not handled by controllers have empty message";

            return ResponseEntity.internalServerError().body(convertToVitamUIError(createDefaultErrorMessage(message)));
        }

        final VitamErrorDto vitamErrorDto;

        try {
            vitamErrorDto = objectMapper.readValue(exception.getMessage(), VitamErrorDto.class);
        } catch (IOException e) {
            final String message = "Fail to read error message from VitamClientException";

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(convertToVitamUIError(createDefaultErrorMessage(message)));
        }

        if (vitamErrorDto == null) {
            final String message = "The message read from VitamClientException is null";

            return ResponseEntity.internalServerError().body(convertToVitamUIError(createDefaultErrorMessage(message)));
        }

        final VitamUIError error = convertToVitamUIError(vitamErrorDto);
        final VitamUIError refinedError = refine(error);

        LOGGER.debug("Refined error: {}", refinedError);

        return ResponseEntity.status(HttpStatus.valueOf(refinedError.getStatus())).body(refinedError);
    }

    private VitamErrorDto createDefaultErrorMessage(final String message) {
        final VitamErrorDto vitamErrorDto = new VitamErrorDto();

        vitamErrorDto.setHttpCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        vitamErrorDto.setMessage(message);

        return vitamErrorDto;
    }

    private VitamUIError convertToVitamUIError(final VitamErrorDto vitamErrorDto) {
        final VitamUIError vitamUIError = new VitamUIError();

        vitamUIError.setStatus(vitamErrorDto.getHttpCode());
        vitamUIError.setMessage(vitamErrorDto.getMessage());
        vitamUIError.setTimestamp(LocalDateUtil.now().toString());
        vitamUIError.setException(vitamErrorDto.toString());
        vitamUIError.setError(vitamErrorDto.toString());
        vitamUIError.setArgs(null);

        return vitamUIError;
    }

    private VitamUIError refine(final VitamUIError error) {
        if (isAboutTrackTotalHits(error)) {
            error.setStatus(EXPECTATION_FAILED.value());

            return error;
        }

        return error;
    }

    private boolean isAboutTrackTotalHits(final VitamUIError vitamUIError) {
        final String expectation = "track_total_hits is not authorized";

        return vitamUIError.getError() != null && vitamUIError.getError().contains(expectation);
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
