package fr.gouv.vitamui.iam.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { ProfileController.class })
public class ProfileControllerTest extends ApiIamControllerTest<ProfileDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileControllerTest.class);

    @MockitoBean
    private ProfileService profileService;

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
        super.performGet(CommonConstants.PATH_LEVELS, ImmutableMap.of(), status().isOk());
        Mockito.verify(profileService, Mockito.times(1)).getLevels(ArgumentMatchers.any());
    }

    @Override
    protected ProfileDto buildDto() {
        return IamServerUtilsTest.buildProfileDto();
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_PROFILES_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.SERVICE_PROFILES };
    }

    @Override
    protected Class<ProfileDto> getDtoClass() {
        return ProfileDto.class;
    }
}
