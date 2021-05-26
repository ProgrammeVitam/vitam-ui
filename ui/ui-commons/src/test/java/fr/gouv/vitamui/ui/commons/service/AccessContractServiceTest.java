package fr.gouv.vitamui.ui.commons.service;

import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitamui.commons.api.domain.AccessContractsDto;
import fr.gouv.vitamui.commons.rest.client.accesscontract.AccessContractExternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AccessContractServiceTest {

    private AccessContractService accessContractService;

    @Mock
    private AccessContractExternalRestClient accessContractExternalRestClient;

    @Before
    public void init() {
        accessContractService = new AccessContractService(accessContractExternalRestClient);

    }

    @Test
    public void testFindAllAccessContracts() {
        // Given When
        when(accessContractExternalRestClient.getAll(ArgumentMatchers.any()))
                .thenReturn(buildAccessContractDto());

        // When
        List<AccessContractsDto> all = accessContractService.getAll(null);

        // Then
        assertNotNull(all);
        assertEquals(all.size(), 5);
    }


    private List<AccessContractsDto> buildAccessContractDto() {
        List<AccessContractsDto> AccessContractsDtos = new ArrayList<>();
        IntStream.rangeClosed(0, 4)
            .forEach(i -> {
                final AccessContractsDto accessContractDto = new AccessContractsDto();
                accessContractDto.setId("id" + i);
                accessContractDto.setIdentifier("identifier "+ i);
                accessContractDto.setName("name " + i);
                accessContractDto.setStatus(ActivationStatus.ACTIVE.toString());
                AccessContractsDtos.add(accessContractDto);
            });
        return AccessContractsDtos;
    }
}
