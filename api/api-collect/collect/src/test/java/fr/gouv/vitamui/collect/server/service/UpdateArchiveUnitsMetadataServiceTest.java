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

package fr.gouv.vitamui.collect.server.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.error.VitamError;
import fr.gouv.vitam.common.error.VitamErrorDetails;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class UpdateArchiveUnitsMetadataServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private CollectService collectService;

    @Test
    void updateCollectArchiveUnits_should_pass_when_Vitam_Return_Ok()
        throws VitamClientException, InvalidParseOperationException, JacksonException {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        String resultDto =
            """
                {
                  "httpCode" : 200,
                  "$hits" : {
                    "total" : 0,
                    "offset" : 0,
                    "limit" : 0,
                    "size" : 0
                  },
                  "$results" : [ ],
                  "$facetResults" : [ ]
                }
            """;
        final RequestResponse<JsonNode> resultsDto = RequestResponseOK.getFromJsonNode(
            new ObjectMapper().readTree(resultDto)
        );
        RequestResponseOK<JsonNode> expectedResponse = new RequestResponseOK<>();
        expectedResponse.addResult(JsonHandler.toJsonNode(resultsDto));
        final String transactionId = "transactionId";
        InputStream csvFileInputStream =
            UpdateArchiveUnitsMetadataServiceTest.class.getClassLoader()
                .getResourceAsStream("data/updateCollectArchiveUnits/collect_metadata.csv");

        //When
        // TODO : do not mix raw values and Matchers !
        Mockito.when(
            collectService.updateCollectArchiveUnitsWithCsv(eq(vitamContext), eq(transactionId), any())
        ).thenReturn(expectedResponse);
        RequestResponse<JsonNode> response = transactionService.updateArchiveUnitsFromCsvFile(
            csvFileInputStream,
            transactionId,
            vitamContext
        );

        //Then
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void updateCollectArchiveUnits_should_not_pass_when_Vitam_throw_exception()
        throws VitamClientException, JacksonException, InvalidParseOperationException {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        VitamClientException exception = new VitamClientException("error message");
        List<VitamErrorDetails> errorDetailsList = List.of(new VitamErrorDetails("ERROR_KEY", null));
        exception.setVitamError(
            new VitamError<>("BAD_REQUEST")
                .setHttpCode(HttpStatus.BAD_REQUEST.value())
                .setContext("Collect")
                .setMessage("error message")
                .setErrorsDetails(errorDetailsList)
        );
        String resultDto =
            """
                  {
                  "httpCode" : 400,
                  "code" : "BAD_REQUEST",
                  "context" : "Collect",
                  "message" : "error message",
                  "errorsDetails" : [{
                    "key" : "ERROR_KEY"
                  }]
                }
            """;
        RequestResponse<JsonNode> expectedResponse = VitamError.getFromJsonNode(new ObjectMapper().readTree(resultDto));
        final String transactionId = "transactionId";
        InputStream csvFileInputStream =
            UpdateArchiveUnitsMetadataServiceTest.class.getClassLoader()
                .getResourceAsStream("data/updateCollectArchiveUnits/wrong_collect_metadata.csv");

        //When
        Mockito.when(
            collectService.updateCollectArchiveUnitsWithCsv(eq(vitamContext), eq(transactionId), any())
        ).thenReturn(expectedResponse);

        RequestResponse<JsonNode> response = transactionService.updateArchiveUnitsFromCsvFile(
            csvFileInputStream,
            transactionId,
            vitamContext
        );

        //Then
        assertThat(response).isEqualTo(expectedResponse);
    }
}
