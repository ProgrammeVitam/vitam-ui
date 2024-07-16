package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInfoInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { UserInfoInternalController.class })
public class UserInfoInternalControllerTest extends ApiIamControllerTest<UserInfoDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoInternalControllerTest.class);

    @MockBean
    private UserInfoInternalService userInfoInternalService;

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
