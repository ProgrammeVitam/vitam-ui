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
package fr.gouv.vitamui.cas.pm;

import static org.apereo.cas.web.flow.CasWebflowConfigurer.FLOW_ID_LOGIN;

import java.util.Locale;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.io.CommunicationsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

/**
 * Rest controller for CAS extra features.
 *
 *
 */
@RestController
@RequestMapping("/extras")
public class ResetPasswordController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ResetPasswordController.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private PasswordManagementService passwordManagementService;

    @Autowired
    private CommunicationsManager communicationsManager;

    @Autowired
    private PmTokenTicketFactory pmTokenTicketFactory;

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("messageSource")
    private HierarchicalMessageSource messageSource;

    @Autowired
    private Utils utils;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/resetPassword")
    public boolean resetPassword(@RequestParam(value = "username", defaultValue = "") final String username,
            @RequestParam(value = "firstname", defaultValue = "") final String firstname,
            @RequestParam(value = "lastname", defaultValue = "") final String lastname, @RequestParam(value = "ttl", defaultValue = "") final String ttl,
            @RequestParam(value = "language", defaultValue = "en") final String language) {

        if (StringUtils.isBlank(username)) {
            LOGGER.warn("No username is provided");
            return false;
        }

        communicationsManager.validate();
        if (!communicationsManager.isMailSenderDefined()) {
            LOGGER.warn("CAS is unable to send password-reset emails given no settings are defined to account for email servers");
            return false;
        }
        final String usernameLower = username.toLowerCase();
        final String to = passwordManagementService.findEmail(usernameLower);
        if (StringUtils.isBlank(to)) {
            LOGGER.warn("No recipient is provided");
            return false;
        }

        final int expMinutes;
        if (PmMessageToSend.ONE_DAY.equals(ttl)) {
            expMinutes = 24 * 60;
        }
        else {
            expMinutes = (int) casProperties.getAuthn().getPm().getReset().getExpirationMinutes();
        }

        final String url = buildPasswordResetUrl(usernameLower, casProperties, expMinutes);
        final PmMessageToSend messageToSend = PmMessageToSend.buildMessage(messageSource, firstname, lastname, ttl, url, new Locale(language));

        LOGGER.debug("Generated password reset URL [{}] for: {} ({}); Link is only active for the next [{}] minute(s)", utils.sanitizePasswordResetUrl(url), to,
                messageToSend.getSubject(), expMinutes);
        if (!sendPasswordResetEmailToAccount(to, messageToSend.getSubject(), messageToSend.getText())) {
            return false;
        }

        return true;
    }

    protected String buildPasswordResetUrl(final String username, final CasConfigurationProperties casProperties, final int expMinutes) {
        final String token = createToken(username, expMinutes);
        return casProperties.getServer().getPrefix().concat('/' + FLOW_ID_LOGIN + '?'
            + PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN + '=').concat(token);
    }

    protected String createToken(final String to, final int expMinutes) {
        final PmTokenTicket ticket = pmTokenTicketFactory.create(to, expMinutes);
        ticketRegistry.addTicket(ticket);
        return ticket.getId();
    }

    protected boolean sendPasswordResetEmailToAccount(final String to, final String subject, final String msg) {
        return htmlEmail(msg, casProperties.getAuthn().getPm().getReset().getMail().getFrom(), subject, to, null, null);
    }

    private boolean htmlEmail(final String text, final String from, final String subject, final String to, final String cc, final String bcc) {
        try {
            if (mailSender == null || StringUtils.isBlank(text) || StringUtils.isBlank(from) || StringUtils.isBlank(subject) || StringUtils.isBlank(to)) {
                LOGGER.warn("Could not send email to [{}] because either no address/subject/text is found or email settings are not configured.", to);
                return false;
            }

            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(to);
            message.setContent(text, "text/html; charset=UTF-8");
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setPriority(1);

            if (StringUtils.isNotBlank(cc)) {
                helper.setCc(cc);
            }

            if (StringUtils.isNotBlank(bcc)) {
                helper.setBcc(bcc);
            }
            mailSender.send(message);
            return true;
        }
        catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return false;
    }
}
