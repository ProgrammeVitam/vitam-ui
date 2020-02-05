package fr.gouv.vitamui.iam.internal.server.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { ProfileInternalController.class })
public class ProfileInternalControllerTest extends ApiIamControllerTest<ProfileDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileInternalControllerTest.class);

    @MockBean
    private ProfileInternalService profileInternalService;

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
    public void testGetPaginatedProfile() {
        LOGGER.debug("testGetPaginatedProfile");
        super.testGetPaginatedEntities();
    }

    @Test
    public void testGetLevels() {
        LOGGER.debug("testGetLevels");
        super.performGet(CommonConstants.PATH_LEVELS, ImmutableMap.of(),
                status().isOk());
        Mockito.verify(profileInternalService, Mockito.times(1)).getLevels(ArgumentMatchers.any());
    }

    @Override
    protected ProfileDto buildDto() {
        return IamServerUtilsTest.buildProfileDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
    }

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_PROFILES_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] {};
    }

    @Override
    protected Class<ProfileDto> getDtoClass() {
        return ProfileDto.class;
    }
}
