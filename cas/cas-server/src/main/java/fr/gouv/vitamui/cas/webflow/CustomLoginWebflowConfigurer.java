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

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import fr.gouv.vitamui.cas.webflow.actions.TriggerChangePasswordAction;

/**
 * A webflow configurer:
 * - with a specific processing for the redirections ("secured connection" intermediate page)
 * - to handle the change password action even if the user is already authenticated.
 *
 *
 */
public class CustomLoginWebflowConfigurer extends DefaultLoginWebflowConfigurer {

    private static final String STATE_ID_TRIGGER_CHANGE_PASSWORD = "triggerChangePassword"; // NOSONAR

    public static final String DIRECT_RESULT = "direct";

    private static final String DIRECT_ACTION = "directRedirection";

    public static final String INDIRECT_RESULT = "indirect";

    private static final String INDIRECT_ACTION = "indirectRedirection";

    public CustomLoginWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry flowDefinitionRegistry,
            final ApplicationContext applicationContext, final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void createRedirectEndState(final Flow flow) {
        final ActionState redirectState = createActionState(flow, CasWebflowConstants.STATE_ID_REDIRECT_VIEW, "selectRedirectAction");
        createTransitionForState(redirectState, DIRECT_RESULT, DIRECT_ACTION);
        createTransitionForState(redirectState, INDIRECT_RESULT, INDIRECT_ACTION);
        createEndState(flow, DIRECT_ACTION, "requestScope.url", true);
        createEndState(flow, INDIRECT_ACTION, "casGetResponseView");
    }

    @Override
    protected void createTicketGrantingTicketCheckAction(final Flow flow) {
        final ActionState action = createActionState(flow, CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK,
                CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TGT_NOT_EXISTS, CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TGT_INVALID, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
        // instead of STATE_ID_HAS_SERVICE_CHECK, send to STATE_ID_TRIGGER_CHANGE_PASSWORD
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TGT_VALID, STATE_ID_TRIGGER_CHANGE_PASSWORD);

        createTriggerChangePasswordAction(flow);
    }

    private void createTriggerChangePasswordAction(final Flow flow) {
        final ActionState action = createActionState(flow, STATE_ID_TRIGGER_CHANGE_PASSWORD, "triggerChangePasswordAction");
        createTransitionForState(action, TriggerChangePasswordAction.EVENT_ID_CHANGE_PASSWORD, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
        createTransitionForState(action, TriggerChangePasswordAction.EVENT_ID_CONTINUE, CasWebflowConstants.STATE_ID_HAS_SERVICE_CHECK);
    }

    @Override
    protected void createHandleAuthenticationFailureAction(final Flow flow) {
        final ActionState handler = createActionState(flow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE,
                CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER);
        createTransitionForState(handler, AccountDisabledException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED);
        createTransitionForState(handler, AccountLockedException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED);
        // JLE custo:
        createTransitionForState(handler, AccountPasswordMustChangeException.class.getSimpleName(), "sendInstructions");
        createTransitionForState(handler, CredentialExpiredException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
        createTransitionForState(handler, InvalidLoginLocationException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION);
        createTransitionForState(handler, InvalidLoginTimeException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS);
        createTransitionForState(handler, FailedLoginException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, AccountNotFoundException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnauthorizedServiceForPrincipalException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, PrincipalException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnsatisfiedAuthenticationPolicyException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnauthorizedAuthenticationException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED);
        createTransitionForState(handler, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        createStateDefaultTransition(handler, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
    }
}
