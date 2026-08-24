package fr.gouv.vitamui.iam.server.user.service;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.VitamuiRestClientFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.cas.PasswordResetUrlDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link UserEmailService}.
 */
final class UserEmailServiceTest {

    private static final String FIRSTNAME = "John";

    private static final String LASTNAME = "Doe";

    private static final String CUSTOMER_ID = "customerId";

    private static final String EMAIL = "john.doe@vitamui.com";

    private static final String BASE_URL = "http://mycassserver";

    private static final String RESET_URL = "https://cas.vitamui.com/cas/login?pswdrst=PWDRST-1";

    private static final String PATH = "/cas/extras/passwordResetUrl";

    private IdentityProviderHelper identityProviderHelper;

    private IdentityProviderService identityProviderService;

    private VitamuiRestClientFactory vitamuiRestClientFactory;

    private RestClient restClient;

    private RestClient.RequestHeadersUriSpec uriSpec;

    private RestClient.ResponseSpec responseSpec;

    private MessageSource messageSource;

    private JavaMailSender mailSender;

    private UserEmailService userEmailService;

    private UserInfoService userInfoService;

    @BeforeEach
    public void setUp() {
        identityProviderHelper = mock(IdentityProviderHelper.class);
        userInfoService = mock(UserInfoService.class);
        identityProviderService = mock(IdentityProviderService.class);
        vitamuiRestClientFactory = mock(VitamuiRestClientFactory.class);
        restClient = mock(RestClient.class);
        uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        messageSource = mock(MessageSource.class);
        mailSender = mock(JavaMailSender.class);

        when(vitamuiRestClientFactory.getRestClient()).thenReturn(restClient);
        when(vitamuiRestClientFactory.getBaseUrl()).thenReturn(BASE_URL);
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(String.class), any(Map.class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PasswordResetUrlDto.class)).thenReturn(new PasswordResetUrlDto(RESET_URL, 24 * 60L));
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("message");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((jakarta.mail.Session) null));

        userEmailService = new UserEmailService(vitamuiRestClientFactory);
        userEmailService.setInternalIdentityProviderService(identityProviderService);
        userEmailService.setIdentityProviderHelper(identityProviderHelper);
        userEmailService.setUserInfoService(userInfoService);
        userEmailService.setCasPasswordResetUrlPath(PATH);
        userEmailService.setIamMessageSource(messageSource);
        userEmailService.setMailSender(mailSender);
        userEmailService.setMailSenderAddress("noreply@vitamui.com");
        userEmailService.setPlatformName("VITAM-UI");

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
            BASE_URL + PATH + "?email={email}&customerId={customerId}",
            Map.of("email", EMAIL, "customerId", CUSTOMER_ID)
        );
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testTheEmailIsBuiltFromTheLinkReturnedByCas() {
        userEmailService.sendCreationEmail(buildUser());

        final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(messageSource).getMessage(
            eq("iam.password.initialization.text"),
            captor.capture(),
            eq(Locale.forLanguageTag("fr"))
        );

        assertThat(captor.getValue()).containsExactly(FIRSTNAME, LASTNAME, 24L, RESET_URL, "VITAM-UI");
    }

    @Test
    void testSendEmailWhenCasIsUnreachable() {
        when(responseSpec.body(PasswordResetUrlDto.class)).thenThrow(new RuntimeException("CAS is down"));

        assertThatCode(() -> userEmailService.sendCreationEmail(buildUser())).doesNotThrowAnyException();

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWhenCasReturnsNoUrl() {
        when(responseSpec.body(PasswordResetUrlDto.class)).thenReturn(null);

        assertThatCode(() -> userEmailService.sendCreationEmail(buildUser())).doesNotThrowAnyException();

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWhenNoMailSenderIsConfigured() {
        userEmailService.setMailSender(null);

        assertThatCode(() -> userEmailService.sendCreationEmail(buildUser())).doesNotThrowAnyException();
    }

    @Test
    void testSendEmailWhenSendingFails() {
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> userEmailService.sendCreationEmail(buildUser())).doesNotThrowAnyException();
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
