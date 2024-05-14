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
 *
 */

package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.DataObjectVersionType;
import fr.gouv.vitam.common.model.dip.QualifierVersion;
import fr.gouv.vitam.common.model.export.transfer.TransferRequest;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.TransferAcknowledgmentService;
import fr.gouv.vitamui.commons.vitam.api.access.TransferRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

import static fr.gouv.vitam.common.model.administration.DataObjectVersionType.BINARY_MASTER;
import static fr.gouv.vitam.common.model.dip.QualifierVersion.FIRST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class TransferVitamOperationsInternalServiceTest {

    @Mock
    private TransferAcknowledgmentService transferAcknowledgmentService;

    @Mock
    private TransferRequestService transferRequestService;

    @Mock
    private ArchiveSearchInternalService archiveSearchInternalService;

    @InjectMocks
    TransferVitamOperationsInternalService transferVitamOperationsInternalService;

    @BeforeEach
    public void beforeEach() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void transferRequest_should_pass() throws Exception {
        //Given
        final Map<DataObjectVersionType, Set<QualifierVersion>> dataObjectVersionsPatterns = Map.of(
            BINARY_MASTER,
            Set.of(FIRST)
        );
        final TransferRequestDto transferRequestDto = newTransferRequestDto(true, "2.2", dataObjectVersionsPatterns);
        VitamContext vitamContext = newVitamContext();
        String jsonDslQuery =
            "{\"$roots\":[],\"$query\":[{\"$and\":[{\"$eq\":{\"#id\":\"aeaqaaaaaehmay6yaaqhual6ysiaariaaaba\"}}]}],\"$filter\":{\"$limit\":10},\"$projection\":{},\"$facets\":[]}";
        JsonNode dslQuery = newJsonNode(jsonDslQuery);

        Mockito.when(
            archiveSearchInternalService.prepareDslQuery(transferRequestDto.getSearchCriteria(), vitamContext)
        ).thenReturn(dslQuery);
        String requestResponseOKJson =
            "{\"httpCode\":202,\"$hits\":{\"total\":1,\"offset\":0,\"limit\":0,\"size\":1},\"$results\":[{\"itemId\":\"aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq\",\"message\":\"toutestOK\",\"globalStatus\":\"STARTED\",\"globalState\":\"RUNNING\",\"lifecycleEnable\":true}]}";
        RequestResponse<JsonNode> responseReturned = RequestResponseOK.getFromJsonNode(
            newJsonNode(requestResponseOKJson)
        );
        Mockito.when(transferRequestService.transferRequest(eq(vitamContext), any())).thenReturn(responseReturned);

        //When
        String response = transferVitamOperationsInternalService.transferRequest(transferRequestDto, vitamContext);

        //then
        assertThat(response).isEqualTo("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");

        ArgumentCaptor<TransferRequest> argumentCaptor = ArgumentCaptor.forClass(TransferRequest.class);
        verify(transferRequestService).transferRequest(any(VitamContext.class), argumentCaptor.capture());
        final TransferRequest transferRequest = argumentCaptor.getValue();
        assertThat(transferRequest.isTransferWithLogBookLFC()).isTrue();
        assertThat(transferRequest.isTransferWithoutObjects()).isFalse();
        assertThat(transferRequest.getSedaVersion()).isEqualTo("2.2");
        assertThat(transferRequest.getDataObjectVersionToExport().getDataObjectVersionsPatterns()).isEqualTo(
            dataObjectVersionsPatterns
        );
    }

    @Test
    void transferAcknowledgmentRequest_should_pass_and_Vitam_Return_OperationDetails()
        throws VitamClientException, IOException, InvalidParseOperationException {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        InputStream atrFileInputStream =
            TransferVitamOperationsInternalServiceTest.class.getClassLoader()
                .getResourceAsStream("data/transferAcknowledgment/ATR_aeeaaaaaaghduohdabkogamdgezb2taaaaaq.xml");

        InputStream transferReplyVitamResponse =
            TransferVitamOperationsInternalServiceTest.class.getClassLoader()
                .getResourceAsStream("data/transferAcknowledgment/transferReplyVitamResponse.json");

        String vitamResponseToString = readFromInputStream(transferReplyVitamResponse);
        RequestResponse<JsonNode> vitamJsonResponse = RequestResponseOK.getFromJsonNode(
            new ObjectMapper().readTree(vitamResponseToString)
        );

        //When
        Mockito.when(transferAcknowledgmentService.transferAcknowledgment(eq(vitamContext), any())).thenReturn(
            vitamJsonResponse
        );
        String response = transferVitamOperationsInternalService.transferAcknowledgmentService(
            atrFileInputStream,
            vitamContext
        );

        //Then
        assertThat(response).isEqualTo("aeeaaaaaaghduohdabjwkamdgezrs7aaaaaq");
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    private JsonNode newJsonNode(String json) throws JsonProcessingException {
        return new ObjectMapper().readTree(json);
    }

    private VitamContext newVitamContext() {
        return new VitamContext(1);
    }

    private TransferRequestDto newTransferRequestDto(
        boolean lifeCycleLogs,
        String sedaVersion,
        Map<DataObjectVersionType, Set<QualifierVersion>> dataObjectVersionsPatterns
    ) {
        final TransferRequestDto transferRequestDto = new TransferRequestDto();
        transferRequestDto.setLifeCycleLogs(lifeCycleLogs);
        transferRequestDto.setSedaVersion(sedaVersion);
        transferRequestDto.setWithoutObjects(false);
        transferRequestDto.setDataObjectVersionsPatterns(dataObjectVersionsPatterns);
        return transferRequestDto;
    }
}
