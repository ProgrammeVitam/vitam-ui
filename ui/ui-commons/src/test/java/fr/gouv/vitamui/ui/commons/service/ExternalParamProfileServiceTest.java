package fr.gouv.vitamui.ui.commons.service;

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.external.client.ExternalParamProfileExternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ExternalParamProfileServiceTest extends ServiceTest<ExternalParamProfileDto> {

    private ExternalParamProfileService service;

    @Mock
    private ExternalParamProfileExternalRestClient client;

    @Before
    public void setup() {

        Mockito.when(factory.getExternalParamProfileExternalRestClient()).thenReturn(client);

        when(factory.getExternalParamProfileExternalRestClient()).thenReturn(client);
        service = new ExternalParamProfileService(factory, commonService);
    }

    @Test
    public void testCreate() {
        Mockito.when(client.create(any(), any())).thenReturn(buildDto("ID"));
        final ExternalParamProfileDto dto = service.create(null, buildDto("ID"));
        assertThat(dto).isNotNull();
    }

    @Test
    public void testUpdate() {
        Mockito.when(getClient().update(any(), any())).thenReturn(buildDto(ID));
        final ExternalParamProfileDto dtoUpdate = buildDto(null);
        dtoUpdate.setId(ID);
        final ExternalParamProfileDto dto = getService().update(null, dtoUpdate);
        assertThat(dto).isNotNull();
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
