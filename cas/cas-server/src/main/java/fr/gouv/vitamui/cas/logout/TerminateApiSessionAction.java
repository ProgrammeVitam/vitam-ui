/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitamui.cas.logout;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitamui.commons.api.CommonConstants.AUTHTOKEN_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;

/** Terminate session action with custom IAM logout call. */
@Slf4j
public class TerminateApiSessionAction extends TerminateSessionAction {

    private final Utils utils;
    private final CasApi casApi;
    private final ServicesManager servicesManager;
    private final CasConfigurationProperties casProperties;
    private final Action frontChannelLogoutAction;
    private final TicketRegistry ticketRegistry;

    public TerminateApiSessionAction(
        final CentralAuthenticationService centralAuthenticationService,
        final CasCookieBuilder ticketGrantingTicketCookieGenerator,
        final CasCookieBuilder warnCookieGenerator,
        final LogoutProperties logoutProperties,
        final LogoutManager logoutManager,
        final ConfigurableApplicationContext applicationContext,
        final Utils utils,
        final CasApi casApi,
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        final Action frontChannelLogoutAction,
        final TicketRegistry ticketRegistry
    ) {
        super(centralAuthenticationService, ticketGrantingTicketCookieGenerator, warnCookieGenerator, logoutProperties,
            logoutManager, applicationContext, null);
        this.utils = utils;
        this.casApi = casApi;
        this.servicesManager = servicesManager;
        this.casProperties = casProperties;
        this.frontChannelLogoutAction = frontChannelLogoutAction;
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    @SneakyThrows
    public Event terminate(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        String tgtId = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isBlank(tgtId)) {
            tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }

        TicketGrantingTicket ticket = null;
        if (StringUtils.isNotBlank(tgtId)) {
            try {
                ticket = ticketRegistry.getTicket(tgtId, TicketGrantingTicket.class);
                if (ticket != null) {
                    final Principal principal = ticket.getAuthentication().getPrincipal();
                    final Map<String, List<Object>> attributes = principal.getAttributes();
                    final String authToken = (String) utils.getAttributeValue(attributes, AUTHTOKEN_ATTRIBUTE);
                    final String superUserEmail = (String) utils.getAttributeValue(attributes, SUPER_USER_ATTRIBUTE);
                    final String superUserCustomerId = (String) utils.getAttributeValue(attributes, SUPER_USER_CUSTOMER_ID_ATTRIBUTE);

                    LOGGER.debug("Calling logout for authToken={} and superUser={}, superUserCustomerId={}",
                        authToken, superUserEmail, superUserCustomerId);

                    casApi.logout(authToken, superUserEmail, superUserCustomerId);
                }
            } catch (final InvalidTicketException e) {
                LOGGER.warn("No TGT found for the CAS cookie: {}", tgtId);
            }
        }

        final Event event = super.terminate(context);

        // Remove IdP cookie
        response.addCookie(utils.buildIdpCookie(null, casProperties.getTgc()));

        // Fallback general logout
        if (tgtId == null || ticket == null || ticket.isExpired()) {
            List<SingleLogoutRequestContext> logoutRequests = performGeneralLogout(tgtId != null ? tgtId : "nocookie");
            WebUtils.putLogoutRequests(context, logoutRequests);
        }

        // Front channel logout in login flow
        if ("login".equals(context.getFlowExecutionContext().getDefinition().getId())) {
            LOGGER.debug("Computing front channel logout URLs");
            frontChannelLogoutAction.execute(context);
        }

        return event;
    }

    protected List<SingleLogoutRequestContext> performGeneralLogout(final String tgtId) {
        try {
            // 1️⃣ Crée l'Authentication factice
            Authentication fakeAuthentication = new DefaultAuthenticationBuilder()
                .setPrincipal(new FakePrincipal(tgtId))
                .build();

            // 2️⃣ Crée un AuthenticationResult factice à partir de l'authentication
            AuthenticationResult authenticationResult = new DefaultAuthenticationResultBuilder()
                .collect(fakeAuthentication)
                .build(new DefaultPrincipalElectionStrategy()); // PrincipalElectionStrategy trivial

            // 3️⃣ Crée le TGT factice via l'API CAS 7
            TicketGrantingTicket fakeTgt = centralAuthenticationService.createTicketGrantingTicket(authenticationResult);

            Collection<RegisteredService> registeredServices = servicesManager.getAllServices();
            // Préparer le factory de services CAS
            WebApplicationServiceFactory serviceFactory = new WebApplicationServiceFactory();

            for (RegisteredService registeredService : registeredServices) {
                if (!(registeredService instanceof BaseRegisteredService baseService)) continue;

                String logoutUrl = baseService.getLogoutUrl();
                if (logoutUrl != null) {
                    WebApplicationService service = serviceFactory.createService(logoutUrl);

                    // Génère le ST factice lié au TGT factice
                    centralAuthenticationService.grantServiceTicket(
                        fakeTgt.getId(),
                        service,
                        authenticationResult
                    );
                    // Pas besoin de collecter les ST dans une liste
                }
            }

            // Ensuite le logout général
            return logoutManager.performLogout(
                SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(fakeTgt)
                    .build()
            );
        } catch (Throwable e) {
            LOGGER.error("Unable to perform general logout", e);
            return new ArrayList<>();
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class FakePrincipal implements Principal {
        private final String id;
    }
}
