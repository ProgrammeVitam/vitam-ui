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
 *
 */

package fr.gouv.vitamui.commons.vitam.api.access;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistentIdentifierServiceTest {

    @Mock
    AccessExternalClient accessExternalClient;

    @InjectMocks
    PersistentIdentifierService persistentIdentifierService;

    VitamContext defaultVitamContext = new VitamContext(1);
    JsonNode selectByIdQuery = new SelectMultiQuery().getFinalSelectById();

    @Test
    void findUnitsByPersistentIdentifier() throws Exception {
        // Given
        String arkId = "ark:/22567/001a957db5eadaac";
        when(accessExternalClient.selectUnitsByUnitPersistentIdentifier(any(VitamContext.class), any(JsonNode.class), eq(arkId)))
            .thenReturn(responseFromFile("data/ark/bad_ark_id.json"));
        // When
        RequestResponse<JsonNode> response = persistentIdentifierService.findUnitsByPersistentIdentifier(arkId, defaultVitamContext);
        // Then
        assertThat(response.isOk()).isTrue();
        assertThat(((RequestResponseOK<JsonNode>) response).getResults()).size().isEqualTo(0);
        verify(accessExternalClient).selectUnitsByUnitPersistentIdentifier(eq(defaultVitamContext), eq(selectByIdQuery), eq(arkId));
    }

    @Test
    void findUnitsByPersistentIdentifier_with_no_results_return() throws Exception {
        // Given
        String arkId = "ark:/22567/001a957db5eadaac";
        when(accessExternalClient.selectUnitsByUnitPersistentIdentifier(any(VitamContext.class), any(JsonNode.class), eq(arkId)))
            .thenThrow(new VitamClientException("exception thrown by client"));
        // When Then
        assertThatThrownBy(() -> persistentIdentifierService.findUnitsByPersistentIdentifier(arkId, defaultVitamContext))
            .isInstanceOf(VitamClientException.class)
            .hasMessage("exception thrown by client");
        verify(accessExternalClient).selectUnitsByUnitPersistentIdentifier(eq(defaultVitamContext), eq(selectByIdQuery), eq(arkId));
    }

    private RequestResponse<JsonNode> responseFromFile(String filename) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = PersistentIdentifierServiceTest.class.getClassLoader().getResourceAsStream(filename);
        Assertions.assertThat(inputStream).isNotNull();
        return RequestResponseOK.getFromJsonNode(objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class));
    }

}
