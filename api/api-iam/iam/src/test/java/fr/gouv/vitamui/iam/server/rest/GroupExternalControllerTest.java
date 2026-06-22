package fr.gouv.vitamui.iam.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { GroupController.class })
class GroupExternalControllerTest extends ApiIamControllerTest<GroupDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupControllerTest.class);

    @MockitoBean
    private GroupService service;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private SignedDownloadTokenService signedDownloadTokenService;

    private GroupController mockedController = MvcUriComponentsBuilder.on(GroupController.class);

    @Test
    void testGetAllGroups() {
        LOGGER.debug("testGetAllEntity");
        super.testGetAllEntityWithCriteria();
    }

    @Test
    void testPatchGroup() {
        LOGGER.debug("testPatchGroup");
        super.testPatchEntity();
    }

    @Test
    void testGetPaginatedGroup() {
        LOGGER.debug("testGetPaginatedGroup");
        super.testGetPaginatedEntities();
    }

    @Test
    void testGetLevels() throws Exception {
        LOGGER.debug("testGetLevels");
        ResultActions result = super.performGet(CommonConstants.PATH_LEVELS, ImmutableMap.of(), status().isOk());
        result.andExpect(MockMvcResultMatchers.handler().methodCall(mockedController.getLevels(Optional.empty())));
        Mockito.verify(service, Mockito.times(1)).getLevels(Optional.empty());
    }

    @Override
    protected GroupDto buildDto() {
        return new GroupDto();
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_GROUPS_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.SERVICE_GROUPS };
    }

    @Override
    protected Class<GroupDto> getDtoClass() {
        return GroupDto.class;
    }
}
