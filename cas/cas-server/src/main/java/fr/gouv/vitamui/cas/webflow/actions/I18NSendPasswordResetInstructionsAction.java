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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.cas.model.UserLoginModel;
import fr.gouv.vitamui.cas.pm.PmMessageToSend;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URL;
import java.util.Objects;

/**
 * Send reset password emails with i18n messages.
 */
public class I18NSendPasswordResetInstructionsAction extends SendPasswordResetInstructionsAction {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(I18NSendPasswordResetInstructionsAction.class);

    private final HierarchicalMessageSource messageSource;

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    private final Utils utils;

    private final String vitamuiPlatformName;

    private final ObjectMapper objectMapper;

    public I18NSendPasswordResetInstructionsAction(final CasConfigurationProperties casProperties,
        final CommunicationsManager communicationsManager,
        final PasswordManagementService passwordManagementService,
        final TicketRegistry ticketRegistry,
        final TicketFactory ticketFactory,
        final PrincipalResolver principalResolver,
        final PasswordResetUrlBuilder passwordResetUrlBuilder,
        final HierarchicalMessageSource messageSource,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper,
        final Utils utils,
        final String vitamuiPlatformName) {
        super(casProperties, communicationsManager, passwordManagementService, ticketRegistry, ticketFactory,
            principalResolver, passwordResetUrlBuilder);
        this.messageSource = messageSource;
        this.providersService = providersService;
        this.identityProviderHelper = identityProviderHelper;
        this.utils = utils;
        this.vitamuiPlatformName = vitamuiPlatformName;
        this.objectMapper = new ObjectMapper();
    }

    @Audit(action = AuditableActions.REQUEST_CHANGE_PASSWORD,
        principalResolverName = "REQUEST_CHANGE_PASSWORD_PRINCIPAL_RESOLVER",
        actionResolverName = AuditActionResolvers.REQUEST_CHANGE_PASSWORD_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.REQUEST_CHANGE_PASSWORD_RESOURCE_RESOLVER)
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        if (!communicationsManager.isMailSenderDefined() && !communicationsManager.isSmsSenderDefined()) {
            return getErrorEvent("contact.failed", "Unable to send email as no mail sender is defined", requestContext);
        }

        val query = buildPasswordManagementQuery(requestContext);

        val email = passwordManagementService.findEmail(query);
        val service = WebUtils.getService(requestContext);
        final String customerId = (String) query.getRecord().getFirst(Constants.RESET_PWD_CUSTOMER_ID_ATTR);

        // CUSTO: only retrieve email (and not phone) and force success event (instead of error) when failure
        if (StringUtils.isBlank(email) || customerId == null) {
            LOGGER.warn("No recipient is provided; nonetheless, we return to the success page");
            return success();
        } else if (!identityProviderHelper.identifierMatchProviderPattern(providersService.getProviders(),
            email, customerId)) {

            LOGGER.warn("Recipient: {} is not internal; ignoring and returning to the success page", email);
            return success();
        }

        // Hack: Encode loginEmail+loginCustomerId pair into a json-serialized UserLoginModel as we are not able to
        //       persist 2 separate fields.
        UserLoginModel userLoginModel = new UserLoginModel();
        userLoginModel.setUserEmail(email);
        userLoginModel.setCustomerId(customerId);
        String userLoginModelToToken = objectMapper.writeValueAsString(userLoginModel);

        val url = buildPasswordResetUrl(userLoginModelToToken, service);

        if (url != null) {
            val pm = casProperties.getAuthn().getPm();
            val duration = Beans.newDuration(pm.getReset().getExpiration());
            LOGGER.debug("Generated password reset URL [{}]; Link is only active for the next [{}] minute(s)", url,
                duration);
            // CUSTO: only send email (and not SMS)
            val sendEmail = sendPasswordResetEmailToAccount(email, url);
            if (sendEmail.isSuccess()) {
                return success(url);
            }
        } else {
            LOGGER.error("No password reset URL could be built and sent to [{}]", email);
        }
        LOGGER.error("Failed to notify account [{}]", email);
        return getErrorEvent("contact.failed", "Failed to send the password reset link via email address or phone",
            requestContext);
    }

    @Override
    protected PasswordManagementQuery buildPasswordManagementQuery(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final MutableAttributeMap<Object> flowScope = requestContext.getFlowScope();
        // CUSTO: try to get the username from the credentials also (after a password expiration)
        String username = request.getParameter(REQUEST_PARAMETER_USERNAME);
        if (StringUtils.isBlank(username)) {
            final Object credential = flowScope.get("credential");
            if (credential instanceof UsernamePasswordCredential) {
                final UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;
                username = usernamePasswordCredential.getUsername();
            }
        }
        final String loginEmail = (String) flowScope.get(Constants.FLOW_LOGIN_EMAIL);
        final String loginCustomerId = (String) flowScope.get(Constants.FLOW_LOGIN_CUSTOMER_ID);
        if (StringUtils.isAnyBlank(loginEmail, loginCustomerId)) {
            throw new IllegalStateException("Missing loginEmail or loginCustomer");
        }
        if (!Objects.equals(loginEmail, username)) {
            throw new IllegalStateException("Missing loginCustomerId (" + loginCustomerId + ") " +
                "mismatches username (" + username + ")");
        }

        LinkedMultiValueMap<String, Object> records = new LinkedMultiValueMap<>();
        records.add(Constants.RESET_PWD_CUSTOMER_ID_ATTR, loginCustomerId);

        val builder = PasswordManagementQuery.builder();
        return builder.username(username).record(records).build();
    }

    private EmailCommunicationResult sendPasswordResetEmailToAccount(
        final String to, final URL url) {
        val duration = Beans.newDuration(casProperties.getAuthn().getPm().getReset().getExpiration());

        final PmMessageToSend messageToSend = PmMessageToSend.buildMessage(messageSource, "", "",
            String.valueOf(duration.toMinutes()), url.toString(), vitamuiPlatformName, LocaleContextHolder.getLocale());
        return EmailCommunicationResult.builder().success(
            utils.htmlEmail(messageToSend.getText(), casProperties.getAuthn().getPm().getReset().getMail().getFrom(),
                messageToSend.getSubject(), to, null, null)).build();
    }

}
