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
package fr.gouv.vitamui.cas.config;

import fr.gouv.vitamui.cas.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.flow.action.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * CUSTO: Full rewrite of {@link SurrogateInitialAuthenticationAction}
 */
@Slf4j
public class CustomSurrogateInitialAuthenticationAction extends BaseCasWebflowAction {

    @Override
    protected Event doExecuteInternal(RequestContext context) {
        final var up = WebUtils.getCredential(context, UsernamePasswordCredential.class);
        if (up == null) {
            LOGGER.debug(
                "Provided credentials cannot be found, or are not of type [{}]",
                UsernamePasswordCredential.class.getName()
            );
            return null;
        }

        final var flowScope = context.getFlowScope();
        if (isSubrogationMode(flowScope)) {
            String surrogateEmail = (String) flowScope.get(Constants.FLOW_SURROGATE_EMAIL);
            String surrogateCustomerId = (String) flowScope.get(Constants.FLOW_SURROGATE_CUSTOMER_ID);
            String superUserEmail = (String) flowScope.get(Constants.FLOW_LOGIN_EMAIL);
            String superUserCustomerId = (String) flowScope.get(Constants.FLOW_LOGIN_CUSTOMER_ID);

            LOGGER.debug(
                "Subrogation of '{}' (customerId '{}') by super admin '{}' (customerId '{}')",
                surrogateEmail,
                surrogateCustomerId,
                superUserEmail,
                superUserCustomerId
            );

            SurrogateUsernamePasswordCredential credential = new SurrogateUsernamePasswordCredential();
            credential.setUsername(superUserEmail);
            credential.setSurrogateUsername(surrogateEmail);
            credential.assignPassword(up.toPassword());
            WebUtils.putCredential(context, credential);
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.FALSE);
        }
        return null;
    }

    private static boolean isSubrogationMode(MutableAttributeMap<Object> flowScope) {
        return flowScope.contains(Constants.FLOW_SURROGATE_EMAIL);
    }
}
