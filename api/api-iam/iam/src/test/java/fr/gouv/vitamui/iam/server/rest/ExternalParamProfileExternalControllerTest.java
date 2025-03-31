package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.externalparamprofile.service.ExternalParamProfileService;
import fr.gouv.vitamui.iam.server.utils.ApiIamServerUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { ExternalParamProfileController.class })
public class ExternalParamProfileExternalControllerTest extends ApiIamControllerTest<ExternalParamProfileDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalParamProfileExternalControllerTest.class);

    @Autowired
    private ExternalParamProfileController externalParamProfileController;

    @MockBean
    ExternalParamProfileService externalParamProfileService;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(externalParamProfileController)
            .setControllerAdvice(new RestExceptionHandler())
            .build();
    }

    @Test
    @WithMockUser(roles = "SEARCH_EXTERNAL_PARAM_PROFILE")
    public void testGetPaginatedExternalParamProfile() {
        LOGGER.debug("testGetPaginatedExternalParamProfile");
        doReturn(new PaginatedValuesDto<>())
            .when(externalParamProfileService)
            .getAllPaginated(any(), any(), any(), any(), any());
        super.testGetPaginatedEntities();
    }

    @Test(expected = AssertionError.class)
    @WithMockUser(roles = "EDIT_EXTERNAL_PARAM_PROFILE")
    public void testUpdatePaginatedExternalParamProfile() {
        LOGGER.debug("testUpdatePaginatedExternalParamProfile");
        super.testUpdateEntity();
    }

    @Override
    protected ExternalParamProfileDto buildDto() {
        return ApiIamServerUtils.buildExternalParamProfile("id");
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_EXTERNAL_PARAM_PROFILE_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE };
    }

    @Override
    protected Class<ExternalParamProfileDto> getDtoClass() {
        return ExternalParamProfileDto.class;
    }
}
