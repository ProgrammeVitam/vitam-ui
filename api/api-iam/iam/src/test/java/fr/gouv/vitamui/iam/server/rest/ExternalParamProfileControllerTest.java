package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.externalparamprofile.service.ExternalParamProfileService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = { ExternalParamProfileController.class })
public class ExternalParamProfileControllerTest extends ApiIamControllerTest<ExternalParamProfileDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalParamProfileControllerTest.class);

    @MockitoBean
    private ExternalParametersService externalParametersService;

    @MockitoBean
    private ExternalParamProfileService externalParamProfileService;

    @Test
    public void testCreateExternalParamProfile() {
        LOGGER.debug("testCreateExternalParamProfile");
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
        return new String[] { ServicesData.ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE };
    }

    @Override
    protected Class<ExternalParamProfileDto> getDtoClass() {
        return ExternalParamProfileDto.class;
    }
}
