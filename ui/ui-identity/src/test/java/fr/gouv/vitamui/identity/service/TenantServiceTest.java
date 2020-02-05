package fr.gouv.vitamui.identity.service;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.external.client.TenantExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public class TenantServiceTest extends UIIdentityServiceTest<TenantDto> {

    private TenantService service;

    @SuppressWarnings("unused")
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantServiceTest.class);

    @Mock
    private TenantExternalRestClient client;

    @Test
    public void testCreate() {
        super.createEntite();
    }

    @Test
    public void testGetTenantsByCriteria() {
        service.getAll(null, Optional.empty());
    }

    @Test
    public void testUpdate() {
        super.updateEntite();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithIdEmpty() {
        super.updateWithIdEmpty();
    }

    @Before
    public void setup() {
        service = new TenantService(factory);
        Mockito.when(factory.getTenantExternalRestClient()).thenReturn(client);
    }

    @Override
    public TenantExternalRestClient getClient() {
        return client;
    }

    @Override
    protected TenantDto buildDto(final String id) {
        final TenantDto dto = new TenantDto();
        dto.setIdentifier(1);
        dto.setCustomerId("customerId");
        dto.setEnabled(true);
        dto.setName("name");
        return dto;
    }

    @Override
    protected AbstractCrudService<TenantDto> getService() {
        return service;
    }
}
