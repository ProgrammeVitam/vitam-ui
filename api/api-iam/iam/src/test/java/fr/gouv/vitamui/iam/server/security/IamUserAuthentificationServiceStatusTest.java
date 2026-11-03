package fr.gouv.vitamui.iam.server.security;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.server.token.domain.Token;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A token stops authenticating as soon as its account becomes DISABLED or REMOVED, even though the token
 * itself is not deleted when the status changes.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IamUserAuthentificationServiceStatusTest {

    private static final String SECRET = "tokcas_secret_for_tests";
    private static final String TOKEN_VALUE = "TOK-still-valid";

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private SubrogationRepository subrogationRepository;

    @InjectMocks
    private IamUserAuthentificationService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "casSecretToken", SECRET);
        ReflectionTestUtils.setField(service, "tokenMaxTtl", 480);
        ReflectionTestUtils.setField(service, "tokenAdditionalTtl", 240);
    }

    @Test
    void a_token_of_a_disabled_account_is_rejected() {
        assertRejected(UserStatusEnum.DISABLED);
    }

    @Test
    void a_token_of_a_removed_account_is_rejected() {
        assertRejected(UserStatusEnum.REMOVED);
    }

    private void assertRejected(final UserStatusEnum status) {
        final Token token = new Token();
        token.setId(TOKEN_VALUE);
        token.setRefId("account-id");
        token.setCreatedDate(new Date());
        token.setUpdatedDate(new Date(System.currentTimeMillis() + 60_000));
        when(tokenRepository.findById(TOKEN_VALUE)).thenReturn(Optional.of(token));

        final UserDto user = new UserDto();
        user.setId("account-id");
        user.setStatus(status);
        when(userService.findUserById("account-id")).thenReturn(user);

        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken(TOKEN_VALUE))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    private PreAuthenticatedAuthenticationToken callWithToken(final String userToken) {
        final HttpContext httpContext = mock(HttpContext.class);
        when(httpContext.getUserToken()).thenReturn(userToken);
        return new PreAuthenticatedAuthenticationToken(httpContext, null);
    }
}
