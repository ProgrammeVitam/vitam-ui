package fr.gouv.vitamui.iam.external.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.SubrogationExternalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { SubrogationExternalController.class })
public class SubrogationExternalControllerTest extends ApiIamControllerTest<SubrogationDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubrogationExternalControllerTest.class);

    @MockBean
    private SubrogationExternalService subrogationExternalService;

    @Test
    public void testDeleteSubrogation() {
        super.performDelete(
            RestApi.CAS_SUBROGATIONS_PATH,
            ImmutableMap.of("superUser", "julien@vitamui.com", "surrogate", "pierre@vitamui.com")
        );
    }

    @Override
    protected SubrogationDto buildDto() {
        return null;
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

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
            ServicesData.ROLE_DELETE_SUBROGATIONS,
        };
    }

    @Override
    protected Class<SubrogationDto> getDtoClass() {
        return SubrogationDto.class;
    }
}
