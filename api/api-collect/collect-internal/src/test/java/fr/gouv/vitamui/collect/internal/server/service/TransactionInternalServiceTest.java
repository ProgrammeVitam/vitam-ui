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

package fr.gouv.vitamui.collect.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.collect.common.dto.TransactionDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.internal.server.service.converters.TransactionConverter;
import fr.gouv.vitamui.commons.api.exception.RequestTimeOutException;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TransactionInternalServiceTest {

    @InjectMocks
    TransactionInternalService transactionInternalService;

    @Mock
    CollectService collectService;

    final PodamFactory factory = new PodamFactoryImpl();
    final VitamContext vitamContext = new VitamContext(1);
    ObjectMapper objectMapper = new ObjectMapper();
    public final String TRANSACTION_ID = "TRANSACTION_ID_FOR_LIFE";

    @BeforeEach
    public void beforeEach() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void shouldValidateTransactionWithSuccess() throws VitamClientException {
        // GIVEN
        when(collectService.validateTransaction(vitamContext, TRANSACTION_ID)).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );
        // THEN
        assertDoesNotThrow(() -> transactionInternalService.validateTransaction(TRANSACTION_ID, vitamContext));
    }

    @Test
    void shouldThrowExceptionWhenValidateTransaction() throws VitamClientException {
        // GIVEN
        when(collectService.validateTransaction(vitamContext, TRANSACTION_ID)).thenThrow(VitamClientException.class);
        // THEN
        assertThrows(
            VitamClientException.class,
            () -> transactionInternalService.validateTransaction(TRANSACTION_ID, vitamContext)
        );
    }

    @Test
    void shouldSendTransactionWithSuccess() throws VitamClientException {
        // GIVEN
        when(collectService.sendTransaction(vitamContext, TRANSACTION_ID)).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );
        // THEN
        assertDoesNotThrow(() -> transactionInternalService.sendTransaction(TRANSACTION_ID, vitamContext));
    }

    @Test
    void shouldThrowExceptionWhenSendTransaction() throws VitamClientException {
        // GIVEN
        when(collectService.sendTransaction(vitamContext, TRANSACTION_ID)).thenThrow(VitamClientException.class);
        // THEN
        assertThrows(
            VitamClientException.class,
            () -> transactionInternalService.sendTransaction(TRANSACTION_ID, vitamContext)
        );
    }

    @Test
    void shouldAbortTransactionWithSuccess() throws VitamClientException {
        // GIVEN
        when(collectService.abortTransaction(vitamContext, TRANSACTION_ID)).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );
        // THEN
        assertDoesNotThrow(() -> transactionInternalService.abortTransaction(TRANSACTION_ID, vitamContext));
    }

    @Test
    void shouldThrowExceptionWhenAbortTransaction() throws VitamClientException {
        // GIVEN
        when(collectService.abortTransaction(vitamContext, TRANSACTION_ID)).thenThrow(VitamClientException.class);
        // THEN
        assertThrows(
            VitamClientException.class,
            () -> transactionInternalService.abortTransaction(TRANSACTION_ID, vitamContext)
        );
    }

    @Test
    void shouldReopenTransactionWithSuccess() throws VitamClientException {
        // GIVEN
        when(collectService.reopenTransaction(vitamContext, TRANSACTION_ID)).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );
        // THEN
        assertDoesNotThrow(() -> transactionInternalService.reopenTransaction(TRANSACTION_ID, vitamContext));
    }

    @Test
    void shouldThrowExceptionWhenReopenTransaction() throws VitamClientException {
        // GIVEN
        when(collectService.reopenTransaction(vitamContext, TRANSACTION_ID)).thenThrow(VitamClientException.class);
        // THEN
        assertThrows(
            VitamClientException.class,
            () -> transactionInternalService.reopenTransaction(TRANSACTION_ID, vitamContext)
        );
    }

    @Test
    void shouldGetTransactionByIdWithSuccess()
        throws VitamClientException, InvalidParseOperationException, JsonProcessingException {
        // GIVEN
        TransactionDto transactionDto = factory.manufacturePojo(TransactionDto.class);
        RequestResponseOK<JsonNode> fakeResponse = new RequestResponseOK<>();
        fakeResponse.setHttpCode(200);
        fakeResponse.addResult(JsonHandler.toJsonNode(transactionDto));
        RequestResponse<JsonNode> mockResponse = RequestResponse.parseFromResponse(
            Response.ok(objectMapper.writeValueAsString(fakeResponse)).build()
        );

        when(collectService.getTransactionById(vitamContext, TRANSACTION_ID)).thenReturn(mockResponse);
        // WHEN
        CollectTransactionDto resultedTransaction = transactionInternalService.getTransactionById(
            TRANSACTION_ID,
            vitamContext
        );
        // THEN
        assertNotNull(resultedTransaction);
        assertEquals(transactionDto.getId(), resultedTransaction.getId());
        assertEquals(transactionDto.getAcquisitionInformation(), resultedTransaction.getAcquisitionInformation());
        assertEquals(transactionDto.getArchivalAgreement(), resultedTransaction.getArchivalAgreement());
        assertEquals(transactionDto.getLegalStatus(), resultedTransaction.getLegalStatus());
        assertEquals(transactionDto.getProjectId(), resultedTransaction.getProjectId());
        assertEquals(transactionDto.getName(), resultedTransaction.getName());
    }

    @Test
    void shouldThrowExceptionWhenGetTransactionById() throws VitamClientException {
        // GIVEN
        when(collectService.getTransactionById(vitamContext, TRANSACTION_ID)).thenThrow(VitamClientException.class);
        // THEN
        assertThrows(
            VitamClientException.class,
            () -> transactionInternalService.getTransactionById(TRANSACTION_ID, vitamContext)
        );
    }

    @Test
    void shouldUpdateTransactionWithSuccess()
        throws VitamClientException, InvalidParseOperationException, JsonProcessingException {
        // GIVEN
        TransactionDto transactionDto = factory.manufacturePojo(TransactionDto.class);
        RequestResponseOK<JsonNode> fakeResponse = new RequestResponseOK<>();
        fakeResponse.setHttpCode(200);
        fakeResponse.addResult(JsonHandler.toJsonNode(transactionDto));
        RequestResponse<JsonNode> mockResponse = RequestResponse.parseFromResponse(
            Response.ok(objectMapper.writeValueAsString(fakeResponse)).build()
        );

        when(collectService.updateTransaction(vitamContext, transactionDto)).thenReturn(mockResponse);
        // WHEN
        CollectTransactionDto resultedTransaction = transactionInternalService.updateTransaction(
            TransactionConverter.toVitamUiDto(transactionDto),
            vitamContext
        );
        // THEN
        assertNotNull(resultedTransaction);
        assertEquals(transactionDto.getId(), resultedTransaction.getId());
        assertEquals(transactionDto.getAcquisitionInformation(), resultedTransaction.getAcquisitionInformation());
        assertEquals(transactionDto.getArchivalAgreement(), resultedTransaction.getArchivalAgreement());
        assertEquals(transactionDto.getLegalStatus(), resultedTransaction.getLegalStatus());
        assertEquals(transactionDto.getProjectId(), resultedTransaction.getProjectId());
    }

    @Test
    void shouldThrowExceptionWhenUpdateTransaction() throws VitamClientException {
        // GIVEN
        when(collectService.getTransactionById(vitamContext, TRANSACTION_ID)).thenThrow(VitamClientException.class);
        // THEN
        assertThrows(
            VitamClientException.class,
            () -> transactionInternalService.getTransactionById(TRANSACTION_ID, vitamContext)
        );
    }

    @Test
    void shouldUpdateArchiveUnitsFromFileWithSuccess() throws InvalidParseOperationException {
        // GIVEN
        TransactionDto transactionDto = factory.manufacturePojo(TransactionDto.class);
        RequestResponseOK<JsonNode> fakeResponse = new RequestResponseOK<>();
        fakeResponse.setHttpCode(200);
        fakeResponse.addResult(JsonHandler.toJsonNode(transactionDto));

        String fakeContent = "Path;Name;blablabla";
        InputStream csvContent = new ByteArrayInputStream(fakeContent.getBytes());

        when(collectService.updateCollectArchiveUnits(vitamContext, TRANSACTION_ID, csvContent)).thenReturn(
            "BELIEVE ME, I AM 200"
        );
        // WHEN
        String resultedOperation = transactionInternalService.updateArchiveUnitsFromFile(
            csvContent,
            TRANSACTION_ID,
            vitamContext
        );
        // THEN
        assertNotNull(resultedOperation);
    }

    @Test
    void shouldThrowExceptionWhenUpdateArchiveUnitsFromFile() throws VitamClientException {
        // GIVEN
        when(collectService.updateCollectArchiveUnits(any(), any(), any())).thenReturn("ERROR_400");
        // THEN
        assertThrows(
            RequestTimeOutException.class,
            () -> transactionInternalService.updateArchiveUnitsFromFile(any(), any(), any())
        );
    }
}
