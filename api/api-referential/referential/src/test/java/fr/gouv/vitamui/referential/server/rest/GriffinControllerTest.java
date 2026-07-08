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
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.dtos.OperationIdDto;
import fr.gouv.vitamui.referential.common.dto.preservation.griffin.Griffin;
import fr.gouv.vitamui.referential.server.service.preservation.GriffinService;
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
class GriffinControllerTest {

    @Mock
    private GriffinService griffinService;

    private GriffinController griffinController;

    @BeforeEach
    void setUp() {
        griffinController = new GriffinController(griffinService);
    }

    // ------------------------------------------------------------------ getGriffins

    @Test
    void getGriffins_should_return_all_griffins_from_service() throws VitamClientException {
        // Given
        Griffin griffin = buildGriffin("id-1");
        when(griffinService.getAll()).thenReturn(List.of(griffin));

        // When
        List<Griffin> result = griffinController.getGriffins();

        // Then
        assertThat(result).containsExactly(griffin);
        verify(griffinService).getAll();
    }

    @Test
    void getGriffins_should_return_empty_list_when_no_griffins() throws VitamClientException {
        // Given
        when(griffinService.getAll()).thenReturn(List.of());

        // When
        List<Griffin> result = griffinController.getGriffins();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getGriffins_should_propagate_VitamClientException() throws VitamClientException {
        // Given
        when(griffinService.getAll()).thenThrow(new VitamClientException("Vitam error"));

        // When / Then
        assertThatCode(() -> griffinController.getGriffins()).isInstanceOf(VitamClientException.class);
    }

    // ------------------------------------------------------------------ putGriffins

    @Test
    void putGriffins_should_delegate_to_service()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        List<Griffin> griffins = List.of(buildGriffin("id-1"), buildGriffin("id-2"));
        when(griffinService.put(griffins)).thenReturn(ResponseEntity.ok(new OperationIdDto("42")));

        // When / Then
        assertThatCode(() -> griffinController.putGriffins(griffins)).doesNotThrowAnyException();
        verify(griffinService).put(griffins);
    }

    @Test
    void putGriffins_should_propagate_VitamClientException()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        List<Griffin> griffins = List.of(buildGriffin("id-1"));
        doThrow(new VitamClientException("Vitam error")).when(griffinService).put(griffins);

        // When / Then
        assertThatCode(() -> griffinController.putGriffins(griffins)).isInstanceOf(VitamClientException.class);
    }

    @Test
    void putGriffins_should_propagate_IOException()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        List<Griffin> griffins = List.of(buildGriffin("id-1"));
        doThrow(new IOException("IO error")).when(griffinService).put(griffins);

        // When / Then
        assertThatCode(() -> griffinController.putGriffins(griffins)).isInstanceOf(IOException.class);
    }

    // ------------------------------------------------------------------ updateGriffin

    @Test
    void updateGriffin_should_delegate_to_service()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        Griffin griffin = buildGriffin("id-1");
        doNothing().when(griffinService).update(griffin);

        // When / Then
        assertThatCode(() -> griffinController.updateGriffin(griffin)).doesNotThrowAnyException();
        verify(griffinService).update(griffin);
    }

    @Test
    void updateGriffin_should_propagate_VitamClientException()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        Griffin griffin = buildGriffin("id-1");
        doThrow(new VitamClientException("Vitam error")).when(griffinService).update(griffin);

        // When / Then
        assertThatCode(() -> griffinController.updateGriffin(griffin)).isInstanceOf(VitamClientException.class);
    }

    @Test
    void updateGriffin_should_propagate_AccessExternalClientException()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        Griffin griffin = buildGriffin("id-1");
        doThrow(new AccessExternalClientException("Access error")).when(griffinService).update(griffin);

        // When / Then
        assertThatCode(() -> griffinController.updateGriffin(griffin)).isInstanceOf(
            AccessExternalClientException.class
        );
    }

    // ------------------------------------------------------------------ deleteGriffin

    @Test
    void deleteGriffin_should_delegate_to_service()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        Griffin griffin = buildGriffin("id-1");
        doNothing().when(griffinService).delete(griffin);

        // When / Then
        assertThatCode(() -> griffinController.deleteGriffin(griffin)).doesNotThrowAnyException();
        verify(griffinService).delete(griffin);
    }

    @Test
    void deleteGriffin_should_propagate_VitamClientException()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        Griffin griffin = buildGriffin("id-1");
        doThrow(new VitamClientException("Vitam error")).when(griffinService).delete(griffin);

        // When / Then
        assertThatCode(() -> griffinController.deleteGriffin(griffin)).isInstanceOf(VitamClientException.class);
    }

    @Test
    void deleteGriffin_should_propagate_IOException()
        throws VitamClientException, AccessExternalClientException, IOException {
        // Given
        Griffin griffin = buildGriffin("id-1");
        doThrow(new IOException("IO error")).when(griffinService).delete(griffin);

        // When / Then
        assertThatCode(() -> griffinController.deleteGriffin(griffin)).isInstanceOf(IOException.class);
    }

    // ------------------------------------------------------------------ helpers

    private Griffin buildGriffin(String id) {
        return new Griffin(id, 0, 1, "IDENTIFIER_" + id, "Griffin " + id, null, "griffin-exec", "1.0.0", null, null);
    }
}
