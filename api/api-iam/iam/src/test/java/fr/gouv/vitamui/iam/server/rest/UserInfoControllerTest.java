package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = { UserInfoController.class })
class UserInfoControllerTest extends ApiIamControllerTest<UserInfoDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoControllerTest.class);

    @MockitoBean
    private UserInfoService userInfoService;

    @Test
    void testGetAllProfiles() {
        LOGGER.debug("testGetAllEntity");
        super.testGetAllEntityWithCriteria();
    }

    @Test
    void testPatchProfile() {
        LOGGER.debug("testPatchProfile");
        super.testPatchEntity();
    }

    @Test
    void testGetPaginatedProfile() {
        LOGGER.debug("testGetPaginatedProfile");
        super.testGetPaginatedEntities();
    }

    @Override
    protected UserInfoDto buildDto() {
        return IamServerUtilsTest.buildUserInfoDto();
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_USERS_INFO_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] {};
    }

    @Override
    protected Class<UserInfoDto> getDtoClass() {
        return UserInfoDto.class;
    }
}
