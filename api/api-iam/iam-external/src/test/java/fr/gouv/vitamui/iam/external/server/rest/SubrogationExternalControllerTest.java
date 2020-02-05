package fr.gouv.vitamui.iam.external.server.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.SubrogationExternalService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { SubrogationExternalController.class })
public class SubrogationExternalControllerTest extends ApiIamControllerTest<SubrogationDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SubrogationExternalControllerTest.class);

    @MockBean
    private SubrogationExternalService subrogationExternalService;

    @Test
    public void testDeleteSubrogation() {
        super.performDelete(RestApi.CAS_SUBROGATIONS_PATH,
                ImmutableMap.of("superUser", "julien@vitamui.com", "surrogate", "pierre@vitamui.com"));
    }

    @Override
    protected SubrogationDto buildDto() {
        return null;
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
        return RestApi.V1_SUBROGATIONS_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] {
                ServicesData.ROLE_GET_SUBROGATIONS,
                ServicesData.ROLE_CREATE_SUBROGATIONS,
                ServicesData.ROLE_GET_USERS_SUBROGATIONS,
                ServicesData.ROLE_GET_GROUPS_SUBROGATIONS,
                ServicesData.ROLE_DELETE_SUBROGATIONS};
    }

    @Override
    protected Class<SubrogationDto> getDtoClass() {
        return SubrogationDto.class;
    }

}
