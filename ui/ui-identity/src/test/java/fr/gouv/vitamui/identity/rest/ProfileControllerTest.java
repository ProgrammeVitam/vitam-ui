package fr.gouv.vitamui.identity.rest;

import static org.mockito.ArgumentMatchers.any;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.identity.service.ProfileService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { ProfileController.class })
public class ProfileControllerTest extends UiIdentityRestControllerTest<ProfileDto> {

    @MockBean
    private BuildProperties buildProperties;

    @Value("${ui-identity.prefix}")
    protected String apiUrl;

    @MockBean
    private ProfileService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileControllerTest.class);

    private static final String PREFIX = "/profiles";

    @Test
    public void testGetAllProfiles() {
        LOGGER.debug("testGetAllEntity");
        super.testGetAllEntityWithCriteria();
    }

    @Test
    public void testPatchProfile() {
        LOGGER.debug("testPatchProfile");
        super.testPatchEntity();
    }

    @Test
    public void testGetPatchProfileById() {
        super.testGetEntityById();
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected Class<ProfileDto> getDtoClass() {
        return ProfileDto.class;
    }

    @Override
    protected ProfileDto buildDto() {
        final ProfileDto dto = new ProfileDto();
        dto.setEnabled(true);
        dto.setName("name");
        dto.setCustomerId("customerId");
        return dto;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        try {
            Mockito.when(service.create(any(), any())).thenReturn(new ProfileDto());
        }
        catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        Mockito.when(service.update(any(), any(ProfileDto.class))).thenReturn(new ProfileDto());
        Mockito.when(service.patch(any(), any(), any())).thenReturn(new ProfileDto());
    }
}
