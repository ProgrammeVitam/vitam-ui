package fr.gouv.vitamui.iam.external.server.rest;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.UserExternalService;
import fr.gouv.vitamui.iam.external.server.utils.ApiIamServerUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { UserExternalController.class })
public class UserExternalControllerTest extends ApiIamControllerTest<UserDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserExternalControllerTest.class);

    @MockBean
    private UserExternalService userExternalService;

    private final UserExternalController userExternalController = MvcUriComponentsBuilder
            .on(UserExternalController.class);

    @Test
    public void updateUserStatus_thenOk() throws Exception {
        LOGGER.debug("testUpdateUserStatus");
        final String id = "iduser";
        final String status = UserStatusEnum.DISABLED.toString();
        final String endpoint = "/" + id;
        final ResultActions result = super.performPatch(endpoint,
                asJsonString(ImmutableMap.of("id", id, "status", status)));
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userExternalController.patch(null, null)));
        Mockito.verify(userExternalService, Mockito.times(1)).patch(ArgumentMatchers.any());
    }

    @Test
    public void findHistoryById_thenOk() throws Exception {
        ResultActions result = super.performGet("/2626/history");
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userExternalController.findHistoryById(null)));
    }

    @Test
    public void getLevels_thenOk() throws Exception {
        LOGGER.debug("testGetLevels");
        ResultActions result = super.performGet(CommonConstants.PATH_LEVELS, ImmutableMap.of(), status().isOk());
        result.andExpect(
                MockMvcResultMatchers.handler().methodCall(userExternalController.getLevels(Optional.empty())));
        Mockito.verify(userExternalService, Mockito.times(1)).getLevels(Optional.empty());
    }

    @Test
    public void patchMe_thenOk() throws Exception {
        LOGGER.debug("testPatchMe");
        ResultActions result = super.performPatch(CommonConstants.PATH_ME, asJsonString(ImmutableMap.of("id", "id")));
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userExternalController.patchMe(null)));
        Mockito.verify(userExternalService, Mockito.times(1)).patchMe(ArgumentMatchers.any());
    }

    @Test
    public void patchAnalytics_thenOk() throws Exception {
        Map<String, Object> analytics = ImmutableMap.of(APPLICATION_ID, "API_SUPERVISION_APP");
        ResultActions result = this.performPost(getUriBuilder(CommonConstants.PATH_ANALYTICS), asJsonString(analytics), status().isOk());
        result.andExpect(MockMvcResultMatchers.handler().methodCall(userExternalController.patchAnalytics(analytics)));
        Mockito.verify(userExternalService).patchAnalytics(analytics);
    }

    @Override
    protected UserDto buildDto() {
        return ApiIamServerUtils.buildUserDto("id");
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
