package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.internal.client.IngestContractInternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IngestContractExternalServiceTest extends ExternalServiceTest {

    @Mock
    private IngestContractInternalRestClient ingestContractInternalRestClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private IngestContractExternalService ingestContractExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        ingestContractExternalService = new IngestContractExternalService(externalSecurityService, ingestContractInternalRestClient);
    }

    @Test
    public void getAll_should_return_IngestContractDtoList_when_ingestContractInternalRestClient_return_IngestContractDtoList() {

        List<IngestContractDto> list = new ArrayList<>();
        IngestContractDto ingestContractDto = new IngestContractDto();
        ingestContractDto.setId("1");
        list.add(ingestContractDto);

        when(ingestContractInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);

        assertThatCode(() -> {
            ingestContractExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();

    }

    @Test
    public void create_should_return_IngestContractDto_when_ingestContractInternalRestClient_return_IngestContractDto() {

        IngestContractDto ingestContractDto = new IngestContractDto();
        ingestContractDto.setId("1");

        when(ingestContractInternalRestClient.create(any(InternalHttpContext.class), any(IngestContractDto.class)))
            .thenReturn(ingestContractDto);

        assertThatCode(() -> {
            ingestContractExternalService.create(new IngestContractDto());
        }).doesNotThrowAnyException();


    }

    @Test
    public void check_should_return_boolean_when_ingestContractInternalRestClient_return_boolean() {

        when(ingestContractInternalRestClient.check(any(InternalHttpContext.class), any(IngestContractDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            ingestContractExternalService.check(new IngestContractDto());
        }).doesNotThrowAnyException();

    }
}
