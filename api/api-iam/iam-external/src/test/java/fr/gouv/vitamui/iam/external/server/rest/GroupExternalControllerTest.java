package fr.gouv.vitamui.iam.external.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.GroupExternalService;
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
@WebMvcTest(controllers = { GroupExternalController.class })
public class GroupExternalControllerTest extends ApiIamControllerTest<GroupDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupExternalControllerTest.class);

    @MockBean
    private GroupExternalService service;

    private GroupExternalController mockedController = MvcUriComponentsBuilder.on(GroupExternalController.class);

    @Test
    public void testGetAllGroups() {
        LOGGER.debug("testGetAllEntity");
        super.testGetAllEntityWithCriteria();
    }

    @Test
    public void testPatchGroup() {
        LOGGER.debug("testPatchGroup");
        super.testPatchEntity();
    }

    @Test
    public void testGetPaginatedGroup() {
        LOGGER.debug("testGetPaginatedGroup");
        super.testGetPaginatedEntities();
    }

    @Test
    public void testGetLevels() throws Exception {
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
