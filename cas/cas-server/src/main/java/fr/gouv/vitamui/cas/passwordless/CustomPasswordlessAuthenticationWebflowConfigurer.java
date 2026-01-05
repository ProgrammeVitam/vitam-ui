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

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * Change the passwordless webflow to handle custom use cases: user disabled + bad configuration.
 */
public class CustomPasswordlessAuthenticationWebflowConfigurer extends PasswordlessAuthenticationWebflowConfigurer {

    public static final String USER_DISABLED = "userDisabled";
    public static final String BAD_CONFIGURATION = "badConfiguration";

    private static final String BAD_CONFIGURATION_VIEW = "casAccountBadConfigurationView";

    public CustomPasswordlessAuthenticationWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties
    ) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        // To be removed when upgrading to CAS v7.1:
        setOrder(8);
        //
    }

    @Override
    protected void createStateVerifyPasswordlessAccount(final Flow flow) {
        val verifyAccountState = createActionState(
            flow,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT,
            CasWebflowConstants.ACTION_ID_VERIFY_PASSWORDLESS_ACCOUNT_AUTHN
        );
        createTransitionForState(
            verifyAccountState,
            CasWebflowConstants.TRANSITION_ID_ERROR,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID
        );
        // CUSTO:
        createTransitionForState(verifyAccountState, BAD_CONFIGURATION, BAD_CONFIGURATION_VIEW);
        createEndState(flow, BAD_CONFIGURATION_VIEW, BAD_CONFIGURATION_VIEW);

        createTransitionForState(verifyAccountState, USER_DISABLED, CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED);
        //

        if (applicationContext.containsBean(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN)) {
            createTransitionForState(
                verifyAccountState,
                CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_DELEGATED_AUTHN
            );
        } else {
            createTransitionForState(
                verifyAccountState,
                CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_MFA
            );
        }

        val state = getTransitionableState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        val transition = state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        createTransitionForState(
            verifyAccountState,
            CasWebflowConstants.TRANSITION_ID_PROMPT,
            transition.getTargetStateId()
        );
    }
}
