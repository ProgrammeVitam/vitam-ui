package fr.gouv.vitamui.cas.config;

import fr.gouv.vitamui.cas.util.Constants;
import lombok.val;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.flow.action.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * CUSTO: Full rewrite of {@link SurrogateInitialAuthenticationAction}
 */
public class CustomSurrogateInitialAuthenticationAction extends BaseCasWebflowAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomSurrogateInitialAuthenticationAction.class);

    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        val up = WebUtils.getCredential(context, UsernamePasswordCredential.class);
        if (up == null) {
            LOGGER.debug(
                "Provided credentials cannot be found, or are already of type [{}]",
                SurrogateUsernamePasswordCredential.class.getName()
            );
            return null;
        }

        val flowScope = context.getFlowScope();
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
