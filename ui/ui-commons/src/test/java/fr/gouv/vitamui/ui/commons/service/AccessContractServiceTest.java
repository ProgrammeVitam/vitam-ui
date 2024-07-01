package fr.gouv.vitamui.ui.commons.service;

import fr.gouv.vitamui.commons.rest.client.accesscontract.AccessContractExternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccessContractServiceTest {

    private AccessContractService accessContractService;

    @Mock
    private AccessContractExternalRestClient accessContractExternalRestClient;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        accessContractService = new AccessContractService(accessContractExternalRestClient);
    }

    @Test
    public void should_call_appropriate_rest_client_method_when_get_all_operation_is_invoked() {
        // Given
        when(accessContractExternalRestClient.getAll(ArgumentMatchers.any())).thenReturn(new ArrayList<>());

        // When
        accessContractService.getAll(null);

        // Then
        verify(accessContractExternalRestClient, Mockito.times(1)).getAll(ArgumentMatchers.any());
    }
}
