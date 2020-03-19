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
package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.pm.PmMessageToSend;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

/**
 * Send reset password emails with i18n messages.
 *
 *
 */
public class I18NSendPasswordResetInstructionsAction extends SendPasswordResetInstructionsAction {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory
            .getInstance(I18NSendPasswordResetInstructionsAction.class);

    @Autowired
    @Qualifier("messageSource")
    private HierarchicalMessageSource messageSource;

    @Autowired
    private ProvidersService providersService;

    @Autowired
    private IdentityProviderHelper identityProviderHelper;

    @Autowired
    private Utils utils;

    private final CasConfigurationProperties casProperties;

    private final CommunicationsManager communicationsManager;

    @Autowired
    private JavaMailSender mailSender;

    private final PasswordManagementService passwordManagementService;

    public I18NSendPasswordResetInstructionsAction(final CasConfigurationProperties casProperties,
                                                   final CommunicationsManager communicationsManager,
                                                   final PasswordManagementService passwordManagementService,
                                                   final TicketRegistry ticketRegistry,
                                                   final TicketFactory ticketFactory) {
        super(casProperties, communicationsManager, passwordManagementService, ticketRegistry, ticketFactory);
        this.casProperties = casProperties;
        this.communicationsManager = communicationsManager;
        this.passwordManagementService = passwordManagementService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        communicationsManager.validate();
        if (!communicationsManager.isMailSenderDefined()) {
            LOGGER.warn(
                    "CAS is unable to send password-reset emails given no settings are defined to account for email servers");
            return error();
        }
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        // JLE: changed from CAS
        String username = request.getParameter("username");
        if (StringUtils.isBlank(username)) {
            // try to get the username from the credentials also (after a password expiration)
            final MutableAttributeMap flowScope = requestContext.getFlowScope();
            final Object credential = flowScope.get("credential");
            if (credential instanceof UsernamePasswordCredential) {
                final UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;
                username = usernamePasswordCredential.getUsername();
            }
        }

        if (StringUtils.isBlank(username)) {
            LOGGER.warn("No username is provided");
            return error();
        }

        // JLE: changed from CAS
        final String to = passwordManagementService.findEmail(username);
        if (StringUtils.isBlank(to)) {
            LOGGER.warn("No recipient is provided; nonetheless, we return to the success page");
            return success();
        } else if (!identityProviderHelper.identifierMatchProviderPattern(providersService.getProviders(), to)) {
            LOGGER.warn("Recipient: {} is not internal; ignoring and returning to the success page", to);
            return success();
        }

        val service = WebUtils.getService(requestContext);
        final String url = buildPasswordResetUrl(username, passwordManagementService, casProperties, service);

        LOGGER.debug("Generated password reset URL [{}]; Link is only active for the next [{}] minute(s)",
                utils.sanitizePasswordResetUrl(url), pm.getReset().getExpirationMinutes());
        if (sendPasswordResetEmailToAccount(to, url)) {
            return success();
        }
        LOGGER.error("Failed to notify account [{}]", to);
        return error();
    }

    @Override
    protected boolean sendPasswordResetEmailToAccount(final String to, final String url) {
        final PmMessageToSend messageToSend = PmMessageToSend.buildMessage(messageSource, "", "", "10", url,
                LocaleContextHolder.getLocale());
        return htmlEmail(messageToSend.getText(), casProperties.getAuthn().getPm().getReset().getMail().getFrom(),
                messageToSend.getSubject(), to, null, null);
    }

    private boolean htmlEmail(final String text, final String from, final String subject, final String to,
            final String cc, final String bcc) {
        try {
            if (this.mailSender == null || StringUtils.isBlank(text) || StringUtils.isBlank(from)
                    || StringUtils.isBlank(subject) || StringUtils.isBlank(to)) {
                LOGGER.warn(
                        "Could not send email to [{}] because either no address/subject/text is found or email settings are not configured.",
                        to);
                return false;
            }

            final MimeMessage message = this.mailSender.createMimeMessage();
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
            this.mailSender.send(message);
            return true;
        }
        catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return false;
    }
}
