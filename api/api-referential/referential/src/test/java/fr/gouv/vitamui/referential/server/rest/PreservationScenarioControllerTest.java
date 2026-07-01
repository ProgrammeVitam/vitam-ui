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

package fr.gouv.vitamui.referential.server.rest;

import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.dtos.OperationIdDto;
import fr.gouv.vitamui.referential.common.dto.preservation.scenario.ActionType;
import fr.gouv.vitamui.referential.common.dto.preservation.scenario.PreservationScenario;
import fr.gouv.vitamui.referential.server.service.preservation.PreservationScenarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreservationScenarioControllerTest {

    @Mock
    private PreservationScenarioService preservationScenarioService;

    private PreservationScenarioController preservationScenarioController;

    @BeforeEach
    void setUp() {
        preservationScenarioController = new PreservationScenarioController(preservationScenarioService);
    }

    // ------------------------------------------------------------------ getPreservationScenarios

    @Test
    void getPreservationScenarios_should_return_all_scenarios_from_service()
        throws VitamClientException, InvalidCreateOperationException {
        // Given
        PreservationScenario scenario = buildScenario("id-1");
        when(preservationScenarioService.getAll()).thenReturn(List.of(scenario));

        // When
        List<PreservationScenario> result = preservationScenarioController.getPreservationScenarios();

        // Then
        assertThat(result).containsExactly(scenario);
        verify(preservationScenarioService).getAll();
    }

    @Test
    void getPreservationScenarios_should_return_empty_list_when_no_scenarios()
        throws VitamClientException, InvalidCreateOperationException {
        // Given
        when(preservationScenarioService.getAll()).thenReturn(List.of());

        // When
        List<PreservationScenario> result = preservationScenarioController.getPreservationScenarios();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getPreservationScenarios_should_propagate_VitamClientException()
        throws VitamClientException, InvalidCreateOperationException {
        // Given
        when(preservationScenarioService.getAll()).thenThrow(new VitamClientException("Vitam error"));

        // When / Then
        assertThatCode(() -> preservationScenarioController.getPreservationScenarios()).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    void getPreservationScenarios_should_propagate_InvalidCreateOperationException()
        throws VitamClientException, InvalidCreateOperationException {
        // Given
        when(preservationScenarioService.getAll()).thenThrow(new InvalidCreateOperationException("Query error"));

        // When / Then
        assertThatCode(() -> preservationScenarioController.getPreservationScenarios()).isInstanceOf(
            InvalidCreateOperationException.class
        );
    }

    // ------------------------------------------------------------------ putPreservationScenarios

    @Test
    void putPreservationScenarios_should_delegate_to_service()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        List<PreservationScenario> scenarios = List.of(buildScenario("id-1"), buildScenario("id-2"));
        when(preservationScenarioService.put(scenarios)).thenReturn(ResponseEntity.ok(new OperationIdDto("42")));

        // When / Then
        assertThatCode(
            () -> preservationScenarioController.putPreservationScenarios(scenarios)
        ).doesNotThrowAnyException();
        verify(preservationScenarioService).put(scenarios);
    }

    @Test
    void putPreservationScenarios_should_propagate_VitamClientException()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        List<PreservationScenario> scenarios = List.of(buildScenario("id-1"));
        doThrow(new VitamClientException("Vitam error")).when(preservationScenarioService).put(scenarios);

        // When / Then
        assertThatCode(() -> preservationScenarioController.putPreservationScenarios(scenarios)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    void putPreservationScenarios_should_propagate_IOException()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        List<PreservationScenario> scenarios = List.of(buildScenario("id-1"));
        doThrow(new IOException("IO error")).when(preservationScenarioService).put(scenarios);

        // When / Then
        assertThatCode(() -> preservationScenarioController.putPreservationScenarios(scenarios)).isInstanceOf(
            IOException.class
        );
    }

    // ------------------------------------------------------------------ updatePreservationScenario

    @Test
    void updatePreservationScenario_should_delegate_to_service()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        // Given
        PreservationScenario scenario = buildScenario("id-1");
        doNothing().when(preservationScenarioService).update(scenario);

        // When / Then
        assertThatCode(
            () -> preservationScenarioController.updatePreservationScenario(scenario)
        ).doesNotThrowAnyException();
        verify(preservationScenarioService).update(scenario);
    }

    @Test
    void updatePreservationScenario_should_propagate_VitamClientException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        // Given
        PreservationScenario scenario = buildScenario("id-1");
        doThrow(new VitamClientException("Vitam error")).when(preservationScenarioService).update(scenario);

        // When / Then
        assertThatCode(() -> preservationScenarioController.updatePreservationScenario(scenario)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    void updatePreservationScenario_should_propagate_InvalidCreateOperationException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        // Given
        PreservationScenario scenario = buildScenario("id-1");
        doThrow(new InvalidCreateOperationException("Query error")).when(preservationScenarioService).update(scenario);

        // When / Then
        assertThatCode(() -> preservationScenarioController.updatePreservationScenario(scenario)).isInstanceOf(
            InvalidCreateOperationException.class
        );
    }

    // ------------------------------------------------------------------ deletePreservationScenario

    @Test
    void deletePreservationScenario_should_delegate_to_service()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        // Given
        PreservationScenario scenario = buildScenario("id-1");
        doNothing().when(preservationScenarioService).delete(scenario);

        // When / Then
        assertThatCode(
            () -> preservationScenarioController.deletePreservationScenario(scenario)
        ).doesNotThrowAnyException();
        verify(preservationScenarioService).delete(scenario);
    }

    @Test
    void deletePreservationScenario_should_propagate_VitamClientException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        // Given
        PreservationScenario scenario = buildScenario("id-1");
        doThrow(new VitamClientException("Vitam error")).when(preservationScenarioService).delete(scenario);

        // When / Then
        assertThatCode(() -> preservationScenarioController.deletePreservationScenario(scenario)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    void deletePreservationScenario_should_propagate_IOException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        // Given
        PreservationScenario scenario = buildScenario("id-1");
        doThrow(new IOException("IO error")).when(preservationScenarioService).delete(scenario);

        // When / Then
        assertThatCode(() -> preservationScenarioController.deletePreservationScenario(scenario)).isInstanceOf(
            IOException.class
        );
    }

    @Test
    void deletePreservationScenario_should_propagate_InvalidCreateOperationException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        // Given
        PreservationScenario scenario = buildScenario("id-1");
        doThrow(new InvalidCreateOperationException("Query error")).when(preservationScenarioService).delete(scenario);

        // When / Then
        assertThatCode(() -> preservationScenarioController.deletePreservationScenario(scenario)).isInstanceOf(
            InvalidCreateOperationException.class
        );
    }

    // ------------------------------------------------------------------ helpers

    private PreservationScenario buildScenario(String id) {
        return new PreservationScenario(
            id,
            0,
            1,
            "IDENTIFIER_" + id,
            "Scenario " + id,
            null,
            null,
            null,
            List.of(ActionType.GENERATE),
            null,
            null,
            null
        );
    }
}
