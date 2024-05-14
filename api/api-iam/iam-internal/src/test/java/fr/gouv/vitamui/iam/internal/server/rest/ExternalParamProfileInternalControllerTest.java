package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.externalparamprofile.service.ExternalParamProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { ExternalParamProfileInternalController.class })
public class ExternalParamProfileInternalControllerTest extends ApiIamControllerTest<ExternalParamProfileDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        ExternalParamProfileInternalControllerTest.class
    );

    @MockBean
    private ExternalParametersInternalService externalParametersInternalService;

    @MockBean
    private ExternalParamProfileInternalService externalParamProfileInternalService;

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
    protected VitamUILogger getLog() {
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
