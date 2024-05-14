package fr.gouv.vitamui.ui.commons.service;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.iam.external.client.ExternalParamProfileExternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExternalParamProfileServiceTest extends ServiceTest<ExternalParamProfileDto> {

    private ExternalParamProfileService service;

    @Mock
    private ExternalParamProfileExternalRestClient client;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(factory.getExternalParamProfileExternalRestClient()).thenReturn(client);
        service = new ExternalParamProfileService(factory, commonService);
    }

    @Test
    public void should_call_appropriate_rest_client_method_when_get_one_operation_is_invoked() {
        //Given
        when(client.getOne(any(), any())).thenReturn(buildDto("ID"));

        //When
        service.getOne(null, "ID");

        //Then
        verify(client, Mockito.times(1)).getOne(any(), any());
    }

    @Test
    public void should_call_appropriate_rest_client_method_when_create_operation_is_invoked() {
        //Given
        when(client.create(any(), any())).thenReturn(buildDto("ID"));

        //When
        service.create(null, buildDto("ID"));

        //Then
        verify(client, Mockito.times(1)).create(any(), any());
    }

    @Test
    public void should_call_appropriate_rest_client_method_when_find_history_by_id_operation_is_invoked() {
        //Given
        when(client.findHistoryById(any(), any())).thenReturn(null);

        //When
        service.findHistoryById(null, "ID");

        //Then
        verify(client, Mockito.times(1)).findHistoryById(any(), any());
    }

    @Override
    protected ExternalParamProfileDto buildDto(String id) {
        final ExternalParamProfileDto dto = new ExternalParamProfileDto();
        dto.setId(id);
        dto.setName("profile externe");
        dto.setDescription("description");
        dto.setEnabled(true);
        dto.setAccessContract("ContratTNR");
        dto.setDateTime(OffsetDateTime.now());
        dto.setExternalParamIdentifier("ExternalParamId");
        dto.setIdExternalParam("IdExternalParam");
        dto.setIdProfile("IdProfile");
        dto.setProfileIdentifier("ProfileId");
        return dto;
    }

    @Override
    protected ExternalParamProfileExternalRestClient getClient() {
        return client;
    }

    @Override
    protected AbstractCrudService<ExternalParamProfileDto> getService() {
        return service;
    }
}
