package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.BaseWebflowActionTest;
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
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;

/**
 * Tests {@link UserPrincipalResolver}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class UserPrincipalResolverTest extends BaseWebflowActionTest {

    private static final String PROVIDER = "google";
    private static final String MAIL = "mail";

    private static final String USERNAME = "jleleu@test.com";
    private static final String ADMIN = "admin@test.com";

    private static final String PWD = "password";

    private static final String USERNAME_ID = "jleleu";
    private static final String ADMIN_ID = "admin";

    private static final String ROLE_NAME = "role1";

    private UserPrincipalResolver resolver;

    private CasExternalRestClient casExternalRestClient;

    private PrincipalFactory principalFactory;

    private SessionStore sessionStore;

    private IdentityProviderHelper identityProviderHelper;

    private ProvidersService providersService;

    @Before
    public void setUp() {
        super.setUp();

        casExternalRestClient = mock(CasExternalRestClient.class);
        val utils = new Utils(null, 0, null, null);
        principalFactory = new DefaultPrincipalFactory();
        sessionStore = mock(SessionStore.class);
        identityProviderHelper = mock(IdentityProviderHelper.class);
        providersService = mock(ProvidersService.class);
        resolver = new UserPrincipalResolver(principalFactory, casExternalRestClient, utils, sessionStore,
            identityProviderHelper, providersService);
    }

    @Test
    public void testResolveUserSuccessfully() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
                eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));

        val principal = resolver.resolve(new UsernamePasswordCredential(USERNAME, PWD),
            Optional.of(principalFactory.createPrincipal(USERNAME)), Optional.empty());

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(Arrays.asList(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegation() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.empty());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.PROVIDER_TECHNICAL_NAME))).thenReturn(Optional.of(PROVIDER));
        when(providersService.getProviders()).thenReturn(new ArrayList<>());
        when(identityProviderHelper.findByTechnicalName(any(List.class), eq(PROVIDER))).thenReturn(Optional.of(new IdentityProviderDto()));

        val principal = resolver.resolve(new ClientCredential(),
            Optional.of(principalFactory.createPrincipal(USERNAME)), Optional.empty());

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(Arrays.asList(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegationMailAttribute() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.empty());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.PROVIDER_TECHNICAL_NAME))).thenReturn(Optional.of(PROVIDER));
        when(providersService.getProviders()).thenReturn(new ArrayList<>());
        val provider = new IdentityProviderDto();
        provider.setMailAttribute(MAIL);
        when(identityProviderHelper.findByTechnicalName(any(List.class), eq(PROVIDER))).thenReturn(Optional.of(provider));

        val princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(MAIL, Collections.singletonList(USERNAME));

        val principal = resolver.resolve(new ClientCredential(),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)), Optional.empty());

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(Arrays.asList(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegationMailAttributeNoValue() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.empty());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.PROVIDER_TECHNICAL_NAME))).thenReturn(Optional.of(PROVIDER));
        when(providersService.getProviders()).thenReturn(new ArrayList<>());
        val provider = new IdentityProviderDto();
        provider.setMailAttribute(MAIL);
        when(identityProviderHelper.findByTechnicalName(any(List.class), eq(PROVIDER))).thenReturn(Optional.of(provider));

        val princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(MAIL, Collections.emptyList());

        val principal = resolver.resolve(new ClientCredential(),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)), Optional.empty());

        assertEquals("nobody", principal.getId());
    }

    @Test
    public void testResolveSurrogateUser() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(ADMIN),
            eq(Optional.empty()))).thenReturn(adminProfile());

        val credential = new SurrogateUsernamePasswordCredential();
        credential.setUsername(ADMIN);
        credential.setSurrogateUsername(USERNAME);
        val principal = resolver.resolve(credential, Optional.of(principalFactory.createPrincipal(ADMIN)), Optional.empty());

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(Arrays.asList(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(ADMIN, attributes.get(SUPER_USER_ATTRIBUTE).get(0));
    }

    @Test
    public void testResolveAuthnDelegationSurrogate() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(ADMIN),
            eq(Optional.empty()))).thenReturn(adminProfile());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.of(USERNAME));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.PROVIDER_TECHNICAL_NAME))).thenReturn(Optional.of(PROVIDER));
        when(providersService.getProviders()).thenReturn(new ArrayList<>());
        when(identityProviderHelper.findByTechnicalName(any(List.class), eq(PROVIDER))).thenReturn(Optional.of(new IdentityProviderDto()));

        val  principal = resolver.resolve(new ClientCredential(), Optional.of(principalFactory.createPrincipal(ADMIN)), Optional.empty());

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(Arrays.asList(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(ADMIN, attributes.get(SUPER_USER_ATTRIBUTE).get(0));
    }

    @Test
    public void testResolveAuthnDelegationSurrogateMailAttribute() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(ADMIN),
            eq(Optional.empty()))).thenReturn(adminProfile());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.of(USERNAME));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.PROVIDER_TECHNICAL_NAME))).thenReturn(Optional.of(PROVIDER));
        when(providersService.getProviders()).thenReturn(new ArrayList<>());
        val provider = new IdentityProviderDto();
        provider.setMailAttribute(MAIL);
        when(identityProviderHelper.findByTechnicalName(any(List.class), eq(PROVIDER))).thenReturn(Optional.of(provider));

        val princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(MAIL, Collections.singletonList(ADMIN));

        val  principal = resolver.resolve(new ClientCredential(), Optional.of(principalFactory.createPrincipal("fake", princAttributes)), Optional.empty());

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(Arrays.asList(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(ADMIN, attributes.get(SUPER_USER_ATTRIBUTE).get(0));
    }

    @Test
    public void testResolveAuthnDelegationSurrogateMailAttributeNoMail() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(ADMIN),
            eq(Optional.empty()))).thenReturn(adminProfile());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.of(USERNAME));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.PROVIDER_TECHNICAL_NAME))).thenReturn(Optional.of(PROVIDER));
        when(providersService.getProviders()).thenReturn(new ArrayList<>());
        val provider = new IdentityProviderDto();
        provider.setMailAttribute(MAIL);
        when(identityProviderHelper.findByTechnicalName(any(List.class), eq(PROVIDER))).thenReturn(Optional.of(provider));

        val  principal = resolver.resolve(new ClientCredential(), Optional.of(principalFactory.createPrincipal("fake")), Optional.empty());

        assertEquals("nobody", principal.getId());
    }

    @Test
    public void testResolveAddressDeserializSuccessfully() {
        AuthUserDto authUserDto = userProfile(UserStatusEnum.ENABLED);
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME), eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
                .thenReturn(authUserDto);

        val principal = resolver.resolve(new UsernamePasswordCredential(USERNAME, PWD),
            Optional.of(principalFactory.createPrincipal(USERNAME)), Optional.empty());

        assertEquals(USERNAME_ID, principal.getId());
        AddressDto addressDto = (AddressDto) ((CasJsonWrapper) principal.getAttributes().get(CommonConstants.ADDRESS_ATTRIBUTE).get(0)).getData();
        assertThat(addressDto).isEqualToComparingFieldByField(authUserDto.getAddress());
        assertNull(principal.getAttributes().get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testNoUser() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
                eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(null);

        assertNull(resolver.resolve(new UsernamePasswordCredential(USERNAME, PWD),
            Optional.of(principalFactory.createPrincipal(USERNAME)), Optional.empty()));
    }

    @Test
    public void testDisabledUser() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
                eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
                        .thenReturn(userProfile(UserStatusEnum.DISABLED));

        assertNull(resolver.resolve(new UsernamePasswordCredential(USERNAME, PWD),
            Optional.of(principalFactory.createPrincipal(USERNAME)), Optional.empty()));
    }

    @Test
    public void testUserCannotLogin() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class), eq(USERNAME),
                eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.BLOCKED));

        assertNull(resolver.resolve(new UsernamePasswordCredential(USERNAME, PWD),
            Optional.of(principalFactory.createPrincipal(USERNAME)), Optional.empty()));
    }

    private AuthUserDto adminProfile() {
        return profile(UserStatusEnum.ENABLED, ADMIN_ID);
    }

    private AuthUserDto userProfile(final UserStatusEnum status) {
        return profile(status, USERNAME_ID);
    }

    private AuthUserDto profile(final UserStatusEnum status, final String id) {
        val user = new AuthUserDto();
        user.setId(id);
        user.setStatus(status);
        user.setType(UserTypeEnum.NOMINATIVE);
        AddressDto address = new AddressDto();
        address.setStreet("73 rue du faubourg poissonnière");
        address.setZipCode("75009");
        address.setCity("Paris");
        address.setCountry("France");
        user.setAddress(address);
        val profile = new ProfileDto();
        profile.setRoles(Arrays.asList(new Role(ROLE_NAME)));
        val group = new GroupDto();
        group.setProfiles(Arrays.asList(profile));
        user.setProfileGroup(group);
        user.setCustomerId("customerId");
        return user;
    }
}
