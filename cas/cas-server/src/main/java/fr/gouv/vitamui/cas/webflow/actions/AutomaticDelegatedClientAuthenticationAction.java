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
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.pac4j.DelegatedSessionCookieManager;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Regular authentication delegation + automatic delegation given the provided IdP.
 *
 *
 */
public class AutomaticDelegatedClientAuthenticationAction extends DelegatedClientAuthenticationAction {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AutomaticDelegatedClientAuthenticationAction.class);

    @Autowired
    private IdentityProviderHelper identityProviderHelper;

    @Autowired
    private ProvidersService providersService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private Utils utils;

    @Autowired
    private TicketRegistry ticketRegistry;

    public AutomaticDelegatedClientAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                        final Clients clients,
                                                        final ServicesManager servicesManager,
                                                        final AuditableExecution delegatedAuthenticationPolicyEnforcer,
                                                        final DelegatedClientWebflowManager delegatedClientWebflowManager,
                                                        final DelegatedSessionCookieManager delegatedSessionCookieManager,
                                                        final AuthenticationSystemSupport authenticationSystemSupport,
                                                        final String localeParamName,
                                                        final String themeParamName,
                                                        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                                        final CentralAuthenticationService centralAuthenticationService) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, clients, servicesManager,
            delegatedAuthenticationPolicyEnforcer, delegatedClientWebflowManager, delegatedSessionCookieManager, authenticationSystemSupport,
            localeParamName, themeParamName, authenticationRequestServiceSelectionStrategies, centralAuthenticationService);
    }

    @Override
    public Event doExecute(final RequestContext context) {

        final Event event = super.doExecute(context);
        if ("error".equals(event.getId())) {

            TicketGrantingTicket tgt = null;
            final String tgtId = WebUtils.getTicketGrantingTicketId(context);
            if (tgtId != null) {
                tgt = ticketRegistry.getTicket(tgtId, TicketGrantingTicket.class);
            }

            if (tgt == null || tgt.isExpired()) {
                final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
                final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

                // get the idp and redirect if it exists
                final String idp = getIdpValue(request);
                LOGGER.debug("Provided idp: {}", idp);
                if (StringUtils.isNotBlank(idp)) {
                    final Optional<IdentityProviderDto> optProvider = identityProviderHelper.findByTechnicalName(providersService.getProviders(), idp);
                    if (optProvider.isPresent()) {
                        response.addCookie(utils.buildIdpCookie(idp, casProperties.getTgc()));
                        final SAML2Client client = ((SamlIdentityProviderDto) optProvider.get()).getSaml2Client();
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

    private String getIdpValue(final HttpServletRequest request) {
        String idp = request.getParameter(CommonConstants.IDP_PARAMETER);
        if (StringUtils.isNotBlank(idp)) {
            return idp;
        }
        final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(request, CommonConstants.IDP_PARAMETER);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
}
