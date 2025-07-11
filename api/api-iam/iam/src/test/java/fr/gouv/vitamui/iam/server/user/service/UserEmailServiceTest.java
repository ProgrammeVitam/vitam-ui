package fr.gouv.vitamui.iam.server.user.service;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.RestClientFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link UserEmailService}.
 */
public final class UserEmailServiceTest {

    private static final String LASTNAME = "John";

    private static final String FIRSTNAME = "Doe";
    private static final String CUSTOMER_ID = "CustomerId";

    private static final String EMAIL = "john.doe@vitamui.com";

    private static final String BASE_URL = "http://mycassserver";

    private IdentityProviderHelper identityProviderHelper;

    private IdentityProviderService identityProviderService;

    private RestClientFactory restClientFactory;

    private RestTemplate restTemplate;

    private UserEmailService userEmailService;

    private UserInfoService userInfoService;

    private final String casResetPasswordUrl =
        "/cas/extras/resetPassword?username={username}&firstname={firstname}&lastname={lastname}&language={language}&customerId={customerId}&ttl=1day";

    @BeforeEach
    public void setUp() {
        identityProviderHelper = mock(IdentityProviderHelper.class);
        userInfoService = mock(UserInfoService.class);
        identityProviderService = mock(IdentityProviderService.class);
        restClientFactory = mock(RestClientFactory.class);
        restTemplate = mock(RestTemplate.class);
        when(restClientFactory.getRestTemplate()).thenReturn(restTemplate);
        when(restClientFactory.getBaseUrl()).thenReturn(BASE_URL);
        userEmailService = new UserEmailService(restClientFactory);
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
    public void testSendEmailOk() {
        final UserDto user = buildUser();

        userEmailService.sendCreationEmail(user);

        verify(restTemplate).getForEntity(
            BASE_URL + casResetPasswordUrl,
            Boolean.class,
            EMAIL,
            FIRSTNAME,
            LASTNAME,
            "fr",
            CUSTOMER_ID
        );
    }

    @Test
    public void testSendEmailKoNoUser() {
        userEmailService.sendCreationEmail(null);

        verify(restTemplate, times(0)).getForEntity(
            BASE_URL + casResetPasswordUrl,
            Boolean.class,
            EMAIL,
            FIRSTNAME,
            LASTNAME,
            "fr",
            CUSTOMER_ID
        );
    }

    @Test
    public void testSendEmailKoUserIsDisabled() {
        final UserDto user = buildUser();
        user.setStatus(UserStatusEnum.DISABLED);

        userEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(
            BASE_URL + casResetPasswordUrl,
            Boolean.class,
            EMAIL,
            FIRSTNAME,
            LASTNAME,
            "fr",
            CUSTOMER_ID
        );
    }

    @Test
    public void testSendEmailKoUserCannotLogin() {
        final UserDto user = buildUser();
        user.setStatus(UserStatusEnum.BLOCKED);

        userEmailService.sendCreationEmail(user);

        Mockito.verify(restTemplate, times(0)).getForEntity(
            BASE_URL + casResetPasswordUrl,
            Boolean.class,
            EMAIL,
            FIRSTNAME,
            LASTNAME,
            "fr",
            CUSTOMER_ID
        );
    }

    @Test
    public void testSendEmailKoUserIsNotInternal() {
        final UserDto user = buildUser();
        when(
            identityProviderHelper.identifierMatchProviderPattern(any(List.class), eq(EMAIL), eq(CUSTOMER_ID))
        ).thenReturn(false);

        userEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(
            BASE_URL + casResetPasswordUrl,
            Boolean.class,
            EMAIL,
            FIRSTNAME,
            LASTNAME,
            "fr",
            CUSTOMER_ID
        );
    }

    @Test
    public void testSendEmailKoUserIsNotNominative() {
        final UserDto user = buildUser();
        user.setType(UserTypeEnum.GENERIC);

        userEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(
            BASE_URL + casResetPasswordUrl,
            Boolean.class,
            EMAIL,
            FIRSTNAME,
            LASTNAME,
            "fr",
            CUSTOMER_ID
        );
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
