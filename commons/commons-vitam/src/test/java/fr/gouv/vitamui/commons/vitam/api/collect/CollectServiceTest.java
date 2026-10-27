package fr.gouv.vitamui.commons.vitam.api.collect;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.collect.common.dto.CriteriaTransactionDto;
import fr.gouv.vitam.collect.external.client.CollectExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectServiceTest {

    @InjectMocks
    private CollectService collectService;

    @Mock
    private CollectExternalClient collectExternalClient;

    private final VitamContext vitamContext = new VitamContext(1);

    @Test
    void shouldReturnResponseWhenSearchTransactionsSucceeds() throws VitamClientException {
        // GIVEN
        CriteriaTransactionDto criteria = new CriteriaTransactionDto();
        RequestResponseOK<JsonNode> fakeResponse = new RequestResponseOK<JsonNode>().setHttpCode(200);
        when(collectExternalClient.searchTransactions(vitamContext, criteria)).thenReturn(fakeResponse);

        // WHEN
        RequestResponse<JsonNode> result = collectService.searchTransactions(vitamContext, criteria);

        // THEN
        assertEquals(fakeResponse, result);
    }

    @Test
    void shouldThrowExceptionWhenSearchTransactionsReturnsKo() throws VitamClientException {
        // GIVEN
        CriteriaTransactionDto criteria = new CriteriaTransactionDto();
        RequestResponseOK<JsonNode> fakeResponse = new RequestResponseOK<JsonNode>().setHttpCode(500);
        when(collectExternalClient.searchTransactions(vitamContext, criteria)).thenReturn(fakeResponse);

        // THEN
        assertThrows(InternalServerException.class, () -> collectService.searchTransactions(vitamContext, criteria));
    }
}
