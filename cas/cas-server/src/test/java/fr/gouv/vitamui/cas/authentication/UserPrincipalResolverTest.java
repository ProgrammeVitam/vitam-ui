package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.x509.X509AttributeMapping;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.utils.CasJsonWrapper;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.FileNotFoundException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.commons.api.CommonConstants.IDENTIFIER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link UserPrincipalResolver}.
 */
@ContextConfiguration(classes = UserPrincipalResolverTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class UserPrincipalResolverTest extends BaseWebflowActionTest {

    private static final String PROVIDER_NAME = "google";
    private static final String MAIL = "mail";
    private static final String IDENTIFIER = "identifier";
    private static final String USERNAME = "user@test.com";
    private static final String USERNAME_EMAIL_WITH_OTHER_CASE = "USER@test.com";
    private static final String CUSTOMER_ID = "customerId";
    private static final String ADMIN = "admin@test.com";
    private static final String ADMIN_CUSTOMER_ID = "customer_admin";
    private static final String IDENTIFIER_VALUE = "007";
    private static final String PWD = "password";
    private static final String USERNAME_ID = "userId";
    private static final String ADMIN_ID = "admin";
    private static final String ROLE_NAME = "role1";
    private static final String PROVIDER_ID = "providerId";
    public static final String CERTIFICATE_PROTOCOL_TYPE = "CERTIFICAT";

    private UserPrincipalResolver resolver;
    private CasApi casApi;
    private PrincipalFactory principalFactory;
    private SessionStore sessionStore;
    private IdentityProviderHelper identityProviderHelper;
    private ProvidersService providersService;

    @Before
    public void setUp() throws FileNotFoundException {
        super.setUp();

        casApi = mock(CasApi.class);
        principalFactory = new DefaultPrincipalFactory();
        sessionStore = mock(SessionStore.class);
        identityProviderHelper = mock(IdentityProviderHelper.class);
        providersService = mock(ProvidersService.class);
        final var emailMapping = new X509AttributeMapping("subject_dn", null, null);
        final var identifierMapping = new X509AttributeMapping("issuer_dn", null, null);
        resolver = new UserPrincipalResolver(
            principalFactory,
            casApi,
            sessionStore,
            identityProviderHelper,
            providersService,
            emailMapping,
            identifierMapping,
            ""
        );
    }

    @Test
    public void testResolveUserSuccessfully() {
        when(
            casApi.getUser(eq(USERNAME), eq(CUSTOMER_ID), eq(null), eq(null), eq(CommonConstants.AUTH_TOKEN_PARAMETER))
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));

        final var principal = resolver.resolve(
            new UsernamePasswordCredential(USERNAME, PWD),
            Optional.of(createLoginPrincipal()),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).getFirst());
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE));
    }

    @Test
    public void testResolveX509() throws Throwable {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setCustomerId(CUSTOMER_ID);
        provider.setProtocoleType(CERTIFICATE_PROTOCOL_TYPE);
        provider.setPatterns(List.of(".*@test.com"));
        when(
            identityProviderHelper.findAllProvidersByUserIdentifier(providersService.getProviders(), USERNAME)
        ).thenReturn(List.of(provider));

        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq(IDENTIFIER),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        final var cert = mock(X509Certificate.class);
        final var subjectDn = mock(java.security.Principal.class);
        when(subjectDn.getName()).thenReturn(USERNAME);
        when(cert.getSubjectDN()).thenReturn(subjectDn);
        final var issuerDn = mock(java.security.Principal.class);
        when(issuerDn.getName()).thenReturn(IDENTIFIER);
        when(cert.getIssuerDN()).thenReturn(issuerDn);

        final var principal = resolver.resolve(
            new X509CertificateCredential(new X509Certificate[] { cert }),
            Optional.of(principalFactory.createPrincipal(USERNAME)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).getFirst());
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE));
    }

    @Test
    public void testResolveX509CaseInsensitive() throws Throwable {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setCustomerId(CUSTOMER_ID);
        provider.setProtocoleType(CERTIFICATE_PROTOCOL_TYPE);
        provider.setPatterns(List.of(".*@TesT.com"));

        when(providersService.getProviders()).thenReturn(List.of(provider));

        when(
            identityProviderHelper.findAllProvidersByUserIdentifier(
                providersService.getProviders(),
                USERNAME_EMAIL_WITH_OTHER_CASE
            )
        ).thenReturn(List.of(provider));

        when(
            casApi.getUser(
                eq(USERNAME_EMAIL_WITH_OTHER_CASE),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq(IDENTIFIER),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        final var cert = mock(X509Certificate.class);
        final var subjectDn = mock(java.security.Principal.class);
        when(subjectDn.getName()).thenReturn(USERNAME_EMAIL_WITH_OTHER_CASE);
        when(cert.getSubjectDN()).thenReturn(subjectDn);
        final var issuerDn = mock(java.security.Principal.class);
        when(issuerDn.getName()).thenReturn(IDENTIFIER);
        when(cert.getIssuerDN()).thenReturn(issuerDn);

        final var principal = resolver.resolve(
            new X509CertificateCredential(new X509Certificate[] { cert }),
            Optional.of(principalFactory.createPrincipal(USERNAME)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME_EMAIL_WITH_OTHER_CASE, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).getFirst());
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegation() throws Throwable {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq(USERNAME),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        givenLoginInfoInSessionForDeleguatedAuthn();
        when(providersService.getProviders()).thenReturn(new ArrayList<>());
        when(
            identityProviderHelper.findByTechnicalName(eq(providersService.getProviders()), eq(PROVIDER_NAME))
        ).thenReturn(Optional.of(provider));

        final var principal = resolver.resolve(
            new ClientCredential(null, PROVIDER_NAME),
            Optional.of(principalFactory.createPrincipal(USERNAME)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).getFirst());
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegationMailAttribute() throws Throwable {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setMailAttribute(MAIL);
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq("fake"),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        givenLoginInfoInSessionForDeleguatedAuthn();
        when(
            identityProviderHelper.findByTechnicalName(eq(providersService.getProviders()), eq(PROVIDER_NAME))
        ).thenReturn(Optional.of(provider));

        final var princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(MAIL, Collections.singletonList(USERNAME));

        final var principal = resolver.resolve(
            new ClientCredential(null, PROVIDER_NAME),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).getFirst());
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegationIdentifierAttribute() throws Throwable {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setIdentifierAttribute(IDENTIFIER);
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq(IDENTIFIER_VALUE),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        givenLoginInfoInSessionForDeleguatedAuthn();
        when(
            identityProviderHelper.findByTechnicalName(eq(providersService.getProviders()), eq(PROVIDER_NAME))
        ).thenReturn(Optional.of(provider));

        final var princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(IDENTIFIER, Collections.singletonList(IDENTIFIER_VALUE));

        final var principal = resolver.resolve(
            new ClientCredential(null, PROVIDER_NAME),
            Optional.of(principalFactory.createPrincipal(USERNAME, princAttributes)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegationMailAttributeNoValue() throws Throwable {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setMailAttribute(MAIL);
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq("fake"),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        givenLoginInfoInSessionForDeleguatedAuthn();
        when(
            identityProviderHelper.findByTechnicalName(eq(providersService.getProviders()), eq(PROVIDER_NAME))
        ).thenReturn(Optional.of(provider));

        final var princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(MAIL, Collections.emptyList());

        final var principal = resolver.resolve(
            new ClientCredential(null, PROVIDER_NAME),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals("nobody", principal.getId());
    }

    @Test
    public void testResolveAuthnDelegationIdentifierAttributeNoValue() throws Throwable {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setIdentifierAttribute(IDENTIFIER_ATTRIBUTE);
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq("fake"),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        givenLoginInfoInSessionForDeleguatedAuthn();
        when(
            identityProviderHelper.findByTechnicalName(eq(providersService.getProviders()), eq(PROVIDER_NAME))
        ).thenReturn(Optional.of(provider));

        final var princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(IDENTIFIER, Collections.emptyList());

        final var principal = resolver.resolve(
            new ClientCredential(null, PROVIDER_NAME),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals("nobody", principal.getId());
    }

    @Test
    public void testResolveSurrogateUser() {
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(null),
                eq(null),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casApi.getUser(eq(ADMIN), eq(ADMIN_CUSTOMER_ID), eq(null), eq(null), eq(null))).thenReturn(
            infoProfile(UserStatusEnum.ENABLED, ADMIN_ID)
        );

        final var credential = new SurrogateUsernamePasswordCredential();
        credential.setUsername(ADMIN);
        credential.setSurrogateUsername(USERNAME);
        final var principal = resolver.resolve(
            credential,
            Optional.of(createSubrogationPrincipal()),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).getFirst());
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(ADMIN, attributes.get(SUPER_USER_ATTRIBUTE).getFirst());
        assertEquals(ADMIN_CUSTOMER_ID, attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE).getFirst());
    }

    @Test
    public void testResolveAuthnDelegationSurrogate() throws Throwable {
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(null),
                eq(null),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casApi.getUser(eq(ADMIN), eq(ADMIN_CUSTOMER_ID), eq(null), eq(null), eq(null))).thenReturn(
            infoProfile(UserStatusEnum.ENABLED, ADMIN_ID)
        );
        givenSubrogationInfoInSessionForDeleguatedAuthn();
        when(
            identityProviderHelper.findByTechnicalName(eq(providersService.getProviders()), eq(PROVIDER_NAME))
        ).thenReturn(Optional.of(new IdentityProviderDto()));

        final var principal = resolver.resolve(
            new ClientCredential(null, PROVIDER_NAME),
            Optional.of(principalFactory.createPrincipal(ADMIN)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).getFirst());
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(ADMIN, attributes.get(SUPER_USER_ATTRIBUTE).getFirst());
        assertEquals(ADMIN_CUSTOMER_ID, attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE).getFirst());
    }

    @Test
    public void testResolveAuthnDelegationSurrogateMailAttribute() throws Throwable {
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(null),
                eq(null),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casApi.getUser(eq(ADMIN), eq(ADMIN_CUSTOMER_ID), eq(null), eq(null), eq(null))).thenReturn(
            infoProfile(UserStatusEnum.ENABLED, ADMIN_ID)
        );
        givenSubrogationInfoInSessionForDeleguatedAuthn();
        final var provider = new IdentityProviderDto();
        provider.setMailAttribute(MAIL);
        when(
            identityProviderHelper.findByTechnicalName(eq(providersService.getProviders()), eq(PROVIDER_NAME))
        ).thenReturn(Optional.of(provider));

        final var princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(MAIL, Collections.singletonList(ADMIN));

        final var principal = resolver.resolve(
            new ClientCredential(null, PROVIDER_NAME),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(USERNAME, attributes.get(CommonConstants.EMAIL_ATTRIBUTE).getFirst());
        assertEquals(List.of(ROLE_NAME), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(ADMIN, attributes.get(SUPER_USER_ATTRIBUTE).getFirst());
        assertEquals(ADMIN_CUSTOMER_ID, attributes.get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE).getFirst());
    }

    @Test
    public void testResolveAuthnDelegationSurrogateMailAttributeNoMail() throws Throwable {
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(null),
                eq(null),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casApi.getUser(eq(ADMIN), eq(ADMIN_CUSTOMER_ID), eq(null), eq(null), eq(null))).thenReturn(
            infoProfile(UserStatusEnum.ENABLED, ADMIN_ID)
        );
        givenSubrogationInfoInSessionForDeleguatedAuthn();
        final var provider = new IdentityProviderDto();
        provider.setMailAttribute(MAIL);
        when(
            identityProviderHelper.findByTechnicalName(eq(providersService.getProviders()), eq(PROVIDER_NAME))
        ).thenReturn(Optional.of(provider));

        final var principal = resolver.resolve(
            new ClientCredential(null, PROVIDER_NAME),
            Optional.of(principalFactory.createPrincipal("fake")),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals("nobody", principal.getId());
    }

    @Test
    public void testResolveAddressDeserializeSuccessfully() {
        AuthUserDto userProfile = userProfile(UserStatusEnum.ENABLED);
        when(
            casApi.getUser(eq(USERNAME), eq(CUSTOMER_ID), eq(null), eq(null), eq(CommonConstants.AUTH_TOKEN_PARAMETER))
        ).thenReturn(userProfile);

        final var principal = resolver.resolve(
            new UsernamePasswordCredential(USERNAME, PWD),
            Optional.of(createLoginPrincipal()),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(USERNAME_ID, principal.getId());
        AddressDto addressDto = (AddressDto) ((CasJsonWrapper) principal
                .getAttributes()
                .get(CommonConstants.ADDRESS_ATTRIBUTE)
                .getFirst()).getData();
        assertThat(addressDto).isEqualToComparingFieldByField(userProfile.getAddress());
        assertNull(principal.getAttributes().get(SUPER_USER_ATTRIBUTE));
        assertNull(principal.getAttributes().get(SUPER_USER_CUSTOMER_ID_ATTRIBUTE));
    }

    @Test
    public void testNoUser() {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(
                providersService.getProviders(),
                USERNAME,
                CUSTOMER_ID
            )
        ).thenReturn(Optional.of(provider));
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq(null),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(null);

        assertNull(
            resolver.resolve(
                new UsernamePasswordCredential(USERNAME, PWD),
                Optional.of(createLoginPrincipal()),
                Optional.empty(),
                Optional.empty()
            )
        );
    }

    @Test
    public void testDisabledUser() {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(
                providersService.getProviders(),
                USERNAME,
                CUSTOMER_ID
            )
        ).thenReturn(Optional.of(provider));
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq(null),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.DISABLED));

        assertNull(
            resolver.resolve(
                new UsernamePasswordCredential(USERNAME, PWD),
                Optional.of(createLoginPrincipal()),
                Optional.empty(),
                Optional.empty()
            )
        );
    }

    @Test
    public void testUserCannotLogin() {
        final var provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(
                providersService.getProviders(),
                USERNAME,
                CUSTOMER_ID
            )
        ).thenReturn(Optional.of(provider));
        when(
            casApi.getUser(
                eq(USERNAME),
                eq(CUSTOMER_ID),
                eq(PROVIDER_ID),
                eq(null),
                eq(CommonConstants.AUTH_TOKEN_PARAMETER)
            )
        ).thenReturn(userProfile(UserStatusEnum.BLOCKED));

        assertNull(
            resolver.resolve(
                new UsernamePasswordCredential(USERNAME, PWD),
                Optional.of(createLoginPrincipal()),
                Optional.empty(),
                Optional.empty()
            )
        );
    }

    private AuthUserDto userProfile(final UserStatusEnum status) {
        return infoProfile(status, USERNAME_ID);
    }

    private AuthUserDto infoProfile(final UserStatusEnum status, final String id) {
        final AddressDto address = new AddressDto();
        address.setStreet("73 rue du faubourg poissonnière");
        address.setZipCode("75009");
        address.setCity("Paris");
        address.setCountry("France");

        final var user = new AuthUserDto();
        user.setId(id);
        user.setStatus(status);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setAddress(address);
        user.setCustomerId("customerId");

        Role role = new Role();
        role.setName(ROLE_NAME);
        ProfileDto profile = new ProfileDto();
        profile.setRoles(Collections.singletonList(role));
        GroupDto group = new GroupDto();
        group.setProfiles(Collections.singletonList(profile));
        user.setProfileGroup(group);

        return user;
    }

    private Principal createLoginPrincipal() {
        Principal principal;
        try {
            principal = principalFactory.createPrincipal(UserPrincipalResolverTest.USERNAME);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        principal.getAttributes().put(Constants.FLOW_LOGIN_EMAIL, List.of(UserPrincipalResolverTest.USERNAME));
        principal.getAttributes().put(Constants.FLOW_LOGIN_CUSTOMER_ID, List.of(UserPrincipalResolverTest.CUSTOMER_ID));
        return principal;
    }

    private Principal createSubrogationPrincipal() {
        Principal principal;
        try {
            principal = principalFactory.createPrincipal(UserPrincipalResolverTest.ADMIN);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        principal.getAttributes().put(Constants.FLOW_LOGIN_EMAIL, List.of(UserPrincipalResolverTest.ADMIN));
        principal
            .getAttributes()
            .put(Constants.FLOW_LOGIN_CUSTOMER_ID, List.of(UserPrincipalResolverTest.ADMIN_CUSTOMER_ID));
        principal.getAttributes().put(Constants.FLOW_SURROGATE_EMAIL, List.of(UserPrincipalResolverTest.USERNAME));
        principal
            .getAttributes()
            .put(Constants.FLOW_SURROGATE_CUSTOMER_ID, List.of(UserPrincipalResolverTest.CUSTOMER_ID));
        return principal;
    }

    private void givenLoginInfoInSessionForDeleguatedAuthn() {
        when(sessionStore.get(any(JEEContext.class), eq(Constants.FLOW_LOGIN_EMAIL))).thenReturn(Optional.of(USERNAME));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.FLOW_LOGIN_CUSTOMER_ID))).thenReturn(
            Optional.of(CUSTOMER_ID)
        );
        when(sessionStore.get(any(JEEContext.class), eq(Constants.FLOW_SURROGATE_EMAIL))).thenReturn(Optional.empty());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.FLOW_SURROGATE_CUSTOMER_ID))).thenReturn(
            Optional.empty()
        );
    }

    private void givenSubrogationInfoInSessionForDeleguatedAuthn() {
        when(sessionStore.get(any(JEEContext.class), eq(Constants.FLOW_LOGIN_EMAIL))).thenReturn(Optional.of(ADMIN));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.FLOW_LOGIN_CUSTOMER_ID))).thenReturn(
            Optional.of(ADMIN_CUSTOMER_ID)
        );
        when(sessionStore.get(any(JEEContext.class), eq(Constants.FLOW_SURROGATE_EMAIL))).thenReturn(
            Optional.of(USERNAME)
        );
        when(sessionStore.get(any(JEEContext.class), eq(Constants.FLOW_SURROGATE_CUSTOMER_ID))).thenReturn(
            Optional.of(CUSTOMER_ID)
        );
    }
}
