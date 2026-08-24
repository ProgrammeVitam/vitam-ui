/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.iam.server.user.service;

import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.VitamuiRestClientFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.cas.PasswordResetUrlDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Send user email service.
 */
@Getter
@Setter
public class UserEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEmailService.class);

    private static final String SUBJECT_KEY = "iam.password.initialization.subject";

    private static final String TEXT_KEY = "iam.password.initialization.text";

    private static final long MINUTES_PER_HOUR = 60L;

    @Value("${cas.password-reset-url.path}")
    @NotNull
    @Setter
    private String casPasswordResetUrlPath;

    @Value("${iam.mail.sender}")
    @Setter
    private String mailSenderAddress;

    @Value("${iam.mail.platform-name:VITAM-UI}")
    @Setter
    private String platformName;

    @Autowired
    private IdentityProviderHelper identityProviderHelper;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private IdentityProviderService internalIdentityProviderService;

    @Autowired
    private MessageSource iamMessageSource;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final VitamuiRestClientFactory vitamuiRestClientFactory;

    public UserEmailService(final VitamuiRestClientFactory vitamuiRestClientFactory) {
        this.vitamuiRestClientFactory = vitamuiRestClientFactory;
    }

    public void sendCreationEmailAfterCommit(final UserDto userDto) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sendCreationEmail(userDto);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendCreationEmail(userDto);
                }
            }
        );
    }

    public void sendCreationEmail(final UserDto userDto) {
        if (
            userDto != null &&
            userDto.getStatus() == UserStatusEnum.ENABLED &&
            userDto.getType() == UserTypeEnum.NOMINATIVE
        ) {
            final List<IdentityProviderDto> providers = internalIdentityProviderService.getAll(
                Optional.empty(),
                Optional.empty()
            );
            if (
                identityProviderHelper.identifierMatchProviderPattern(
                    providers,
                    userDto.getEmail(),
                    userDto.getCustomerId()
                )
            ) {
                LOGGER.debug("Sending mail after creating  user: {}", userDto.getEmail());
                final UserInfoDto userInfoDto = userInfoService.getOne(userDto.getUserInfoId());
                sendPasswordInitializationEmail(userDto, userInfoDto);
            }
        }
    }

    private void sendPasswordInitializationEmail(final UserDto userDto, final UserInfoDto userInfoDto) {
        final PasswordResetUrlDto passwordResetUrl = fetchPasswordResetUrl(userDto);
        if (passwordResetUrl == null || passwordResetUrl.getUrl() == null) {
            LOGGER.error(
                "No password reset URL could be obtained for {} (customerId {}); no email sent",
                userDto.getEmail(),
                userDto.getCustomerId()
            );
            return;
        }

        final Locale locale = Locale.forLanguageTag(
            LanguageDto.valueOf(userInfoDto.getLanguage()).getLanguage()
        );
        final Object[] arguments = new Object[] {
            userDto.getFirstname(),
            userDto.getLastname(),
            passwordResetUrl.getExpirationInMinutes() / MINUTES_PER_HOUR,
            passwordResetUrl.getUrl(),
            platformName,
        };
        final String subject = iamMessageSource.getMessage(SUBJECT_KEY, null, locale);
        final String text = iamMessageSource.getMessage(TEXT_KEY, arguments, locale);

        sendHtmlEmail(userDto, subject, text);
    }

    private PasswordResetUrlDto fetchPasswordResetUrl(final UserDto userDto) {
        final Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("email", userDto.getEmail());
        uriVariables.put("customerId", userDto.getCustomerId());

        try {
            return vitamuiRestClientFactory
                .getRestClient()
                .get()
                .uri(
                    vitamuiRestClientFactory.getBaseUrl() +
                    casPasswordResetUrlPath +
                    "?email={email}&customerId={customerId}",
                    uriVariables
                )
                .retrieve()
                .body(PasswordResetUrlDto.class);
        } catch (final Exception e) {
            LOGGER.error(
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
            LOGGER.error(
                "No mail sender is configured; the password initialization email to {} (customerId {}) has not been sent",
                userDto.getEmail(),
                userDto.getCustomerId()
            );
            return;
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
            LOGGER.error(
                "The password initialization email to {} (customerId {}) has not been sent",
                userDto.getEmail(),
                userDto.getCustomerId(),
                e
            );
        }
    }
}
