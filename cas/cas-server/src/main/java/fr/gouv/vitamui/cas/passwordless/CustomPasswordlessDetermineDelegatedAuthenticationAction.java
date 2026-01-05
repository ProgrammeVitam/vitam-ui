/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020) and the signatories
 * of the "VITAM - Accord du Contributeur" agreement.
 *
 * <p>contact@programmevitam.fr
 *
 * <p>This software is a computer program whose purpose is to implement implement a digital
 * archiving front-office system for the secure and efficient high volumetry VITAM solution.
 *
 * <p>This software is governed by the CeCILL-C license under French law and abiding by the rules of
 * distribution of free software. You can use, modify and/ or redistribute the software under the
 * terms of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * <p>As a counterpart to the access to the source code and rights to copy, modify and redistribute
 * granted by the license, users are provided only with a limited warranty and the software's
 * author, the holder of the economic rights, and the successive licensors have only limited
 * liability.
 *
 * <p>In this respect, the user's attention is drawn to the risks associated with loading, using,
 * modifying and/or developing or reproducing the software by the user in light of its specific
 * status of free software, that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the software's suitability as
 * regards their requirements in conditions enabling the security of their systems and/or data to be
 * ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * <p>The fact that you are presently reading this means that you have had knowledge of the CeCILL-C
 * license and that you accept its terms.
 */
package fr.gouv.vitamui.cas.passwordless;

import fr.gouv.vitamui.cas.delegation.ProvidersService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.BasePasswordlessCasWebflowAction;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/** Custom action to rely on loaded providers from the ProvidersService. */
@Slf4j
public class CustomPasswordlessDetermineDelegatedAuthenticationAction extends BasePasswordlessCasWebflowAction {

    private final ProvidersService providersService;

    public CustomPasswordlessDetermineDelegatedAuthenticationAction(
        final CasConfigurationProperties casProperties,
        final ProvidersService providersService
    ) {
        super(casProperties);
        this.providersService = providersService;
    }

    @Override
    protected Event doExecuteInternal(RequestContext requestContext) {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(
            requestContext,
            PasswordlessUserAccount.class
        );
        if (user == null) {
            LOGGER.error("Unable to locate passwordless account in the flow");
            return error();
        }

        val delegatedClients = user.getAllowedDelegatedClients();
        if (delegatedClients == null || delegatedClients.isEmpty()) {
            LOGGER.debug("No delegation requested");
            return success();
        }

        val client = providersService.getClients().findClient(delegatedClients.getFirst());
        if (client.isPresent()) {
            val clientName = client.get().getName();
            LOGGER.debug("Delegating to client: {}", clientName);
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, clientName);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROMPT);
        }

        DelegationWebflowUtils.putDelegatedAuthenticationDisabled(requestContext, true);
        LOGGER.debug("No delegation performed");
        return success();
    }
}
