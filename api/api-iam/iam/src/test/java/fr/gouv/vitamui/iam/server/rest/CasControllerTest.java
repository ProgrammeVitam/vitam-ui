package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.cas.service.CasService;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { CasController.class })
class CasControllerTest extends ApiIamControllerTest<IdDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasControllerTest.class);

    private static final String LOGIN_EMAIL = "user@email.com";
    private static final String CUSTOMER_ID = "customerId";

    @Autowired
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
    void test_login_withMissingLoginEmail() throws Exception {
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
    void test_login_withMissingCustomerId() throws Exception {
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

    @Test
    @WithMockUser(roles = "CAS_LOGIN")
    void test_login_withExpiredPassword_returnsMustChangePassword() throws Exception {
        givenAuthenticatedUserWithPasswordExpiringAt(OffsetDateTime.now().minusDays(1));

        performLogin().andExpect(jsonPath("$.mustChangePassword").value(true));
    }

    @Test
    @WithMockUser(roles = "CAS_LOGIN")
    void test_login_withValidPassword_doesNotRequireChange() throws Exception {
        givenAuthenticatedUserWithPasswordExpiringAt(OffsetDateTime.now().plusDays(1));

        performLogin().andExpect(jsonPath("$.mustChangePassword").value(false));
    }

    @Test
    @WithMockUser(roles = "CAS_LOGIN")
    void test_login_withoutExpirationDate_requiresChange() throws Exception {
        givenAuthenticatedUserWithPasswordExpiringAt(null);

        performLogin().andExpect(jsonPath("$.mustChangePassword").value(true));
    }

    private void givenAuthenticatedUserWithPasswordExpiringAt(final OffsetDateTime expirationDate) {
        final User user = new User();
        user.setId("id");
        user.setEmail(LOGIN_EMAIL);
        user.setCustomerId(CUSTOMER_ID);
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setPassword("hash");
        user.setNbFailedAttempts(0);
        user.setPasswordExpirationDate(expirationDate);

        final UserDto userDto = new UserDto();
        userDto.setEmail(LOGIN_EMAIL);
        userDto.setCustomerId(CUSTOMER_ID);
        userDto.setPasswordExpirationDate(expirationDate);

        when(casService.findUserByEmailAndCustomerId(LOGIN_EMAIL, CUSTOMER_ID)).thenReturn(user);
        when(casService.getTimeIntervalForLoginAttempts()).thenReturn(20);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(userService.internalConvertFromEntityToDto(user)).thenReturn(userDto);
    }

    private ResultActions performLogin() throws Exception {
        final LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setPassword("1234");
        loginRequestDto.setLoginEmail(LOGIN_EMAIL);
        loginRequestDto.setLoginCustomerId(CUSTOMER_ID);

        return this.performPost(getUriBuilder(RestApi.CAS_LOGIN_PATH), asJsonString(loginRequestDto), status().isOk());
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
