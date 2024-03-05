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

import fr.gouv.vitamui.cas.provider.Pac4jClientIdentityProviderDto;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.util.regex.Pattern;

import static fr.gouv.vitamui.cas.authentication.UserPrincipalResolver.EMAIL_VALID_REGEXP;

/**
 * Custom authentication delegation:
 * - automatic delegation given the provided IdP
 * - extraction of the username/surrogate passed as a request parameter
 * - save the portalUrl in the webflow.
 */
public class CustomDelegatedClientAuthenticationAction extends DelegatedClientAuthenticationAction {

    public static final Pattern CUSTOMER_ID_VALIDATION_PATTER = Pattern.compile("^[_a-z0-9]+$");

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(CustomDelegatedClientAuthenticationAction.class);

    private final IdentityProviderHelper identityProviderHelper;

    private final ProvidersService providersService;

    private final Utils utils;

    private final TicketRegistry ticketRegistry;

    private final String vitamuiPortalUrl;

    public CustomDelegatedClientAuthenticationAction(
        final DelegatedClientAuthenticationConfigurationContext configContext,
        final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager,
        final DelegatedClientAuthenticationFailureEvaluator failureEvaluator,
        final IdentityProviderHelper identityProviderHelper,
        final ProvidersService providersService,
        final Utils utils,
        final TicketRegistry ticketRegistry,
        final String vitamuiPortalUrl) {
        super(configContext, delegatedClientAuthenticationWebflowManager, failureEvaluator);
        this.identityProviderHelper = identityProviderHelper;
        this.providersService = providersService;
        this.utils = utils;
        this.ticketRegistry = ticketRegistry;
        this.vitamuiPortalUrl = vitamuiPortalUrl;
    }

    @Override
    public Event doExecute(final RequestContext context) {

        // save a label in the webflow
        val flowScope = context.getFlowScope();
        flowScope.put(Constants.PORTAL_URL, vitamuiPortalUrl);

        // retrieve the service if it exists to prepare the serviceUrl parameter (for the back links)
        val service = WebUtils.getService(context);
        if (service != null) {
            flowScope.put("serviceUrl", service.getOriginalUrl());
        }

        val event = super.doExecute(context);
        if (CasWebflowConstants.TRANSITION_ID_GENERATE.equals(event.getId())) {

            // extract and parse username
            String username = context.getRequestParameters().get(Constants.LOGIN_USER_EMAIL_PARAM);

            // extract and parse subrogation information
            String surrogateEmail = context.getRequestParameters().get(Constants.LOGIN_SURROGATE_EMAIL_PARAM);
            String surrogateCustomerId =
                context.getRequestParameters().get(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM);

            String superUserEmail = context.getRequestParameters().get(Constants.LOGIN_SUPER_USER_EMAIL_PARAM);
            String superUserCustomerId =
                context.getRequestParameters().get(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM);

            if (StringUtils.isNoneBlank(surrogateEmail, surrogateCustomerId, superUserEmail, superUserCustomerId)) {

                validateEmail(surrogateEmail);
                validateEmail(superUserEmail);
                validateCustomerId(surrogateCustomerId);
                validateCustomerId(superUserCustomerId);

                LOGGER.debug("Subrogation of '{}' (customerId '{}') by super admin '{}' (customerId '{}')",
                    surrogateEmail, surrogateCustomerId, superUserEmail, superUserCustomerId);

                flowScope.put(Constants.FLOW_SURROGATE_EMAIL, surrogateEmail);
                flowScope.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, surrogateCustomerId);
                flowScope.put(Constants.FLOW_LOGIN_EMAIL, superUserEmail);
                flowScope.put(Constants.FLOW_LOGIN_CUSTOMER_ID, superUserCustomerId);

                SurrogateUsernamePasswordCredential credential = new SurrogateUsernamePasswordCredential();
                credential.setUsername(superUserEmail);
                credential.setSurrogateUsername(surrogateEmail);
                WebUtils.putCredential(context, credential);

            } else if (StringUtils.isNotBlank(username)) {
                validateEmail(username);
                WebUtils.putCredential(context, new UsernamePasswordCredential(username, null));
                flowScope.put(Constants.PROVIDED_USERNAME, username);
            }

            // get the idp if it exists
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            val idp = utils.getIdpValue(request);
            LOGGER.debug("Provided idp: {}", idp);
            if (StringUtils.isNotBlank(idp)) {

                TicketGrantingTicket tgt = null;
                val tgtId = WebUtils.getTicketGrantingTicketId(context);
                if (tgtId != null) {
                    tgt = ticketRegistry.getTicket(tgtId, TicketGrantingTicket.class);
                }

                // if no authentication
                if (tgt == null || tgt.isExpired()) {

                    // if it matches an existing IdP, save it and redirect
                    val optProvider = identityProviderHelper.findByTechnicalName(providersService.getProviders(), idp);
                    if (optProvider.isPresent()) {
                        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
                        response.addCookie(utils.buildIdpCookie(idp, configContext.getCasProperties().getTgc()));
                        val client = ((Pac4jClientIdentityProviderDto) optProvider.get()).getClient();
                        LOGGER.debug("Force redirect to the SAML IdP: {}", client.getName());
                        try {
                            return utils.performClientRedirection(this, client, context);
                        } catch (final IOException e) {
                            throw new RuntimeException("Unable to perform redirection", e);
                        }
                    }
                }
            }

        }

        return event;
    }

    private void validateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Null email");
        }
        if (!Pattern.matches(EMAIL_VALID_REGEXP, email)) {
            throw new IllegalArgumentException("email : '" + email + "' format is not allowed");
        }
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Null customerId");
        }
        if (!CUSTOMER_ID_VALIDATION_PATTER.matcher(customerId).matches()) {
            throw new IllegalArgumentException("Invalid customerId: '" + customerId + "'");
        }
    }
}
