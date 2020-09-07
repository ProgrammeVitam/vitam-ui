package fr.gouv.vitamui.identity.service;

import static org.mockito.ArgumentMatchers.eq;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.external.client.UserExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;
@RunWith(SpringJUnit4ClassRunner.class)
public class UserServiceTest extends UIIdentityServiceTest<UserDto> {

    private UserService service;

    @Mock
    private UserExternalRestClient client;

    @Before
    public void setup() {
        service = new UserService(factory, commonService);
        Mockito.when(factory.getUserExternalRestClient()).thenReturn(client);
    }

    @Test
    public void testCreate() {
        super.createEntite();
    }

    @Test
    public void testUpdate() {
        super.updateEntite();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithIdEmpty() {
        super.updateWithIdEmpty();
    }

    @Test
    public void testGetsUsersByCriteria() {
        //        Mockito.when(clientMock.getUsersByCriteria(anyInt(), anyInt(), anyString(), any(Map.class)))
        //        .thenReturn(new PaginatedValuesDto<>());
        //        final PaginatedValuesDto<BasicUserDto> values = service.getUsersByCriteria(0, 2, null, null);
        //        assertNull("Values should be null", values);
    }

    @Test
    public void testGetUsersFilterByCustomer() {
        //        Mockito.when(clientMock.getUsersByCriteria(anyInt(), anyInt(), anyString(), any(Map.class)))
        //                .thenReturn(new PaginatedValuesDto<>());

        //        final PaginatedValuesDto<BasicUserDto> values = service.getUsersFilterByCustomer("1", 0, 2, "",
        //                new ExternalHttpContext(null, null, null, null));
        //        assertThat(values).isNotNull();

        //        final ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        //        Mockito.verify(clientMock, Mockito.times(1)).getUsersByCriteria(anyInt(), anyInt(), anyString(),
        //                argument.capture());

        //        final Map<String, String> map = argument.getValue();
        //        assertThat(map).isNotNull();
        //        assertThat(map.get("customerId")).isEqualTo("1");
    }

    @Override
    protected UserExternalRestClient getClient() {
        return client;
    }

    @Override
    protected UserDto buildDto(final String id) {
        final UserDto user = new UserDto();
        user.setFirstname("pierre");
        user.setOtp(false);
        user.setEmail("test@blabla.com");
        user.setIdentifier("1234");
        user.setCustomerId("1");
        user.setLastname("nole");
        user.setCustomerId(ID);
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setLanguage("FRENCH");
        user.setLevel("DEV");
        user.setGroupId("123456");
        return user;
    }

    @Override
    protected AbstractCrudService<UserDto> getService() {
        return service;
    }
}
