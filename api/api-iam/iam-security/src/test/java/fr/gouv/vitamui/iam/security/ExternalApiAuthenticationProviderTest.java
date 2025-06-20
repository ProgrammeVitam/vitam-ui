package fr.gouv.vitamui.iam.security;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.iam.openapiclient.UsersApi;
import fr.gouv.vitamui.iam.openapiclient.domain.AuthUserDto;
import fr.gouv.vitamui.iam.security.authentication.AuthenticationToken;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.IamClientUserAuthenticationService;
import fr.gouv.vitamui.iam.security.service.UserAuthenticationService;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.openapiclient.ContextsApi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ApiAuthenticationProvider}.
 *
 *
 */
public final class ExternalApiAuthenticationProviderTest {

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
        final ContextsApi contextsApi = mock(ContextsApi.class);
        final UsersApi usersApi = mock(UsersApi.class);
        final UserAuthenticationService userAuthenticationService = new IamClientUserAuthenticationService(usersApi);

        provider = new ExternalApiAuthenticationProvider(contextsApi, userAuthenticationService);
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
        when(contextsApi.findByCertificate(eq(Base64.getEncoder().encodeToString(CERTIFICATE)))).thenReturn(context);
        when(usersApi.getMe()).thenReturn(userProfile);
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
        final HttpContext httpContext = new HttpContext(
            TENANT_IDENTIFIER,
            USER_TOKEN,
            true,
            APPLICATION_ID,
            IDENTITY,
            REQUEST_ID,
            null,
            null
        );
        context.setTenants(Arrays.asList(TENANT_IDENTIFIER));
        context.setRoleNames(Arrays.asList(ROLE));
        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(Integer.valueOf(TENANT_IDENTIFIER));
        profile.setRoles(Arrays.asList(new Role(ROLE)));
        profiles.add(profile);
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(
            httpContext,
            certificate
        );
        final AuthenticationToken securityContext = (AuthenticationToken) provider.authenticate(token);
        final Collection<GrantedAuthority> authorities = securityContext.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals(ROLE, authorities.iterator().next().getAuthority());
    }

    @Test
    public void testOkBadContextTenantButFullAccess() {
        final HttpContext httpContext = new HttpContext(
            TENANT_IDENTIFIER,
            USER_TOKEN,
            true,
            APPLICATION_ID,
            IDENTITY,
            REQUEST_ID,
            null,
            null
        );
        context.setFullAccess(true);
        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(TENANT_IDENTIFIER);
        profiles.add(profile);
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(
            httpContext,
            certificate
        );
        provider.authenticate(token);
    }

    @Test
    public void testBadContextTenant() {
        final HttpContext httpContext = new HttpContext(
            TENANT_IDENTIFIER,
            USER_TOKEN,
            true,
            APPLICATION_ID,
            IDENTITY,
            REQUEST_ID,
            null,
            null
        );
        context.setTenants(Arrays.asList(12));
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(
            httpContext,
            certificate
        );
        try {
            provider.authenticate(token);
            fail("should fail");
        } catch (final InvalidAuthenticationException e) {
            assertEquals(
                "This tenant: " + TENANT_IDENTIFIER + " is not allowed for the application context: " + IDENTITY,
                e.getMessage()
            );
        } catch (final BadCredentialsException e) {
            assertEquals(
                "This tenant: " + TENANT_IDENTIFIER + " is not allowed for the application context: " + IDENTITY,
                e.getMessage()
            );
        }
    }

    @Test
    public void testBadUserTenant() {
        final HttpContext httpContext = new HttpContext(
            TENANT_IDENTIFIER,
            USER_TOKEN,
            true,
            APPLICATION_ID,
            IDENTITY,
            REQUEST_ID,
            null,
            null
        );
        context.setTenants(Arrays.asList(TENANT_IDENTIFIER));

        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(12);

        profiles.add(profile);
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(
            httpContext,
            certificate
        );
        try {
            provider.authenticate(token);
            fail("should fail");
        } catch (final BadCredentialsException e) {
            assertEquals(
                "This tenant: " + TENANT_IDENTIFIER + " is not allowed for this user: " + USER_ID,
                e.getMessage()
            );
        }
    }

    @Test
    public void testNoMatchingRole() {
        final HttpContext httpContext = new HttpContext(
            TENANT_IDENTIFIER,
            USER_TOKEN,
            true,
            APPLICATION_ID,
            IDENTITY,
            REQUEST_ID,
            null,
            null
        );
        context.setTenants(Arrays.asList(TENANT_IDENTIFIER));
        context.setRoleNames(Arrays.asList("role1"));
        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(TENANT_IDENTIFIER);
        profile.setRoles(Arrays.asList(new Role("role2")));
        profiles.add(profile);
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(
            httpContext,
            certificate
        );
        final AuthenticationToken securityContext = (AuthenticationToken) provider.authenticate(token);
        final Collection<GrantedAuthority> authorities = securityContext.getAuthorities();
        assertEquals(0, authorities.size());
    }
}
