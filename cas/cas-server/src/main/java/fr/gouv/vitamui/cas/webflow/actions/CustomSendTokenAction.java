package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.util.Utils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;

/**
 * The custom action to send SMS for the MFA simple token.
 */
@Slf4j
@RequiredArgsConstructor
public class CustomSendTokenAction extends AbstractMultifactorAuthenticationAction<CasSimpleMultifactorAuthenticationProvider> {
    private static final String MESSAGE_MFA_TOKEN_SENT = "cas.mfa.simple.label.tokensent";

    private final TicketRegistry ticketRegistry;

    private final CommunicationsManager communicationsManager;

    private final CasSimpleMultifactorAuthenticationTicketFactory ticketFactory;

    private final CasSimpleMultifactorAuthenticationProperties properties;

    private final CasSimpleMultifactorTokenCommunicationStrategy tokenCommunicationStrategy;

    private final Utils utils;

    private static boolean isSmsSent(final CommunicationsManager communicationsManager,
                                     final CasSimpleMultifactorAuthenticationProperties properties,
                                     final Principal principal,
                                     final Ticket token) {
        if (communicationsManager.isSmsSenderDefined()) {
            val smsProperties = properties.getSms();
            String smsText = StringUtils.isNotBlank(smsProperties.getText())
                ? smsProperties.getFormattedText(token.getId())
                : token.getId();
            // CUSTO: remove the prefix
            smsText = smsText.replace(CasSimpleMultifactorAuthenticationTicket.PREFIX + "-", "");
            return communicationsManager.sms(principal, smsProperties.getAttributeName(), smsText, smsProperties.getFrom());
        }
        return false;
    }

    private static boolean isMailSent(final CommunicationsManager communicationsManager,
                                      final CasSimpleMultifactorAuthenticationProperties properties,
                                      final Principal principal, final Ticket token,
                                      final RequestContext requestContext) {
        if (communicationsManager.isMailSenderDefined()) {
            val mailProperties = properties.getMail();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val body = EmailMessageBodyBuilder.builder().properties(mailProperties)
                .locale(Optional.ofNullable(request.getLocale()))
                .parameters(Map.of("token", token.getId())).build().produce();
            return communicationsManager.email(principal, mailProperties.getAttributeName(), mailProperties, body);
        }
        return false;
    }

    private static boolean isNotificationSent(final CommunicationsManager communicationsManager,
                                              final Principal principal,
                                              final Ticket token) {
        return communicationsManager.isNotificationSenderDefined()
            && communicationsManager.notify(principal, "Apereo CAS Token", String.format("Token: %s", token.getId()));
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getInProgressAuthentication();
        val principal = resolvePrincipal(authentication.getPrincipal());
        val token = getOrCreateToken(requestContext, principal);
        LOGGER.debug("Using token [{}] created at [{}]", token.getId(), token.getCreationTime());

        // CUSTO: check for a principal attribute and redirect to a custom page when missing
        val principalAttributes = principal.getAttributes();
        val mobile = (String) utils.getAttributeValue(principalAttributes, "mobile");
        if (mobile == null) {
            requestContext.getFlowScope().put("firstname", utils.getAttributeValue(principalAttributes, "firstname"));
            return getEventFactorySupport().event(this, "missingPhone");
        }

        val strategy = tokenCommunicationStrategy.determineStrategy(token);
        val smsSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.SMS)
            && isSmsSent(communicationsManager, properties, principal, token);

        val emailSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.EMAIL)
            && isMailSent(communicationsManager, properties, principal, token, requestContext);

        val notificationSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.NOTIFICATION)
            && isNotificationSent(communicationsManager, principal, token);

        if (smsSent || emailSent || notificationSent) {
            ticketRegistry.addTicket(token);
            LOGGER.debug("Successfully submitted token via strategy option [{}] to [{}]", strategy, principal.getId());

            // CUSTO: add the obfuscated phone to the webflow
            requestContext.getFlowScope().put("mobile", obfuscateMobile(mobile));

            WebUtils.addInfoMessageToContext(requestContext, MESSAGE_MFA_TOKEN_SENT);
            val attributes = new LocalAttributeMap<Object>("token", token.getId());
            WebUtils.putSimpleMultifactorAuthenticationToken(requestContext, token);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS, attributes);
        }
        LOGGER.error("Communication strategies failed to submit token [{}] to user", token.getId());
        return error();
    }

    private String obfuscateMobile(final String mobile) {
        String m = mobile.replaceFirst("\\+33", "0");
        m = m.substring(0, 2) + " XX XX XX " + m.substring(m.length() - 2);
        return m;
    }

    private CasSimpleMultifactorAuthenticationTicket getOrCreateToken(final RequestContext requestContext, final Principal principal) {
        return Optional.ofNullable(WebUtils.getSimpleMultifactorAuthenticationToken(requestContext, CasSimpleMultifactorAuthenticationTicket.class))
            .filter(token -> !token.isExpired())
            .orElseGet(() -> {
                WebUtils.removeSimpleMultifactorAuthenticationToken(requestContext);
                val service = WebUtils.getService(requestContext);
                val token = ticketFactory.create(service,
                    CollectionUtils.wrap(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
                LOGGER.debug("Created multifactor authentication token [{}] for service [{}]", token.getId(), service);
                return token;
            });
    }
}
