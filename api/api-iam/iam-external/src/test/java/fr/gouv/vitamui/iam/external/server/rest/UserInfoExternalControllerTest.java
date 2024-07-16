package fr.gouv.vitamui.iam.external.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.UserInfoExternalService;
import fr.gouv.vitamui.iam.external.server.utils.ApiIamServerUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { UserInfoExternalController.class })
class UserInfoExternalControllerTest extends ApiIamControllerTest<UserInfoDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoExternalControllerTest.class);

    @MockBean
    private UserInfoExternalService userExternalService;

    private final UserInfoExternalController userExternalController = MvcUriComponentsBuilder.on(
        UserInfoExternalController.class
    );

    @Test
    void test_patch_should_be_Ok() throws Exception {
        final String id = "iduser";
        final String endpoint = "/" + id;
        final ResultActions result = super.performPatch(
            endpoint,
            asJsonString(ImmutableMap.of("id", id, "language", "fr"))
        );
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userExternalController.patch(null, null)));
        Mockito.verify(userExternalService, Mockito.times(1)).patch(ArgumentMatchers.any());
    }

    @Override
    protected UserInfoDto buildDto() {
        return ApiIamServerUtils.buildUserInfoDto("id");
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
        return new String[] { ServicesData.SERVICE_USER_INFOS };
    }

    @Override
    protected Class<UserInfoDto> getDtoClass() {
        return UserInfoDto.class;
    }
}
