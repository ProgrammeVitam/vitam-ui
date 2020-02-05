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
package fr.gouv.vitamui.cas.webflow;

import fr.gouv.vitamui.cas.util.Constants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Map;

/**
 * A webflow manager for authentication delegation which saves/restores the surrogate.
 *
 *
 */
public class CustomDelegatedClientWebflowManager extends DelegatedClientWebflowManager {

    public CustomDelegatedClientWebflowManager(final TicketRegistry ticketRegistry,
        final TicketFactory ticketFactory,
        final String themeParamName,
        final String localParamName,
        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
        final ArgumentExtractor argumentExtractor) {
        super(ticketRegistry, ticketFactory, themeParamName, localParamName, authenticationRequestServiceSelectionStrategies, argumentExtractor);
    }

    @Override
    protected Map<String, Serializable> buildTicketProperties(final J2EContext webContext) {
        final Map<String, Serializable> properties = super.buildTicketProperties(webContext);

        properties.put(Constants.SURROGATE, (String) webContext.getRequestAttribute(Constants.SURROGATE));

        return properties;
    }

    @Override
    protected Service restoreDelegatedAuthenticationRequest(final RequestContext requestContext, final WebContext webContext,
                                                            final TransientSessionTicket ticket) {

        webContext.setRequestAttribute(Constants.SURROGATE, ticket.getProperties().get(Constants.SURROGATE));

        return super.restoreDelegatedAuthenticationRequest(requestContext, webContext, ticket);
    }
}
