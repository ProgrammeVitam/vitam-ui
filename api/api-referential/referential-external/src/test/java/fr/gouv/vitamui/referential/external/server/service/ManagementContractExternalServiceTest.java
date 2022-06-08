package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.ManagementContractDto;
import fr.gouv.vitamui.referential.internal.client.ManagementContractInternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagementContractExternalServiceTest extends ExternalServiceTest {

    @Mock
    private ManagementContractInternalRestClient managementContractInternalRestClient;

    @Mock
    private ExternalSecurityService externalSecurityService;

    private  ManagementContractExternalService managementContractExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        managementContractExternalService = new ManagementContractExternalService(externalSecurityService);
        managementContractExternalService.setManagementContractInternalRestClient(managementContractInternalRestClient);
    }

    @Test
    public void getAll_should_return_ManagementContractDtoList_when_managementContractInternalRestClient_return_ManagementContractDtoList() {
        List<ManagementContractDto> list = new ArrayList<>();
        ManagementContractDto mcd = new ManagementContractDto();
        mcd.setId("id");
        mcd.setTenant(0);
        list.add(mcd);

        when(managementContractInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);
        assertThatCode(() -> {
            managementContractExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_boolean_when_managementContractInternalRestClient_return_boolean() {
        ManagementContractDto mcd = new ManagementContractDto();
        mcd.setId("id");
        mcd.setTenant(0);

        when(managementContractInternalRestClient.check(any(InternalHttpContext.class), any(ManagementContractDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            managementContractExternalService.check(new ManagementContractDto());
        }).doesNotThrowAnyException();
    }
}
