package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.internal.client.ContextInternalRestClient;
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
public class ContextExternalServiceTest extends ExternalServiceTest {

    @Mock
    private ContextInternalRestClient contextInternalRestClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private ContextExternalService contextExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        contextExternalService = new ContextExternalService(externalSecurityService, contextInternalRestClient);
    }

    @Test
    public void getAll_should_return_ContextDtoList_when_contextInternalRestClient_return_ContextDtoList() {

        List<ContextDto> list = new ArrayList<>();
        ContextDto contextDto = new ContextDto();
        contextDto.setId("1");
        list.add(contextDto);

        when(contextInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);

        assertThatCode(() -> {
            contextExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();

    }

    @Test
    public void create_should_return_ContextDto_when_contextInternalRestClient_return_ContextDto() {

        ContextDto contextDto = new ContextDto();
        contextDto.setId("1");

        when(contextInternalRestClient.create(any(InternalHttpContext.class), any(ContextDto.class)))
            .thenReturn(contextDto);

        assertThatCode(() -> {
            contextExternalService.create(new ContextDto());
        }).doesNotThrowAnyException();

    }

    @Test
    public void check_should_return_boolean_when_contextInternalRestClient_return_boolean() {

        when(contextInternalRestClient.check(any(InternalHttpContext.class), any(ContextDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            contextExternalService.check(new ContextDto());
        }).doesNotThrowAnyException();

    }
}
