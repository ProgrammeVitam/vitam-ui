package fr.gouv.vitamui.iam.internal.server.user.service;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.RestClientFactory;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.junit.Before;
import org.junit.Test;
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
 * Tests {@link UserEmailInternalService}.
 */
public final class UserEmailInternalServiceTest {


    private static final String LASTNAME = "John";

    private static final String FIRSTNAME = "Doe";
    private static final String CUSTOMER_ID = "CustomerId";

    private static final String EMAIL = "john.doe@vitamui.com";

    private static final String BASE_URL = "http://mycassserver";

    private IdentityProviderHelper identityProviderHelper;

    private IdentityProviderInternalService internalIdentityProviderService;

    private RestClientFactory restClientFactory;

    private RestTemplate restTemplate;

    private UserEmailInternalService internalUserEmailService;

    private UserInfoInternalService userInfoInternalService;

    private final String casResetPasswordUrl =
        "/cas/extras/resetPassword?username={username}&firstname={firstname}&lastname={lastname}&language={language}&ttl=1day";

    @Before
    public void setUp() {
        identityProviderHelper = mock(IdentityProviderHelper.class);
        userInfoInternalService = mock(UserInfoInternalService.class);
        internalIdentityProviderService = mock(IdentityProviderInternalService.class);
        restClientFactory = mock(RestClientFactory.class);
        restTemplate = mock(RestTemplate.class);
        when(restClientFactory.getRestTemplate()).thenReturn(restTemplate);
        when(restClientFactory.getBaseUrl()).thenReturn(BASE_URL);
        internalUserEmailService = new UserEmailInternalService(restClientFactory);
        internalUserEmailService.setInternalIdentityProviderService(internalIdentityProviderService);
        internalUserEmailService.setIdentityProviderHelper(identityProviderHelper);
        internalUserEmailService.setUserInfoInternalService(userInfoInternalService);
        internalUserEmailService.setCasResetPasswordUrl(casResetPasswordUrl);
        final List<IdentityProviderDto> providers = new ArrayList<>();
        when(internalIdentityProviderService.getAll(Optional.empty(), Optional.empty())).thenReturn(providers);
        when(identityProviderHelper.identifierMatchProviderPattern(providers, EMAIL, CUSTOMER_ID)).thenReturn(true);
        when(userInfoInternalService.getOne(any())).thenReturn(buildUserInfoDto());
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void testSendEmailOk() {
        final UserDto user = buildUser();

        internalUserEmailService.sendCreationEmail(user);

        verify(restTemplate).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME, LASTNAME,
            "fr", CUSTOMER_ID);
    }

    @Test
    public void testSendEmailKoNoUser() {

        internalUserEmailService.sendCreationEmail(null);

        verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME,
            LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoUserIsDisabled() {
        final UserDto user = buildUser();
        user.setStatus(UserStatusEnum.DISABLED);

        internalUserEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME,
            LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoUserCannotLogin() {
        final UserDto user = buildUser();
        user.setStatus(UserStatusEnum.BLOCKED);

        internalUserEmailService.sendCreationEmail(user);

        Mockito.verify(restTemplate, times(0))
            .getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME, LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoUserIsNotInternal() {
        final UserDto user = buildUser();
        when(identityProviderHelper.identifierMatchProviderPattern(any(List.class), eq(EMAIL), eq(CUSTOMER_ID)))
            .thenReturn(false);

        internalUserEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME,
            LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoUserIsNotNominative() {
        final UserDto user = buildUser();
        user.setType(UserTypeEnum.GENERIC);

        internalUserEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME,
            LASTNAME, "fr");
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

