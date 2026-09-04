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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.logout.FinishLogoutAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Ends the logout flow on the delegated provider rather than on the service.
 * <p>
 * {@link CustomDelegatedAuthenticationClientLogoutAction} parks the provider logout url in the flow
 * scope. It cannot promote it itself: `LogoutAction` runs after it and overwrites `logoutRedirectUrl`
 * with the service url whenever `cas.logout.follow-service-redirects` is on. This action runs last,
 * so the url it sets is the one `FinishLogoutAction` ends on.
 * <p>
 * The url CAS was about to use is not discarded but chained behind the provider one as
 * `post_logout_redirect_uri`, so the browser comes back to the application once the provider session
 * is closed.
 */
@Slf4j
public class CustomFinishLogoutAction extends FinishLogoutAction {

    private static final String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";

    public CustomFinishLogoutAction(
        final TicketRegistry ticketRegistry,
        final CasCookieBuilder ticketGrantingTicketCookieGenerator,
        final ArgumentExtractor argumentExtractor,
        final ServicesManager servicesManager,
        final LogoutExecutionPlan logoutExecutionPlan,
        final CasConfigurationProperties casProperties
    ) {
        super(
            ticketRegistry,
            ticketGrantingTicketCookieGenerator,
            argumentExtractor,
            servicesManager,
            logoutExecutionPlan,
            casProperties
        );
    }

    @Override
    protected Event doInternalExecute(final RequestContext context) {
        final String providerLogoutUrl = context
            .getFlowScope()
            .get(CustomDelegatedAuthenticationClientLogoutAction.PROVIDER_LOGOUT_URL_ATTRIBUTE, String.class);

        if (StringUtils.isNotBlank(providerLogoutUrl)) {
            final String casRedirectUrl = WebUtils.getLogoutRedirectUrl(context, String.class);
            final String redirectUrl = chainPostLogoutRedirectUri(providerLogoutUrl, casRedirectUrl);
            LOGGER.debug("Ending the logout flow on the delegated provider: {}", redirectUrl);
            WebUtils.putLogoutRedirectUrl(context, redirectUrl);
        }

        return super.doInternalExecute(context);
    }

    // pac4j only sets post_logout_redirect_uri when the flow carries a service, which the OIDC
    // end-session path does not: without this the browser would stall on the provider logout page.
    private static String chainPostLogoutRedirectUri(final String providerLogoutUrl, final String casRedirectUrl) {
        if (StringUtils.isBlank(casRedirectUrl) || providerLogoutUrl.contains(POST_LOGOUT_REDIRECT_URI + "=")) {
            return providerLogoutUrl;
        }
        return (
            providerLogoutUrl +
            (providerLogoutUrl.contains("?") ? '&' : '?') +
            POST_LOGOUT_REDIRECT_URI +
            '=' +
            URLEncoder.encode(casRedirectUrl, StandardCharsets.UTF_8)
        );
    }
}
