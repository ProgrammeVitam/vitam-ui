package fr.gouv.vitamui.cas.webflow.actions;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * An always "success" action.
 */
public class AlwaysSuccessAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return success();
    }
}
