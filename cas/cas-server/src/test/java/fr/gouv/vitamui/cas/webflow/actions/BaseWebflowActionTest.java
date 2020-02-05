package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockParameterMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A base webflow action test.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public abstract class BaseWebflowActionTest {

    protected RequestContext context;

    protected MockParameterMap requestParameters;

    protected MutableAttributeMap<Object> flowParameters;

    protected HttpSession session;

    @Before
    public void setUp() {
        context = mock(RequestContext.class);

        requestParameters = new MockParameterMap();
        when(context.getRequestParameters()).thenReturn(requestParameters);

        flowParameters = new LocalAttributeMap<>();
        when(context.getFlowScope()).thenReturn(flowParameters);
        when(context.getRequestScope()).thenReturn(flowParameters);
        when(context.getConversationScope()).thenReturn(flowParameters);

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(context.getExternalContext()).thenReturn(servletExternalContext);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(request);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(servletExternalContext.getNativeResponse()).thenReturn(response);

        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        final FlowExecutionContext flowContext = mock(FlowExecutionContext.class);
        when(context.getFlowExecutionContext()).thenReturn(flowContext);
        final FlowSession flowSession = mock(FlowSession.class);
        when(flowContext.getActiveSession()).thenReturn(flowSession);
        when(flowSession.getScope()).thenReturn(new LocalAttributeMap<>());
    }
}
