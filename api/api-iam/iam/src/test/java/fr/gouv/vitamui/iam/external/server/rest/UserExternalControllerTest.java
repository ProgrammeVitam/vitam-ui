package fr.gouv.vitamui.iam.external.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.external.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.external.server.user.service.UserService;
import fr.gouv.vitamui.iam.external.server.utils.ApiIamServerUtils;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import org.junit.Test;
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

import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { UserController.class })
public class UserExternalControllerTest extends ApiIamControllerTest<UserDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserExternalControllerTest.class);

    @MockBean
    private ExternalSecurityService externalSecurityService;

    @MockBean
    private UserService userService;

    @MockBean
    private ConnectionHistoryService connectionHistoryService;

    private final UserController userController = MvcUriComponentsBuilder.on(UserController.class);

    @Test
    public void updateUserStatus_thenOk() throws Exception {
        LOGGER.debug("testUpdateUserStatus");
        final String id = "iduser";
        final String status = UserStatusEnum.DISABLED.toString();
        final String endpoint = "/" + id;
        final ResultActions result = super.performPatch(
            endpoint,
            asJsonString(ImmutableMap.of("id", id, "status", status))
        );
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userController.patch(null, null)));
        Mockito.verify(userService, Mockito.times(1)).patch(ArgumentMatchers.any());
    }

    @Test
    public void findHistoryById_thenOk() throws Exception {
        ResultActions result = super.performGet("/2626/history");
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userController.findHistoryById(null)));
    }

    @Test
    public void getLevels_thenOk() throws Exception {
        LOGGER.debug("testGetLevels");
        ResultActions result = super.performGet(CommonConstants.PATH_LEVELS, ImmutableMap.of(), status().isOk());
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userController.getLevels(Optional.empty())));
        Mockito.verify(userService, Mockito.times(1)).getLevels(Optional.empty());
    }

    @Test
    public void patchMe_thenOk() throws Exception {
        LOGGER.debug("testPatchMe");
        ResultActions result = super.performPatch(CommonConstants.PATH_ME, asJsonString(ImmutableMap.of("id", "id")));
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userController.patchMe(null)));
        Mockito.verify(userService, Mockito.times(1)).patchMe(ArgumentMatchers.any());
    }

    @Test
    public void patchAnalytics_thenOk() throws Exception {
        Map<String, Object> analytics = ImmutableMap.of(APPLICATION_ID, "API_SUPERVISION_APP");
        ResultActions result =
            this.performPost(getUriBuilder(CommonConstants.PATH_ANALYTICS), asJsonString(analytics), status().isOk());
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userController.patchAnalytics(analytics)));
        Mockito.verify(userService).patchAnalytics(analytics);
    }

    @Override
    protected UserDto buildDto() {
        return ApiIamServerUtils.buildUserDto("id");
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_USERS_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.SERVICE_USERS };
    }

    @Override
    protected Class<UserDto> getDtoClass() {
        return UserDto.class;
    }
}
