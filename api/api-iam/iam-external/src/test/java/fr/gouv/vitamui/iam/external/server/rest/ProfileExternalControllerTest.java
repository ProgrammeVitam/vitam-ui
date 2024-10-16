package fr.gouv.vitamui.iam.external.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.ProfileExternalService;
import fr.gouv.vitamui.iam.external.server.utils.ApiIamServerUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { ProfileExternalController.class })
public class ProfileExternalControllerTest extends ApiIamControllerTest<ProfileDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileExternalControllerTest.class);

    @MockBean
    private ProfileExternalService profileExternalService;

    private ProfileExternalController mockedController = MvcUriComponentsBuilder.on(ProfileExternalController.class);

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
    public void testGetLevels() throws Exception {
        LOGGER.debug("testGetLevels");
        ResultActions result = super.performGet(CommonConstants.PATH_LEVELS, ImmutableMap.of(), status().isOk());
        result.andExpect(MockMvcResultMatchers.handler().methodCall(mockedController.getLevels(Optional.empty())));
        Mockito.verify(profileExternalService, Mockito.times(1)).getLevels(Optional.empty());
    }

    @Override
    protected ProfileDto buildDto() {
        return ApiIamServerUtils.buildProfileDto("id");
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
