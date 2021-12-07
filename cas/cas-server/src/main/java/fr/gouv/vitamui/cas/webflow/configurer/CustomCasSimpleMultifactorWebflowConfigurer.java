package fr.gouv.vitamui.cas.webflow.configurer;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Custom webflow for simple MFA.
 */
public class CustomCasSimpleMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    public static final String MFA_SIMPLE_EVENT_ID = "mfa-simple";

    public CustomCasSimpleMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                       final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                       final FlowDefinitionRegistry flowDefinitionRegistry,
                                                       final ConfigurableApplicationContext applicationContext,
                                                       final CasConfigurationProperties casProperties,
                                                       final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext,
            casProperties, Optional.of(flowDefinitionRegistry), mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flow = getFlow(registry, MFA_SIMPLE_EVENT_ID);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, CasSimpleMultifactorTokenCredential.class);
            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, "sendSimpleToken");
            setStartState(flow, initLoginFormState);
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);
            createEndState(flow, CasWebflowConstants.STATE_ID_UNAVAILABLE);

            val sendSimpleToken = createActionState(flow, "sendSimpleToken",
                createEvaluateAction("mfaSimpleMultifactorSendTokenAction"));
            createTransitionForState(sendSimpleToken, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_UNAVAILABLE);
            createTransitionForState(sendSimpleToken, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            // CUSTO:
            createTransitionForState(sendSimpleToken, "missingPhone", "missingPhone");
            createViewState(flow, "missingPhone", "casSmsMissingPhoneView");
            //

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            val propertiesToBind = CollectionUtils.wrapList("token");
            val binder = createStateBinderConfiguration(propertiesToBind);
            val viewLoginFormState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM,
                "simple-mfa/casSimpleMfaLoginView", binder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, CasSimpleMultifactorTokenCredential.class);
            viewLoginFormState.getEntryActionList().add(setPrincipalAction);

            // CUSTO: instead of CasWebflowConstants.STATE_ID_REAL_SUBMIT, send to intermediateSubmit
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                "intermediateSubmit", Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_RESEND, "sendSimpleToken",
                Map.of("bind", Boolean.FALSE, "validate", Boolean.FALSE));

            // CUSTO:
            val intermediateSubmit = createActionState(flow, "intermediateSubmit", createEvaluateAction("checkMfaTokenAction"));
            createTransitionForState(intermediateSubmit, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
            createTransitionForState(intermediateSubmit, CasWebflowConstants.TRANSITION_ID_ERROR, "codeExpired");
            val codeExpired = createViewState(flow, "codeExpired", "casSmsCodeExpiredView");
            createTransitionForState(codeExpired, "resend", CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
            //

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_OTP_AUTHENTICATION_ACTION));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        });

        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_SIMPLE_EVENT_ID,
            casProperties.getAuthn().getMfa().getSimple().getId());
    }
}
