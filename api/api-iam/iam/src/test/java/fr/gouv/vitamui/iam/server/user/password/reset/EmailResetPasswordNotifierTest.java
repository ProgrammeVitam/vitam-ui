/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.iam.server.user.password.reset;

import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.rest.client.RestClientFactory;
import fr.gouv.vitamui.iam.auth.contract.PasswordResetUrlDto;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailResetPasswordNotifierTest {

    private static final String CAS_PASSWORD_RESET_URL_PATH = "/reset-password-url";

    private static final String MAIL_SENDER_ADDRESS = "no-reply@vitamui.com";

    private static final String PLATFORM_NAME = "VITAM-UI";

    private static final String BASE_URL = "https://cas.dev.vitamui.com";

    @Mock
    private RestClientFactory restClientFactory;

    @Mock
    private UserInfoService userInfoService;

    @Mock
    private MessageSource iamMessageSource;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private EmailResetPasswordNotifier notifier;

    private UserDto userDto;

    private UserInfoDto userInfoDto;

    @BeforeEach
    void setUp() {
        notifier = new EmailResetPasswordNotifier(
            restClientFactory,
            userInfoService,
            iamMessageSource,
            mailSender,
            CAS_PASSWORD_RESET_URL_PATH,
            MAIL_SENDER_ADDRESS,
            PLATFORM_NAME
        );

        userDto = new UserDto();
        userDto.setId("user-1");
        userDto.setEmail("john.doe@example.com");
        userDto.setFirstname("John");
        userDto.setLastname("Doe");
        userDto.setCustomerId("customer-1");
        userDto.setUserInfoId("user-info-1");

        userInfoDto = new UserInfoDto();
        userInfoDto.setLanguage(LanguageDto.FRENCH.name());
    }

    @Test
    void notify_should_send_mail_when_reset_url_and_language_are_resolved() {
        mockRestClientChain();
        final PasswordResetUrlDto passwordResetUrl = new PasswordResetUrlDto();
        passwordResetUrl.setUrl("https://dev.vitamui.com/reset?token=abc");
        passwordResetUrl.setExpirationInMinutes(120L);
        when(responseSpec.body(PasswordResetUrlDto.class)).thenReturn(passwordResetUrl);

        when(userInfoService.getOne("user-info-1")).thenReturn(userInfoDto);
        when(iamMessageSource.getMessage(eq("iam.password.reset.subject"), any(), any(Locale.class))).thenReturn(
            "Réinitialisation de votre mot de passe"
        );
        when(iamMessageSource.getMessage(eq("iam.password.reset.text"), any(), any(Locale.class))).thenReturn(
            "<p>Bonjour John</p>"
        );

        final MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        notifier.notify(userDto);

        verify(mailSender).send(mimeMessage);

        final ArgumentCaptor<Object[]> argumentsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(iamMessageSource).getMessage(
            eq("iam.password.reset.text"),
            argumentsCaptor.capture(),
            eq(Locale.FRENCH)
        );
        final Object[] arguments = argumentsCaptor.getValue();
        assertThat(arguments[0]).isEqualTo("John");
        assertThat(arguments[1]).isEqualTo("Doe");
        assertThat(arguments[2]).isEqualTo(2L); // 120 minutes / 60
        assertThat(arguments[3]).isEqualTo("https://dev.vitamui.com/reset?token=abc");
        assertThat(arguments[4]).isEqualTo(PLATFORM_NAME);
    }

    @Test
    void notify_should_throw_when_cas_returns_no_reset_url() {
        mockRestClientChain();
        when(responseSpec.body(PasswordResetUrlDto.class)).thenReturn(null);

        assertThatThrownBy(() -> notifier.notify(userDto))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("user-1");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notify_should_throw_when_reset_url_field_is_null() {
        mockRestClientChain();
        final PasswordResetUrlDto passwordResetUrl = new PasswordResetUrlDto();
        passwordResetUrl.setUrl(null);
        when(responseSpec.body(PasswordResetUrlDto.class)).thenReturn(passwordResetUrl);

        assertThatThrownBy(() -> notifier.notify(userDto)).isInstanceOf(IllegalStateException.class);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notify_should_throw_when_cas_call_fails() {
        doReturn(restClient).when(restClientFactory).getRestClient();
        when(restClientFactory.getBaseUrl()).thenReturn(BASE_URL);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        when(requestHeadersUriSpec.uri(anyString(), anyMap())).thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> notifier.notify(userDto)).isInstanceOf(IllegalStateException.class);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notify_should_throw_when_mail_sending_fails() {
        mockRestClientChain();
        final PasswordResetUrlDto passwordResetUrl = new PasswordResetUrlDto();
        passwordResetUrl.setUrl("https://dev.vitamui.com/reset?token=abc");
        passwordResetUrl.setExpirationInMinutes(60L);
        when(responseSpec.body(PasswordResetUrlDto.class)).thenReturn(passwordResetUrl);

        when(userInfoService.getOne("user-info-1")).thenReturn(userInfoDto);
        when(iamMessageSource.getMessage(eq("iam.password.reset.subject"), any(), any(Locale.class))).thenReturn(
            "Sujet"
        );
        when(iamMessageSource.getMessage(eq("iam.password.reset.text"), any(), any(Locale.class))).thenReturn("Texte");

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("smtp unavailable"));

        assertThatThrownBy(() -> notifier.notify(userDto))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unable to send reset password email");
    }

    private void mockRestClientChain() {
        doReturn(restClient).when(restClientFactory).getRestClient();
        when(restClientFactory.getBaseUrl()).thenReturn(BASE_URL);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), anyMap());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    }
}
