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

import fr.gouv.vitamui.cas.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.CasSimpleMultifactorProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationHandler;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

@Slf4j
@RequiredArgsConstructor
public class CustomSendTokenAction extends AbstractAction {
    private static final String MESSAGE_MFA_TOKEN_SENT = "cas.mfa.simple.label.tokensent";

    private final TicketRegistry ticketRegistry;
    private final CommunicationsManager communicationsManager;
    private final TransientSessionTicketFactory ticketFactory;
    private final CasSimpleMultifactorProperties properties;
    private final Utils utils;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getInProgressAuthentication();
        val principal = authentication.getPrincipal();
        val principalAttributes = principal.getAttributes();

        // custo
        val mobile = (String) utils.getAttributeValue(principalAttributes, "mobile");
        if (mobile == null) {
            requestContext.getFlowScope().put("firstname", utils.getAttributeValue(principalAttributes, "firstname"));
            return getEventFactorySupport().event(this, "missingPhone");
        }

        val service = WebUtils.getService(requestContext);
        val token = ticketFactory.create(service, CollectionUtils.wrap(CasSimpleMultifactorAuthenticationHandler.PROPERTY_PRINCIPAL, principal));
        LOGGER.debug("Created multifactor authentication token [{}] for service [{}]", token, service);

        val smsSent = isSmsSent(communicationsManager, properties, principal, token);
        val emailSent = isMailSent(communicationsManager, properties, principal, token);

        if (smsSent || emailSent) {
            ticketRegistry.addTicket(token);
            LOGGER.debug("Successfully submitted token via SMS and/or email to [{}]", principal.getId());

            val resolver = new MessageBuilder()
                .info()
                .code(MESSAGE_MFA_TOKEN_SENT)
                .defaultText(MESSAGE_MFA_TOKEN_SENT)
                .build();
            requestContext.getMessageContext().addMessage(resolver);

            // custo
            requestContext.getFlowScope().put("mobile", obfuscateMobile(mobile));

            val attributes = new LocalAttributeMap("token", token.getId());
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS, attributes);
        }
        LOGGER.error("Both email and SMS communication strategies failed to submit token [{}] to user", token);
        return error();
    }

    private String obfuscateMobile(final String mobile) {
        String m = mobile.replaceFirst("\\+33", "0");
        m = m.substring(0, 2) + " XX XX XX " + m.substring(m.length() - 2);
        return m;
    }

    private boolean isSmsSent(final CommunicationsManager communicationsManager,
                                final CasSimpleMultifactorProperties properties,
                                final Principal principal,
                                final Ticket token) {
        if (communicationsManager.isSmsSenderDefined()) {
            val smsProperties = properties.getSms();
            String smsText = StringUtils.isNotBlank(smsProperties.getText())
                ? smsProperties.getFormattedText(token.getId())
                : token.getId();
            // custo: remove the CAS prefix
            smsText = smsText.replace(CasSimpleMultifactorAuthenticationTicketFactory.PREFIX + "-", "");
            return communicationsManager.sms(principal, smsProperties.getAttributeName(), smsText, smsProperties.getFrom());
        }
        return false;
    }

    private boolean isMailSent(final CommunicationsManager communicationsManager,
                                 final CasSimpleMultifactorProperties properties,
                                 final Principal principal,
                                 final Ticket token) {
        if (communicationsManager.isMailSenderDefined()) {
            val mailProperties = properties.getMail();
            return communicationsManager.email(principal, mailProperties.getAttributeName(), mailProperties, mailProperties.getFormattedBody(token.getId()));
        }
        return false;
    }
}
