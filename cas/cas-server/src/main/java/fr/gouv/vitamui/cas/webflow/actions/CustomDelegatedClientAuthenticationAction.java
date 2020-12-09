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

import fr.gouv.vitamui.cas.provider.SamlIdentityProviderDto;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.util.List;

import lombok.val;

/**
 * Custom authentication delegation:
 * - automatic delegation given the provided IdP
 * - extraction of the username/surrogate passed as a request parameter
 * - save the portalUrl in the webflow.
 *
 *
 */
public class CustomDelegatedClientAuthenticationAction extends DelegatedClientAuthenticationAction {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomDelegatedClientAuthenticationAction.class);

    private final IdentityProviderHelper identityProviderHelper;

    private final ProvidersService providersService;

    private final CasConfigurationProperties casProperties;

    private final Utils utils;

    private final TicketRegistry ticketRegistry;

    private final String vitamuiPortalUrl;

    private final String surrogationSeparator;

    public CustomDelegatedClientAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                     final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                     final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                     final Clients clients,
                                                     final ServicesManager servicesManager,
                                                     final AuditableExecution delegatedAuthenticationPolicyEnforcer,
                                                     final DelegatedClientWebflowManager delegatedClientWebflowManager,
                                                     final AuthenticationSystemSupport authenticationSystemSupport,
                                                     final CasConfigurationProperties casProperties,
                                                     final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                                     final CentralAuthenticationService centralAuthenticationService,
                                                     final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy,
                                                     final SessionStore<JEEContext> sessionStore,
                                                     final List<ArgumentExtractor> argumentExtractors,
                                                     final IdentityProviderHelper identityProviderHelper,
                                                     final ProvidersService providersService,
                                                     final Utils utils,
                                                     final TicketRegistry ticketRegistry,
                                                     final String vitamuiPortalUrl,
                                                     final String surrogationSeparator) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy,
            clients, servicesManager, delegatedAuthenticationPolicyEnforcer, delegatedClientWebflowManager, authenticationSystemSupport,
            casProperties, authenticationRequestServiceSelectionStrategies, centralAuthenticationService, singleSignOnParticipationStrategy,
            sessionStore, argumentExtractors);
        this.identityProviderHelper = identityProviderHelper;
        this.casProperties = casProperties;
        this.providersService = providersService;
        this.utils = utils;
        this.ticketRegistry = ticketRegistry;
        this.vitamuiPortalUrl = vitamuiPortalUrl;
        this.surrogationSeparator = surrogationSeparator;
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
        if ("error".equals(event.getId())) {

            // extract and parse the request username if provided
            String username = context.getRequestParameters().get(Constants.USERNAME);
            if (username != null) {

                username = username.toLowerCase();
                LOGGER.debug("Provided username: {}", username);
                if (username.startsWith(surrogationSeparator)) {
                    username = org.apache.commons.lang.StringUtils.substringAfter(username, surrogationSeparator);
                }

                WebUtils.putCredential(context, new UsernamePasswordCredential(username, null));

                if (username.contains(surrogationSeparator)) {
                    final String[] parts = username.split("\\" + surrogationSeparator);
                    flowScope.put(Constants.SURROGATE, parts[0]);
                    flowScope.put(Constants.SUPER_USER, parts[1]);
                }
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
                        response.addCookie(utils.buildIdpCookie(idp, casProperties.getTgc()));
                        val client = ((SamlIdentityProviderDto) optProvider.get()).getSaml2Client();
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
}
