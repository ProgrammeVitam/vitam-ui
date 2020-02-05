package fr.gouv.vitamui.ui.commons.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.external.client.SubrogationExternalRestClient;

@RunWith(SpringJUnit4ClassRunner.class)
public class SubrogationServiceTest extends ServiceTest<SubrogationDto> {

    private SubrogationService service;

    @Mock
    private SubrogationExternalRestClient client;

    @Before
    public void setup() {
        Mockito.when(factory.getSubrogationExternalRestClient()).thenReturn(client);
        service = new SubrogationService(factory);
    }

    @Test
    public void testCreate() {
        super.createEntite();
    }

    @Test
    public void testUpdate() {
        super.updateEntite();
    }

    @Test
    public void testDelete() {
        super.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithIdEmpty() {
        super.updateWithIdEmpty();
    }

    @Test
    public void testAccept() {
        Mockito.when(client.accept(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new SubrogationDto());
        final SubrogationDto subro = service.accept(null, "1");
        assertThat(subro).isNotNull();
    }

    @Test
    public void testGetMySubrogationAsSurrogate() {
        Mockito.when(client.getMySubrogationAsSurrogate(ArgumentMatchers.any())).thenReturn(new SubrogationDto());
        final SubrogationDto subro = service.getMySubrogationAsSurrogate(null);
        assertThat(subro).isNotNull();
    }

    @Test
    public void testGetMySubrogationAsSuperuser() {
        Mockito.when(client.getMySubrogationAsSuperuser(ArgumentMatchers.any())).thenReturn(new SubrogationDto());
        final SubrogationDto subro = service.getMySubrogationAsSuperuser(null);
        assertThat(subro).isNotNull();
    }

    @Test
    public void testDeline() {
        service.decline(null, "1");
    }

    @Override
    public SubrogationExternalRestClient getClient() {
        return client;
    }

    @Override
    protected SubrogationDto buildDto(final String id) {
        final SubrogationDto dto = new SubrogationDto();
        dto.setDate(OffsetDateTime.now());
        dto.setStatus(SubrogationStatusEnum.CREATED);
        dto.setSuperUser("superuser@vitamui.com");
        dto.setSurrogate("surrogate@vitamui.com");
        return dto;
    }

    @Override
    protected AbstractCrudService<SubrogationDto> getService() {
        return service;
    }
}
