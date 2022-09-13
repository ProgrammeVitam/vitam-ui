package fr.gouv.vitamui.cas.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.x509.X509AttributeMapping;
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
import lombok.val;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
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

import java.io.FileNotFoundException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.*;

import static fr.gouv.vitamui.commons.api.CommonConstants.IDENTIFIER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
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
public final class UserPrincipalResolverTest extends BaseWebflowActionTest {

    private static final String CREDENTIALS_DETAILS_FILE = "credentialsRepository/resolverUserCredentials.json";

    private UserPrincipalResolver resolver;

    private CasExternalRestClient casExternalRestClient;

    private PrincipalFactory principalFactory;

    private SessionStore sessionStore;

    private IdentityProviderHelper identityProviderHelper;

    private ProvidersService providersService;

    private JsonNode jsonNode;

    @Before
    public void setUp() throws FileNotFoundException, InvalidParseOperationException {
        super.setUp();

        jsonNode =
            JsonHandler.getFromFile(PropertiesUtils.findFile(CREDENTIALS_DETAILS_FILE));

        casExternalRestClient = mock(CasExternalRestClient.class);
        val utils = new Utils(null, 0, null, null, "");
        principalFactory = new DefaultPrincipalFactory();
        sessionStore = mock(SessionStore.class);
        identityProviderHelper = mock(IdentityProviderHelper.class);
        providersService = mock(ProvidersService.class);
        val emailMapping = new X509AttributeMapping("subject_dn", null, null);
        val identifierMapping = new X509AttributeMapping("issuer_dn", null, null);
        resolver = new UserPrincipalResolver(principalFactory, casExternalRestClient, utils, sessionStore,
            identityProviderHelper, providersService, emailMapping, identifierMapping, "");
    }

    @Test
    public void testResolveUserSuccessfully() {
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(null), eq(Optional.empty()), eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));

        val principal = resolver.resolve(new UsernamePasswordCredential(jsonNode.findValue("USERNAME").textValue(),
                jsonNode.findValue("PWD").textValue()), Optional.of(principalFactory.createPrincipal(jsonNode
            .findValue("USERNAME").textValue())), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(jsonNode.findValue("USERNAME").textValue(), attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(List.of(jsonNode.findValue("ROLE_NAME").textValue()), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testResolveX509() {
        val provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        when(identityProviderHelper.findByUserIdentifier(providersService.getProviders(), USERNAME)).thenReturn(Optional.of(provider));

        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(PROVIDER_ID), eq(Optional.of(jsonNode.findValue("IDENTIFIER").textValue())),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        val cert = mock(X509Certificate.class);
        val subjectDn = mock(Principal.class);
        when(subjectDn.getName()).thenReturn(jsonNode.findValue("USERNAME").textValue());
        when(cert.getSubjectDN()).thenReturn(subjectDn);
        val issuerDn = mock(Principal.class);
        when(issuerDn.getName()).thenReturn(jsonNode.findValue("IDENTIFIER").textValue());
        when(cert.getIssuerDN()).thenReturn(issuerDn);

        val principal = resolver.resolve(new X509CertificateCredential(new X509Certificate[] { cert }),
            Optional.of(principalFactory.createPrincipal(jsonNode.findValue("USERNAME").textValue())), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(jsonNode.findValue("USERNAME").textValue(), attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(List.of(jsonNode.findValue("ROLE_NAME").textValue()), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegation() {
        val provider = new IdentityProviderDto();
        provider.setId(jsonNode.findValue("PROVIDER_ID").textValue());
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(jsonNode.findValue("PROVIDER_ID").textValue()), eq(Optional.of(jsonNode.findValue("USERNAME").textValue())),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.empty());
        when(providersService.getProviders()).thenReturn(new ArrayList<>());
        when(identityProviderHelper.findByTechnicalName(providersService.getProviders(), jsonNode.findValue("PROVIDER_NAME")
            .textValue())).thenReturn(Optional.of(provider));

        val principal = resolver.resolve(new ClientCredential(null, jsonNode.findValue("PROVIDER_NAME").textValue()),
            Optional.of(principalFactory.createPrincipal(jsonNode.findValue("USERNAME").textValue())), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(jsonNode.findValue("USERNAME").textValue(), attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(List.of(jsonNode.findValue("ROLE_NAME").textValue()), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegationMailAttribute() {
        val provider = new IdentityProviderDto();
        provider.setId(jsonNode.findValue("PROVIDER_ID").textValue());
        provider.setMailAttribute(jsonNode.findValue("MAIL").textValue());
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(jsonNode.findValue("PROVIDER_ID").textValue()), eq(Optional.of("fake")),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.empty());
        when(identityProviderHelper.findByTechnicalName(providersService.getProviders(), jsonNode.findValue("PROVIDER_NAME")
            .textValue())).thenReturn(Optional.of(provider));

        val princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(jsonNode.findValue("MAIL").textValue(), Collections.singletonList(jsonNode.findValue("USERNAME")
            .textValue()));

        val principal = resolver.resolve(new ClientCredential(null, jsonNode.findValue("PROVIDER_NAME").textValue()),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(jsonNode.findValue("USERNAME").textValue(), attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(List.of(jsonNode.findValue("ROLE_NAME").textValue()), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegationIdentifierAttribute() {
        val provider = new IdentityProviderDto();
        provider.setId(jsonNode.findValue("PROVIDER_ID").textValue());
        provider.setIdentifierAttribute(jsonNode.findValue("IDENTIFIER").textValue());
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(jsonNode.findValue("PROVIDER_ID").textValue()), eq(Optional.of(jsonNode.findValue("IDENTIFIER_VALUE").textValue())),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.empty());
        when(identityProviderHelper.findByTechnicalName(providersService.getProviders(), jsonNode.findValue("PROVIDER_NAME")
            .textValue())).thenReturn(Optional.of(provider));

        val princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(jsonNode.findValue("IDENTIFIER").textValue(), Collections.singletonList(jsonNode.findValue("IDENTIFIER_VALUE")
            .textValue()));

        val principal = resolver.resolve(new ClientCredential(null, jsonNode.findValue("PROVIDER_NAME").textValue()),
            Optional.of(principalFactory.createPrincipal(jsonNode.findValue("USERNAME").textValue(), princAttributes)), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(List.of(jsonNode.findValue("ROLE_NAME").textValue()), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertNull(attributes.get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testResolveAuthnDelegationMailAttributeNoValue() {
        val provider = new IdentityProviderDto();
        provider.setId(jsonNode.findValue("PROVIDER_ID").textValue());
        provider.setMailAttribute(jsonNode.findValue("MAIL").textValue());
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(jsonNode.findValue("PROVIDER_ID").textValue()), eq(Optional.of("fake")),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.empty());
        when(identityProviderHelper.findByTechnicalName(providersService.getProviders(), jsonNode.findValue("PROVIDER_NAME").textValue()))
            .thenReturn(Optional.of(provider));

        val princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(jsonNode.findValue("MAIL").textValue(), Collections.emptyList());

        val principal = resolver.resolve(new ClientCredential(null, jsonNode.findValue("PROVIDER_NAME").textValue()),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)), Optional.empty());

        assertEquals("nobody", principal.getId());
    }

    @Test
    public void testResolveAuthnDelegationIdentifierAttributeNoValue() {
        val provider = new IdentityProviderDto();
        provider.setId(jsonNode.findValue("PROVIDER_ID").textValue());
        provider.setIdentifierAttribute(IDENTIFIER_ATTRIBUTE);
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(jsonNode.findValue("PROVIDER_ID").textValue()), eq(Optional.of("fake")),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER)))).thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.empty());
        when(identityProviderHelper.findByTechnicalName(providersService.getProviders(), jsonNode.findValue("PROVIDER_NAME").textValue()))
            .thenReturn(Optional.of(provider));

        val princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(jsonNode.findValue("IDENTIFIER").textValue(), Collections.emptyList());

        val principal = resolver.resolve(new ClientCredential(null, jsonNode.findValue("PROVIDER_NAME").textValue()),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)), Optional.empty());

        assertEquals("nobody", principal.getId());
    }
    @Test
    public void testResolveSurrogateUser() {
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(null), eq(Optional.empty()),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("ADMIN").textValue()),
            eq(null), eq(Optional.empty()),
            eq(Optional.empty()))).thenReturn(adminProfile());

        val credential = new SurrogateUsernamePasswordCredential();
        credential.setUsername(jsonNode.findValue("ADMIN").textValue());
        credential.setSurrogateUsername(jsonNode.findValue("USERNAME").textValue());
        val principal = resolver.resolve(credential, Optional.of(principalFactory.createPrincipal(jsonNode.findValue("ADMIN")
            .textValue())), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(jsonNode.findValue("USERNAME").textValue(), attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(List.of(jsonNode.findValue("ROLE_NAME").textValue()), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(jsonNode.findValue("ADMIN").textValue(), attributes.get(SUPER_USER_ATTRIBUTE).get(0));
    }

    @Test
    public void testResolveAuthnDelegationSurrogate() {
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(null), eq(Optional.empty()),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("ADMIN").textValue()),
            eq(null), eq(Optional.empty()),
            eq(Optional.empty()))).thenReturn(adminProfile());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.of(jsonNode.findValue("USERNAME").textValue()));
        when(identityProviderHelper.findByTechnicalName(providersService.getProviders(), jsonNode.findValue("PROVIDER_NAME").textValue()))
            .thenReturn(Optional.of(new IdentityProviderDto()));

        val  principal = resolver.resolve(new ClientCredential(null, jsonNode.findValue("PROVIDER_NAME").textValue()),
            Optional.of(principalFactory.createPrincipal(jsonNode.findValue("ADMIN").textValue())), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(jsonNode.findValue("USERNAME").textValue(), attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(List.of(jsonNode.findValue("ROLE_NAME").textValue()), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(jsonNode.findValue("ADMIN").textValue(), attributes.get(SUPER_USER_ATTRIBUTE).get(0));
    }

    @Test
    public void testResolveAuthnDelegationSurrogateMailAttribute() {
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(null), eq(Optional.empty()),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("ADMIN").textValue()),
            eq(null), eq(Optional.empty()),
            eq(Optional.empty()))).thenReturn(adminProfile());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.of(jsonNode.findValue("USERNAME").textValue()));
        val provider = new IdentityProviderDto();
        provider.setMailAttribute(jsonNode.findValue("MAIL").textValue());
        when(identityProviderHelper.findByTechnicalName(providersService.getProviders(), jsonNode.findValue("PROVIDER_NAME").textValue()))
            .thenReturn(Optional.of(provider));

        val princAttributes = new HashMap<String, List<Object>>();
        princAttributes.put(jsonNode.findValue("MAIL").textValue(), Collections.singletonList(jsonNode.findValue("ADMIN").textValue()));

        val  principal = resolver.resolve(new ClientCredential(null, jsonNode.findValue("PROVIDER_NAME").textValue()),
            Optional.of(principalFactory.createPrincipal("fake", princAttributes)), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        final Map<String, List<Object>> attributes = principal.getAttributes();
        assertEquals(jsonNode.findValue("USERNAME").textValue(), attributes.get(CommonConstants.EMAIL_ATTRIBUTE).get(0));
        assertEquals(List.of(jsonNode.findValue("ROLE_NAME").textValue()), attributes.get(CommonConstants.ROLES_ATTRIBUTE));
        assertEquals(jsonNode.findValue("ADMIN").textValue(), attributes.get(SUPER_USER_ATTRIBUTE).get(0));
    }

    @Test
    public void testResolveAuthnDelegationSurrogateMailAttributeNoMail() {
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(null), eq(Optional.empty()),
            eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER + "," + CommonConstants.SURROGATION_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.ENABLED));
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("ADMIN").textValue()),
            eq(null), eq(Optional.empty()),
            eq(Optional.empty()))).thenReturn(adminProfile());
        when(sessionStore.get(any(JEEContext.class), eq(Constants.SURROGATE))).thenReturn(Optional.of(jsonNode.findValue("USERNAME").textValue()));
        val provider = new IdentityProviderDto();
        provider.setMailAttribute(jsonNode.findValue("MAIL").textValue());
        when(identityProviderHelper.findByTechnicalName(providersService.getProviders(), jsonNode.findValue("PROVIDER_NAME").textValue()))
            .thenReturn(Optional.of(provider));

        val  principal = resolver.resolve(new ClientCredential(null, jsonNode.findValue("PROVIDER_NAME").textValue()),
            Optional.of(principalFactory.createPrincipal("fake")), Optional.empty());

        assertEquals("nobody", principal.getId());
    }

    @Test
    public void testResolveAddressDeserializeSuccessfully() {
        AuthUserDto authUserDto = userProfile(UserStatusEnum.ENABLED);
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()), eq(null),
            eq(Optional.empty()), eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
                .thenReturn(authUserDto);

        val principal = resolver.resolve(new UsernamePasswordCredential(jsonNode.findValue("USERNAME").textValue(),
                jsonNode.findValue("PWD").textValue()), Optional.of(principalFactory.createPrincipal(jsonNode
            .findValue("USERNAME").textValue())), Optional.empty());

        assertEquals(jsonNode.findValue("USERNAME_ID").textValue(), principal.getId());
        AddressDto addressDto = (AddressDto) ((CasJsonWrapper) principal.getAttributes().get(CommonConstants.ADDRESS_ATTRIBUTE).get(0)).getData();
        assertThat(addressDto).isEqualToComparingFieldByField(authUserDto.getAddress());
        assertNull(principal.getAttributes().get(SUPER_USER_ATTRIBUTE));
    }

    @Test
    public void testNoUser() {
        val provider = new IdentityProviderDto();
        provider.setId(jsonNode.findValue("PROVIDER_ID").textValue());
        when(identityProviderHelper.findByUserIdentifier(providersService.getProviders(), jsonNode.findValue("USERNAME").textValue())).thenReturn(Optional.of(provider));
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()), eq(jsonNode.findValue("PROVIDER_ID").textValue()), eq(Optional.empty()), eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
            .thenReturn(null);

        assertNull(resolver.resolve(new UsernamePasswordCredential(jsonNode.findValue("USERNAME").textValue(),
                jsonNode.findValue("PWD").textValue()), Optional.of(principalFactory.createPrincipal(jsonNode
            .findValue("USERNAME").textValue())), Optional.empty()));
    }

    @Test
    public void testDisabledUser() {
        val provider = new IdentityProviderDto();
        provider.setId(jsonNode.findValue("PROVIDER_ID").textValue());
        when(identityProviderHelper.findByUserIdentifier(providersService.getProviders(), jsonNode.findValue("USERNAME").textValue()))
            .thenReturn(Optional.of(provider));
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(jsonNode.findValue("PROVIDER_ID").textValue()), eq(Optional.empty()), eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.DISABLED));

        assertNull(resolver.resolve(new UsernamePasswordCredential(jsonNode.findValue("USERNAME").textValue(),
                jsonNode.findValue("PWD").textValue()), Optional.of(principalFactory.createPrincipal(jsonNode
            .findValue("USERNAME").textValue())), Optional.empty()));
    }

    @Test
    public void testUserCannotLogin() {
        val provider = new IdentityProviderDto();
        provider.setId(jsonNode.findValue("PROVIDER_ID").textValue());
        when(identityProviderHelper.findByUserIdentifier(providersService.getProviders(), jsonNode.findValue("USERNAME").textValue()))
            .thenReturn(Optional.of(provider));
        when(casExternalRestClient.getUser(any(ExternalHttpContext.class), eq(jsonNode.findValue("USERNAME").textValue()),
            eq(jsonNode.findValue("PROVIDER_ID").textValue()), eq(Optional.empty()), eq(Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER))))
            .thenReturn(userProfile(UserStatusEnum.BLOCKED));

        assertNull(resolver.resolve(new UsernamePasswordCredential(jsonNode.findValue("USERNAME").textValue(), jsonNode.findValue("PWD").textValue()),
            Optional.of(principalFactory.createPrincipal(jsonNode.findValue("USERNAME").textValue())), Optional.empty()));
    }

    private AuthUserDto adminProfile() {
        return profile(UserStatusEnum.ENABLED, jsonNode.findValue("ADMIN_ID").textValue());
    }

    private AuthUserDto userProfile(final UserStatusEnum status) {
        return profile(status, jsonNode.findValue("USERNAME_ID").textValue());
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
        profile.setRoles(List.of(new Role(jsonNode.findValue("ROLE_NAME").textValue())));
        val group = new GroupDto();
        group.setProfiles(List.of(profile));
        user.setProfileGroup(group);
        user.setCustomerId("customerId");
        return user;
    }
}
