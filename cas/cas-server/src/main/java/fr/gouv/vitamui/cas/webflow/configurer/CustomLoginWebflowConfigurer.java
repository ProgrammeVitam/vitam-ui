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
package fr.gouv.vitamui.cas.webflow.configurer;

import fr.gouv.vitamui.cas.webflow.actions.DispatcherAction;
import fr.gouv.vitamui.cas.webflow.actions.TriggerChangePasswordAction;
import lombok.val;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.StringToCharArrayConverter;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.History;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.Map;

/**
 * A webflow configurer:
 * - to handle the change password action even if the user is already authenticated
 * - with a username page
 * - with an optional customer selection page
 * - with a password page.
 */
public class CustomLoginWebflowConfigurer extends DefaultLoginWebflowConfigurer {

    private static final String VIEW_STATE_PASSWORD_FORM = "viewPwfForm";
    private static final String VIEW_STATE_LOGIN_CUSTOMER_FORM = "viewStateCustomerForm";
    private static final String ACTION_STATE_TRIGGER_CHANGE_PASSWORD = "triggerChangePassword"; // NOSONAR
    private static final String ACTION_STATE_INTERMEDIATE_SUBMIT = "intermediateSubmit";
    private static final String ACTION_STATE_LIST_CUSTOMERS = "listCustomers";
    private static final String ACTION_STATE_SELECTED_CUSTOMER_SUBMIT = "selectedCustomerSubmit";
    public static final String TRANSITION_TO_CUSTOMER_SELECTION_VIEW = "customerSelectionView";
    public static final String TRANSITION_TO_CUSTOMER_SELECTED = "customerSelected";

    private static final String TEMPLATE_BAD_CONFIGURATION = "casAccountBadConfigurationView";
    public static final String TEMPLATE_PASSWORD_FORM = "passwordForm";
    public static final String TEMPLATE_CUSTOMER_FORM = "customerForm";
    public static final String TEMPLATE_EMAIL_FORM = "emailForm";

    // FORMS FIELDS
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String CUSTOMER_ID = "customerId";

    public CustomLoginWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry flowDefinitionRegistry,
                                        final ConfigurableApplicationContext applicationContext, final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void createTicketGrantingTicketCheckAction(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK,
            CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_NOT_EXISTS,
            CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_INVALID,
            CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
        // CUSTO: instead of STATE_ID_HAS_SERVICE_CHECK, send to STATE_ID_TRIGGER_CHANGE_PASSWORD
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID,
            ACTION_STATE_TRIGGER_CHANGE_PASSWORD);

        createTriggerChangePasswordAction(flow);
    }

    private void createTriggerChangePasswordAction(final Flow flow) {
        final ActionState action = createActionState(flow, ACTION_STATE_TRIGGER_CHANGE_PASSWORD, "triggerChangePasswordAction");
        createTransitionForState(action, TriggerChangePasswordAction.EVENT_ID_CHANGE_PASSWORD, CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
        createTransitionForState(action, TriggerChangePasswordAction.EVENT_ID_CONTINUE, CasWebflowConstants.STATE_ID_HAS_SERVICE_CHECK);
    }

    @Override
    protected void createLoginFormView(final Flow flow) {
        val propertiesToBind = Map.of(
            USERNAME, Map.of("required", "true"));
        val binder = createStateBinderConfiguration(propertiesToBind);

        val state = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, TEMPLATE_EMAIL_FORM, binder);
        state.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM));
        createStateModelBinding(state, CasWebflowConstants.VAR_ID_CREDENTIAL, UsernamePasswordCredential.class);

        // CUSTO: CasWebflowConstants.STATE_ID_REAL_SUBMIT becomes ACTION_STATE_LIST_CUSTOMERS
        val transition = createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUBMIT,
            ACTION_STATE_LIST_CUSTOMERS);
        val attributes = transition.getAttributes();
        attributes.put("bind", Boolean.TRUE);
        attributes.put("validate", Boolean.TRUE);
        attributes.put("history", History.INVALIDATE);

        createListCustomersAction(flow);
        createLoginCustomerFormView(flow);
        createSelectedCustomerAction(flow);
        createIntermediateSubmitAction(flow);
        createPwdFormView(flow);
    }

    protected void createIntermediateSubmitAction(final Flow flow) {
        val action = createActionState(flow, ACTION_STATE_INTERMEDIATE_SUBMIT, "dispatcherAction");
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS, VIEW_STATE_PASSWORD_FORM);
        createTransitionForState(action, DispatcherAction.TRANSITION_SELECT_CUSTOMER, VIEW_STATE_LOGIN_CUSTOMER_FORM);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);
        createTransitionForState(action, DispatcherAction.DISABLED, CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED);
        createTransitionForState(action, DispatcherAction.BAD_CONFIGURATION, TEMPLATE_BAD_CONFIGURATION);
        createEndState(flow, TEMPLATE_BAD_CONFIGURATION, TEMPLATE_BAD_CONFIGURATION);
    }

    protected void createPwdFormView(final Flow flow) {
        val propertiesToBind = Map.of(
            USERNAME, Map.of("required", "true"),
            PASSWORD, Map.of("converter", StringToCharArrayConverter.ID)
        );
        val binder = createStateBinderConfiguration(propertiesToBind);

        val state = createViewState(flow, VIEW_STATE_PASSWORD_FORM, TEMPLATE_PASSWORD_FORM, binder);
        state.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM));
        createStateModelBinding(state, CasWebflowConstants.VAR_ID_CREDENTIAL, UsernamePasswordCredential.class);

        val transition = createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        val attributes = transition.getAttributes();
        attributes.put("bind", Boolean.TRUE);
        attributes.put("validate", Boolean.TRUE);
        attributes.put("history", History.INVALIDATE);

        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD, CasWebflowConstants.STATE_ID_SEND_RESET_PASSWORD_ACCT_INFO);
    }

    private void createListCustomersAction(final Flow flow) {
        val action = createActionState(flow, ACTION_STATE_LIST_CUSTOMERS, "listCustomersAction");
        createTransitionForState(action, TRANSITION_TO_CUSTOMER_SELECTION_VIEW, VIEW_STATE_LOGIN_CUSTOMER_FORM);
        createTransitionForState(action, TRANSITION_TO_CUSTOMER_SELECTED, ACTION_STATE_INTERMEDIATE_SUBMIT);
        createTransitionForState(action, DispatcherAction.DISABLED, CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED);
        createTransitionForState(action, DispatcherAction.BAD_CONFIGURATION, TEMPLATE_BAD_CONFIGURATION);
    }

    protected void createLoginCustomerFormView(final Flow flow) {
        val propertiesToBind = Map.of(
            CUSTOMER_ID, Map.of("required", "true")
        );
        val binder = createStateBinderConfiguration(propertiesToBind);
        val state = createViewState(flow, VIEW_STATE_LOGIN_CUSTOMER_FORM, TEMPLATE_CUSTOMER_FORM, binder);
        val transition = createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUBMIT,
            ACTION_STATE_SELECTED_CUSTOMER_SUBMIT);
        val attributes = transition.getAttributes();
        attributes.put("bind", Boolean.TRUE);
        attributes.put("validate", Boolean.TRUE);
        attributes.put("history", History.INVALIDATE);
    }

    private void createSelectedCustomerAction(final Flow flow) {
        val action = createActionState(flow, ACTION_STATE_SELECTED_CUSTOMER_SUBMIT, "customerSelectedAction");
        createTransitionForState(action, TRANSITION_TO_CUSTOMER_SELECTED, ACTION_STATE_INTERMEDIATE_SUBMIT);
    }
}
