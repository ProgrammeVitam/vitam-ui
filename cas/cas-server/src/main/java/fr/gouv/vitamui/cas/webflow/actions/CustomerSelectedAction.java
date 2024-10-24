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

import fr.gouv.vitamui.cas.model.CustomerModel;
import fr.gouv.vitamui.cas.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.util.List;

import static fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTED;

/**
 * This class persists user selected customerId into flow scope and redirect to dispatcher
 */
@RequiredArgsConstructor
public class CustomerSelectedAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerSelectedAction.class);

    @Override
    protected Event doExecute(final RequestContext requestContext) throws IOException {
        val flowScope = requestContext.getFlowScope();

        String loginEmail = flowScope.getRequiredString(Constants.FLOW_LOGIN_EMAIL);
        String customerId = requestContext.getRequestParameters().get(Constants.SELECT_CUSTOMER_ID_PARAM);

        List<CustomerModel> customerModels = (List<CustomerModel>) flowScope.getRequired(
            Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST
        );

        CustomerModel customerModel = customerModels
            .stream()
            .filter(c -> c.getCustomerId().equals(customerId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid customerId '" + customerId + "'"));

        LOGGER.debug("Valid customer selected: {} for user: {}", customerModel, loginEmail);

        flowScope.put(Constants.FLOW_LOGIN_CUSTOMER_ID, customerId);
        flowScope.remove(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST);

        return new Event(this, TRANSITION_TO_CUSTOMER_SELECTED);
    }
}
