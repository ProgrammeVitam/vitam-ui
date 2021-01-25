package fr.gouv.vitamui.ui.commons.service;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import fr.gouv.vitamui.commons.api.CommonConstants;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.external.client.UserExternalRestClient;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserServiceTest extends ServiceTest<UserDto> {

    @InjectMocks
    private UserService service;

    @Mock
    private UserExternalRestClient client;

    @Before
    public void setup() {
        when(factory.getUserExternalRestClient()).thenReturn(client);
        service = new UserService(factory);
    }

    @Test
    public void testPatchAnalytics() {
        Map<String, Object> analytics = Map.of(APPLICATION_ID, "HIERARCHY_PROFILE_APP");
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        UserDto user = buildDto("5");
        when(client.patchAnalytics(any(), any())).thenReturn(user);

        UserDto result = service.patchAnalytics(context, analytics);

        final ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(getClient()).patchAnalytics(eq(context), argument.capture());
        assertThat(argument.getValue()).isEqualTo(analytics);
        assertThat( result).isEqualTo(user);
    }

    @Override
    protected UserExternalRestClient getClient() {
        return client;
    }

    @Override
    protected UserDto buildDto(final String id) {
        final UserDto user = new UserDto();
        user.setFirstname("YB");
        user.setOtp(true);
        user.setEmail("test@test.com");
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
