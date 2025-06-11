package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.cas.service.CasService;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { CasController.class })
public class CasControllerTest extends ApiIamControllerTest<IdDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasControllerTest.class);

    @InjectMocks
    private CasController casController;

    @MockitoBean
    private CasService casService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private IamLogbookService iamLogbookService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(casController)
            .setControllerAdvice(new RestExceptionHandler())
            .build();
    }

    @Test
    public void test_login_withMissingLoginEmail() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setPassword("1234");
        loginRequestDto.setLoginEmail(null);
        loginRequestDto.setLoginCustomerId("customerId");

        ResultActions result =
            this.performPost(
                    getUriBuilder(RestApi.CAS_LOGIN_PATH),
                    asJsonString(loginRequestDto),
                    status().isBadRequest()
                );
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("exception", "fr.gouv.vitamui.commons.api.exception.BadRequestException");
        expectedResult.put("error", "apierror.badrequest");
        expectedResult.put("status", HttpStatus.BAD_REQUEST.value());
        result.andExpect(content().json(asJsonString(expectedResult), false));
    }

    @Test
    public void test_login_withMissingCustomerId() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setPassword("1234");
        loginRequestDto.setLoginEmail("user@email.com");
        loginRequestDto.setLoginCustomerId(null);

        ResultActions result =
            this.performPost(
                    getUriBuilder(RestApi.CAS_LOGIN_PATH),
                    asJsonString(loginRequestDto),
                    status().isBadRequest()
                );
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("exception", "fr.gouv.vitamui.commons.api.exception.BadRequestException");
        expectedResult.put("error", "apierror.badrequest");
        expectedResult.put("status", HttpStatus.BAD_REQUEST.value());
        result.andExpect(content().json(asJsonString(expectedResult), false));
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
        return RestApi.V1_CAS_URL;
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
