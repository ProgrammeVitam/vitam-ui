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
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Service("resetPasswordNotifier")
public class EmailResetPasswordNotifier implements ResetPasswordNotifier<UserDto> {

    private static final String SUBJECT_KEY = "iam.password.initialization.subject";

    private static final String TEXT_KEY = "iam.password.initialization.text";

    private static final long MINUTES_PER_HOUR = 60L;

    private final RestClientFactory restClientFactory;

    private final UserInfoService userInfoService;

    private final MessageSource iamMessageSource;

    private final JavaMailSender mailSender;

    private final String casPasswordResetUrlPath;

    private final String mailSenderAddress;

    private final String platformName;

    @Autowired
    public EmailResetPasswordNotifier(
        @Qualifier("casRestClientFactory") final RestClientFactory restClientFactory,
        final UserInfoService userInfoService,
        final MessageSource iamMessageSource,
        @Autowired(required = false) final JavaMailSender mailSender,
        @Value("${cas.password-reset-url.path}") final String casPasswordResetUrlPath,
        @Value("${mail.sender}") final String mailSenderAddress,
        @Value("${mail.platform-name:VITAM-UI}") final String platformName
    ) {
        this.restClientFactory = restClientFactory;
        this.userInfoService = userInfoService;
        this.iamMessageSource = iamMessageSource;
        this.mailSender = mailSender;
        this.casPasswordResetUrlPath = casPasswordResetUrlPath;
        this.mailSenderAddress = mailSenderAddress;
        this.platformName = platformName;
    }

    @Override
    public void notify(final UserDto target) {
        log.debug("Sending reset password email: {}", target.getId());

        final PasswordResetUrlDto passwordResetUrl = fetchPasswordResetUrl(target);
        if (passwordResetUrl == null || passwordResetUrl.getUrl() == null) {
            throw new IllegalStateException("Unable to obtain a password reset URL for " + target.getId());
        }

        final UserInfoDto userInfoDto = userInfoService.getOne(target.getUserInfoId());
        final Locale locale = Locale.forLanguageTag(LanguageDto.valueOf(userInfoDto.getLanguage()).getLanguage());

        final Object[] arguments = new Object[] {
            target.getFirstname(),
            target.getLastname(),
            passwordResetUrl.getExpirationInMinutes() / MINUTES_PER_HOUR,
            passwordResetUrl.getUrl(),
            platformName,
        };
        final String subject = iamMessageSource.getMessage(SUBJECT_KEY, null, locale);
        final String text = iamMessageSource.getMessage(TEXT_KEY, arguments, locale);

        sendHtmlEmail(target, subject, text);
    }

    private PasswordResetUrlDto fetchPasswordResetUrl(final UserDto userDto) {
        final Map<String, Object> uriVariables = Map.of(
            "email",
            userDto.getEmail(),
            "customerId",
            userDto.getCustomerId()
        );

        try {
            return restClientFactory
                .getRestClient()
                .get()
                .uri(
                    restClientFactory.getBaseUrl() + casPasswordResetUrlPath + "?email={email}&customerId={customerId}",
                    uriVariables
                )
                .retrieve()
                .body(PasswordResetUrlDto.class);
        } catch (final Exception e) {
            log.error(
                "Cannot obtain a password reset URL for {} (customerId {})",
                userDto.getEmail(),
                userDto.getCustomerId(),
                e
            );
            return null;
        }
    }

    private void sendHtmlEmail(final UserDto userDto, final String subject, final String text) {
        if (mailSender == null) {
            log.error(
                "No mail sender is configured; the reset password email to {} has not been sent",
                userDto.getEmail()
            );
            throw new IllegalStateException("No mail sender configured");
        }

        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(userDto.getEmail());
            helper.setSubject(subject);
            helper.setFrom(mailSenderAddress);
            helper.setPriority(1);
            message.setContent(text, "text/html; charset=UTF-8");
            mailSender.send(message);
        } catch (final Exception e) {
            log.error(
                "The reset password email to {} (customerId {}) has not been sent",
                userDto.getEmail(),
                userDto.getCustomerId(),
                e
            );
            throw new IllegalStateException("Unable to send reset password email", e);
        }
    }
}
