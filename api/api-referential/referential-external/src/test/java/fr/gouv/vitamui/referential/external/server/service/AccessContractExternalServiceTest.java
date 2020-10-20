package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.AccessContractDto;
import fr.gouv.vitamui.referential.internal.client.AccessContractInternalRestClient;
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
public class AccessContractExternalServiceTest extends ExternalServiceTest {

    @Mock
    private AccessContractInternalRestClient accessContractInternalRestClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private AccessContractExternalService accessContractExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        accessContractExternalService = new AccessContractExternalService(externalSecurityService, accessContractInternalRestClient);
    }

    @Test
    public void getAll_should_return_AccessContractDtoList_when_accessContractInternalRestClient_return_AccessContractDtoList() {

        List<AccessContractDto> list = new ArrayList<>();
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(1);
        accessContractDto.setDescription("description");
        list.add(accessContractDto);

        when(accessContractInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(new ArrayList<AccessContractDto>());

        assertThatCode(() -> {
            accessContractExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();

    }

    @Test
    public void create_should_return_AccessContractDto_when_accessContractInternalRestClient_return_AccessContractDtoList() {

        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(1);

        when(accessContractInternalRestClient.create(any(InternalHttpContext.class), any(AccessContractDto.class)))
            .thenReturn(accessContractDto);

        assertThatCode(() -> {
            accessContractExternalService.create(new AccessContractDto());
        }).doesNotThrowAnyException();

    }

    @Test
    public void check_should_return_boolean_when_accessContractInternalRestClient_return_boolean() {

        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setTenant(1);

        when(accessContractInternalRestClient.check(any(InternalHttpContext.class), any(AccessContractDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            accessContractExternalService.check(new AccessContractDto());
        }).doesNotThrowAnyException();

    }


}
