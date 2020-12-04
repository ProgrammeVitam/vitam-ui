package fr.gouv.vitamui.identity.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.identity.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { UserController.class })
public class UserControllerTest extends UiIdentityRestControllerTest<UserDto> {

    @Value("${ui-identity.prefix}")
    protected String apiUrl;

    private final String PREFIX = "/users";

    @MockBean
    private UserService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserControllerTest.class);

    @Test
    public void testCreateUser() {
        super.testCreateEntity();
    }

    @MockBean
    private BuildProperties buildProperties;


    @Test
    public void testUpdateUser() {
        super.testUpdateEntity();
    }

    @Test
    public void testGetUserById() {
        super.testGetEntityById();
    }

    @Test
    public void testFindUsersByCriteria() {
        LOGGER.debug("getAllPaginated");
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.X_TENANT_ID_HEADER, "1");
        super.performGet("/archive", ImmutableMap.of("page", 1, "size", 20, "orderBy", "id"), headers);
    }

    @Test
    public void testFindUsersByBadHeaderValueThenReturnBadRequest() {
        LOGGER.debug("testFindUsersByBadHeaderValueThenReturnBadRequest");
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.X_TENANT_ID_HEADER, "%ds#</><!-sdq");
        super.performGet("/archive", ImmutableMap.of("page", 1, "size", 20, "orderBy", "id"), headers, status().isBadRequest());
    }

    @Test
    public void testCheckExistByEmail() {
        LOGGER.debug("testCheckExistByEmail");
        Mockito.when(service.checkExist(any(), any())).thenReturn(true);
        final QueryDto criteria = QueryDto.criteria().addCriterion("email", "email@test.fr",
                CriterionOperator.EQUALS);
        super.performGet(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()));
    }

    @Test
    public void testCheckExistByEmailNotFound() {
        LOGGER.debug("testCheckExistByEmail");
        Mockito.when(service.checkExist(any(), any())).thenReturn(false);
        final QueryDto criteria = QueryDto.criteria().addCriterion("email", "email@test.fr",
                CriterionOperator.EQUALS);
        super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()),
                status().isNoContent());
    }

    @Test
    public void testCheckExistByTotoWithBadCriteraiThenReturnBadRequest() {
        LOGGER.debug("testCheckExistByTotoWithBadCriteraiThenReturnBadRequest");
        Mockito.when(service.checkExist(any(), any())).thenReturn(false);
        final QueryDto criteria = QueryDto.criteria().addCriterion("toto<s></s>", "titi",
            CriterionOperator.EQUALS);
        super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()),
            status().isBadRequest());
    }

    @Test
    public void testGetLevels() {
        LOGGER.debug("testGetLevels");
        super.performGet(CommonConstants.PATH_LEVELS, ImmutableMap.of(),
                status().isOk());
    }

    @Test
    public void logbook_when_call_return_ok() {
        super.performGet("/1/history");
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected Class<UserDto> getDtoClass() {
        return UserDto.class;
    }

    @Override
    protected UserDto buildDto() {
        return new UserDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.create(any(), any(UserDto.class))).thenReturn(new UserDto());
        Mockito.when(service.update(any(), any(UserDto.class))).thenReturn(new UserDto());
        Mockito.when(service.getOne(any(), any(), any())).thenReturn(new UserDto());
    }
}
