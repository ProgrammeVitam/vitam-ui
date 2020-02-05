package fr.gouv.vitamui.identity.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.iam.external.client.OwnerExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public class OwnerServiceTest extends UIIdentityServiceTest<OwnerDto> {

    private OwnerService service;

    @Mock
    private OwnerExternalRestClient client;

    @Before
    public void setup() {
        service = new OwnerService(factory);
        Mockito.when(factory.getOwnerExternalRestClient()).thenReturn(client);
    }

    @Test
    public void testCreate() {
        super.createEntite();
    }

    @Test
    public void testUpdate() {
        super.updateEntite();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithIdEmpty() {
        super.updateWithIdEmpty();
    }

    @Override
    public OwnerDto buildDto(final String id) {
        final OwnerDto dto = new OwnerDto();
        dto.setId(id);
        dto.setCode("0123456");
        dto.setCustomerId("0123456");
        dto.setCompanyName("companyName");
        dto.setName("name");
        return dto;
    }

    @Override
    protected OwnerExternalRestClient getClient() {
        return client;
    }

    @Override
    protected AbstractCrudService<OwnerDto> getService() {
        return service;
    }
}
