package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.externalparamprofile.service.ExternalParamProfileService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { ExternalParamProfileController.class })
public class ExternalParamProfileControllerTest extends ApiIamControllerTest<ExternalParamProfileDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalParamProfileControllerTest.class);

    @MockBean
    private ExternalParametersService externalParametersService;

    @MockBean
    private ExternalParamProfileService externalParamProfileService;

    @Test
    public void testCreateExternalParamProfile() {
        LOGGER.debug("testPatchProfile");
        super.testCreateEntity();
    }

    @Test
    public void testPatchExternalParamProfile() {
        LOGGER.debug("testPatchProfile");
        super.testPatchEntity();
    }

    @Test
    public void testGetPaginatedExternalParamProfile() {
        LOGGER.debug("testGetPaginatedExternalParamProfile");
        super.testGetPaginatedEntities();
    }

    @Override
    protected ExternalParamProfileDto buildDto() {
        return IamServerUtilsTest.buildExternalParamProfileDto();
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
        return new String[] {};
    }

    @Override
    protected Class<ExternalParamProfileDto> getDtoClass() {
        return ExternalParamProfileDto.class;
    }
}
