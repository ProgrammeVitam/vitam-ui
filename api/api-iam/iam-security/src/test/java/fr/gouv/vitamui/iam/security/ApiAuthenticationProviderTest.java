package fr.gouv.vitamui.iam.security;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.authentication.ExternalAuthentication;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalAuthentificationService;
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.security.cert.X509Certificate;
import java.util.*;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ExternalApiAuthenticationProvider}.
 *
 *
 */
public final class ApiAuthenticationProviderTest extends AbstractServerIdentityBuilder {

    private static final byte[] CERTIFICATE = "CERTIFICATE".getBytes();

    private static final String USER_TOKEN = "userToken";

    private static final String USER_ID = "userId";

    private static final Integer TENANT_IDENTIFIER = 10;

    private static final String REQUEST_ID = "requestId";

    private static final String IDENTITY = "identity";

    private static final String ROLE = "ROLE_role1";

    private X509Certificate certificate;

    private ContextDto context;

    private List<ProfileDto> profiles;

    private ExternalApiAuthenticationProvider provider;

    @Before
    public void setUp() throws Exception {
        final ContextRestClient contextRestClient = mock(ContextRestClient.class);
        final UserInternalRestClient userInternalRestClient = mock(UserInternalRestClient.class);
        final ExternalAuthentificationService securityService = new ExternalAuthentificationService(contextRestClient,
                userInternalRestClient);
        provider = new ExternalApiAuthenticationProvider(securityService);
        profiles = new ArrayList<>();
        context = new ContextDto();
        context.setName("name");
        certificate = mock(X509Certificate.class);
        when(certificate.getEncoded()).thenReturn(CERTIFICATE);
        final AuthUserDto userProfile = new AuthUserDto();
        final GroupDto group = new GroupDto();
        group.setProfiles(profiles);
        userProfile.setProfileGroup(group);
        userProfile.setId(USER_ID);
        userProfile.setIdentifier("identifier");
        userProfile.setCustomerIdentifier("customerIdentifier");
        when(contextRestClient.findByCertificate(any(InternalHttpContext.class),
                eq(Base64.getEncoder().encodeToString(CERTIFICATE)))).thenReturn(context);
        when(userInternalRestClient.getMe(ArgumentMatchers.any())).thenReturn(userProfile);
    }

    @Test(expected = BadCredentialsException.class)
    public void testBadToken() {
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("test", "test");
        provider.authenticate(token);
    }

    @Test(expected = BadCredentialsException.class)
    public void testNoPrincipalOrCredential() {
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(null, null);
        provider.authenticate(token);
    }

    @Test
    public void testOk() {
        final ExternalHttpContext httpContext = new ExternalHttpContext(TENANT_IDENTIFIER, USER_TOKEN, APPLICATION_ID,
                IDENTITY, REQUEST_ID);
        context.setTenants(Arrays.asList(TENANT_IDENTIFIER));
        context.setRoleNames(Arrays.asList(ROLE));
        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(Integer.valueOf(TENANT_IDENTIFIER));
        profile.setRoles(Arrays.asList(new Role(ROLE)));
        profiles.add(profile);
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(httpContext,
                certificate);
        final ExternalAuthentication securityContext = (ExternalAuthentication) provider.authenticate(token);
        final Collection<GrantedAuthority> authorities = securityContext.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals(ROLE, authorities.iterator().next().getAuthority());
    }

    @Test
    public void testOkBadContextTenantButFullAccess() {
        final ExternalHttpContext httpContext = new ExternalHttpContext(TENANT_IDENTIFIER, USER_TOKEN, APPLICATION_ID,
                IDENTITY, REQUEST_ID);
        context.setFullAccess(true);
        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(TENANT_IDENTIFIER);
        profiles.add(profile);
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(httpContext,
                certificate);
        provider.authenticate(token);
    }

    @Test
    public void testBadContextTenant() {
        final ExternalHttpContext httpContext = new ExternalHttpContext(TENANT_IDENTIFIER, USER_TOKEN, APPLICATION_ID,
                IDENTITY, REQUEST_ID);
        context.setTenants(Arrays.asList(12));
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(httpContext,
                certificate);
        try {
            provider.authenticate(token);
            fail("should fail");
        }
        catch (final InvalidAuthenticationException e) {
            assertEquals(
                    "This tenant: " + TENANT_IDENTIFIER + " is not allowed for the application context: " + IDENTITY,
                    e.getMessage());
        }
        catch (final BadCredentialsException e) {
            assertEquals(
                    "This tenant: " + TENANT_IDENTIFIER + " is not allowed for the application context: " + IDENTITY,
                    e.getMessage());
        }
    }

    @Test
    public void testBadUserTenant() {
        final ExternalHttpContext httpContext = new ExternalHttpContext(TENANT_IDENTIFIER, USER_TOKEN, APPLICATION_ID,
                IDENTITY, REQUEST_ID);
        context.setTenants(Arrays.asList(TENANT_IDENTIFIER));

        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(12);

        profiles.add(profile);
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(httpContext,
                certificate);
        try {
            provider.authenticate(token);
            fail("should fail");
        }
        catch (final BadCredentialsException e) {
            assertEquals("This tenant: " + TENANT_IDENTIFIER + " is not allowed for this user: " + USER_ID,
                    e.getMessage());
        }
    }

    @Test
    public void testNoMatchingRole() {
        final ExternalHttpContext httpContext = new ExternalHttpContext(TENANT_IDENTIFIER, USER_TOKEN, APPLICATION_ID,
                IDENTITY, REQUEST_ID);
        context.setTenants(Arrays.asList(TENANT_IDENTIFIER));
        context.setRoleNames(Arrays.asList("role1"));
        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(TENANT_IDENTIFIER);
        profile.setRoles(Arrays.asList(new Role("role2")));
        profiles.add(profile);
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(httpContext,
                certificate);
        final ExternalAuthentication securityContext = (ExternalAuthentication) provider.authenticate(token);
        final Collection<GrantedAuthority> authorities = securityContext.getAuthorities();
        assertEquals(0, authorities.size());
    }
}
