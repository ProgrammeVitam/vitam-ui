package fr.gouv.vitamui.iam.internal.server.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.RestClientFactory;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;

/**
 * Tests {@link UserEmailInternalService}.
 *
 *
 */
public final class UserEmailInternalServiceTest {

    private static final String LANGUAGE = "FRENCH";

    private static final String LASTNAME = "Leleu";

    private static final String FIRSTNAME = "Jérome";

    private static final String EMAIL = "jerome.leleu@vitamui.com";

    private static final String BASE_URL = "http://mycassserver";

    private IdentityProviderHelper identityProviderHelper;

    private IdentityProviderInternalService internalIdentityProviderService;

    private RestClientFactory restClientFactory;

    private RestTemplate restTemplate;

    private UserEmailInternalService internalUserEmailService;

    private final String casResetPasswordUrl = "/cas/extras/resetPassword?username={username}&firstname={firstname}&lastname={lastname}&language={language}&ttl=1day";

    @Before
    public void setUp() {
        identityProviderHelper = mock(IdentityProviderHelper.class);
        internalIdentityProviderService = mock(IdentityProviderInternalService.class);
        restClientFactory = mock(RestClientFactory.class);
        restTemplate = mock(RestTemplate.class);
        when(restClientFactory.getRestTemplate()).thenReturn(restTemplate);
        when(restClientFactory.getBaseUrl()).thenReturn(BASE_URL);
        internalUserEmailService = new UserEmailInternalService(restClientFactory);
        internalUserEmailService.setInternalIdentityProviderService(internalIdentityProviderService);
        internalUserEmailService.setIdentityProviderHelper(identityProviderHelper);
        internalUserEmailService.setCasResetPasswordUrl(casResetPasswordUrl);
        final List<IdentityProviderDto> providers = new ArrayList<>();
        when(internalIdentityProviderService.getAll(Optional.empty(), Optional.empty())).thenReturn(providers);
        when(identityProviderHelper.identifierMatchProviderPattern(providers, EMAIL)).thenReturn(true);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void testSendEmailOk() {
        final UserDto user = buildUser();

        internalUserEmailService.sendCreationEmail(user);

        verify(restTemplate).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME, LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoNoUser() {

        internalUserEmailService.sendCreationEmail(null);

        verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME, LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoUserIsDisabled() {
        final UserDto user = buildUser();
        user.setStatus(UserStatusEnum.DISABLED);

        internalUserEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME, LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoUserCannotLogin() {
        final UserDto user = buildUser();
        user.setStatus(UserStatusEnum.BLOCKED);

        internalUserEmailService.sendCreationEmail(user);

        Mockito.verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME, LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoUserIsNotInternal() {
        final UserDto user = buildUser();
        when(identityProviderHelper.identifierMatchProviderPattern(any(List.class), eq(EMAIL))).thenReturn(false);

        internalUserEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME, LASTNAME, "fr");
    }

    @Test
    public void testSendEmailKoUserIsNotNominative() {
        final UserDto user = buildUser();
        user.setType(UserTypeEnum.GENERIC);

        internalUserEmailService.sendCreationEmail(user);

        verify(restTemplate, times(0)).getForEntity(BASE_URL + casResetPasswordUrl, Boolean.class, EMAIL, FIRSTNAME, LASTNAME, "fr");
    }

    private UserDto buildUser() {
        final UserDto user = new UserDto();
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setEmail(EMAIL);
        user.setFirstname(FIRSTNAME);
        user.setLastname(LASTNAME);
        user.setLanguage("FRENCH");
        return user;
    }
}
