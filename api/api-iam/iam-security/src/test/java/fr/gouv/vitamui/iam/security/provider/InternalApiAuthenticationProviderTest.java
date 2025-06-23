package fr.gouv.vitamui.iam.security.provider;

import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.UserAuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InternalApiAuthenticationProviderTest {

    private static final String USER_ID = "userId";

    private InternalApiAuthenticationProvider provider;

    @Before
    public void setUp() throws Exception {
        final UserAuthenticationService userAuthenticationService = mock(UserAuthenticationService.class);
        provider = new InternalApiAuthenticationProvider(userAuthenticationService);

        final AuthUserDto userProfile = new AuthUserDto();
        userProfile.setId(USER_ID);
        userProfile.setLevel("LEVEL");

        when(userAuthenticationService.getUserFromHttpContext(any())).thenReturn(userProfile);
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
}
