package fr.gouv.vitamui.iam.external.server.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.CasExternalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { CasExternalController.class })
public class CasExternalControllerTest extends ApiIamControllerTest<IdDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasExternalControllerTest.class);

    @MockBean
    private CasExternalService casExternalService;

    private CasExternalController casExternalController = MvcUriComponentsBuilder.on(CasExternalController.class);

    @Test
    public void test_login_isOK() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setPassword("1234");
        loginRequestDto.setLoginEmail("user");
        loginRequestDto.setLoginCustomerId("customerId");

        ResultActions result =
            this.performPost(getUriBuilder(RestApi.CAS_LOGIN_PATH), asJsonString(loginRequestDto), status().isOk());
        result.andExpect(handler().methodCall(casExternalController.login(null)));
        Mockito.verify(casExternalService, Mockito.times(1)).login(ArgumentMatchers.any(LoginRequestDto.class));
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
                    status().is(HttpStatus.BAD_REQUEST.value())
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
                    status().is(HttpStatus.BAD_REQUEST.value())
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
