package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.api.utils.CasJsonWrapper;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link UserPrincipalResolver}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class UserPrincipalResolverTest {

    private static final String USERNAME = "jleleu@test.com";

    private static final String ID = "1234";

    private static final String ROLE_NAME = "role1";

    private UserPrincipalResolver resolver;

    private CasExternalRestClient casExternalRestClient;

    @Before
    public void setUp() {
        resolver = new UserPrincipalResolver();
        casExternalRestClient = mock(CasExternalRestClient.class);
        resolver.setCasExternalRestClient(casExternalRestClient);
        resolver.setPrincipalFactory(new DefaultPrincipalFactory());
        final Utils utils = new Utils(casExternalRestClient, null);
        resolver.setUtils(utils);
        final RequestContext context = mock(RequestContext.class);
        RequestContextHolder.setRequestContext(context);
    }

    @Test
    public void testResolveUserSuccessfully() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
                eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));

        final Principal principal = resolver.resolve(USERNAME, new HashMap<>());
        assertEquals(ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(Arrays.asList(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
    }

    @Test public void testResolveAddressDeserializSuccessfully() {
        AuthUserDto authUserDto = userProfile(UserStatusEnum.ENABLED);
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME), eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
                .thenReturn(authUserDto);

        final Principal principal = resolver.resolve(USERNAME, new HashMap<>());

        AddressDto addressDto = (AddressDto) ((CasJsonWrapper) principal.getAttributes().get(CommonConstants.ADDRESS_ATTRIBUTE).get(0)).getData();

        assertEquals(ID, principal.getId());
        assertThat(addressDto).isEqualToComparingFieldByField(authUserDto.getAddress());
    }

    @Test
    public void testNoUser() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
                eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(null);

        assertNull(resolver.resolve(USERNAME, new HashMap<>()));
    }

    @Test
    public void testDisabledUser() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
                eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
                        .thenReturn(userProfile(UserStatusEnum.DISABLED));

        assertNull(resolver.resolve(USERNAME, new HashMap<>()));
    }

    @Test
    public void testUserCannotLogin() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
                eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.BLOCKED));

        assertNull(resolver.resolve(USERNAME, new HashMap<>()));
    }

    private AuthUserDto userProfile(final UserStatusEnum status) {
        final AuthUserDto user = new AuthUserDto();
        user.setId(ID);
        user.setStatus(status);
        user.setType(UserTypeEnum.NOMINATIVE);
        AddressDto address = new AddressDto();
        address.setStreet("73 rue du faubourg poissonnière");
        address.setZipCode("75009");
        address.setCity("Paris");
        address.setCountry("France");
        user.setAddress(address);
        final ProfileDto profile = new ProfileDto();
        profile.setRoles(Arrays.asList(new Role(ROLE_NAME)));
        final GroupDto group = new GroupDto();
        group.setProfiles(Arrays.asList(profile));
        user.setProfileGroup(group);
        user.setCustomerId("customerId");
        return user;
    }
}
