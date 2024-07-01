package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailDto;
import fr.gouv.vitamui.referential.internal.client.AccessionRegisterDetailInternalRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AccessionRegisterDetailExternalServiceTest extends ExternalServiceTest {

    @Mock
    private ExternalSecurityService externalSecurityService;

    @Mock
    private AccessionRegisterDetailInternalRestClient accessionRegisterDetailInternalRestClient;

    @InjectMocks
    private AccessionRegisterDetailExternalService accessionRegisterDetailExternalService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        accessionRegisterDetailExternalService = new AccessionRegisterDetailExternalService(
            externalSecurityService,
            accessionRegisterDetailInternalRestClient
        );
    }

    @Test
    void should_call_the_right_rest_client_method_once_when_paginated_service_is_invoked() {
        //Given
        doReturn(new PaginatedValuesDto<AccessionRegisterDetailDto>())
            .when(accessionRegisterDetailInternalRestClient)
            .getAllPaginated(any(), any(), any(), any(), any(), any());

        //When
        accessionRegisterDetailExternalService.getAllPaginated(
            0,
            20,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );

        //THen
        verify(accessionRegisterDetailInternalRestClient, times(1)).getAllPaginated(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }
}
