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
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import lombok.RequiredArgsConstructor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Check if the password change must be started, even authenticated.
 *
 *
 */
@RequiredArgsConstructor
public class TriggerChangePasswordAction extends AbstractAction {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TriggerChangePasswordAction.class);

    public static final String EVENT_ID_CHANGE_PASSWORD = "changePassword";
    public static final String EVENT_ID_CONTINUE = "continue";

    private final TicketRegistrySupport ticketRegistrySupport;

    private final Utils utils;

    protected Event doExecute(final RequestContext context) {

        final String doChangePassword = context.getRequestParameters().get(PasswordManagementWebflowConfigurer.DO_CHANGE_PASSWORD_PARAMETER);
        LOGGER.debug("doChangePassword: {}", doChangePassword);
        if (doChangePassword != null) {
            // we force to change the password and as the user is already authenticated,
            // we must simulate the authentication process by providing the credentials
            // and a specific property: pswdChangePostLogin in the flow
            final RequestContext requestContext = RequestContextHolder.getRequestContext();
            final Principal principal = WebUtils.getPrincipalFromRequestContext(requestContext, ticketRegistrySupport);
            final String username = (String) utils.getAttributeValue(principal.getAttributes(), CommonConstants.EMAIL_ATTRIBUTE);
            final UsernamePasswordCredential credential = new UsernamePasswordCredential(username, null);
            WebUtils.putCredential(requestContext, credential);
            requestContext.getFlowScope().put("pswdChangePostLogin", true);
            return new Event(this, EVENT_ID_CHANGE_PASSWORD);
        } else {
            return new Event(this, EVENT_ID_CONTINUE);
        }
    }
}
