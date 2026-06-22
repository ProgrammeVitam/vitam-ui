package fr.gouv.vitamui.iam.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import fr.gouv.vitamui.iam.server.utils.ApiIamServerUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { UserController.class })
class UserExternalControllerTest extends ApiIamControllerTest<UserDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserExternalControllerTest.class);

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ConnectionHistoryService connectionHistoryService;

    @MockitoBean
    private SignedDownloadTokenService signedDownloadTokenService;

    private final UserController userController = MvcUriComponentsBuilder.on(UserController.class);

    @Test
    void updateUserStatus_thenOk() throws Exception {
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
    void findHistoryById_thenOk() throws Exception {
        ResultActions result = super.performGet("/2626/history");
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userController.findHistoryById(null)));
    }

    @Test
    void getLevels_thenOk() throws Exception {
        LOGGER.debug("testGetLevels");
        ResultActions result = super.performGet(CommonConstants.PATH_LEVELS, ImmutableMap.of(), status().isOk());
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userController.getLevels(Optional.empty())));
        Mockito.verify(userService, Mockito.times(1)).getLevels(Optional.empty());
    }

    @Test
    void patchAnalytics_thenOk() throws Exception {
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
