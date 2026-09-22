package fr.gouv.vitamui.iam.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.auth.contract.AuthContractApi;
import fr.gouv.vitamui.iam.auth.contract.LoginRequestDto;
import fr.gouv.vitamui.iam.server.cas.service.CasService;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { CasController.class })
class CasInternalControllerTest extends ApiIamControllerTest<IdDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasInternalControllerTest.class);

    @MockitoBean
    private CasService casService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private IamLogbookService iamLogbookService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private CasController casController = MvcUriComponentsBuilder.on(CasController.class);

    @Test
    void test_login_isOK() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setPassword("1234");
        loginRequestDto.setLoginEmail("user");
        loginRequestDto.setLoginCustomerId("customerId");

        User user = new User();
        user.setEmail(loginRequestDto.getLoginEmail());
        user.setPassword(passwordEncoder.encode(loginRequestDto.getPassword()));
        user.setStatus(UserStatusEnum.ENABLED);
        Mockito.when(casService.findUserByEmailAndCustomerId(anyString(), anyString())).thenReturn(user);

        ResultActions result =
            this.performPost(getUriBuilder(AuthContractApi.LOGIN_PATH), asJsonString(loginRequestDto), status().isOk());
        result.andExpect(handler().methodCall(casController.login(null)));
        Mockito.verify(casService, Mockito.times(1)).findUserByEmailAndCustomerId(anyString(), anyString());
    }

    @Test
    void testDeleteSubrogation() {
        super.performDelete(
            AuthContractApi.SUBROGATIONS_PATH,
            ImmutableMap.of("superUser", "julien@vitamui.com", "surrogate", "pierre@vitamui.com")
        );
    }

    @Override
    protected IdDto buildDto() {
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
        return AuthContractApi.V1_AUTH_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] {
            ServicesData.ROLE_CAS_SUBROGATIONS,
            ServicesData.ROLE_CAS_LOGIN,
            ServicesData.ROLE_CAS_CHANGE_PASSWORD,
            ServicesData.ROLE_CAS_USERS,
        };
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return IdDto.class;
    }
}
