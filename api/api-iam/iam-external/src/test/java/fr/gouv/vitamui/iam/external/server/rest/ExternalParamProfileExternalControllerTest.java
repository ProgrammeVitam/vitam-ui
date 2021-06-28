package fr.gouv.vitamui.iam.external.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.ExternalParamProfileExternalService;
import fr.gouv.vitamui.iam.external.server.utils.ApiIamServerUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@WebMvcTest(controllers = { ExternalParamProfileExternalController.class })
public class ExternalParamProfileExternalControllerTest extends ApiIamControllerTest<ExternalParamProfileDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        ExternalParamProfileExternalControllerTest.class);

    @MockBean
    private ExternalParamProfileExternalService externalParamProfileExternalService;

    private ExternalParamProfileExternalController mockedController = MvcUriComponentsBuilder.on(ExternalParamProfileExternalController.class);

    @Test
    public void testPatchExternalParamProfile() {
        LOGGER.debug("testPatchExternalParamProfile");
        super.testPatchEntity();
    }

    @Test
    public void testGetPaginatedExternalParamProfile() {
        LOGGER.debug("testGetPaginatedExternalParamProfile");
        super.testGetPaginatedEntities();
    }

    @Test
    public void testUpdatePaginatedExternalParamProfile() {
        LOGGER.debug("testUpdatePaginatedExternalParamProfile");
        super.testUpdateEntity();
    }

    @Override
    protected ExternalParamProfileDto buildDto() {
        return ApiIamServerUtils.buildExternalParamProfile("id");
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
        return RestApi.V1_EXTERNAL_PARAM_PROFILE_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] {ServicesData.ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE};
    }

    @Override
    protected Class<ExternalParamProfileDto> getDtoClass() {
        return ExternalParamProfileDto.class;
    }

}
