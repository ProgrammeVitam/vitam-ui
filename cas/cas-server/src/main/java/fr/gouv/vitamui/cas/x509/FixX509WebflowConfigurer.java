package fr.gouv.vitamui.cas.x509;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * Ensures the proper registration of the X509 flow for the passwordless flow. To be removed when
 * upgrading to CAS v7.1
 */
public class FixX509WebflowConfigurer extends AbstractCasWebflowConfigurer {

    public FixX509WebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties
    ) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getX509().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val actionState = createActionState(
                flow,
                CasWebflowConstants.STATE_ID_X509_START,
                CasWebflowConstants.ACTION_ID_X509_CHECK
            );
            val transitionSet = actionState.getTransitionSet();

            transitionSet.add(
                createTransition(
                    CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET
                )
            );
            transitionSet.add(
                createTransition(CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.TRANSITION_ID_WARN)
            );
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStateIdOnX509Failure(flow)));
            transitionSet.add(
                createTransition(
                    CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                    CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE
                )
            );
            transitionSet.add(
                createTransition(
                    CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
                    CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS
                )
            );

            actionState
                .getExitActionList()
                .add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));

            // CUSTO:
            val initState = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            createTransitionForState(
                initState,
                CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID,
                CasWebflowConstants.STATE_ID_X509_START,
                true
            );
        }
    }

    private String getStateIdOnX509Failure(final Flow flow) {
        // CUSTO:
        val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
        return state.getTransition(CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID).getTargetStateId();
    }
}
