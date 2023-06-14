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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ControllerExceptionHandlerTest {

    @Mock
    private VitamClientException exception;

    @InjectMocks
    private ControllerExceptionHandler controllerExceptionHandler;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowsForNullContentVitamClientException() throws IOException {
        // Mocking the exception message
        when(exception.getMessage()).thenReturn(null);

        // Executing the method under test
        ResponseEntity<VitamUIError> response = controllerExceptionHandler.handleVitamClientException(exception);

        // Verifying the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("A VitamClientException not handled by controllers does not have message");
    }

    @Test
    void shouldThrowsForEmptyContentVitamClientException() throws IOException {
        // Mocking the exception message
        when(exception.getMessage()).thenReturn("");

        // Executing the method under test
        ResponseEntity<VitamUIError> response = controllerExceptionHandler.handleVitamClientException(exception);

        // Verifying the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("A VitamClientException not handled by controllers have empty message");
    }

    @Test
    void shouldThrowsForUnparsableVitamClientExceptionContent() throws IOException {
        // Mocking the exception message
        when(exception.getMessage()).thenReturn("{\"hello\": 400, \"world\": \"Bad request\"}");

        // Mocking ObjectMapper behavior
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(anyString(), eq(VitamErrorDto.class))).thenThrow(new JsonMappingException(""));

        // Injecting the mocked ObjectMapper
        controllerExceptionHandler.setObjectMapper(objectMapper);

        // Executing the method under test
        ResponseEntity<VitamUIError> response = controllerExceptionHandler.handleVitamClientException(exception);

        // Verifying the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Fail to read error message from VitamClientException");

        // Verifying that ObjectMapper is called
        verify(objectMapper, times(1)).readValue(anyString(), eq(VitamErrorDto.class));
    }

    @Test
    void shouldThrowsForObjectVitamClientExceptionContent() throws IOException {
        // Mocking the exception message
        when(exception.getMessage()).thenReturn("{}");

        // Mocking ObjectMapper behavior
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(anyString(), eq(VitamErrorDto.class))).thenReturn(null);

        // Injecting the mocked ObjectMapper
        controllerExceptionHandler.setObjectMapper(objectMapper);

        // Executing the method under test
        ResponseEntity<VitamUIError> response = controllerExceptionHandler.handleVitamClientException(exception);

        // Verifying the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("The message read from VitamClientException is null");

        // Verifying that ObjectMapper is called
        verify(objectMapper, times(1)).readValue(anyString(), eq(VitamErrorDto.class));
    }

    @Test
    void shouldHandleVitamClientException() throws IOException {
        // Mocking the exception message
        when(exception.getMessage()).thenReturn("{\"httpCode\": 400, \"message\": \"Bad request\"}");

        // Mocking ObjectMapper behavior
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(anyString(), eq(VitamErrorDto.class))).thenReturn(new VitamErrorDto(400, null, null, null, "Bad request", null));

        // Injecting the mocked ObjectMapper
        controllerExceptionHandler.setObjectMapper(objectMapper);

        // Executing the method under test
        ResponseEntity<VitamUIError> response = controllerExceptionHandler.handleVitamClientException(exception);

        // Verifying the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Bad request");

        // Verifying that ObjectMapper is called
        verify(objectMapper, times(1)).readValue(anyString(), eq(VitamErrorDto.class));
    }

    @Test
    void shouldHandleTrackTotalHitsVitamClientException() throws IOException {
        // Mocking the exception message
        when(exception.getMessage()).thenReturn("{\"httpCode\": 400, \"description\": \"track_total_hits is not authorized\"}");

        // Mocking ObjectMapper behavior
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(anyString(), eq(VitamErrorDto.class))).thenReturn(new VitamErrorDto(400, null, null, null, null, "track_total_hits is not authorized"));

        // Injecting the mocked ObjectMapper
        controllerExceptionHandler.setObjectMapper(objectMapper);

        // Executing the method under test
        ResponseEntity<VitamUIError> response = controllerExceptionHandler.handleVitamClientException(exception);

        // Verifying the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.EXPECTATION_FAILED);
        assertThat(response.getBody().getStatus()).isEqualTo(417);
        assertThat(response.getBody().getError()).contains("track_total_hits is not authorized");

        // Verifying that ObjectMapper is called
        verify(objectMapper, times(1)).readValue(anyString(), eq(VitamErrorDto.class));
    }
}

