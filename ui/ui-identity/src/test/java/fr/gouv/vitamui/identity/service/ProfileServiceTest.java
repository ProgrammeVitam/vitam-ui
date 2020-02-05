package fr.gouv.vitamui.identity.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.iam.external.client.ProfileExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public class ProfileServiceTest extends UIIdentityServiceTest<ProfileDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileServiceTest.class);

    private ProfileService service;

    @Mock
    private ProfileExternalRestClient client;

    @Before
    public void setup() {
        service = new ProfileService(commonService, factory);
        Mockito.when(factory.getProfileExternalRestClient()).thenReturn(client);
    }

    @Override
    protected ProfileExternalRestClient getClient() {
        return client;
    }

    @Override
    protected ProfileDto buildDto(final String id) {
        final ProfileDto dto = new ProfileDto();
        dto.setName("user");
        dto.setApplicationName("APP_USER");
        dto.setDescription("description");
        dto.setTenantIdentifier(10);
        dto.setCustomerId("customerId");
        return dto;
    }

    @Override
    protected AbstractCrudService<ProfileDto> getService() {
        return service;
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

}
