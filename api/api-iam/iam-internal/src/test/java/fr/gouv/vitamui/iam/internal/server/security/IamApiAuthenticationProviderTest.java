package fr.gouv.vitamui.iam.internal.server.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;

/**
 * Tests the {@link IamApiAuthenticationProvider}.
 *
 *
 */
public final class IamApiAuthenticationProviderTest extends AbstractServerIdentityBuilder {

    private static final String USER_TOKEN = "userToken";

    private static final String USER_ID = "userId";

    private IamApiAuthenticationProvider provider;

    @Before
    public void setUp() throws Exception {
        final UserInternalService internalUserService = mock(UserInternalService.class);
        final TokenRepository tokenRepository = mock(TokenRepository.class);
        final SubrogationRepository subrogationRepository = mock(SubrogationRepository.class);
        provider = new IamApiAuthenticationProvider(
                new IamAuthentificationService(internalUserService, tokenRepository, subrogationRepository));

        final UserDto userProfile = new UserDto();
        userProfile.setId(USER_ID);
        userProfile.setLevel("LEVEL");

        when(internalUserService.findUserById(USER_TOKEN)).thenReturn(userProfile);
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
