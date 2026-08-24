package fr.gouv.vitamui.iam.server.user.service;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.VitamuiRestClientFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link UserEmailService}.
 */
final class UserEmailServiceTest {

    private static final String LASTNAME = "John";

    private static final String FIRSTNAME = "Doe";
    private static final String CUSTOMER_ID = "CustomerId";

    private static final String EMAIL = "john.doe@vitamui.com";

    private static final String BASE_URL = "http://mycassserver";

    private IdentityProviderHelper identityProviderHelper;

    private IdentityProviderService identityProviderService;

    private VitamuiRestClientFactory vitamuiRestClientFactory;

    private RestClient restClient;

    private RestClient.RequestHeadersUriSpec uriSpec;

    private RestClient.ResponseSpec responseSpec;

    private UserEmailService userEmailService;

    private UserInfoService userInfoService;

    private final String casResetPasswordUrl =
        "/cas/extras/resetPassword?username={username}&firstname={firstname}&lastname={lastname}&language={language}&customerId={customerId}&ttl=1day";

    @BeforeEach
    public void setUp() {
        identityProviderHelper = mock(IdentityProviderHelper.class);
        userInfoService = mock(UserInfoService.class);
        identityProviderService = mock(IdentityProviderService.class);
        vitamuiRestClientFactory = mock(VitamuiRestClientFactory.class);
        restClient = mock(RestClient.class);
        uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(vitamuiRestClientFactory.getRestClient()).thenReturn(restClient);
        when(vitamuiRestClientFactory.getBaseUrl()).thenReturn(BASE_URL);
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(String.class), any(Map.class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Boolean.class)).thenReturn(true);

        userEmailService = new UserEmailService(vitamuiRestClientFactory);
        userEmailService.setInternalIdentityProviderService(identityProviderService);
        userEmailService.setIdentityProviderHelper(identityProviderHelper);
        userEmailService.setUserInfoService(userInfoService);
        userEmailService.setCasResetPasswordUrl(casResetPasswordUrl);
        final List<IdentityProviderDto> providers = new ArrayList<>();
        when(identityProviderService.getAll(Optional.empty(), Optional.empty())).thenReturn(providers);
        when(identityProviderHelper.identifierMatchProviderPattern(providers, EMAIL, CUSTOMER_ID)).thenReturn(true);
        when(userInfoService.getOne(any())).thenReturn(buildUserInfoDto());
    }

    @Test
    void testSendEmailOk() {
        final UserDto user = buildUser();

        userEmailService.sendCreationEmail(user);

        verify(restClient).get();
        verify(uriSpec).uri(
            BASE_URL + casResetPasswordUrl,
            Map.of(
                "username",
                EMAIL,
                "firstname",
                FIRSTNAME,
                "lastname",
                LASTNAME,
                "language",
                "fr",
                "customerId",
                CUSTOMER_ID
            )
        );
        verify(uriSpec).retrieve();
        verify(responseSpec).body(Boolean.class);
    }

    @Test
    void testSendEmailWhenCasReportsFailure() {
        final UserDto user = buildUser();
        when(responseSpec.body(Boolean.class)).thenReturn(false);

        assertThatCode(() -> userEmailService.sendCreationEmail(user)).doesNotThrowAnyException();

        verify(responseSpec).body(Boolean.class);
    }

    @Test
    void testSendEmailWhenCasIsUnreachable() {
        final UserDto user = buildUser();
        when(responseSpec.body(Boolean.class)).thenThrow(new RuntimeException("CAS is down"));

        assertThatCode(() -> userEmailService.sendCreationEmail(user)).doesNotThrowAnyException();

        verify(responseSpec).body(Boolean.class);
    }

    @Test
    void testSendEmailKoNoUser() {
        userEmailService.sendCreationEmail(null);

        verify(restClient, times(0)).get();
    }

    @Test
    void testSendEmailKoUserIsDisabled() {
        final UserDto user = buildUser();
        user.setStatus(UserStatusEnum.DISABLED);

        userEmailService.sendCreationEmail(user);

        verify(restClient, times(0)).get();
    }

    @Test
    void testSendEmailKoUserCannotLogin() {
        final UserDto user = buildUser();
        user.setStatus(UserStatusEnum.BLOCKED);

        userEmailService.sendCreationEmail(user);

        verify(restClient, times(0)).get();
    }

    @Test
    void testSendEmailKoUserIsNotInternal() {
        final UserDto user = buildUser();
        when(
            identityProviderHelper.identifierMatchProviderPattern(any(List.class), eq(EMAIL), eq(CUSTOMER_ID))
        ).thenReturn(false);

        userEmailService.sendCreationEmail(user);

        verify(restClient, times(0)).get();
    }

    @Test
    void testSendEmailKoUserIsNotNominative() {
        final UserDto user = buildUser();
        user.setType(UserTypeEnum.GENERIC);

        userEmailService.sendCreationEmail(user);

        verify(restClient, times(0)).get();
    }

    private UserDto buildUser() {
        final UserDto user = new UserDto();
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setEmail(EMAIL);
        user.setCustomerId(CUSTOMER_ID);
        user.setFirstname(FIRSTNAME);
        user.setLastname(LASTNAME);
        user.setUserInfoId("userInfoId");
        return user;
    }

    private UserInfoDto buildUserInfoDto() {
        return IamServerUtilsTest.buildUserInfoDto();
    }
}
