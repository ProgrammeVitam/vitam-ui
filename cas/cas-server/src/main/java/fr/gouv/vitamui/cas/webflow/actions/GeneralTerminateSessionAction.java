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
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static fr.gouv.vitamui.commons.api.CommonConstants.*;

/**
 * Terminate session action with custom IAM logout and fallback mechanisms (to perform a general logout).
 *
 *
 */
@Getter
public class GeneralTerminateSessionAction extends TerminateSessionAction {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(GeneralTerminateSessionAction.class);

    private final Utils utils;

    private final CasExternalRestClient casExternalRestClient;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    private final Action frontChannelLogoutAction;

    private final TicketRegistry ticketRegistry;

    private final ServiceTicketSessionTrackingPolicy serviceTicketSessionTrackingPolicy;

    public GeneralTerminateSessionAction(final CentralAuthenticationService centralAuthenticationService,
                                         final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                         final CasCookieBuilder warnCookieGenerator,
                                         final LogoutProperties logoutProperties,
                                         final LogoutManager logoutManager,
                                         final ConfigurableApplicationContext applicationContext,
                                         final SingleLogoutRequestExecutor singleLogoutRequestExecutor,
                                         final Utils utils,
                                         final CasExternalRestClient casExternalRestClient,
                                         final ServicesManager servicesManager,
                                         final CasConfigurationProperties casProperties,
                                         final Action frontChannelLogoutAction,
                                         final TicketRegistry ticketRegistry,
                                         final ServiceTicketSessionTrackingPolicy serviceTicketSessionTrackingPolicy) {
        super(centralAuthenticationService, ticketGrantingTicketCookieGenerator, warnCookieGenerator,
            logoutProperties, logoutManager, applicationContext, singleLogoutRequestExecutor);
        this.utils = utils;
        this.casExternalRestClient = casExternalRestClient;
        this.servicesManager = servicesManager;
        this.casProperties = casProperties;
        this.frontChannelLogoutAction = frontChannelLogoutAction;
        this.ticketRegistry = ticketRegistry;
        this.serviceTicketSessionTrackingPolicy = serviceTicketSessionTrackingPolicy;
    }

    @Override @SneakyThrows
    public Event terminate(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        String tgtId = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isBlank(tgtId)) {
            tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }

        // if we found a ticket, properly log out the user in the IAM web services
        TicketGrantingTicket ticket = null;
        if (StringUtils.isNotBlank(tgtId)) {
            try {
                ticket = ticketRegistry.getTicket(tgtId, TicketGrantingTicket.class);
                if (ticket != null) {

                    final Principal principal = ticket.getAuthentication().getPrincipal();
                    final Map<String, List<Object>> attributes = principal.getAttributes();
                    final String authToken = (String) utils.getAttributeValue(attributes, AUTHTOKEN_ATTRIBUTE);
                    final String principalEmail = (String) utils.getAttributeValue(attributes, EMAIL_ATTRIBUTE);
                    final String emailSuperUser = (String) utils.getAttributeValue(attributes, SUPER_USER_ATTRIBUTE);

                    final ExternalHttpContext externalHttpContext;
                    if (StringUtils.isNotBlank(emailSuperUser)) {
                        externalHttpContext = utils.buildContext(emailSuperUser);
                    } else {
                        externalHttpContext = utils.buildContext(principalEmail);
                    }

                    LOGGER.debug("calling logout for authToken={} and superUser={}", authToken, emailSuperUser);
                    casExternalRestClient.logout(externalHttpContext, authToken, emailSuperUser);
                }
            } catch (final InvalidTicketException e) {
                LOGGER.warn("No TGT found for the CAS cookie: {}", tgtId);
            }
        }

        final Event event = super.terminate(context);

        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        // remove the idp cookie
        response.addCookie(utils.buildIdpCookie(null, casProperties.getTgc()));

        // fallback cases:
        // no CAS cookie -> general logout
        if (tgtId == null) {
            final List<SingleLogoutRequestContext> logoutRequests = performGeneralLogout("nocookie");
            WebUtils.putLogoutRequests(context, logoutRequests);

            // no ticket or expired -> general logout
        } else if (ticket == null || ticket.isExpired()) {
            final List<SingleLogoutRequestContext> logoutRequests = performGeneralLogout(tgtId);
            WebUtils.putLogoutRequests(context, logoutRequests);
        }

        // if we are in the login webflow, compute the logout URLs
        if ("login".equals(context.getFlowExecutionContext().getDefinition().getId())) {
            LOGGER.debug("Computing front channel logout URLs");
            frontChannelLogoutAction.execute(context);
        }

        return event;
    }

    protected List<SingleLogoutRequestContext> performGeneralLogout(final String tgtId) {
        try {

            final Map<String, AuthenticationHandlerExecutionResult> successes = new HashMap<>();
            successes.put("fake", null);

            final Authentication authentication = new DefaultAuthenticationBuilder()
                .setPrincipal(new FakePrincipal(tgtId))
                .setSuccesses(successes)
                .addCredential(null)
                .build();

            final TicketGrantingTicketImpl fakeTgt = new TicketGrantingTicketImpl(tgtId, authentication, new NeverExpiresExpirationPolicy());

            final Collection<RegisteredService> registeredServices = servicesManager.getAllServices();
            int i = 1;
            for (final RegisteredService registeredService : registeredServices) {
                final String logoutUrl = ((BaseRegisteredService) registeredService).getLogoutUrl();
                if (logoutUrl != null) {
                    final String serviceId = logoutUrl.toString();
                    final String fakeSt = "ST-fake-" + i;
                    final Service service = new FakeSimpleWebApplicationServiceImpl(serviceId, serviceId, fakeSt);
                    fakeTgt.grantServiceTicket(fakeSt, service, new NeverExpiresExpirationPolicy(), false, serviceTicketSessionTrackingPolicy);
                    i++;
                }
            }

            return logoutManager.performLogout(
                SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(fakeTgt)
                    .build());

        } catch (final RuntimeException e) {
            LOGGER.error("Unable to perform general logout", e);
            return new ArrayList<>();
        }
    }

    private static class FakeSimpleWebApplicationServiceImpl extends SimpleWebApplicationServiceImpl {

        public FakeSimpleWebApplicationServiceImpl(final String id, final String originalUrl, final String artifactId) {
            super(id, originalUrl, artifactId);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class FakePrincipal implements Principal {

        private final String id;
    }
}
