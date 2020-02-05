package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.cas.service.CasInternalService;
import fr.gouv.vitamui.iam.internal.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { CasInternalController.class })
public class CasInternalControllerTest extends ApiIamControllerTest<IdDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CasInternalControllerTest.class);

    @MockBean
    private CasInternalService casInternalService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserInternalService internalUserService;

    @MockBean
    private IamLogbookService iamLogbookService;

    @Test
    public void testDeleteSubrogation() {
        super.performDelete(RestApi.CAS_SUBROGATIONS_PATH, ImmutableMap.of("superUser", "julien@vitamui.com", "surrogate", "pierre@vitamui.com"));
    }

    @Override
    protected IdDto buildDto() {
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
        return RestApi.V1_CAS_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.ROLE_CAS_SUBROGATIONS, ServicesData.ROLE_CAS_LOGIN, ServicesData.ROLE_CAS_CHANGE_PASSWORD,
                ServicesData.ROLE_CAS_USERS };
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return IdDto.class;
    }
}
