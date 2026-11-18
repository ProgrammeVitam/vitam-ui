package fr.gouv.vitamui.iam.server.security;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.subrogation.domain.Subrogation;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tokens stop authenticating as soon as their account becomes DISABLED or REMOVED, and subrogation tokens
 * only authenticate while their subrogation is accepted and not expired.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IamUserAuthentificationServiceTokenTest {

    private static final String SECRET = "tokcas_secret_for_tests";
    private static final String TOKEN_VALUE = "TOK-still-valid";
    private static final String SUPER_USER_EMAIL = "support@example.com";
    private static final String SUBROGATION_ID = "subrogation-id";
    private static final String SUPER_USER_CUSTOMER_ID = "support-customer";

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

    @Test
    void a_subrogation_token_with_an_accepted_subrogation_is_accepted() {
        givenSubrogationToken(subrogation(SubrogationStatusEnum.ACCEPTED, inMinutes(30)));

        assertThat(service.getUserFromHttpContext(callWithToken(TOKEN_VALUE)).getSuperUser()).isEqualTo(
            SUPER_USER_EMAIL
        );
    }

    @Test
    void a_subrogation_token_without_subrogation_is_rejected() {
        givenSubrogationToken(null);

        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken(TOKEN_VALUE))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    @Test
    void a_subrogation_token_with_a_subrogation_not_accepted_is_rejected() {
        givenSubrogationToken(subrogation(SubrogationStatusEnum.CREATED, inMinutes(30)));

        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken(TOKEN_VALUE))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    @Test
    void a_subrogation_token_with_an_expired_subrogation_is_rejected() {
        givenSubrogationToken(subrogation(SubrogationStatusEnum.ACCEPTED, inMinutes(-1)));

        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken(TOKEN_VALUE))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    @Test
    void a_subrogation_token_of_an_ended_subrogation_is_rejected_after_a_new_subrogation_of_the_surrogate() {
        final Subrogation newSubrogation = subrogation(SubrogationStatusEnum.ACCEPTED, inMinutes(30));
        newSubrogation.setId("new-subrogation-id");
        givenSubrogationToken(newSubrogation);

        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken(TOKEN_VALUE))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    @Test
    void a_subrogation_token_of_a_disabled_super_user_is_rejected() {
        givenSubrogationToken(subrogation(SubrogationStatusEnum.ACCEPTED, inMinutes(30)));
        final UserDto superUser = new UserDto();
        superUser.setStatus(UserStatusEnum.DISABLED);
        when(userService.findUserByEmailAndCustomerId(SUPER_USER_EMAIL, SUPER_USER_CUSTOMER_ID)).thenReturn(superUser);

        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken(TOKEN_VALUE))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    private void givenSubrogationToken(final Subrogation subrogation) {
        final Token token = new Token();
        token.setId(TOKEN_VALUE);
        token.setRefId("account-id");
        token.setSurrogation(true);
        token.setSubrogationId(SUBROGATION_ID);
        when(tokenRepository.findById(TOKEN_VALUE)).thenReturn(Optional.of(token));

        final UserDto user = new UserDto();
        user.setId("account-id");
        user.setEmail("surrogate@example.com");
        user.setCustomerId("customer-id");
        user.setStatus(UserStatusEnum.ENABLED);
        when(userService.findUserById("account-id")).thenReturn(user);
        when(userService.loadGroupAndProfiles(user)).thenReturn(new AuthUserDto());
        when(subrogationRepository.findOneBySurrogateAndSurrogateCustomerId(any(), any())).thenReturn(subrogation);

        final UserDto superUser = new UserDto();
        superUser.setStatus(UserStatusEnum.ENABLED);
        superUser.setIdentifier("42");
        when(userService.findUserByEmailAndCustomerId(SUPER_USER_EMAIL, SUPER_USER_CUSTOMER_ID)).thenReturn(superUser);
    }

    private static Subrogation subrogation(final SubrogationStatusEnum status, final Date date) {
        final Subrogation subrogation = new Subrogation();
        subrogation.setId(SUBROGATION_ID);
        subrogation.setStatus(status);
        subrogation.setDate(date);
        subrogation.setSuperUser(SUPER_USER_EMAIL);
        subrogation.setSuperUserCustomerId(SUPER_USER_CUSTOMER_ID);
        return subrogation;
    }

    private static Date inMinutes(final int minutes) {
        return new Date(System.currentTimeMillis() + minutes * 60_000L);
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
