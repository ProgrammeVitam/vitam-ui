package org.apereo.cas.mfa.simple.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;

/**
 * To be removed when upgrading to CAS version >= 6.6.3.
 */

public class CasSimpleMultifactorSendTokenAction
    extends AbstractMultifactorAuthenticationAction<CasSimpleMultifactorAuthenticationProvider> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        CasSimpleMultifactorSendTokenAction.class
    );

    private static final String MESSAGE_MFA_TOKEN_SENT = "cas.mfa.simple.label.tokensent";

    private final CommunicationsManager communicationsManager;

    private final CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    private final CasSimpleMultifactorAuthenticationProperties properties;

    private final CasSimpleMultifactorTokenCommunicationStrategy tokenCommunicationStrategy;

    private final BucketConsumer bucketConsumer;

    public CasSimpleMultifactorSendTokenAction(
        final CommunicationsManager communicationsManager,
        final CasSimpleMultifactorAuthenticationService multifactorAuthenticationService,
        final CasSimpleMultifactorAuthenticationProperties properties,
        final CasSimpleMultifactorTokenCommunicationStrategy tokenCommunicationStrategy,
        final BucketConsumer bucketConsumer
    ) {
        this.communicationsManager = communicationsManager;
        this.multifactorAuthenticationService = multifactorAuthenticationService;
        this.properties = properties;
        this.tokenCommunicationStrategy = tokenCommunicationStrategy;
        this.bucketConsumer = bucketConsumer;
    }

    protected boolean isSmsSent(
        final CommunicationsManager communicationsManager,
        final CasSimpleMultifactorAuthenticationProperties properties,
        final Principal principal,
        final Ticket tokenTicket,
        final RequestContext requestContext
    ) {
        if (communicationsManager.isSmsSenderDefined()) {
            var smsProperties = properties.getSms();
            var token = tokenTicket.getId();
            // CUSTO:
            var tokenWithoutPrefix = token.substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
            var smsText = StringUtils.isNotBlank(smsProperties.getText())
                ? SmsBodyBuilder.builder()
                    .properties(smsProperties)
                    .parameters(Map.of("token", token, "tokenWithoutPrefix", tokenWithoutPrefix))
                    .build()
                    .get()
                : token;

            var smsRequest = SmsRequest.builder()
                .from(smsProperties.getFrom())
                .principal(principal)
                .attribute(smsProperties.getAttributeName())
                .text(smsText)
                .build();
            try {
                return communicationsManager.sms(smsRequest);
            } catch (final Throwable e) {
                LOGGER.error("Error sending SMS", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Send an email.
     *
     * @param communicationsManager the communication manager
     * @param properties            the properties
     * @param principal             the principal
     * @param tokenTicket           the token
     * @param requestContext        the request context
     * @return whether the email has been sent.
     */
    protected EmailCommunicationResult isMailSent(
        final CommunicationsManager communicationsManager,
        final CasSimpleMultifactorAuthenticationProperties properties,
        final Principal principal,
        final Ticket tokenTicket,
        final RequestContext requestContext
    ) {
        if (communicationsManager.isMailSenderDefined()) {
            var mailProperties = properties.getMail();
            var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            var parameters = CoreAuthenticationUtils.convertAttributeValuesToObjects(principal.getAttributes());

            var token = tokenTicket.getId();
            var tokenWithoutPrefix = token.substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
            parameters.put("token", token);
            // CUSTO:
            parameters.put("tokenWithoutPrefix", tokenWithoutPrefix);

            var locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request)).map(
                resolver -> resolver.resolveLocale(request)
            );
            var body = EmailMessageBodyBuilder.builder()
                .properties(mailProperties)
                .locale(locale)
                .parameters(parameters)
                .build()
                .get();
            var emailRequest = EmailMessageRequest.builder()
                .emailProperties(mailProperties)
                .principal(principal)
                .attribute(mailProperties.getAttributeName().get(0)) // CAS 7 change:
                // attributeName is a list
                .body(body)
                .build();
            try {
                return communicationsManager.email(emailRequest);
            } catch (final Throwable e) {
                LOGGER.error("Error sending email", e);
                return EmailCommunicationResult.builder().build();
            }
        }
        return EmailCommunicationResult.builder().build();
    }

    protected boolean isNotificationSent(
        final CommunicationsManager communicationsManager,
        final Principal principal,
        final Ticket token
    ) {
        return (
            communicationsManager.isNotificationSenderDefined() &&
            communicationsManager.notify(principal, "Apereo CAS Token", String.format("Token: %s", token.getId()))
        );
    }

    @Override
    protected Event doPreExecute(final RequestContext requestContext) throws Exception {
        var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        var authentication = WebUtils.getInProgressAuthentication();
        var result = bucketConsumer.consume(getThrottledRequestKeyFor(authentication, requestContext));
        result.getHeaders().forEach(response::addHeader);
        return result.isConsumed() ? super.doPreExecute(requestContext) : error();
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        var authentication = WebUtils.getInProgressAuthentication();
        var principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        var token = getOrCreateToken(requestContext, principal);
        LOGGER.debug("Using token [{}] created at [{}]", token.getId(), token.getCreationTime());

        var strategy = tokenCommunicationStrategy.determineStrategy(token);
        var smsSent =
            strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.SMS) &&
            isSmsSent(communicationsManager, properties, principal, token, requestContext);

        var emailSent =
            strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.EMAIL) &&
            isMailSent(communicationsManager, properties, principal, token, requestContext).isSuccess();

        var notificationSent =
            strategy.contains(
                CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.NOTIFICATION
            ) &&
            isNotificationSent(communicationsManager, principal, token);

        if (smsSent || emailSent || notificationSent) {
            try {
                multifactorAuthenticationService.store(token);
            } catch (final Throwable e) {
                LOGGER.error("Error storing token", e);
                return error();
            }
            LOGGER.debug("Successfully submitted token via strategy option [{}] to [{}]", strategy, principal.getId());
            WebUtils.addInfoMessageToContext(requestContext, MESSAGE_MFA_TOKEN_SENT);
            var attributes = new LocalAttributeMap<Object>("token", token.getId());
            WebUtils.putSimpleMultifactorAuthenticationToken(requestContext, token);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS, attributes);
        }
        LOGGER.error("Communication strategies failed to submit token [{}] to user", token.getId());
        return error();
    }

    /**
     * Get or create a token.
     *
     * @param requestContext the request context
     * @param principal      the principal
     * @return the token
     */
    protected CasSimpleMultifactorAuthenticationTicket getOrCreateToken(
        final RequestContext requestContext,
        final Principal principal
    ) {
        var currentToken = WebUtils.getSimpleMultifactorAuthenticationToken(
            requestContext,
            CasSimpleMultifactorAuthenticationTicket.class
        );
        return Optional.ofNullable(currentToken)
            .filter(token -> !token.isExpired())
            .orElseGet(
                Unchecked.supplier(() -> {
                    WebUtils.removeSimpleMultifactorAuthenticationToken(requestContext);
                    var service = WebUtils.getService(requestContext);
                    return multifactorAuthenticationService.generate(principal, service);
                })
            );
    }

    private String getThrottledRequestKeyFor(final Authentication authentication, final RequestContext requestContext) {
        var principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        return principal.getId();
    }
}
