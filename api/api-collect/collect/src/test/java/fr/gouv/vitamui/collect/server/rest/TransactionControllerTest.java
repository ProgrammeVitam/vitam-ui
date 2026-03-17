/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.error.VitamError;
import fr.gouv.vitam.common.error.VitamErrorDetails;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.collect.server.service.ExternalParametersService;
import fr.gouv.vitamui.collect.server.service.TransactionService;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_TRANSACTION_PATH;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = { TransactionController.class })
class TransactionControllerTest extends ApiCollectControllerTest<IdDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionControllerTest.class);

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private ExternalParametersService externalParametersService;

    private TransactionController transactionController;

    @BeforeEach
    public void setUp() {
        transactionController = new TransactionController(transactionService, externalParametersService);
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.TRANSACTIONS };
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return IdDto.class;
    }

    @Override
    protected IdDto buildDto() {
        return null;
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return COLLECT_TRANSACTION_PATH;
    }

    @Test
    void when_abortTransaction_ok() throws VitamClientException {
        Mockito.when(externalParametersService.buildVitamContextFromExternalParam()).thenReturn(new VitamContext(0));
        Mockito.doNothing().when(transactionService).abortTransaction(eq("transactionId"), any(VitamContext.class));
        this.transactionController.abortTransaction("transactionId");
        verify(transactionService, times(1)).abortTransaction(eq("transactionId"), any(VitamContext.class));
    }

    @Test
    void when_searchCollectUnitsByCriteria_Service_ko_should_return_ko() throws VitamClientException {
        Mockito.when(externalParametersService.buildVitamContextFromExternalParam()).thenReturn(new VitamContext(0));
        Mockito.doNothing().when(transactionService).reopenTransaction(eq("transactionId"), any(VitamContext.class));
        this.transactionController.reopenTransaction("transactionId");
        verify(transactionService, times(1)).reopenTransaction(eq("transactionId"), any(VitamContext.class));
    }

    @Test
    void testUpdateUnitsMetadataThenReturnVitamOperationDetails()
        throws PreconditionFailedException, JsonProcessingException, InvalidParseOperationException, VitamClientException {
        // Given
        String fileName = "FileName";
        String transactionId = "transactionId";
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
        VitamClientException exception = new VitamClientException("error message");
        List<VitamErrorDetails> errorDetailsList = List.of(new VitamErrorDetails("ERROR_KEY", null));
        exception.setVitamError(
            new VitamError<>("BAD_REQUEST")
                .setHttpCode(HttpStatus.BAD_REQUEST.value())
                .setContext("Collect")
                .setMessage("error message")
                .setErrorsDetails(errorDetailsList)
        );
        RequestResponse<JsonNode> expectedResponse = VitamError.getFromJsonNode(new ObjectMapper().readTree(resultDto));
        String initialString = "csv file to update collect units";
        InputStream csvFile = new ByteArrayInputStream(initialString.getBytes());

        // When
        Mockito.when(externalParametersService.buildVitamContextFromExternalParam()).thenReturn(new VitamContext(0));
        Mockito.when(
            transactionService.updateArchiveUnitsFromCsvFile(
                any(InputStream.class),
                eq(transactionId),
                any(VitamContext.class)
            )
        )
            .thenThrow(exception)
            .thenReturn(expectedResponse);

        assertThatCode(
            () -> transactionController.updateArchiveUnitsMetadataFromCsvFile(transactionId, csvFile, fileName)
        )
            .isInstanceOf(VitamClientException.class)
            .hasMessage("error message");
    }
}
